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
 * OverdueStatusResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-07T18:48:06.328397500-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class OverdueStatusResponse {

  private UUID creditId;

  private Boolean hasOverdueDebt;

  private String overdueAmount;

  private Integer daysOverdue;

  public OverdueStatusResponse creditId(UUID creditId) {
    this.creditId = creditId;
    return this;
  }

  /**
   * Get creditId
   * @return creditId
  */
  @Valid 
  @Schema(name = "creditId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("creditId")
  public UUID getCreditId() {
    return creditId;
  }

  public void setCreditId(UUID creditId) {
    this.creditId = creditId;
  }

  public OverdueStatusResponse hasOverdueDebt(Boolean hasOverdueDebt) {
    this.hasOverdueDebt = hasOverdueDebt;
    return this;
  }

  /**
   * Get hasOverdueDebt
   * @return hasOverdueDebt
  */
  
  @Schema(name = "hasOverdueDebt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hasOverdueDebt")
  public Boolean getHasOverdueDebt() {
    return hasOverdueDebt;
  }

  public void setHasOverdueDebt(Boolean hasOverdueDebt) {
    this.hasOverdueDebt = hasOverdueDebt;
  }

  public OverdueStatusResponse overdueAmount(String overdueAmount) {
    this.overdueAmount = overdueAmount;
    return this;
  }

  /**
   * Get overdueAmount
   * @return overdueAmount
  */
  
  @Schema(name = "overdueAmount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("overdueAmount")
  public String getOverdueAmount() {
    return overdueAmount;
  }

  public void setOverdueAmount(String overdueAmount) {
    this.overdueAmount = overdueAmount;
  }

  public OverdueStatusResponse daysOverdue(Integer daysOverdue) {
    this.daysOverdue = daysOverdue;
    return this;
  }

  /**
   * Get daysOverdue
   * @return daysOverdue
  */
  
  @Schema(name = "daysOverdue", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("daysOverdue")
  public Integer getDaysOverdue() {
    return daysOverdue;
  }

  public void setDaysOverdue(Integer daysOverdue) {
    this.daysOverdue = daysOverdue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OverdueStatusResponse overdueStatusResponse = (OverdueStatusResponse) o;
    return Objects.equals(this.creditId, overdueStatusResponse.creditId) &&
        Objects.equals(this.hasOverdueDebt, overdueStatusResponse.hasOverdueDebt) &&
        Objects.equals(this.overdueAmount, overdueStatusResponse.overdueAmount) &&
        Objects.equals(this.daysOverdue, overdueStatusResponse.daysOverdue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(creditId, hasOverdueDebt, overdueAmount, daysOverdue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OverdueStatusResponse {\n");
    sb.append("    creditId: ").append(toIndentedString(creditId)).append("\n");
    sb.append("    hasOverdueDebt: ").append(toIndentedString(hasOverdueDebt)).append("\n");
    sb.append("    overdueAmount: ").append(toIndentedString(overdueAmount)).append("\n");
    sb.append("    daysOverdue: ").append(toIndentedString(daysOverdue)).append("\n");
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

