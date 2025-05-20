package com.matteria.mapping.configuration;

import com.matteria.mapping.core.MappingAspect;
import com.matteria.mapping.core.MappingRegistry;
import com.matteria.mapping.core.MappingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnClass(MappingService.class)
public class MappingAutoConfiguration {

    @Bean("com.matteria.mapping.core.MappingRegistry")
    @ConditionalOnMissingBean
    public MappingRegistry mappingRegistry() {
        return new MappingRegistry();
    }

    @Bean("com.matteria.mapping.core.MappingAspect")
    @ConditionalOnMissingBean
    public MappingAspect mappingAspect(MappingRegistry registry) {
        return new MappingAspect(registry);
    }

    @Bean("com.matteria.mapping.core.MappingService")
    @ConditionalOnMissingBean
    public MappingService mappingService(MappingRegistry registry) {
        return new MappingService(registry);
    }

}