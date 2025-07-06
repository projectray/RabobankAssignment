package nl.rabobank.service;

import lombok.RequiredArgsConstructor;
import nl.rabobank.document.UserDocument;
import nl.rabobank.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepo;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserDocument u = userRepo.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    return User.builder()
      .username(u.getUsername())
      .password(u.getPassword())
      .authorities(u.getRoles().stream()
        .map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
      .build();
  }
}
