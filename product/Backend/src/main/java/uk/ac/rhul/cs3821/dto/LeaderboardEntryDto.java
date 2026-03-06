package uk.ac.rhul.cs3821.dto;

/**
 * Data transfer object for returning a single leaderboard entry in API responses.
 * Represents a user's ranking stats for a given leaderboard period.
 */
public class LeaderboardEntryDto {
  public String email;
  public int points;
  public int pagesRead;
  public int booksCompleted;
  public String period;

  /**
   * Constructs a LeaderboardEntryDto with all fields.
   *
   * @param email          the user's email
   * @param points         points earned in the period
   * @param pagesRead      pages read in the period
   * @param booksCompleted books completed in the period
   * @param period         the leaderboard period label
   */
  public LeaderboardEntryDto(String email, int points, int pagesRead,
                             int booksCompleted, String period) {
    this.email = email;
    this.points = points;
    this.pagesRead = pagesRead;
    this.booksCompleted = booksCompleted;
    this.period = period;
  }
}