package com.matteria.mapping;

import com.matteria.mapping.configuration.MappingAutoConfiguration;
import com.matteria.mapping.configuration.MappingConfiguration;
import com.matteria.mapping.configuration.model.Product;
import com.matteria.mapping.configuration.model.ProductDto;
import com.matteria.mapping.core.MappingAspect;
import com.matteria.mapping.core.MappingRegistry;
import com.matteria.mapping.core.MappingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(classes = MappingAutoConfiguration.class)
class SpringBootStarterMappingApplicationTests {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MappingAutoConfiguration.class, MappingConfiguration.class));

    private static MappingService mappingService;

    @BeforeAll
    static void beforeAll() {
        contextRunner.run(context -> {
            Assertions.assertTrue(context.containsBean(MappingService.class.getName()), "No bean MappingService found");
            Assertions.assertTrue(context.containsBean(MappingAspect.class.getName()), "No bean MappingAspect found");
            Assertions.assertTrue(context.containsBean(MappingRegistry.class.getName()), "No bean MappingRegistry found");
            mappingService = context.getBean(MappingService.class);
        });
    }

    @Test
    void simpleTest() {
        contextRunner.run(context -> {
            Integer a = mappingService.map("1", Integer.class);
            Assertions.assertEquals(Integer.valueOf(1), a);

            String b = mappingService.map(1, String.class);
            Assertions.assertEquals("1", b);

            Integer c = mappingService.map("plus", "1", Integer.class);
            Assertions.assertEquals(Integer.valueOf(2), c);
        });
    }

    @Test
    void testSimpleCollections() {
        Set<Integer> integers = Set.of(1, 2, 3);
        contextRunner.run(context -> {
            Set<String> strings = mappingService.map(integers, String.class)
                    .toSet();
            Assertions.assertEquals(3, integers.size());
            Assertions.assertTrue(strings.contains("1")
                    && strings.contains("2")
                    && strings.contains("3"));
        });
    }

    @Test
    void testObjectCollections() {
        Set<Product> products = Set.of(
                new Product(
                        UUID.randomUUID(),
                        "Product 1",
                        "Some description",
                        new BigDecimal("1.5")
                ),
                new Product(
                        UUID.randomUUID(),
                        "Product 2",
                        "Some description",
                        new BigDecimal("2.5")
                ),
                new Product(
                        UUID.randomUUID(),
                        "Product 3",
                        "Some description",
                        new BigDecimal("1.8")
                )
        );

        contextRunner.run(context -> {
            List<ProductDto> dtoList =  mappingService.map(products, ProductDto.class).toList();
            Assertions.assertEquals(3, dtoList.size());
            Assertions.assertEquals("1.8", dtoList.stream()
                    .filter(it -> it.price().equals("1.8")).findFirst()
                        .orElseThrow(RuntimeException::new)
                    .price());

            List<ProductDto> dtoListHiddenUuid =  mappingService.map("hiddenUuid", products, ProductDto.class).toList();
            Assertions.assertEquals(3, dtoListHiddenUuid.size());
            Assertions.assertTrue(dtoListHiddenUuid.stream()
                    .filter(it -> !it.uuid().equals("hidden"))
                    .collect(Collectors.toSet()).isEmpty());

            Set<Product> productSet = mappingService.map(dtoList, Product.class).toSet();
            Assertions.assertEquals(3, productSet.size());
            Assertions.assertEquals("1.8", productSet.stream()
                    .filter(it -> it.getPrice().equals(new BigDecimal("1.8"))).findFirst()
                        .orElseThrow(RuntimeException::new)
                    .getPrice().toString());
        });
    }

    @Test
    void testNullObject() {
        contextRunner.run(context -> {
            Assertions.assertThrows(MappingException.class, () -> {
                mappingService.map(null, Integer.class);
            });
            Assertions.assertThrows(MappingException.class, () -> {
                mappingService.map(new Object(), null);
            });
            Assertions.assertThrows(MappingException.class, () -> {
                mappingService.map(Set.of(), null);
            });
        });
    }

}
