package com.example.ewalllet.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/create_user")
    public ResponseEntity createUser(@RequestBody UserRequestDto userRequestDto){
        String response = userService.saveUser(userRequestDto);
        return new ResponseEntity(response, HttpStatus.CREATED);
    }

    @GetMapping("/get_user/{username}")
    public ResponseEntity getUser(@PathVariable String username){

        UserResponseDto user = userService.findByUserName(username);
        return new ResponseEntity(user,HttpStatus.OK);
    }
}
