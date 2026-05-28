package ru.senla.scooterrental.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.InsufficientBalanceException;
import ru.senla.scooterrental.user.exceptions.UserAlreadyExistsException;
import ru.senla.scooterrental.user.exceptions.UserBlockedException;
import ru.senla.scooterrental.user.exceptions.UserNotFoundException;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.repository.UserRepository;
import ru.senla.scooterrental.user.service.impl.UserServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_shouldRegisterUserSuccessfully() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(user.getPhone())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUser(user);

        assertSame(user, result);
        assertEquals(Role.USER, result.getRole());

        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByPhone(user.getPhone());
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_shouldThrowException_whenUserIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenEmailIsBlank() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn(" ");

        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(user)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenPasswordIsBlank() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("ivan@example.com");
        when(user.getPassword()).thenReturn(" ");

        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(user)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenFirstNameIsBlank() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("ivan@example.com");
        when(user.getPassword()).thenReturn("Password123");
        when(user.getFirstName()).thenReturn(" ");

        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(user)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenLastNameIsBlank() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("ivan@example.com");
        when(user.getPassword()).thenReturn("Password123");
        when(user.getFirstName()).thenReturn("Ivan");
        when(user.getLastName()).thenReturn(" ");

        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(user)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenPhoneIsBlank() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("ivan@example.com");
        when(user.getPassword()).thenReturn("Password123");
        when(user.getFirstName()).thenReturn("Ivan");
        when(user.getLastName()).thenReturn("Ivanov");
        when(user.getPhone()).thenReturn(" ");

        assertThrows(
                UserValidationException.class,
                () -> userService.registerUser(user)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(user)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldThrowException_whenPhoneAlreadyExists() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(user.getPhone())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(user)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAdmin_shouldRegisterAdminSuccessfully() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(user.getPhone())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerAdmin(user);

        assertSame(user, result);
        assertEquals(Role.ADMIN, result.getRole());

        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByPhone(user.getPhone());
        verify(userRepository).save(user);
    }

    @Test
    void registerAdmin_shouldThrowException_whenUserIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.registerAdmin(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerAdmin_shouldThrowException_whenEmailAlreadyExists() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerAdmin(user)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAdmin_shouldThrowException_whenPhoneAlreadyExists() {
        User user = defaultUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(user.getPhone())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerAdmin(user)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertSame(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(1L)
        );

        verify(userRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenIdIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getById(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getById_shouldThrowException_whenIdIsInvalid() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getById(0L)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getByEmail_shouldReturnUser_whenUserExists() {
        User user = defaultUser();

        when(userRepository.findByEmail("ivan@example.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getByEmail("ivan@example.com");

        assertSame(user, result);
        verify(userRepository).findByEmail("ivan@example.com");
    }

    @Test
    void getByEmail_shouldThrowException_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getByEmail("unknown@example.com")
        );

        verify(userRepository).findByEmail("unknown@example.com");
    }

    @Test
    void getByEmail_shouldThrowException_whenEmailIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getByEmail(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getByEmail_shouldThrowException_whenEmailIsBlank() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getByEmail(" ")
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getByPhone_shouldReturnUser_whenUserExists() {
        User user = defaultUser();

        when(userRepository.findByPhone("+79991234567"))
                .thenReturn(Optional.of(user));

        User result = userService.getByPhone("+79991234567");

        assertSame(user, result);
        verify(userRepository).findByPhone("+79991234567");
    }

    @Test
    void getByPhone_shouldThrowException_whenPhoneNotFound() {
        when(userRepository.findByPhone("+79990000000"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getByPhone("+79990000000")
        );

        verify(userRepository).findByPhone("+79990000000");
    }

    @Test
    void getByPhone_shouldThrowException_whenPhoneIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getByPhone(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getByPhone_shouldThrowException_whenPhoneIsBlank() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getByPhone(" ")
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = defaultUser();
        User user2 = new User(
                "petr@example.com",
                "Password123",
                "Petr",
                "Petrov",
                "+79991234568"
        );

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenUsersDoNotExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUsersByRole_shouldReturnUsersByRole() {
        User user = defaultUser();

        when(userRepository.findAllByRole(Role.USER)).thenReturn(List.of(user));

        List<User> result = userService.getUsersByRole(Role.USER);

        assertEquals(1, result.size());
        verify(userRepository).findAllByRole(Role.USER);
    }

    @Test
    void getUsersByRole_shouldReturnEmptyList_whenUsersWithRoleDoNotExist() {
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of());

        List<User> result = userService.getUsersByRole(Role.ADMIN);

        assertTrue(result.isEmpty());
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    void getUsersByRole_shouldThrowException_whenRoleIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getUsersByRole(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void getUsersByStatus_shouldReturnUsersByStatus() {
        User user = defaultUser();

        when(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(List.of(user));

        List<User> result = userService.getUsersByStatus(UserStatus.ACTIVE);

        assertEquals(1, result.size());
        verify(userRepository).findAllByStatus(UserStatus.ACTIVE);
    }

    @Test
    void getUsersByStatus_shouldReturnEmptyList_whenUsersWithStatusDoNotExist() {
        when(userRepository.findAllByStatus(UserStatus.BLOCKED))
                .thenReturn(List.of());

        List<User> result = userService.getUsersByStatus(UserStatus.BLOCKED);

        assertTrue(result.isEmpty());
        verify(userRepository).findAllByStatus(UserStatus.BLOCKED);
    }

    @Test
    void getUsersByStatus_shouldThrowException_whenStatusIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.getUsersByStatus(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldUpdateProfileSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone("+79990000000")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                1L,
                "Alex",
                "Sidorov",
                "+7 (999) 000-00-00"
        );

        assertEquals("Alex", result.getFirstName());
        assertEquals("Sidorov", result.getLastName());
        assertEquals("+79990000000", result.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldUpdateOnlyFirstNameSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                1L,
                "Alex",
                null,
                null
        );

        assertEquals("Alex", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("+79991234567", result.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldUpdateOnlyLastNameSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                1L,
                null,
                "Sidorov",
                null
        );

        assertEquals("Ivan", result.getFirstName());
        assertEquals("Sidorov", result.getLastName());
        assertEquals("+79991234567", result.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldUpdateOnlyPhoneSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone("+79990000000")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                1L,
                null,
                null,
                "+7 (999) 000-00-00"
        );

        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("+79990000000", result.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldNotCheckPhoneUniqueness_whenPhoneIsSameAfterNormalization() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                1L,
                null,
                null,
                "+7 (999) 123-45-67"
        );

        assertEquals("+79991234567", result.getPhone());

        verify(userRepository, never()).existsByPhone(any());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldThrowException_whenIdIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.updateProfile(null, "Alex", null, null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldThrowException_whenNoDataProvided() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.updateProfile(1L, null, null, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.updateProfile(1L, "Alex", null, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldThrowException_whenPhoneAlreadyExists() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone("+79990000000")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateProfile(
                        1L,
                        null,
                        null,
                        "+7 (999) 000-00-00"
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldChangeEmailSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(1L, "NEW@example.com");

        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_shouldNotCheckEmailUniqueness_whenEmailIsSameIgnoringCase() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(1L, "IVAN@example.com");

        assertEquals("ivan@example.com", result.getEmail());

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_shouldThrowException_whenEmailAlreadyExists() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.changeEmail(1L, "new@example.com")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldThrowException_whenEmailIsNull() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.changeEmail(1L, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldThrowException_whenEmailIsBlank() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.changeEmail(1L, " ")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.changeEmail(1L, "new@example.com")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changePassword(1L, "NewPassword123");

        assertEquals("NewPassword123", result.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldThrowException_whenPasswordIsNull() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.changePassword(1L, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowException_whenPasswordIsBlank() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.changePassword(1L, " ")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.changePassword(1L, "NewPassword123")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void blockUser_shouldBlockUserSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.blockUser(1L);

        assertTrue(result.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void blockUser_shouldThrowException_whenUserAlreadyBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.blockUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void activateUser_shouldActivateUserSuccessfully() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.activateUser(1L);

        assertFalse(result.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_shouldThrowException_whenUserAlreadyActive() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.activateUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyUser_shouldVerifyUserSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.verifyUser(1L);

        assertTrue(result.isVerified());
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_shouldThrowException_whenUserAlreadyVerified() {
        User user = defaultUser();
        user.verify();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.verifyUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyUser_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.verifyUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void addBalance_shouldAddBalanceSuccessfully() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.addBalance(1L, BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), result.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void addBalance_shouldThrowException_whenAmountIsNull() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.addBalance(1L, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void addBalance_shouldThrowException_whenAmountIsNegative() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.addBalance(1L, BigDecimal.valueOf(-100))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void addBalance_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.addBalance(1L, BigDecimal.valueOf(500))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void subtractBalance_shouldSubtractBalanceSuccessfully() {
        User user = defaultUser();
        user.addBalance(BigDecimal.valueOf(500));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.subtractBalance(1L, BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(300), result.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void subtractBalance_shouldThrowException_whenAmountIsNull() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.subtractBalance(1L, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void subtractBalance_shouldThrowException_whenAmountIsNegative() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserValidationException.class,
                () -> userService.subtractBalance(1L, BigDecimal.valueOf(-100))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void subtractBalance_shouldThrowException_whenBalanceIsInsufficient() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                InsufficientBalanceException.class,
                () -> userService.subtractBalance(1L, BigDecimal.valueOf(200))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void subtractBalance_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.subtractBalance(1L, BigDecimal.valueOf(200))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void buyThreeDaySubscription_shouldBuySubscriptionSuccessfully() {
        User user = defaultUser();
        user.addBalance(BigDecimal.valueOf(2000));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.buyThreeDaySubscription(1L);

        assertTrue(result.hasActiveSubscription());
        assertEquals(BigDecimal.valueOf(501), result.getBalance());
        assertNotNull(result.getSubscriptionPurchasedAt());
        assertNotNull(result.getSubscriptionExpiresAt());

        verify(userRepository).save(user);
    }

    @Test
    void buyThreeDaySubscription_shouldThrowException_whenBalanceIsInsufficient() {
        User user = defaultUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                InsufficientBalanceException.class,
                () -> userService.buyThreeDaySubscription(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void buyThreeDaySubscription_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        user.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                UserBlockedException.class,
                () -> userService.buyThreeDaySubscription(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_shouldThrowException_whenIdIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.deleteUser(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUser_shouldThrowException_whenIdIsInvalid() {
        assertThrows(
                UserValidationException.class,
                () -> userService.deleteUser(0L)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("ivan@example.com");

        assertTrue(result);
        verify(userRepository).existsByEmail("ivan@example.com");
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("unknown@example.com");

        assertFalse(result);
        verify(userRepository).existsByEmail("unknown@example.com");
    }

    @Test
    void existsByEmail_shouldThrowException_whenEmailIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.existsByEmail(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void existsByEmail_shouldThrowException_whenEmailIsBlank() {
        assertThrows(
                UserValidationException.class,
                () -> userService.existsByEmail(" ")
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void existsByPhone_shouldReturnTrue_whenPhoneExists() {
        when(userRepository.existsByPhone("+79991234567")).thenReturn(true);

        boolean result = userService.existsByPhone("+79991234567");

        assertTrue(result);
        verify(userRepository).existsByPhone("+79991234567");
    }

    @Test
    void existsByPhone_shouldReturnFalse_whenPhoneDoesNotExist() {
        when(userRepository.existsByPhone("+79990000000")).thenReturn(false);

        boolean result = userService.existsByPhone("+79990000000");

        assertFalse(result);
        verify(userRepository).existsByPhone("+79990000000");
    }

    @Test
    void existsByPhone_shouldThrowException_whenPhoneIsNull() {
        assertThrows(
                UserValidationException.class,
                () -> userService.existsByPhone(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void existsByPhone_shouldThrowException_whenPhoneIsBlank() {
        assertThrows(
                UserValidationException.class,
                () -> userService.existsByPhone(" ")
        );

        verifyNoInteractions(userRepository);
    }

    private User defaultUser() {
        return new User(
                "ivan@example.com",
                "Password123",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );
    }
}