package com.angelov00.server;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.model.entity.Session;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.SessionRepository;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final int SESSION_TIME_TO_LIVE = 3600;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private AuthService authService;

    User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setUsername("username");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Smith");
        testUser.setEmail("test@email.com");
        testUser.setRole(Role.USER);
    }

    @Test
    public void testRegister_UserAlreadyExists_ThrowsException() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("username", "password", "John", "Smith", "johnsmith@gmail.com");

        when(userRepository.exists(userRegisterDTO.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(userRegisterDTO);
        });

        verify(userRepository, never()).save(any(User.class));
        verify(sessionRepository, never()).createSession(any(User.class), anyInt());
    }

    @Test
    public void testRegister_validRegistration_ShouldReturnSessionId() {

        Session testSession = new Session(testUser, SESSION_TIME_TO_LIVE);
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("username", "password", "John", "Smith", "johnsmith@gmail.com");

        when(userRepository.exists(userRegisterDTO.getUsername())).thenReturn(false);
        doNothing().when(userRepository).save(any(User.class));
        when(sessionRepository.createSession(any(User.class), eq(SESSION_TIME_TO_LIVE))).thenReturn(testSession);

        String sessionId = authService.register(userRegisterDTO);

        assertEquals(testSession.getSessionId(), sessionId);

        verify(userRepository, times(1)).save(any(User.class));  // Провери дали save() е извикан
        verify(sessionRepository, times(1)).createSession(any(User.class), eq(SESSION_TIME_TO_LIVE));  // Провери дали createSession() е извикан с правилните параметри
    }

    @Test
    public void testLoginWithSessionId_InvalidId_ShouldThrowException() {
        String testSessionID = "invalid-session-id";
        when(sessionRepository.isValid(testSessionID)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(testSessionID);
        });
    }

    @Test
    public void testLogout_validSessionId_ShouldInvalidateSession() {
        String testSessionID = "invalid-session-id";
        when(sessionRepository.isValid(testSessionID)).thenReturn(true);

        authService.logout(testSessionID);
        verify(sessionRepository, times(1)).invalidate(testSessionID);
    }

    @Test
    public void testLogout_InvalidSessionId_ShouldThrowException() {
        String testSessionID = "invalid-session-id";
        when(sessionRepository.isValid(testSessionID)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {authService.logout(testSessionID);});

        verify(sessionRepository, never()).invalidate(testSessionID);
    }

    @Test
    public void promoteToAdmin_NotAdmin_ShouldThrowException() {

        String testSessionId = "non-an-admin-session-id";
        String testClientIP = "12.13.14.15";
        String testUsername = "to_be_promoted";

        when(sessionRepository.getUserBySessionId(testSessionId)).thenReturn(Optional.ofNullable(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                authService.promoteToAdmin(testSessionId, testUsername, testClientIP));

        verify(userRepository, never()).promoteToAdmin(anyString());
    }
}
