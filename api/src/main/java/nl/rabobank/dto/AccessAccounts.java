package nl.rabobank.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessAccounts {
  private String grantor;
  private String accountNumber;
  private String accountHolderName;
  private String accountType;
  private String authorizationType;
}
