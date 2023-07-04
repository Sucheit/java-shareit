package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByUserIdOrderByIdAsc(Long userId, PageRequest pageRequest);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, PageRequest pageRequest);

    List<Item> findByItemRequestId(Long id);
}
