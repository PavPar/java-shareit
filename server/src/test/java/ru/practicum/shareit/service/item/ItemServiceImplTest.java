package ru.practicum.shareit.service.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

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
public class ItemServiceImplTest {
    @Autowired
    @Qualifier("itemServiceImpl")
    private ItemService itemService;
    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;
    @Autowired
    @Qualifier("bookingServiceImpl")
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private EntityManager em;


    @Test
    void shouldAddItemToDB() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Test")
                .build();

        UserDto user = createTestUser();

        ItemDto item = itemService.add(user.getId(), itemCreateDto);

        Item itemAfterCreation = getItemFromDB(item.getId());

        Assertions.assertNotNull(itemAfterCreation);
        assertThat(itemAfterCreation.getId()).isNotNull();
        assertThat(itemAfterCreation.getName()).isEqualTo(itemCreateDto.getName());
    }

    @Test
    void shouldUpdateItem_Name() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Test")
                .description("test desc")
                .build();

        UserDto user = createTestUser();

        ItemDto item = itemService.add(user.getId(), itemCreateDto);

        Item itemAfterCreation = getItemFromDB(item.getId());

        Assertions.assertNotNull(itemAfterCreation);
        assertThat(itemAfterCreation.getId()).isNotNull();
        assertThat(itemAfterCreation.getName()).isEqualTo(itemCreateDto.getName());

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("New Name")
                .build();

        ItemDto itemAfterUpdate = itemService.update(user.getId(), itemAfterCreation.getId(), itemUpdateDto);

        Assertions.assertNotNull(itemAfterUpdate);
        assertThat(itemAfterUpdate.getId()).isNotNull();
        assertThat(itemAfterUpdate.getName()).isEqualTo(itemUpdateDto.getName());
        assertThat(itemAfterUpdate.getDescription()).isEqualTo(itemAfterCreation.getDescription());
    }

    @Test
    void shouldAddComment() throws InterruptedException {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Test")
                .description("test desc")
                .build();

        UserDto user = createTestUser();

        ItemDto item = itemService.add(user.getId(), itemCreateDto);

        Item itemAfterCreation = getItemFromDB(item.getId());

        Assertions.assertNotNull(itemAfterCreation);
        assertThat(itemAfterCreation.getId()).isNotNull();
        assertThat(itemAfterCreation.getName()).isEqualTo(itemCreateDto.getName());

        UserDto commenterUser = createTestUser();

        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(itemAfterCreation.getId())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusSeconds(1))
                .build();
        BookingDto createdBooking = bookingService.add(commenterUser.getId(), bookingCreateDto);
        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto
                .builder()
                .description("somthing")
                .build();
        itemRequestService.save(commenterUser.getId(), itemRequestCreateDto);
        bookingService.changeApprovedStatusTo(user.getId(), createdBooking.getId(), true);

        Thread.sleep(2000);
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("cools stuff")
                .build();

        CommentDto comment = itemService.addComment(commenterUser.getId(), itemAfterCreation.getId(), commentCreateDto);
        Comment commentAfterCreation = getCommentFromDB(comment.getId());

        Assertions.assertNotNull(commentAfterCreation);
        assertThat(commentAfterCreation.getId()).isNotNull();
        assertThat(commentAfterCreation.getText()).isEqualTo(commentCreateDto.getText());
        assertThat(commentAfterCreation.getAuthor().getId()).isEqualTo(commenterUser.getId());
    }

    @Test
    void shouldGetItemById() {
        UserDto user = createTestUser();
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("test item")
                .description("test item desc")
                .available(true)
                .build();

        ItemDto created = itemService.add(user.getId(), createDto);
        ItemDto found = itemService.getOne(user.getId(), created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo(createDto.getName());
        assertThat(found.getDescription()).isEqualTo(createDto.getDescription());
    }

    @Test
    void getItemById_WhenItemNotFound_ShouldThrowException() {
        UserDto user = createTestUser();
        Long nonExistentId = 999L;

        Assertions.assertThrows(NotFoundException.class, () -> {
            itemService.getOne(user.getId(), nonExistentId);
        });
    }

    @Test
    void shouldGetAllUserItems() {
        UserDto user = createTestUser();

        ItemCreateDto item1 = ItemCreateDto.builder()
                .name("test item 1")
                .description("desc 1")
                .build();
        ItemCreateDto item2 = ItemCreateDto.builder()
                .name("test item 2")
                .description("desc 2")
                .build();

        itemService.add(user.getId(), item1);
        itemService.add(user.getId(), item2);

        var items = itemService.getAll(user.getId());

        assertThat(items).isNotNull();
        assertThat(items.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateItem_Description() {
        UserDto user = createTestUser();
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("test item")
                .description("test item description")
                .build();

        ItemDto created = itemService.add(user.getId(), createDto);

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .description("new test item description")
                .build();

        ItemDto updated = itemService.update(user.getId(), created.getId(), updateDto);

        assertThat(updated.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(updated.getName()).isEqualTo(createDto.getName());
    }

    @Test
    void shouldUpdateItem_Availability() {
        UserDto user = createTestUser();
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("test item")
                .available(true)
                .build();

        ItemDto created = itemService.add(user.getId(), createDto);

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .available(false)
                .build();

        ItemDto updated = itemService.update(user.getId(), created.getId(), updateDto);

        assertThat(updated.isAvailable()).isFalse();
    }

    @Test
    void updateItem_WhenUserNotOwner_ShouldThrowException() {
        UserDto owner = createTestUser();
        UserDto otherUser = createTestUser();

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("test item")
                .build();

        ItemDto created = itemService.add(owner.getId(), createDto);

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("new test item")
                .build();

        Assertions.assertThrows(ForbiddenException.class, () -> {
            itemService.update(otherUser.getId(), created.getId(), updateDto);
        });
    }

    @Test
    void addItem_WithRequestId_ShouldLinkToRequest() {
        UserDto user = createTestUser();

        ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
                .description("test desc")
                .build();
        var request = itemRequestService.save(user.getId(), requestDto);

        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("test item")
                .description("test item desc")
                .requestId(request.getId())
                .build();

        ItemDto item = itemService.add(user.getId(), itemCreateDto);

        Item itemAfterCreation = getItemFromDB(item.getId());
        assertThat(itemAfterCreation.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void updateItem_WhenItemNotFound_ShouldThrowException() {
        UserDto user = createTestUser();
        Long nonExistentId = 999L;
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("test name")
                .build();

        Assertions.assertThrows(NotFoundException.class, () -> {
            itemService.update(user.getId(), nonExistentId, updateDto);
        });
    }

    @Test
    void addComment_WhenUserNotRentedItem_ShouldThrowException() {
        UserDto owner = createTestUser();
        UserDto stranger = createTestUser();

        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("test name")
                .description("test desc")
                .build();

        ItemDto item = itemService.add(owner.getId(), itemCreateDto);
        Item itemAfterCreation = getItemFromDB(item.getId());

        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("test comment")
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            itemService.addComment(stranger.getId(), itemAfterCreation.getId(), commentCreateDto);
        });
    }

    @Test
    void addComment_WhenItemNotFound_ShouldThrowException() {
        UserDto user = createTestUser();
        Long nonExistentId = 999L;
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("test comment")
                .build();

        Assertions.assertThrows(NotFoundException.class, () -> {
            itemService.addComment(user.getId(), nonExistentId, commentCreateDto);
        });
    }

    @Test
    void shouldSearchItems() {
        UserDto user = createTestUser();
        ItemCreateDto drill = ItemCreateDto.builder()
                .name("test item 1")
                .description("test item 1 desc")
                .available(true)
                .build();
        ItemCreateDto hammer = ItemCreateDto.builder()
                .name("test item 2")
                .description("test item 2 desc")
                .available(true)
                .build();

        itemService.add(user.getId(), drill);
        itemService.add(user.getId(), hammer);

        Collection<ItemDto> results = itemService.search("test");

        assertThat(results).isNotNull();
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void searchItems_WhenTextEmpty_ShouldReturnEmptyList() {
        Collection<ItemDto> results = itemService.search("");

        assertThat(results.size()).isEqualTo(0);
    }

    @Test
    void searchItems_WhenTextBlank_ShouldReturnEmptyList() {
        Collection<ItemDto> results = itemService.search("   ");

        assertThat(results.size()).isEqualTo(0);
    }

    private Comment getCommentFromDB(Long id) {
        try {
            TypedQuery<Comment> query = em
                    .createQuery("Select c from Comment c " +
                                    "where c.id = :id",
                            Comment.class);
            return query
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Item getItemFromDB(Long id) {
        try {
            TypedQuery<Item> query = em
                    .createQuery("Select i from Item i " +
                                    "where i.id = :id",
                            Item.class);
            return query
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private static long userCounter = 0L;

    private UserDto createTestUser() {
        UserDto userDto = UserDto.builder()
                .email("test" + userCounter++ + "@example.com")
                .name("test")
                .build();

        return userService.add(userDto);
    }
}
