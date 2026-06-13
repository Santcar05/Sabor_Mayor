package com.sabormayor.content;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "management.tracing.enabled=false"
})
class ContentServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    private RequestPostProcessor asCustomer(UUID id) {
        return jwt().jwt(j -> j.subject(id.toString()).claim("email", "c@x.com").claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("email", "a@x.com").claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void publicCanReadSeededPostsAndGallery() throws Exception {
        mockMvc.perform(get("/api/content/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/content/posts/renacer-cocina-latinoamericana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaTitle", Matchers.containsString("Sabor Mayor")));

        mockMvc.perform(get("/api/content/gallery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(3)));
    }

    @Test
    void editorPublishesPost_andCommentModerationFlow() throws Exception {
        String body = mockMvc.perform(post("/api/content/admin/posts").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Nuevo plato de temporada","body":"Contenido del articulo","published":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("nuevo-plato-de-temporada"))
                .andReturn().getResponse().getContentAsString();
        String postId = JsonPath.read(body, "$.id");

        // Customer posts a comment -> held for moderation, not visible yet
        String commentJson = mockMvc.perform(post("/api/content/posts/{id}/comments", postId)
                        .with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Que delicia!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approved").value(false))
                .andReturn().getResponse().getContentAsString();
        String commentId = JsonPath.read(commentJson, "$.id");

        mockMvc.perform(get("/api/content/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));

        // Editor approves -> now visible
        mockMvc.perform(post("/api/content/admin/comments/{id}/approve", commentId).with(asAdmin()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/content/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }

    @Test
    void customerCannotCreatePosts() throws Exception {
        mockMvc.perform(post("/api/content/admin/posts").with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"body\":\"Y\",\"published\":true}"))
                .andExpect(status().isForbidden());
    }
}
