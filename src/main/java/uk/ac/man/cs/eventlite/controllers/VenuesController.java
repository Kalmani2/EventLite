package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.VenueService;

@Controller
@RequestMapping("/venues")
public class VenuesController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public String getVenues(Model model) {
        model.addAttribute("venues", venueService.findAll());
        return "venues/index";
    }


}