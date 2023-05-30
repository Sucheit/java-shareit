package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.user.model.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void givenNewBooking_whenSave_thenSuccess() {
        ItemEntity itemEntity = ItemEntity.builder().name("name").description("desc").build();
        UserEntity userEntity = UserEntity.builder().name("username").email("username@mail.ru").build();
        entityManager.persist(itemEntity);
        entityManager.persist(userEntity);
        BookingEntity bookingEntity = BookingEntity.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .itemEntity(itemEntity).userEntity(userEntity).build();
        BookingEntity insertedBookingEntity = bookingRepository.save(bookingEntity);
        assertThat(entityManager.find(BookingEntity.class, insertedBookingEntity.getId())).isEqualTo(bookingEntity);
    }

    @Test
    public void givenBookingCreated_whenUpdate_thenSuccess() {
        BookingEntity bookingEntity = BookingEntity.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2)).build();
        entityManager.persist(bookingEntity);
        bookingEntity.setStatus(Status.APPROVED);
        bookingRepository.save(bookingEntity);
        assertThat(entityManager.find(BookingEntity.class, bookingEntity.getId()).getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    public void givenBookingCreated_whenFindById_thenSuccess() {
        BookingEntity bookingEntity = BookingEntity.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2)).build();
        entityManager.persist(bookingEntity);
        Optional<BookingEntity> retrievedBooking = bookingRepository.findById(bookingEntity.getId());
        assertThat(retrievedBooking).contains(bookingEntity);
    }

    @Test
    public void givenBookingCreated_whenDelete_thenSuccess() {
        BookingEntity bookingEntity = BookingEntity.builder()
                .status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();
        entityManager.persist(bookingEntity);
        bookingRepository.delete(bookingEntity);
        assertThat(entityManager.find(BookingEntity.class, bookingEntity.getId())).isNull();
    }

    @Test
    public void givenBookingCreated_whenFindByUserEntityId_thenSuccess() {
        ItemEntity item = ItemEntity.builder().name("name").description("desc").build();
        UserEntity user = UserEntity.builder().name("username").email("username@mail.ru").build();
        BookingEntity booking = BookingEntity.builder()
                .status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .itemEntity(item)
                .userEntity(user)
                .build();
        entityManager.persist(item);
        entityManager.persist(user);
        entityManager.persist(booking);
        List<BookingEntity> bookings = bookingRepository.findByUserEntityId(user.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityUserEntityId_thenSuccess() {
        UserEntity owner = UserEntity.builder().name("owner").email("owner@mail.ru").build();
        ItemEntity item = ItemEntity.builder().name("name").description("desc").userEntity(owner).build();
        UserEntity booker = UserEntity.builder().name("booker").email("booker@mail.ru").build();
        BookingEntity booking = BookingEntity.builder()
                .status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .itemEntity(item)
                .userEntity(booker)
                .build();
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.persist(booker);
        entityManager.persist(booking);
        List<BookingEntity> bookings = bookingRepository.findByItemEntityUserEntityId(owner.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityId_thenSuccess() {
        UserEntity owner = UserEntity.builder().name("owner").email("owner@mail.ru").build();
        ItemEntity item = ItemEntity.builder().name("name").description("desc").userEntity(owner).build();
        UserEntity booker = UserEntity.builder().name("booker").email("booker@mail.ru").build();
        BookingEntity booking = BookingEntity.builder()
                .status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .itemEntity(item)
                .userEntity(booker)
                .build();
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.persist(booker);
        entityManager.persist(booking);
        List<BookingEntity> bookings = bookingRepository.findByItemEntityId(item.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityIdAndUserEntityId_thenSuccess() {
        UserEntity owner = UserEntity.builder().name("owner").email("owner@mail.ru").build();
        ItemEntity item = ItemEntity.builder().name("name").description("desc").userEntity(owner).build();
        UserEntity booker = UserEntity.builder().name("booker").email("booker@mail.ru").build();
        BookingEntity booking = BookingEntity.builder()
                .status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .itemEntity(item)
                .userEntity(booker)
                .build();
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.persist(booker);
        entityManager.persist(booking);
        List<BookingEntity> bookings = bookingRepository.findByItemEntityIdAndUserEntityId(item.getId(), booker.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
    }
}
