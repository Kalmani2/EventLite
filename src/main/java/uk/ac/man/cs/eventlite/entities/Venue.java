package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*;;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
