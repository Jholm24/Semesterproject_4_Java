# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ST4 — Production Line Integration** (SDU 4th Semester Project)

Java 17 JPMS Maven multi-module project integrating three Industry 4.0 assets:
- **Warehouse** — SOAP via Apache CXF JAX-WS (ports 8087–8092)
- **AGV** (Automated Guided Vehicle) — REST via `java.net.http` (ports 8082–8086)
- **Assembly Station** — MQTT via Eclipse Paho (broker port 1883)

## Commands

### Build
```bash
mvn clean install
```
> **Warning:** The `warehouse` module fetches the WSDL from a live warehouse emulator endpoint during the `generate-sources` phase. Docker must be running before building `warehouse` or running `mvn clean install`.

### Start Docker services
```bash
docker compose up -d
```

### Run tests for a single module
```bash
mvn test -pl agv          # uses WireMock — no Docker needed
mvn test -pl warehouse
mvn test -pl app
```

> `AgvIntegrationTest` hits the real AGV container and requires Docker. `AgvServiceImplTest` uses WireMock and runs standalone.

### Run the application (HTTP server + UI)
```bash
docker compose up -d
mvn install -pl common,agv,warehouse,assemblystation,app,core
mvn exec:java -pl core        # starts server on http://localhost:8080
```
Then open **http://localhost:8080** in a browser. The UI is served directly by the Java server.

Optional overrides:
```bash
mvn exec:java -pl core -Dserver.port=9090
mvn exec:java -pl core -Dui.path=/absolute/path/to/ui
```

## Architecture

### Module dependency graph
```
core  (entry point — Main + ApiServer + React UI)
 ├──> app   (ProductionOrchestrator + machine registries)
 │     ├──> agv           (AgvClient + AgvServiceImpl)
 │     ├──> warehouse     (WarehouseService via CXF/SOAP stubs)
 │     └──> assemblystation (AssemblyController + MQTT)
 └──> (all above) ──> common (no module-info) — interfaces, models, DB utilities, AppConfig
```

### Module responsibilities

| Module | Package | Role |
|---|---|---|
| `core` | `dk.sdu.st4.core.server` | HTTP server (`ApiServer` on port 8080) and React UI static files in `core/ui/`. Entry point is `Main`. Delegates production control to `ProductionOrchestrator` in `app`. |
| `common` | `dk.sdu.st4.common` | No `module-info.java`. Contains `AppConfig` (endpoint/topic constants), `JsonUtil` (Jackson wrapper), data models, enums, DB utilities (`DBConnection`, `DbMachineConnect`, `DbLineRepository`), and service interfaces (`IAgv`, `IWarehouse`, `IAssembly`, `IConnect`). |
| `agv` | `dk.sdu.st4.agv` | `AgvClient` (HTTP GET/PUT via `java.net.http`) + `AgvServiceImpl` implements `IAgv`. |
| `warehouse` | `dk.sdu.st4.warehouse` | `WarehouseService` in `dk.sdu.st4.warehouse.service` implements `IWarehouse` via Apache CXF JAX-WS stubs generated from the live WSDL at build time. |
| `assemblystation` | `dk.sdu.st4.assemblystation` | `AssemblyController` implements `IConnect` + `IAssembly` via Eclipse Paho MQTT. `AssemblyModel` holds broker/state/health. `AssemblyServiceImpl` wraps the controller. Subscribes to topics: `emulator/status`, `emulator/operation`, `emulator/checkhealth`, `emulator/response`. |
| `app` | `dk.sdu.st4.app` | `ProductionOrchestrator` drives the full production cycle. `AgvRegistry`, `WarehouseRegistry`, `AssemblyRegistry` manage thread-safe machine pools backed by the DB. |

### HTTP API (`ApiServer` in `core`)

| Route | Method | Description |
|---|---|---|
| `/api/status` | GET | Returns `{agv, lineStatus, cycles, fails}` — live AGV telemetry + line state |
| `/api/events` | GET | Array of `{t, lvl, m}` log entries (max 20, newest first) |
| `/api/control` | POST | Accepts `{action: "start"\|"pause"\|"stop"\|"abort"}` |
| `/api/machines` | GET | Returns `{agv: [...], warehouse: [...], assembly: [...]}` from DB |
| `/api/lines` | GET/POST/PUT/PATCH/DELETE | Production line CRUD + status/cycle/success/warnings updates |
| `/api/employees` | GET/POST/PUT/DELETE | Employee CRUD |
| `/api/templates` | GET/POST/DELETE | Sequence template CRUD by `lineId` |
| `/*` | GET | Serves static React UI from `core/ui/` |

CORS is enabled on all endpoints.

### React UI (`core/src/main/java/dk/sdu/st4/core/ui/`)

CDN-based React 18 with in-browser Babel/JSX transpilation — no build step needed. Components: `App.jsx` (router + localStorage state), `Dashboard.jsx` (operator/manager views, live polling every 2–3 s), `Login.jsx`, `Topbar.jsx`, `Tweaks.jsx`, `Other.jsx` (task builder, employees, production lines).

`Dashboard.jsx` converts `/api/machines` responses to UI objects via `toAgvMachine`, `toWarehouseMachine`, `toAssemblyMachine` and tracks pool occupancy across production lines.

### ProductionOrchestrator (`app`) — production cycle

Manages line status (`standby`, `running`, `paused`, `stopped`, `alarm`), cycle counts, fail counts, and a ring-buffered event log (max 20 entries). Exposes these via volatile fields read by `ApiServer`.

Full production cycle per `runOneCycle()`:
1. Acquire AGV, warehouse, and assembly instances from their registries
2. `warehouse.PickItem(trayId)` → tray moved to outlet
3. AGV: `MoveToStorageOperation` → `PickWarehouseOperation` → `MoveToAssemblyOperation` → `PutAssemblyOperation`
4. Assembly: `sendOperationId(processId)` → poll for `AssemblyState.IDLE` (30 s startup + 5 min execution timeout)
5. AGV: `PickAssemblyOperation` → `MoveToStorageOperation` → `PutWarehouseOperation`
6. `warehouse.InsertItem(trayId, serialNo)`
7. Release all machines back to registries

AGV is polled every 600 ms until `AgvState.Idle` between each command.

### Machine registries (`app/Registries/`)

`AgvRegistry`, `WarehouseRegistry`, `AssemblyRegistry` each extend `DbMachineConnect` and manage thread-safe queues of idle/active machines. On startup, `Main` loads all machines from the DB `machines` table and enqueues them. `acquire()` / `release()` bracket each production cycle.

### Database layer (`common/db/`)

- `DBConnection` — singleton JDBC connection, loads credentials from environment variables or a `.env` file (searched up to 4 directories up from working directory).
- `DbMachineConnect` — reads the `machines` table (`serial_no`, `type`, `variant`, `base_url`) and instantiates the appropriate service impl.
- `DbLineRepository` — CRUD for `production_lines`, `line_machines`, `line_operators`, `employees`, `templates`.

PostgreSQL credentials: user/password/db = `skateboardas`. The `machines` table is pre-populated by `db/init.sql`.

### JPMS / `common` library constraint
`common` has no `module-info.java`. Named modules access it via the unnamed module. Each named module that uses `common` must have in its `pom.xml`:
```xml
<compilerArgs>
    <arg>--add-reads</arg>
    <arg>dk.sdu.st4.<module>=ALL-UNNAMED</arg>
</compilerArgs>
```
Do **not** add `requires dk.sdu.st4.common` to any `module-info.java`. Tests run off the classpath (`<useModulePath>false</useModulePath>`) to avoid JPMS friction with test dependencies.

### Service interfaces (`common/services/`)
- `IAgv` — `loadProgram(AgvProgram)`, `executeProgram()`, `getStatus()`
- `IWarehouse` — `PickItem(int)`, `InsertItem(int, String)`, `GetInventory()`, `GetState()`
- `IAssembly` — `sendOperationId(String)`, `getStatus()`, `getHealth()`, `getOperation()`, plus state/health/operation setters
- `IConnect` — machine connection lifecycle (`addMachine`, `removeMachine`, `connect`, `disconnect`)

### Key constants (`AppConfig`)
- `AGV_BASE_URL` = `http://localhost:8082/v1/status/`
- `WAREHOUSE_SERVICE_URL` = `http://localhost:8081/Service.asmx`
- `WAREHOUSE_SOAP_NAMESPACE` = `http://tempuri.org/`
- `MQTT_BROKER_URL` = `tcp://localhost:1883`
- `MQTT_TOPIC_OPERATION` = `emulator/operation`
- `MQTT_TOPIC_STATUS` = `emulator/status`
- `MQTT_TOPIC_HEALTH` = `emulator/checkhealth`
- `MQTT_UNHEALTHY_PROCESS_ID` = `9999`
- `AGV_LOAD_STATE` = `1`, `AGV_EXECUTE_STATE` = `2`

### Jackson / JSON field mapping
`AgvStatus` uses `@JsonProperty` for lowercase AGV REST keys:

| Java field | `@JsonProperty` value |
|---|---|
| `battery` | `"battery"` |
| `programName` | `"program name"` |
| `state` | `"state"` |
| `timestamp` | `"timestamp"` |

`AgvState.fromState(int)` has `@JsonCreator`. `AgvProgram.getProgram()` returns the description string used as `"Program name"` in PUT request bodies.

### AGV operation pattern
Two-step: `loadProgram()` then `executeProgram()`, poll `getStatus()` until `AgvState.Idle` before the next command.

`AgvState` constants are PascalCase: `Idle` (1), `Executing` (2), `Charging` (3).  
`AssemblyState` constants are UPPER_CASE: `IDLE` (0), `EXECUTING` (1), `ERROR` (2).

### Warehouse SOAP client
`WarehouseService` in `dk.sdu.st4.warehouse.service` is backed by Apache CXF. The CXF codegen plugin runs `wsdl2java` against the live warehouse endpoint during `generate-sources`. **Docker must be running** when building the `warehouse` module from scratch.

### Error handling
Interface methods declare `throws Exception`. No custom exception classes — use `Exception` directly and propagate.

### Docker services

| Service | Ports |
|---|---|
| MQTT broker | 1883 (TCP), 9001 (WS) |
| AGV emulators (1–5) | 8082–8086 |
| Warehouse emulators (1–6) | 8087–8092 |
| Assembly station emulators (1–5) | MQTT only (no exposed port) |
| PostgreSQL | 5432 (`skateboardas`/`skateboardas`) |
