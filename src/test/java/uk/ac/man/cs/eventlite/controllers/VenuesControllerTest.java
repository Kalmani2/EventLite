package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

//using ArgumentCaptor to capture the venue object passed to save
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService; // used in deleteVenue

    @SpyBean
    private VenuesController venuesController;

    private Venue testVenue1;
    private Venue testVenue2;

    // creds for authenticated requests
    private final String USERNAME = "Markel";
    private final String PASSWORD = "Vigo";

    @BeforeEach
    public void setUp() {
        testVenue1 = new Venue();
        testVenue1.setId(1L);
        testVenue1.setName("Kilburn Building");
        testVenue1.setCapacity(1000);
        testVenue1.setAddress("Oxford Rd, Manchester, M13 9PL");
        testVenue1.setLatitude(53.467495);
        testVenue1.setLongitude(-2.234009);

        testVenue2 = new Venue();
        testVenue2.setId(2L);
        testVenue2.setName("University Place");
        testVenue2.setCapacity(500);
        testVenue2.setAddress("Oxford Rd, Manchester, M13 9PL"); // Same address for simplicity
        testVenue2.setLatitude(53.467495);
        testVenue2.setLongitude(-2.234009);
    }

    @Test
    public void testGetAllVenuesWithResults() throws Exception {
        List<Venue> venues = Arrays.asList(testVenue1, testVenue2);
        when(venueService.findAll()).thenReturn(venues);

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", hasSize(2)))
                .andExpect(model().attribute("venues", containsInAnyOrder(
                        hasProperty("name", is("Kilburn Building")),
                        hasProperty("name", is("University Place")))));

        verify(venueService).findAll();
    }

    // --- Tests for deleteVenue ---

    @Test
    public void testDeleteVenueSuccess() throws Exception {
        long venueId = 1L;
        when(venueService.existsById(venueId)).thenReturn(true);
        when(eventService.existsByVenueId(venueId)).thenReturn(false); // No events associated

        mvc.perform(delete("/venues/{id}", venueId)
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE)) // Authenticate
                .with(csrf()) // Include CSRF token
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound()) // Expecting a redirect (302 Found)
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attribute("ok_message", "Venue deleted."));

        verify(venueService).existsById(venueId);
        verify(eventService).existsByVenueId(venueId);
        verify(venueService).deleteById(venueId);
    }

    @Test
    public void testDeleteVenueNotFound() throws Exception {
        long venueId = 99L;
        when(venueService.existsById(venueId)).thenReturn(false);

        mvc.perform(delete("/venues/{id}", venueId)
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound()); // Expecting 404 Not Found

        verify(venueService).existsById(venueId);
        verify(eventService, never()).existsByVenueId(anyLong()); // Should not check for events if venue doesn't exist
        verify(venueService, never()).deleteById(anyLong()); // Should not attempt delete
    }

    @Test
    public void testDeleteVenueWithEvents() throws Exception {
        long venueId = 1L;
        when(venueService.existsById(venueId)).thenReturn(true);
        when(eventService.existsByVenueId(venueId)).thenReturn(true); // Venue HAS events

        mvc.perform(delete("/venues/{id}", venueId)
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attribute("error_message", "Cannot delete venue with existing events."));

        verify(venueService).existsById(venueId);
        verify(eventService).existsByVenueId(venueId);
        verify(venueService, never()).deleteById(anyLong()); // Should not delete
    }

    // --- Tests for updateVenue ---

    @Test
    public void testUpdateVenueSuccessAddressUnchanged() throws Exception {
        long venueId = 1L;
        String updatedName = "Kilburn Building Updated";
        int updatedCapacity = 1200;

        when(venueService.findById(venueId)).thenReturn(testVenue1); // Return the original venue

        // Prepare form data
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", updatedName);
        params.add("capacity", String.valueOf(updatedCapacity));
        params.add("address", testVenue1.getAddress()); // Address remains the same

        mvc.perform(put("/venues/{id}", venueId)
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attribute("ok_message", "Venue updated."));

        // Capture the venue passed to save
        ArgumentCaptor<Venue> venueCaptor = ArgumentCaptor.forClass(Venue.class);
        verify(venueService).findById(venueId);
        verify(venueService).save(venueCaptor.capture());

        // Assertions on the captured venue
        Venue savedVenue = venueCaptor.getValue();
        assertNotNull(savedVenue);
        assert (savedVenue.getId() == venueId); // ID should not change
        assert (savedVenue.getName().equals(updatedName));
        assert (savedVenue.getCapacity() == updatedCapacity);
        assert (savedVenue.getAddress().equals(testVenue1.getAddress())); // Address is the same
        // Coordinates should remain the original ones as address didn't change
        assert (savedVenue.getLatitude().equals(testVenue1.getLatitude()));
        assert (savedVenue.getLongitude().equals(testVenue1.getLongitude()));
    }

    @Test
    public void testUpdateVenueNotFound() throws Exception {
        long venueId = 99L;
        when(venueService.findById(venueId)).thenReturn(null); // Venue does not exist

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Doesn't Matter");
        params.add("capacity", "100");
        params.add("address", "Any Address");

        mvc.perform(put("/venues/{id}", venueId)
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());

        verify(venueService).findById(venueId);
        verify(venueService, never()).save(any(Venue.class)); // Save should not be called
    }

    // --- Tests for addVenueForm (GET /venues/new_venue) ---

    @Test
    public void testAddVenueForm() throws Exception {
        mvc.perform(get("/venues/new_venue")
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE)) // Requires auth
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/new_venue"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", instanceOf(Venue.class)));
    }

    // --- Tests for createVenue ---

    @Test
    public void testCreateVenueSuccess() throws Exception {
        String newName = "New Test Venue";
        int newCapacity = 50;
        String newAddress = "Kilburn Building, Oxford Road, M13 9PL";
        ;

        // Prepare form data
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", newName);
        params.add("capacity", String.valueOf(newCapacity));
        params.add("address", newAddress);

        // Mock the save operation to simulate ID generation
        when(venueService.save(any(Venue.class))).thenAnswer(invocation -> {
            Venue venueToSave = invocation.getArgument(0);
            venueToSave.setId(100L); // Simulate setting an ID
            // Simulate potential coordinate setting by geocodeAddress
            if (venueToSave.getAddress().equals(newAddress)) {
                // Assume geocoding succeeded for this test case
                venueToSave.setLatitude(50.0);
                venueToSave.setLongitude(-1.0);
            }
            return venueToSave;
        });

        mvc.perform(post("/venues")
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attribute("ok_message", "Venue created successfully."));

        // Capture the venue passed to save
        ArgumentCaptor<Venue> venueCaptor = ArgumentCaptor.forClass(Venue.class);
        verify(venueService).save(venueCaptor.capture());

        // Assertions on the captured venue
        Venue savedVenue = venueCaptor.getValue();
        assertNotNull(savedVenue);
        assert (savedVenue.getName().equals(newName));
        assert (savedVenue.getCapacity() == newCapacity);
        assert (savedVenue.getAddress().equals(newAddress));
        // Verify coordinates were set (simulated in the mock above)
        assertNotNull(savedVenue.getLatitude());
        assertNotNull(savedVenue.getLongitude());
        assert (savedVenue.getLatitude() == 50.0);
        assert (savedVenue.getLongitude() == -1.0);
    }

    @Test
    public void testCreateVenueValidationError() throws Exception {
        // Prepare form data with invalid input (e.g., name exceeds max size)
        String longName = "V".repeat(257); // @Size(max=256)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", longName);
        params.add("capacity", "50");
        params.add("address", "Valid Address, Postcode"); // Assuming this passes @ValidAddress

        mvc.perform(post("/venues")
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk()) // Validation error, return to form
                .andExpect(view().name("venues/new_venue"))
                .andExpect(model().attributeHasFieldErrors("venue", "name")) // Check for error on 'name' field
                .andExpect(model().attributeExists("venue")); // Model should still contain the venue object

        verify(venueService, never()).save(any(Venue.class)); // Save should not be called on validation failure
    }
}