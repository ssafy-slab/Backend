package com.ssafy.ssafy_slap.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidEmailFormat() {
        SignupRequest request = new SignupRequest("not-email", "password123", "tester");

        var violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
    }

    @Test
    void rejectsPasswordWithoutNumber() {
        SignupRequest request = new SignupRequest("test@example.com", "password", "tester");

        var violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }

    @Test
    void rejectsPasswordWithoutLetter() {
        SignupRequest request = new SignupRequest("test@example.com", "12345678", "tester");

        var violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }

    @Test
    void acceptsPasswordWithLetterAndNumber() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "tester");

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
