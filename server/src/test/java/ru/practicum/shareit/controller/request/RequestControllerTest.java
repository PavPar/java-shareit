package ru.practicum.shareit.controller.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ShouldReturnOk() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("test desc")
                .build();

        when(requestService.save(anyLong(), any())).thenReturn(ItemRequestDto.builder().id(1L).build());

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUserRequests_ShouldReturnOk() throws Exception {
        when(requestService.getAllFromUser(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnOk() throws Exception {
        when(requestService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ShouldReturnOk() throws Exception {
        when(requestService.getById(anyLong())).thenReturn(ItemRequestDto.builder().id(1L).build());

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk());
    }
}