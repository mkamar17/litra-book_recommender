package uk.ac.rhul.cs3821.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity represents a user.
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

  @Column(nullable = false)
  private int totalPoints = 0;

  @Column(nullable = false)
  private int weeklyPoints = 0;

  @Column(nullable = false)
  private int monthlyPoints = 0;

  @Column(nullable = false)
  private int currentStreak = 0;

  @Column(nullable = false)
  private int longestStreak = 0;

  @Column
  private LocalDate lastReadDate;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "use_library",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "book_id")
  )
  private Set<Book> library;

  @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
  @JsonIgnore
  private List<Friendship> sentRequests = new ArrayList<>();

  @OneToMany(mappedBy = "addressee", cascade = CascadeType.ALL)
  @JsonIgnore
  private List<Friendship> receivedRequests = new ArrayList<>();
}