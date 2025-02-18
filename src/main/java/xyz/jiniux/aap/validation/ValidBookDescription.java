package xyz.jiniux.aap.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EmptyOrPattern(regexp = "^(?!\\s*$).+")
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 5000)
public @interface ValidBookDescription {
    String message() default "Invalid publisher id";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}