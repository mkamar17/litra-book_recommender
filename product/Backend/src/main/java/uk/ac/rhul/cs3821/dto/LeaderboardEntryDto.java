package uk.ac.rhul.cs3821.dto;

public class LeaderboardEntryDto {
  public String email;
  public int points;
  public int pagesRead;
  public int booksCompleted;
  public String period;

  public LeaderboardEntryDto(String email, int points, int pagesRead,
                             int booksCompleted, String period) {
    this.email = email;
    this.points = points;
    this.pagesRead = pagesRead;
    this.booksCompleted = booksCompleted;
    this.period = period;
  }
}