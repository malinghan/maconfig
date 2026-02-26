package com.malinghan.maconfig.client.processor;

import com.malinghan.maconfig.client.PlaceholderHelper;
import com.malinghan.maconfig.client.SpringValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

public class SpringValueProcessor implements BeanPostProcessor, ApplicationListener<EnvironmentChangeEvent>, EnvironmentAware {

    private static final Logger log = Logger.getLogger(SpringValueProcessor.class.getName());

    private Environment environment;
    private final PlaceholderHelper placeholderHelper = new PlaceholderHelper();
    private final Map<String, List<SpringValue>> springValueMap = new HashMap<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : findAllFields(clazz)) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            if (valueAnnotation != null) {
                String placeholder = valueAnnotation.value();
                Set<String> keys = placeholderHelper.extractKeys(placeholder);
                for (String key : keys) {
                    SpringValue springValue = new SpringValue();
                    springValue.setBean(bean);
                    springValue.setField(field);
                    springValue.setPlaceholder(placeholder);
                    springValueMap.computeIfAbsent(key, k -> new ArrayList<>()).add(springValue);
                }
            }
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changedKeys = event.getKeys();
        for (String key : changedKeys) {
            List<SpringValue> springValues = springValueMap.get(key);
            if (springValues == null) continue;
            for (SpringValue springValue : springValues) {
                updateField(springValue);
            }
        }
    }

    private void updateField(SpringValue springValue) {
        Field field = springValue.getField();
        Object bean = springValue.getBean();
        try {
            String placeholder = springValue.getPlaceholder();
            String resolved = environment.resolvePlaceholders(placeholder);
            Object value = convertValue(resolved, field.getType());
            ReflectionUtils.makeAccessible(field);
            field.set(bean, value);
            log.info("[MACONFIG] updated " + bean.getClass().getSimpleName() + "." + field.getName() + " = " + value);
        } catch (Exception e) {
            log.severe("[MACONFIG] failed to update " + bean.getClass().getSimpleName() + "." + field.getName() + ": " + e.getMessage());
        }
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        return value;
    }

    private List<Field> findAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
