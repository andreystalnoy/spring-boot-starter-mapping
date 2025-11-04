package com.matteria.mapping;

import com.matteria.mapping.configuration.MappingAutoConfiguration;
import com.matteria.mapping.configuration.MappingConfiguration;
import com.matteria.mapping.configuration.model.Address;
import com.matteria.mapping.configuration.model.Country;
import com.matteria.mapping.configuration.model.Product;
import com.matteria.mapping.configuration.model.ProductDto;
import com.matteria.mapping.core.MappingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

class MappingPerformanceTest {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int TEST_ITERATIONS = 1_000_000;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MappingAutoConfiguration.class, MappingConfiguration.class));

    /**
     * Performance result holder for a single benchmark run
     */
    private static class BenchmarkResult {
        long directIndividualTime;
        long serviceIndividualTime;
        double individualRatio;

        long directCollectionTime;
        long serviceCollectionTime;
        double collectionRatio;

        BenchmarkResult(long directIndividualTime, long serviceIndividualTime,
                        long directCollectionTime, long serviceCollectionTime) {
            this.directIndividualTime = directIndividualTime;
            this.serviceIndividualTime = serviceIndividualTime;
            this.individualRatio = (double) serviceIndividualTime / directIndividualTime;

            this.directCollectionTime = directCollectionTime;
            this.serviceCollectionTime = serviceCollectionTime;
            this.collectionRatio = (double) serviceCollectionTime / directCollectionTime;
        }
    }

    /**
     * Statistical summary of multiple benchmark runs
     */
    private static class StatisticalSummary {
        double mean;
        double median;
        double min;
        double max;
        double stdDev;
        int directWins;
        int serviceWins;

        StatisticalSummary(double[] values) {
            this.mean = calculateMean(values);
            this.median = calculateMedian(values);
            this.min = calculateMin(values);
            this.max = calculateMax(values);
            this.stdDev = calculateStdDev(values, mean);

            directWins = 0;
            serviceWins = 0;
            for (double value : values) {
                if (value < 1.0) serviceWins++;
                else if (value > 1.0) directWins++;
            }
        }

        private double calculateMean(double[] values) {
            double sum = 0;
            for (double value : values) {
                sum += value;
            }
            return sum / values.length;
        }

        private double calculateMedian(double[] values) {
            double[] sortedValues = Arrays.copyOf(values, values.length);
            Arrays.sort(sortedValues);
            int middle = sortedValues.length / 2;
            if (sortedValues.length % 2 == 0) {
                return (sortedValues[middle-1] + sortedValues[middle]) / 2.0;
            } else {
                return sortedValues[middle];
            }
        }

        private double calculateMin(double[] values) {
            double min = Double.MAX_VALUE;
            for (double value : values) {
                if (value < min) min = value;
            }
            return min;
        }

        private double calculateMax(double[] values) {
            double max = Double.MIN_VALUE;
            for (double value : values) {
                if (value > max) max = value;
            }
            return max;
        }

        private double calculateStdDev(double[] values, double mean) {
            double sumSquaredDiff = 0;
            for (double value : values) {
                double diff = value - mean;
                sumSquaredDiff += diff * diff;
            }
            return Math.sqrt(sumSquaredDiff / values.length);
        }
    }

    @Test
    void runMultipleBenchmarks() {
        int runs = 10; // Number of benchmark runs
        runAndAnalyze(runs);
    }

    public void runAndAnalyze(int runs) {
        contextRunner.run(context -> {
            List<BenchmarkResult> results = new ArrayList<>();
            System.out.println("Starting " + runs + " benchmark runs...");

            // Run the benchmarks multiple times
            for (int i = 0; i < runs; i++) {
                System.out.println("\nRun " + (i+1) + "/" + runs);
                results.add(runSingleBenchmark(context));
            }

            // Analyze the results
            analyzeResults(results);
        });
    }

    private BenchmarkResult runSingleBenchmark(ApplicationContext context) {
        MappingService mappingService = context.getBean(MappingService.class);

        // Create test data
        List<Product> products = createTestProducts(TEST_ITERATIONS);

        // Define direct mapping function
        Function<Address, String> directAddressMapper = address ->  {
                if (address != null)
                    return address.formattedAddress();
                else
                    return null;
        };

        Function<Product, ProductDto> directMapper = product -> new ProductDto(
                product.uuid().toString(),
                product.name(),
                product.description(),
                product.price().toString(),
                directAddressMapper.apply(product.address())
        );

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Product product = products.get(i % products.size());
            ProductDto dto1 = directMapper.apply(product);
            ProductDto dto2 = mappingService.map(product, ProductDto.class);
        }

        // Test 1: Direct mapping individual objects
        System.out.println("Testing direct mapping of individual objects...");
        long directStart = System.nanoTime();

        for (Product product : products) {
            ProductDto dto = directMapper.apply(product);
        }

        long directEnd = System.nanoTime();
        long directTime = directEnd - directStart;

        // Test 2: MappingService for individual objects
        System.out.println("Testing MappingService for individual objects...");
        long serviceStart = System.nanoTime();

        for (Product product : products) {
            ProductDto dto = mappingService.map(product, ProductDto.class);
        }

        long serviceEnd = System.nanoTime();
        long serviceTime = serviceEnd - serviceStart;

        // Test 3: Direct mapping with collection
        System.out.println("Testing direct mapping of collection...");
        List<ProductDto> directDtos = new ArrayList<>(products.size());

        long directCollectionStart = System.nanoTime();

        for (Product product : products) {
            directDtos.add(directMapper.apply(product));
        }

        long directCollectionEnd = System.nanoTime();
        long directCollectionTime = directCollectionEnd - directCollectionStart;

        // Test 4: MappingService with collection
        System.out.println("Testing MappingService for collection...");
        long serviceCollectionStart = System.nanoTime();

        List<ProductDto> serviceDtos = mappingService.map(products, ProductDto.class).toList();

        long serviceCollectionEnd = System.nanoTime();
        long serviceCollectionTime = serviceCollectionEnd - serviceCollectionStart;

        // Print individual result
        BenchmarkResult result = new BenchmarkResult(directTime, serviceTime,
                directCollectionTime, serviceCollectionTime);
        printResult(result);

        return result;
    }

    private List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>(count);
        Address address1 =
                new Address("some street 1", "some city", "010020", Country.US);

        for (int i = 0; i < count; i++) {
            products.add(new Product(
                    UUID.randomUUID(),
                    "Product " + i,
                    "Description for product " + i,
                    new BigDecimal(String.format("%.2f", 10 + Math.random() * 100)),
                    address1
            ));
        }
        return products;
    }

    private void printResult(BenchmarkResult result) {
        System.out.println("\n====== BENCHMARK RESULT ======");
        System.out.printf("Individual objects:\n");
        System.out.printf("Direct mapping:  %,d ns (%.2f ms)\n",
                result.directIndividualTime, result.directIndividualTime / 1_000_000.0);
        System.out.printf("MappingService:  %,d ns (%.2f ms)\n",
                result.serviceIndividualTime, result.serviceIndividualTime / 1_000_000.0);
        System.out.printf("Ratio: %.2fx\n\n", result.individualRatio);

        System.out.printf("Collections:\n");
        System.out.printf("Direct mapping:  %,d ns (%.2f ms)\n",
                result.directCollectionTime, result.directCollectionTime / 1_000_000.0);
        System.out.printf("MappingService:  %,d ns (%.2f ms)\n",
                result.serviceCollectionTime, result.serviceCollectionTime / 1_000_000.0);
        System.out.printf("Ratio: %.2fx\n", result.collectionRatio);
        System.out.println("===============================");
    }

    private void analyzeResults(List<BenchmarkResult> results) {
        // Extract ratio arrays for statistical analysis
        double[] individualRatios = results.stream()
                .mapToDouble(r -> r.individualRatio)
                .toArray();

        double[] collectionRatios = results.stream()
                .mapToDouble(r -> r.collectionRatio)
                .toArray();

        // Calculate statistics
        StatisticalSummary individualStats = new StatisticalSummary(individualRatios);
        StatisticalSummary collectionStats = new StatisticalSummary(collectionRatios);

        // Determine overall winner for each category
        String individualWinner = determineWinner(individualStats);
        String collectionWinner = determineWinner(collectionStats);

        // Print comprehensive analysis
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║               MAPPING PERFORMANCE ANALYSIS                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Individual Objects                                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        printStatistics("Individual Objects", individualStats);
        System.out.println("║ " + individualWinner);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Collections                                                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        printStatistics("Collections", collectionStats);
        System.out.println("║ " + collectionWinner);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Conclusion                                                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        printConclusion(individualStats, collectionStats);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Print raw data table
        System.out.println("\nRaw Results (Service/Direct ratio - lower is better for MappingService):");
        System.out.println("┌───────┬─────────────────┬─────────────┐");
        System.out.println("│ Run # │ Individual Obj  │ Collections │");
        System.out.println("├───────┼─────────────────┼─────────────┤");
        for (int i = 0; i < results.size(); i++) {
            BenchmarkResult result = results.get(i);
            System.out.printf("│ %5d │ %15.2f │ %11.2f │\n",
                    i+1, result.individualRatio, result.collectionRatio);
        }
        System.out.println("└───────┴─────────────────┴─────────────┘");
    }

    private void printStatistics(String category, StatisticalSummary stats) {
        // Format with proper padding to align with the table width
        System.out.printf("║ Mean Ratio:   %7.2fx                                       ║\n", stats.mean);
        System.out.printf("║ Median Ratio: %7.2fx                                       ║\n", stats.median);
        System.out.printf("║ Min Ratio:    %7.2fx                                       ║\n", stats.min);
        System.out.printf("║ Max Ratio:    %7.2fx                                       ║\n", stats.max);
        System.out.printf("║ Std Dev:      %7.2f                                        ║\n", stats.stdDev);
        System.out.printf("║ Direct wins:  %7d times                                  ║\n", stats.directWins);
        System.out.printf("║ Service wins: %7d times                                  ║\n", stats.serviceWins);
    }

    private String determineWinner(StatisticalSummary stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("Winner: ");

        if (Math.abs(stats.mean - 1.0) < 0.05) {
            sb.append("Statistical tie (within 5% difference)");
        } else if (stats.mean < 1.0) {
            sb.append("MappingService is faster by ");
            sb.append(formatPercent(1.0 - stats.mean));
            sb.append(" on average");
        } else {
            sb.append("Direct mapping is faster by ");
            sb.append(formatPercent(stats.mean - 1.0));
            sb.append(" on average");
        }

        // Pad to fill the table width
        while (sb.length() < 60) {
            sb.append(" ");
        }
        sb.append(" ║");

        return sb.toString();
    }

    private void printConclusion(StatisticalSummary indStats, StatisticalSummary colStats) {
        // Calculate overall average performance difference
        double overallMean = (indStats.mean + colStats.mean) / 2.0;
        System.out.printf("Overall, the performance difference is %.1f%% on average.\n",
                Math.abs(overallMean - 1.0) * 100);

        // Add variability analysis
        double avgStdDev = (indStats.stdDev + colStats.stdDev) / 2.0;
        System.out.printf("Variability between runs (%.2f) suggests JVM optimizations\n",
                avgStdDev);

        // Provide practical recommendation
        System.out.println("are affecting results in a non-deterministic manner.");
        System.out.println();

        if (Math.abs(overallMean - 1.0) < 0.1) {
            System.out.println("RECOMMENDATION: Choose based on architectural benefits rather");
            System.out.println("than performance, as the difference is negligible in practice.");
        } else if (overallMean < 1.0) {
            System.out.println("RECOMMENDATION: MappingService shows better overall performance");
            System.out.println("and provides architectural benefits. Prefer it in most cases.");
        } else {
            System.out.println("RECOMMENDATION: Direct mapping is consistently faster, but");
            System.out.println("consider if the architectural benefits of MappingService");
            System.out.println("outweigh the " + formatPercent(overallMean - 1.0) + " average performance difference.");
        }
    }

    private String formatPercent(double value) {
        BigDecimal bd = new BigDecimal(value * 100);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.toString() + "%";
    }

    public static void main(String[] args) {
        MappingPerformanceTest analyzer = new MappingPerformanceTest();

        // Default to 10 runs if no argument is provided
        int runs = args.length > 0 ? Integer.parseInt(args[0]) : 50;

        analyzer.runAndAnalyze(runs);
    }
}
