package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingServiceTest {

    final PageRequest pageRequest = PageRequest.of(0, 20);

    @InjectMocks
    BookingService bookingService;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @Test
    public void addBooking_givenValidData_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.findById(3L)).thenReturn(Optional.ofNullable(booker));
        Item itemEntity = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE)
                .user(owner).build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemEntity));
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).itemId(2L).build();
        Booking booking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.WAITING).endTime(LocalDateTime.now().plusHours(2)).item(itemEntity)
                .user(booker).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto retrievedBooking = bookingService.addBooking(3L, bookingDto);

        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.WAITING, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void addBooking_givenUnavailableItem_thenExpectBadRequest() {
        Optional<User> owner = Optional.of(User.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<User> booker = Optional.of(User.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        when(userRepository.findById(anyLong())).thenReturn(booker);
        Optional<Item> itemEntity = Optional.of(Item.builder().id(2L).name("item").description("desc")
                .available(Boolean.FALSE).user(owner.get()).build());
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).itemId(2L).build();
        when(itemRepository.findById(anyLong())).thenReturn(itemEntity);
        try {
            bookingService.addBooking(3L, bookingDto);
        } catch (Exception exception) {
            assertEquals(BadRequestException.class, exception.getClass());
            assertEquals("Вещь не доступна!", exception.getMessage());
        }
    }

    @Test
    public void addBooking_givenOwnerWantsToBookOwnItem_thenExpectNotFound() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(owner));
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).itemId(2L).build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(1L, bookingDto));
    }

    @Test
    public void addBooking_givenStartTimeEqualsEndTime_thenBadRequest() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booker));
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        LocalDateTime localDateTime = LocalDateTime.of(2023, 6, 20, 3, 30, 30);
        BookingDto bookingDto = BookingDto.builder().start(localDateTime)
                .end(localDateTime).itemId(2L).build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        assertThrows(BadRequestException.class, () -> bookingService.addBooking(3L, bookingDto));
    }

    @Test
    public void addBooking_givenStartTimeAfterEndTime_thenBadRequest() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booker));
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(1)).itemId(2L).build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        assertThrows(BadRequestException.class, () -> bookingService.addBooking(3L, bookingDto));
    }

    @Test
    public void updateBooking_givenStatusApproved_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.existsById(anyLong())).thenReturn(true);
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE)
                .user(owner).build();
        Booking booking = Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        Booking updateBooking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.APPROVED).endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(updateBooking);

        BookingDto retrievedBooking = bookingService.updateBooking(1L, true, 1L);

        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.APPROVED, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void updateBooking_givenStatusRejected_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.existsById(anyLong())).thenReturn(true);
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE)
                .user(owner).build();
        Booking booking = Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        Booking updateBooking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.REJECTED).endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(updateBooking);

        BookingDto retrievedBooking = bookingService.updateBooking(1L, false, 1L);

        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.REJECTED, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void updateBooking_givenInvalidOwnerId_expectSuccess() {
        when(userRepository.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(1L, true, 1L));
    }

    @Test
    public void updateBooking_givenOwnerIsIncorrect_expectNotFound() {
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        User user = User.builder().id(4L).name("user").email("user@mail.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booker));
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE)
                .user(user).build();
        Booking booking = Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.updateBooking(1L, true, 1L));
    }

    @Test
    public void updateBooking_givenStatusNotWaiting_expectBadRequest() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        when(userRepository.existsById(anyLong())).thenReturn(true);
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        Booking booking = Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.APPROVED)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(1L, true, 1L));
    }

    @Test
    public void getBooking_givenValidData_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        when(userRepository.existsById(1L)).thenReturn(true);
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        Booking booking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item).user(booker).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));

        BookingDto retrievedBooking = bookingService.getBooking(1L, 1L);

        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.WAITING, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void getBooking_givenInvalidUserId_expectNotFound() {
        when(userRepository.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(1L, 1L));
    }

    @Test
    public void getBooking_givenIdNotOwnerNorBooker_expectNotFound() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User user = User.builder().id(99L).name("user").email("user@mail.ru").build();
        when(userRepository.findById(99L)).thenReturn(Optional.ofNullable(user));
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        Booking booking = Booking.builder().id(1L).startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item).user(booker).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(99L, 1L));
    }

    @Test
    public void getBookingsByBookerId_givenStateIsAll_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookings = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(3L)).thenReturn(true);
        when(bookingRepository.findByUserIdOrderByStartTimeDesc(3L, pageRequest)).thenReturn(bookings);

        List<BookingDto> bookingDtoList = bookingService.getBookingsByBookerId(3L, "ALL", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByBookerId_givenStateIsWaiting_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookings = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(3L)).thenReturn(true);
        when(bookingRepository
                .findByUserIdAndStatusEqualsOrderByStartTimeDesc(3L, Status.WAITING, pageRequest))
                .thenReturn(bookings);

        List<BookingDto> bookingDtoList = bookingService
                .getBookingsByBookerId(3L, "WAITING", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByBookerId_givenStateIsREJECTED_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookings = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.REJECTED)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(3L)).thenReturn(true);
        when(bookingRepository
                .findByUserIdAndStatusEqualsOrderByStartTimeDesc(3L, Status.REJECTED, pageRequest))
                .thenReturn(bookings);

        List<BookingDto> bookingDtoList = bookingService
                .getBookingsByBookerId(3L, "REJECTED", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByBookerId_givenInvalidBookerId_expectNotFound() {
        when(userRepository.existsById(3L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByBookerId(3L, "ALL", 0, 20));
    }

    @Test
    public void getBookingsByOwnerId_givenStateIsAll_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookingList = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemUserIdOrderByStartTimeDesc(1L, pageRequest)).thenReturn(bookingList);

        List<BookingDto> bookingDtoList = bookingService.getBookingsByOwnerId(1L, "ALL", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByOwnerId_givenStateIsWAITING_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookingList = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository
                .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(1L, Status.WAITING, pageRequest))
                .thenReturn(bookingList);

        List<BookingDto> bookingDtoList = bookingService
                .getBookingsByOwnerId(1L, "WAITING", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByOwnerId_givenStateIsREJECTED_expectSuccess() {
        User owner = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();
        Item item = Item.builder().id(2L).name("item").description("desc").available(Boolean.TRUE).user(owner).build();
        List<Booking> bookingList = List.of(Booking.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.REJECTED)
                .endTime(LocalDateTime.now().plusHours(2)).item(item)
                .user(booker).build());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository
                .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(1L, Status.REJECTED, pageRequest))
                .thenReturn(bookingList);

        List<BookingDto> bookingDtoList = bookingService
                .getBookingsByOwnerId(1L, "REJECTED", 0, 20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByOwnerId_givenInvalidOwnerId_expectNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByOwnerId(1L, "ALL", 0, 20));
    }
}
