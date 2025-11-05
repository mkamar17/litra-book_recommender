package uk.ac.rhul.cs3821.dto;

import java.util.List;

/**
 * Data transfer object representing the response structure returned by the
 * Google Books API search endpoint. This DTO maps only the subset of fields
 * required by the application, including book metadata and available image
 * links.
 */

public class GoogleBooksDto {
  public List<Item> items;

  /**
   * Represents a single search result within the Google Books API response.
   * Contains the book's unique identifier and associated volume information.
   */

  public static class Item {
    public String id;
    public VolumeInfo volumeInfo;
  }

  /**
   * Contains descriptive metadata about a particular book volume. This includes
   * bibliographic information such as title, authors, description, categories,
   * and available image links.
   */

  public static class VolumeInfo {
    public String title;
    public List<String> authors;
    public String description;
    public List<String> categories;
    public ImageLinks imageLinks;
  }

  /**
   * Represents available thumbnail images for a book cover as provided by the
   * Google Books API.
   */
  
  public static class ImageLinks {
    public String thumbnail;
    public String smallThumbnail;
  }
}