package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

    private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<String> venueNotFoundHandler(VenueNotFoundException ex) {
        String errorBody = String.format("{ \"error\": \"%s\", \"id\": %d }", ex.getMessage(), ex.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody);
    }
    
    @GetMapping
    public CollectionModel<EntityModel<Venue>> getAllVenues() {
        Iterable<Venue> venues = venueService.findAll();
        
        List<EntityModel<Venue>> resources = StreamSupport.stream(venues.spliterator(), false)
                .map(venue -> EntityModel.of(venue, 
                        linkTo(methodOn(VenuesControllerApi.class).getVenue(venue.getId())).withSelfRel(),
                        linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(venue.getId())).withRel("events"),
                        linkTo(methodOn(VenuesControllerApi.class).getVenueNext3Events(venue.getId())).withRel("next3events")))
                .collect(Collectors.toList());
        
        return CollectionModel.of(resources,
                linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id);
        if (venue == null) {
            throw new VenueNotFoundException(id);
        }
        
        return EntityModel.of(venue,
                linkTo(methodOn(VenuesControllerApi.class).getVenue(id)).withSelfRel(),
                linkTo(methodOn(VenuesControllerApi.class).getVenue(id)).withRel("venue"),
                linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(id)).withRel("events"),
                linkTo(methodOn(VenuesControllerApi.class).getVenueNext3Events(id)).withRel("next3events"));
    }
    
    @GetMapping("/{id}/events")
    public CollectionModel<EntityModel<Event>> getVenueEvents(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id);
        if (venue == null) {
            throw new VenueNotFoundException(id);
        }
        
        List<Event> venueEvents = new ArrayList<>();
        for (Event event : eventService.findAll()) {
            if (event.getVenue() != null && event.getVenue().getId() == id) {
                venueEvents.add(event);
            }
        }
        
        List<EntityModel<Event>> resources = venueEvents.stream()
                .map(event -> EntityModel.of(event,
                        linkTo(methodOn(EventsControllerApi.class).getEvent(event.getId())).withSelfRel()))
                .collect(Collectors.toList());
        
        return CollectionModel.of(resources,
                linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(id)).withSelfRel());
    }
    
    @GetMapping("/{id}/next3events")
    public CollectionModel<EntityModel<Event>> getVenueNext3Events(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id);
        if (venue == null) {
            throw new VenueNotFoundException(id);
        }
        
        List<Event> venueEvents = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        for (Event event : eventService.findAll()) {
            if (event.getVenue() != null && event.getVenue().getId() == id) {
                // Filter for upcoming events
                if (event.getDate().isAfter(today) || 
                    (event.getDate().isEqual(today) && 
                    (event.getTime() == null || event.getTime().isAfter(now)))) {
                    venueEvents.add(event);
                }
            }
        }
        
        // Sort by date and time
        venueEvents.sort(Comparator.comparing(Event::getDate)
                .thenComparing(event -> event.getTime() != null ? event.getTime() : LocalTime.MAX));
        
        // Limit to next 3 events
        List<Event> next3Events = venueEvents.stream().limit(3).collect(Collectors.toList());
        
        List<EntityModel<Event>> resources = next3Events.stream()
                .map(event -> EntityModel.of(event,
                        linkTo(methodOn(EventsControllerApi.class).getEvent(event.getId())).withSelfRel()))
                .collect(Collectors.toList());
        
        return CollectionModel.of(resources,
                linkTo(methodOn(VenuesControllerApi.class).getVenueNext3Events(id)).withSelfRel());
    }
}