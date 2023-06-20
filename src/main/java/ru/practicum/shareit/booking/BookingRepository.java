package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByStartTimeDesc(Long bookerId, PageRequest pageRequest);

    List<Booking> findByUserIdAndStatusEqualsOrderByStartTimeDesc(
            Long bookerId, Status status, PageRequest pageRequest);

    List<Booking> findByUserIdAndEndTimeBeforeOrderByStartTimeDesc(
            Long bookerId, LocalDateTime endTime, PageRequest pageRequest);

    List<Booking> findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long bookerId, LocalDateTime starTime, LocalDateTime endTime, PageRequest pageRequest);

    List<Booking> findByUserIdAndStartTimeAfterOrderByStartTimeDesc(
            Long bookerId, LocalDateTime starTime, PageRequest pageRequest);

    List<Booking> findByItemUserIdOrderByStartTimeDesc(Long ownerId, PageRequest pageRequest);

    List<Booking> findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(Long ownerId, Status status, PageRequest pageRequest);

    List<Booking> findByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(Long ownerId, LocalDateTime endTime, PageRequest pageRequest);

    List<Booking> findByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long ownerId, LocalDateTime starTime, LocalDateTime endTime, PageRequest pageRequest);

    List<Booking> findByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(
            Long ownerId, LocalDateTime startTime, PageRequest pageRequest);

    List<Booking> findByItemIdOrderByStartTimeDesc(Long itemId);

    List<Booking> findByItemIdAndUserIdOrderByStartTimeDesc(Long itemId, Long bookerId);

    Optional<Booking> findFirstByItemIdAndAndStartTimeBeforeAndStatusEqualsOrderByStartTimeDesc(
            Long itemId, LocalDateTime startTime, Status status);

    Optional<Booking> findFirstByItemIdAndAndStartTimeAfterAndStatusEqualsOrderByStartTimeAsc(
            Long itemId, LocalDateTime startTime, Status status);
}
