package com.celine.onlineticketmanagementserver.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Person;
import com.celine.onlineticketmanagementserver.service.PersonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    private final PersonService personService;

    @GetMapping("/all")
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @GetMapping("/all-with-relations")
    public ResponseEntity<List<Person>> getAllPersonsWithRelations() {
        return ResponseEntity.ok(personService.getAllPersonsWithRelations());
    }

    @GetMapping("/ordered-by-date")
    public ResponseEntity<List<Person>> getAllPersonsOrderedByRegistrationDate() {
        return ResponseEntity.ok(personService.getAllPersonsOrderedByRegistrationDate());
    }

    @GetMapping("/ordered-by-name")
    public ResponseEntity<List<Person>> getAllPersonsOrderedByName() {
        return ResponseEntity.ok(personService.getAllPersonsOrderedByName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    @GetMapping("/{id}/with-relations")
    public ResponseEntity<Person> getPersonByIdWithRelationships(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonByIdWithRelationships(id));
    }

    @GetMapping("/email")
    public ResponseEntity<Person> getPersonByEmail(@RequestParam String email) {
        return ResponseEntity.ok(personService.getPersonByEmail(email));
    }

    @GetMapping("/email-ignore-case")
    public ResponseEntity<Person> getPersonByEmailIgnoreCase(@RequestParam String email) {
        return ResponseEntity.ok(personService.getPersonByEmailIgnoreCase(email));
    }

    @GetMapping("/phone")
    public ResponseEntity<Person> getPersonByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(personService.getPersonByPhone(phone));
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Person>> searchByFirstName(@RequestParam String name) {
        return ResponseEntity.ok(personService.searchByFirstName(name));
    }

    @GetMapping("/search/lastname")
    public ResponseEntity<List<Person>> searchByLastName(@RequestParam String name) {
        return ResponseEntity.ok(personService.searchByLastName(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Person>> searchPersons(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String name) {
        String term = searchTerm != null ? searchTerm : name;
        if (term == null || term.isBlank()) {
            return ResponseEntity.ok(personService.getAllPersons());
        }
        return ResponseEntity.ok(personService.searchAll(term));
    }

    @GetMapping("/search/column")
    public ResponseEntity<List<Person>> searchPersonsByColumn(
            @RequestParam String searchTerm,
            @RequestParam String column) {
        return ResponseEntity.ok(personService.searchByColumn(searchTerm, column));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Person>> getPersonsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(personService.getPersonsByLocation(locationId));
    }

    @GetMapping("/location/{locationId}/with-details")
    public ResponseEntity<List<Person>> getPersonsByLocationWithDetails(@PathVariable Long locationId) {
        return ResponseEntity.ok(personService.getPersonsByLocationWithDetails(locationId));
    }

    @GetMapping("/role/{roleType}")
    public ResponseEntity<List<Person>> getPersonsByRoleType(@PathVariable RoleType roleType) {
        return ResponseEntity.ok(personService.getPersonsByRoleType(roleType));
    }

    @GetMapping("/venue-managers")
    public ResponseEntity<List<Person>> getAllVenueManagers() {
        return ResponseEntity.ok(personService.getAllVenueManagers());
    }

    @GetMapping("/venue-managers/with-venues")
    public ResponseEntity<List<Person>> getAllVenueManagersWithVenues() {
        return ResponseEntity.ok(personService.getAllVenueManagersWithVenues());
    }

    @GetMapping("/venue/{venueId}/managers")
    public ResponseEntity<List<Person>> getManagersOfVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(personService.getManagersOfVenue(venueId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Person>> getRecentlyRegistered(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date afterDate) {
        return ResponseEntity.ok(personService.getRecentlyRegistered(afterDate));
    }

    @PostMapping
    public ResponseEntity<Person> createPerson(@Valid @RequestBody Person person) {
        log.info("POST request to create person with email: {}", person.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(person));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {
        return ResponseEntity.ok(personService.updatePerson(id, person));
    }

    @PutMapping("/{personId}/roles")
    public ResponseEntity<Person> updatePersonRoles(
            @PathVariable Long personId,
            @RequestParam Set<Long> roleIds) {
        return ResponseEntity.ok(personService.updatePersonRoles(personId, roleIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{personId}/role/{roleId}")
    public ResponseEntity<Person> assignRoleToPerson(@PathVariable Long personId, @PathVariable Long roleId) {
        return ResponseEntity.ok(personService.assignRoleToPerson(personId, roleId));
    }

    @DeleteMapping("/{personId}/role/{roleId}")
    public ResponseEntity<Person> removeRoleFromPerson(@PathVariable Long personId, @PathVariable Long roleId) {
        return ResponseEntity.ok(personService.removeRoleFromPerson(personId, roleId));
    }

    @PostMapping("/{personId}/venue/{venueId}")
    public ResponseEntity<Person> addVenueManager(@PathVariable Long personId, @PathVariable Long venueId) {
        return ResponseEntity.ok(personService.addVenueManager(personId, venueId));
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(personService.existsByEmail(email));
    }

    @GetMapping("/exists/phone")
    public ResponseEntity<Boolean> existsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(personService.existsByPhone(phone));
    }

    @GetMapping("/{personId}/is-venue-manager")
    public ResponseEntity<Boolean> isVenueManager(@PathVariable Long personId) {
        return ResponseEntity.ok(personService.isVenueManager(personId));
    }

    @GetMapping("/{personId}/manages-venue/{venueId}")
    public ResponseEntity<Boolean> managesVenue(@PathVariable Long personId, @PathVariable Long venueId) {
        return ResponseEntity.ok(personService.managesVenue(personId, venueId));
    }

    @GetMapping("/{personId}/has-bookings")
    public ResponseEntity<Boolean> hasBookings(@PathVariable Long personId) {
        return ResponseEntity.ok(personService.hasBookings(personId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAllPersons() {
        return ResponseEntity.ok(personService.countAllPersons());
    }

    @GetMapping("/count/location/{locationId}")
    public ResponseEntity<Long> countPersonsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(personService.countPersonsByLocation(locationId));
    }

    @GetMapping("/count/role/{roleType}")
    public ResponseEntity<Long> countPersonsByRoleType(@PathVariable RoleType roleType) {
        return ResponseEntity.ok(personService.countPersonsByRoleType(roleType));
    }

    @GetMapping("/count/bookings/{personId}")
    public ResponseEntity<Long> countBookingsByPerson(@PathVariable Long personId) {
        return ResponseEntity.ok(personService.countBookingsByPerson(personId));
    }

    @GetMapping("/province/code/{provinceCode}")
    public ResponseEntity<List<Person>> getPersonsByProvinceCode(@PathVariable String provinceCode) {
        return ResponseEntity.ok(personService.getPersonsByProvinceCode(provinceCode));
    }

    @GetMapping("/province/name/{provinceName}")
    public ResponseEntity<List<Person>> getPersonsByProvinceName(@PathVariable String provinceName) {
        return ResponseEntity.ok(personService.getPersonsByProvinceName(provinceName));
    }

    @GetMapping("/province/search")
    public ResponseEntity<List<Person>> getPersonsByProvinceCodeOrName(@RequestParam String value) {
        return ResponseEntity.ok(personService.getPersonsByProvinceCodeOrName(value));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Person>> getPersonsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(personService.getPersonsPage(pageable));
    }
}
