package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    List<ItemEntity> findByUserEntity(UserEntity userEntity);

    List<ItemEntity> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
