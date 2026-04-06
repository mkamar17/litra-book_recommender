package uk.ac.rhul.cs3821.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FriendshipTest {

  private User requester;
  private User addressee;
  private Friendship friendship;

  @BeforeEach
  void setUp() {
    requester = new User();
    requester.setId(1L);
    requester.setEmail("requester@example.com");

    addressee = new User();
    addressee.setId(2L);
    addressee.setEmail("addressee@example.com");

    friendship = new Friendship();
    friendship.setRequester(requester);
    friendship.setAddressee(addressee);
    friendship.setStatus(FriendshipStatus.PENDING);
  }

  @Test
  void settersAndGettersWork() {
    assertEquals(requester, friendship.getRequester());
    assertEquals(addressee, friendship.getAddressee());
    assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
  }

  @Test
  void statusCanBeUpdatedToAccepted() {
    friendship.setStatus(FriendshipStatus.ACCEPTED);
    assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
  }

  @Test
  void statusCanBeUpdatedToBlocked() {
    friendship.setStatus(FriendshipStatus.BLOCKED);
    assertEquals(FriendshipStatus.BLOCKED, friendship.getStatus());
  }

  @Test
  void noArgsConstructorCreatesEmptyObject() {
    Friendship empty = new Friendship();
    assertNull(empty.getId());
    assertNull(empty.getRequester());
    assertNull(empty.getAddressee());
    assertNull(empty.getStatus());
  }

  @Test
  void requesterAndAddresseeAreDistinct() {
    assertNotNull(friendship.getRequester());
    assertNotNull(friendship.getAddressee());
    assertEquals(1L, friendship.getRequester().getId());
    assertEquals(2L, friendship.getAddressee().getId());
  }
}