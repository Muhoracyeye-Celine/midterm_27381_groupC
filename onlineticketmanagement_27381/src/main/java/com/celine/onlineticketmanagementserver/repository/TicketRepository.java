package com.celine.onlineticketmanagementserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.TicketStatus;
import com.celine.onlineticketmanagementserver.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by event ID
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId")
    List<Ticket> findByEventId(@Param("eventId") Long eventId);

    // Find tickets by booking ID
    @Query("SELECT t FROM Ticket t WHERE t.booking.id = :bookingId")
    List<Ticket> findByBookingId(@Param("bookingId") Long bookingId);

    // Find tickets by status
    List<Ticket> findByStatus(TicketStatus status);

    // Find available tickets for an event
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    List<Ticket> findAvailableTicketsByEventId(
            @Param("eventId") Long eventId,
            @Param("status") TicketStatus status
    );

    // Find ticket with event details (eager fetch)
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.event WHERE t.id = :id")
    Optional<Ticket> findByIdWithEvent(@Param("id") Long id);

    // Find ticket with booking details (eager fetch)
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.booking WHERE t.id = :id")
    Optional<Ticket> findByIdWithBooking(@Param("id") Long id);

    // Find ticket with full details
    @Query("SELECT t FROM Ticket t "
            + "LEFT JOIN FETCH t.event "
            + "LEFT JOIN FETCH t.booking "
            + "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

    // Check if seat number exists for an event
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END "
            + "FROM Ticket t WHERE t.event.id = :eventId AND t.seatNumber = :seatNumber")
    boolean existsBySeatNumberAndEventId(
            @Param("seatNumber") String seatNumber,
            @Param("eventId") Long eventId
    );

    // Count tickets by status for an event
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    Long countByEventIdAndStatus(
            @Param("eventId") Long eventId,
            @Param("status") TicketStatus status
    );

    // Count all tickets for an event
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    // Find tickets by person (through booking)
    @Query("SELECT t FROM Ticket t "
            + "LEFT JOIN FETCH t.event e "
            + "LEFT JOIN FETCH e.venue "
            + "WHERE t.booking.bookedBy.id = :personId")
    List<Ticket> findByPersonId(@Param("personId") Long personId);

    // Comprehensive search across multiple columns
    @Query("SELECT DISTINCT t FROM Ticket t " +
            "LEFT JOIN t.event e " +
            "LEFT JOIN e.venue v " +
            "LEFT JOIN t.booking b WHERE " +
            "LOWER(t.seatNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(t.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<Ticket> searchAll(@Param("searchTerm") String searchTerm);

    // Search by specific column
    @Query("SELECT DISTINCT t FROM Ticket t " +
            "LEFT JOIN t.event e " +
            "LEFT JOIN e.venue v WHERE " +
            "CASE :columnName " +
            "WHEN 'id' THEN CAST(t.id AS string) " +
            "WHEN 'seatNumber' THEN LOWER(t.seatNumber) " +
            "WHEN 'eventName' THEN LOWER(e.title) " +
            "WHEN 'venueName' THEN LOWER(v.name) " +
            "WHEN 'status' THEN LOWER(CAST(t.status AS string)) " +
            "ELSE CAST(t.id AS string) " +
            "END LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> searchByColumn(@Param("searchTerm") String searchTerm, @Param("columnName") String columnName);
}
