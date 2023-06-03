package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByUserEntityId(Long bookerId);

    List<BookingEntity> findByUserEntityIdAndStatusEquals(Long bookerId, Status status);

    List<BookingEntity> findByUserEntityIdAndEndTimeBefore(Long bookerId, LocalDateTime endTime);

    List<BookingEntity> findByUserEntityIdAndStartTimeBeforeAndEndTimeAfter(
            Long bookerId, LocalDateTime starTime, LocalDateTime endTime);

    List<BookingEntity> findByUserEntityIdAndStartTimeAfter(Long bookerId, LocalDateTime starTime);

    List<BookingEntity> findByItemEntityUserEntityId(Long ownerId);

    List<BookingEntity> findByItemEntityUserEntityIdAndStatusEquals(Long ownerId, Status status);

    List<BookingEntity> findByItemEntityUserEntityIdAndEndTimeBefore(Long ownerId, LocalDateTime endTime);

    List<BookingEntity> findByItemEntityUserEntityIdAndStartTimeBeforeAndEndTimeAfter(
            Long ownerId, LocalDateTime starTime, LocalDateTime endTime);

    List<BookingEntity> findByItemEntityUserEntityIdAndStartTimeAfter(Long ownerId, LocalDateTime startTime);

    List<BookingEntity> findByItemEntityId(Long itemId);

    List<BookingEntity> findByItemEntityIdAndUserEntityId(Long itemId, Long bookerId);
}
