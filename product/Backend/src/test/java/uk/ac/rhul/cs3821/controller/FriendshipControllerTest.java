package uk.ac.rhul.cs3821.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.rhul.cs3821.dto.FriendshipDto;
import uk.ac.rhul.cs3821.dto.UserSummaryDto;
import uk.ac.rhul.cs3821.model.User;
import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;
import uk.ac.rhul.cs3821.repository.UserRepository;
import uk.ac.rhul.cs3821.service.FriendshipService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {

  @Mock
  private FriendshipService friendshipService;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private FriendshipController controller;

  private User currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

    when(userRepository.findByEmail("test@example.com"))
        .thenReturn(Optional.of(currentUser));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void sendRequestReturns201WithDto() {
    FriendshipDto dto = new FriendshipDto(10L, 2L, "friend@example.com", FriendshipStatus.PENDING);
    when(friendshipService.sendRequest(1L, 2L)).thenReturn(dto);

    var response = controller.sendRequest(2L);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(dto, response.getBody());
  }

  @Test
  void acceptRequestReturns200WithDto() throws AccessDeniedException {
    FriendshipDto dto = new FriendshipDto(10L, 2L, "friend@example.com", FriendshipStatus.ACCEPTED);
    when(friendshipService.acceptRequest(10L, 1L)).thenReturn(dto);

    var response = controller.acceptRequest(10L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(dto, response.getBody());
  }

  @Test
  void removeFriendReturns204() throws AccessDeniedException {
    var response = controller.removeFriend(10L);

    verify(friendshipService).removeFriend(10L, 1L);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void getMyFriendsReturnsListOfSummaries() {
    List<UserSummaryDto> friends = List.of(
        new UserSummaryDto(2L, "friend@example.com", 100)
    );
    when(friendshipService.getFriends(1L)).thenReturn(friends);

    var response = controller.getMyFriends();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(friends, response.getBody());
  }

  @Test
  void getPendingRequestsReturnsList() {
    List<FriendshipDto> pending = List.of(
        new FriendshipDto(10L, 3L, "pending@example.com", FriendshipStatus.PENDING)
    );
    when(friendshipService.getPendingRequests(1L)).thenReturn(pending);

    var response = controller.getPendingRequests();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pending, response.getBody());
  }

  @Test
  void throwsIfUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        jakarta.persistence.EntityNotFoundException.class,
        () -> controller.getMyFriends()
    );
  }
}