package uk.ac.man.cs.eventlite.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressValidator implements ConstraintValidator<ValidAddress, String> {

    // This pattern assumes the full address ends with a comma followed by the
    // postcode.
    // The regex below is a simplified version and checks that there is a comma
    // before the postcode.
    private static final Pattern PATTERN = Pattern.compile(".*,\\s*([A-Z]{1,2}[0-9][0-9A-Z]?\\s*[0-9][A-Z]{2})$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(ValidAddress constraintAnnotation) {
    }

    @Override
    public boolean isValid(String address, ConstraintValidatorContext context) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = PATTERN.matcher(address.trim());
        if (!matcher.matches()) {
            // Case 1: No postcode at all
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid Address").addConstraintViolation();
            return false;
        }

        // Case 2: Postcode present, but missing address part before comma
        String roadPart = matcher.group(1).trim(); // before the comma
        if (roadPart.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid Address").addConstraintViolation();
            return false;
        }

        // If both parts are present
        return true;
    }
}