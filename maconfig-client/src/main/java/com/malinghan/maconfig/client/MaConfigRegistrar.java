package com.malinghan.maconfig.client;

import com.malinghan.maconfig.client.annotation.EnableMaConfig;
import com.malinghan.maconfig.client.processor.PropertySourcesProcessor;
import com.malinghan.maconfig.client.processor.SpringValueProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class MaConfigRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = importingClassMetadata
                .getAnnotationAttributes(EnableMaConfig.class.getName());
        if (attrs == null) return;

        String app       = (String) attrs.get("app");
        String env       = (String) attrs.get("env");
        String ns        = (String) attrs.get("ns");
        String serverUrl = (String) attrs.get("serverUrl");

        // Register ConfigMeta bean
        BeanDefinitionBuilder metaBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(ConfigMeta.class, () ->
                        ConfigMeta.builder()
                                .app(app)
                                .env(env)
                                .ns(ns)
                                .serverUrl(serverUrl)
                                .build());
        registry.registerBeanDefinition("maConfigMeta", metaBuilder.getBeanDefinition());

        // Register MaConfigServiceImpl bean
        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(MaConfigServiceImpl.class);
        serviceBuilder.addConstructorArgReference("maConfigMeta");
        registry.registerBeanDefinition("maConfigServiceImpl", serviceBuilder.getBeanDefinition());

        // Register PropertySourcesProcessor bean
        BeanDefinitionBuilder pspBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(PropertySourcesProcessor.class);
        registry.registerBeanDefinition("maPropertySourcesProcessor", pspBuilder.getBeanDefinition());

        // Register SpringValueProcessor bean - Environment injected via EnvironmentAware
        BeanDefinitionBuilder svpBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(SpringValueProcessor.class);
        registry.registerBeanDefinition("maSpringValueProcessor", svpBuilder.getBeanDefinition());
    }
}
