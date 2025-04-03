package uk.ac.man.cs.eventlite.dao;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")

public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;
	
	@MockBean
	private VenueRepository venueRepository;


	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	
    @Test
    public void testCount() throws Exception {        
    	when(venueRepository.count()).thenReturn(2L);
        assertEquals(2L, venueService.count());
    }
    
    @Test 
    public void testFindAll() throws Exception {
    	List<Venue> venues = Arrays.asList(new Venue(), new Venue());
        when(venueRepository.findAll()).thenReturn(venues);
        
        Iterable<Venue> result = venueService.findAll();
        assertNotNull(result);
        assertEquals(2, ((List<Venue>) result).size());
        verify(venueRepository).findAll();
    }
    
    @Test
    public void testSave() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        when(venueRepository.save(venue)).thenReturn(venue);
        
        Venue result = venueService.save(venue);
        assertNotNull(result);
        assertEquals("Test Venue", result.getName());
        verify(venueRepository).save(venue);
    }
    
    @Test
    public void testFindByIdExists() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        
        Venue result = venueService.findById(1);
        assertNotNull(result);
        assertEquals("Test Venue", result.getName());
        verify(venueRepository).findById(1L);
    }

    @Test
    public void testFindByIdNotExists() throws Exception {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        
        when(venueRepository.findById(2L)).thenReturn(Optional.empty());
        
        Venue result = venueService.findById(2L);
        assertNull(result);
        verify(venueRepository).findById(2L);
    }
    
    @Test
    public void testFindByNameContainingIgnoreCase() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        
        Venue venueTwo = new Venue();
        venueTwo.setId(2);
        venueTwo.setName("Test Venue");
        venueTwo.setCapacity(100);
        
        List<Venue> venues = Arrays.asList(venue, venueTwo);
	
        when(venueRepository.findByNameContainingIgnoreCase(venue.getName())).thenReturn(venues);
        
        List<Venue> result = venueService.findByNameContainingIgnoreCase(venue.getName());
        assertNotNull(result);
        assertEquals(2, ((List<Venue>) result).size());
        verify(venueRepository).findByNameContainingIgnoreCase(venue.getName());
    }
	
    @Test
    public void testExistsByIdTrue() throws Exception {
        when(venueRepository.existsById(1L)).thenReturn(true);
        assertTrue(venueService.existsById(1L));
        verify(venueRepository).existsById(1L);
    }

    @Test
    public void testExistsByIdFalse() throws Exception {
        when(venueRepository.existsById(1L)).thenReturn(false);
        assertFalse(venueService.existsById(1L));
        verify(venueRepository).existsById(1L);
    }
    
    @Test
    public void testDeleteById() throws Exception {
    	Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        
        venueService.deleteById(1L);
        
        Iterable<Venue> result = venueService.findAll();
        assertNotNull(result);
        assertEquals(0, ((List<Venue>) result).size());
        
        verify(venueRepository).deleteById(1L);
    
    }
	
	
}
