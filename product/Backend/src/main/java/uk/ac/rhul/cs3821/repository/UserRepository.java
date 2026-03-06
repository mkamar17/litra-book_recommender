package uk.ac.rhul.cs3821.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.User;

/**
 * User repository to link User entity to database.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Retrieves a User entity by email.
   *
   * @param email the email address of the User.
   * @return an Optional containing the matching User if found or empty.
   */

  Optional<User> findByEmail(String email);

  /**
   * Returns true or false depending on whether a user exists or not.
   *
   * @param email the email address to check for existence.
   * @return true if the user with the specified email address exists.
   */

  boolean existsByEmail(String email);
  
  /**
   * Returns all users whose email contains the given string, case-insensitively.
   * Used for friend search functionality.
   *
   * @param email partial email string to search by
   * @return list of matching users
   */
  List<User> findByEmailContainingIgnoreCase(String email);
}