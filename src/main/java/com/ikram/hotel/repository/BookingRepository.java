package com.ikram.hotel.repository;

import com.ikram.hotel.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookedRoom,Long> {
    BookedRoom findByBookingConfirmationCode(String confirmationCode);
}
