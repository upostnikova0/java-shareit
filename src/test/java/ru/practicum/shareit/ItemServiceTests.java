//package ru.practicum.shareit;
//
//import org.junit.jupiter.api.*;
//import ru.practicum.shareit.item.model.ItemDto;
//import ru.practicum.shareit.item.service.ItemServiceImpl;
//import ru.practicum.shareit.item.storage.InMemoryItemStorage;
//import ru.practicum.shareit.user.User;
//import ru.practicum.shareit.user.UserMapper;
//import ru.practicum.shareit.user.service.UserServiceImpl;
//
//import javax.validation.ValidationException;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class ItemServiceTests {
//    InMemoryItemStorage inMemoryItemStorage = new InMemoryItemStorage();
//    InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();
//    UserServiceImpl userServiceImpl = new UserServiceImpl(inMemoryUserStorage);
//    ItemServiceImpl itemServiceImpl = new ItemServiceImpl(inMemoryItemStorage, userServiceImpl);
//    User user = new User(1L, "Vasya", "vasya@mail.ru");
//
//    @BeforeAll
//    void addUser() {
//        userServiceImpl.create(UserMapper.toUserDto(user));
//    }
//
//    @BeforeEach
//    void clean() {
//        itemServiceImpl.getAll().clear();
//
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenNameEqualsNull() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            ItemDto itemDto = new ItemDto(1L, null, "Item1 Description", true);
//            itemServiceImpl.create(1L, itemDto);
//        });
//
//        Assertions.assertEquals("Поле name не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenNameIsBlank() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            ItemDto itemDto = new ItemDto(1L, "   ", "Item1 Description", true);
//            itemServiceImpl.create(1L, itemDto);
//        });
//
//        Assertions.assertEquals("Поле name не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenDescriptionEqualsNull() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            ItemDto itemDto = new ItemDto(1L, "Item Name", null, true);
//            itemServiceImpl.create(1L, itemDto);
//        });
//
//        Assertions.assertEquals("Поле description не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenDescriptionIsBlank() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            ItemDto itemDto = new ItemDto(1L, "Item Name", "   ", true);
//            itemServiceImpl.create(1L, itemDto);
//        });
//
//        Assertions.assertEquals("Поле description не может быть пустым и содержать пробелы.", thrown.getMessage());
//    }
//
//    @Test
//    void isBodyValid_shouldReturnExceptionWhenAvailableEqualsNull() {
//        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> {
//            ItemDto itemDto = new ItemDto(1L, "Item Name", "Item Description", null);
//            itemServiceImpl.create(1L, itemDto);
//        });
//
//        Assertions.assertEquals("Поле available отсутствует.", thrown.getMessage());
//    }
//}
