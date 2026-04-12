package ru.practicum.shareit.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @Qualifier("userServiceImpl")
    private UserService userService;

    @Test
    void getAll_ShouldReturnOk() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void getOne_ShouldReturnOk() throws Exception {
        when(userService.getOne(anyLong())).thenReturn(UserDto.builder().id(1L).build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void add_ShouldReturnOk() throws Exception {
        UserDto dto = UserDto.builder()
                .email("test@mail.com")
                .name("test user")
                .build();

        when(userService.add(any(UserDto.class))).thenReturn(UserDto.builder().id(1L).build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnOk() throws Exception {
        UserUpdateDto dto = UserUpdateDto.builder()
                .name("new name")
                .build();

        when(userService.update(anyLong(), any(UserUpdateDto.class)))
                .thenReturn(UserDto.builder().id(1L).name(dto.getName()).build());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnOk() throws Exception {
        doNothing().when(userService).remove(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}