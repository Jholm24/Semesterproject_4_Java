CREATE TABLE IF NOT EXISTS machines (
    id       SERIAL       PRIMARY KEY,
    type     VARCHAR(32)  NOT NULL,
    name     VARCHAR(64)  NOT NULL UNIQUE,
    base_url VARCHAR(256)
);

INSERT INTO machines (type, name, base_url) VALUES
    ('AGV',               'st4-agv-1',           'http://localhost:8082/v1/status/'),
    ('AGV',               'st4-agv-2',           'http://localhost:8083/v1/status/'),
    ('AGV',               'st4-agv-3',           'http://localhost:8084/v1/status/'),
    ('AGV',               'st4-agv-4',           'http://localhost:8085/v1/status/'),
    ('AGV',               'st4-agv-5',           'http://localhost:8086/v1/status/'),
    ('WAREHOUSE',         'st4-warehouse-1',     'http://localhost:8087/Service.asmx'),
    ('WAREHOUSE',         'st4-warehouse-2',     'http://localhost:8088/Service.asmx'),
    ('WAREHOUSE',         'st4-warehouse-3',     'http://localhost:8089/Service.asmx'),
    ('WAREHOUSE',         'st4-warehouse-4',     'http://localhost:8090/Service.asmx'),
    ('WAREHOUSE',         'st4-warehouse-5',     'http://localhost:8091/Service.asmx'),
    ('ASSEMBLY_STATION',  'st4-assemblystation-1', NULL),
    ('ASSEMBLY_STATION',  'st4-assemblystation-2', NULL),
    ('ASSEMBLY_STATION',  'st4-assemblystation-3', NULL),
    ('ASSEMBLY_STATION',  'st4-assemblystation-4', NULL),
    ('ASSEMBLY_STATION',  'st4-assemblystation-5', NULL)
ON CONFLICT (name) DO NOTHING;
