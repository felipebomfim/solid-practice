package com.example.solidpractice.task;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.solidpractice.AbstractIntegrationTest;
import com.example.solidpractice.task.dto.TaskResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class ListTasks {

            // Optionally verify the ordering manually, since JsonPathResultMatchers doesn't
            // support isBefore check.
            // Example: Parse and compare in the test body if strict check is desired.
            // Let's do it
        @Test
        void returnsListOfTasks() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].title").isString())
                    .andExpect(jsonPath("$[0].completed").isBoolean())
                    .andExpect(jsonPath("$[0].createdAt").isString())
                    .andExpect(jsonPath("$[0].updatedAt").isString());
        }

        @Test
        void filterByCompletedFalse() throws Exception {
            mockMvc.perform(get("/api/tasks").param("completed", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].completed", everyItem(is(false))));
        }

        @Test
        void filterByCompletedTrue() throws Exception {
            mockMvc.perform(get("/api/tasks").param("completed", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].completed", everyItem(is(true))));
        }

        @Test
        void returnsTasksOrderedByCreatedAtDesc() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            List<TaskResponse> tasks = objectMapper.readValue(mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andReturn().getResponse().getContentAsString(), new TypeReference<List<TaskResponse>>() {
                    });

            // Check that tasks are sorted by createdAt in descending order (pairs i, i+1)
            for (int i = 0; i < Math.min(tasks.size(), 10) - 1; i++) {
                Instant current = tasks.get(i).createdAt();
                Instant next = tasks.get(i + 1).createdAt();
                assertThat(current.compareTo(next), greaterThanOrEqualTo(0));
            }
        }
    }

    class GetTaskById {

        @Test
        void returnsTaskWhenExists() throws Exception {
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").isString())
                    .andExpect(jsonPath("$.description").exists())
                    .andExpect(jsonPath("$.completed").isBoolean())
                    .andExpect(jsonPath("$.createdAt").isString())
                    .andExpect(jsonPath("$.updatedAt").isString());
        }

        @Test
        void returns404WhenNotExists() throws Exception {
            mockMvc.perform(get("/api/tasks/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        void createsTaskWithTitleOnly() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("title", "Integration test task"));
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("Integration test task"))
                    .andExpect(jsonPath("$.description").value(""))
                    .andExpect(jsonPath("$.completed").value(false));
        }

        @Test
        void createsTaskWithTitleAndDescription() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "title", "Task with description",
                    "description", "Some details here"));
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Task with description"))
                    .andExpect(jsonPath("$.description").value("Some details here"));
        }

        @Test
        void returns400WhenTitleMissing() throws Exception {
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("title is required"));
        }

        @Test
        void returns400WhenTitleBlank() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("title", "   "));
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("title is required"));
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{id}")
    class UpdateTask {

        @Test
        void updatesTask() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "title", "Updated title",
                    "description", "Updated description",
                    "completed", true));
            mockMvc.perform(put("/api/tasks/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Updated title"))
                    .andExpect(jsonPath("$.description").value("Updated description"))
                    .andExpect(jsonPath("$.completed").value(true));
        }

        @Test
        void returns404WhenNotExists() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("title", "Any"));
            mockMvc.perform(put("/api/tasks/99999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returns400WhenTitleBlank() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("title", "   "));
            mockMvc.perform(put("/api/tasks/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("title cannot be blank when provided"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{id}/complete")
    class MarkComplete {

        @Test
        void marksTaskAsComplete() throws Exception {
            mockMvc.perform(patch("/api/tasks/2/complete").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));
        }

        @Test
        void returns404WhenNotExists() throws Exception {
            mockMvc.perform(patch("/api/tasks/99999/complete"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {

        @Test
        void deletesTaskAndReturns204() throws Exception {
            String createBody = objectMapper.writeValueAsString(Map.of("title", "To be deleted"));
            String createResponse = mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            Long id = objectMapper.readTree(createResponse).get("id").longValue();

            mockMvc.perform(delete("/api/tasks/" + id))
                    .andExpect(status().isNoContent());
            mockMvc.perform(get("/api/tasks/" + id))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returns404WhenNotExists() throws Exception {
            mockMvc.perform(delete("/api/tasks/99999"))
                    .andExpect(status().isNotFound());
        }
    }
}
