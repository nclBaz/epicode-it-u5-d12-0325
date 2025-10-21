package riccardogulin.u5d12.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import riccardogulin.u5d12.entities.User;
import riccardogulin.u5d12.payload.NewUserDTO;
import riccardogulin.u5d12.services.UsersService;

import java.io.IOException;
import java.util.UUID;

/*

1. GET http://localhost:3001/users
3. GET http://localhost:3001/users/{userId}
4. PUT http://localhost:3001/users/{userId} (+ request body)
5. DELETE  http://localhost:3001/users/{userId}

*/

@RestController
@RequestMapping("/users")
public class UsersController {
	@Autowired
	private UsersService usersService;


	@GetMapping
	//@PreAuthorize("hasAuthority('ADMIN')") // Solo gli admin possono visualizzare la lista di tutti gli utenti
	public Page<User> findAll(@RequestParam(defaultValue = "0") int page,
	                          @RequestParam(defaultValue = "10") int size,
	                          @RequestParam(defaultValue = "id") String sortBy) {
		// Mettere dei valori di default nei query params è solitamente una buona idea per far si che non
		// ci siano errori se il client non li passa
		return this.usersService.findAll(page, size, sortBy);
	}

	// I "/me" endpoint servono agli utenti semplici per interagire in lettura/modifica/cancellazione dei propri profili
	// qualora decidessimo di riservare gli endpoint /{userId} solo agli admin (come è giusto che sia altrimenti un utente
	// qualsiasi potrebbe andare a modificare/cancellare i profili degli altri utenti

	@GetMapping("/me")
	public User getProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
		return currentAuthenticatedUser;

	}

	@PutMapping("/me")
	public User updateProfile(@AuthenticationPrincipal User currentAuthenticatedUser, @RequestBody NewUserDTO payload) {
		return this.usersService.findByIdAndUpdate(currentAuthenticatedUser.getId(), payload);
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
		this.usersService.findByIdAndDelete(currentAuthenticatedUser.getId());
	}


	@GetMapping("/{userId}")
	public User findById(@PathVariable UUID userId) {
		return this.usersService.findById(userId);
	}


	@PutMapping("/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public User findByIdAndUpdate(@PathVariable UUID userId, @RequestBody NewUserDTO payload) {
		return this.usersService.findByIdAndUpdate(userId, payload);
	}

	@DeleteMapping("/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void findByIdAndDelete(@PathVariable UUID userId) {
		this.usersService.findByIdAndDelete(userId);
	}

	@PatchMapping("/{userId}/avatar")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String uploadImage(@RequestParam("avatar") MultipartFile file) throws IOException {
		// "avatar" deve corrispondere ESATTAMENTE al nome del campo del MultiPart che contiene il file
		// che è quello che verrà inserito dal frontend
		// Se non corrisponde non troverò il file
		System.out.println(file.getSize());
		System.out.println(file.getOriginalFilename());
		return this.usersService.uploadAvatar(file);

	}
}
