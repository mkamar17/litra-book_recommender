package uk.ac.rhul.cs3821.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}