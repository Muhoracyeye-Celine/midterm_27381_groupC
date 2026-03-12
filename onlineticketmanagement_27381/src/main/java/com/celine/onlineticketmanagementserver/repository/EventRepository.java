package com.celine.onlineticketmanagementserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.EventStatus;
import com.celine.onlineticketmanagementserver.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find events by status
    List<Event> findByStatus(EventStatus status);
    
    // Find events by venue ID
    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId")
    List<Event> findByVenueId(@Param("venueId") Long venueId);
    
    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.eventDate > :currentDate AND e.status = :status")
    List<Event> findUpcomingEvents(
        @Param("currentDate") LocalDateTime currentDate,
        @Param("status") EventStatus status
    );
    
    // Find events within date range
    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate")
    List<Event> findEventsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find event with venue (eager fetch)
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.venue WHERE e.id = :id")
    Optional<Event> findByIdWithVenue(@Param("id") Long id);
    
    // Find event with tickets (eager fetch)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.id = :id")
    Optional<Event> findByIdWithTickets(@Param("id") Long id);
    
    // Find events with full details
    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN FETCH e.venue " +
           "LEFT JOIN FETCH e.tickets")
    List<Event> findAllWithDetails();
    
    // Search events by title
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitle(@Param("keyword") String keyword);
    
    // Count available tickets for an event
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'AVAILABLE'")
    Long countAvailableTickets(@Param("eventId") Long eventId);

    // Comprehensive search across multiple columns
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.venue v WHERE " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(e.status AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Event> searchAll(@Param("searchTerm") String searchTerm);

    // Search by specific column
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.venue v WHERE " +
            "CASE :columnName " +
            "WHEN 'title' THEN LOWER(e.title) " +
            "WHEN 'description' THEN LOWER(e.description) " +
            "WHEN 'category' THEN LOWER(e.category) " +
            "WHEN 'venueName' THEN LOWER(v.name) " +
            "WHEN 'status' THEN LOWER(CAST(e.status AS string)) " +
            "ELSE LOWER(e.title) " +
            "END LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Event> searchByColumn(@Param("searchTerm") String searchTerm, @Param("columnName") String columnName);
}