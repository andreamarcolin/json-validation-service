CREATE SCHEMA jvs;

CREATE TABLE json_schema (
    id        serial       PRIMARY KEY,
    schema_id varchar(255) UNIQUE NOT NULL,
    schema    jsonb
);