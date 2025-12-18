package uk.ac.rhul.cs3821.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * User entity class represented an authenticated application user.
 * Each user is uniquely identifiable by their email address and id.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> roles;

  // this table represents the relation between user and book
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "use_library",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "book_id")
  )
  private Set<Book> library;
}
