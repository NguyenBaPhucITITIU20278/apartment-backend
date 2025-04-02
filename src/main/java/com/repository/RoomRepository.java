package com.repository;

import com.model.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> { 
   boolean existsByAddress(String address);
   List<Room> findByAddress(String address);
   List<Room> findByNumberOfBedrooms(Integer numberOfBedrooms);
   @Query(value = "SELECT * FROM room WHERE address = ?1 AND number_of_bedroom = ?2", nativeQuery = true)
   List<Room> findByAddressAndNumberOfBedrooms(String address, Integer numberOfBedrooms); 
   List<Room> findByAddressStartingWith(String prefix);
   List<Room> findByUsername(String username);
}