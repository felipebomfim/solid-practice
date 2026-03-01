package com.example.solidpractice.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.solidpractice.task.dto.CreateTaskRequest;
import com.example.solidpractice.task.dto.TaskResponse;
import com.example.solidpractice.task.dto.UpdateTaskRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Nested
    @DisplayName("list")
    class ListTasks {

        @Test
        void returnsAllTasksOrderedByCreatedAtDesc() {
            Task task1 = taskWithId(1L, "A", false);
            Task task2 = taskWithId(2L, "B", false);
            TaskResponse resp1 = response(1L, "A", false);
            TaskResponse resp2 = response(2L, "B", false);

            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(task1, task2));
            when(taskMapper.toResponse(task1)).thenReturn(resp1);
            when(taskMapper.toResponse(task2)).thenReturn(resp2);

            List<TaskResponse> result = taskService.list(null);

            assertThat(result).containsExactly(resp1, resp2);
            verify(taskRepository).findAllByOrderByCreatedAtDesc();
        }

        @Test
        void returnsTasksFilteredByCompleted() {
            Task task = taskWithId(1L, "Done", true);
            TaskResponse resp = response(1L, "Done", true);

            when(taskRepository.findByCompletedOrderByCreatedAtDesc(true)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(resp);

            List<TaskResponse> result = taskService.list(true);

            assertThat(result).containsExactly(resp);
            verify(taskRepository).findByCompletedOrderByCreatedAtDesc(true);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        void returnsTaskWhenExists() {
            Task task = taskWithId(1L, "Title", false);
            TaskResponse resp = response(1L, "Title", false);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskMapper.toResponse(task)).thenReturn(resp);

            Optional<TaskResponse> result = taskService.getById(1L);

            assertThat(result).contains(resp);
        }

        @Test
        void returnsEmptyWhenNotExists() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<TaskResponse> result = taskService.getById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        void createsAndReturnsTaskResponse() {
            CreateTaskRequest request = new CreateTaskRequest("New task", "Desc");
            Task entity = taskWithId(1L, "New task", false);
            TaskResponse resp = response(1L, "New task", false);

            when(taskMapper.toEntity(request)).thenReturn(entity);
            when(taskRepository.save(any(Task.class))).thenReturn(entity);
            when(taskMapper.toResponse(entity)).thenReturn(resp);

            TaskResponse result = taskService.create(request);

            assertThat(result).isEqualTo(resp);
            verify(taskRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void updatesAndReturnsTaskResponseWhenExists() {
            Long id = 1L;
            UpdateTaskRequest request = new UpdateTaskRequest("Updated", null, null);
            Task existing = taskWithId(id, "Old", false);
            Task saved = taskWithId(id, "Updated", false);
            TaskResponse resp = response(id, "Updated", false);

            when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(resp);

            Optional<TaskResponse> result = taskService.update(id, request);

            assertThat(result).contains(resp);
            verify(taskMapper).applyUpdate(existing, request);
        }

        @Test
        void returnsEmptyWhenNotExists() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<TaskResponse> result = taskService.update(999L,
                    new UpdateTaskRequest("Any", null, null));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markComplete")
    class MarkComplete {

        @Test
        void marksCompleteAndReturnsTaskResponseWhenExists() {
            Long id = 1L;
            Task task = taskWithId(id, "Task", false);
            task.setCompleted(true);
            TaskResponse resp = response(id, "Task", true);

            when(taskRepository.findById(id)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(resp);

            Optional<TaskResponse> result = taskService.markComplete(id);

            assertThat(result).contains(resp);
            verify(taskRepository).save(task);
        }

        @Test
        void returnsEmptyWhenNotExists() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<TaskResponse> result = taskService.markComplete(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        void returnsTrueWhenTaskExisted() {
            when(taskRepository.existsById(1L)).thenReturn(true);

            boolean result = taskService.delete(1L);

            assertThat(result).isTrue();
            verify(taskRepository).deleteById(1L);
        }

        @Test
        void returnsFalseWhenTaskDidNotExist() {
            when(taskRepository.existsById(999L)).thenReturn(false);

            boolean result = taskService.delete(999L);

            assertThat(result).isFalse();
            verify(taskRepository).existsById(999L);
        }
    }

    private static Task taskWithId(Long id, String title, boolean completed) {
        Task t = new Task();
        t.setId(id);
        t.setTitle(title);
        t.setCompleted(completed);
        t.setCreatedAt(NOW);
        t.setUpdatedAt(NOW);
        return t;
    }

    private static TaskResponse response(Long id, String title, boolean completed) {
        return new TaskResponse(id, title, "", completed, NOW, NOW);
    }
}
