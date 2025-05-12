package com.ikram.hotel.repository;

import com.ikram.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {

    @Query("select DISTINCT r.roomType from Room r")
    List<String> findDistinctRoomTypes();

}
