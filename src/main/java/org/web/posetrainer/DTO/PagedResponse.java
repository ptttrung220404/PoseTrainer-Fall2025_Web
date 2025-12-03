package org.web.posetrainer.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasPrev;
    private boolean hasNext;
    private int fromItem;
    private int toItem;

    public static <T> PagedResponse<T> of(List<T> source, int page, int size) {
        List<T> safeSource = source == null ? Collections.emptyList() : source;
        int safeSize = Math.max(1, Math.min(size, 100));
        long totalItems = safeSource.size();
        int totalPages = (int) Math.ceil(totalItems / (double) safeSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int safePage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (safePage - 1) * safeSize;
        if (fromIndex > totalItems) {
            fromIndex = (int) totalItems;
        }
        int toIndex = (int) Math.min(fromIndex + safeSize, totalItems);
        List<T> pageItems = safeSource.subList(fromIndex, toIndex);

        int fromItem = totalItems == 0 ? 0 : fromIndex + 1;
        int toItem = totalItems == 0 ? 0 : fromIndex + pageItems.size();

        boolean hasPrev = safePage > 1 && totalItems > 0;
        boolean hasNext = safePage < totalPages && totalItems > 0;

        return new PagedResponse<>(
                pageItems,
                safePage,
                safeSize,
                totalItems,
                totalPages,
                hasPrev,
                hasNext,
                fromItem,
                toItem
        );
    }
}

