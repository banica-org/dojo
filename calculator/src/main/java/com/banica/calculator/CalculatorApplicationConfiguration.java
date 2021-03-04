package com.banica.calculator;

import com.banica.common.ShutdownManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalculatorApplicationConfiguration {

    @Bean
    ShutdownManager getShutdownManager(final ApplicationContext applicationContext) {
        return new ShutdownManager(applicationContext);
    }

}
