package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();

	public Event save(Event event);

	public Event addEvent(Event event);
	
	public boolean existsById(long id);
	
	public void delete(Event event);

	public void deleteById(long id);

	public void deleteAll();

	public void deleteAll(Iterable<Event> greetings);

	public void deleteAllById(Iterable<Long> ids);
	
//	public void deleteAll();
//
//	public void deleteAll(Iterable<Event> events);

}
