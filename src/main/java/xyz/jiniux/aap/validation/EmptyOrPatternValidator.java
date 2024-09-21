package xyz.jiniux.aap.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmptyOrPatternValidator implements ConstraintValidator<EmptyOrPattern, String> {
    private String pattern;

    @Override
    public void initialize(EmptyOrPattern params) {
        this.pattern = params.regexp();
    }


    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null)
            return true;

        return s.isEmpty() || s.matches(this.pattern);
    }
}
