package com.celine.onlineticketmanagementserver.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.exception.ResourceNotFoundException;
import com.celine.onlineticketmanagementserver.exception.ValidationException;
import com.celine.onlineticketmanagementserver.model.Event;
import com.celine.onlineticketmanagementserver.model.Person;
import com.celine.onlineticketmanagementserver.model.Venue;
import com.celine.onlineticketmanagementserver.repository.EventRepository;
import com.celine.onlineticketmanagementserver.repository.PersonRepository;
import com.celine.onlineticketmanagementserver.repository.VenueRepository;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VenueService {

    @Autowired
    private VenueRepository venueRepo;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public String saveVenue(Venue venue) {
        try {
            if (venueRepo.existsByName(venue.getName())) {
                return "Venue with this name already exists";
            }
            if (venue.getCapacity() == null || venue.getCapacity() <= 0) {
                return "Venue capacity must be greater than 0";
            }
            venueRepo.save(venue);
            return "Venue saved successfully";
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMostSpecificCause().getMessage();
            if (errorMessage.contains("name") || errorMessage.contains("unique")) {
                return "Venue name already exists (database constraint)";
            }
            return "Database constraint violation: " + errorMessage;
        } catch (Exception e) {
            return "Error saving venue: " + e.getMessage();
        }
    }

    @Transactional
    public Venue createVenueWithManagers(Venue venueRequest) {
        try {
            log.info("Creating venue: {}", venueRequest.getName());
            if (venueRepo.existsByName(venueRequest.getName())) {
                throw new ValidationException("Venue with this name already exists");
            }
            if (venueRequest.getCapacity() == null || venueRequest.getCapacity() <= 0) {
                throw new ValidationException("Venue capacity must be greater than 0");
            }

            Venue venue = new Venue();
            venue.setName(venueRequest.getName());
            venue.setAddress(venueRequest.getAddress());
            venue.setCapacity(venueRequest.getCapacity());
            venue.setEvents(new HashSet<>());
            venue.setManagers(new HashSet<>());

            Venue savedVenue = venueRepo.save(venue);

            if (venueRequest.getManagers() != null && !venueRequest.getManagers().isEmpty()) {
                for (Person managerReference : venueRequest.getManagers()) {
                    Person manager = personRepo.findById(managerReference.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerReference.getId()));
                    savedVenue.getManagers().add(manager);
                    manager.getManagesVenues().add(savedVenue);
                }
                personRepo.saveAll(savedVenue.getManagers());
            }

            if (venueRequest.getEvents() != null && !venueRequest.getEvents().isEmpty()) {
                for (Event eventReference : venueRequest.getEvents()) {
                    Event event = eventRepository.findById(eventReference.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventReference.getId()));
                    event.setVenue(savedVenue);
                }
                eventRepository.saveAll(venueRequest.getEvents().stream()
                        .map(eventReference -> eventRepository.findById(eventReference.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventReference.getId())))
                        .toList());
            }

            return venueRepo.findByIdWithManagers(savedVenue.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found after creation"));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Database constraint violation: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Venue addManagers(Long venueId, Venue venueRequest) {
        log.info("Adding managers to venue ID: {}", venueId);
        Venue venue = venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        if (venue.getManagers() == null) {
            venue.setManagers(new HashSet<>());
        }

        Set<Person> managersToSave = new HashSet<>();
        if (venueRequest.getManagers() != null && !venueRequest.getManagers().isEmpty()) {
            for (Person personToAdd : venueRequest.getManagers()) {
                boolean alreadyManager = venue.getManagers().stream().anyMatch(m -> m.getId().equals(personToAdd.getId()));
                if (alreadyManager) {
                    continue;
                }
                Person manager = personRepo.findById(personToAdd.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + personToAdd.getId()));
                venue.getManagers().add(manager);
                manager.getManagesVenues().add(venue);
                managersToSave.add(manager);
            }
        }

        if (!managersToSave.isEmpty()) {
            personRepo.saveAll(managersToSave);
            entityManager.flush();
            entityManager.clear();
        }
        return venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found after update"));
    }

    @Transactional
    public Venue removeManagers(Long venueId, Venue venueRequest) {
        Venue venue = venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
        if (venue.getManagers() == null) {
            venue.setManagers(new HashSet<>());
        }

        Set<Person> managersToSave = new HashSet<>();
        if (venueRequest.getManagers() != null && !venueRequest.getManagers().isEmpty()) {
            for (Person personToRemove : venueRequest.getManagers()) {
                Person manager = personRepo.findById(personToRemove.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + personToRemove.getId()));
                venue.getManagers().remove(manager);
                if (manager.getManagesVenues() != null) {
                    manager.getManagesVenues().remove(venue);
                    managersToSave.add(manager);
                }
            }
        }

        if (!managersToSave.isEmpty()) {
            personRepo.saveAll(managersToSave);
            entityManager.flush();
            entityManager.clear();
        }
        return venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found after update"));
    }

    @Transactional
    public Venue setManagers(Long venueId, Venue venueRequest) {
        Venue venue = venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
        Set<Person> managersToSave = new HashSet<>();

        if (venue.getManagers() != null && !venue.getManagers().isEmpty()) {
            for (Person oldManager : new HashSet<>(venue.getManagers())) {
                oldManager.getManagesVenues().remove(venue);
                managersToSave.add(oldManager);
            }
            venue.getManagers().clear();
        } else {
            venue.setManagers(new HashSet<>());
        }

        if (venueRequest.getManagers() != null && !venueRequest.getManagers().isEmpty()) {
            for (Person personToAdd : venueRequest.getManagers()) {
                Person manager = personRepo.findById(personToAdd.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + personToAdd.getId()));
                venue.getManagers().add(manager);
                manager.getManagesVenues().add(venue);
                managersToSave.add(manager);
            }
        }

        personRepo.saveAll(managersToSave);
        entityManager.flush();
        entityManager.clear();

        return venueRepo.findByIdWithManagers(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found after update"));
    }

    @Transactional
    public Venue assignEvents(Long venueId, Set<Long> eventIds) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            Set<Long> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(eventIds);
            missingIds.removeAll(foundIds);
            throw new ValidationException("Events not found with IDs: " + missingIds);
        }
        for (Event event : events) {
            event.setVenue(venue);
        }
        eventRepository.saveAll(events);
        entityManager.flush();
        entityManager.clear();
        return venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found after update"));
    }

    @Transactional
    public Venue removeEvents(Long venueId, Set<Long> eventIds) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
        List<Event> events = eventRepository.findAllById(eventIds);
        for (Event event : events) {
            if (event.getVenue() != null && event.getVenue().getId().equals(venueId)) {
                throw new ValidationException("Cannot remove events from venue. Events must have a venue. Please reassign to another venue instead.");
            }
        }
        return venue;
    }

    @Transactional
    public Venue moveEventsToVenue(Long sourceVenueId, Long targetVenueId, Set<Long> eventIds) {
        Venue targetVenue = venueRepo.findById(targetVenueId)
                .orElseThrow(() -> new ResourceNotFoundException("Target venue not found with id: " + targetVenueId));
        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            throw new ValidationException("Some event IDs not found");
        }
        for (Event event : events) {
            event.setVenue(targetVenue);
        }
        eventRepository.saveAll(events);
        entityManager.flush();
        entityManager.clear();
        return venueRepo.findById(targetVenueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found after update"));
    }

    @Transactional(readOnly = true)
    public Venue getVenueWithDetails(Long id) {
        Venue venue = venueRepo.findByIdWithManagers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        if (venue.getEvents() != null) {
            venue.getEvents().size();
        }
        if (venue.getManagers() != null) {
            venue.getManagers().size();
        }
        return venue;
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenues() {
        return venueRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenuesWithDetails() {
        return venueRepo.findAll().stream().peek(venue -> {
            if (venue.getEvents() != null) {
                venue.getEvents().size();
            }
            if (venue.getManagers() != null) {
                venue.getManagers().size();
            }
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenuesOrderedByName() {
        return venueRepo.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenuesOrderedByCapacity() {
        return venueRepo.findAllByOrderByCapacityDesc();
    }

    @Transactional(readOnly = true)
    public Page<Venue> getAllVenuesWithPagination(Pageable pageable) {
        return venueRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Venue> getVenueById(Long id) {
        return venueRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Venue> getVenueByIdWithManagers(Long id) {
        return venueRepo.findByIdWithManagers(id);
    }

    @Transactional(readOnly = true)
    public Optional<Venue> getVenueByName(String name) {
        return venueRepo.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<Venue> getVenueByNameIgnoreCase(String name) {
        return venueRepo.findByNameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Venue> searchVenuesByName(String name) {
        return venueRepo.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Venue> searchVenuesByAddress(String address) {
        return venueRepo.findByAddressContainingIgnoreCase(address);
    }

    @Transactional(readOnly = true)
    public List<Venue> getVenuesByMinimumCapacity(Integer minCapacity) {
        return venueRepo.findByCapacityGreaterThanEqual(minCapacity);
    }

    @Transactional(readOnly = true)
    public List<Venue> getVenuesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return venueRepo.findByCapacityBetween(minCapacity, maxCapacity);
    }

    @Transactional(readOnly = true)
    public List<Venue> getVenuesByMaximumCapacity(Integer maxCapacity) {
        return venueRepo.findByCapacityLessThanEqual(maxCapacity);
    }

    @Transactional(readOnly = true)
    public List<Venue> getVenuesWithUpcomingEvents() {
        return venueRepo.findVenuesWithUpcomingEvents();
    }

    @Transactional(readOnly = true)
    public List<Venue> getVenuesManagedByPerson(Long personId) {
        return venueRepo.findVenuesManagedByPerson(personId);
    }

    @Transactional(readOnly = true)
    public Long countEventsAtVenue(Long venueId) {
        return venueRepo.countEventsByVenue(venueId);
    }

    @Transactional(readOnly = true)
    public Long countManagersOfVenue(Long venueId) {
        return venueRepo.countManagersByVenue(venueId);
    }

    @Transactional(readOnly = true)
    public Long countVenuesWithMinimumCapacity(Integer minCapacity) {
        return venueRepo.countByCapacityGreaterThanEqual(minCapacity);
    }

    @Transactional(readOnly = true)
    public Boolean hasEvents(Long venueId) {
        return venueRepo.hasEvents(venueId);
    }

    @Transactional(readOnly = true)
    public Boolean hasUpcomingEvents(Long venueId) {
        return venueRepo.hasUpcomingEvents(venueId);
    }

    @Transactional(readOnly = true)
    public Boolean isPersonManagerOfVenue(Long venueId, Long personId) {
        return venueRepo.isPersonManagerOfVenue(venueId, personId);
    }

    @Transactional
    public String updateVenue(Long id, Venue updatedVenue) {
        try {
            Optional<Venue> existingVenue = venueRepo.findById(id);
            if (existingVenue.isPresent()) {
                Venue venue = existingVenue.get();
                if (!venue.getName().equals(updatedVenue.getName()) && venueRepo.existsByName(updatedVenue.getName())) {
                    return "Venue name already exists";
                }
                if (updatedVenue.getCapacity() == null || updatedVenue.getCapacity() <= 0) {
                    return "Venue capacity must be greater than 0";
                }
                venue.setName(updatedVenue.getName());
                venue.setAddress(updatedVenue.getAddress());
                venue.setCapacity(updatedVenue.getCapacity());
                venueRepo.save(venue);
                return "Venue updated successfully";
            }
            return "Venue not found with ID: " + id;
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMostSpecificCause().getMessage();
            if (errorMessage.contains("name") || errorMessage.contains("unique")) {
                return "Venue name already exists (database constraint)";
            }
            return "Database constraint violation: " + errorMessage;
        } catch (Exception e) {
            return "Error updating venue: " + e.getMessage();
        }
    }

    @Transactional
    public String deleteVenue(Long id) {
        try {
            Optional<Venue> existingVenue = venueRepo.findByIdWithManagers(id);
            if (existingVenue.isPresent()) {
                Venue venue = existingVenue.get();
                if (venueRepo.hasEvents(id)) {
                    return "Cannot delete venue because it has associated events.";
                }
                if (venue.getManagers() != null && !venue.getManagers().isEmpty()) {
                    Set<Person> managers = new HashSet<>(venue.getManagers());
                    for (Person manager : managers) {
                        if (manager.getManagesVenues() != null) {
                            manager.getManagesVenues().remove(venue);
                        }
                    }
                    venue.getManagers().clear();
                    personRepo.saveAll(managers);
                }
                venueRepo.delete(venue);
                return "Venue deleted successfully.";
            }
            return "Venue not found with ID: " + id;
        } catch (DataIntegrityViolationException e) {
            return "Database constraint violation: " + e.getMostSpecificCause().getMessage();
        } catch (Exception e) {
            return "Error deleting venue: " + e.getMessage();
        }
    }

    public List<Venue> searchAll(String searchTerm) {
        return venueRepo.searchAll(searchTerm);
    }

    public List<Venue> searchByColumn(String searchTerm, String columnName) {
        return venueRepo.searchByColumn(searchTerm, columnName);
    }
}
