package com.celine.onlineticketmanagementserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.PaymentStatus;
import com.celine.onlineticketmanagementserver.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find all bookings by person ID
    @Query("SELECT b FROM Booking b WHERE b.bookedBy.id = :personId")
    List<Booking> findByPersonId(@Param("personId") Long personId);
    
    // Find bookings by payment status
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);
    
    // Find bookings within a date range
    @Query("SELECT b FROM Booking b WHERE b.bookedAt BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsBetweenDates(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find booking with tickets (eager fetch to avoid N+1)
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.tickets t " +
           "LEFT JOIN FETCH t.event e " +
           "LEFT JOIN FETCH e.venue " +
           "LEFT JOIN FETCH b.bookedBy " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithTickets(@Param("id") Long id);
    
    // Find bookings with person details (eager fetch)
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.bookedBy WHERE b.id = :id")
    Optional<Booking> findByIdWithPerson(@Param("id") Long id);
    
    // Get all bookings with full details (use with caution - can be heavy)
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.bookedBy " +
           "LEFT JOIN FETCH b.tickets")
    List<Booking> findAllWithDetails();
    
    // Count bookings by person
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookedBy.id = :personId")
    Long countByPersonId(@Param("personId") Long personId);

    // Comprehensive search across multiple columns
    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookedBy p " +
            "LEFT JOIN b.tickets t " +
            "LEFT JOIN t.event e " +
            "LEFT JOIN e.venue v WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CAST(b.paymentStatus AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(b.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<Booking> searchAll(@Param("searchTerm") String searchTerm);

    // Search by specific column
    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN b.bookedBy p " +
            "LEFT JOIN b.tickets t " +
            "LEFT JOIN t.event e " +
            "LEFT JOIN e.venue v WHERE " +
            "CASE :columnName " +
            "WHEN 'id' THEN CAST(b.id AS string) " +
            "WHEN 'personName' THEN LOWER(CONCAT(p.firstName, ' ', p.lastName)) " +
            "WHEN 'eventName' THEN LOWER(e.title) " +
            "WHEN 'venueName' THEN LOWER(v.name) " +
            "WHEN 'paymentStatus' THEN LOWER(CAST(b.paymentStatus AS string)) " +
            "ELSE CAST(b.id AS string) " +
            "END LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Booking> searchByColumn(@Param("searchTerm") String searchTerm, @Param("columnName") String columnName);
}