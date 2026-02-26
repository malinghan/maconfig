DROP TABLE IF EXISTS configs;
DROP TABLE IF EXISTS locks;
DROP TABLE IF EXISTS config_history;

CREATE TABLE configs (
    app        VARCHAR(64)  NOT NULL,
    env        VARCHAR(64)  NOT NULL,
    ns         VARCHAR(64)  NOT NULL,
    pkey       VARCHAR(128) NOT NULL,
    pval       TEXT,
    updated_at BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (app, env, ns, pkey)
);

CREATE TABLE locks (
    id  INT PRIMARY KEY,
    app VARCHAR(64) NOT NULL
);

INSERT INTO locks VALUES (1, 'maconfig-server');

CREATE TABLE config_history (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    app        VARCHAR(64)  NOT NULL,
    env        VARCHAR(64)  NOT NULL,
    ns         VARCHAR(64)  NOT NULL,
    pkey       VARCHAR(128) NOT NULL,
    pval       TEXT,
    op         VARCHAR(16)  NOT NULL,
    created_at BIGINT       NOT NULL
);