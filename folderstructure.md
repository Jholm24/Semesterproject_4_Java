# Project Folder Structure

**ST4 — Production Line Integration**
JPMS Maven multi-module project (Java 17, groupId `dk.sdu.st4`)

```
Semesterproject_4_Java/
├── docker-compose.yml                          # Spins up MQTT, AGV, Warehouse, AssemblyStation, PostgreSQL
├── pom.xml                                     # Parent POM — module aggregator, dependency management
│
├── Assets/
│   ├── ProjectDescription.pdf
│   ├── Proposal.docx
│   └── TechnicalDocumentation.pdf
│
├── core/                                       # module: dk.sdu.st4.core
│   ├── pom.xml
│   └── src/main/java/
│       ├── module-info.java
│       └── dk/sdu/st4/core/
│           ├── enums/
│           │   ├── AgvProgram.java             # REST program names (MoveToAssemblyOperation, etc.)
│           │   ├── AgvState.java               # IDLE(1), EXECUTING(2), CHARGING(3)
│           │   ├── AssemblyState.java          # IDLE(0), EXECUTING(1), ERROR(2)
│           │   └── WarehouseState.java         # IDLE(0), EXECUTING(1), ERROR(2)
│           ├── exception/
│           │   ├── St4Exception.java           # Base checked exception
│           │   ├── AgvException.java
│           │   ├── AssemblyStationException.java
│           │   └── WarehouseException.java
│           ├── model/
│           │   ├── AgvStatus.java              # battery, programName, state, timestamp
│           │   ├── AssemblyStatus.java         # lastOperation, currentOperation, state, timestamp
│           │   ├── HealthCheckResult.java      # healthy, processId, timestamp
│           │   └── WarehouseInventory.java     # inventory (Map<Integer,String>), state, timestamp
│           └── service/
│               ├── IAgvService.java            # loadProgram(), executeProgram(), getStatus()
│               ├── IAssemblyStationService.java# startOperation(), subscribeToStatus/Health(), disconnect()
│               └── IWarehouseService.java      # pickItem(), insertItem(), getInventory()
│
├── common/                                     # module: dk.sdu.st4.common
│   ├── pom.xml                                 # depends on: core, jackson-databind
│   └── src/main/java/
│       ├── module-info.java
│       └── dk/sdu/st4/common/
│           ├── config/
│           │   └── AppConfig.java              # Endpoint URLs, MQTT topics, port constants
│           └── util/
│               └── JsonUtil.java               # Jackson toJson() / fromJson() wrapper
│
├── agv/                                        # module: dk.sdu.st4.agv
│   ├── pom.xml                                 # depends on: core, common
│   └── src/main/java/
│       ├── module-info.java                    # requires java.net.http
│       └── dk/sdu/st4/agv/
│           ├── client/
│           │   └── AgvClient.java              # HTTP GET (getStatus) + PUT (sendPut) via java.net.http
│           └── service/
│               └── AgvServiceImpl.java         # implements IAgvService
│
├── warehouse/                                  # module: dk.sdu.st4.warehouse
│   ├── pom.xml                                 # depends on: core, common
│   └── src/main/java/
│       ├── module-info.java                    # requires java.net.http, java.xml
│       └── dk/sdu/st4/warehouse/
│           ├── client/
│           │   └── dk.sdu.st4.warehouse.service.WarehouseClient.java        # Manual SOAP envelope builder + HTTP POST
│           └── service/
│               └── WarehouseServiceImpl.java   # implements IWarehouseService
│
├── assemblystation/                            # module: dk.sdu.st4.assemblystation
│   ├── pom.xml                                 # depends on: core, common, paho-mqttv3
│   └── src/main/java/
│       ├── module-info.java                    # requires org.eclipse.paho.client.mqttv3
│       └── dk/sdu/st4/assemblystation/
│           ├── client/
│           │   └── AssemblyStationClient.java  # Paho MqttClient — connect/disconnect/publish/subscribe
│           └── service/
│               └── AssemblyStationServiceImpl.java  # implements IAssemblyStationService
│
└── app/                                        # module: dk.sdu.st4.app
    ├── pom.xml                                 # depends on: all modules above
    └── src/main/java/
        ├── module-info.java
        └── dk/sdu/st4/app/
            ├── Main.java                       # Entry point — wires services, starts production cycle
            └── orchestration/
                └── ProductionOrchestrator.java # Coordinates full cycle across all three assets
```

---

## Module dependency graph

```
app
 ├── agv           ──┐
 ├── warehouse     ──┤──> common ──> core
 └── assemblystation┘
```

## External dependencies

| Library | Version | Used by |
|---|---|---|
| `com.fasterxml.jackson.core:jackson-databind` | 2.17.0 | common |
| `org.eclipse.paho:org.eclipse.paho.client.mqttv3` | 1.2.5 | assemblystation |
| `java.net.http` *(JDK built-in)* | Java 17 | agv, warehouse |
| `java.xml` *(JDK built-in)* | Java 17 | warehouse |

## Docker services (docker-compose.yml)

| Service | Image | Port |
|---|---|---|
| mqtt | thmork/st4-mqtt:latest | 1883 (TCP), 9001 (WS) |
| st4-agv | thmork/st4-agv:latest | 8082 |
| st4-warehouse | thmork/st4-warehouse:latest | 8081 |
| st4-assemblystation | thmork/st4-assemblystation:latest | — (connects to mqtt internally) |
| db | postgres:16 | 5432 |
