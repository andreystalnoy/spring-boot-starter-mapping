package com.matteria.mapping;

import com.matteria.mapping.configuration.MappingAutoConfiguration;
import com.matteria.mapping.configuration.MappingConfiguration;
import com.matteria.mapping.configuration.model.Product;
import com.matteria.mapping.configuration.model.ProductDto;
import com.matteria.mapping.core.MappingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

class MappingPerformanceTest {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int TEST_ITERATIONS = 1_000_000;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MappingAutoConfiguration.class, MappingConfiguration.class));

    @Test
    void comparePerformance() {
        contextRunner.run(context -> {
            MappingService mappingService = context.getBean(MappingService.class);

            // Create test data
            List<Product> products = createTestProducts(TEST_ITERATIONS);

            // Define direct mapping function
            Function<Product, ProductDto> directMapper = product -> new ProductDto(
                    product.getUuid().toString(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice().toString()
            );

            // Warmup
            System.out.println("Warming up...");
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                Product product = products.get(i % products.size());
                ProductDto dto1 = directMapper.apply(product);
                ProductDto dto2 = mappingService.map(product, ProductDto.class);
            }

            // Test 1: Direct mapping individual objects
            System.out.println("\nTesting direct mapping of " + TEST_ITERATIONS + " individual objects...");
            long directStart = System.nanoTime();

            for (Product product : products) {
                ProductDto dto = directMapper.apply(product);
            }

            long directEnd = System.nanoTime();
            long directTime = directEnd - directStart;

            // Test 2: MappingService for individual objects
            System.out.println("Testing MappingService for " + TEST_ITERATIONS + " individual objects...");
            long serviceStart = System.nanoTime();

            for (Product product : products) {
                ProductDto dto = mappingService.map(product, ProductDto.class);
            }

            long serviceEnd = System.nanoTime();
            long serviceTime = serviceEnd - serviceStart;

            // Test 3: Direct mapping with collection
            System.out.println("\nTesting direct mapping of collection with " + TEST_ITERATIONS + " elements...");
            List<ProductDto> directDtos = new ArrayList<>(products.size());

            long directCollectionStart = System.nanoTime();

            for (Product product : products) {
                directDtos.add(directMapper.apply(product));
            }

            long directCollectionEnd = System.nanoTime();
            long directCollectionTime = directCollectionEnd - directCollectionStart;

            // Test 4: MappingService with collection
            System.out.println("Testing MappingService for collection with " + TEST_ITERATIONS + " elements...");
            long serviceCollectionStart = System.nanoTime();

            List<ProductDto> serviceDtos = mappingService.map(products, ProductDto.class).toList();

            long serviceCollectionEnd = System.nanoTime();
            long serviceCollectionTime = serviceCollectionEnd - serviceCollectionStart;

            // Print results
            printResults(directTime, serviceTime, directCollectionTime, serviceCollectionTime);
        });
    }

    private List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            products.add(new Product(
                    UUID.randomUUID(),
                    "Product " + i,
                    "Description for product " + i,
                    new BigDecimal(String.format("%.2f", 10 + Math.random() * 100))
            ));
        }
        return products;
    }

    private void printResults(long directTime, long serviceTime, long directCollectionTime, long serviceCollectionTime) {
        System.out.println("\n====== PERFORMANCE COMPARISON ======");
        System.out.printf("Individual objects:\n");
        System.out.printf("Direct mapping:  %,d ns (%.2f ms)\n", directTime, directTime / 1_000_000.0);
        System.out.printf("MappingService:  %,d ns (%.2f ms)\n", serviceTime, serviceTime / 1_000_000.0);
        System.out.printf("Ratio: %.2fx\n\n", (double) serviceTime / directTime);

        System.out.printf("Collections:\n");
        System.out.printf("Direct mapping:  %,d ns (%.2f ms)\n", directCollectionTime, directCollectionTime / 1_000_000.0);
        System.out.printf("MappingService:  %,d ns (%.2f ms)\n", serviceCollectionTime, serviceCollectionTime / 1_000_000.0);
        System.out.printf("Ratio: %.2fx\n", (double) serviceCollectionTime / directCollectionTime);
        System.out.println("===================================");
    }
}
