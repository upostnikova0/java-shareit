package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.service.Create;
import ru.practicum.shareit.user.service.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым.")
    private String name;
    @Email(groups = {Update.class, Create.class}, message = "Почтовый адрес не валидный.")
    @NotEmpty(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    private String email;
}
