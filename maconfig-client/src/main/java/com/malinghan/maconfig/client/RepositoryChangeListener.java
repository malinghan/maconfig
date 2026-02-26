package com.malinghan.maconfig.client;

import java.util.Map;

public interface RepositoryChangeListener {
    void onChange(Map<String, String> newConfigs);
}
