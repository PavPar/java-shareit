package ru.practicum.shareit.controller.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @Qualifier("bookingServiceImpl")
    private BookingService bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void getOne_ShouldReturnOk() throws Exception {
        when(bookingService.getBooking(any(), any())).thenReturn(BookingDto.builder().id(1L).build());

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnOk() throws Exception {
        when(bookingService.getAllUserBookings(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_ShouldReturnOk() throws Exception {
        when(bookingService.getAllBookingsCreatedByUser(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void add_ShouldReturnOk() throws Exception {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        when(bookingService.add(any(), any())).thenReturn(BookingDto.builder().id(1L).build());

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void changeApprove_ShouldReturnOk() throws Exception {
        when(bookingService.changeApprovedStatusTo(any(), any(), any())).thenReturn(BookingDto.builder().id(1L).build());

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }
}
