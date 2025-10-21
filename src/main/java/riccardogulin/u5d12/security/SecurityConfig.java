package riccardogulin.u5d12.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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

		return httpSecurity.build();
	}
}
