package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByStartTimeDesc(Long bookerId);

    List<Booking> findByUserIdAndStatusEqualsOrderByStartTimeDesc(Long bookerId, Status status);

    List<Booking> findByUserIdAndEndTimeBeforeOrderByStartTimeDesc(Long bookerId, LocalDateTime endTime);

    List<Booking> findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long bookerId, LocalDateTime starTime, LocalDateTime endTime);

    List<Booking> findByUserIdAndStartTimeAfterOrderByStartTimeDesc(Long bookerId, LocalDateTime starTime);

    List<Booking> findByItemUserIdOrderByStartTimeDesc(Long ownerId);

    List<Booking> findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(Long ownerId, Status status);

    List<Booking> findByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(Long ownerId, LocalDateTime endTime);

    List<Booking> findByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long ownerId, LocalDateTime starTime, LocalDateTime endTime);

    List<Booking> findByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(Long ownerId, LocalDateTime startTime);

    List<Booking> findByItemIdOrderByStartTimeDesc(Long itemId);

    List<Booking> findByItemIdAndUserIdOrderByStartTimeDesc(Long itemId, Long bookerId);
}
