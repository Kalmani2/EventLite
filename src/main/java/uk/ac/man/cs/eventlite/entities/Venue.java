package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    private int capacity;

    @Column(name = "road_address", nullable = true)
    private String roadAddress;

    @Column(name = "postcode", nullable = true)
    private String postcode;

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

    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
