package uk.ac.rhul.cs3821.dto;

import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;

/**
 * Data transfer object for returning friendship data in API responses.
 * Represents one side of a friendship from the perspective of the other user.
 */
public class FriendshipDto {
  public Long friendshipId;
  public Long userId;
  public String email;
  public FriendshipStatus status;

  /**
   * Constructs a FriendshipDto with all fields.
   *
   * @param friendshipId the ID of the friendship record
   * @param userId       the ID of the other user
   * @param email        the email of the other user
   * @param status       the current friendship status
   */
  public FriendshipDto(Long friendshipId, Long userId, String email, FriendshipStatus status) {
    this.friendshipId = friendshipId;
    this.userId = userId;
    this.email = email;
    this.status = status;
  }
}
