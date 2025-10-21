package riccardogulin.u5d12.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import riccardogulin.u5d12.entities.User;
import riccardogulin.u5d12.exceptions.UnauthorizedException;
import riccardogulin.u5d12.services.UsersService;

import java.io.IOException;
import java.util.UUID;

@Component // Per poter essere inserito nella FilterChain questo dovrà essere un Component!
public class JWTFilter extends OncePerRequestFilter {
	// Estendendo OncePerRequestFilter sto dicendo che questa classe sarà compatibile con la FilterChain
	// Una caratteristica importante dei filtri è che hanno accesso alle richieste che arrivano e inoltre possono anche mandare delle risposte
	@Autowired
	private JWTTools jwtTools;
	@Autowired
	private UsersService usersService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// Questo metodo verrà eseguito ad ogni richiesta. Sarà lui a dover verificare il token

		// ********************************************************* AUTENTICAZIONE ************************************************************

		// PIANO DI BATTAGLIA
		// 1. Verifichiamo se nella request esiste un header che si chiama Authorization, verifichiamo anche che sia fatto con il formato giusto
		// (Authorization: "Bearer 123j21389912391283..."). Se non c'è oppure se è nel formato sbagliato --> 401
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer "))
			throw new UnauthorizedException("Inserire il token nell'authorization header nel formato giusto!");

		// 2. Se l'header esiste, estraiamo il token da esso
		// "Bearer 123j21389912391283..."
		String accessToken = authHeader.replace("Bearer ", "");

		// 3. Verifichiamo se il token è valido, cioè controlleremo se è stato modificato oppure no, se non è scaduto e se non è malformato
		jwtTools.verifyToken(accessToken);

		// 4. Se qualcosa non va con il token --> 401

		// ****************************************************** AUTORIZZAZIONE *************************************************************

		// 1. Cerchiamo l'utente nel db (l'id sta nel token!)
		// 1.1 Estraiamo l'id dal token
		UUID userId = jwtTools.extractIdFromToken(accessToken);
		// 1.2 findById
		User found = usersService.findById(userId);

		// 2. Associamo l'utente al Security Context
		// Questo fondamentale step, serve per associare l'utente che sta effettuando la richiesta alla richiesta stessa per tutto il corso
		// della sua durata, quindi fino a che la richiesta non otterrà una risposta. Questo significa che sia in filtri successivi, che nel
		// controller, che negli endpoint potrò sempre risalire a chi sia l'utente che ha fatto la richiesta
		// Questo ci servirà sia per controllare il ruolo dell'utente prima di arrivare agli endpoint, sia per effettuare dei controlli all'interno
		// degli endpoint stessi per capire se chi sta cercando di leggere/modificare/cancellare una risorsa sia effettivamente il proprietario di
		// tale risorsa
		Authentication authentication = new UsernamePasswordAuthenticationToken(found, null, found.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		// Aggiorniamo il Security Context associando ad esso l'utente corrente ed i suoi ruoli

		// 3. Se tutto è OK passiamo la richiesta al prossimo (che può essere o un altro filtro o direttamente il controller)
		filterChain.doFilter(request, response); // .doFilter chiama il prossimo elemento della catena (o un altro filtro o il controller direttamente)
	}

	// Tramite l'override di questo metodo posso disabilitare il lavoro del filtro per determinati tipi di richieste
	// Ad esempio, posso disabilitare tutte le richieste dirette al controller /auth
	// Quindi tutte le richieste tipo /auth/login oppure /auth/register non richiederanno alcun token
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return new AntPathMatcher().match("/auth/**", request.getServletPath());
		// Tutti gli endpoint nel controller "/auth/" non verranno controllati dal filtro
	}
}
