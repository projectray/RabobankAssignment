package nl.rabobank.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.authorizations.Authorization;
import org.springframework.data.annotation.Id;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PoaDocument {
  @Id
  private String id;
  private String grantorName;
  private String granteeName;
  private Account account;
  private Authorization authorization;
}
