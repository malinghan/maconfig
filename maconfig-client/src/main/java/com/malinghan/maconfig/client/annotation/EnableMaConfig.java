package com.malinghan.maconfig.client.annotation;

import com.malinghan.maconfig.client.MaConfigRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MaConfigRegistrar.class)
public @interface EnableMaConfig {
    String app();
    String env() default "dev";
    String ns() default "default";
    String serverUrl() default "http://localhost:9090";
}
