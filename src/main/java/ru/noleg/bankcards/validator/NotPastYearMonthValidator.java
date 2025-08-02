package ru.noleg.bankcards.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.noleg.bankcards.validator.annotation.NotPastYearMonth;

import java.time.YearMonth;

public class NotPastYearMonthValidator implements ConstraintValidator<NotPastYearMonth, YearMonth> {

    @Override
    public boolean isValid(YearMonth value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(YearMonth.now());
    }
}