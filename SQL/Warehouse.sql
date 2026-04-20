DROP TABLE IF EXISTS warehouse_machines;

CREATE TABLE warehouse_machines (
    machine_id INT PRIMARY KEY ,
    machine_type VARCHAR(255) NOT NULL ,
    url VARCHAR(255) NOT NULL ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


