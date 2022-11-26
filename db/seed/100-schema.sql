CREATE SCHEMA PC_LOAD_LETTER;

SET
search_path TO PC_LOAD_LETTER;

-- A table to hold a ton of data.
CREATE TABLE SOURCE
(
    ID INTEGER NOT NULL PRIMARY KEY,
    TEXT_VAL TEXT NOT NULL
);

-- A table to demonstrate record lockikng.
CREATE TABLE LOCKING
(
    ID INTEGER NOT NULL PRIMARY KEY,
    LOCKED_VAL BOOLEAN NOT NULL DEFAULT FALSE,
    OWNER VARCHAR(100)
);