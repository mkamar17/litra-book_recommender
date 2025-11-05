package uk.ac.rhul.cs3821.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.rhul.cs3821.dto.auth.LoginRequest;
import uk.ac.rhul.cs3821.dto.auth.RegisterRequest;
import uk.ac.rhul.cs3821.dto.auth.TokenResponse;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.JwtService;

/**
 * Controller to handle user authentication on login/signup.
 */

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthenticationManager authManager;
  private final JwtService jwt;
  private final UserRepository repo;
  private final PasswordEncoder encoder;

  /**
   * Public constructor for AuthController.
   *
   * @param authManager to handle authentication and validate credentials.
   * @param jwt         to handle token generation.
   * @param repo        to manage user repository.
   * @param enc         to securely hash passwords.
   */
  public AuthController(AuthenticationManager authManager, JwtService jwt,
                        UserRepository repo, PasswordEncoder enc) {
    this.authManager = authManager;
    this.jwt = jwt;
    this.repo = repo;
    this.encoder = enc;
  }

  /**
   * Handles user registration requests. If the provided email is already in use,
   * the request is rejected. Otherwise, a new user record is created and persisted.
   *
   * @param req the registration request containing user credentials
   * @return {@link ResponseEntity} indicating success or failure
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    if (repo.existsByEmail(req.email())) {
      return ResponseEntity.badRequest().body("Email taken");
    }
    var u = new User();
    u.setEmail(req.email());
    u.setPassword(encoder.encode(req.password()));
    u.setRoles(java.util.Set.of("USER"));
    repo.save(u);
    return ResponseEntity.ok().build();
  }

  /**
   * Authenticates the provided user credentials. If authentication succeeds, a JWT
   * token is generated and returned to the client. This token can then be used to
   * access protected endpoints.
   *
   * @param req the login request containing the user's email and password
   * @return {@link ResponseEntity} containing a generated {@link TokenResponse}
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
    Authentication auth = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.email(), req.password()));
    String token = jwt.generate(auth.getName(), Map.of("roles", auth.getAuthorities()));
    return ResponseEntity.ok(new TokenResponse(token));
  }
}