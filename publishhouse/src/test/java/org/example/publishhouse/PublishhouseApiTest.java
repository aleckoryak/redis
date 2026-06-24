package org.example.publishhouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:publishhouse;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class PublishhouseApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateArticleAndExposeRatingAndCommentsCount() throws Exception {
        long articleId = createArticle("API Article 1");

        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "Great",
                                "score", 80
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + articleId + ")].averageRating").value(hasItem(80.0)))
                .andExpect(jsonPath("$[?(@.id==" + articleId + ")].commentsCount").value(hasItem(1)));
    }

    @Test
    void shouldReturnCommentsForSpecificArticleRoute() throws Exception {
        long articleId = createArticle("API Article 2");

        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "First",
                                "score", 55
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "Second",
                                "score", 75
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/articles/" + articleId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].articleId").value((int) articleId));
    }

    @Test
    void shouldReturnAnyTopTrendingArticleByAverageScore() throws Exception {
        long topArticleId = createArticle("API Article 3");
        long lowerArticleId = createArticle("API Article 4");

        mockMvc.perform(post("/articles/" + topArticleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "Good",
                                "score", 90
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/articles/" + lowerArticleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "Average",
                                "score", 60
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) topArticleId))
                .andExpect(jsonPath("$.averageRating").value(90.0));
    }

    @Test
    void shouldRejectScoreOutOfRange() throws Exception {
        long articleId = createArticle("API Article 5");

        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "text", "Invalid",
                                "score", 101
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnSingleArticleById() throws Exception {
        long articleId = createArticle("Single Article");

        mockMvc.perform(get("/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) articleId))
                .andExpect(jsonPath("$.title").value("Single Article"));
    }

    @Test
    void shouldReturn404ForUnknownArticleId() throws Exception {
        mockMvc.perform(get("/articles/999999"))
                .andExpect(status().isNotFound());
    }

    private long createArticle(String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", title,
                                "text", "Article text"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }
}
