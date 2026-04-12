package ru.practicum.shareit.mappers.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ItemMapperTest {

    private static final User OWNER = User.builder()
            .id(1L)
            .name("test owner")
            .email("test-owner@mail.com")
            .build();

    @Test
    void toDto_ShouldMapAllFields() {
        Item item = Item.builder()
                .id(1L)
                .name("test item")
                .description("test item description")
                .available(true)
                .ownerId(OWNER.getId())
                .build();

        ItemDto dto = ItemMapper.toDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.isAvailable()).isTrue();
    }

    @Test
    void toDto_WithComments_ShouldMapComments() {
        Item item = createTestItem();
        Comment comment = Comment.builder()
                .id(1L)
                .text("test comment")
                .author(OWNER)
                .created(LocalDateTime.now())
                .item(item)
                .build();

        List<Comment> comments = List.of(comment);
        ItemDto dto = ItemMapper.toDto(item, comments);

        assertThat(dto).isNotNull();
        assertThat(dto.getComments()).isNotNull();
        assertThat(dto.getComments().size()).isEqualTo(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo(comment.getText());
    }

    @Test
    void toDto_WithEmptyComments_ShouldReturnEmptyList() {
        Item item = createTestItem();
        List<Comment> comments = List.of();

        ItemDto dto = ItemMapper.toDto(item, comments);

        assertThat(dto).isNotNull();
        assertThat(dto.getComments().size()).isEqualTo(0);
    }

    @Test
    void toDto_WithLastAndNextBooking_ShouldMapBookings() {
        Item item = createTestItem();
        User booker = createTestUser();

        List<Comment> comments = List.of();

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .booker(booker)
                .item(item)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .booker(booker)
                .item(item)
                .build();

        ItemDto dto = ItemMapper.toDto(item, comments, lastBooking, nextBooking);

        assertThat(dto).isNotNull();
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(lastBooking.getId());
        assertThat(dto.getNextBooking().getId()).isEqualTo(nextBooking.getId());
    }

    @Test
    void toDto_WithNullBookings_ShouldNotSetBookings() {
        Item item = createTestItem();
        List<Comment> comments = List.of();

        ItemDto dto = ItemMapper.toDto(item, comments, null, null);

        assertThat(dto).isNotNull();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void toDto_WithOnlyLastBooking_ShouldMapOnlyLast() {
        Item item = createTestItem();
        List<Comment> comments = List.of();
        User booker = createTestUser();

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .booker(booker)
                .item(item)
                .build();

        ItemDto dto = ItemMapper.toDto(item, comments, lastBooking, null);

        assertThat(dto).isNotNull();
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(1L);
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void toDto_WithOnlyNextBooking_ShouldMapOnlyNext() {
        Item item = createTestItem();
        List<Comment> comments = List.of();
        User booker = createTestUser();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .booker(booker)
                .item(item)
                .build();

        ItemDto dto = ItemMapper.toDto(item, comments, null, nextBooking);

        assertThat(dto).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(nextBooking.getId());
        assertThat(dto.getLastBooking()).isNull();
    }

    @Test
    void toEntity_FromItemDto_ShouldMapAllFields() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("test desc")
                .available(false)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.isAvailable()).isFalse();
    }

    @Test
    void toEntity_FromItemUpdateDto_ShouldMapAllFields() {
        ItemUpdateDto dto = ItemUpdateDto.builder()
                .name("item updated")
                .description("updated desc")
                .available(true)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.isAvailable()).isTrue();
    }

    @Test
    void toEntity_FromItemUpdateDto_WithNullFields_ShouldMapNulls() {
        ItemUpdateDto dto = ItemUpdateDto.builder()
                .name(null)
                .description(null)
                .available(false)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isNull();
        assertThat(entity.getDescription()).isNull();
        assertThat(entity.isAvailable()).isFalse();
    }

    @Test
    void toEntity_FromItemCreateDto_ShouldMapAllFields() {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("item")
                .description("item desc")
                .available(true)
                .requestId(5L)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.isAvailable()).isTrue();
        assertThat(entity.getRequestId()).isEqualTo(dto.getRequestId());
    }

    @Test
    void toEntity_FromItemCreateDto_WithNullDescription_ShouldSetEmptyString() {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("item")
                .description(null)
                .available(true)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getDescription()).isEqualTo("");
    }

    @Test
    void toEntity_FromItemCreateDto_WithNullAvailable_ShouldSetDefaultTrue() {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("item")
                .description("item desc")
                .available(null)
                .build();

        Item entity = ItemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.isAvailable()).isTrue();
    }

    @Test
    void mapToItem_ShouldUpdateOnlyName() {
        Item existingItem = Item.builder()
                .id(1L)
                .name("item name")
                .description("item desc")
                .available(true)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("item new name")
                .build();

        Item updatedItem = ItemMapper.mapToItem(updateDto, existingItem);

        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getId()).isEqualTo(existingItem.getId());
        assertThat(updatedItem.getName()).isEqualTo(updateDto.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(existingItem.getDescription());
        assertThat(updatedItem.isAvailable()).isTrue();
    }

    @Test
    void mapToItem_ShouldUpdateOnlyDescription() {
        Item existingItem = createTestItem();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .description("new desc")
                .build();

        Item updatedItem = ItemMapper.mapToItem(updateDto, existingItem);

        assertThat(updatedItem.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(updatedItem.getName()).isEqualTo(existingItem.getName());
    }

    @Test
    void mapToItem_ShouldUpdateOnlyAvailable() {
        Item existingItem = createTestItem();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .available(false)
                .build();

        Item updatedItem = ItemMapper.mapToItem(updateDto, existingItem);

        assertThat(updatedItem.isAvailable()).isFalse();
        assertThat(updatedItem.getName()).isEqualTo(existingItem.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(existingItem.getDescription());
    }

    @Test
    void mapToItem_ShouldUpdateAllFields() {
        Item existingItem = createTestItem();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("item")
                .description("item desc")
                .available(false)
                .build();

        Item updatedItem = ItemMapper.mapToItem(updateDto, existingItem);

        assertThat(updatedItem.getName()).isEqualTo(updateDto.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(updatedItem.isAvailable()).isFalse();
    }

    private Item createTestItem() {
        return Item.builder()
                .id(1L)
                .name("test item")
                .description("test desc")
                .available(true)
                .ownerId(OWNER.getId())
                .build();
    }

    private static long userCounter = 0l;

    private User createTestUser() {
        return User.builder()
                .id(userCounter)
                .email("test" + userCounter++ + "@example.com")
                .name("test")
                .build();
    }

}
