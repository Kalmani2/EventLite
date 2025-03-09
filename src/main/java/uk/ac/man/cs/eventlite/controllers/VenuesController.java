package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.List;

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
        List<Venue> filteredVenues = venueService.findByNameContainingIgnoreCase(query);
        model.addAttribute("venues", filteredVenues);
        return "venues/index";
    }
}