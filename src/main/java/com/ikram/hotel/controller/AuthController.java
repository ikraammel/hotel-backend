package com.ikram.hotel.controller;

import com.ikram.hotel.exception.UserAlreadyExistsException;
import com.ikram.hotel.model.User;
import com.ikram.hotel.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(User user){
        try{
            userService.registerUser(user);
            return ResponseEntity.ok("Registration successful!");
        }catch (UserAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
