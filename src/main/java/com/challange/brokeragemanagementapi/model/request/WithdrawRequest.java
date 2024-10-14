package com.challange.brokeragemanagementapi.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Request model to withdraw money from a customer's account")
public class WithdrawRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Amount to be withdrawn", example = "500")
    private BigDecimal amount;

    @NotNull(message = "IBAN is required")
    @Schema(description = "IBAN of the account to withdraw money from", example = "TR330006100519786457841326")
    private String iban;
}

