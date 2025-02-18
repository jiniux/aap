package xyz.jiniux.aap.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmptyOrPatternValidator.class})
public @interface EmptyOrPattern {
    String regexp();

    String message() default "The string is neither empty nor it matches the pattern";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
