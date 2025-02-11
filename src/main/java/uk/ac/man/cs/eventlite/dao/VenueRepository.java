package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
