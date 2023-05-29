//package ru.practicum.shareit;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.practicum.shareit.item.ItemRepository;
//import ru.practicum.shareit.user.UserDto;
//import ru.practicum.shareit.user.service.UserServiceImpl;
//import ru.practicum.shareit.user.UserRepository;
//
//import javax.validation.ValidationException;
//
//public class UserServiceTests {
//    UserRepository userRepository;
//    ItemRepository itemRepository;
//    UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository, itemRepository);
//
//    @BeforeEach
//    void clean() {
//        userServiceImpl.getAll().clear();
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenEmailIsNotValid() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, "Vasya", "vasya.mail.ru");
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenEmailIsEmpty() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, "Vasya", "");
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenEmailIsBlank() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, "Vasya", "   ");
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenEmailEqualsNull() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, "Vasya", null);
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenNameEqualsNull() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, null, "vasya@mail.ru");
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Имя не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenNameIsBlank() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            UserDto userDto1 = new UserDto(1L, "   ", "vasya@mail.ru");
//            userServiceImpl.create(userDto1);
//        });
//
//        Assertions.assertEquals("Имя не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//}
