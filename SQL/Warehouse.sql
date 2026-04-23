DROP TABLE IF EXISTS machines;
CREATE TABLE IF NOT EXISTS machines (
    machineSerialNumber   INT PRIMARY KEY UNIQUE NOT NULL,
    type        VARCHAR(32)  NOT NULL,
    variant     VARCHAR(32),
    base_url    VARCHAR(256),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO machines (machineSerialNumber, type, variant, base_url) VALUES
    (1,'AGV',               '',           'http://localhost:8082/v1/status/'),
    (2,'AGV',               '',           'http://localhost:8083/v1/status/'),
    (3,'AGV',               '',           'http://localhost:8084/v1/status/'),
    (4,'AGV',               '',           'http://localhost:8085/v1/status/'),
    (5,'AGV',               '',           'http://localhost:8086/v1/status/'),
    (6,'WAREHOUSE',         'parts',     'http://localhost:8087/Service.asmx'),
    (7,'WAREHOUSE',         'parts',     'http://localhost:8088/Service.asmx'),
    (8,'WAREHOUSE',         'accepted',     'http://localhost:8089/Service.asmx'),
    (9,'WAREHOUSE',         'accepted',     'http://localhost:8090/Service.asmx'),
    (10,'WAREHOUSE',         'defect',     'http://localhost:8091/Service.asmx'),
    (11,'WAREHOUSE',         'defect',     'http://localhost:8092/Service.asmx'),
    (12,'ASSEMBLY_STATION',  '', NULL),
    (13,'ASSEMBLY_STATION',  '', NULL),
    (15,'ASSEMBLY_STATION',  '', NULL),
    (16,'ASSEMBLY_STATION',  '', NULL),
    (17,'ASSEMBLY_STATION',  '', NULL)
