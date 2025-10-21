package riccardogulin.u5d12.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import riccardogulin.u5d12.entities.User;
import riccardogulin.u5d12.exceptions.ValidationException;
import riccardogulin.u5d12.payload.LoginDTO;
import riccardogulin.u5d12.payload.LoginResponseDTO;
import riccardogulin.u5d12.payload.NewUserDTO;
import riccardogulin.u5d12.services.AuthService;
import riccardogulin.u5d12.services.UsersService;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private AuthService authService;

	@Autowired
	private UsersService usersService;

	@PostMapping("/login")
	public LoginResponseDTO login(@RequestBody LoginDTO body) {
		return new LoginResponseDTO(authService.checkCredentialsAndGenerateToken(body));
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public User createUser(@RequestBody @Validated NewUserDTO payload, BindingResult validationResult) {
		// @Validated serve per "attivare" la validazione
		// BindingResult è un oggetto che contiene tutti gli errori e anche dei metodi comodi da usare tipo .hasErrors()
		if (validationResult.hasErrors()) {

			throw new ValidationException(validationResult.getFieldErrors()
					.stream().map(fieldError -> fieldError.getDefaultMessage()).toList());
		}
		return this.usersService.save(payload);
	}

}
