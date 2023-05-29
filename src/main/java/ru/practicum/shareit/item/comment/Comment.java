package ru.practicum.shareit.item.comment;

import lombok.*;
import lombok.experimental.NonFinal;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @NonFinal
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @NotNull
    @Column(name = "text", nullable = false)
    private String text;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @NotNull
    @OneToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @NotNull
    @Column(name = "created_date", nullable = false)
    private LocalDateTime created;
}
