package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.notification.RegisterTokenRequest;
import kg.megalab.pivnitsabackend.entity.DeviceType;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.entity.UserDeviceToken;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.DeviceTokenRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceTokenService deviceTokenService;

    private static final String PHONE = "+996700000000";

    @Test
    void shouldDeleteOwnToken() {
        User user = User.builder().id(42L).phone(PHONE).phoneVerified(true).build();

        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(user));
        when(deviceTokenRepository.deleteByTokenAndUserId("token123", 42L)).thenReturn(1);

        deviceTokenService.removeToken(PHONE, "token123");

        verify(deviceTokenRepository).deleteByTokenAndUserId("token123", 42L);
    }

    @Test
    void shouldNotThrowWhenTokenBelongsToAnotherUser() {
        User user = User.builder().id(42L).phone(PHONE).phoneVerified(true).build();

        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(user));
        when(deviceTokenRepository.deleteByTokenAndUserId("someoneElseToken", 42L)).thenReturn(0);

        assertDoesNotThrow(() -> deviceTokenService.removeToken(PHONE, "someoneElseToken"));

        verify(deviceTokenRepository).deleteByTokenAndUserId("someoneElseToken", 42L);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnRemove() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> deviceTokenService.removeToken(PHONE, "token123"));

        verify(deviceTokenRepository, never()).deleteByTokenAndUserId(any(), any());
    }

    @Test
    void shouldThrowWhenPhoneNotVerifiedOnRemove() {
        User user = User.builder().id(42L).phone(PHONE).phoneVerified(false).build();

        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class,
                () -> deviceTokenService.removeToken(PHONE, "token123"));

        verify(deviceTokenRepository, never()).deleteByTokenAndUserId(any(), any());
    }

    @Test
    void shouldReturnEmptyListWhenUserIdsIsEmpty() {
        List<String> tokens = deviceTokenService.getTokensByUserIds(List.of());

        assertTrue(tokens.isEmpty());
        verify(deviceTokenRepository, never()).findAllTokensByUserIds(any());
    }

    @Test
    void shouldReturnEmptyListWhenUserIdsIsNull() {
        List<String> tokens = deviceTokenService.getTokensByUserIds(null);

        assertTrue(tokens.isEmpty());
        verify(deviceTokenRepository, never()).findAllTokensByUserIds(any());
    }

    @Test
    void shouldReturnTokensForGivenUserIds() {
        when(deviceTokenRepository.findAllTokensByUserIds(List.of(1L, 2L)))
                .thenReturn(List.of("token1", "token2"));

        List<String> tokens = deviceTokenService.getTokensByUserIds(List.of(1L, 2L));

        assertEquals(2, tokens.size());
        assertTrue(tokens.containsAll(List.of("token1", "token2")));
    }

    @Test
    void shouldCreateNewTokenWhenNotExists() {
        RegisterTokenRequest request = new RegisterTokenRequest("newToken", DeviceType.ANDROID);

        when(deviceTokenRepository.findByToken("newToken")).thenReturn(Optional.empty());

        deviceTokenService.saveOrUpdateToken(42L, request);

        ArgumentCaptor<UserDeviceToken> captor = ArgumentCaptor.forClass(UserDeviceToken.class);
        verify(deviceTokenRepository).save(captor.capture());

        UserDeviceToken saved = captor.getValue();
        assertEquals(42L, saved.getUserId());
        assertEquals("newToken", saved.getToken());
        assertEquals(DeviceType.ANDROID, saved.getDeviceType());
    }

    @Test
    void shouldReassignExistingTokenToNewUser() {
        RegisterTokenRequest request = new RegisterTokenRequest("existingToken", DeviceType.IOS);

        UserDeviceToken existing = UserDeviceToken.builder()
                .userId(1L)
                .token("existingToken")
                .deviceType(DeviceType.ANDROID)
                .build();

        when(deviceTokenRepository.findByToken("existingToken")).thenReturn(Optional.of(existing));

        deviceTokenService.saveOrUpdateToken(99L, request);

        assertEquals(99L, existing.getUserId());
        assertEquals(DeviceType.IOS, existing.getDeviceType());
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void shouldRemoveInvalidTokensWhenListNotEmpty() {
        List<String> invalidTokens = List.of("dead1", "dead2");

        deviceTokenService.removeInvalidTokens(invalidTokens);

        verify(deviceTokenRepository).deleteByTokenIn(invalidTokens);
    }

    @Test
    void shouldNotCallRepositoryWhenInvalidTokensListIsEmpty() {
        deviceTokenService.removeInvalidTokens(List.of());

        verify(deviceTokenRepository, never()).deleteByTokenIn(any());
    }
}