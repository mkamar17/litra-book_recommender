package uk.ac.rhul.cs3821.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object representing a login request. Contains the credentials
 * required to authenticate a user through the authentication controller.
 *
 * @param email    the email address of the user attempting to log in
 * @param password the raw password provided during authentication
 */

public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {
}