package net.samitkumar.springsecurityweb;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SpringSecurityWebApplication {
	final UserRepository userRepository;
	final PasswordEncoder passwordEncoder;
	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityWebApplication.class, args);
	}

	@EventListener(ApplicationStartedEvent.class)
	void onStart() {
		log.info("## onStart load some user");

        userRepository.saveAll(List.of(
					new User(null, "one", passwordEncoder.encode("secret1")),
					new User(null, "two", passwordEncoder.encode("secret2"))
				)
		);

		userRepository.findAll().forEach(System.out::println);
		System.out.println(userRepository.findByUsername("one"));
	}
}

@Controller
class ApplicationView {

	@GetMapping("/")
	String home() {
		return "hello";
	}

	@GetMapping("/hello")
	String hello() {
		return "hello";
	}

	@GetMapping("/signup")
	String signUp() {
		return "signup";
	}
}

@Configuration
@EnableWebSecurity
class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	@SneakyThrows
	public SecurityFilterChain securityFilterChain(HttpSecurity http)  {
		http
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/hello", "/signup").permitAll()
						.anyRequest().authenticated()
				)
				//.httpBasic(Customizer.withDefaults()) //For Basic auth
				.formLogin(Customizer.withDefaults()); // For Basic Form Login

		return http.build();
	}

	/*@Bean
	// This is optional, If you want to get more control
	public AuthenticationManager authenticationManager(UserService userService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(authenticationProvider);
	}*/
}

@Table("users")
record User(@Id Integer id, String username, String password) implements UserDetails {
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}

interface UserRepository extends ListCrudRepository<User, Integer> {
	User findByUsername(String username);
}

@Service
@RequiredArgsConstructor
@Slf4j
class UserService implements UserDetailsService {
	final UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("## loadUserByUsername USERNAME {}", username);
		return userRepository.findByUsername(username);
	}
}