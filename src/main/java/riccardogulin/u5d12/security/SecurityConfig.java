package riccardogulin.u5d12.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity // Annotazione che serve per indicare che questa non sarà una classe di configurazione qualsiasi, bensì sarà una classe di
// configurazione speciale per Spring Security
@EnableMethodSecurity
// Se volessimo utilizzare regole di AUTHORIZATION specifiche per ogni singolo endpoint, dobbiamo ricordarci di questa annotazione
// altrimenti verranno ignorate
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		// Questo bean mi consentirà di configurare la sicurezza di spring, in particolare potrò:
		// - disabilitare comportamenti di default che non mi interessano
		httpSecurity.formLogin(formLogin -> formLogin.disable()); // Non voglio l'auth basata su form proposta da Spring
		// (avremo React + JWT per quello)
		httpSecurity.csrf(csrf -> csrf.disable()); // Non voglio la protezione da CSRF (non ne abbiamo bisogno, inoltre
		// ci complicherebbe la vita anche lato FE)
		httpSecurity.sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// Non vogliamo utilizzare le sessioni perché l'autenticazione basata su token è l'esatto opposto dell'utilizzare una sessione

		// - personalizzare il comportamento di funzionalità pre-esistente
		httpSecurity.authorizeHttpRequests(req -> req.requestMatchers("/**").permitAll());
		// Disabilitiamo il comportamento di default di Security che restituisce 401 per ogni richiesta su ogni endpoint.
		// Siccome andremo ad implementare un meccanismo custom di autenticazione, sarà esso a fare i controlli ed eventualmente rispondere con 401
		// non Security direttamente. Quindi col nostro meccanismo andremo anche a selezionare quali endpoint proteggere e quali no

		// - aggiungere ulteriori funzionalità custom

		httpSecurity.cors(Customizer.withDefaults()); // OBBLIGATORIA SE VOGLIAMO USARE IL BEAN SOTTOSTANTE PER LA CONFIGURAZIONE CORS

		return httpSecurity.build();
	}

	@Bean
	public PasswordEncoder getBCrypt() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://www.mywonderfulfe.com"));
		// Mi sto creando una WHITELIST di uno o più indirizzi FRONTEND che voglio possano accedere a questo BE senza problemi di CORS
		// Volendo (anche se rischioso, ma utile per API pubbliche) potrei mettere '*' che permette l'accesso a tutti
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // applico la configurazione di sopra a tutti gli endpoint ("/**")
		return source;
	}// N.B. Non dimenticarsi di aggiungere nella filter chain httpSecurity.cors(Customizer.withDefaults())!!!!!!!!!!!!!!!

}
