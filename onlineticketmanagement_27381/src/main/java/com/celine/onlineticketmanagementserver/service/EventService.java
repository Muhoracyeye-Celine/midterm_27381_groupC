package com.celine.onlineticketmanagementserver.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.celine.onlineticketmanagementserver.enums.EventStatus;
import com.celine.onlineticketmanagementserver.model.Event;

public interface EventService {

    Event createEvent(Event event);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

    Optional<Event> getEventById(Long id);

    Optional<Event> getEventByIdWithVenue(Long id);

    Optional<Event> getEventByIdWithTickets(Long id);

    List<Event> getAllEvents();

    List<Event> getAllEventsWithDetails();

    List<Event> getEventsByStatus(EventStatus status);

    List<Event> getEventsByVenueId(Long venueId);

    List<Event> getUpcomingEvents();

    List<Event> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    Long countAvailableTickets(Long eventId);

    List<Event> searchAll(String searchTerm);

    List<Event> searchByColumn(String searchTerm, String columnName);
}
