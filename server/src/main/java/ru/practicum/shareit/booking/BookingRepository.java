package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerOrderByStartDesc(User booker, PageRequest pageRequest);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime now, LocalDateTime now1, PageRequest pageRequest);

    List<Booking> findAllByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime now, PageRequest pageRequest);

    List<Booking> findAllByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime now, PageRequest pageRequest);

    List<Booking> findAllByBookerAndStatusEqualsOrderByStartDesc(User booker, Status status, PageRequest pageRequest);

    List<Booking> findAllByItemOwnerOrderByStartDesc(User owner, PageRequest pageRequest);

    List<Booking> findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner, LocalDateTime now, LocalDateTime now1, PageRequest pageRequest);

    List<Booking> findAllByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime now, PageRequest pageRequest);

    List<Booking> findAllByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime now, PageRequest pageRequest);

    List<Booking> findAllByItemOwnerAndStatusEqualsOrderByStartDesc(User owner, Status status, PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(Long bookerId, Long itemId, Status status, LocalDateTime end);

    @Query(value = "select * from bookings where item_id = ?1 and status = 'APPROVED' " +
            "and start_date < ?2 order by start_date desc limit 1", nativeQuery = true)
    Optional<Booking> findLastItem(Long itemId, LocalDateTime now);

    @Query(value = "select * from bookings where item_id = ?1 and status = 'APPROVED' " +
            "and start_date > ?2 order by start_date limit 1", nativeQuery = true)
    Optional<Booking> findNextItem(Long itemId, LocalDateTime now);
}
