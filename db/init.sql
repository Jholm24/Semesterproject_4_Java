CREATE TABLE IF NOT EXISTS machines (
serial_no   VARCHAR(9)      PRIMARY KEY NOT NULL,
type        VARCHAR(32)     NOT NULL,
variant     VARCHAR(32),
base_url    VARCHAR(256)    UNIQUE,
created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO machines (serial_no, type, variant, base_url) VALUES
('AG-294751', 'AGV',              '',          'http://localhost:8082/v1/status/'),
('AG-183640', 'AGV',              '',          'http://localhost:8083/v1/status/'),
('AG-572913', 'AGV',              '',          'http://localhost:8084/v1/status/'),
('AG-846203', 'AGV',              '',          'http://localhost:8085/v1/status/'),
('AG-731485', 'AGV',              '',          'http://localhost:8086/v1/status/'),
('WH-P13752', 'WAREHOUSE',        'parts',     'http://localhost:8087/Service.asmx'),
('WH-P47281', 'WAREHOUSE',        'parts',     'http://localhost:8088/Service.asmx'),
('WH-A64819', 'WAREHOUSE',        'accepted',  'http://localhost:8089/Service.asmx'),
('WH-A29047', 'WAREHOUSE',        'accepted',  'http://localhost:8090/Service.asmx'),
('WH-D81234', 'WAREHOUSE',        'defect',    'http://localhost:8091/Service.asmx'),
('WH-D02765', 'WAREHOUSE',        'defect',    'http://localhost:8092/Service.asmx'),
('AS-739281', 'ASSEMBLY_STATION', '',          NULL),
('AS-516403', 'ASSEMBLY_STATION', '',          NULL),
('AS-284971', 'ASSEMBLY_STATION', '',          NULL),
('AS-653820', 'ASSEMBLY_STATION', '',          NULL),
('AS-917346', 'ASSEMBLY_STATION', '',          NULL);
