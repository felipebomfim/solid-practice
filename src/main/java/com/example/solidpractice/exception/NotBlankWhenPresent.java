package com.example.solidpractice.exception;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Valida que o campo, quando presente (não nulo), não seja em branco.
 * Null é considerado válido (campo opcional).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NotBlankWhenPresentValidator.class)
public @interface NotBlankWhenPresent {

    String message() default "cannot be blank when provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
