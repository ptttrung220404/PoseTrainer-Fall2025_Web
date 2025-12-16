package org.web.posetrainer.Service;

import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.*;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
@Service
public class PageService {
    public List<Excercise> filterAndSortExercises(List<Excercise> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<Excercise> comparator = switch (option.field()) {
            case "name" -> Comparator.comparing(ex -> safeString(ex.getName()), String.CASE_INSENSITIVE_ORDER);
            case "level" -> Comparator.comparing(ex -> safeString(ex.getLevel()), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingLong(Excercise::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(ex -> matchesKeyword(keyword, ex.getName(), ex.getSlug()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

   public List<WorkoutTemplate> filterAndSortWorkouts(List<WorkoutTemplate> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<WorkoutTemplate> comparator = switch (option.field()) {
            case "title" -> Comparator.comparing(w -> safeString(w.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case "level" -> Comparator.comparing(w -> safeString(w.getLevel()), String.CASE_INSENSITIVE_ORDER);
            case "duration" -> Comparator.comparingInt(WorkoutTemplate::getEstDurationMin);
            default -> Comparator.comparingLong(WorkoutTemplate::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(w -> matchesKeyword(keyword, w.getTitle(), w.getDescription(), w.getId()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Collections> filterAndSortCollections(List<Collections> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<Collections> comparator = switch (option.field()) {
            case "title" -> Comparator.comparing(c -> safeString(c.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case "category" -> Comparator.comparing(c -> safeString(c.getCategory()), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingLong(Collections::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(c -> matchesKeyword(keyword, c.getTitle(), c.getCategory()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<User> filterAndSortUsers(List<User> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "created", "desc");
        Comparator<User> comparator = switch (option.field()) {
            case "name" -> Comparator.comparing(u -> safeString(u.getDisplayName()), String.CASE_INSENSITIVE_ORDER);
            case "email" -> Comparator.comparing(u -> safeString(u.getEmail()), String.CASE_INSENSITIVE_ORDER);
            case "lastLogin" -> Comparator.comparingLong(User::getLastLoginAt);
            default -> Comparator.comparingLong(User::getCreatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(u -> matchesKeyword(keyword, u.getDisplayName(), u.getEmail()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Community> filterAndSortPosts(List<Community> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "created", "desc");
        Comparator<Community> comparator = switch (option.field()) {
            case "author" -> Comparator.comparing(Community::getDisplayName, String.CASE_INSENSITIVE_ORDER);
            case "likes" -> Comparator.comparingLong(Community::getLikesCount);
            case "comments" -> Comparator.comparingLong(Community::getCommentsCount);
            default -> Comparator.comparingLong(c -> timestampToMillis(c.getCreatedAt()));
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(c -> matchesKeyword(keyword, c.getDisplayName(), c.getContent()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public boolean matchesKeyword(String keyword, String... candidates) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String needle = keyword.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public String safeString(String value) {
        return value == null ? "" : value;
    }

    public long timestampToMillis(com.google.cloud.Timestamp ts) {
        return ts == null ? 0L : ts.toDate().getTime();
    }

    public SortOption resolveSort(String sort, String defaultField, String defaultDir) {
        if (sort == null || sort.isBlank()) {
            return new SortOption(defaultField, defaultDir);
        }
        String[] parts = sort.split("_");
        if (parts.length != 2) {
            return new SortOption(defaultField, defaultDir);
        }
        String direction = "asc".equalsIgnoreCase(parts[1]) ? "asc" : "desc";
        return new SortOption(parts[0], direction);
    }

    private record SortOption(String field, String direction) {
        boolean isDesc() {
            return "desc".equalsIgnoreCase(direction);
        }
    }
}
