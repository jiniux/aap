package xyz.jiniux.aap.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "new|like-new|very-good|acceptable|worn|digital")
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidStockQuality {
    String message() default "Invalid stock stockQuality";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
