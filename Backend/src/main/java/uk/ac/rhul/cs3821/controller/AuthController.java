package uk.ac.rhul.cs3821.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uk.ac.rhul.cs3821.dto.auth.LoginRequest;
import uk.ac.rhul.cs3821.dto.auth.RegisterRequest;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, JwtService jwt, UserRepository repo, PasswordEncoder enc) {
        this.authManager = authManager; this.jwt = jwt; this.repo = repo; this.encoder = enc;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (repo.existsByEmail(req.email())) return ResponseEntity.badRequest().body("Email taken");
        var u = new User();
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setRoles(java.util.Set.of("USER"));
        repo.save(u);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String token = jwt.generate(auth.getName(), Map.of("roles", auth.getAuthorities()));
        return ResponseEntity.ok(new TokenResponse(token));
    }
}