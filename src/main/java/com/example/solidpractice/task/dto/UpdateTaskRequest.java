package com.example.solidpractice.task.dto;

import com.example.solidpractice.exception.NotBlankWhenPresent;

/**
 * DTO de entrada para atualização de tarefa (PUT /api/tasks/{id}).
 * Todos os campos opcionais; apenas os enviados no body são atualizados.
 * Se title for enviado, não pode ser em branco.
 */
public record UpdateTaskRequest(
                @NotBlankWhenPresent(message = "title cannot be blank when provided") String title,
                String description,
                Boolean completed) {
}
