package uk.ac.rhul.cs3821.service;

import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.repository.UserRepository;

/**
 * Loads user information for authentication through Spring Security.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository repo;

  /**
   * Creates the service with the user repository.
   *
   * @param repo the repository used to find users
   */
  public AppUserDetailsService(UserRepository repo) {
    this.repo = repo;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    var authorities = u.getRoles().stream()
        .map(r -> "ROLE_".concat(r))
        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
    return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), authorities);
  }
}
