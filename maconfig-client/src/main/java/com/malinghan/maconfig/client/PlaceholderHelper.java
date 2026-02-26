package com.malinghan.maconfig.client;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderHelper {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:}]+)");

    public Set<String> extractKeys(String placeholder) {
        Set<String> keys = new HashSet<>();
        if (placeholder == null || placeholder.isEmpty()) {
            return keys;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(placeholder);
        while (matcher.find()) {
            keys.add(matcher.group(1).trim());
        }
        return keys;
    }
}
