package com.banking.credit.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.credit.dto.CreditType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreditCreateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-07T18:48:06.328397500-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class CreditCreateRequest {

  private UUID customerId;

  private CreditType type;

  private String amount;

  private String currency;

  private Integer termMonths;

  public CreditCreateRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreditCreateRequest(UUID customerId, CreditType type, String amount, String currency, Integer termMonths) {
    this.customerId = customerId;
    this.type = type;
    this.amount = amount;
    this.currency = currency;
    this.termMonths = termMonths;
  }

  public CreditCreateRequest customerId(UUID customerId) {
    this.customerId = customerId;
    return this;
  }

  /**
   * Get customerId
   * @return customerId
  */
  @NotNull @Valid 
  @Schema(name = "customerId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("customerId")
  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public CreditCreateRequest type(CreditType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @NotNull @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public CreditType getType() {
    return type;
  }

  public void setType(CreditType type) {
    this.type = type;
  }

  public CreditCreateRequest amount(String amount) {
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

  public CreditCreateRequest currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull @Pattern(regexp = "^[A-Z]{3}$") 
  @Schema(name = "currency", example = "PEN", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public CreditCreateRequest termMonths(Integer termMonths) {
    this.termMonths = termMonths;
    return this;
  }

  /**
   * Get termMonths
   * minimum: 1
   * @return termMonths
  */
  @NotNull @Min(1) 
  @Schema(name = "termMonths", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("termMonths")
  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreditCreateRequest creditCreateRequest = (CreditCreateRequest) o;
    return Objects.equals(this.customerId, creditCreateRequest.customerId) &&
        Objects.equals(this.type, creditCreateRequest.type) &&
        Objects.equals(this.amount, creditCreateRequest.amount) &&
        Objects.equals(this.currency, creditCreateRequest.currency) &&
        Objects.equals(this.termMonths, creditCreateRequest.termMonths);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, type, amount, currency, termMonths);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreditCreateRequest {\n");
    sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    termMonths: ").append(toIndentedString(termMonths)).append("\n");
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

