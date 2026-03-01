package com.example.solidpractice.task;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByCompletedOrderByCreatedAtDesc(boolean completed);
}
