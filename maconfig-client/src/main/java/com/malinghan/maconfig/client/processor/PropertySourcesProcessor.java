package com.malinghan.maconfig.client.processor;

import com.malinghan.maconfig.client.MaConfigServiceImpl;
import com.malinghan.maconfig.client.MaPropertySource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment ce) {
            this.environment = ce;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MaConfigServiceImpl service = beanFactory.getBean(MaConfigServiceImpl.class);
        MaPropertySource propertySource = new MaPropertySource("maconfig", service);
        if (environment != null) {
            MutablePropertySources sources = environment.getPropertySources();
            if (!sources.contains("maconfig")) {
                sources.addFirst(propertySource);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
