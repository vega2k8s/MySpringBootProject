package com.basic.myspringboot.controller;

import com.basic.myspringboot.controller.dto.UserDTO;
import com.basic.myspringboot.entity.User;
import com.basic.myspringboot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserServiceController {
    private final UserService userService;

    @PostMapping
    public UserDTO.UserResponse create(@Valid @RequestBody
                                           UserDTO.UserCreateRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User savedUser = userService.createUser(user);
        return new UserDTO.UserResponse(savedUser);
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

}
