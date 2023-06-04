package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {


    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void givenNewItem_whenSave_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        ItemEntity insertedItemEntity = itemRepository.save(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, insertedItemEntity.getId())).isEqualTo(itemEntity);
    }

    @Test
    public void givenItemCreated_whenUpdate_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        String newName = "NewName";
        itemEntity.setName(newName);
        itemRepository.save(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, itemEntity.getId()).getName()).isEqualTo(newName);
    }

    @Test
    public void givenItemCreated_whenFindById_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        Optional<ItemEntity> retrievedItem = itemRepository.findById(itemEntity.getId());
        assertThat(retrievedItem).contains(itemEntity);
    }

    @Test
    public void givenItemCreated_whenDelete_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        itemRepository.delete(itemEntity);
        assertThat(entityManager.find(ItemEntity.class, itemEntity.getId())).isNull();
    }

    @Test
    public void givenUserAndItemCreated_whenFindByUserId_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity);
        List<ItemEntity> items = itemRepository.findByUserEntityId(userEntity.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(itemEntity, items.get(0));
    }

    @Test
    public void givenItems_whenFindByNameOrDesc_thenSuccess() {
        UserEntity userEntity = UserEntity.builder().name("username").email("email@mail.com").build();
        entityManager.persist(userEntity);
        ItemEntity itemEntity1 = ItemEntity.builder().name("hammer").description("desc")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        ItemEntity itemEntity2 = ItemEntity.builder().name("name").description("welding")
                .available(Boolean.TRUE).userEntity(userEntity).build();
        entityManager.persist(itemEntity1);
        entityManager.persist(itemEntity2);
        List<ItemEntity> items1 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("hAmM", "hAmM");
        assertNotNull(items1);
        assertEquals(1, items1.size());
        assertEquals(itemEntity1, items1.get(0));
        List<ItemEntity> items2 = itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("WEld", "WEld");
        assertNotNull(items2);
        assertEquals(1, items2.size());
        assertEquals(itemEntity2, items2.get(0));
    }
}
