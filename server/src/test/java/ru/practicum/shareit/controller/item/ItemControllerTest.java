package ru.practicum.shareit.controller.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @Qualifier("itemServiceImpl")
    private ItemService itemService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private ItemDto testItemDto;
    private CommentDto testCommentDto;

    @BeforeEach
    void setUp() {
        testItemDto = ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("test desc")
                .available(true)
                .build();

        testCommentDto = CommentDto.builder()
                .id(1L)
                .text("test comment")
                .authorName("test user name")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getAll_ShouldReturnItems() throws Exception {
        Long userId = 1L;
        List<ItemDto> items = List.of(testItemDto);
        when(itemService.getAll(userId)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(items.size()))
                .andExpect(jsonPath("$[0].id").value(testItemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(testItemDto.getName()));
    }

    @Test
    void getAll_WithoutUserId_ShouldReturnAllItems() throws Exception {
        when(itemService.getAll(null)).thenReturn(List.of(testItemDto));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOne_ShouldReturnItem() throws Exception {
        Long userId = 1L;

        when(itemService.getOne(userId, testItemDto.getId())).thenReturn(testItemDto);

        mockMvc.perform(get("/items/{id}", testItemDto.getId())
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItemDto.getId()))
                .andExpect(jsonPath("$.name").value(testItemDto.getName()));
    }

    @Test
    void getOne_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/9999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_ShouldReturnItems() throws Exception {
        String searchText = "test";
        List<ItemDto> items = List.of(testItemDto);
        when(itemService.search(searchText)).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_WithoutTextParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_ShouldCreateItem() throws Exception {
        Long userId = 1L;
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("new item")
                .description("new item desc")
                .available(true)
                .build();

        when(itemService.add(eq(userId), any(ItemCreateDto.class))).thenReturn(testItemDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItemDto.getId()))
                .andExpect(jsonPath("$.name").value(testItemDto.getName()))
                .andExpect(jsonPath("$.description").value(testItemDto.getDescription()));
    }

    @Test
    void add_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("evil item")
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldUpdateItem() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("new item name")
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(itemId)
                .name(updateDto.getName())
                .description("some desc")
                .available(true)
                .build();

        when(itemService.update(eq(userId), eq(itemId), any(ItemUpdateDto.class))).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateDto.getName()))
                .andExpect(jsonPath("$.description").value(updatedItem.getDescription()));
    }

    @Test
    void update_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("new updated item")
                .build();

        mockMvc.perform(patch("/items/{id}", testItemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ShouldCreateComment() throws Exception {
        Long userId = 1L;

        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text(testCommentDto.getText())
                .build();

        when(itemService.addComment(eq(userId), eq(testItemDto.getId()), any(CommentCreateDto.class)))
                .thenReturn(testCommentDto);

        mockMvc.perform(post("/items/{id}/comment", testItemDto.getId())
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCommentDto.getId()))
                .andExpect(jsonPath("$.text").value(testCommentDto.getText()));
    }


    @Test
    void addComment_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("new comment")
                .build();

        mockMvc.perform(post("/items/{id}/comment", testItemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }
}