package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import(Security.class)
public class VenuesControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService;

    private Venue venue1;
    private Venue venue2;
    private List<Event> events;

    @BeforeEach
    public void setup() {
        // Setup Venue 1
        venue1 = new Venue();
        venue1.setId(1L);
        venue1.setName("Venue One");

        // Setup Venue 2
        venue2 = new Venue();
        venue2.setId(2L);
        venue2.setName("Venue Two");

        // Setup Events
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Event pastEvent = new Event();
        pastEvent.setId(1L);
        pastEvent.setName("Past Event");
        pastEvent.setDate(today.minusDays(1));
        pastEvent.setVenue(venue1);

        Event todayPastEvent = new Event();
        todayPastEvent.setId(2L);
        todayPastEvent.setName("Today Past Event");
        todayPastEvent.setDate(today);
        todayPastEvent.setTime(now.minusHours(1));
        todayPastEvent.setVenue(venue1);

        Event todayUpcomingEvent = new Event();
        todayUpcomingEvent.setId(3L);
        todayUpcomingEvent.setName("Today Upcoming Event");
        todayUpcomingEvent.setDate(today);
        todayUpcomingEvent.setTime(now.plusHours(1));
        todayUpcomingEvent.setVenue(venue1);

        Event futureEvent1 = new Event();
        futureEvent1.setId(4L);
        futureEvent1.setName("Future Event 1");
        futureEvent1.setDate(today.plusDays(1));
        futureEvent1.setVenue(venue1);

        Event futureEvent2 = new Event();
        futureEvent2.setId(5L);
        futureEvent2.setName("Future Event 2");
        futureEvent2.setDate(today.plusDays(2));
        futureEvent2.setVenue(venue1);

        Event futureEvent3 = new Event();
        futureEvent3.setId(6L);
        futureEvent3.setName("Future Event 3");
        futureEvent3.setDate(today.plusDays(3));
        futureEvent3.setVenue(venue1);

        Event eventForVenue2 = new Event();
        eventForVenue2.setId(7L);
        eventForVenue2.setName("Event for Venue 2");
        eventForVenue2.setDate(today.plusDays(1));
        eventForVenue2.setVenue(venue2);

        events = Arrays.asList(pastEvent, todayPastEvent, todayUpcomingEvent, futureEvent1, futureEvent2, futureEvent3, eventForVenue2);
    }
    
    @Test
    public void testGetAllVenuesWithMultipleVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Arrays.asList(venue1, venue2));

        mockMvc.perform(get("/api/venues")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.venues", hasSize(2)))
                .andExpect(jsonPath("$._embedded.venues[0].name", is("Venue One")))
                .andExpect(jsonPath("$._embedded.venues[1].name", is("Venue Two")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.self.href", endsWith("/api/venues/1")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("/api/venues/1/events")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.next3events.href", endsWith("/api/venues/1/next3events")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
    }
    
    @Test
    public void testGetAllVenuesWithNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/venues")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
    }
    
    @Test
    public void testGetVenueExisting() throws Exception {
        when(venueService.findById(1L)).thenReturn(venue1);

        mockMvc.perform(get("/api/venues/1")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.name", is("Venue One")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1")))
                .andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/1")))
                .andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/1/events")))
                .andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/1/next3events")));
    }
    
    @Test
    public void testGetVenueNonExisting() throws Exception {
        when(venueService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/venues/99")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Could not find venue 99")))
                .andExpect(jsonPath("$.id", is(99)));
    }
    
    @Test
    public void testGetVenueEventsExistingWithEvents() throws Exception {
        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/venues/1/events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(6)))
                .andExpect(jsonPath("$._embedded.events[*].name", containsInAnyOrder(
                        "Past Event", "Today Past Event", "Today Upcoming Event",
                        "Future Event 1", "Future Event 2", "Future Event 3")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/events")));
    }
    @Test
    public void testGetVenueEventsExistingNoEvents() throws Exception {
        when(venueService.findById(2L)).thenReturn(venue2);
        when(eventService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/venues/2/events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/2/events")));
    }
    
    @Test
    public void testGetVenueEventsNonExisting() throws Exception {
        when(venueService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/venues/99/events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Could not find venue 99")))
                .andExpect(jsonPath("$.id", is(99)));
    }
    
    @Test
    public void testGetVenueNext3EventsWithMoreThanThree() throws Exception {
        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(3)))
                .andExpect(jsonPath("$._embedded.events[0].name", is("Today Upcoming Event")))
                .andExpect(jsonPath("$._embedded.events[1].name", is("Future Event 1")))
                .andExpect(jsonPath("$._embedded.events[2].name", is("Future Event 2")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));
    }
    
    @Test
    public void testGetVenueNext3EventsWithExactlyThree() throws Exception {
        List<Event> threeEvents = Arrays.asList(
                events.get(2), // todayUpcomingEvent
                events.get(3), // futureEvent1
                events.get(4)  // futureEvent2
        );
        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(threeEvents);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(3)))
                .andExpect(jsonPath("$._embedded.events[0].name", is("Today Upcoming Event")))
                .andExpect(jsonPath("$._embedded.events[1].name", is("Future Event 1")))
                .andExpect(jsonPath("$._embedded.events[2].name", is("Future Event 2")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));
    }
    
    @Test
    public void testGetVenueNext3EventsWithFewerThanThree() throws Exception {
        List<Event> twoEvents = Arrays.asList(
                events.get(2), // todayUpcomingEvent
                events.get(3)  // futureEvent1
        );
        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(twoEvents);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(2)))
                .andExpect(jsonPath("$._embedded.events[0].name", is("Today Upcoming Event")))
                .andExpect(jsonPath("$._embedded.events[1].name", is("Future Event 1")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));
    }
    
    @Test
    public void testGetVenueNext3EventsWithNoUpcomingEvents() throws Exception {
        List<Event> pastEvents = Arrays.asList(
                events.get(0), // pastEvent
                events.get(1)  // todayPastEvent
        );
        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(pastEvents);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));
    }
    
    @Test
    public void testGetVenueNext3EventsNonExisting() throws Exception {
        when(venueService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/venues/99/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Could not find venue 99")))
                .andExpect(jsonPath("$.id", is(99)));
    }
    
    @Test
    public void testGetVenueNext3EventsSortingSameDay() throws Exception {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Event todayEvent1 = new Event();
        todayEvent1.setId(8L);
        todayEvent1.setName("Today Event 1");
        todayEvent1.setDate(today);
        todayEvent1.setTime(now.plusMinutes(30));
        todayEvent1.setVenue(venue1);

        Event todayEvent2 = new Event();
        todayEvent2.setId(9L);
        todayEvent2.setName("Today Event 2");
        todayEvent2.setDate(today);
        todayEvent2.setTime(now.plusHours(1));
        todayEvent2.setVenue(venue1);

        Event futureEvent = new Event();
        futureEvent.setId(10L);
        futureEvent.setName("Future Event");
        futureEvent.setDate(today.plusDays(1));
        futureEvent.setVenue(venue1);

        List<Event> testEvents = Arrays.asList(todayEvent1, todayEvent2, futureEvent);

        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(testEvents);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(3)))
                .andExpect(jsonPath("$._embedded.events[0].name", is("Today Event 1")))
                .andExpect(jsonPath("$._embedded.events[1].name", is("Today Event 2")))
                .andExpect(jsonPath("$._embedded.events[2].name", is("Future Event")));
    }
    
    @Test
    public void testGetVenueNext3EventsWithNullTime() throws Exception {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Event todayEventWithTime = new Event();
        todayEventWithTime.setId(11L);
        todayEventWithTime.setName("Today Event With Time");
        todayEventWithTime.setDate(today);
        todayEventWithTime.setTime(now.plusHours(1));
        todayEventWithTime.setVenue(venue1);

        Event todayEventNoTime = new Event();
        todayEventNoTime.setId(12L);
        todayEventNoTime.setName("Today Event No Time");
        todayEventNoTime.setDate(today);
        todayEventNoTime.setTime(null);
        todayEventNoTime.setVenue(venue1);

        Event futureEvent = new Event();
        futureEvent.setId(13L);
        futureEvent.setName("Future Event");
        futureEvent.setDate(today.plusDays(1));
        futureEvent.setVenue(venue1);

        List<Event> testEvents = Arrays.asList(todayEventWithTime, todayEventNoTime, futureEvent);

        when(venueService.findById(1L)).thenReturn(venue1);
        when(eventService.findAll()).thenReturn(testEvents);

        mockMvc.perform(get("/api/venues/1/next3events")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.events", hasSize(3)))
                .andExpect(jsonPath("$._embedded.events[0].name", is("Today Event With Time")))
                .andExpect(jsonPath("$._embedded.events[1].name", is("Today Event No Time")))
                .andExpect(jsonPath("$._embedded.events[2].name", is("Future Event")));
    }
}