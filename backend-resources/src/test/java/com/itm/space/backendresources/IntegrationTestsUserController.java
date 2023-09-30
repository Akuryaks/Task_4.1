package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Интеграционное тестирование UserController")
public class IntegrationTestsUserController extends KeycloakTestContainers {

    @BeforeAll
    public static void setUp(){
        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", keycloak.getAuthServerUrl() + "/realms/ITM");
        System.setProperty("keycloak.auth-server-url", keycloak.getAuthServerUrl());
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private String getToken() {
        return given().contentType("application/x-www-form-urlencoded")
                .formParams(Map.of(
                        "username", "akuryak",
                        "password", "test",
                        "grant_type", "password",
                        "client_id", "backend-gateway-client",
                        "client_secret", "secret"
                ))
                .post(keycloak.getAuthServerUrl() + "/realms/ITM/protocol/openid-connect/token")
                .then().assertThat().statusCode(200).extract()
                .path("access_token");
    }

    @Test
    @DisplayName("Отправляю токен пользователя, когда получаю данные пользователя, должен вернуть данные пользователя")
    void givenUserToken_whenGetUser_shouldReturnUserInfo() {
        Response response = given().auth().oauth2(getToken())
                .when()
                .get("http://localhost:9191/api/users/25d9535d-601f-4840-809c-5a8f5ab00d71");
        response.then()
                .statusCode(200)
                .body("firstName", equalTo(""))
                .body("lastName", equalTo(""))
                .body("email", equalTo(null))
                .body("email", equalTo(null));
        System.out.println(response.getBody().asString());
    }

    @Test
    @DisplayName("Отправляю токен пользователя, когда получаю данные пользователя, должен вернуть UUID пользователя")
    void givenUserToken_whenGetUser_shouldReturnUuid() {
        Response response = given()
                .auth().oauth2(getToken())
                .when()
                .get( "http://localhost:9191/api/users/hello");
        response.then().statusCode(200);

        assertEquals("25d9535d-601f-4840-809c-5a8f5ab00d71", response.getBody().asString());
    }

    @Test
    @DisplayName("Отправляю токен пользователя, когда отправляю данные нового пользователя, должен вернуть нового пользователя")
    void givenUserToken_whenPostNewUser_shouldAddNewUser() {
        given().auth().oauth2(getToken())
                .header("Content-type", "application/json").when()
                .and().body(new UserRequest("test", "test@test.com", "test", "test", "test"))
                .when().post("http://localhost:9191/api/users")
                .then()
                .statusCode(200);
    }

    /*@Test
    @DisplayName("test")
    void test() throws InterruptedException {
        Thread.sleep(1000000000);
    }*/
}
