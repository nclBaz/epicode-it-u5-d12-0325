package riccardogulin.u5d12.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import riccardogulin.u5d12.entities.User;
import riccardogulin.u5d12.exceptions.BadRequestException;
import riccardogulin.u5d12.exceptions.NotFoundException;
import riccardogulin.u5d12.payload.NewUserDTO;
import riccardogulin.u5d12.repositories.UsersRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UsersService {
	private static final long MAX_SIZE = 5 * 1024; // 5 MB
	private static final List<String> ALLOWED_TYPES = List.of("image/png", "image/jpeg");

	@Autowired
	private UsersRepository usersRepository;
	@Autowired
	private Cloudinary imageUploader;
	@Autowired
	private PasswordEncoder bcrypt;

	public Page<User> findAll(int pageNumber, int pageSize, String sortBy) {
		if (pageSize > 50) pageSize = 50;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
		return this.usersRepository.findAll(pageable);
	}

	public User save(NewUserDTO payload) {
		// 1. Verifichiamo che l'email passata non sia già in uso
		this.usersRepository.findByEmail(payload.email()).ifPresent(user -> {
					throw new BadRequestException("L'email " + user.getEmail() + " è già in uso!");
				}
		);

		// 2. Aggiungo dei campi server-generated (tipo avatarURL)
		User newUser = new User(payload.name(), payload.surname(), payload.email(), bcrypt.encode(payload.password()));
		newUser.setAvatarURL("https://ui-avatars.com/api/?name=" + payload.name() + "+" + payload.surname());

		// 3. Salvo
		User savedUser = this.usersRepository.save(newUser);

		// 4. Log
		log.info("L'utente con id: " + savedUser.getId() + " è stato salvato correttamente!");

		// 5. Ritorno l'utente salvato
		return savedUser;
	}

	public User findById(UUID userId) {
		return this.usersRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
	}

	public User findByIdAndUpdate(UUID userId, NewUserDTO payload) {
		// 1. Cerco l'utente nel db
		User found = this.findById(userId);

		// 2. Controllo che la nuova email non sia già in uso
		if (!found.getEmail().equals(payload.email())) { // Il controllo sull'email lo faccio solo quando effettivamente
			// mi viene passata un'email diversa da quella precedente
			this.usersRepository.findByEmail(payload.email()).ifPresent(user -> {
						throw new BadRequestException("L'email " + user.getEmail() + " è già in uso!");
					}
			);
		}

		// 3. Modifico l'utente trovato nel db
		found.setName(payload.name());
		found.setSurname(payload.surname());
		found.setEmail(payload.email());
		found.setPassword(payload.password());
		found.setAvatarURL("https://ui-avatars.com/api/?name=" + payload.name() + "+" + payload.surname());

		// 4. Salvo
		User modifiedUser = this.usersRepository.save(found);

		// 5. Log
		log.info("L'utente con id " + modifiedUser.getId() + " è stato modificato correttamente");

		// 6. Return dell'utente modificato
		return modifiedUser;
	}

	public void findByIdAndDelete(UUID userId) {
		User found = this.findById(userId);
		this.usersRepository.delete(found);
	}

	public String uploadAvatar(MultipartFile file) {

		if (file.isEmpty()) throw new BadRequestException("File vuoto!");
		if (file.getSize() > MAX_SIZE) throw new BadRequestException("File troppo grande!");
		if (!ALLOWED_TYPES.contains(file.getContentType())) throw new BadRequestException("I formati permessi sono png e jpeg!");

		// Controllo che l'utente esista...
		try {
			Map result = imageUploader.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
			String imageURL = (String) result.get("url");

			// ... qua va salvato l'url dentro il record dello user di riferimento
			return imageURL;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public User findByEmail(String email) {
		return this.usersRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("L'utente con l'email " + email + " non è stato trovato"));
	}
}
