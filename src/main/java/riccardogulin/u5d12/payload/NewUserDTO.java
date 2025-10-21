package riccardogulin.u5d12.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserDTO(
		@NotBlank(message = "Il nome è obbligatorio!")
		@Size(min = 2, max = 30, message = "Il nome deve avere una lunghezza compresa tra 2 e 30 caratteri")
		String name,
		@NotBlank(message = "Il cognome è obbligatorio!")
		@Size(min = 2, max = 30, message = "Il cognome deve avere una lunghezza compresa tra 2 e 30 caratteri")
		String surname,
		@NotBlank(message = "L'email è obbligatoria!")
		@Email(message = "L'indirizzo email inserito non è nel formato corretto!")
		String email,
		@NotBlank(message = "La password è obbligatoria!")
		@Size(min = 4, message = "La password deve avere minimo 4 caratteri")
		// @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{4,}$", message = "La password deve contenere una maiuscola, una minuscola ecc ecc ...")
		String password) {
}
