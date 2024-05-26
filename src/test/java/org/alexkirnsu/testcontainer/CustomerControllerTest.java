package org.alexkirnsu.testcontainer;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.alexkirnsu.testcontainer.dao.CustomerRepository;
import org.alexkirnsu.testcontainer.model.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {

    @LocalServerPort
    private Integer port;

    static OracleContainer oracle = new OracleContainer(
            "gvenzl/oracle-xe:18.4.0-slim"
    );

    @BeforeAll
    static void beforeAll() {
        oracle.start();
    }

    @AfterAll
    static void afterAll() {
        oracle.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracle::getJdbcUrl);
        registry.add("spring.datasource.username", oracle::getUsername);
        registry.add("spring.datasource.password", oracle::getPassword);
    }

    @Autowired
    CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        customerRepository.deleteAll();
    }

    @Test
    void shouldGetAllCustomers() {
        List<Customer> customers = List.of(
                new Customer(1L, "John", "john@mail.com"),
                new Customer(2L, "Dennis", "dennis@mail.com")
        );
        customerRepository.saveAll(customers);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/customers")
                .then()
                .statusCode(200)
                .body(".", hasSize(2));

        List<Customer> dbCustomers = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/customers")
                .then()
                .extract()
                .body()
                .jsonPath().getList(".", Customer.class);

        assertEquals(dbCustomers.size(), 2);
        assertTrue(customers.stream().anyMatch(c -> c.getId() == 1L));
    }
}
