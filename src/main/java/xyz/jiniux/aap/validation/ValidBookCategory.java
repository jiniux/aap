package xyz.jiniux.aap.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "fiction|non-fiction|science-fiction|mystery|fantasy|biography|history|self-help|thriller|romance|children|poetry|horror|technology|business")
@Target( {ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidBookCategory {
    String message() default "Invalid book category";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
