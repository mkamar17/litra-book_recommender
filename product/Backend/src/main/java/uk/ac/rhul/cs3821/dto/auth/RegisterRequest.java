package uk.ac.rhul.cs3821.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object representing a registration request. Contains the
 * information required to create a new user account within the system.
 *
 * @param email    the email address to associate with the new user account
 * @param password the raw password supplied during account creation
 */

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {
}