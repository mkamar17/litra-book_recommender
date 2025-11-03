package uk.ac.rhul.cs3821.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.AppUser;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}