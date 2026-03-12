package com.celine.onlineticketmanagementserver.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.celine.onlineticketmanagementserver.model.Venue;
import com.celine.onlineticketmanagementserver.service.VenueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @PostMapping("/save")
    public ResponseEntity<String> saveVenue(@RequestBody Venue venue) {
        String result = venueService.saveVenue(venue);
        if (result.contains("already exists")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        if (result.contains("capacity must be")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        if (result.contains("successfully")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/create")
    public ResponseEntity<Venue> createVenueWithManagers(@Valid @RequestBody Venue venue) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenueWithManagers(venue));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Venue>> getAllVenues() {
        return new ResponseEntity<>(venueService.getAllVenuesWithDetails(), HttpStatus.OK);
    }

    @GetMapping("/all/ordered-by-name")
    public ResponseEntity<List<Venue>> getAllVenuesOrderedByName() {
        return new ResponseEntity<>(venueService.getAllVenuesOrderedByName(), HttpStatus.OK);
    }

    @GetMapping("/all/ordered-by-capacity")
    public ResponseEntity<List<Venue>> getAllVenuesOrderedByCapacity() {
        return new ResponseEntity<>(venueService.getAllVenuesOrderedByCapacity(), HttpStatus.OK);
    }

    @GetMapping("/all/paginated")
    public ResponseEntity<Page<Venue>> getAllVenuesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(venueService.getAllVenuesWithPagination(pageable), HttpStatus.OK);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Venue>> searchVenuesByName(@RequestParam String name) {
        return new ResponseEntity<>(venueService.searchVenuesByName(name), HttpStatus.OK);
    }

    @GetMapping("/search/address")
    public ResponseEntity<List<Venue>> searchVenuesByAddress(@RequestParam String address) {
        return new ResponseEntity<>(venueService.searchVenuesByAddress(address), HttpStatus.OK);
    }

    @GetMapping("/capacity/minimum/{minCapacity}")
    public ResponseEntity<List<Venue>> getVenuesByMinimumCapacity(@PathVariable Integer minCapacity) {
        return new ResponseEntity<>(venueService.getVenuesByMinimumCapacity(minCapacity), HttpStatus.OK);
    }

    @GetMapping("/capacity/range")
    public ResponseEntity<List<Venue>> getVenuesByCapacityRange(@RequestParam Integer minCapacity, @RequestParam Integer maxCapacity) {
        return new ResponseEntity<>(venueService.getVenuesByCapacityRange(minCapacity, maxCapacity), HttpStatus.OK);
    }

    @GetMapping("/capacity/maximum/{maxCapacity}")
    public ResponseEntity<List<Venue>> getVenuesByMaximumCapacity(@PathVariable Integer maxCapacity) {
        return new ResponseEntity<>(venueService.getVenuesByMaximumCapacity(maxCapacity), HttpStatus.OK);
    }

    @GetMapping("/count/capacity/minimum/{minCapacity}")
    public ResponseEntity<Long> countVenuesWithMinimumCapacity(@PathVariable Integer minCapacity) {
        return new ResponseEntity<>(venueService.countVenuesWithMinimumCapacity(minCapacity), HttpStatus.OK);
    }

    @GetMapping("/with-upcoming-events")
    public ResponseEntity<List<Venue>> getVenuesWithUpcomingEvents() {
        return new ResponseEntity<>(venueService.getVenuesWithUpcomingEvents(), HttpStatus.OK);
    }

    @GetMapping("/managed-by/{personId}")
    public ResponseEntity<List<Venue>> getVenuesManagedByPerson(@PathVariable Long personId) {
        return new ResponseEntity<>(venueService.getVenuesManagedByPerson(personId), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getVenueByName(@PathVariable String name) {
        Optional<Venue> venue = venueService.getVenueByNameIgnoreCase(name);
        return venue.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Venue not found with name: " + name, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getVenueById(@PathVariable Long id) {
        Optional<Venue> venue = venueService.getVenueById(id);
        return venue.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Venue not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/id/{id}/with-managers")
    public ResponseEntity<?> getVenueByIdWithManagers(@PathVariable Long id) {
        Optional<Venue> venue = venueService.getVenueByIdWithManagers(id);
        return venue.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Venue not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/id/{id}/details")
    public ResponseEntity<Venue> getVenueWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueWithDetails(id));
    }

    @GetMapping("/id/{venueId}/events/count")
    public ResponseEntity<Long> countEventsAtVenue(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.countEventsAtVenue(venueId), HttpStatus.OK);
    }

    @GetMapping("/id/{venueId}/managers/count")
    public ResponseEntity<Long> countManagersOfVenue(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.countManagersOfVenue(venueId), HttpStatus.OK);
    }

    @GetMapping("/id/{venueId}/has-events")
    public ResponseEntity<Boolean> hasEvents(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.hasEvents(venueId), HttpStatus.OK);
    }

    @GetMapping("/id/{venueId}/has-upcoming-events")
    public ResponseEntity<Boolean> hasUpcomingEvents(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.hasUpcomingEvents(venueId), HttpStatus.OK);
    }

    @GetMapping("/id/{venueId}/is-manager/{personId}")
    public ResponseEntity<Boolean> isPersonManagerOfVenue(@PathVariable Long venueId, @PathVariable Long personId) {
        return new ResponseEntity<>(venueService.isPersonManagerOfVenue(venueId, personId), HttpStatus.OK);
    }

    @PostMapping("/id/{id}/managers/add")
    public ResponseEntity<Venue> addManagers(@PathVariable Long id, @Valid @RequestBody Venue venue) {
        return ResponseEntity.ok(venueService.addManagers(id, venue));
    }

    @DeleteMapping("/id/{id}/managers/remove")
    public ResponseEntity<Venue> removeManagers(@PathVariable Long id, @Valid @RequestBody Venue venue) {
        return ResponseEntity.ok(venueService.removeManagers(id, venue));
    }

    @PutMapping("/id/{id}/managers/set")
    public ResponseEntity<Venue> setManagers(@PathVariable Long id, @Valid @RequestBody Venue venue) {
        return ResponseEntity.ok(venueService.setManagers(id, venue));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateVenue(@PathVariable Long id, @RequestBody Venue updatedVenue) {
        String message = venueService.updateVenue(id, updatedVenue);
        if (message.equals("Venue updated successfully")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if (message.contains("already exists")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        if (message.contains("capacity must be")) {
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        if (message.contains("not found")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/id/{id}/events/assign")
    public ResponseEntity<Venue> assignEvents(@PathVariable Long id, @RequestBody Set<Long> eventIds) {
        return ResponseEntity.ok(venueService.assignEvents(id, eventIds));
    }

    @PostMapping("/id/{sourceId}/events/move-to/{targetId}")
    public ResponseEntity<Venue> moveEvents(@PathVariable Long sourceId, @PathVariable Long targetId, @RequestBody Set<Long> eventIds) {
        return ResponseEntity.ok(venueService.moveEventsToVenue(sourceId, targetId, eventIds));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteVenue(@PathVariable Long id) {
        String result = venueService.deleteVenue(id);
        if (result.startsWith("Venue deleted")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("has events")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        if (result.contains("not found")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Venue>> searchVenues(@RequestParam String searchTerm) {
        return ResponseEntity.ok(venueService.searchAll(searchTerm));
    }

    @GetMapping("/search/column")
    public ResponseEntity<List<Venue>> searchVenuesByColumn(@RequestParam String searchTerm, @RequestParam String column) {
        return ResponseEntity.ok(venueService.searchByColumn(searchTerm, column));
    }
}
