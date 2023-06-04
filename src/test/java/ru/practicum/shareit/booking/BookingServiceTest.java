package ru.practicum.shareit.booking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

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
        Optional<UserEntity> owner = Optional.of(UserEntity.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<UserEntity> booker = Optional.of(UserEntity.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(booker);
        Optional<ItemEntity> itemEntity = Optional.of(ItemEntity.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).userEntity(owner.get()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(itemEntity);
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).itemId(2L).build();
        BookingEntity bookingEntity = BookingEntity.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.WAITING).endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build();
        when(bookingRepository.save(Mockito.any(BookingEntity.class))).thenReturn(bookingEntity);
        BookingDto retrievedBooking = bookingService.addBooking(3L, bookingDto);
        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.WAITING, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void updateBooking() {
        Optional<UserEntity> owner = Optional.of(UserEntity.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<UserEntity> booker = Optional.of(UserEntity.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(booker);
        Optional<ItemEntity> itemEntity = Optional.of(ItemEntity.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).userEntity(owner.get()).build());
        Optional<BookingEntity> optionalBookingEntity = Optional.of(BookingEntity.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build());
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(optionalBookingEntity);
        BookingEntity bookingEntity = BookingEntity.builder().id(1L).startTime(LocalDateTime.now().plusHours(1))
                .status(Status.APPROVED).endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build();
        when(bookingRepository.save(Mockito.any(BookingEntity.class))).thenReturn(bookingEntity);
        BookingDto retrievedBooking = bookingService.updateBooking(1L, true, 1L);
        assertNotNull(retrievedBooking);
        assertEquals(1, retrievedBooking.getId());
        assertEquals(Status.APPROVED, retrievedBooking.getStatus());
        assertEquals(3, retrievedBooking.getBooker().getId());
        assertEquals(2, retrievedBooking.getItem().getId());
    }

    @Test
    public void getBooking() {
        Optional<UserEntity> owner = Optional.of(UserEntity.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        when(userRepository.findById(1L)).thenReturn(owner);
        Optional<UserEntity> booker = Optional.of(UserEntity.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<ItemEntity> itemEntity = Optional.of(ItemEntity.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).userEntity(owner.get()).build());
        Optional<BookingEntity> optionalBookingEntity = Optional.of(BookingEntity.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build());
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
        Optional<UserEntity> owner = Optional.of(UserEntity.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<UserEntity> booker = Optional.of(UserEntity.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<ItemEntity> itemEntity = Optional.of(ItemEntity.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).userEntity(owner.get()).build());
        List<BookingEntity> bookingEntities = List.of(BookingEntity.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build());
        when(userRepository.findById(3L)).thenReturn(booker);
        when(bookingRepository.findByUserEntityId(3L)).thenReturn(bookingEntities);
        List<BookingDto> bookingDtoList = bookingService.getBookingsByBookerId(3L, "ALL");
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }

    @Test
    public void getBookingsByOwnerId() {
        Optional<UserEntity> owner = Optional.of(UserEntity.builder().id(1L).name("owner")
                .email("owner@mail.ru").build());
        Optional<UserEntity> booker = Optional.of(UserEntity.builder().id(3L).name("booker")
                .email("booker@mail.ru").build());
        Optional<ItemEntity> itemEntity = Optional.of(ItemEntity.builder().id(2L).name("item").description("desc")
                .available(Boolean.TRUE).userEntity(owner.get()).build());
        List<BookingEntity> bookingEntities = List.of(BookingEntity.builder().id(1L)
                .startTime(LocalDateTime.now().plusHours(1)).status(Status.WAITING)
                .endTime(LocalDateTime.now().plusHours(2)).itemEntity(itemEntity.get())
                .userEntity(booker.get()).build());
        when(userRepository.findById(1L)).thenReturn(owner);
        when(bookingRepository.findByItemEntityUserEntityId(1L)).thenReturn(bookingEntities);
        List<BookingDto> bookingDtoList = bookingService.getBookingsByOwnerId(1L, "ALL");
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(1, bookingDtoList.get(0).getId());
    }
}
