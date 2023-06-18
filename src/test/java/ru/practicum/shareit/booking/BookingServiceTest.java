package ru.practicum.shareit.booking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Test
    public void addBooking() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(booker);
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).user(owner.get()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(itemEntity);
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).itemId(2L).build();
        Booking booking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.WAITING).endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build();
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);
        BookingDto retrievedBooking = bookingService.addBooking(3L, bookingDto);
        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.WAITING, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void updateBooking() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(booker);
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).user(owner.get()).build());
        Optional<Booking> optionalBookingEntity = Optional.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build());
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(optionalBookingEntity);
        Booking booking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.APPROVED).endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build();
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);
        BookingDto retrievedBooking = bookingService.updateBooking(1L, true, 1L);
        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.APPROVED, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void getBooking() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        when(userRepository.findById(1L)).thenReturn(owner);
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).user(owner.get()).build());
        Optional<Booking> optionalBookingEntity = Optional.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build());
        when(bookingRepository.findById(1L)).thenReturn(optionalBookingEntity);
        BookingDto retrievedBooking = bookingService.getBooking(1L, 1L);
        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.WAITING, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void getBookingsByBookerId() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).user(owner.get()).build());
        List<Booking> bookingEntities = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build());
        when(userRepository.findById(3L)).thenReturn(booker);
        when(bookingRepository.findByUserIdOrderByStartTimeDesc(3L)).thenReturn(bookingEntities);
        List<BookingDto> bookingDtoList = bookingService.getBookingsByBookerId(3L, "ALL");
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByOwnerId() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).user(owner.get()).build());
        List<Booking> bookingEntities = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(itemEntity.get())
                .user(booker.get()).build());
        when(userRepository.findById(1L)).thenReturn(owner);
        when(bookingRepository.findByItemUserIdOrderByStartTimeDesc(1L)).thenReturn(bookingEntities);
        List<BookingDto> bookingDtoList = bookingService.getBookingsByOwnerId(1L, "ALL");
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }
}
