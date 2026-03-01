package com.example.solidpractice.task.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para criação de tarefa (POST /api/tasks).
 * Title obrigatório; description opcional.
 */
public record CreateTaskRequest(
                @NotBlank(message = "title is required") String title,
                String description) {
}
