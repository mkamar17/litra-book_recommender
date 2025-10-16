package uk.ac.rhul.cs3821.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalId;   // Google volumeId
    private String title;
    private String author;

    @Column(length = 4000)
    private String description;

    private String coverUrl;
    private String genre;
    private String source;       // "google_books"

}

