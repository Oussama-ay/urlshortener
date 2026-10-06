package com.nova.urlshortener.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import com.nova.urlshortener.user.UserRepository;
import com.nova.urlshortener.url.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    void setUp() {
      urlRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerNewEmailReturns201() throws Exception {
        mvc.perform(post("/api/auth/register").header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nova@example.com",
                                  "password": "my-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("nova@example.com"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        mvc.perform(post("/api/auth/register").header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nova@example.com",
                                  "password": "my-password"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register").header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nova@example.com",
                                  "password": "my-password"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: nova@example.com"));
    }

    @Test
    void loginFlowProtectsManagementAndKeepsRedirectPublic() throws Exception {
        String body = "{\"email\":\"flow@example.com\",\"password\":\"my-password\"}";
        mvc.perform(post("/api/auth/register").header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        var login = mvc.perform(post("/api/auth/login").header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().path("access_token", "/"))
                .andReturn().getResponse();
        Cookie authorization = login.getCookie("access_token");
        String created = mvc.perform(post("/api/urls").header("Origin", "http://localhost:5173").cookie(authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/auth-flow\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String code = objectMapper.readTree(created).get("shortCode").asText();
        mvc.perform(get("/api/urls").cookie(authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/urls/" + code).cookie(authorization))
                .andExpect(status().isOk());
        mvc.perform(get("/" + code)).andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/auth-flow"));
        for (String invalidToken : new String[]{"", "invalid"}) {
            mvc.perform(get("/api/urls").cookie(new Cookie("access_token", invalidToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }
        mvc.perform(post("/api/auth/login").header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"flow@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void openApiDocumentsCookieAuthAndPublicOperations() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.name").value("access_token"))
                .andExpect(jsonPath("$.security[0].cookieAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/{shortCode}'].get.security").isEmpty());
    }

    @Test
    void logoutClearsCookieAndWritesRequireTrustedOrigin() throws Exception {
        mvc.perform(post("/api/auth/logout").header("Origin", "http://localhost:5173"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().path("access_token", "/"));
        for (String origin : new String[]{"https://attacker.example", "null", "http://localhost:5173.attacker.example"}) {
            mvc.perform(post("/api/auth/logout").header("Origin", origin))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
        mvc.perform(get("/api/urls").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/urls")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

}