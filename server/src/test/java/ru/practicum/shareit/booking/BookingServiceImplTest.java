package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
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
public class BookingServiceImplTest {
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
    void shouldCreateBooking() throws InterruptedException {
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

        UserDto booker = createTestUser();

        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(itemAfterCreation.getId())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusSeconds(1))
                .build();

        BookingDto bookingAfterCreateDto = bookingService.add(booker.getId(), bookingCreateDto);
        Booking bookingAfterCreate = getBookingFromDB(bookingAfterCreateDto.getId());

        Assertions.assertNotNull(bookingAfterCreate);
        assertThat(bookingAfterCreate.getId()).isNotNull();
        assertThat(bookingAfterCreate.getStart()).isEqualTo(bookingCreateDto.getStart());
        assertThat(bookingAfterCreate.getEnd()).isEqualTo(bookingCreateDto.getEnd());
        assertThat(bookingAfterCreate.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void shouldChangeBookingStatus() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Test")
                .description("test desc")
                .build();

        UserDto owner = createTestUser();

        ItemDto item = itemService.add(owner.getId(), itemCreateDto);

        Item itemAfterCreation = getItemFromDB(item.getId());

        Assertions.assertNotNull(itemAfterCreation);
        assertThat(itemAfterCreation.getId()).isNotNull();
        assertThat(itemAfterCreation.getName()).isEqualTo(itemCreateDto.getName());

        UserDto booker = createTestUser();

        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(itemAfterCreation.getId())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusSeconds(1))
                .build();

        BookingDto bookingAfterCreateDto = bookingService.add(booker.getId(), bookingCreateDto);
        Booking bookingAfterCreate = getBookingFromDB(bookingAfterCreateDto.getId());

        Assertions.assertNotNull(bookingAfterCreate);
        assertThat(bookingAfterCreate.getId()).isNotNull();
        assertThat(bookingAfterCreate.getStart()).isEqualTo(bookingCreateDto.getStart());
        assertThat(bookingAfterCreate.getEnd()).isEqualTo(bookingCreateDto.getEnd());
        assertThat(bookingAfterCreate.getBooker().getId()).isEqualTo(booker.getId());

        bookingService.changeApprovedStatusTo(owner.getId(), bookingAfterCreate.getId(), true);
        Booking bookingAfterStatusChange = getBookingFromDB(bookingAfterCreateDto.getId());

        Assertions.assertNotNull(bookingAfterCreate);
        Assertions.assertNotNull(bookingAfterStatusChange);
        assertThat(bookingAfterStatusChange.getStatus().toString()).isEqualTo(BookingStatus.APPROVED.toString());
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

    private Booking getBookingFromDB(Long id) {
        try {
            TypedQuery<Booking> query = em
                    .createQuery("Select i from Booking i " +
                                    "where i.id = :id",
                            Booking.class);
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
