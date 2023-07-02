package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRepositoryTest {

    final PageRequest pageRequest = PageRequest.of(0, 20);
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    TestEntityManager entityManager;

    @Test
    public void givenNewItem_whenSave_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        Item insertedItem = itemRepository.save(item);
        assertThat(entityManager.find(Item.class, insertedItem.getId())).isEqualTo(item);
    }

    @Test
    public void givenItemCreated_whenUpdate_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        String newName = "NewName";
        item.setName(newName);
        itemRepository.save(item);
        assertThat(entityManager.find(Item.class, item.getId()).getName()).isEqualTo(newName);
    }

    @Test
    public void givenItemCreated_whenFindById_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Optional<Item> retrievedItem = itemRepository.findById(item.getId());
        assertThat(retrievedItem).contains(item);
    }

    @Test
    public void givenItemCreated_whenDelete_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        itemRepository.delete(item);
        assertThat(entityManager.find(Item.class, item.getId())).isNull();
    }

    @Test
    public void givenUserAndItemCreated_whenFindByUserId_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        List<Item> items = itemRepository.findByUserIdOrderByIdAsc(user.getId(), pageRequest);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    public void givenItems_whenFindByNameOrDesc_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item1 = Item.builder().name("hammer").description("desc")
                .available(Boolean.TRUE).user(user).build();
        Item item2 = Item.builder().name("name").description("welding")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item1);
        entityManager.persist(item2);
        List<Item> items1 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("hAmM", "hAmM", pageRequest);
        assertNotNull(items1);
        assertEquals(1, items1.size());
        assertEquals(item1, items1.get(0));
        List<Item> items2 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("WEld", "WEld", pageRequest);
        assertNotNull(items2);
        assertEquals(1, items2.size());
        assertEquals(item2, items2.get(0));
    }
}
