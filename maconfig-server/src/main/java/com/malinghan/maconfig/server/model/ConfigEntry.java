package com.malinghan.maconfig.server.model;

import lombok.Data;

@Data
public class ConfigEntry {
    private String app;
    private String env;
    private String ns;
    private String pkey;
    private String pval;
}
