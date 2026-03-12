package com.celine.onlineticketmanagementserver.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    Optional<Person> findByEmail(String email);

    Optional<Person> findByEmailIgnoreCase(String email);

    Optional<Person> findByPhone(String phone);

    Optional<Person> findByUsername(String username);

    Optional<Person> findByRefreshToken(String refreshToken);

    Optional<Person> findByPasswordResetToken(String token);

    Optional<Person> findByEmailVerificationToken(String token);

    List<Person> findByFirstNameContainingIgnoreCase(String firstName);

    List<Person> findByLastNameContainingIgnoreCase(String lastName);

    List<Person> findByLivesInId(Long locationId);

    Page<Person> findByLivesInId(Long locationId, Pageable pageable);

    List<Person> findAllByOrderByRegisteredAtDesc();

    List<Person> findAllByOrderByFirstNameAscLastNameAsc();

    @Query("SELECT p FROM Person p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Person> searchByName(@Param("name") String name);

    @Query("SELECT DISTINCT p FROM Person p JOIN p.roles r WHERE r.name = :roleType")
    List<Person> findByRoleType(@Param("roleType") RoleType roleType);

    @Query("SELECT DISTINCT p FROM Person p JOIN p.roles r WHERE r.name = :roleType")
    Page<Person> findByRoleType(@Param("roleType") RoleType roleType, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Person p WHERE SIZE(p.managesVenues) > 0")
    List<Person> findAllVenueManagers();

    @Query("SELECT p FROM Person p JOIN p.managesVenues v WHERE v.id = :venueId")
    List<Person> findManagersOfVenue(@Param("venueId") Long venueId);

    @Query("SELECT COUNT(p) > 0 FROM Person p JOIN p.managesVenues v WHERE p.id = :personId")
    Boolean isVenueManager(@Param("personId") Long personId);

    @Query("SELECT COUNT(p) > 0 FROM Person p JOIN p.managesVenues v WHERE p.id = :personId AND v.id = :venueId")
    Boolean managesVenue(@Param("personId") Long personId, @Param("venueId") Long venueId);

    Long countByLivesInId(Long locationId);

    @Query("SELECT COUNT(DISTINCT p) FROM Person p JOIN p.roles r WHERE r.name = :roleType")
    Long countByRoleType(@Param("roleType") RoleType roleType);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookedBy.id = :personId")
    Long countBookingsByPerson(@Param("personId") Long personId);

    @Query("SELECT DISTINCT p FROM Person p JOIN FETCH p.managesVenues")
    List<Person> findAllWithManagedVenues();

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.bookedBy.id = :personId")
    Boolean hasBookings(@Param("personId") Long personId);

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.roles LEFT JOIN FETCH p.livesIn WHERE p.id = :personId")
    Optional<Person> findByIdWithRelationships(@Param("personId") Long personId);

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.roles LEFT JOIN FETCH p.managesVenues LEFT JOIN FETCH p.livesIn")
    List<Person> findAllWithRelations();

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.managesVenues mv WHERE SIZE(p.managesVenues) > 0")
    List<Person> findAllVenueManagersWithVenues();

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.roles LEFT JOIN FETCH p.managesVenues LEFT JOIN FETCH p.livesIn WHERE p.livesIn.id = :locationId")
    List<Person> findByLocationIdWithRolesAndVenues(@Param("locationId") Long locationId);

    @Query("SELECT p FROM Person p WHERE p.registeredAt > :date ORDER BY p.registeredAt DESC")
    List<Person> findRecentlyRegistered(@Param("date") Date date);

    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.roles WHERE p.username = :username")
    Optional<Person> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.roles WHERE p.username = :identifier OR p.email = :identifier")
    Optional<Person> findByUsernameOrEmailWithRoles(@Param("identifier") String identifier);

    @Query("SELECT DISTINCT p FROM Person p JOIN p.roles r WHERE r.name = 'ADMIN'")
    List<Person> findAllAdmins();

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN p.livesIn loc WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(loc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Person> searchAll(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN p.livesIn loc WHERE CASE :columnName WHEN 'firstName' THEN LOWER(p.firstName) WHEN 'lastName' THEN LOWER(p.lastName) WHEN 'email' THEN LOWER(p.email) WHEN 'username' THEN LOWER(p.username) WHEN 'phone' THEN LOWER(p.phone) WHEN 'locationName' THEN LOWER(loc.name) ELSE LOWER(p.firstName) END LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Person> searchByColumn(@Param("searchTerm") String searchTerm, @Param("columnName") String columnName);

    @Query("""
        SELECT p FROM Person p
        JOIN p.livesIn v
        JOIN v.parentLocation c
        JOIN c.parentLocation s
        JOIN s.parentLocation d
        JOIN d.parentLocation pr
        WHERE v.type = :villageType
          AND pr.type = :provinceType
          AND LOWER(pr.code) = LOWER(:provinceCode)
        """)
    List<Person> findByProvinceCode(
            @Param("provinceCode") String provinceCode,
            @Param("villageType") LocationType villageType,
            @Param("provinceType") LocationType provinceType);

    @Query("""
        SELECT p FROM Person p
        JOIN p.livesIn v
        JOIN v.parentLocation c
        JOIN c.parentLocation s
        JOIN s.parentLocation d
        JOIN d.parentLocation pr
        WHERE v.type = :villageType
          AND pr.type = :provinceType
          AND LOWER(pr.name) = LOWER(:provinceName)
        """)
    List<Person> findByProvinceName(
            @Param("provinceName") String provinceName,
            @Param("villageType") LocationType villageType,
            @Param("provinceType") LocationType provinceType);

    @Query("""
        SELECT p FROM Person p
        JOIN p.livesIn v
        JOIN v.parentLocation c
        JOIN c.parentLocation s
        JOIN s.parentLocation d
        JOIN d.parentLocation pr
        WHERE v.type = :villageType
          AND pr.type = :provinceType
          AND (LOWER(pr.code) = LOWER(:value) OR LOWER(pr.name) = LOWER(:value))
        """)
    List<Person> findByProvinceCodeOrProvinceName(
            @Param("value") String value,
            @Param("villageType") LocationType villageType,
            @Param("provinceType") LocationType provinceType);
}
