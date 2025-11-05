package uk.ac.rhul.cs3821.service;

import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.ac.rhul.cs3821.repository.UserRepository;

@Service
public class AppUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
  private final UserRepository repo;

  public AppUserDetailsService(UserRepository repo) {
    this.repo = repo;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    var authorities = u.getRoles().stream()
        .map(r -> "ROLE_".concat(r))
        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
    return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), authorities);
  }
}
