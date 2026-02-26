package com.malinghan.maconfig.client;

import lombok.Data;

import java.lang.reflect.Field;

@Data
public class SpringValue {
    private Object bean;
    private Field field;
    private String placeholder;
}
