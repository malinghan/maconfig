package com.malinghan.maconfig.client.repository;

import com.malinghan.maconfig.client.ConfigMeta;

import java.util.Map;

public interface MaRepository {
    Map<String, String> getConfigs(ConfigMeta meta);
    long getVersion(ConfigMeta meta);
}
