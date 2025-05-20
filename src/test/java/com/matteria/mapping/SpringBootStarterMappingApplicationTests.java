package com.matteria.mapping;

import com.matteria.mapping.configuration.MappingAutoConfiguration;
import com.matteria.mapping.configuration.MappingConfiguration;
import com.matteria.mapping.core.MappingAspect;
import com.matteria.mapping.core.MappingRegistry;
import com.matteria.mapping.core.MappingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;



@SpringBootTest(classes = MappingAutoConfiguration.class)
class SpringBootStarterMappingApplicationTests {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MappingAutoConfiguration.class, MappingConfiguration.class));

    @BeforeAll
    static void beforeAll() {
        contextRunner.run(context -> {
            Assertions.assertTrue(context.containsBean(MappingService.class.getName()), "No bean MappingService found");
            Assertions.assertTrue(context.containsBean(MappingAspect.class.getName()), "No bean MappingAspect found");
            Assertions.assertTrue(context.containsBean(MappingRegistry.class.getName()), "No bean MappingRegistry found");
        });
    }

    @Test
    void simpleTest() {
        contextRunner.run(context -> {
            MappingService mappingService = context.getBean(MappingService.class);

            Integer a = mappingService.map("1", Integer.class);
            Assertions.assertEquals(Integer.valueOf(1), a);

            String b = mappingService.map(1, String.class);
            Assertions.assertEquals("1", b);

            Integer c = mappingService.map("plus", "1", Integer.class);
            Assertions.assertEquals(Integer.valueOf(2), c);
        });
    }

    @Test
    void testCollections() {

    }

}
