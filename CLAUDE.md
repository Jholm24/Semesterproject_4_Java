# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ST4 — Production Line Integration** (SDU 4th Semester Project)

Java 17 JPMS Maven multi-module project integrating three Industry 4.0 assets:
- **Warehouse** — SOAP via Apache CXF JAX-WS (port 8081)
- **AGV** (Automated Guided Vehicle) — REST via `java.net.http` (port 8082)
- **Assembly Station** — MQTT via Eclipse Paho (port 1883)

## Commands

### Build
```bash
mvn clean install
```
> **Warning:** The `warehouse` module fetches the WSDL from `http://localhost:8081/Service.asmx?wsdl` during the `generate-sources` phase. Docker must be running before building warehouse or running `mvn clean install`.

### Start Docker services
```bash
docker compose up -d
```

### Run tests for a single module
```bash
mvn test -pl agv          # uses WireMock — no Docker needed
mvn test -pl warehouse
mvn test -pl assemblystation
```

> `AgvIntegrationTest` hits the real AGV container and requires Docker. `AgvServiceImplTest` uses WireMock and runs standalone.

### Run the application (HTTP server + UI)
```bash
docker compose up -d          # AGV emulator on :8082, Warehouse on :8081
mvn install -pl core,agv,common  # build first (skip warehouse to avoid needing Docker WSDL)
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
app  (does not exist yet — must be created)
 ├── agv           ──┐
 ├── warehouse     ──┤──> common (no module-info) ──> core
 └── assemblystation┘
```

### Module responsibilities

| Module | Package | Role |
|---|---|---|
| `core` | `dk.sdu.st4.core` | Domain layer — models, enums. Also contains the JavaFX UI sources in `core/ui/`. No service interfaces here. |
| `common` | `dk.sdu.st4.common` | No `module-info.java`. Contains `AppConfig` (endpoint/topic constants), `JsonUtil` (Jackson wrapper — **fully implemented**), and service interfaces in `common.Interfaces` (`IAgv`, `IWarehouse`, `IConnect`). |
| `agv` | `dk.sdu.st4.agv` | `AgvClient` (HTTP GET/PUT via `java.net.http`) + `AgvServiceImpl` implements `IAgv`. |
| `warehouse` | `dk.sdu.st4.warehouse` | `WarehouseClient` (in default package) implements `IWarehouse` using Apache CXF JAX-WS stubs generated from the live WSDL at build time. |
| `assemblystation` | `dk.sdu.st4.assemblystation` | Only `module-info.java` exists — client and service implementation not yet written. |
| `app` | `dk.sdu.st4.app` | **Does not exist.** Must be created as a Maven module, added to the parent `pom.xml` `<modules>` list, and contain `Main` + `ProductionOrchestrator`. |

### JPMS / `common` library constraint
`common` has no `module-info.java`. Named modules access it via the unnamed module. Each named module that uses `common` must have in its `pom.xml`:
```xml
<compilerArgs>
    <arg>--add-reads</arg>
    <arg>dk.sdu.st4.<module>=ALL-UNNAMED</arg>
</compilerArgs>
```
Do **not** add `requires dk.sdu.st4.common` to any `module-info.java`. Tests run off the classpath (`<useModulePath>false</useModulePath>`) to avoid JPMS friction with test dependencies.

### Service interfaces
All service interfaces live in `common/Interfaces/`, **not** `core/service/`:
- `IAgv` — `loadProgram(AgvProgram)`, `executeProgram()`, `getStatus()`
- `IWarehouse` — `PickItem(int)`, `InsertItem(int, String)`, `GetInventory()`, `GetState()`
- `IConnect` — machine connection lifecycle (add/remove/connect/disconnect)

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
`AgvStatus` is already annotated. The AGV REST API uses **lowercase** keys (not PascalCase):

| Java field | `@JsonProperty` value |
|---|---|
| `battery` | `"battery"` |
| `programName` | `"program name"` |
| `state` | `"state"` |
| `timestamp` | `"timestamp"` |

`AgvState.fromState(int)` already has `@JsonCreator` for integer deserialisation.

`AgvProgram.getProgram()` returns the full description string used as the `"Program name"` value in PUT request bodies.

### AGV operation pattern
Two-step: `loadProgram()` then `executeProgram()`, poll `getStatus()` until `AgvState.Idle` before the next command.

`AgvState` constants are PascalCase: `Idle` (1), `Executing` (2), `Charging` (3).  
`AssemblyState` constants are UPPER_CASE: `IDLE` (0), `EXECUTING` (1), `ERROR` (2).

### Warehouse SOAP client
`WarehouseClient` (in the default package in `warehouse/src/main/java/`) is generated/backed by Apache CXF. The CXF codegen plugin runs `wsdl2java` against the live endpoint during `generate-sources`, placing stubs in `dk.sdu.st4.warehouse.service`. This means **Docker must be running** when building the `warehouse` module from scratch.

### Production cycle (to implement in `ProductionOrchestrator`)
1. Warehouse picks tray → part moved to outlet
2. AGV: `MoveToStorageOperation` → `PickWarehouseOperation` → `MoveToAssemblyOperation` → `PutAssemblyOperation`
3. Assembly station: publish `startOperation(processId)`, await `emulator/checkhealth` callback
4. AGV: `PickAssemblyOperation` → `MoveToStorageOperation` → `PutWarehouseOperation`
5. Warehouse inserts assembled item

### Error handling
Interface methods declare `throws Exception`. No custom exception classes — use `Exception` directly and propagate.

### Docker services
| Service | Port |
|---|---|
| MQTT broker | 1883 (TCP), 9001 (WS) |
| AGV emulator | 8082 |
| Warehouse emulator | 8081 |
| Assembly station emulator | — (internal MQTT only) |
| PostgreSQL | 5432 (`skateboardas`/`skateboardas`) |