package uk.ac.rhul.cs3821.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import uk.ac.rhul.cs3821.model.enums.LeaderboardPeriod;

/**
 * Leaderboard cache model for user's leaderboard statistics.
 */
@Entity
@Table(name = "leaderboard_cache")
@Getter
@Setter
public class LeaderboardCache {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Integer points = 0;

  @Column(nullable = false)
  private Integer pagesRead = 0;

  @Column(nullable = false)
  private Integer booksCompleted = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LeaderboardPeriod period;  // WEEKLY, MONTHLY, ALL_TIME

  @Column(nullable = false)
  private LocalDateTime computedAt;
}
