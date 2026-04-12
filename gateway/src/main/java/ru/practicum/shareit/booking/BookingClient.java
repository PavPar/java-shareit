package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getAll(long ownerId, BookingGetAllState state) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> getAllByOwner(long ownerId, BookingGetAllState state) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );
        return get("/owner?state={state}", ownerId, parameters);
    }

    public ResponseEntity<Object> add(long userId, BookingCreateDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> changeApprovedStatusTo(long ownerId, long id, Boolean approved) {
        return patch("/" + id + "?approved=" + approved, ownerId);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }
}
