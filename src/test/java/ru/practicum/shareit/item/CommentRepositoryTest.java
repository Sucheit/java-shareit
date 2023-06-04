package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.CommentEntity;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.time.LocalDateTime;
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
        UserEntity userEntity = UserEntity.builder().name("user").email("user@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("item").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        CommentEntity commentEntity = CommentEntity.builder().userEntity(userEntity)
                .itemEntity(itemEntity).text("text").created(LocalDateTime.now()).build();
        CommentEntity insertedComment = commentRepository.save(commentEntity);
        assertThat(entityManager.find(CommentEntity.class, insertedComment.getId())).isEqualTo(commentEntity);
    }

    @Test
    public void givenComments_findByItemEntityId_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("user").email("user@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("item").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        CommentEntity commentEntity = CommentEntity.builder().userEntity(userEntity)
                .itemEntity(itemEntity).text("text").created(LocalDateTime.now()).build();
        entityManager.persist(commentEntity);
        List<CommentEntity> commentEntities = commentRepository.findByItemEntityId(itemEntity.getId());
        assertNotNull(commentEntities);
        assertEquals(1, commentEntities.size());
        assertEquals(commentEntity, commentEntities.get(0));
        assertEquals(userEntity, commentEntities.get(0).getUserEntity());
        assertEquals(itemEntity, commentEntities.get(0).getItemEntity());
    }
}
