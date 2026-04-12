package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private final String USER_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient client;

    @Autowired
    private BookingController controller;

    @Test
    void getAll_WithoutState_ShouldReturnAllBookings() throws Exception {
        Long ownerId = 1L;
        String expectedJson = """
                [
                    {"id": 1, "status": "WAITING"},
                    {"id": 2, "status": "APPROVED"}
                ]
                """;

        when(client.getAll(eq(ownerId), isNull()))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client, times(1)).getAll(eq(ownerId), isNull());
    }

    @Test
    void getAll_WithWaitingState_ShouldReturnWaitingBookings() throws Exception {
        Long ownerId = 1L;
        String expectedJson = """
                [
                    {"id": 1, "status": "WAITING"}
                ]
                """;

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.WAITING)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId)
                        .param("state", BookingGetAllState.WAITING.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.WAITING));
    }

    @Test
    void getAll_WithCurrentState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        String expectedJson = """
                [
                    {"id": 1, "status": "CURRENT"}
                ]
                """;

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.CURRENT)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId)
                        .param("state", BookingGetAllState.CURRENT.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.CURRENT));
    }

    @Test
    void getAll_WithPastState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        String expectedJson = """
                [
                    {"id": 1, "status": "PAST"}
                ]
                """;

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.PAST)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId)
                        .param("state", BookingGetAllState.PAST.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.PAST));
    }

    @Test
    void getAll_WithFutureState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        String expectedJson = """
                [
                    {"id": 1, "status": "FUTURE"}
                ]
                """;

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.FUTURE)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId)
                        .param("state", BookingGetAllState.FUTURE.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.FUTURE));
    }

    @Test
    void getAll_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, ownerId)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(client, never()).getAll(anyLong(), any());
    }

    @Test
    void getAll_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());

        verify(client, never()).getAll(anyLong(), any());
    }

    @Test
    void getOne_ShouldReturnBooking() throws Exception {
        UserDto owner = UserDto.builder().id(1L).build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(5))
                .status(BookingStatus.WAITING)
                .booker(owner)
                .build();


        when(client.getBooking(owner.getId(), dto.getId()))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/bookings/{id}", dto.getId())
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()));

        verify(client, times(1)).getBooking(owner.getId(), dto.getId());
    }

    @Test
    void getOne_WhenNotFound_ShouldReturn404() throws Exception {
        UserDto owner = UserDto.builder().id(1L).build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(5))
                .status(BookingStatus.WAITING)
                .booker(owner)
                .build();


        when(client.getBooking(owner.getId(), dto.getId()))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/bookings/{id}", dto.getId())
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isNotFound());

        verify(client, times(1)).getBooking(owner.getId(), dto.getId());
    }

    @Test
    void getOne_WhenNotHeader_ShouldReturnBadRequest() throws Exception {
        UserDto owner = UserDto.builder().id(1L).build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(5))
                .status(BookingStatus.WAITING)
                .booker(owner)
                .build();


        when(client.getBooking(owner.getId(), dto.getId()))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/bookings/{id}", dto.getId()))
                .andExpect(status().isBadRequest());

        verify(client, never()).getBooking(anyLong(), anyLong());
    }

    @Test
    void getAllByOwner_WithoutState_ShouldReturnAllOwnerBookings() throws Exception {
        UserDto owner = UserDto.builder().id(1L).build();

        String expectedJson = """
                [
                    {"id": 1, "status": "WAITING"},
                    {"id": 2, "status": "WAITING"},
                    {"id": 3, "status": "WAITING"}
                ]
                """;

        when(client.getAllByOwner(eq(owner.getId()), isNull()))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client, times(1)).getAllByOwner(eq(owner.getId()), isNull());
    }

    @Test
    void getAllByOwner_WithWaitingState_ShouldReturnWaitingOwnerBookings() throws Exception {
        Long ownerId = 1L;
        String state = "WAITING";

        when(client.getAllByOwner(eq(ownerId), eq(BookingGetAllState.WAITING)))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", state))
                .andExpect(status().isOk());

        verify(client).getAllByOwner(eq(ownerId), eq(BookingGetAllState.WAITING));
    }

    @Test
    void getAllByOwner_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "Unknown"))
                .andExpect(status().isBadRequest());

        verify(client, never()).getAllByOwner(anyLong(), any());
    }


    @Test
    void getAllByOwner_WithNoHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "WAITING"))
                .andExpect(status().isBadRequest());

        verify(client, never()).getAllByOwner(anyLong(), any());
    }

    @Test
    void add_WithValidData_ShouldCreateBooking() throws Exception {
        Long ownerId = 1L;
        String createJson = """
                {
                      "itemId": 199,
                      "start": "2026-04-13T08:45:25",
                      "end": "2026-04-15T08:45:25"
                  }
                """;

        BookingDto bookingDto = BookingDto.builder()
                .id(10)
                .status(BookingStatus.WAITING)
                .item(ItemDto.builder().id(1).build())
                .build();


        when(client.add(eq(ownerId), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));

        verify(client, times(1)).add(eq(ownerId), any(BookingCreateDto.class));
    }

    @Test
    void add_WhenNoHeader_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        String createJson = """
                {
                    "itemId": 1,
                    "start": "2026-04-05T10:00:00",
                    "end": "2026-04-05T11:00:00"
                }
                """;

        BookingDto bookingDto = BookingDto.builder()
                .id(10)
                .status(BookingStatus.WAITING)
                .item(ItemDto.builder().id(1).build())
                .build();


        when(client.add(eq(ownerId), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).getAll(anyLong(), any());
    }

    @Test
    void add_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        String invalidJson = """
                {
                    "itemId": 1,
                    "start": "2024-06-05T10:00:00",
                    "end": "2024-06-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WithNullStartDate_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        String invalidJson = """
                {
                    "itemId": 1,
                    "end": "2024-06-05T10:00:00"
                }
                """;

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        String createJson = """
                {
                    "itemId": 1,
                    "start": "2024-06-01T10:00:00",
                    "end": "2024-06-05T10:00:00"
                }
                """;

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WhenItemNotAvailable_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        String createJson = """
                {
                    "itemId": 1,
                    "start": "2024-06-01T10:00:00",
                    "end": "2024-06-05T10:00:00"
                }
                """;

        when(client.add(eq(ownerId), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.badRequest().body("Item not available"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest());

    }

    @Test
    void changeApprove_WithApprovedTrue_ShouldApproveBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = true;
        String updatedJson = """
                {
                    "id": 10,
                    "status": "APPROVED"
                }
                """;

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.ok(updatedJson));

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(client, times(1))
                .changeApprovedStatusTo(ownerId, bookingId, approved);
    }

    @Test
    void changeApprove_WithApprovedFalse_ShouldRejectBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = false;
        String updatedJson = """
                {
                    "id": 10,
                    "status": "REJECTED"
                }
                """;

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.ok(updatedJson));

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(client).changeApprovedStatusTo(ownerId, bookingId, approved);
    }

    @Test
    void changeApprove_WhenUserNotOwner_ShouldReturnForbidden() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = true;

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.status(403).build());

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isForbidden());

        verify(client).changeApprovedStatusTo(ownerId, bookingId, approved);
    }

    @Test
    void changeApprove_WhenBookingAlreadyApproved_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = true;

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.badRequest().body("Booking already approved"));

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isBadRequest());

        verify(client).changeApprovedStatusTo(ownerId, bookingId, approved);
    }

    @Test
    void changeApprove_WithoutApprovedParam_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isBadRequest());

        verify(client, never()).changeApprovedStatusTo(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void changeApprove_WithInvalidApprovedParam_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "not-a-boolean"))
                .andExpect(status().isBadRequest());

        verify(client, never()).changeApprovedStatusTo(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void changeApprove_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verify(client, never()).changeApprovedStatusTo(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void changeApprove_WhenBookingDoesNotExist_ShouldReturnNotFound() throws Exception {
        Long ownerId = 1L;
        Long nonExistentId = 999L;
        Boolean approved = true;

        when(client.changeApprovedStatusTo(ownerId, nonExistentId, approved))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(patch("/bookings/{id}", nonExistentId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isNotFound());

        verify(client).changeApprovedStatusTo(ownerId, nonExistentId, approved);
    }
}
