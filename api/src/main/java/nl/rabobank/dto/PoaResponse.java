package nl.rabobank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoaResponse {
  private String id;
  private String grantor;
  private String grantee;
  private String accountNumber;
  private String accountType;
  private String authorizationType;
}
