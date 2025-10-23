package com.boit_droid.wallet.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status values for transactions, wallets, and other entities in the system")
public enum Status {
    @Schema(description = "Entity is active and operational")
    ACTIVE,

    @Schema(description = "Entity is active and operational")
    INACTIVE,
    
    @Schema(description = "Entity is pending approval or processing")
    PENDING,
    
    @Schema(description = "Entity is locked due to security or compliance reasons")
    LOCKED,
    
    @Schema(description = "Entity is permanently closed")
    CLOSED,
    
    @Schema(description = "Entity has timed out due to inactivity or processing delay")
    TIMEOUT,
    
    @Schema(description = "Entity has been cancelled by user or system")
    CANCELLED
}
