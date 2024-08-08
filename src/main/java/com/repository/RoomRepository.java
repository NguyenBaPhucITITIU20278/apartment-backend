package com.repository;

import com.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> { // Đảm bảo kiểu ID khớp với entity Room
   boolean existsByAddress(String address);
   Room findByAddress(String address);

   @Query(value = "SELECT * FROM room WHERE address = ?1 AND number_of_bedroom = ?2", nativeQuery = true)
   Room findByAddressAndNumberOfBedrooms(String address, int numberOfBedrooms); // Đổi kiểu dữ liệu tham số
}