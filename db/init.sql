-- ── UI data tables ──────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS production_lines (
    id           VARCHAR(32)    PRIMARY KEY,
    name         VARCHAR(128)   NOT NULL,
    product      VARCHAR(128)   NOT NULL DEFAULT '—',
    status       VARCHAR(32)    NOT NULL DEFAULT 'standby',
    cycles       INT            NOT NULL DEFAULT 0,
    success_rate NUMERIC(5,2)   NOT NULL DEFAULT 0,
    warnings     INT            NOT NULL DEFAULT 0,
    created_at   TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id             VARCHAR(32)   PRIMARY KEY,
    name           VARCHAR(128)  NOT NULL,
    username       VARCHAR(64),
    role           VARCHAR(32)   NOT NULL DEFAULT 'operator',
    pic            VARCHAR(4),
    since          VARCHAR(7),
    password_plain VARCHAR(256),
    created_at     TIMESTAMP              DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_templates (
    id         SERIAL        PRIMARY KEY,
    line_id    VARCHAR(32)   NOT NULL REFERENCES production_lines(id) ON DELETE CASCADE,
    name       VARCHAR(128)  NOT NULL,
    seq        TEXT          NOT NULL,
    created_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP
);

-- ── Machine registry ─────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS machines (
serial_no   VARCHAR(9)      PRIMARY KEY,
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

-- ── Join tables (depend on both machines and production_lines) ───────────────

CREATE TABLE IF NOT EXISTS line_machines (
    line_id   VARCHAR(32)  NOT NULL REFERENCES production_lines(id) ON DELETE CASCADE,
    serial_no VARCHAR(9)   NOT NULL REFERENCES machines(serial_no)  ON DELETE CASCADE,
    PRIMARY KEY (line_id, serial_no)
);

CREATE TABLE IF NOT EXISTS line_employees (
    line_id     VARCHAR(32)  NOT NULL REFERENCES production_lines(id) ON DELETE CASCADE,
    employee_id VARCHAR(32)  NOT NULL REFERENCES employees(id)        ON DELETE CASCADE,
    PRIMARY KEY (line_id, employee_id)
);

-- ── Seed data ────────────────────────────────────────────────────────────────

INSERT INTO production_lines (id, name, product, status, cycles, success_rate, warnings) VALUES
('line-1', 'Line-01 · Skateboard',    'Neo X5',              'standby',   0,  0.0, 0)
ON CONFLICT DO NOTHING;

INSERT INTO line_machines (line_id, serial_no) VALUES
('line-1', 'WH-P13752'), ('line-1', 'AG-294751'), ('line-1', 'AS-739281'),
ON CONFLICT DO NOTHING;

INSERT INTO employees (id, name, username, role, pic, since, password_plain) VALUES
('e1', 'Lars Ottesen',   'lottesen',   'operator', 'LO', '2023-08', 'pass123'),
('e2', 'Mette Nørgaard', 'mnoergaard', 'operator', 'MN', '2024-01', 'pass123'),
('e3', 'Anders Kjær',    'akjaer',     'operator', 'AK', '2022-11', 'pass123'),
('e4', 'Sofie Bech',     'sbech',      'operator', 'SB', '2025-02', 'pass123'),
('e5', 'Mikkel Hansen',  'mhansen',    'manager',  'MH', '2021-05', 'pass123'),
('e6', 'Johan Vest',     'jvest',      'operator', 'JV', '2023-03', 'pass123')
ON CONFLICT DO NOTHING;

INSERT INTO line_employees (line_id, employee_id) VALUES
('line-1', 'e1'), ('line-1', 'e3'),
('line-2', 'e2'),
('line-3', 'e4')
ON CONFLICT DO NOTHING;