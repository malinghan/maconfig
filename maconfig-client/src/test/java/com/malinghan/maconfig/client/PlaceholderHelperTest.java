package com.malinghan.maconfig.client;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderHelperTest {

    private final PlaceholderHelper helper = new PlaceholderHelper();

    @Test
    void extracts_simple_key() {
        Set<String> keys = helper.extractKeys("${demo.message}");
        assertThat(keys).containsExactly("demo.message");
    }

    @Test
    void extracts_key_with_default_value() {
        Set<String> keys = helper.extractKeys("${demo.message:Hello}");
        assertThat(keys).containsExactly("demo.message");
    }

    @Test
    void extracts_multiple_keys() {
        Set<String> keys = helper.extractKeys("${key1} and ${key2:default}");
        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void returns_empty_for_null() {
        assertThat(helper.extractKeys(null)).isEmpty();
    }

    @Test
    void returns_empty_for_no_placeholder() {
        assertThat(helper.extractKeys("plain text")).isEmpty();
    }

    @Test
    void handles_nested_placeholder() {
        Set<String> keys = helper.extractKeys("${outer.${inner}}");
        assertThat(keys).contains("outer.");
    }
}
