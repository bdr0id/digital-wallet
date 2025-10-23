package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Generic wrapper for paginated responses containing data and pagination metadata",
    example = """
    {
        "content": [
            {
                "transactionId": "TXN-20240130-001234",
                "amount": 150.50,
                "currency": "USD",
                "type": "TRANSFER",
                "status": "ACTIVE",
                "description": "Payment for services",
                "timestamp": "2024-01-30T10:15:30Z"
            }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 150,
        "totalPages": 8,
        "first": true,
        "last": false,
        "hasNext": true,
        "hasPrevious": false
    }
    """
)
public class PagedResponse<T> {
    
    @Schema(
        description = "List of items for the current page. The actual content type varies by endpoint (transactions, notifications, etc.)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<T> content;
    
    @Schema(
        description = "Current page number (zero-based). First page is 0, second page is 1, etc.",
        example = "0",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private int page;
    
    @Schema(
        description = "Number of items requested per page. This is the page size that was requested, not necessarily the number of items returned",
        example = "20",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1",
        maximum = "100"
    )
    private int size;
    
    @Schema(
        description = "Total number of items across all pages that match the query criteria",
        example = "150",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private long totalElements;
    
    @Schema(
        description = "Total number of pages available based on the total elements and page size",
        example = "8",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private int totalPages;
    
    @Schema(
        description = "Whether this is the first page (page number 0)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean first;
    
    @Schema(
        description = "Whether this is the last page (no more pages available after this one)",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean last;
    
    @Schema(
        description = "Whether there is a next page available (equivalent to !last)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean hasNext;
    
    @Schema(
        description = "Whether there is a previous page available (equivalent to !first)",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean hasPrevious;
    
    public static <T> PagedResponse<T> of(List<T> content, Page<?> page) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
    
    public static <T> PagedResponse<T> empty(String message) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(List.of());
        response.setPage(0);
        response.setSize(0);
        response.setTotalElements(0);
        response.setTotalPages(0);
        response.setFirst(true);
        response.setLast(true);
        response.setHasNext(false);
        response.setHasPrevious(false);
        return response;
    }
}