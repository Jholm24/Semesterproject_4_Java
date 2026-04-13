# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ST4 — Production Line Integration** (SDU 4th Semester Project)

Java 17 JPMS Maven multi-module project integrating three Industry 4.0 assets:
- **Warehouse** — communicated via SOAP (port 8081)
- **AGV** (Automated Guided Vehicle) — communicated via REST (port 8082)
- **Assembly Station** — communicated via MQTT (port 1883)

The project is currently scaffolded; most business logic is stubbed with `TODO` comments and `UnsupportedOperationException`. The primary task is implementing `ProductionOrchestrator` and `Main`.

## Commands

### Build
```bash
mvn clean install
```

### Run the application
```bash
# Start Docker services first
docker compose up -d

# Run via Maven or directly
java --module-path target/modules --module dk.sdu.st4.app/dk.sdu.st4.app.Main
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
 ├── warehouse     ──┤──> common ──> core
 └── assemblystation┘
```

### Module responsibilities

| Module | Package | Role |
|---|---|---|
| `core` | `dk.sdu.st4.core` | Pure domain layer — interfaces (`IAgvService`, `IWarehouseService`, `IAssemblyStationService`), models (`AgvStatus`, `AssemblyStatus`, `HealthCheckResult`, `WarehouseInventory`), enums, and checked exceptions. No external dependencies. |
| `common` | `dk.sdu.st4.common` | Shared utilities — `AppConfig` (all endpoint URLs/ports/topics as constants), `JsonUtil` (Jackson wrapper). |
| `agv` | `dk.sdu.st4.agv` | REST client (`AgvClient` via `java.net.http`) + `AgvServiceImpl`. Two-step pattern: `loadProgram()` then `executeProgram()`, poll `getStatus()` until `IDLE`. |
| `warehouse` | `dk.sdu.st4.warehouse` | SOAP client (`WarehouseClient` — manually builds envelopes, uses `java.xml`) + `WarehouseServiceImpl`. |
| `assemblystation` | `dk.sdu.st4.assemblystation` | MQTT client (`AssemblyStationClient` using Eclipse Paho) + `AssemblyStationServiceImpl`. Publish to `emulator/operation`, subscribe to `emulator/status` and `emulator/checkhealth`. |
| `app` | `dk.sdu.st4.app` | Entry point (`Main`) and `ProductionOrchestrator` — the only place where all services are wired together. |

### Production cycle (to implement in `ProductionOrchestrator`)
1. Warehouse picks tray → part moved to outlet
2. AGV: `MOVE_TO_STORAGE` → `PICK_WAREHOUSE` → `MOVE_TO_ASSEMBLY` → `PUT_ASSEMBLY` (each step: load, execute, poll until IDLE)
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

### AGV state machine
- `State 1` (`AGV_LOAD_STATE`) — load/stage a program
- `State 2` (`AGV_EXECUTE_STATE`) — execute the staged program
- Poll `getStatus()` until `AgvState.IDLE` before issuing the next command

### Docker services
All external dependencies run via Docker Compose: MQTT broker, AGV emulator, Warehouse emulator, Assembly Station emulator, and PostgreSQL (port 5432, credentials: `skateboardas`/`skateboardas`).
