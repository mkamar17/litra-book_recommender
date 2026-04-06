package uk.ac.rhul.cs3821.dto;

/**
 * Data transfer object for submitting a book rating.
 *
 * @param rating the rating value, must be between 1 and 5
 */
public record RatingDto(short rating) {
}
