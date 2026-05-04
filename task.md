# Task: Wire Real Machine Data into the UI

## Goal
Replace all hardcoded/mock machine cards in the UI (`Dashboard.jsx`) with live data from the AGV, Warehouse, and Assembly Station registries. Start with the machine pool cards; production line panels come later.

## Confirmed Architecture Facts

### What's actually implemented (CLAUDE.md is outdated)
- **AssemblyController** (`assemblystation/src/main/java/dk/sdu/st4/assemblystation/AssemblyController.java`) — fully implemented MQTT client. Subscribes to `emulator/status`, `emulator/checkhealth`, `emulator/operation`. Stores state in `AssemblyModel` (in-memory, no network call to read).
- **WarehouseService** (`warehouse/src/main/java/dk/sdu/st4/warehouse/service/WarehouseService.java`) — SOAP client via CXF. `GetState(machineID)` calls `proxy.getInventory()`, parses the JSON response and returns the `State` int (0=IDLE, 1=EXECUTING, 2=ERROR). `GetInventory(machineID)` returns void (discards result). No stored inventory object.
- **AgvServiceImpl** — REST client, `getStatus()` calls AGV HTTP endpoint → returns `AgvStatus` (battery, state, programName, timestamp).

### Registry internals (all three follow same pattern)
- `private final Map<String, ServiceImpl> services` — all connected machines (both idle and active)
- `private final Queue<String> available` — idle machine serial numbers
- `private final Set<String> active` — in-use machine serial numbers
- `getAvailable()` — returns copy of available set only
- `acquire()` / `release()` — pool management
- `loadFromDb()` — loads all machines of that type from PostgreSQL `machines` table

### Current API endpoints (ApiServer.java)
- `GET /api/status` → `{"agv": {...}, "lineStatus": "..."}` — only active AGV data
- `GET /api/events` → array of 20 log entries
- `POST /api/control` → start/pause/stop/abort

### ProductionOrchestrator
- Has references to `agvRegistry`, `warehouseRegistry`, `assemblyRegistry`
- Already exposes: `agvJson()`, `getLineStatus()`, `getEvents()`
- Does NOT currently expose per-machine pool status

---

## Plan

### Step 1: Add pool-info methods to each registry

**AgvRegistry** — add:
```java
/** Returns all serials → "idle" or "active" */
public Map<String, String> getPoolInfo() {
    Map<String, String> info = new LinkedHashMap<>();
    for (String sn : services.keySet()) {
        info.put(sn, active.contains(sn) ? "active" : "idle");
    }
    return info;
}
```

**WarehouseRegistry** — add:
```java
/** Returns all serials → {poolStatus, warehouseState int} */
public Map<String, String[]> getPoolInfo() {
    Map<String, String[]> info = new LinkedHashMap<>();
    for (Map.Entry<String, WarehouseService> e : services.entrySet()) {
        String sn = e.getKey();
        String poolStatus = active.contains(sn) ? "active" : "idle";
        String stateStr;
        try {
            stateStr = String.valueOf(e.getValue().GetState(sn));
        } catch (Exception ex) {
            stateStr = "-1";
        }
        info.put(sn, new String[]{poolStatus, stateStr});
    }
    return info;
}
```

**AssemblyRegistry** — add:
```java
/** Returns all serials → {poolStatus, state, healthy, operationId, lastOperationId} */
public Map<String, Object[]> getPoolInfo() {
    Map<String, Object[]> info = new LinkedHashMap<>();
    for (Map.Entry<String, AssemblyServiceImpl> e : services.entrySet()) {
        String sn = e.getKey();
        String poolStatus = active.contains(sn) ? "active" : "idle";
        AssemblyServiceImpl svc = e.getValue();
        try {
            info.put(sn, new Object[]{
                poolStatus,
                svc.getStatus(),      // int 0/1/2
                svc.getHealth(),      // boolean
                svc.getOperation(),   // int
                svc.getLastOperationId() // int
            });
        } catch (Exception ex) {
            info.put(sn, new Object[]{poolStatus, -1, false, -1, -1});
        }
    }
    return info;
}
```

Note: `AssemblyServiceImpl` wraps `AssemblyController`. The `getStatus()`, `getHealth()`, `getOperation()` just read from `AssemblyModel` (no network call). Safe to call on every request.

`WarehouseService.GetState()` makes a SOAP call — acceptable overhead for 5 machines polled every 2s.

`AgvServiceImpl.getStatus()` makes an HTTP call — only include it for **active** AGVs (to avoid 5 HTTP calls every 2s for idle ones). For idle AGVs just report battery/state as null/unknown.

### Step 2: Add `machinesJson()` to ProductionOrchestrator

Build and return a JSON string:
```json
{
  "agv": [
    {"serialNumber": "AGV-001", "poolStatus": "idle|active", "battery": null, "agvState": null, "program": null},
    ...
  ],
  "warehouse": [
    {"serialNumber": "WH-001", "poolStatus": "idle|active", "warehouseState": 0},
    ...
  ],
  "assembly": [
    {"serialNumber": "AS-001", "poolStatus": "idle|active", "state": 0, "healthy": true, "operationId": 0, "lastOperationId": 0},
    ...
  ]
}
```

For AGV: active machines → call `agv.getStatus()` (already polled in cycle, so fresh); idle → battery/state/program = null (avoid extra HTTP calls).

Use `JsonUtil` or manual `StringBuilder` (same pattern as existing `agvJson()`).

**File:** `app/src/main/java/dk/sdu/st4/app/ProductionOrchestrator.java`

### Step 3: Add `GET /api/machines` to ApiServer

```java
server.createContext("/api/machines", exchange -> {
    if (handleCors(exchange)) return;
    String json = orchestrator.machinesJson();
    sendJson(exchange, 200, json);
});
```

**File:** `core/src/main/java/dk/sdu/st4/core/server/ApiServer.java`

### Step 4: Update Dashboard.jsx

Replace the hardcoded `MACHINE_POOL` constant with a state variable, populated by polling `GET /api/machines` every 2 seconds (same cadence as existing `/api/status` and `/api/events` polls).

**Mapping:**
- AGV card: serialNumber, poolStatus (idle/active), battery (null → "—"), agvState
- Warehouse card: serialNumber, poolStatus, warehouseState (0=IDLE, 1=EXECUTING, 2=ERROR)
- Assembly card: serialNumber, poolStatus, state (0=IDLE,1=EXECUTING,2=ERROR), healthy, operationId, lastOperationId

Keep the existing mock data as a **fallback** when backend is offline (same pattern the dashboard already uses for `/api/status`).

**File:** `core/src/main/java/dk/sdu/st4/core/ui/Dashboard.jsx`

---

## Files to Modify (in order)

| # | File | Change |
|---|------|--------|
| 1 | `app/.../Registries/AgvRegistry.java` | Add `getPoolInfo()` → `Map<String, String>` |
| 2 | `app/.../Registries/WarehouseRegistry.java` | Add `getPoolInfo()` → `Map<String, String[]>` |
| 3 | `app/.../Registries/AssemblyRegistry.java` | Add `getPoolInfo()` → `Map<String, Object[]>` |
| 4 | `app/.../ProductionOrchestrator.java` | Add `machinesJson()` using the three registries |
| 5 | `core/.../server/ApiServer.java` | Add `GET /api/machines` route |
| 6 | `core/.../ui/Dashboard.jsx` | Poll `/api/machines`, replace `MACHINE_POOL` with live state |

---

## Known Constraints
- `common` has no `module-info.java` — named modules access it via unnamed module. Do NOT add `requires dk.sdu.st4.common` to any module-info. Use `--add-reads` in pom.xml (already configured).
- Warehouse SOAP calls require Docker running. If Docker is down, `GetState()` throws — catch and return -1/error state.
- Assembly MQTT data is only fresh once a status message arrives after connect. Before first message: state=0, healthy=false, operationId=0. This is fine.
- `AgvRegistry.services` is private — must add accessor method inside AgvRegistry itself (don't make it public).

## Status
- [x] Step 1a: AgvRegistry.getPoolInfo()
- [x] Step 1b: WarehouseRegistry.getPoolInfo()
- [x] Step 1c: AssemblyRegistry.getPoolInfo()
- [x] Step 2: ProductionOrchestrator.machinesJson()
- [x] Step 3: ApiServer GET /api/machines
- [x] Step 4: Dashboard.jsx live machine pool — all mock data removed, location fields removed, null/-1 fallback

## Not in Scope (yet)
- Production line panel wiring (Line-01, Line-02, etc.)
- Employees panel
- Task builder palette
- CLAUDE.md update (update at the end once all changes are done)
