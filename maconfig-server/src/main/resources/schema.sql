DROP TABLE IF EXISTS configs;
DROP TABLE IF EXISTS locks;

CREATE TABLE configs (
    app   VARCHAR(64)  NOT NULL,
    env   VARCHAR(64)  NOT NULL,
    ns    VARCHAR(64)  NOT NULL,
    pkey  VARCHAR(128) NOT NULL,
    pval  TEXT,
    PRIMARY KEY (app, env, ns, pkey)
);

CREATE TABLE locks (
    id  INT PRIMARY KEY,
    app VARCHAR(64) NOT NULL
);

INSERT INTO locks VALUES (1, 'maconfig-server');
