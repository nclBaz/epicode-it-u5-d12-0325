package riccardogulin.u5d12.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import riccardogulin.u5d12.payload.ErrorsDTO;
import riccardogulin.u5d12.payload.ErrorsWithListDTO;

import java.time.LocalDateTime;

@RestControllerAdvice
// Questa è un'annotazione obbligatoria se vogliamo avere un controller che gestisca le eccezioni
// Così facendo creiamo un controller "speciale" il cui scopo non è ricevere richieste come gli altri, bensì ricevere
// le eccezioni. Ogniqualvolta verrà fatto un throw di un'eccezione, l'eccezione arriverà qua, pertanto questa classe
// conterrà dei metodi per rispondere adeguatamente ad ogni eccezione

// Questa classe ci consente di evitare di dover fare una gestione delle risposte di errore per ogni singolo endpoint con dei try-catch
// Ogni metodo di questa classe userà un'annotazione specifica che si chiama @ExceptionHandler per stabilire quale tipo di
// eccezione dovrà gestire
public class ExceptionsHandler {

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorsWithListDTO handleValidationErrors(ValidationException ex) {
		return new ErrorsWithListDTO(ex.getMessage(), LocalDateTime.now(), ex.getErrorsMessages());
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
	public ErrorsDTO handleUnauthorizedException(UnauthorizedException ex) {
		return new ErrorsDTO(ex.getMessage(), LocalDateTime.now());
	}

	@ExceptionHandler(BadRequestException.class) // Tra le parentesi indico quale eccezione dovrà gestire questo metodo
	@ResponseStatus(HttpStatus.BAD_REQUEST) // 400
	public ErrorsDTO handleBadRequest(BadRequestException ex) {
		return new ErrorsDTO(ex.getMessage(), LocalDateTime.now());
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorsDTO handleForbidden(AuthorizationDeniedException ex) {
		return new ErrorsDTO("Non hai i permessi per accedere!", LocalDateTime.now());
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND) // 404
	public ErrorsDTO handleNotFound(NotFoundException ex) {
		return new ErrorsDTO(ex.getMessage(), LocalDateTime.now());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
	@ExceptionHandler(Exception.class) // Tutte le eccezioni che non sono BadRequestException o NotFoundException vengono gestite da questo handler
	public ErrorsDTO handleServerError(Exception ex) {
		ex.printStackTrace(); // E' importante avere il print dello stack trace per sapere dove intervenire per fixare il bug
		return new ErrorsDTO("C'è stato un errore generico, giuro che lo risolveremo presto!", LocalDateTime.now());
	}
}
