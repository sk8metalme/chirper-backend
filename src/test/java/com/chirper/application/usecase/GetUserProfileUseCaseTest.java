package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProfileUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private GetUserProfileUseCase getUserProfileUseCase;

    private Username testUsername;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUsername = new Username("testuser");
        testUser = User.create(
            testUsername,
            new Email("test@example.com"),
            "hashedPassword"
        );
    }

    @Test
    @DisplayName("ユーザープロフィール取得成功")
    void getUserProfile_success() {
        // Arrange
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        // Act
        User result = getUserProfileUseCase.execute(testUsername);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUsername);
        assertThat(result.getEmail().value()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).findByUsername(testUsername);
    }

    @Test
    @DisplayName("ユーザープロフィール取得失敗 - ユーザーが存在しない")
    void getUserProfile_failure_userNotFound() {
        // Arrange
        Username nonExistentUsername = new Username("nonexistent");
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getUserProfileUseCase.execute(nonExistentUsername))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");

        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
    }

    @Test
    @DisplayName("ユーザープロフィール取得失敗 - usernameがnull")
    void getUserProfile_failure_nullUsername() {
        // Act & Assert
        assertThatThrownBy(() -> getUserProfileUseCase.execute(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Username cannot be null");

        verify(userRepository, never()).findByUsername(any());
    }
}
