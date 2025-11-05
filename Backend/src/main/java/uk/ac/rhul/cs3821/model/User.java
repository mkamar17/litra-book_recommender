package uk.ac.rhul.cs3821.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.Set;

/**
 * User entity class represented an authenticated application user.
 * Each user is uniquely identifiable by their email address and id.
 */
@Entity
public class User {
  @Id
  @GeneratedValue
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> roles;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }
}
