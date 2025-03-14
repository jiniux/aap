package xyz.jiniux.aap.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LongValidator implements ConstraintValidator<ValidLong, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Long.parseLong(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
