CREATE TABLE IF NOT EXISTS machines (
    machineId          SERIAL       PRIMARY KEY,
    type        VARCHAR(32)  NOT NULL,
    variant     VARCHAR(32),
    base_url    VARCHAR(256),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO machines (type, variant, base_url) VALUES
    ('AGV',               '',           'http://localhost:8082/v1/status/'),
    ('AGV',               '',           'http://localhost:8083/v1/status/'),
    ('AGV',               '',           'http://localhost:8084/v1/status/'),
    ('AGV',               '',           'http://localhost:8085/v1/status/'),
    ('AGV',               '',           'http://localhost:8086/v1/status/'),
    ('WAREHOUSE',         'parts',     'http://localhost:8087/Service.asmx'),
    ('WAREHOUSE',         'parts',     'http://localhost:8088/Service.asmx'),
    ('WAREHOUSE',         'accepted',     'http://localhost:8089/Service.asmx'),
    ('WAREHOUSE',         'accepted',     'http://localhost:8090/Service.asmx'),
    ('WAREHOUSE',         'defect',     'http://localhost:8091/Service.asmx'),
    ('WAREHOUSE',         'defect',     'http://localhost:8092/Service.asmx'),
    ('ASSEMBLY_STATION',  '', NULL),
    ('ASSEMBLY_STATION',  '', NULL),
    ('ASSEMBLY_STATION',  '', NULL),
    ('ASSEMBLY_STATION',  '', NULL),
    ('ASSEMBLY_STATION',  '', NULL)
