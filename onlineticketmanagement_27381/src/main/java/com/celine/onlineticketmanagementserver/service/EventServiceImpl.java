package com.celine.onlineticketmanagementserver.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.EventStatus;
import com.celine.onlineticketmanagementserver.exception.ResourceNotFoundException;
import com.celine.onlineticketmanagementserver.exception.ValidationException;
import com.celine.onlineticketmanagementserver.model.Event;
import com.celine.onlineticketmanagementserver.model.Venue;
import com.celine.onlineticketmanagementserver.repository.EventRepository;
import com.celine.onlineticketmanagementserver.repository.VenueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public Event createEvent(Event event) {
        validateEvent(event);
        Venue venue = venueRepository.findById(event.getVenue().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + event.getVenue().getId()));
        event.setVenue(venue);
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.UPCOMING);
        }
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, Event event) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        validateEvent(event);
        Venue venue = venueRepository.findById(event.getVenue().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + event.getVenue().getId()));

        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setEventDate(event.getEventDate());
        existing.setCategory(event.getCategory());
        existing.setTicketPrice(event.getTicketPrice());
        existing.setTotalTickets(event.getTotalTickets());
        existing.setStatus(event.getStatus() != null ? event.getStatus() : existing.getStatus());
        existing.setVenue(venue);
        return eventRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public Optional<Event> getEventByIdWithVenue(Long id) {
        return eventRepository.findByIdWithVenue(id);
    }

    @Override
    public Optional<Event> getEventByIdWithTickets(Long id) {
        return eventRepository.findByIdWithTickets(id);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getAllEventsWithDetails() {
        return eventRepository.findAllWithDetails();
    }

    @Override
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public List<Event> getEventsByVenueId(Long venueId) {
        return eventRepository.findByVenueId(venueId);
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), EventStatus.UPCOMING);
    }

    @Override
    public List<Event> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findEventsBetweenDates(startDate, endDate);
    }

    @Override
    public Long countAvailableTickets(Long eventId) {
        return eventRepository.countAvailableTickets(eventId);
    }

    @Override
    public List<Event> searchAll(String searchTerm) {
        return eventRepository.searchAll(searchTerm);
    }

    @Override
    public List<Event> searchByColumn(String searchTerm, String columnName) {
        return eventRepository.searchByColumn(searchTerm, columnName);
    }

    private void validateEvent(Event event) {
        if (event.getVenue() == null || event.getVenue().getId() == null) {
            throw new ValidationException("Venue is required");
        }
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new ValidationException("Title is required");
        }
        if (event.getEventDate() == null) {
            throw new ValidationException("Event date is required");
        }
        log.info("Validated event request for title {}", event.getTitle());
    }
}
