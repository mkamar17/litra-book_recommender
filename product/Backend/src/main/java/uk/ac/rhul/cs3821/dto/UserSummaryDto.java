package uk.ac.rhul.cs3821.dto;

public class UserSummaryDto {
  public Long id;
  public String email;
  public int totalPoints;

  public UserSummaryDto(Long id, String email, int totalPoints) {
    this.id = id;
    this.email = email;
    this.totalPoints = totalPoints;
  }
}
