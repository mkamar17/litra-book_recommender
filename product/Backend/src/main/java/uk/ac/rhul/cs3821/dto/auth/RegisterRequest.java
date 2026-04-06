package uk.ac.rhul.cs3821.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object representing a registration request. Contains the
 * information required to create a new user account within the system.
 *
 * @param email    the email address to associate with the new user account
 * @param password the raw password supplied during account creation
 */

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 128)
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one capital letter")
    @Pattern(regexp = ".*[^a-zA-Z0-9].*", message = "Password must contain at least one special character")
    String password
) {
}