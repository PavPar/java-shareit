package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

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

    private UserDto createTestUser() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        return userService.add(userDto);
    }
}
