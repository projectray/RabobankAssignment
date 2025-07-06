package nl.rabobank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.validation.AllowedValues;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PoaRequest {
  @NotBlank
  private String grantor;
  @NotBlank
  private String grantee;
  @NotBlank
  private String accountNumber;
  @NotBlank
  private String accountHolderName;
  @NotBlank
  @AllowedValues(value = {"SAVING", "PAYMENT"}, message = "AccountType must be SAVING or PAYMENT")
  private String accountType;
  @NotNull
  @PositiveOrZero
  private Double initialBalance;
  @NotBlank
  @AllowedValues(value = {"READ", "WRITE"}, message = "Authorization must be READ or WRITE")
  private String authorizationType;
}
