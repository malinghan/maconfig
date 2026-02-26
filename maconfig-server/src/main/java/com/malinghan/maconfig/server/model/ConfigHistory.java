package com.malinghan.maconfig.server.model;

import lombok.Data;

@Data
public class ConfigHistory {
    private Long id;
    private String app;
    private String env;
    private String ns;
    private String pkey;
    private String pval;
    private String op;
    private long createdAt;
}
