package com.model;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class RoomRequest {
    private String address;
    private String id;
    private Integer numberOfBedrooms;

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getNumberOfBedrooms() {
        return numberOfBedrooms;
    }

    public void setNumberOfBedrooms(Integer numberOfBedrooms) {
        this.numberOfBedrooms = numberOfBedrooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}