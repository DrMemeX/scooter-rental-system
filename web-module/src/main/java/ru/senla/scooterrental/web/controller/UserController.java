package ru.senla.scooterrental.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.user.dto.UserCreateRequest;
import ru.senla.scooterrental.user.dto.UserResponse;
import ru.senla.scooterrental.user.facade.UserFacade;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody UserCreateRequest request) {
        return userFacade.registerUser(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable("id") Long id) {
        return userFacade.getById(id);
    }
}