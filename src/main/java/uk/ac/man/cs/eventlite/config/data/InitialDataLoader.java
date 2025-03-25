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
                String[] venueNames = { "Old Trafford", "Kilburn Building", "Crawford House" };
                int[] venueCapacities = { 75000, 100, 100 };
                String[] venueAddressess = {"Stretford, Manchester M16 0RA", "Oxford Rd, Manchester M13 9PL", "Booth St E, Manchester M13 9SS"};
                float[] latitudes = { 53.4631f, 53.4675f, 53.4685f };
                float[] longitudes = { -2.2913f, -2.2340f, -2.2348f };


                for (int i = 0; i < venueNames.length; i++) {
                    Venue venue = new Venue();
                    // Assuming IDs are managed manually here:
                    venue.setId(i + 1);
                    venue.setName(venueNames[i]);
                    venue.setAddress(venueAddressess[i]);                    
                    venue.setCapacity(venueCapacities[i]);
                    venue.setLatitude(latitudes[i]);
                    venue.setLongitude(longitudes[i]);
                    venueService.save(venue);
                }
            }

            // Initialize events if not already populated
            if (eventService.count() > 0) {
                log.info("Database already populated with events. Skipping event initialization.");
            } else {
                // Retrieve the "Old Trafford" venue (assumed to have ID 1)
                // Venue oldTrafford = venueService.findById(1);
                // if (oldTrafford == null) {
                // log.error("Old Trafford venue not found. Creating new instance.");
                // oldTrafford = new Venue();
                // oldTrafford.setId(1);
                // oldTrafford.setName("Old Trafford");
                // oldTrafford.setCapacity(75000);
                // venueService.save(oldTrafford);
                // }

                Venue oldTrafford = venueService.findById(1);
                Venue kilburn = venueService.findById(2);
                Venue crawford = venueService.findById(3);

                String[] eventNames = { "Concert1", "Event Alpha", "Beta", "Apple", "Former", "Previous", "Past" };
                String[] eventDate = { "2025-01-01", "2025-07-11", "2025-07-11", "2025-07-12", "2025-01-11",
                        "2025-01-11", "2025-01-10" };
                String[] eventTime = { "08:00", "12:30", "10:00", null, "11:00", "18:30", "17:00" };
                Venue[] eventVenue = { oldTrafford, crawford, kilburn, kilburn, crawford, kilburn, kilburn };

                for (int i = 0; i < eventNames.length; i++) {
                    Event event = new Event();
                    // Assuming IDs are managed manually here:
                    event.setName(eventNames[i]);
                    event.setDate(LocalDate.parse(eventDate[i]));
                    event.setTime(LocalTime.parse(eventTime[i]));
                    event.setVenue(eventVenue[i]);
                    eventService.save(event);
                }

                // Create first event
                // Event concert1 = new Event();
                // concert1.setId(1);
                // concert1.setDate(LocalDate.parse("2025-01-01"));
                // concert1.setTime(LocalTime.parse("08:00"));
                // concert1.setName("Concert 1");
                // concert1.setVenue(oldTrafford);
                // eventService.save(concert1);
                //
                // // Create second event
                // Event concert2 = new Event();
                // concert2.setId(2);
                // concert2.setDate(LocalDate.parse("2025-08-08"));
                // concert2.setTime(LocalTime.parse("08:00"));
                // concert2.setName("Concert 2");
                // concert2.setVenue(oldTrafford);
                // eventService.save(concert2);
            }
        };
    }
}
