package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestClient client;

    @Autowired
    private RequestController controller;

    @Test
    void getAllRequests_ShouldReturnListOfRequests() throws Exception {
        ItemRequestDto itemA = ItemRequestDto.builder().id(1L).description("Test A").build();
        ItemRequestDto itemB = ItemRequestDto.builder().id(2L).description("Test B").build();

        List<ItemRequestDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.getAll()).thenReturn(responseEntity);

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].description").value(itemA.getDescription()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].description").value(itemB.getDescription()));

        verify(client).getAll();
    }

    @Test
    void getAllFromUser_ShouldReturnItemsIfOk() throws Exception {
        Long USER_ID = 999L;
        ItemRequestDto itemA = ItemRequestDto.builder().id(1L).description("Test A").build();
        ItemRequestDto itemB = ItemRequestDto.builder().id(2L).description("Test B").build();

        List<ItemRequestDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.getAllFromUser(Mockito.anyLong())).thenReturn(responseEntity);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].description").value(itemA.getDescription()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].description").value(itemB.getDescription()));

        verify(client).getAllFromUser(USER_ID);
    }

    @Test
    void getAllFromUser_ShouldFailIfNoHeader() throws Exception {
        mockMvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenNoUserIdHeader_ShouldReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                    "description": "Test A"
                }
                """;

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).save(any(), any());
    }

    @Test
    void create_WhenEmptyDescription_ShouldReturnBadRequest() throws Exception {
        String jsonRequest = """
                {
                    "description": ""
                }
                """;

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).save(any(), any());
    }


    @Test
    void getById_WhenRequestDoesNotExist_ShouldReturnNotFound() throws Exception {
        Long requestId = 999L;
        when(client.getById(requestId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_WhenRequestExists_ShouldReturnRequest() throws Exception {

        ItemRequestDto expectedRequest = ItemRequestDto.builder()
                .id(1L)
                .description("Test A")
                .build();

        when(client.getById(expectedRequest.getId()))
                .thenReturn(ResponseEntity.ok(expectedRequest));

        mockMvc.perform(get("/requests/{id}", expectedRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedRequest.getId()))
                .andExpect(jsonPath("$.description").value(expectedRequest.getDescription()));
    }

}
