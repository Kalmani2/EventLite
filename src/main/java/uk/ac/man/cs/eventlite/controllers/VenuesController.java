package uk.ac.man.cs.eventlite.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import jakarta.validation.Valid;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping("/venues")
public class VenuesController {

    // mapbox access token
    private static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1Ijoia2FsbWFuaS1tYW5jaGVzdGVyIiwiYSI6ImNtOHg5NG92dTAwd2wyaXM0YTRydWh4dzAifQ.cILrNVVAjfJub-a66Y4rOA";

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;
    
    private double[] geocodeAddress(String address) {
        try {
            MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(MAPBOX_ACCESS_TOKEN)
                .query(address)
                .build();
            
            Response<GeocodingResponse> response = mapboxGeocoding.executeCall();

            // added some debugging statements to see what went wrong (if it does)

            if (!response.isSuccessful()) {
                System.err.println("Geocoding API call failed with code: " + response.code());
                return null;
            }

            GeocodingResponse geocodingResponse = response.body();

            if (geocodingResponse == null || geocodingResponse.features().isEmpty()) {
                System.err.println("No geocoding results for address: " + address);
                return null;
            }

            CarmenFeature feature = geocodingResponse.features().get(0);
            Point point = feature.center();

            if (point != null) {

                Thread.sleep(1000L);
                
                System.out.println("Geocoding result: " + point.latitude() + ", " + point.longitude());
                return new double[] { point.longitude(), point.latitude() };
            } else {
                System.err.println("Geocoding feature returned no coordinates.");
            }
            
            Thread.sleep(1000L);

        } catch (IOException e) {
            System.err.println("Error geocoding address: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Interrupted while geocoding: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return null;
    }


    @GetMapping
    public String getAllVenues(Model model) {
        model.addAttribute("venues", venueService.findAll());
        return "venues/index";
    }

    @GetMapping("/search")
    public String searchVenues(@RequestParam("query") String query, Model model) {
        model.addAttribute("venues", venueService.findByNameContainingIgnoreCase(query));
        return "venues/index";
    }

    @GetMapping("/{id}/details")
    public String getVenueDetails(@PathVariable("id") long id, Model model) {
        Venue venue = venueService.findById(id);
        if (venue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        Iterable<Event> allEvents = eventService.findAll();

        List<Event> venueEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getVenue() != null && event.getVenue().getId() == id) {
                venueEvents.add(event);
            }
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Event> upcomingEvents = venueEvents.stream()
                .filter(event -> event.getDate().isAfter(today) ||
                        (event.getDate().isEqual(today) &&
                                (event.getTime() == null || event.getTime().isAfter(now))))
                .collect(Collectors.toList());

        upcomingEvents.sort((e1, e2) -> {
            if (e1.getDate().equals(e2.getDate())) {
                if (e1.getTime() == null || e2.getTime() == null) {
                    return 0;
                }
                return e1.getTime().compareTo(e2.getTime());
            }
            return e1.getDate().compareTo(e2.getDate());
        });

        model.addAttribute("venue", venue);
        model.addAttribute("upcomingEvents", upcomingEvents);
        return "venues/venue_details";
    }

    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
        if (!venueService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        if (eventService.existsByVenueId(id)) {
            redirectAttrs.addFlashAttribute("error_message", "Cannot delete venue with existing events.");
            return "redirect:/venues";
        }

        System.out.println("Deleting venue with ID: " + id); // Debug statement

        venueService.deleteById(id);

        System.out.println("Venue deleted."); // Debug statement

        redirectAttrs.addFlashAttribute("ok_message", "Venue deleted.");
        return "redirect:/venues";
    }

    @PutMapping("/{id}")
    public String updateVenue(@PathVariable("id") long id, @ModelAttribute Venue venue,
            RedirectAttributes redirectAttrs) {
        // Retrieve the existing venue
        Venue existingVenue = venueService.findById(id);
        if (existingVenue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        // Update the existing venue's properties
        existingVenue.setName(venue.getName());
        existingVenue.setAddress(venue.getAddress());
        existingVenue.setCapacity(venue.getCapacity());
        
        // Get and set coordinates if the address was changed
        if (!existingVenue.getAddress().equals(venue.getAddress())) {
            double[] coordinates = geocodeAddress(venue.getAddress());
            if (coordinates != null) {
                existingVenue.setLongitude(coordinates[0]);
                existingVenue.setLatitude(coordinates[1]);
            }
        }

        // Save the updated venue
        venueService.save(existingVenue);
        redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");
        return "redirect:/venues"; // Redirect to the venues index page
    }

    @GetMapping("/new_venue")
    public String addVenueForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/new_venue"; // Ensure this points to the correct Thymeleaf template
    }

    @PostMapping
    public String createVenue(@Valid @ModelAttribute Venue venue, BindingResult bindingResult,
            RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            return "venues/new_venue";
        }
        
        // Get coordinates from the address
        double[] coordinates = geocodeAddress(venue.getAddress());
        if (coordinates != null) {
            venue.setLongitude(coordinates[0]);
            venue.setLatitude(coordinates[1]);
        }
        
        venueService.save(venue);
        System.out.println("New Venue Created: " + venue.getId() + 
                           " with coordinates: " + venue.getLatitude() + ", " + venue.getLongitude());
        redirectAttrs.addFlashAttribute("ok_message", "Venue created successfully.");
        return "redirect:/venues";
    }

}
