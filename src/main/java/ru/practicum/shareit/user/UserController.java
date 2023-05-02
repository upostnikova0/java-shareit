package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.Create;
import ru.practicum.shareit.user.service.Update;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final Create create;
    private final Update update;

    @Autowired
    public UserController(UserService userService, Create create, Update update) {
        this.userService = userService;
        this.create = create;
        this.update = update;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Validated(Create.class) UserDto userDto) {
        return new ResponseEntity<>(create.create(userDto), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
        return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAll() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@RequestBody @Validated(Update.class) UserDto userDto, @PathVariable long id) {
        return new ResponseEntity<>(update.update(userDto, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.delete(id);
    }
}
