package ru.practicum.shareit.itemRequest;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestRepositoryTest {

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void givenNewRequest_whenSave_thenSuccess() {
        User user = User.builder().name("user").email("user@mail.com").build();
        entityManager.persist(user);
        ItemRequest itemRequest = ItemRequest.builder().description("desc").created(LocalDateTime.now())
                .user(user).build();
        ItemRequest itemRequestAdded = itemRequestRepository.save(itemRequest);
        assertThat(entityManager.find(ItemRequest.class, itemRequestAdded.getId())).isEqualTo(itemRequestAdded);
    }

    @Test
    public void findByUserIdOrderByCreatedAscTest() {
        User user = User.builder().name("user").email("user@mail.com").build();
        entityManager.persist(user);
        ItemRequest itemRequest = ItemRequest.builder().description("desc").created(LocalDateTime.now())
                .user(user).build();
        entityManager.persist(itemRequest);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByUserIdOrderByCreatedAsc(user.getId());
        assertNotNull(itemRequestList);
        assertEquals(1, itemRequestList.size());
        assertEquals("desc", itemRequestList.get(0).getDescription());
    }

    @Test
    public void findByUserIdNotOrderByCreatedAscTest() {
        User user = User.builder().name("user").email("user@mail.com").build();
        entityManager.persist(user);
        ItemRequest itemRequest = ItemRequest.builder().description("desc").created(LocalDateTime.now())
                .user(user).build();
        entityManager.persist(itemRequest);
        List<ItemRequest> itemRequestList = itemRequestRepository
                .findByUserIdNotOrderByCreatedAsc(user.getId(), PageRequest.of(0, 20));
        assertNotNull(itemRequestList);
        assertEquals(0, itemRequestList.size());
    }
}
