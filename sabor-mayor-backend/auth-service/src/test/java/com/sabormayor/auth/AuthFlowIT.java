package com.sabormayor.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "management.tracing.enabled=false"
})
class AuthFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void fullAuthLifecycle_register_login_refresh_logout() {
        String email = "it-user@sabormayor.com";
        String password = "Password123!";

        // Register
        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s","fullName":"IT User"}
                        """.formatted(email, password))
                .when().post("/api/auth/register")
                .then().statusCode(201)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .extract().path("refreshToken");

        // Duplicate registration -> 409
        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s","fullName":"IT User"}
                        """.formatted(email, password))
                .when().post("/api/auth/register")
                .then().statusCode(409);

        // Login
        String accessToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");

        // Wrong password -> 401
        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"wrong-password"}
                        """.formatted(email))
                .when().post("/api/auth/login")
                .then().statusCode(401);

        // Me
        given().header("Authorization", "Bearer " + accessToken)
                .when().get("/api/auth/me")
                .then().statusCode(200)
                .body("email", equalTo(email))
                .body("role", equalTo("CLIENTE"));

        // Refresh rotates the token
        String rotatedRefresh = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"refreshToken":"%s"}
                        """.formatted(refreshToken))
                .when().post("/api/auth/refresh")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .extract().path("refreshToken");

        // Reusing the rotated (old) refresh token must fail
        given().contentType(ContentType.JSON)
                .body("""
                        {"refreshToken":"%s"}
                        """.formatted(refreshToken))
                .when().post("/api/auth/refresh")
                .then().statusCode(401);

        // Logout revokes everything, including the access token via Redis denylist
        given().header("Authorization", "Bearer " + accessToken)
                .when().post("/api/auth/logout")
                .then().statusCode(204);

        given().contentType(ContentType.JSON)
                .body("""
                        {"refreshToken":"%s"}
                        """.formatted(rotatedRefresh))
                .when().post("/api/auth/refresh")
                .then().statusCode(401);

        given().header("Authorization", "Bearer " + accessToken)
                .when().get("/api/auth/me")
                .then().statusCode(401);
    }

    @Test
    void seededAdminCanCreateStaffButNotSuperAdmin() {
        String adminToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"admin@sabormayor.com","password":"Admin123!"}
                        """)
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");

        given().header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"nuevo.mesero@sabormayor.com","password":"Mesero123!","fullName":"Nuevo Mesero","role":"MESERO"}
                        """)
                .when().post("/api/auth/staff")
                .then().statusCode(201)
                .body("role", equalTo("MESERO"));

        // ADMIN cannot create another ADMIN (only SUPER_ADMIN can)
        given().header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"otro.admin@sabormayor.com","password":"Admin123!","fullName":"Otro Admin","role":"ADMIN"}
                        """)
                .when().post("/api/auth/staff")
                .then().statusCode(403);

        // CLIENTE cannot create staff at all
        String clientToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"cliente@sabormayor.com","password":"Cliente123!"}
                        """)
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");

        given().header("Authorization", "Bearer " + clientToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"x@sabormayor.com","password":"Password123!","fullName":"X","role":"MESERO"}
                        """)
                .when().post("/api/auth/staff")
                .then().statusCode(403);
    }

    @Test
    void mockOAuthLoginCreatesCustomerAccount() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"idToken":"mock:oauth.user@gmail.com:OAuth User"}
                        """)
                .when().post("/api/auth/oauth2/google")
                .then().statusCode(200)
                .body("accessToken", notNullValue());

        given().contentType(ContentType.JSON)
                .body("""
                        {"idToken":"not-a-mock-token"}
                        """)
                .when().post("/api/auth/oauth2/google")
                .then().statusCode(401);
    }

    @Test
    void jwksEndpointExposesPublicKey() {
        given().when().get("/.well-known/jwks.json")
                .then().statusCode(200)
                .body("keys[0].kty", equalTo("RSA"));
    }
}
