package com.can25;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Utilise application-test.properties
class Can25ApplicationTests {

    @Test
    void contextLoads() {
        // Test de chargement du contexte
    }

}