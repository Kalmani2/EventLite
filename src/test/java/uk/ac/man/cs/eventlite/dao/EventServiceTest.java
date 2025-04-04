package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@MockBean
	private EventRepository eventRepository;

	@Autowired
    private EventService eventService;

	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
    
    @Test
    public void testCount() throws Exception {        
    	when(eventRepository.count()).thenReturn(2L);
        assertEquals(2L, eventService.count());
    }
    
    @Test 
    public void testFindAll() throws Exception {
        
    	List<Event> events = Arrays.asList(new Event(), new Event());
        when(eventRepository.findAllByOrderByDateAscTimeAsc()).thenReturn(events);
        
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(2, ((List<Event>) result).size());
        verify(eventRepository).findAllByOrderByDateAscTimeAsc();
    }
    
    @Test
    public void testFindByIdExists() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-31";
        String eventTime = "09:00";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        
        Optional<Event> result = eventService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getName());
        verify(eventRepository).findById(1L);
    }

    @Test
    public void testFindByIdNotExists() throws Exception {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());
        
        Optional<Event> result = eventService.findById(2L);
        assertFalse(result.isPresent());
        verify(eventRepository).findById(2L);
    }
    
    @Test
    public void testSave() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.save(event)).thenReturn(event);
        
        Event result = eventService.save(event);
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).save(event);
    }
    
    @Test
    public void testAddEvent() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.save(event)).thenReturn(event);
        
        Event result = eventService.addEvent(event);
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).save(event);
    }
    
    @Test
    public void testUpdate() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        
        Venue updatedVenue = new Venue();
        updatedVenue.setId(2);
        updatedVenue.setName("Test Venue Updated");
        updatedVenue.setCapacity(100);


        Event event = new Event();
        event.setName("Test Event");
        event.setDate(LocalDate.parse("2025-12-25"));
        event.setTime(LocalTime.parse("10:30"));
        event.setVenue(venue);
        
        Event updatedEvent = new Event();
        updatedEvent.setName("Updated Event");
        updatedEvent.setDate(LocalDate.parse("2025-12-27"));
        updatedEvent.setTime(LocalTime.parse("11:30"));
        updatedEvent.setVenue(updatedVenue);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
        
        Event result = eventService.update(updatedEvent, 1L);
        assertNotNull(result);
        assertEquals("Updated Event", result.getName());
        assertEquals(LocalDate.parse("2025-12-27"), result.getDate());
        assertEquals(LocalTime.parse("11:30"), result.getTime());
        assertEquals(updatedVenue, result.getVenue());
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }
    
    @Test
    public void testExistsByIdTrue() throws Exception {
        when(eventRepository.existsById(1L)).thenReturn(true);
        assertTrue(eventService.existsById(1L));
        verify(eventRepository).existsById(1L);
    }

    @Test
    public void testExistsByIdFalse() throws Exception {
        when(eventRepository.existsById(1L)).thenReturn(false);
        assertFalse(eventService.existsById(1L));
        verify(eventRepository).existsById(1L);
    }
    
    @Test
    public void testDelete() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        
        eventService.delete(event);
        
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        verify(eventRepository).delete(event);
    }
    
    @Test
    public void testDeleteById() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        
        eventService.deleteById(1L);
        
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        verify(eventRepository).deleteById(1L);
    
    }
    
    @Test
    public void testDeleteAllNoParam() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        Event eventTwo = new Event();
        eventTwo.setName(eventName);
        eventTwo.setDate(LocalDate.parse(eventDate));
        eventTwo.setTime(LocalTime.parse(eventTime));
        eventTwo.setVenue(venue);
       
        eventService.save(event);
        eventService.save(eventTwo);
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        eventService.deleteAll();
        
        result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        verify(eventRepository).deleteAll();
    }
    
    @Test
    public void testDeleteAll() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        Event eventTwo = new Event();
        eventTwo.setName(eventName);
        eventTwo.setDate(LocalDate.parse(eventDate));
        eventTwo.setTime(LocalTime.parse(eventTime));
        eventTwo.setVenue(venue);
        
        List<Event> events = Arrays.asList(event, eventTwo);
        eventService.deleteAll(events);
        
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        verify(eventRepository).deleteAll(events);
    }
    
    @Test
    public void testDeleteAllById() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        
        Event eventTwo = new Event();
        eventTwo.setName(eventName);
        eventTwo.setDate(LocalDate.parse(eventDate));
        eventTwo.setTime(LocalTime.parse(eventTime));
        eventTwo.setVenue(venue);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(eventTwo));
        
        List<Long> ids = Arrays.asList(1L, 2L);
        eventService.deleteAllById(ids);
        
        Iterable<Event> result = eventService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Event>) result).size());
        
        verify(eventRepository).deleteAllById(ids);
    }
    
    @Test 
    public void testFindAllOrderedByDateAndName() throws Exception {
        
    	List<Event> events = Arrays.asList(new Event(), new Event());
        when(eventRepository.findAllByOrderByDateAscTimeAsc()).thenReturn(events);
        
        List<Event> result = eventRepository.findAllByOrderByDateAscTimeAsc();
        assertNotNull(result);
        assertEquals(2, ((List<Event>) result).size());
        verify(eventRepository).findAllByOrderByDateAscTimeAsc();
    }
    
    @Test
    public void testExistsByVenueIdTrue() throws Exception {
        when(eventRepository.existsByVenueId(1L)).thenReturn(true);
        assertTrue(eventService.existsByVenueId(1L));
        verify(eventRepository).existsByVenueId(1L);
    }

    @Test
    public void testExistsByVenueIdFalse() throws Exception {
        when(eventRepository.existsByVenueId(2L)).thenReturn(false);
        assertFalse(eventService.existsByVenueId(2L));
        verify(eventRepository).existsByVenueId(2L);
    }
    
    
    
}
