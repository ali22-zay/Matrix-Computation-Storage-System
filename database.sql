-- ============================================================
--  OOP-based Matrix Computation & Storage System
--  Database Setup Script
--  Run this in MySQL before launching the application.
-- ============================================================

-- 1. Create the database (skip if it already exists)
CREATE DATABASE IF NOT EXISTS matrix_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE matrix_db;

-- 2. Drop old table if re-running this script
DROP TABLE IF EXISTS matrix_operations;

-- 3. Create the operations table
CREATE TABLE matrix_operations (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    operation_type VARCHAR(30)     NOT NULL COMMENT 'ADD | SUBTRACT | MULTIPLY | TRANSPOSE | DETERMINANT',
    matrix_a       TEXT            NOT NULL COMMENT 'Compact JSON-like string [[r0c0,r0c1],[r1c0,r1c1]]',
    matrix_b       TEXT                     COMMENT 'Second operand; NULL for unary operations',
    result_matrix  TEXT            NOT NULL COMMENT 'Result matrix string; "N/A" for scalar results',
    scalar_result  DOUBLE                   COMMENT 'Populated only for DETERMINANT',
    rows_a         INT             NOT NULL COMMENT 'Row count of Matrix A',
    cols_a         INT             NOT NULL COMMENT 'Column count of Matrix A',
    rows_b         INT                      COMMENT 'Row count of Matrix B',
    cols_b         INT                      COMMENT 'Column count of Matrix B',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_op_type  (operation_type),
    INDEX idx_created  (created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COMMENT='Stores every matrix computation performed by the application';

-- 4. Seed a few sample rows for testing the History view
INSERT INTO matrix_operations
    (operation_type, matrix_a, matrix_b, result_matrix, scalar_result, rows_a, cols_a, rows_b, cols_b)
VALUES
    ('ADD',
     '[[1,2],[3,4]]',
     '[[5,6],[7,8]]',
     '[[6,8],[10,12]]',
     NULL, 2, 2, 2, 2),

    ('MULTIPLY',
     '[[1,2],[3,4]]',
     '[[2,0],[1,2]]',
     '[[4,4],[10,8]]',
     NULL, 2, 2, 2, 2),

    ('TRANSPOSE',
     '[[1,2,3],[4,5,6]]',
     NULL,
     '[[1,4],[2,5],[3,6]]',
     NULL, 2, 3, NULL, NULL),

    ('DETERMINANT',
     '[[3,8],[4,6]]',
     NULL,
     'N/A',
     -14.0, 2, 2, NULL, NULL),

    ('SUBTRACT',
     '[[9,8],[7,6]]',
     '[[1,2],[3,4]]',
     '[[8,6],[4,2]]',
     NULL, 2, 2, 2, 2);

-- 5. Verify
SELECT id, operation_type, rows_a, cols_a, created_at
FROM   matrix_operations
ORDER  BY id;
