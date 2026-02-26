package com.malinghan.maconfig.client;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MaConfigServiceImplTest {

    private final ConfigMeta meta = ConfigMeta.builder()
            .app("test").env("dev").ns("default").serverUrl("http://localhost:9091").build();

    private final MaConfigServiceImpl service = new MaConfigServiceImpl(meta);

    @Test
    void calcChangedKeys_detects_new_key() {
        Map<String, String> old = Map.of("a", "1");
        Map<String, String> next = Map.of("a", "1", "b", "2");
        Set<String> changed = service.calcChangedKeys(old, next);
        assertThat(changed).containsExactly("b");
    }

    @Test
    void calcChangedKeys_detects_modified_value() {
        Map<String, String> old = Map.of("a", "1");
        Map<String, String> next = Map.of("a", "2");
        Set<String> changed = service.calcChangedKeys(old, next);
        assertThat(changed).containsExactly("a");
    }

    @Test
    void calcChangedKeys_detects_deleted_key() {
        Map<String, String> old = Map.of("a", "1", "b", "2");
        Map<String, String> next = Map.of("a", "1");
        Set<String> changed = service.calcChangedKeys(old, next);
        assertThat(changed).containsExactly("b");
    }

    @Test
    void calcChangedKeys_returns_empty_when_no_change() {
        Map<String, String> old = Map.of("a", "1");
        Map<String, String> next = Map.of("a", "1");
        assertThat(service.calcChangedKeys(old, next)).isEmpty();
    }

    @Test
    void calcChangedKeys_handles_empty_maps() {
        assertThat(service.calcChangedKeys(Map.of(), Map.of())).isEmpty();
    }
}
