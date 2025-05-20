package com.matteria.mapping;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@ConditionalOnClass(MappingService.class)
@ConditionalOnProperty(prefix = "matteria.mapping", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MappingProperties.class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.matteria.mapping")
public class MappingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MappingRegistry mappingRegistry() {
        return new MappingRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingAspect mappingAspect(MappingRegistry registry) {
        return new MappingAspect(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingService mappingService(MappingRegistry registry, MappingProperties properties) {
        return new MappingService(registry, properties);
    }
}