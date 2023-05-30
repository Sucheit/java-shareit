package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.BookingEntity;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByUserEntityId(Long bookerId);

    List<BookingEntity> findByItemEntityUserEntityId(Long ownerId);

    List<BookingEntity> findByItemEntityId(Long itemId);

    List<BookingEntity> findByItemEntityIdAndUserEntityId(Long itemId, Long bookerId);
}
