package uk.ac.rhul.cs3821.dto.auth;

/**
 * Data transfer object returned upon successful authentication. Contains the
 * generated JSON Web Token (JWT) used to authorize subsequent requests to
 * protected endpoints.
 *
 * @param token the signed JWT assigned to the authenticated user
 */

public record TokenResponse(String token) {
}
