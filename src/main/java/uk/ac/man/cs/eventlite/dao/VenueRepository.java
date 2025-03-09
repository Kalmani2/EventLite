package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Venue;
import java.util.List;

public interface VenueRepository extends CrudRepository<Venue, Long> {

    List<Venue> findByNameContainingIgnoreCase(String name);
}
