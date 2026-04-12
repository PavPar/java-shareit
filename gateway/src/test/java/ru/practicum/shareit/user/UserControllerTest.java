package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient client;

    @Autowired
    private UserController controller;

    @Test
    void getAllRequests_ShouldReturnListOfRequests() throws Exception {
        UserDto itemA = UserDto.builder().id(1L).email("Test A").build();
        UserDto itemB = UserDto.builder().id(2L).email("Test B").build();

        List<UserDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.getAll()).thenReturn(responseEntity);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].email").value(itemA.getEmail()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].email").value(itemB.getEmail()));

        verify(client).getAll();
    }


    @Test
    void getOne_ShouldReturnRequest() throws Exception {
        UserDto itemA = UserDto.builder().id(1L).email("Test@mail.com").name("Test A").build();

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(itemA);

        when(client.getOne(Mockito.anyLong())).thenReturn(responseEntity);

        mockMvc.perform(get("/users/{id}", itemA.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(itemA.getId()))
                .andExpect(jsonPath("$.name").value(itemA.getName()))
                .andExpect(jsonPath("$.email").value(itemA.getEmail()));
        verify(client).getOne(itemA.getId());
    }

    @Test
    void getOne_IfNoUser_ShouldReturn404() throws Exception {
        Long requestId = 999L;
        when(client.getOne(requestId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/users/{id}", requestId))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_WhenEmptyName_ShouldReturnBadRequest() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("")
                .email("test@mail.com")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(updatedUser);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(any());
    }

    @Test
    void add_WhenEmptyEmail_ShouldReturnBadRequest() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("test a")
                .email("")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(updatedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(any());
    }

    @Test
    void add_WhenIncorrectEmail_ShouldReturnBadRequest() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("test a")
                .email("test")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(updatedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(any());
    }

    @Test
    void add_WhenOk_ShouldReturnOk() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("test a")
                .email("test@mail.com")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(updatedUser);

        UserDto itemA = UserDto.builder().id(1L).email("test@email.com").name("test a").build();

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(itemA);

        when(client.add(Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemA.getId()))
                .andExpect(jsonPath("$.name").value(itemA.getName()))
                .andExpect(jsonPath("$.email").value(itemA.getEmail()));

        verify(client).add(Mockito.any());
    }

    @Test
    void delete_WhenUserExists_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        when(client.remove(userId)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(client, times(1)).remove(userId);
    }

    @Test
    void delete_WhenUserNotExists_ShouldReturn404() throws Exception {
        Long userId = 1L;
        when(client.remove(userId)).thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_WhenUserExists_ShouldReturnUpdatedUser() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Test A")
                .email("test@mail.com")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(updatedUser);

        when(client.update(eq(updatedUser.getId()), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(updatedUser));

        mockMvc.perform(patch("/users/{id}", updatedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()));

        verify(client, times(1)).update(eq(updatedUser.getId()), any(UserUpdateDto.class));
    }


    @Test
    void update_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
        Long nonExistentId = 999L;
        UserUpdateDto updatedUser = UserUpdateDto.builder()
                .name("Test A")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String updateJson = mapper.writeValueAsString(updatedUser);

        when(client.update(eq(nonExistentId), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(patch("/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());

        verify(client).update(eq(nonExistentId), any(UserUpdateDto.class));
    }
}
