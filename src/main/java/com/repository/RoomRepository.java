package com.repository;

import com.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
   boolean existsByAddress(String address);
   Room findByAddress(String address);
}