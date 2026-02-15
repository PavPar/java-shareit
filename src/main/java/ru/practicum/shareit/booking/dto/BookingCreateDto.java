package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull
    LocalDateTime start;
    @NotNull
    @Future
    LocalDateTime end;
    @NotNull
    Long itemId;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndAfterStart() {
        if (start == null || end == null) {
            return true; // @NotNull поймает это раньше
        }
        return end.isAfter(start);
    }

    @AssertTrue(message = "Start and end dates cannot be equal")
    public boolean isNotEqual() {
        if (start == null || end == null) {
            return true;
        }
        return !end.isEqual(start);
    }

    @AssertTrue(message = "Start date must be in future or present with 2 sec tolerance")
    public boolean isValidStart() {
        if (start == null) return true;

        return !start.isBefore(LocalDateTime.now().minusSeconds(2));
    }
}

