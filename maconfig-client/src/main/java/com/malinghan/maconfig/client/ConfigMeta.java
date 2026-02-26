package com.malinghan.maconfig.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigMeta {
    private String app;
    private String env;
    private String ns;
    private String serverUrl;
}
