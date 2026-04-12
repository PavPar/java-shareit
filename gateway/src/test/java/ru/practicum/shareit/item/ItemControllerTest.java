package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient client;

    @Autowired
    private ItemController controller;

    @Test
    void getAllRequests_ShouldReturnListOfRequests() throws Exception {
        ItemDto itemA = ItemDto.builder().id(1L).name("Test A").build();
        ItemDto itemB = ItemDto.builder().id(2L).name("Test B").build();

        List<ItemDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.getAll(Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].name").value(itemA.getName()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].name").value(itemB.getName()));

        verify(client).getAll(Mockito.any());
    }

    @Test
    void getAllRequests_ShouldReturnOnlyUserItems() throws Exception {
        ItemDto itemA = ItemDto.builder().id(1L).name("Test A").build();
        ItemDto itemB = ItemDto.builder().id(2L).name("Test B").build();

        List<ItemDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.getAll(Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].name").value(itemA.getName()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].name").value(itemB.getName()));

        verify(client).getAll(Mockito.any());
    }

    @Test
    void getOne_ShouldReturnItem() throws Exception {
        ItemDto itemA = ItemDto.builder().id(1L).name("Test A").build();

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(itemA);

        when(client.getOne(Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/{id}", itemA.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemA.getId()))
                .andExpect(jsonPath("$.name").value(itemA.getName()));

        verify(client).getOne(Mockito.any(), Mockito.any());
    }


    @Test
    void getOne_WhenNoHeader_ShouldReturn400() throws Exception {
        ItemDto itemA = ItemDto.builder().id(1L).name("Test A").build();

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(itemA);

        when(client.getOne(Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/{id}", itemA.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOne_WhenNoItem_ShouldReturn404() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.notFound().build();

        when(client.getOne(Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/{id}", 999L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }


    @Test
    void search_ShouldReturnItems() throws Exception {
        ItemDto itemA = ItemDto.builder().id(1L).name("Test A").build();
        ItemDto itemB = ItemDto.builder().id(2L).name("Test B").build();

        List<ItemDto> expectedRequests = List.of(itemA, itemB);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.search(Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/search?text={text}", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expectedRequests.size()))
                .andExpect(jsonPath("$[0].id").value(itemA.getId()))
                .andExpect(jsonPath("$[0].name").value(itemA.getName()))
                .andExpect(jsonPath("$[1].id").value(itemB.getId()))
                .andExpect(jsonPath("$[1].name").value(itemB.getName()));

        verify(client).search(Mockito.any());
    }

    @Test
    void search_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        List<ItemDto> expectedRequests = List.of();

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(expectedRequests);

        when(client.search(Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/search?text={text}", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));

        verify(client).search(Mockito.any());
    }

    @Test
    void add_ShouldWorkIfCorrectly() throws Exception {
        ItemCreateDto itemA = ItemCreateDto.builder()
                .name("Test")
                .description("Test Desc")
                .available(true)
                .build();
        ObjectMapper mapper = createMapper();
        String updatedJson = mapper.writeValueAsString(itemA);

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(itemA);


        when(client.add(Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemA.getName()))
                .andExpect(jsonPath("$.description").value(itemA.getDescription()));

        verify(client).add(Mockito.any(), Mockito.any());
    }

    @Test
    void add_WhenNOHeader_ShouldSend400() throws Exception {
        ItemCreateDto itemA = ItemCreateDto.builder()
                .name("Test")
                .description("Test Desc")
                .build();
        ObjectMapper mapper = createMapper();
        String jsonRequest = mapper.writeValueAsString(itemA);

        ResponseEntity<Object> responseEntity = ResponseEntity.badRequest().build();


        when(client.add(Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(any(), any());
    }

    @Test
    void update_WhenItemExists_ShouldWork() throws Exception {
        ItemCreateDto itemA = ItemCreateDto.builder()
                .name("Test")
                .description("Test Desc")
                .build();
        ObjectMapper mapper = createMapper();
        String body = mapper.writeValueAsString(itemA);


        Long itemId = 1L;
        Long ownerId = 1L;


        when(client.update(eq(ownerId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(itemA));

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemA.getName()))
                .andExpect(jsonPath("$.description").value(itemA.getDescription()));

        verify(client, times(1)).update(eq(ownerId), eq(itemId), any(ItemUpdateDto.class));
    }

    @Test
    void update_WhenNOHeader_ShouldSend400() throws Exception {
        ItemCreateDto itemA = ItemCreateDto.builder()
                .name("Test")
                .description("Test Desc")
                .build();
        ObjectMapper mapper = createMapper();
        String jsonRequest = mapper.writeValueAsString(itemA);


        ResponseEntity<Object> responseEntity = ResponseEntity.badRequest().build();
        Long itemId = 1L;

        when(client.update(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseEntity);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(client, never()).update(any(), any(), Mockito.any());
    }

    @Test
    void addComment_WithValidData_ShouldCreateComment() throws Exception {
        Long userId = 1L;

        CommentDto commentDto = CommentDto.builder()
                .text("Test comment")
                .build();
        ObjectMapper mapper = createMapper();
        String jsonRequest = mapper.writeValueAsString(commentDto);

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .authorName("Test User")
                .text("Test comment")
                .created(LocalDateTime.now())
                .build();

        when(client.addComment(eq(userId), eq(dto.getId()), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items/{id}/comment", dto.getId())
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.text").value(dto.getText()))
                .andExpect(jsonPath("$.authorName").value(dto.getAuthorName()));


        verify(client, times(1)).addComment(eq(userId), eq(dto.getId()), any(CommentCreateDto.class));
    }

    @Test
    void addComment_WhenNoHeader_ShouldReturn400() throws Exception {
        Long userId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Test comment")
                .build();
        ObjectMapper mapper = createMapper();
        String commentJson = mapper.writeValueAsString(commentDto);

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .authorName("Test User")
                .text("Test comment")
                .created(LocalDateTime.now())
                .build();

        when(client.addComment(eq(userId), eq(dto.getId()), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items/{id}/comment", dto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isBadRequest());
        verify(client, never()).addComment(any(), any(), Mockito.any());
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));

        mapper.registerModule(javaTimeModule);
        return mapper;
    }
}
