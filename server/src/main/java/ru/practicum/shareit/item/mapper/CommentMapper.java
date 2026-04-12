package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .item(ItemMapper.toDto(comment.getItem()))
                .build();
    }

    public static Comment toEntity(CommentCreateDto comment) {
        return Comment.builder()
                .text(comment.getText())
                .build();
    }
}
