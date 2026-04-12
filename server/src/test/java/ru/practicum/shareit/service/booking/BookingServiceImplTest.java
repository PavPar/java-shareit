package ru.practicum.shareit.service.booking;

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
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

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
    private static long userCounter = 0l;
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

    @Test
    void shouldGetBookingById() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        BookingDto booking = createTestBooking(booker, item.getId());

        BookingDto found = bookingService.getBooking(booking.getId(), booker.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingById_WhenBookingNotFound_ShouldThrowException() {
        UserDto user = createTestUser();

        Assertions.assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(user.getId(), 999L);
        });
    }

    @Test
    void shouldGetAllUserBookings() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);

        createTestBooking(booker, item.getId());
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), null);

        assertThat(bookings).isNotNull();
        assertThat(bookings.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetAllUserBookingsWithStateWaiting() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), BookingGetAllState.WAITING);

        assertThat(bookings).isNotNull();
    }

    @Test
    void shouldGetAllUserBookingsWithStateCurrent() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), BookingGetAllState.CURRENT);

        assertThat(bookings).isNotNull();
    }

    @Test
    void shouldGetAllUserBookingsWithStatePast() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), BookingGetAllState.PAST);

        assertThat(bookings).isNotNull();
    }

    @Test
    void shouldGetAllUserBookingsWithStateFuture() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), BookingGetAllState.FUTURE);

        assertThat(bookings).isNotNull();
    }

    @Test
    void shouldGetAllUserBookingsWithStateRejected() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllUserBookings(booker.getId(), BookingGetAllState.REJECTED);

        assertThat(bookings).isNotNull();
    }

    @Test
    void shouldGetAllOwnerBookings() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        ItemDto item = createTestItem(owner);
        createTestBooking(booker, item.getId());

        List<BookingDto> bookings = bookingService.getAllBookingsCreatedByUser(owner.getId(), null);

        assertThat(bookings).isNotNull();
    }

    @Test
    void changeBookingStatus_WhenUserNotOwner_ShouldThrowException() {
        UserDto owner = createTestUser();
        UserDto booker = createTestUser();
        UserDto stranger = createTestUser();
        ItemDto item = createTestItem(owner);
        BookingDto booking = createTestBooking(booker, item.getId());

        Assertions.assertThrows(ForbiddenException.class, () -> {
            bookingService.changeApprovedStatusTo(stranger.getId(), booking.getId(), true);
        });
    }

    @Test
    void changeBookingStatus_WhenBookingNotFound_ShouldThrowException() {
        UserDto user = createTestUser();

        Assertions.assertThrows(NotFoundException.class, () -> {
            bookingService.changeApprovedStatusTo(user.getId(), 999L, true);
        });
    }

    @Test
    void createBooking_WhenItemNotFound_ShouldThrowException() {
        UserDto booker = createTestUser();

        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        Assertions.assertThrows(NotFoundException.class, () -> {
            bookingService.add(booker.getId(), bookingCreateDto);
        });
    }

    @Test
    void createBooking_WhenUserBooksOwnItem_ShouldThrowException() {
        UserDto owner = createTestUser();
        ItemDto item = createTestItem(owner);

        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            bookingService.add(owner.getId(), bookingCreateDto);
        });
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
                .email("test" + userCounter++ + "@example.com")
                .name("test")
                .build();

        return userService.add(userDto);
    }

    private ItemDto createTestItem(UserDto owner) {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("test item")
                .description("test desc")
                .available(true)
                .build();
        return itemService.add(owner.getId(), itemCreateDto);
    }

    private BookingDto createTestBooking(UserDto booker, Long itemId) {
        BookingCreateDto bookingCreateDto = BookingCreateDto
                .builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        return bookingService.add(booker.getId(), bookingCreateDto);
    }
}
