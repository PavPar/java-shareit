package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private final String userHeader = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient client;

    @Autowired
    private BookingController controller;

    @Test
    void getAll_WithoutState_ShouldReturnAllBookings() throws Exception {
        Long ownerId = 1L;
        BookingDto dto1 = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        BookingDto dto2 = BookingDto.builder()
                .id(2L)
                .status(BookingStatus.APPROVED)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(List.of(dto1, dto2));

        when(client.getAll(eq(ownerId), isNull()))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client, times(1)).getAll(eq(ownerId), isNull());
    }

    @Test
    void getAll_WithWaitingState_ShouldReturnWaitingBookings() throws Exception {
        Long ownerId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(dto);

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.WAITING)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId)
                        .param("state", BookingGetAllState.WAITING.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.WAITING));
    }

    @Test
    void getAll_WithCurrentState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(dto);

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.CURRENT)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId)
                        .param("state", BookingGetAllState.CURRENT.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.CURRENT));
    }

    @Test
    void getAll_WithPastState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(dto);

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.PAST)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId)
                        .param("state", BookingGetAllState.PAST.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.PAST));
    }

    @Test
    void getAll_WithFutureState_ShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(dto);

        when(client.getAll(eq(ownerId), eq(BookingGetAllState.FUTURE)))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId)
                        .param("state", BookingGetAllState.FUTURE.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(client).getAll(eq(ownerId), eq(BookingGetAllState.FUTURE));
    }

    @Test
    void getAll_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;

        mockMvc.perform(get("/bookings")
                        .header(userHeader, ownerId)
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
                        .header(userHeader, owner.getId()))
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
                        .header(userHeader, owner.getId()))
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

        BookingDto dto1 = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        BookingDto dto2 = BookingDto.builder()
                .id(2L)
                .status(BookingStatus.APPROVED)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJson = mapper.writeValueAsString(List.of(dto1, dto2));

        when(client.getAllByOwner(eq(owner.getId()), isNull()))
                .thenReturn(ResponseEntity.ok(expectedJson));

        mockMvc.perform(get("/bookings/owner")
                        .header(userHeader, owner.getId()))
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
                        .header(userHeader, ownerId)
                        .param("state", state))
                .andExpect(status().isOk());

        verify(client).getAllByOwner(eq(ownerId), eq(BookingGetAllState.WAITING));
    }

    @Test
    void getAllByOwner_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;

        mockMvc.perform(get("/bookings/owner")
                        .header(userHeader, ownerId)
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
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(199L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1))
                .build();
        ObjectMapper mapper = createMapper();
        String createJson = mapper.writeValueAsString(dto);

        BookingDto bookingDto = BookingDto.builder()
                .id(10)
                .status(BookingStatus.WAITING)
                .item(ItemDto.builder().id(1).build())
                .build();


        when(client.add(eq(ownerId), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header(userHeader, ownerId)
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
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1))
                .build();
        ObjectMapper mapper = createMapper();
        String createJson = mapper.writeValueAsString(dto);

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
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().minusDays(1))
                .build();
        ObjectMapper mapper = createMapper();
        String invalidJson = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/bookings")
                        .header(userHeader, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WithNullStartDate_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .end(LocalDateTime.now().minusDays(1))
                .build();
        ObjectMapper mapper = createMapper();
        String invalidJson = mapper.writeValueAsString(dto);


        mockMvc.perform(post("/bookings")
                        .header(userHeader, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1))
                .build();
        ObjectMapper mapper = createMapper();
        String createJson = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest());

        verify(client, never()).add(anyLong(), any());
    }

    @Test
    void add_WhenItemNotAvailable_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1))
                .build();
        ObjectMapper mapper = createMapper();
        String createJson = mapper.writeValueAsString(dto);

        when(client.add(eq(ownerId), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.badRequest().body("Item not available"));

        mockMvc.perform(post("/bookings")
                        .header(userHeader, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest());

    }

    @Test
    void changeApprove_WithApprovedTrue_ShouldApproveBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = true;
        BookingDto dto = BookingDto.builder()
                .id(bookingId)
                .status(BookingStatus.APPROVED)
                .build();
        ObjectMapper mapper = createMapper();
        String updatedJson = mapper.writeValueAsString(dto);

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.ok(updatedJson));

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userHeader, ownerId)
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
        BookingDto dto = BookingDto.builder()
                .id(bookingId)
                .status(BookingStatus.REJECTED)
                .build();
        ObjectMapper mapper = createMapper();
        String updatedJson = mapper.writeValueAsString(dto);

        when(client.changeApprovedStatusTo(ownerId, bookingId, approved))
                .thenReturn(ResponseEntity.ok(updatedJson));

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userHeader, ownerId)
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
                        .header(userHeader, ownerId)
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
                        .header(userHeader, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isBadRequest());

        verify(client).changeApprovedStatusTo(ownerId, bookingId, approved);
    }

    @Test
    void changeApprove_WithoutApprovedParam_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userHeader, ownerId))
                .andExpect(status().isBadRequest());

        verify(client, never()).changeApprovedStatusTo(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void changeApprove_WithInvalidApprovedParam_ShouldReturnBadRequest() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userHeader, ownerId)
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
                        .header(userHeader, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isNotFound());

        verify(client).changeApprovedStatusTo(ownerId, nonExistentId, approved);
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
