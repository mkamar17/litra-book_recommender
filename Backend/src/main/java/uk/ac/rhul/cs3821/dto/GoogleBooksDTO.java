package uk.ac.rhul.cs3821.dto;

import java.util.List;

public class GoogleBooksDTO {
    public List<Item> items;

    public static class Item {
        public String id;
        public VolumeInfo volumeInfo;
    }

    public static class VolumeInfo {
        public String title;
        public List<String> authors;
        public String description;
        public List<String> categories;
        public ImageLinks imageLinks;
    }

    public static class ImageLinks {
        public String thumbnail;
        public String smallThumbnail;
    }
}