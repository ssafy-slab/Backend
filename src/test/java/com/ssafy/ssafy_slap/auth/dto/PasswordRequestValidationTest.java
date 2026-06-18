package com.ssafy.ssafy_slap.auth.dto;

import com.ssafy.ssafy_slap.user.dto.PasswordChangeRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsResetPasswordWithoutNumber() {
        var violations = validator.validate(new PasswordResetRequest("test@example.com", "password"));

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void rejectsChangePasswordShorterThanEightCharacters() {
        var violations = validator.validate(new PasswordChangeRequest("oldPassword1", "new1"));

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void acceptsValidPasswordRequests() {
        assertThat(validator.validate(new PasswordResetRequest("test@example.com", "newPassword2"))).isEmpty();
        assertThat(validator.validate(new PasswordChangeRequest("oldPassword1", "newPassword2"))).isEmpty();
    }
}
