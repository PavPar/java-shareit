package ru.practicum.shareit.service.request;

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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

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
public class RequestServiceImplTest {
    @Autowired
    @Qualifier("itemServiceImpl")
    private ItemService itemService;

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @Autowired
    private ItemRequestService itemRequestService;


    @Autowired
    private EntityManager em;

    @Test
    void shouldAddRequest() throws InterruptedException {
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

        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto
                .builder()
                .description("somthing")
                .build();
        ItemRequestDto itemDto = itemRequestService.save(commenterUser.getId(), itemRequestCreateDto);
        ItemRequest itemRequest = getRequestFromDB(itemDto.getId());


        Assertions.assertNotNull(itemRequest);
        assertThat(itemRequest.getId()).isNotNull();
        assertThat(itemRequest.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());
        assertThat(itemRequest.getRequester().getId()).isEqualTo(commenterUser.getId());
    }

    private ItemRequest getRequestFromDB(Long id) {
        try {
            TypedQuery<ItemRequest> query = em
                    .createQuery("Select c from ItemRequest c " +
                                    "where c.id = :id",
                            ItemRequest.class);
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

    @Test
    void shouldGetRequestById() {
        UserDto user = createTestUser();
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("request test 1")
                .build();
        ItemRequestDto created = itemRequestService.save(user.getId(), createDto);

        ItemRequestDto found = itemRequestService.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getDescription()).isEqualTo(createDto.getDescription());
    }

    @Test
    void getRequestById_WhenRequestNotFound_ShouldThrowException() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            itemRequestService.getById(999L);
        });
    }

    @Test
    void shouldGetUserRequests() {
        UserDto user = createTestUser();

        ItemRequestCreateDto request1 = ItemRequestCreateDto.builder()
                .description("request test 1")
                .build();
        ItemRequestCreateDto request2 = ItemRequestCreateDto.builder()
                .description("request test 2")
                .build();

        itemRequestService.save(user.getId(), request1);
        itemRequestService.save(user.getId(), request2);

        List<ItemRequestDto> requests = itemRequestService.getAllFromUser(user.getId());

        assertThat(requests).isNotNull();
        assertThat(requests.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetAllRequests() {
        UserDto user1 = createTestUser();
        UserDto user2 = createTestUser();

        ItemRequestCreateDto request1 = ItemRequestCreateDto.builder()
                .description("request test 1")
                .build();
        ItemRequestCreateDto request2 = ItemRequestCreateDto.builder()
                .description("request test 2")
                .build();

        itemRequestService.save(user1.getId(), request1);
        itemRequestService.save(user2.getId(), request2);

        List<ItemRequestDto> requests = itemRequestService.getAll();

        assertThat(requests).isNotNull();
        assertThat(requests.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void createRequest_WhenUserNotFound_ShouldThrowException() {
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("request test 1")
                .build();

        Assertions.assertThrows(NotFoundException.class, () -> {
            itemRequestService.save(999L, createDto);
        });
    }

    @Test
    void getUserRequests_WhenUserHasNoRequests_ShouldReturnEmptyList() {
        UserDto user = createTestUser();

        List<ItemRequestDto> requests = itemRequestService.getAllFromUser(user.getId());

        assertThat(requests).isNotNull();
        assertThat(requests.size()).isEqualTo(0);
    }

    @Test
    void shouldGetRequestWithItems() throws InterruptedException {
        UserDto user = createTestUser();

        ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
                .description("request test 1")
                .build();
        ItemRequestDto request = itemRequestService.save(user.getId(), requestDto);

        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("item")
                .description("item desc")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.add(user.getId(), itemCreateDto);

        ItemRequestDto found = itemRequestService.getById(request.getId());

        assertThat(found).isNotNull();
        assertThat(found.getItems()).isNotNull();
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
