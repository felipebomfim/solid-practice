package com.example.solidpractice.task;

import org.springframework.stereotype.Component;

import com.example.solidpractice.task.dto.CreateTaskRequest;
import com.example.solidpractice.task.dto.TaskResponse;
import com.example.solidpractice.task.dto.UpdateTaskRequest;

/**
 * Responsabilidade única: conversão entre Task (entidade) e DTOs de API.
 * Usado pelo TaskService.
 */
@Component
public class TaskMapper {

    /**
     * Converte entidade em DTO de resposta.
     * Description vazio quando null, para manter contrato JSON atual.
     */
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "",
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    /**
     * Cria nova entidade Task a partir do DTO de criação.
     * Completed inicia como false (regra de negócio).
     */
    public Task toEntity(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description() != null ? request.description() : null);
        task.setCompleted(false);
        return task;
    }

    /**
     * Aplica campos presentes no DTO de atualização na entidade existente.
     * Apenas campos não nulos no request são aplicados.
     */
    public void applyUpdate(Task task, UpdateTaskRequest request) {
        if (request.title() != null) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.completed() != null) {
            task.setCompleted(request.completed());
        }
    }
}
