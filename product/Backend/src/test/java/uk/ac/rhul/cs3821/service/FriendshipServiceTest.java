//package uk.ac.rhul.cs3821.service;
//
//import jakarta.persistence.EntityNotFoundException;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.access.AccessDeniedException;
//import uk.ac.rhul.cs3821.dto.FriendshipDto;
//import uk.ac.rhul.cs3821.dto.UserSummaryDto;
//import uk.ac.rhul.cs3821.model.Friendship;
//import uk.ac.rhul.cs3821.model.User;
//import uk.ac.rhul.cs3821.model.enums.FriendshipStatus;
//import uk.ac.rhul.cs3821.model.enums.NotificationType;
//import uk.ac.rhul.cs3821.repository.FriendshipRepository;
//import uk.ac.rhul.cs3821.repository.UserRepository;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class FriendshipServiceTest {
//
//  @Mock
//  private FriendshipRepository friendshipRepo;
//  @Mock
//  private UserRepository userRepo;
//  @Mock
//  private NotificationService notificationService;
//
//  @InjectMocks
//  private FriendshipService friendshipService;
//
//  private User requester;
//  private User addressee;
//  private Friendship pendingFriendship;
//  private Friendship acceptedFriendship;
//
//  @BeforeEach
//  void setUp() {
//    requester = new User();
//    requester.setId(1L);
//    requester.setEmail("requester@example.com");
//    requester.setTotalPoints(100);
//
//    addressee = new User();
//    addressee.setId(2L);
//    addressee.setEmail("addressee@example.com");
//    addressee.setTotalPoints(200);
//
//    pendingFriendship = new Friendship();
//    pendingFriendship.setId(10L);
//    pendingFriendship.setRequester(requester);
//    pendingFriendship.setAddressee(addressee);
//    pendingFriendship.setStatus(FriendshipStatus.PENDING);
//
//    acceptedFriendship = new Friendship();
//    acceptedFriendship.setId(11L);
//    acceptedFriendship.setRequester(requester);
//    acceptedFriendship.setAddressee(addressee);
//    acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);
//  }
//
//  @Test
//  void sendRequest_createsPendingFriendship() {
//    when(friendshipRepo.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
//    when(userRepo.getReferenceById(1L)).thenReturn(requester);
//    when(userRepo.findById(2L)).thenReturn(Optional.of(addressee));
//    when(friendshipRepo.save(any())).thenReturn(pendingFriendship);
//
//    FriendshipDto result = friendshipService.sendRequest(1L, 2L);
//
//    assertEquals(FriendshipStatus.PENDING, result.status);
//    assertEquals(2L, result.userId);
//    verify(notificationService).send(2L, NotificationType.FRIEND_REQUEST, 10L);
//  }
//
//  @Test
//  void sendRequest_throwsWhenSendingToSelf() {
//    assertThrows(IllegalArgumentException.class,
//        () -> friendshipService.sendRequest(1L, 1L));
//
//    verify(friendshipRepo, never()).save(any());
//  }
//
//  @Test
//  void sendRequest_throwsWhenFriendshipAlreadyExists() {
//    when(friendshipRepo.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(pendingFriendship));
//
//    assertThrows(IllegalStateException.class,
//        () -> friendshipService.sendRequest(1L, 2L));
//
//    verify(friendshipRepo, never()).save(any());
//  }
//
//  @Test
//  void sendRequest_throwsWhenAddresseeNotFound() {
//    when(friendshipRepo.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
//    when(userRepo.getReferenceById(1L)).thenReturn(requester);
//    when(userRepo.findById(2L)).thenReturn(Optional.empty());
//
//    assertThrows(EntityNotFoundException.class,
//        () -> friendshipService.sendRequest(1L, 2L));
//  }
//
//  @Test
//  void acceptRequest_updatesStatusToAccepted() {
//    when(friendshipRepo.findById(10L)).thenReturn(Optional.of(pendingFriendship));
//    when(friendshipRepo.save(any())).thenReturn(pendingFriendship);
//
//    FriendshipDto result = friendshipService.acceptRequest(10L, 2L);
//
//    assertEquals(FriendshipStatus.ACCEPTED, result.status);
//  }
//
//  @Test
//  void acceptRequest_throwsWhenNotAddressee() {
//    when(friendshipRepo.findById(10L)).thenReturn(Optional.of(pendingFriendship));
//
//    // requester (id=1) tries to accept their own request
//    assertThrows(AccessDeniedException.class,
//        () -> friendshipService.acceptRequest(10L, 1L));
//  }
//
//  @Test
//  void acceptRequest_throwsWhenNotFound() {
//    when(friendshipRepo.findById(99L)).thenReturn(Optional.empty());
//
//    assertThrows(EntityNotFoundException.class,
//        () -> friendshipService.acceptRequest(99L, 2L));
//  }
//
//  @Test
//  void removeFriend_deletesSuccessfully() {
//    when(friendshipRepo.findById(10L)).thenReturn(Optional.of(pendingFriendship));
//
//    friendshipService.removeFriend(10L, 1L);
//
//    verify(friendshipRepo).delete(pendingFriendship);
//  }
//
//  @Test
//  void removeFriend_throwsWhenNotPartOfFriendship() {
//    User outsider = new User();
//    outsider.setId(99L);
//
//    when(friendshipRepo.findById(10L)).thenReturn(Optional.of(pendingFriendship));
//
//    assertThrows(AccessDeniedException.class,
//        () -> friendshipService.removeFriend(10L, 99L));
//
//    verify(friendshipRepo, never()).delete(any());
//  }
//
//  @Test
//  void removeFriend_throwsWhenNotFound() {
//    when(friendshipRepo.findById(99L)).thenReturn(Optional.empty());
//
//    assertThrows(EntityNotFoundException.class,
//        () -> friendshipService.removeFriend(99L, 1L));
//  }
//
//  @Test
//  void getFriends_returnsOtherUserInFriendship() {
//    when(friendshipRepo.findAllAcceptedFriendships(1L)).thenReturn(List.of(acceptedFriendship));
//
//    List<UserSummaryDto> result = friendshipService.getFriends(1L);
//
//    assertEquals(1, result.size());
//    assertEquals("addressee@example.com", result.getFirst().email);
//    assertEquals(200, result.getFirst().totalPoints);
//  }
//
//  @Test
//  void getFriends_returnsEmptyListWhenNoFriends() {
//    when(friendshipRepo.findAllAcceptedFriendships(1L)).thenReturn(List.of());
//
//    List<UserSummaryDto> result = friendshipService.getFriends(1L);
//
//    assertEquals(0, result.size());
//  }
//
//  @Test
//  void getPendingRequests_returnsPendingList() {
//    when(friendshipRepo.findByAddresseeIdAndStatus(2L, FriendshipStatus.PENDING))
//        .thenReturn(List.of(pendingFriendship));
//
//    List<FriendshipDto> result = friendshipService.getPendingRequests(2L);
//
//    assertEquals(1, result.size());
//    assertEquals(FriendshipStatus.PENDING, result.getFirst().status);
//  }
//}