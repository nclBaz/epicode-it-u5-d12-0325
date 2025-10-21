package riccardogulin.u5d12.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import riccardogulin.u5d12.entities.User;
import riccardogulin.u5d12.exceptions.UnauthorizedException;
import riccardogulin.u5d12.payload.LoginDTO;
import riccardogulin.u5d12.security.JWTTools;

@Service
public class AuthService {
	@Autowired
	private UsersService usersService;
	@Autowired
	private JWTTools jwtTools;

	public String checkCredentialsAndGenerateToken(LoginDTO body) {
		// 1. Controllo credenziali
		// 1.1 Controllo nel DB se esiste un utente con quell'indirizzo email (fornito nel body)
		User found = this.usersService.findByEmail(body.email());

		// 1.2 Se esiste verifico che la sua password corrisponda a quella del body
		// 1.3 Se una delle 2 verifiche non va a buon fine --> 401
		if (found.getPassword().equals(body.password())) {
			// TODO: Migliorare gestione password
			// 2. Se credenziali OK --> Genero un access token
			// 3. Ritorno il token
			return jwtTools.createToken(found);
		} else {
			throw new UnauthorizedException("Credenziali errate!");
		}

	}
}
