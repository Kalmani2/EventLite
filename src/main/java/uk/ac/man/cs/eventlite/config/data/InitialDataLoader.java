package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

    private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Initialize venues if not already populated
            if (venueService.count() > 0) {
                log.info("Database already populated with venues. Skipping venue initialization.");
            } else {
                String[] venueNames = {"Old Trafford", "Venue A", "Venue B"};
                int[] venueCapacities = {75000, 100, 100};

                for (int i = 0; i < venueNames.length; i++) {
                    Venue venue = new Venue();
                    // Assuming IDs are managed manually here:
                    venue.setId(i + 1);
                    venue.setName(venueNames[i]);
                    venue.setCapacity(venueCapacities[i]);
                    venueService.save(venue);
                }
            }

            // Initialize events if not already populated
            if (eventService.count() > 0) {
                log.info("Database already populated with events. Skipping event initialization.");
            } else {
                // Retrieve the "Old Trafford" venue (assumed to have ID 1)
                Venue oldTrafford = venueService.findById(1);
                if (oldTrafford == null) {
                    log.error("Old Trafford venue not found. Creating new instance.");
                    oldTrafford = new Venue();
                    oldTrafford.setId(1);
                    oldTrafford.setName("Old Trafford");
                    oldTrafford.setCapacity(75000);
                    venueService.save(oldTrafford);
                }

                // Create first event
                Event concert1 = new Event();
                concert1.setId(1);
                concert1.setDate(LocalDate.parse("2025-01-01"));
                concert1.setTime(LocalTime.parse("08:00"));
                concert1.setName("Concert 1");
                concert1.setVenue(oldTrafford);
                eventService.save(concert1);

                // Create second event
                Event concert2 = new Event();
                concert2.setId(2);
                concert2.setDate(LocalDate.parse("2025-08-08"));
                concert2.setTime(LocalTime.parse("08:00"));
                concert2.setName("Concert 2");
                concert2.setVenue(oldTrafford);
                eventService.save(concert2);
            }
        };
    }
}
