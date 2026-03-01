package com.example.solidpractice.task;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intentionally non-SOLID: this controller does too much (SRP violation),
 * contains business logic, manual DTO mapping, and depends on concrete repository (DIP).
 * Refactor later applying SOLID principles.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listAll(@RequestParam(required = false) Boolean completed) {
        List<Task> tasks;
        if (completed != null) {
            tasks = taskRepository.findByCompletedOrderByCreatedAtDesc(completed);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> ResponseEntity.ok(toMap(task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title is required"));
        }
        Task task = new Task();
        task.setTitle(title.trim());
        if (body.containsKey("description")) {
            task.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        }
        task.setCompleted(false);
        task = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (body.containsKey("title")) {
                        String title = body.get("title").toString();
                        if (title == null || title.isBlank()) {
                            throw new IllegalArgumentException("title cannot be blank");
                        }
                        task.setTitle(title.trim());
                    }
                    if (body.containsKey("description")) {
                        task.setDescription(body.get("description") != null ? body.get("description").toString() : null);
                    }
                    if (body.containsKey("completed")) {
                        task.setCompleted(Boolean.TRUE.equals(body.get("completed")));
                    }
                    task = taskRepository.save(task);
                    return ResponseEntity.ok(toMap(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> markComplete(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setCompleted(true);
                    task = taskRepository.save(task);
                    return ResponseEntity.ok(toMap(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> toMap(Task task) {
        return Map.of(
                "id", task.getId(),
                "title", task.getTitle(),
                "description", task.getDescription() != null ? task.getDescription() : "",
                "completed", task.isCompleted(),
                "createdAt", task.getCreatedAt().toString(),
                "updatedAt", task.getUpdatedAt().toString()
        );
    }
}
