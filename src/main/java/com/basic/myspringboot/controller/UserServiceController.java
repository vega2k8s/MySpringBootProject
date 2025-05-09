package com.basic.myspringboot.controller;

import com.basic.myspringboot.controller.dto.UserDTO;
import com.basic.myspringboot.entity.User;
import com.basic.myspringboot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserServiceController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO.UserResponse> create(@Valid @RequestBody
                                           UserDTO.UserCreateRequest request) {
        UserDTO.UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping
    public List<UserDTO.UserResponse> getUsers() {
        return userService.getAllUsers()
                //List<User> => Stream<User>
                .stream()
                //User -> UserDTO.UserResponse
                .map(user -> new UserDTO.UserResponse(user))
                //.map(UserDTO.UserResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public UserDTO.UserResponse getUserById(@PathVariable Long id) {
        User existUser = userService.getUserById(id);
        return new UserDTO.UserResponse(existUser);
    }

    @GetMapping("/email/{email}/")
    public UserDTO.UserResponse getUserByEmail(@PathVariable String email){
        return new UserDTO.UserResponse(userService.getUserByEmail(email));
    }

    @PatchMapping("/{email}")
    public ResponseEntity<UserDTO.UserResponse> updateUser(@PathVariable String email,
                                           @Valid @RequestBody UserDTO.UserUpdateRequest useDetail){

        UserDTO.UserResponse updatedUser = userService.updateUserByEmail(email, useDetail);
        return ResponseEntity.ok(updatedUser);
    }


}
