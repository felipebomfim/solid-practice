package com.example.solidpractice.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankWhenPresentValidator implements ConstraintValidator<NotBlankWhenPresent, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !value.isBlank();
    }
}
