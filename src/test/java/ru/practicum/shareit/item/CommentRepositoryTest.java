package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.CommentEntity;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void givenNewComment_whenSave_thenSuccess() {
        CommentEntity commentEntity = CommentEntity.builder().text("text").build();
        CommentEntity insertedComment = commentRepository.save(commentEntity);
        assertThat(entityManager.find(CommentEntity.class, insertedComment.getId())).isEqualTo(commentEntity);
    }

    @Test
    public void givenComments_findByItemEntityId_thenSuccess() {
        UserEntity user = UserEntity.builder().name("username").email("username@mail.com").build();
        ItemEntity item = ItemEntity.builder().name("name").description("desc").build();
        CommentEntity commentEntity = CommentEntity.builder().itemEntity(item).text("text").userEntity(user).build();
        entityManager.persist(commentEntity);
        List<CommentEntity> commentEntities = commentRepository.findByItemEntityId(1L);
        assertNotNull(commentEntities);
        assertEquals(1, commentEntities.size());
        assertEquals(commentEntity, commentEntities.get(0));
    }
}
