package com.banking.credit.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreditPaymentRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-07T18:48:06.328397500-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class CreditPaymentRequest {

  private String amount;

  private String currency;

  private UUID sourceAccountId;

  private UUID payerCustomerId;

  public CreditPaymentRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreditPaymentRequest(String amount, String currency, UUID sourceAccountId) {
    this.amount = amount;
    this.currency = currency;
    this.sourceAccountId = sourceAccountId;
  }

  public CreditPaymentRequest amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */
  @NotNull @Pattern(regexp = "^(?!0+(\\.0+)?$)[0-9]+(\\.[0-9]{1,2})?$") 
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public CreditPaymentRequest currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull @Pattern(regexp = "^[A-Z]{3}$") 
  @Schema(name = "currency", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public CreditPaymentRequest sourceAccountId(UUID sourceAccountId) {
    this.sourceAccountId = sourceAccountId;
    return this;
  }

  /**
   * Get sourceAccountId
   * @return sourceAccountId
  */
  @NotNull @Valid 
  @Schema(name = "sourceAccountId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("sourceAccountId")
  public UUID getSourceAccountId() {
    return sourceAccountId;
  }

  public void setSourceAccountId(UUID sourceAccountId) {
    this.sourceAccountId = sourceAccountId;
  }

  public CreditPaymentRequest payerCustomerId(UUID payerCustomerId) {
    this.payerCustomerId = payerCustomerId;
    return this;
  }

  /**
   * Permite pago de terceros
   * @return payerCustomerId
  */
  @Valid 
  @Schema(name = "payerCustomerId", description = "Permite pago de terceros", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("payerCustomerId")
  public UUID getPayerCustomerId() {
    return payerCustomerId;
  }

  public void setPayerCustomerId(UUID payerCustomerId) {
    this.payerCustomerId = payerCustomerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreditPaymentRequest creditPaymentRequest = (CreditPaymentRequest) o;
    return Objects.equals(this.amount, creditPaymentRequest.amount) &&
        Objects.equals(this.currency, creditPaymentRequest.currency) &&
        Objects.equals(this.sourceAccountId, creditPaymentRequest.sourceAccountId) &&
        Objects.equals(this.payerCustomerId, creditPaymentRequest.payerCustomerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency, sourceAccountId, payerCustomerId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreditPaymentRequest {\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    sourceAccountId: ").append(toIndentedString(sourceAccountId)).append("\n");
    sb.append("    payerCustomerId: ").append(toIndentedString(payerCustomerId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

