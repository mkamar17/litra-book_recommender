package uk.ac.rhul.cs3821.dto;

/**
 * Data transfer object for returning a minimal user summary in API responses.
 * Used in contexts such as friend lists and user search results.
 */
public class UserSummaryDto {
  public Long id;
  public String email;
  public int totalPoints;

  /**
   * Constructs a UserSummaryDto with all fields.
   *
   * @param id          the user's ID
   * @param email       the user's email address
   * @param totalPoints the user's total points
   */
  public UserSummaryDto(Long id, String email, int totalPoints) {
    this.id = id;
    this.email = email;
    this.totalPoints = totalPoints;
  }
}
