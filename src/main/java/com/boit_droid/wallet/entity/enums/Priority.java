package com.boit_droid.wallet.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Enumeration of notification priority levels affecting delivery order and display prominence",
    example = "MEDIUM"
)
public enum Priority {
    
    @Schema(description = "Low priority - informational notifications that can be delivered with delay")
    LOW,
    
    @Schema(description = "Medium priority - standard notifications with normal delivery timing")
    MEDIUM,
    
    @Schema(description = "High priority - important notifications requiring prompt delivery")
    HIGH,
    
    @Schema(description = "Urgent priority - time-sensitive notifications requiring immediate delivery")
    URGENT,
    
    @Schema(description = "Critical priority - highest priority notifications for security or system issues")
    CRITICAL
}