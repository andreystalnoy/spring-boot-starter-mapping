package com.matteria.mapping.configuration;

import com.matteria.mapping.Mapping;
import com.matteria.mapping.MappingConfiguration;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@MappingConfiguration
public class EmptyMappingConfiguration {

    @Mapping
    public Function<Object, Object> objectToObject(
            @Mapping("nonExistentMapping") Function<Object, Object> function)
    {
        return function::apply;
    }

}
