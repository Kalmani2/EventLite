package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping("/venues")
public class VenuesController {

    @Autowired
    private VenueService venueService;

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
        model.addAttribute("venue", venue);
        return "venues/venue_details";
    }
}
