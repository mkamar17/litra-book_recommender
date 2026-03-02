package uk.ac.rhul.cs3821.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * BookComment model for the social experience - users can interact with other readers.
 */
@Entity
@Table(name = "book_comments")
@Getter
@Setter
public class BookComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private BookComment parentComment;  // null = top-level, non-null = reply

  @OneToMany(mappedBy = "parentComment")
  private List<BookComment> replies = new ArrayList<>();

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  private boolean deleted = false;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
