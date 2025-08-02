package ru.noleg.bankcards.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.noleg.bankcards.validator.NotPastYearMonthValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotPastYearMonthValidator.class)
public @interface NotPastYearMonth {

    String message() default "Expiration date cannot be in the past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}