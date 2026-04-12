package ru.practicum.shareit.user;


import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.hikari.connection-timeout=30000",
        "spring.datasource.hikari.maximum-pool-size=5"
})
class UserServiceImplTest {

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @Autowired
    private EntityManager em;

    @Test
    void shouldAddUserToDatabase() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        UserDto addedUser = userService.add(userDto);

        User user = getUserFromDB(userDto.getEmail(), userDto.getName());

        assertThat(addedUser.getId()).isNotNull();
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(user.getName()).isEqualTo(userDto.getName());
    }

    @Test
    void shouldUpdateUser_Name() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        UserDto addedUser = userService.add(userDto);

        assertThat(addedUser.getId()).isNotNull();

        User user = getUserFromDB(userDto.getEmail(), userDto.getName());

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(user.getName()).isEqualTo(userDto.getName());

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .name("New Test")
                .build();

        UserDto updatedUser = userService.update(user.getId(), userUpdateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(addedUser.getEmail());
        assertThat(updatedUser.getName()).isEqualTo(userUpdateDto.getName());

        User actualUserFromDB = getUserFromDB(userDto.getEmail(), userUpdateDto.getName());

        assertThat(actualUserFromDB).isNotNull();
        assertThat(actualUserFromDB.getEmail()).isEqualTo(addedUser.getEmail());
        assertThat(actualUserFromDB.getName()).isEqualTo(userUpdateDto.getName());

    }

    @Test
    void shouldUpdateUser_Email() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        UserDto addedUser = userService.add(userDto);

        assertThat(addedUser.getId()).isNotNull();

        User user = getUserFromDB(userDto.getEmail(), userDto.getName());

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(user.getName()).isEqualTo(userDto.getName());

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .email("new_email@test.com")
                .build();

        UserDto updatedUser = userService.update(user.getId(), userUpdateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(userUpdateDto.getEmail());
        assertThat(updatedUser.getName()).isEqualTo(addedUser.getName());

        User actualUserFromDB = getUserFromDB(userUpdateDto.getEmail(), userDto.getName());

        assertThat(actualUserFromDB).isNotNull();
        assertThat(actualUserFromDB.getEmail()).isEqualTo(userUpdateDto.getEmail());
        assertThat(actualUserFromDB.getName()).isEqualTo(addedUser.getName());

    }

    @Test
    void shouldUpdateUser_EmailName() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        UserDto addedUser = userService.add(userDto);

        assertThat(addedUser.getId()).isNotNull();

        User user = getUserFromDB(userDto.getEmail(), userDto.getName());

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(user.getName()).isEqualTo(userDto.getName());

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .name("New Test")
                .email("new_email@test.com")
                .build();

        UserDto updatedUser = userService.update(user.getId(), userUpdateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(updatedUser.getEmail());
        assertThat(updatedUser.getName()).isEqualTo(userUpdateDto.getName());

        User actualUserFromDB = getUserFromDB(userUpdateDto.getEmail(), userUpdateDto.getName());

        assertThat(actualUserFromDB).isNotNull();
        assertThat(actualUserFromDB.getEmail()).isEqualTo(userUpdateDto.getEmail());
        assertThat(actualUserFromDB.getName()).isEqualTo(userUpdateDto.getName());

    }

    @Test
    void shouldDeleteUser() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        UserDto addedUser = userService.add(userDto);

        assertThat(addedUser.getId()).isNotNull();

        User user = getUserFromDB(userDto.getEmail(), userDto.getName());

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(user.getName()).isEqualTo(userDto.getName());


        userService.remove(user.getId());

        User actualUserFromDB = getUserFromDB(userDto.getEmail(), userDto.getName());
        assertThat(actualUserFromDB).isNull();

    }

    private User getUserFromDB(String email, String name) {
        try {
            TypedQuery<User> query = em
                    .createQuery("Select u from User u " +
                                    "where u.email = :email and u.name = :name",
                            User.class);
            return query
                    .setParameter("email", email)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
