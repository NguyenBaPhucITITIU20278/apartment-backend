package com.repository;

import com.model.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> { 
   boolean existsByAddress(String address);
   @Query(value = "SELECT * FROM room WHERE LOWER(address) = LOWER(?1)", nativeQuery = true)
   List<Room> findByAddress(String address);
   List<Room> findByNumberOfBedrooms(Integer numberOfBedrooms);
   @Query(value = "SELECT DISTINCT r.* FROM room r WHERE " +
          "LOWER(r.address) LIKE LOWER(CONCAT('%', ?1, '%')) AND " +
          "r.number_of_bedroom = ?2", nativeQuery = true)
   List<Room> findByAddressAndNumberOfBedrooms(String address, Integer numberOfBedrooms);
   @Query(value = "SELECT DISTINCT r.* FROM room r WHERE " +
          "LOWER(r.address) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
          "LOWER(r.address) LIKE LOWER(CONCAT(?1, '%')) OR " +
          "LOWER(r.address) LIKE LOWER(CONCAT('% ', ?1, '%'))", nativeQuery = true)
   List<Room> findByAddressStartingWith(String prefix);
   List<Room> findByUsername(String username);
   boolean existsByPaymentId(String paymentId);
}