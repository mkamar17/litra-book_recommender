package uk.ac.rhul.cs3821.service;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppUserDetailsServiceTest {

  @Test
  void testLoadUserByUsernameSuccess() {

    UserRepository repo = mock(UserRepository.class);
    AppUserDetailsService service = new AppUserDetailsService(repo);

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("hashedpass");
    user.setRoles(Set.of("USER", "ADMIN"));

    when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    UserDetails result = service.loadUserByUsername("test@example.com");

    assertNotNull(result);
    assertEquals("test@example.com", result.getUsername());
    assertEquals("hashedpass", result.getPassword());
    assertTrue(result.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    assertTrue(result.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

    verify(repo, times(1)).findByEmail("test@example.com");
  }

  @Test
  void testLoadUserByUsernameUserNotFound() {

    UserRepository repo = mock(UserRepository.class);
    AppUserDetailsService service = new AppUserDetailsService(repo);

    when(repo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
    
    assertThrows(UsernameNotFoundException.class,
        () -> service.loadUserByUsername("missing@example.com"));

    verify(repo, times(1)).findByEmail("missing@example.com");
  }
}
