package com.sabormayor.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.server.native.search-locations=classpath:/"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }
}
