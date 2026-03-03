package uk.ac.rhul.cs3821.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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

  // to represent all points earned
  @Column(nullable = false)
  private int totalPoints = 0;

  // this table represents the relation between user and book
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "use_library",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "book_id")
  )

  @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
  private List<Friendship> sentRequests = new ArrayList<>();

  @OneToMany(mappedBy = "addressee", cascade = CascadeType.ALL)
  private List<Friendship> receivedRequests = new ArrayList<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private LeaderboardCache leaderboardStats;

  @Column(nullable = false)
  private int weeklyPoints = 0;

  @Column(nullable = false)
  private int monthlyPoints = 0;

  private Set<Book> library;
}
