package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerAddTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService;

    // Test that a valid venue is successfully added.
    @Test
    public void testAddVenueSuccess() throws Exception {
        Venue venue = new Venue();
        venue.setName("New Venue");
        venue.setAddress("123 Main Street, AB1 2CD");
        venue.setCapacity(500);

        when(venueService.save(any(Venue.class))).thenReturn(venue);

        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", "123 Main Street, AB1 2CD")
                .param("capacity", "500")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));

        verify(venueService).save(any(Venue.class));
    }

    // Test missing required field "name"
    @Test
    public void testAddVenueMissingName() throws Exception {
        mvc.perform(post("/venues")
                .param("address", "123 Main Street, AB1 2CD")
                .param("capacity", "500")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    // Test missing required field "address"
    @Test
    public void testAddVenueMissingAddress() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("capacity", "500")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    // Test missing required field "capacity"
    @Test
    public void testAddVenueMissingCapacity() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", "123 Main Street, AB1 2CD")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    // Test invalid capacity (non-numeric)
    @Test
    public void testAddVenueInvalidCapacityNonNumeric() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", "123 Main Street, AB1 2CD")
                .param("capacity", "abc")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    // Test invalid capacity (non-positive value)
    @Test
    public void testAddVenueInvalidCapacityNonPositive() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", "123 Main Street, AB1 2CD")
                .param("capacity", "0")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    // Test address missing postcode: input has no comma/postcode, e.g. just "M1
    // 7N3"
    @Test
    public void testAddVenueAddressMissingPostcode() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", "M1 7N3") // Only postcode, no road address and no comma
                .param("capacity", "500")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError())
                // Optionally check for a specific error message if your view exposes it.
                .andExpect(model().attributeHasFieldErrorCode("venue", "address", "ValidAddress"));
    }

    // Test address missing road part: input like " , M1 7N3"
    @Test
    public void testAddVenueAddressMissingRoadAddress() throws Exception {
        mvc.perform(post("/venues")
                .param("name", "New Venue")
                .param("address", ", M1 7N3") // Comma present, but no road address before it
                .param("capacity", "500")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError())
                // Optionally check for a specific error message if exposed
                .andExpect(model().attributeHasFieldErrorCode("venue", "address", "ValidAddress"));
    }
}