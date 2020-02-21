CREATE SCHEMA jvs;

CREATE TABLE json_schema (
    id        integer      NOT NULL,
    schema_id varchar(255) UNIQUE NOT NULL,
    schema    jsonb
);