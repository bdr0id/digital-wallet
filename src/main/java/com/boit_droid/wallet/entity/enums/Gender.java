package com.boit_droid.wallet.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "Gender",
    description = "User gender options for registration and profile information. Used for demographic data collection and personalization purposes",
    allowableValues = {"MALE", "FEMALE", "NONBINARY"},
    example = "MALE"
)
public enum Gender {
    @Schema(description = "Male gender identity - used for users who identify as male")
    MALE,
    
    @Schema(description = "Female gender identity - used for users who identify as female") 
    FEMALE,
    
    @Schema(description = "Non-binary gender identity - used for users who identify as non-binary, genderfluid, or other gender identities")
    NONBINARY
}
