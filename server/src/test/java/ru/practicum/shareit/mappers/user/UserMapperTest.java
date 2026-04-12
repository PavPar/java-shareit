package ru.practicum.shareit.mappers.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserMapperTest {

    @Test
    void toDto_ShouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        UserDto dto = UserMapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void toDto_WhenUserHasNullFields_ShouldMapCorrectly() {
        User user = User.builder()
                .id(1L)
                .name(null)
                .email(null)
                .build();

        UserDto dto = UserMapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        User user = UserMapper.toEntity(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(dto.getId());
        assertThat(user.getName()).isEqualTo(dto.getName());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void toEntity_WhenDtoHasNullFields_ShouldMapCorrectly() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name(null)
                .email(null)
                .build();

        User user = UserMapper.toEntity(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(dto.getId());
        assertThat(user.getName()).isEqualTo(dto.getName());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void mapTo_ShouldUpdateOnlyName() {
        User existingUser = User.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("new test user")
                .build();

        User updatedUser = UserMapper.mapTo(updateDto, existingUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(existingUser.getEmail());

        assertThat(updatedUser.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void mapTo_ShouldUpdateOnlyEmail() {
        User existingUser = User.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("new-test@mail.com")
                .build();

        User updatedUser = UserMapper.mapTo(updateDto, existingUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());
        assertThat(updatedUser.getName()).isEqualTo(existingUser.getName());

        assertThat(updatedUser.getEmail()).isEqualTo(updateDto.getEmail());
    }

    @Test
    void mapTo_ShouldUpdateBothNameAndEmail() {
        User existingUser = User.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("new-test@mail.com")
                .name("new test user")
                .build();

        User updatedUser = UserMapper.mapTo(updateDto, existingUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo(updateDto.getEmail());
        assertThat(updatedUser.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void mapTo_WhenUpdateDtoHasNullFields_ShouldNotChangeExistingFields() {
        User existingUser = User.builder()
                .id(1L)
                .name("test user")
                .email("test@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name(null)
                .email(null)
                .build();

        User updatedUser = UserMapper.mapTo(updateDto, existingUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo(existingUser.getEmail());
        assertThat(updatedUser.getName()).isEqualTo(existingUser.getName());
    }
}
