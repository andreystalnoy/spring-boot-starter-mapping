package com.matteria.mapping;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringBootStarterMappingApplicationTests {
    @Autowired
    MappingService mappingService;

    @Test
    void contextLoads() {
        Integer a = mappingService.map("1", Integer.class);
        Assertions.assertEquals(Integer.valueOf(1), a);

        String b = mappingService.map(1, String.class);
        Assertions.assertEquals("1", b);

        Integer c = mappingService.map("plus", "1", Integer.class);

        Assertions.assertEquals(Integer.valueOf(2), c);
    }

}
