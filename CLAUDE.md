# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ST4 — Production Line Integration** (SDU 4th Semester Project)

Java 17 JPMS Maven multi-module project integrating three Industry 4.0 assets:
- **Warehouse** — communicated via SOAP (port 8081)
- **AGV** (Automated Guided Vehicle) — communicated via REST (port 8082)
- **Assembly Station** — communicated via MQTT (port 1883)

Most business logic is stubbed with `TODO` comments and `UnsupportedOperationException`. The primary task is implementing `ProductionOrchestrator` and `Main` in the `app` module (which does not yet exist and must be created).

## Commands

### Build
```bash
mvn clean install
```

### Run the application
```bash
# Start Docker services first
docker compose up -d

# common has no module-info.java so --add-reads bridges it to named modules at runtime
java --module-path target/modules \
     --add-reads dk.sdu.st4.agv=ALL-UNNAMED \
     --add-reads dk.sdu.st4.warehouse=ALL-UNNAMED \
     --add-reads dk.sdu.st4.assemblystation=ALL-UNNAMED \
     --add-reads dk.sdu.st4.app=ALL-UNNAMED \
     --module dk.sdu.st4.app/dk.sdu.st4.app.Main
```

### Run a single module's tests
```bash
mvn test -pl agv
mvn test -pl warehouse
mvn test -pl assemblystation
```

## Architecture

### Module dependency graph
```
app
 ├── agv           ──┐
 ├── warehouse     ──┤──> common (plain library, no module-info) ──> core
 └── assemblystation┘
```

### Module responsibilities

| Module | Package | Role |
|---|---|---|
| `core` | `dk.sdu.st4.core` | Pure domain layer — models (`AgvStatus`, `AssemblyStatus`, `HealthCheckResult`, `WarehouseInventory`) and enums only. No implementations live here. |
| `common` | `dk.sdu.st4.common` | Plain library (no `module-info.java`). Contains `AppConfig` (all endpoint/topic constants), `JsonUtil` (Jackson wrapper, **currently fully stubbed**), and service interfaces (`IAgv` in `common.Interfaces`). Named modules access it via `--add-reads …=ALL-UNNAMED` at both compile time (Maven compiler plugin) and runtime. |
| `agv` | `dk.sdu.st4.agv` | REST client (`AgvClient` via `java.net.http`) + `AgvServiceImpl`. Two-step pattern: `loadProgram()` then `executeProgram()`, poll `getStatus()` until `Idle`. |
| `warehouse` | `dk.sdu.st4.warehouse` | SOAP client (`WarehouseClient` — manually builds envelopes, uses `java.xml`) + `WarehouseServiceImpl`. |
| `assemblystation` | `dk.sdu.st4.assemblystation` | MQTT client (`AssemblyStationClient` using Eclipse Paho) + `AssemblyStationServiceImpl`. Publish to `emulator/operation`, subscribe to `emulator/status` and `emulator/checkhealth`. |
| `app` | `dk.sdu.st4.app` | **Does not exist yet.** Entry point (`Main`) and `ProductionOrchestrator` — the only place where all services are wired together. Must be added as a Maven module **and** registered in the parent `pom.xml` `<modules>` list. |

**Key constraint:** Service implementations (`AgvServiceImpl`, etc.) must stay in their own module (`agv`, `warehouse`, `assemblystation`) — never in `core` or `common`. They depend on their module's client class (e.g. `AgvClient`), and moving them to `core` would invert the dependency graph (`core → agv`) creating a cycle.

### Implementation order (stub dependencies)

Everything blocks on `JsonUtil` — it is fully stubbed with `UnsupportedOperationException`. Implement in this order:
1. `JsonUtil.toJson()` / `JsonUtil.fromJson()` — unblocks all JSON serialisation
2. `@JsonProperty` annotations on `AgvStatus` fields (see table below)
3. `@JsonCreator` / `fromState()` hook on `AgvState` for integer deserialisation
4. Service interfaces in `core/service/` (`IAgvService`, `IAssemblyStationService`, `IWarehouseService`)
5. Service implementations (`AgvServiceImpl`, etc.)
6. `app` module with `Main` + `ProductionOrchestrator`

### Production cycle (to implement in `ProductionOrchestrator`)
1. Warehouse picks tray → part moved to outlet
2. AGV: `MOVE_TO_STORAGE` → `PICK_WAREHOUSE` → `MOVE_TO_ASSEMBLY` → `PUT_ASSEMBLY` (each step: load, execute, poll until Idle)
3. Assembly station: publish `startOperation(processId)`, await `emulator/checkhealth` callback
4. AGV: `PICK_ASSEMBLY` → `MOVE_TO_STORAGE` → `PUT_WAREHOUSE`
5. Warehouse inserts assembled item

### Key constants (`AppConfig`)
- `AGV_BASE_URL` = `http://localhost:8082/v1/status/`
- `WAREHOUSE_SERVICE_URL` = `http://localhost:8081/Service.asmx` (SOAP namespace: `http://tempuri.org/`)
- `MQTT_BROKER_URL` = `tcp://localhost:1883`
- `MQTT_TOPIC_OPERATION` = `emulator/operation`
- `MQTT_TOPIC_STATUS` = `emulator/status`
- `MQTT_TOPIC_HEALTH` = `emulator/checkhealth`
- `MQTT_UNHEALTHY_PROCESS_ID` = `9999` (triggers error path for testing)
- `AGV_LOAD_STATE` = `1`, `AGV_EXECUTE_STATE` = `2`

### Error handling
All interface methods (`IAgv`, and any future `IWarehouse`/`IAssemblyStation` equivalents) declare `throws Exception`. No custom exception classes — use `Exception` directly and let it propagate.

### AGV state machine
- `AgvState` constants use **PascalCase**: `Idle` (1), `Executing` (2), `Charging` (3) — look up via `AgvState.fromState(int)`
- `AssemblyState.IDLE` = code `0`, `EXECUTING` = `1`, `ERROR` = `2`
- Poll `getStatus()` until `AgvState.Idle` before issuing the next AGV command

### Jackson / JSON field mapping
The AGV REST API uses PascalCase and a spaced key. `AgvStatus` fields **must be annotated** with `@JsonProperty` (not yet done):

| Java field | JSON key |
|---|---|
| `battery` | `"Battery"` |
| `programName` | `"Program name"` |
| `state` | `"State"` |
| `timestamp` | `"TimeStamp"` |

`AgvState` is serialised as an integer — Jackson needs a `@JsonCreator` on `AgvState.fromState(int)` to map the raw integer to the enum. The same pattern applies to `AssemblyState`.

JSON request bodies for AGV PUT calls must be built as plain strings or via `JsonUtil.toJson()` (once implemented) — not object literals. Example: `"{\"Program name\": \"" + program.getApiName() + "\", \"State\": " + AppConfig.AGV_LOAD_STATE + "}"`.

### JPMS / `common` library constraint
`common` intentionally has no `module-info.java`. Named modules (`agv`, `assemblystation`, and eventually `app`) access it via the unnamed module. Each such module must have in its `pom.xml`:
```xml
<compilerArgs>
    <arg>--add-reads</arg>
    <arg>dk.sdu.st4.<module>=ALL-UNNAMED</arg>
</compilerArgs>
```
Do **not** add `requires dk.sdu.st4.common` to any `module-info.java`. `core` is accessed normally (`requires dk.sdu.st4.core`) since it does have a `module-info.java` and exports its packages.

### Docker services
All external dependencies run via Docker Compose: MQTT broker, AGV emulator, Warehouse emulator, Assembly Station emulator, and PostgreSQL (port 5432, credentials: `skateboardas`/`skateboardas`). The assembly station emulator connects to the MQTT broker internally; it does not expose its own port.
