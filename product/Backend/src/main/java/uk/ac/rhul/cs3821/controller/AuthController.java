package uk.ac.rhul.cs3821.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
  private static final int MAX_ATTEMPTS = 5;
  private static final long WINDOW_MS = 60_000;
  private final AuthenticationManager authManager;
  private final JwtService jwt;
  private final UserRepository repo;
  private final PasswordEncoder encoder;
  private final Map<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();

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
   * Checks whether the given IP has exceeded the allowed login attempts
   * within the time window. Adds the current attempt timestamp if not blocked.
   *
   * @param ip the client's IP address
   * @return true if the IP is rate limited, false otherwise
   */
  private boolean isRateLimited(String ip) {
    long now = System.currentTimeMillis();
    loginAttempts.putIfAbsent(ip, new ArrayList<>());
    List<Long> attempts = loginAttempts.get(ip);

    synchronized (attempts) {
      attempts.removeIf(t -> now - t > WINDOW_MS);
      if (attempts.size() >= MAX_ATTEMPTS) {
        return true;
      }
      attempts.add(now);
    }
    return false;
  }

  /**
   * Handles user registration requests. If the provided email is already in use,
   * the request is rejected. Otherwise, a new user record is created and persisted.
   *
   * @param req the registration request containing user credentials
   * @return {@link ResponseEntity} indicating success or failure
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
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
   * @return a ResponseEntity containing a generated TokenResponse or 429 too many requests error if IP rate is limited
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
    String ip = request.getRemoteAddr();
    if (isRateLimited(ip)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many login attempts. Please try again later.");
    }

    Authentication auth = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.email(), req.password()));
    String token = jwt.generate(auth.getName(), Map.of("roles", auth.getAuthorities()));
    return ResponseEntity.ok(new TokenResponse(token));
  }

  /**
   * Handles failed authentication attempts by returning a 401 Unauthorized response.
   *
   * @param e the exception thrown when credentials are invalid
   * @return ResponseEntity with a 401 status and error message
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
  }
}