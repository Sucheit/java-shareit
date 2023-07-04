package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRepositoryTest {

    final PageRequest pageRequest = PageRequest.of(0, 20);
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    BookingRepository bookingRepository;

    @Test
    public void givenNewBooking_whenSave_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(user).build();
        Booking insertedBooking = bookingRepository.save(booking);
        assertThat(entityManager.find(Booking.class, insertedBooking.getId())).isEqualTo(booking);
    }

    @Test
    public void givenBookingCreated_whenUpdate_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(user).build();
        entityManager.persist(booking);
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
        assertThat(entityManager.find(Booking.class, booking.getId()).getStatus())
                .isEqualTo(Status.APPROVED);
    }

    @Test
    public void givenBookingCreated_whenFindById_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(user).build();
        entityManager.persist(booking);
        Optional<Booking> retrievedBooking = bookingRepository.findById(booking.getId());
        assertThat(retrievedBooking).contains(booking);
    }

    @Test
    public void givenBookingCreated_whenDelete_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(user).build();
        entityManager.persist(booking);
        bookingRepository.delete(booking);
        assertThat(entityManager.find(Booking.class, booking.getId())).isNull();
    }

    @Test
    public void givenBookingCreated_whenFindByUserEntityId_thenSuccess() {
        User user = User.builder().name("username").email("email@mail.com").build();
        entityManager.persist(user);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(user).build();
        entityManager.persist(item);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(user).build();
        entityManager.persist(booking);
        List<Booking> bookings = bookingRepository.findByUserIdOrderByStartTimeDesc(user.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityUserEntityId_thenSuccess() {
        User owner = User.builder().name("owner").email("owner@mail.com").build();
        entityManager.persist(owner);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(owner).build();
        entityManager.persist(item);
        User booker = User.builder().name("booker").email("booker@mail.com").build();
        entityManager.persist(booker);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(booker).build();
        entityManager.persist(booking);
        List<Booking> bookings = bookingRepository.findByItemUserIdOrderByStartTimeDesc(owner.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
        assertEquals(item, bookings.get(0).getItem());
        assertEquals(owner, bookings.get(0).getItem().getUser());
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityId_thenSuccess() {
        User owner = User.builder().name("owner").email("owner@mail.com").build();
        entityManager.persist(owner);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(owner).build();
        entityManager.persist(item);
        User booker = User.builder().name("booker").email("booker@mail.com").build();
        entityManager.persist(booker);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(booker).build();
        entityManager.persist(booking);
        List<Booking> bookings = bookingRepository.findByItemIdOrderByStartTimeDesc(item.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
        assertEquals(item, bookings.get(0).getItem());
        assertEquals(owner, bookings.get(0).getItem().getUser());
    }

    @Test
    public void givenBookingCreated_whenFindByItemEntityIdAndUserEntityId_thenSuccess() {
        User owner = User.builder().name("owner").email("owner@mail.com").build();
        entityManager.persist(owner);
        Item item = Item.builder().name("name").description("desc")
                .available(Boolean.TRUE).user(owner).build();
        entityManager.persist(item);
        User booker = User.builder().name("booker").email("booker@mail.com").build();
        entityManager.persist(booker);
        Booking booking = Booking.builder().status(Status.WAITING)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(2))
                .item(item).user(booker).build();
        entityManager.persist(booking);
        List<Booking> bookings = bookingRepository
                .findByItemIdAndUserIdOrderByStartTimeDesc(item.getId(), booker.getId());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking, bookings.get(0));
        assertEquals(item, bookings.get(0).getItem());
        assertEquals(owner, bookings.get(0).getItem().getUser());
    }
}
