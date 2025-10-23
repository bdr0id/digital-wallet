package com.boit_droid.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(
    description = "Request object for generating comprehensive transaction statements with date range filtering, pagination, and multiple output formats",
    example = """
    {
        "startDate": "2024-01-01",
        "endDate": "2024-01-31",
        "page": 0,
        "size": 20,
        "format": "JSON"
    }
    """
)
public class StatementRequest {
    
    @Schema(
        description = "Start date for the statement period (inclusive). Cannot be more than 1 year in the past. Used to filter transactions from this date onwards",
        example = "2024-01-01",
        required = true,
        format = "date"
    )
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Schema(
        description = "End date for the statement period (inclusive). Must be after or equal to start date. Cannot be in the future. Maximum period is 1 year",
        example = "2024-01-31",
        required = true,
        format = "date"
    )
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @Schema(
        description = "Page number for pagination (zero-based). Use 0 for the first page, 1 for the second page, etc. Only applicable for JSON format",
        example = "0",
        minimum = "0",
        defaultValue = "0"
    )
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Schema(
        description = "Number of transaction records per page. Higher values may impact performance. Only applicable for JSON format with pagination",
        example = "20",
        minimum = "1",
        maximum = "100",
        defaultValue = "20"
    )
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private int size = 20;
    
    @Schema(
        description = "Output format for the statement. JSON: paginated response for API consumption, PDF: formatted document for printing/download, CSV: spreadsheet format for data analysis",
        example = "JSON",
        allowableValues = {"PDF", "JSON", "CSV"},
        defaultValue = "JSON"
    )
    @Pattern(regexp = "^(PDF|JSON|CSV)$", message = "Format must be PDF, JSON, or CSV")
    private String format = "JSON";
    
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return !endDate.isBefore(startDate);
    }
}