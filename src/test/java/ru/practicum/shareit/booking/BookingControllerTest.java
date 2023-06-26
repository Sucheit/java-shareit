package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void addBooking_whenInvokedWithValidBooking_thenExpectOk() {
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1)).itemId(1L)
                .end(LocalDateTime.now().plusHours(2)).build();
        BookingDto retrievedBooking = BookingDto.builder().id(1L).status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(1)).end(LocalDateTime.now().plusHours(2)).build();
        when(bookingService.addBooking(1L, bookingDto)).thenReturn(retrievedBooking);
        String response = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).addBooking(1L, bookingDto);
        assertEquals(response, objectMapper.writeValueAsString(retrievedBooking));
    }

    @Test
    @SneakyThrows
    public void addBooking_whenInvokedWithInvalidItemId_thenExpectNotFound() {
        BookingDto bookingDto = BookingDto.builder().start(LocalDateTime.now().plusHours(1)).itemId(1L)
                .end(LocalDateTime.now().plusHours(2)).build();
        when(bookingService.addBooking(1L, bookingDto)).thenThrow(NotFoundException.class);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).addBooking(1L, bookingDto);
    }

    @Test
    @SneakyThrows
    public void updateBooking_whenInvokedWithValidBooking_thenExpectOk() {
        BookingDto retrievedBooking = BookingDto.builder().id(1L).status(Status.APPROVED)
                .start(LocalDateTime.now().plusHours(1)).end(LocalDateTime.now().plusHours(2)).build();
        when(bookingService.updateBooking(1L, true, 1L)).thenReturn(retrievedBooking);
        String response = mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).updateBooking(1L, true, 1L);
        assertEquals(response, objectMapper.writeValueAsString(retrievedBooking));
    }

    @Test
    @SneakyThrows
    public void updateBooking_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(bookingService.updateBooking(1L, true, 1L)).thenThrow(NotFoundException.class);
        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).updateBooking(1L, true, 1L);
    }

    @Test
    @SneakyThrows
    public void getBooking_whenInvokedWithValidId_thenExpectOk() {
        BookingDto retrievedBooking = BookingDto.builder().id(1L).status(Status.APPROVED)
                .start(LocalDateTime.now().plusHours(1)).end(LocalDateTime.now().plusHours(2)).build();
        when(bookingService.getBooking(1L, 1L)).thenReturn(retrievedBooking);
        String response = mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBooking(1L, 1L);
        assertEquals(response, objectMapper.writeValueAsString(retrievedBooking));
    }

    @Test
    @SneakyThrows
    public void getBooking_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(bookingService.getBooking(1L, 1L)).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBooking(1L, 1L);
    }

    @Test
    @SneakyThrows
    public void getBookingsByBookerId_whenInvokedWithValidId_thenExpectOk() {
        List<BookingDto> retrievedBookings = List.of(BookingDto.builder().id(1L).status(Status.APPROVED)
                .start(LocalDateTime.now().plusHours(1)).end(LocalDateTime.now().plusHours(2)).build());
        when(bookingService.getBookingsByBookerId(1L, "ALL", 0, 20)).thenReturn(retrievedBookings);
        String response = mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBookingsByBookerId(1L, "ALL", 0, 20);
        assertEquals(response, objectMapper.writeValueAsString(retrievedBookings));
    }

    @Test
    @SneakyThrows
    public void getBookingsByBookerId_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(bookingService.getBookingsByBookerId(1L, "ALL", 0, 20)).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBookingsByBookerId(1L, "ALL", 0, 20);
    }

    @Test
    @SneakyThrows
    public void getBookingsByOwner_whenInvokedWithValidId_thenExpectOk() {
        List<BookingDto> retrievedBookings = List.of(BookingDto.builder().id(1L).status(Status.APPROVED)
                .start(LocalDateTime.now().plusHours(1)).end(LocalDateTime.now().plusHours(2)).build());
        when(bookingService.getBookingsByOwnerId(1L, "ALL", 0, 20)).thenReturn(retrievedBookings);
        String response = mockMvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBookingsByOwnerId(1L, "ALL", 0, 20);
        assertEquals(response, objectMapper.writeValueAsString(retrievedBookings));
    }

    @Test
    @SneakyThrows
    public void getBookingsByOwner_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(bookingService.getBookingsByOwnerId(1L, "ALL", 0, 20)).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(bookingService, atLeast(1)).getBookingsByOwnerId(1L, "ALL", 0, 20);
    }
}
