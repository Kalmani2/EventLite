package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import java.util.List;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Optional<Event> findById(long id);
	
	public Event save(Event event);
	
	public Event update(Event event, long id);

	public boolean existsById(long id);

	public Event addEvent(Event event);
	
	public void delete(Event event);

	public void deleteById(long id);

	public void deleteAll();

	public void deleteAll(Iterable<Event> greetings);

	public void deleteAllById(Iterable<Long> ids);
	
//	public void deleteAll();
//
//	public void deleteAll(Iterable<Event> events);

	public List<Event> findAllOrderedByDateAndName();

}
