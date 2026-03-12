package com.celine.onlineticketmanagementserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.model.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    // Check if venue exists by name
    Boolean existsByName(String name);
    
    // Find venue by name
    Optional<Venue> findByName(String name);
    
    // Find venue by name (case insensitive)
    Optional<Venue> findByNameIgnoreCase(String name);
    
    // Find venues by name containing (for search)
    List<Venue> findByNameContainingIgnoreCase(String name);
    
    // Find venues by address containing (for search)
    List<Venue> findByAddressContainingIgnoreCase(String address);
    
    // Find venues with capacity greater than or equal to
    List<Venue> findByCapacityGreaterThanEqual(Integer capacity);
    
    // Find venues with capacity between range
    List<Venue> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    // Find venues with capacity less than or equal to
    List<Venue> findByCapacityLessThanEqual(Integer capacity);
    
    // Get all venues ordered by name
    List<Venue> findAllByOrderByNameAsc();
    
    // Get all venues ordered by capacity descending
    List<Venue> findAllByOrderByCapacityDesc();
    
    // Count venues with capacity greater than
    Long countByCapacityGreaterThanEqual(Integer capacity);
    
    // Custom query to get venues with upcoming events
    @Query("SELECT DISTINCT v FROM Venue v JOIN v.events e WHERE e.eventDate > CURRENT_TIMESTAMP ORDER BY v.name")
    List<Venue> findVenuesWithUpcomingEvents();
    
    // Custom query to count events at a venue
    @Query("SELECT COUNT(e) FROM Event e WHERE e.venue.id = :venueId")
    Long countEventsByVenue(@Param("venueId") Long venueId);
    
    // Custom query to check if venue has any events
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.venue.id = :venueId")
    Boolean hasEvents(@Param("venueId") Long venueId);
    
    // Custom query to check if venue has upcoming events
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.venue.id = :venueId AND e.eventDate > CURRENT_TIMESTAMP")
    Boolean hasUpcomingEvents(@Param("venueId") Long venueId);
    
    // Custom query to get venue with its managers
    @Query("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.managers WHERE v.id = :venueId")
    Optional<Venue> findByIdWithManagers(@Param("venueId") Long venueId);
    
    // Custom query to find venues managed by a specific person
    @Query("SELECT v FROM Venue v JOIN v.managers m WHERE m.id = :personId")
    List<Venue> findVenuesManagedByPerson(@Param("personId") Long personId);
    
    // Custom query to count managers of a venue
    @Query("SELECT COUNT(m) FROM Venue v JOIN v.managers m WHERE v.id = :venueId")
    Long countManagersByVenue(@Param("venueId") Long venueId);
    
    // Custom query to check if a person is a manager of a venue
    @Query("SELECT COUNT(m) > 0 FROM Venue v JOIN v.managers m WHERE v.id = :venueId AND m.id = :personId")
    Boolean isPersonManagerOfVenue(@Param("venueId") Long venueId, @Param("personId") Long personId);

    // Comprehensive search across multiple columns
    @Query("SELECT DISTINCT v FROM Venue v WHERE " +
            "LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(v.capacity AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<Venue> searchAll(@Param("searchTerm") String searchTerm);

    // Search by specific column
    @Query("SELECT DISTINCT v FROM Venue v WHERE " +
            "CASE :columnName " +
            "WHEN 'name' THEN LOWER(v.name) " +
            "WHEN 'address' THEN LOWER(v.address) " +
            "WHEN 'capacity' THEN CAST(v.capacity AS string) " +
            "ELSE LOWER(v.name) " +
            "END LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Venue> searchByColumn(@Param("searchTerm") String searchTerm, @Param("columnName") String columnName);
}