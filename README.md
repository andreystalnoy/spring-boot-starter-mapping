# Spring Boot Starter Mapping

A Spring Boot starter that provides automatic object mapping capabilities using annotations and AOP.

## Features

- Register type converters using the `@Mapping` annotation
- Map objects from one type to another using a simple service API
- Multiple mapping strategies with named values
- Auto-configured for Spring Boot

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.matteria</groupId>
    <artifactId>spring-boot-starter-mapping</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage

### Define your mappings

Create a configuration class with methods that return mapping functions:

```java
@Component
public class MappingConfiguration {

    @Mapping
    public Function<String, Integer> stringToInteger() {
        return Integer::valueOf;
    }

    @Mapping
    public Function<Integer, String> integerToString() {
        return Object::toString;
    }

    @Mapping("custom")
    public Function<String, Integer> stringToIntegerCustom() {
        return s -> Integer.parseInt(s) + 1;
    }
}
```

### Use the MappingService

Inject the `MappingService` and use it to convert objects:

```java
@Service
public class YourService {
    private final MappingService mappingService;

    public YourService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public void process() {
        // Use default mapping
        Integer number = mappingService.map("123", Integer.class);
        
        // Use named mapping
        Integer customNumber = mappingService.map("custom", "123", Integer.class);
    }
}
```

## Configuration

You can configure the starter using the following properties in your `application.properties` or `application.yml`:

```properties
# Enable/disable mapping functionality (default: true)
matteria.mapping.enabled=true

# Set the default mapping value (default: "default")
matteria.mapping.default-value=default
```

## Requirements

- Java 17 or higher
- Spring Boot 3.x

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
