package com.example.solidpractice.task.dto;

import java.time.Instant;

/**
 * DTO de saída da API para uma tarefa.
 * Formato JSON mantido compatível com o contrato atual (id, title, description,
 * completed, createdAt, updatedAt).
 */
public record TaskResponse(
                Long id,
                String title,
                String description,
                boolean completed,
                Instant createdAt,
                Instant updatedAt) {
}
