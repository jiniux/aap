package xyz.jiniux.aap.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "^(?!\\s*$).+")
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 70)
public @interface ValidAuthorLastName {
    String message() default "Invalid author name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
