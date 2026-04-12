package ru.practicum.shareit.request;

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

    private UserDto createTestUser() {
        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .name("Test")
                .build();

        return userService.add(userDto);
    }
}
