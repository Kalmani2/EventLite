package uk.ac.man.cs.eventlite.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = AddressValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidAddress {

    String message() default "Address must contain a road address followed by a valid UK postcode (e.g., '123 Main Street, AB1 2CD')";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}