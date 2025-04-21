package com.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Data
@Entity
@Table(name = "room", schema = "public")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Float price;

    @Column(name = "status")
    private String status;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "number_of_bedroom")
    private Integer numberOfBedrooms;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "posted_time")
    private LocalDateTime postedTime;

    @Column (name ="area")
    private Float area;

    @Column(name = "image_paths")
    private List<String> imagePaths;

    @Column(name = "model_path")
    private String modelPath;

    @Column(name = "web360_path")
    private String web360Path;

    private List<String> web360Paths;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "video_path")
    private String videoPath;

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getWeb360Path() {
        return web360Path;
    }

    public void setWeb360Path(String web360Path) {
        this.web360Path = web360Path;
    }

    public List<String> getWeb360Paths() {
        return web360Paths;
    }

    public void setWeb360Paths(List<String> web360Paths) {
        this.web360Paths = web360Paths;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

}