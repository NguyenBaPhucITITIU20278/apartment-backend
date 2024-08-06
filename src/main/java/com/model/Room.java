package com.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Data
@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private String price;

    @Column(name = "status")
    private String status;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "number_of_bedroom")
    private String numberOfBedrooms;

    @Column(name = "description")
    private String description;

    @Column(name = "phone_number")
    private String phoneNumber;



}
