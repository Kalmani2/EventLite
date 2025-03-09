package uk.ac.man.cs.eventlite.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}

	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}

	@Override
	public Venue findById(long id) {
		return venueRepository.findById(id).orElse(null);
	}

	@Override
    public List<Venue> findByNameContainingIgnoreCase(String name) {
        return venueRepository.findByNameContainingIgnoreCase(name);
    }

}
