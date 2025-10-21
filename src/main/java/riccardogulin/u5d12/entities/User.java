package riccardogulin.u5d12.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"password", "authorities", "enabled", "accountNonLocked", "accountNonExpired", "credentialsNonExpired"})
public class User implements UserDetails {
	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;
	private String name;
	private String surname;
	private String email;
	private String password;
	@Column(name = "avatar_url")
	private String avatarURL;
	@Enumerated(EnumType.STRING)
	private Role role;

	public User(String name, String surname, String email, String password) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
		this.role = Role.USER; // All'inizio sono tutti utenti semplici, poi si può pensare in caso di implementare una funzionalità
		// per far si che gli ADMIN cambino il ruolo agli utenti
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Questo metodo vogliamo che restituisca una lista di Authorities, cioè dei ruoli dell'utente
		// SimpleGrantedAuthority è una classe che implementa GrantedAuthority e ci serve per convertire il ruolo dell'utente
		// che nel nostro caso è un enum in un oggetto utilizzabile dai meccanismi di Spring Security
		return List.of(new SimpleGrantedAuthority(this.role.name()));
	}

	@Override
	public String getUsername() {
		return this.email;
	}
}
