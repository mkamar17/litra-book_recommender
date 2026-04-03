package uk.ac.rhul.cs3821.dto;

/**
 * Data transfer object for returning a single leaderboard entry in API responses.
 */
public record LeaderboardEntryDto(
    String displayName,
    int points,
    int pagesRead,
    int booksCompleted,
    String period
) {
}