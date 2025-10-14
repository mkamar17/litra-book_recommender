package uk.ac.rhul.cs3821.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.rhul.cs3821.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
