package com.example.solidpractice.task;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.solidpractice.task.dto.CreateTaskRequest;
import com.example.solidpractice.task.dto.TaskResponse;
import com.example.solidpractice.task.dto.UpdateTaskRequest;

/**
 * Responsabilidade única: casos de uso de tarefa (regras de negócio,
 * orquestração, repositório e mapper).
 * Não conhece HTTP nem ResponseEntity.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> list(Boolean completed) {
        List<Task> tasks = completed != null
                ? taskRepository.findByCompletedOrderByCreatedAtDesc(completed)
                : taskRepository.findAllByOrderByCreatedAtDesc();
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    public Optional<TaskResponse> getById(Long id) {
        return taskRepository.findById(id).map(taskMapper::toResponse);
    }

    public TaskResponse create(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public Optional<TaskResponse> update(Long id, UpdateTaskRequest request) {
        return taskRepository.findById(id)
                .map(task -> {
                    taskMapper.applyUpdate(task, request);
                    return taskMapper.toResponse(taskRepository.save(task));
                });
    }

    public Optional<TaskResponse> markComplete(Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setCompleted(true);
                    return taskMapper.toResponse(taskRepository.save(task));
                });
    }

    public boolean delete(Long id) {
        if (!taskRepository.existsById(id)) {
            return false;
        }
        taskRepository.deleteById(id);
        return true;
    }
}
