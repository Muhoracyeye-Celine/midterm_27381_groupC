package com.celine.onlineticketmanagementserver.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.PaymentStatus;
import com.celine.onlineticketmanagementserver.enums.TicketStatus;
import com.celine.onlineticketmanagementserver.exception.ResourceNotFoundException;
import com.celine.onlineticketmanagementserver.exception.ValidationException;
import com.celine.onlineticketmanagementserver.model.Booking;
import com.celine.onlineticketmanagementserver.model.Person;
import com.celine.onlineticketmanagementserver.model.Ticket;
import com.celine.onlineticketmanagementserver.repository.BookingRepository;
import com.celine.onlineticketmanagementserver.repository.PersonRepository;
import com.celine.onlineticketmanagementserver.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PersonRepository personRepository;
    private final TicketRepository ticketRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllWithDetails();
    }

    public List<Booking> getAllBookingsWithDetails() {
        return bookingRepository.findAllWithDetails();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findByIdWithTickets(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public Booking getBookingByIdWithTickets(Long id) {
        return bookingRepository.findByIdWithTickets(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public Booking getBookingByIdWithPerson(Long id) {
        return bookingRepository.findByIdWithPerson(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByPersonId(Long personId) {
        return bookingRepository.findByPersonId(personId);
    }

    public List<Booking> getBookingsByPaymentStatus(PaymentStatus paymentStatus) {
        return bookingRepository.findByPaymentStatus(paymentStatus);
    }

    public List<Booking> getBookingsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findBookingsBetweenDates(startDate, endDate);
    }

    public Long countBookingsByPersonId(Long personId) {
        return bookingRepository.countByPersonId(personId);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        if (booking.getBookedBy() == null || booking.getBookedBy().getId() == null) {
            throw new ValidationException("Person ID is required");
        }
        if (booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Total amount must be greater than 0");
        }

        Person person = personRepository.findById(booking.getBookedBy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + booking.getBookedBy().getId()));

        Booking newBooking = new Booking();
        newBooking.setBookedBy(person);
        newBooking.setTotalAmount(booking.getTotalAmount());
        newBooking.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus() : PaymentStatus.PENDING);

        newBooking = bookingRepository.save(newBooking);

        Set<Long> unavailableTickets = new HashSet<>();
        List<Ticket> ticketsToUpdate = new ArrayList<>();

        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            for (Ticket ticketReference : booking.getTickets()) {
                Ticket ticket = ticketRepository.findById(ticketReference.getId()).orElse(null);
                if (ticket == null || ticket.getStatus() != TicketStatus.AVAILABLE || ticket.getBooking() != null) {
                    unavailableTickets.add(ticketReference.getId());
                } else {
                    ticket.setBooking(newBooking);
                    ticket.setStatus(TicketStatus.SOLD);
                    ticketsToUpdate.add(ticket);
                }
            }
            if (!ticketsToUpdate.isEmpty()) {
                ticketRepository.saveAll(ticketsToUpdate);
                ticketRepository.flush();
            }
        }

        if (!unavailableTickets.isEmpty()) {
            log.warn("Some tickets were unavailable during booking creation: {}", unavailableTickets);
        }

        return bookingRepository.findByIdWithTickets(newBooking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found after creation"));
    }

    @Transactional
    public Booking updateBooking(Long id, Booking bookingUpdate) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (bookingUpdate.getTotalAmount() != null) {
            if (bookingUpdate.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Total amount must be greater than 0");
            }
            booking.setTotalAmount(bookingUpdate.getTotalAmount());
        }
        if (bookingUpdate.getPaymentStatus() != null) {
            booking.setPaymentStatus(bookingUpdate.getPaymentStatus());
        }
        if (bookingUpdate.getBookedBy() != null && bookingUpdate.getBookedBy().getId() != null) {
            Person person = personRepository.findById(bookingUpdate.getBookedBy().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + bookingUpdate.getBookedBy().getId()));
            booking.setBookedBy(person);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setPaymentStatus(paymentStatus);
        return bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public List<Booking> searchAll(String searchTerm) {
        return bookingRepository.searchAll(searchTerm);
    }

    public List<Booking> searchByColumn(String searchTerm, String columnName) {
        return bookingRepository.searchByColumn(searchTerm, columnName);
    }
}
