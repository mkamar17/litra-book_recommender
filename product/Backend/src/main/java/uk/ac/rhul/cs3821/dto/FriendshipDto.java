package uk.ac.rhul.cs3821.dto;

import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;

public class FriendshipDto {
  public Long friendshipId;
  public Long userId;
  public String email;
  public FriendshipStatus status;

  public FriendshipDto(Long friendshipId, Long userId, String email, FriendshipStatus status) {
    this.friendshipId = friendshipId;
    this.userId = userId;
    this.email = email;
    this.status = status;
  }
}
