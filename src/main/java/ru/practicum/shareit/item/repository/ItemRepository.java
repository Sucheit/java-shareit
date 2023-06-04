package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.ItemEntity;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    List<ItemEntity> findByUserEntityId(Long userId);

    List<ItemEntity> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
