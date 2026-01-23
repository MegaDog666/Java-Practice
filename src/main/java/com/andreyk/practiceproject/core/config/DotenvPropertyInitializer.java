package com.andreyk.practiceproject.core.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

public class DotenvPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        Dotenv dotenv = Dotenv.load();

        PropertySource<?> propertySource = new PropertySource<>("dotenv") {
            @Override
            public Object getProperty(String name) {
                String value = dotenv.get(name);
                if (value == null) {
                    value = dotenv.get(name.replace('.', '_').toUpperCase());
                }
                return value;
            }
        };

        env.getPropertySources().addFirst(propertySource);
    }
}
