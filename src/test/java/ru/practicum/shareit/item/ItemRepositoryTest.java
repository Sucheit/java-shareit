package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRepositoryTest {


    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void givenNewItem_whenSave_thenSuccess() {
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc").build();
        ItemEntity insertedItemEntity = itemRepository.save(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, insertedItemEntity.getId())).isEqualTo(itemEntity);
    }

    @Test
    public void givenItemCreated_whenUpdate_thenSuccess() {
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc").build();
        entityManager.persist(itemEntity);
        String newName = "NewName";
        itemEntity.setName(newName);
        itemRepository.save(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, itemEntity.getId()).getName()).isEqualTo(newName);
    }

    @Test
    public void givenItemCreated_whenFindById_thenSuccess() {
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc").build();
        entityManager.persist(itemEntity);
        Optional<ItemEntity> retrievedItem = itemRepository.findById(itemEntity.getId());
        assertThat(retrievedItem).contains(itemEntity);
    }

    @Test
    public void givenItemCreated_whenDelete_thenSuccess() {
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc").build();
        entityManager.persist(itemEntity);
        itemRepository.delete(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, itemEntity.getId())).isNull();
    }

    @Test
    public void givenUserAndItemCreated_whenFindByUserId_thenSuccess() {
        UserEntity user = UserEntity.builder().name("name").email("email@mail.com").build();
        ItemEntity item = ItemEntity.builder().name("name").description("desc").userEntity(user).build();
        entityManager.persist(user);
        entityManager.persist(item);
        List<ItemEntity> items = itemRepository.findByUserEntity(user);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    public void givenItems_whenFindByNameOrDesc_thenSuccess() {
        ItemEntity item1 = ItemEntity.builder().name("hammer").description("desc").build();
        ItemEntity item2 = ItemEntity.builder().name("name").description("welding").build();
        entityManager.persist(item1);
        entityManager.persist(item2);
        List<ItemEntity> items1 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("hAmM", "hAmM");
        assertNotNull(items1);
        assertEquals(1, items1.size());
        assertEquals(item1, items1.get(0));
        List<ItemEntity> items2 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("WEld", "WEld");
        assertNotNull(items2);
        assertEquals(1, items2.size());
        assertEquals(item2, items2.get(0));
    }
}
