package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

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
        User user = User.builder().name("user").email("user@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("item").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Comment comment = Comment.builder().user(user)
                .item(item).text("text").created(LocalDateTime.now()).build();
        Comment insertedComment = commentRepository.save(comment);
        assertThat(entityManager.find(Comment.class, insertedComment.getId())).isEqualTo(comment);
    }

    @Test
    public void givenComments_findByItemEntityId_thenSuccess() {
        User user = User.builder().name("user").email("user@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("item").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Comment comment = Comment.builder().user(user)
                .item(item).text("text").created(LocalDateTime.now()).build();
        entityManager.persist(comment);
        List<Comment> commentEntities = commentRepository.findByItemId(item.getId());
        assertNotNull(commentEntities);
        assertEquals(1, commentEntities.size());
        assertEquals(comment, commentEntities.get(0));
        assertEquals(user, commentEntities.get(0).getUser());
        assertEquals(item, commentEntities.get(0).getItem());
    }
}
