package com.ikram.hotel.controller;

import com.ikram.hotel.exception.PhotoRetrievalException;
import com.ikram.hotel.exception.ResourceNotFoundException;
import com.ikram.hotel.model.BookedRoom;
import com.ikram.hotel.model.Room;
import com.ikram.hotel.response.BookingResponse;
import com.ikram.hotel.response.RoomResponse;
import com.ikram.hotel.service.BookingRoomService;
import com.ikram.hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {

    private final IRoomService roomService;
    private final BookingRoomService bookingRoomService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(@RequestParam("photo") MultipartFile photo,
                                                   @RequestParam("roomType") String roomType,
                                                   @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo,roomType,roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(),savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/room-types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }

    @Transactional(readOnly = true)
    @GetMapping("/all")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = rooms.stream()
                .map(room -> {
                    try {
                        return getRoomResponse(room);
                    } catch (SQLException e) {
                        throw new PhotoRetrievalException("Error retrieving room data");
                    }
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(roomResponses);
    }

    private RoomResponse getRoomResponse(Room room) throws SQLException {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());

        List<BookingResponse> bookingInfo = bookings != null ? bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getBooking_id(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmationCode()))
                .collect(Collectors.toList()) : null;

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();

        if (photoBlob != null) {
            try (InputStream inputStream = photoBlob.getBinaryStream()) {
                if (inputStream != null) {
                    photoBytes = inputStream.readAllBytes();
                }
            } catch (IOException | SQLException e) {
                System.err.println("Erreur photo sur room ID: " + room.getId());
                e.printStackTrace(); // pour debug
                photoBytes = null;   // ignorer la photo cassée
            }
        }


        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes,
                bookingInfo);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long id) {
        return bookingRoomService.getAllBookingsByRoomId(id);
    }

    @GetMapping("/{roomId}")
    public List<BookedRoom> getAllRoomPhotosById(@PathVariable Long roomId){
        return bookingRoomService.getAllBookingsByRoomId(roomId);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes = photo != null && !photo.isEmpty() ? photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.updateRoom(roomId,roomType,roomPrice,photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse response = getRoomResponse(theRoom);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId){
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse response = null;
            try {
                response = getRoomResponse(room);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(Optional.of(response));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }
}
