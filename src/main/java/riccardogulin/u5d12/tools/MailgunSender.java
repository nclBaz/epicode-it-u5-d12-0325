package riccardogulin.u5d12.tools;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riccardogulin.u5d12.entities.User;

@Component
public class MailgunSender {
	private String domain;
	private String apiKey;

	public MailgunSender(@Value("${mailgun.domain}") String domain, @Value("${mailgun.apiKey}") String apiKey) {
		this.domain = domain;
		this.apiKey = apiKey;
	}

	public void sendRegistrationEmail(User recipient) {

		HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + this.domain + "/messages")
				.basicAuth("api", this.apiKey)
				.queryString("from", "riccardo.gulin@gmail.com")
				.queryString("to", recipient.getEmail()) // Qua potrà esserci solo uno degli indirizzi autorizzati precedentemente sulla dashboard di Mailgun
				.queryString("subject", "Registrazione completata")
				.queryString("text", "Ciao, " + recipient.getName() + " " + recipient.getSurname() + ", benvenuto sulla nostra piattaforma!")
				.asJson();
		System.out.println(response.getBody()); // <-- Consiglio questo log per debuggare eventuali problemi
	}

	public void sendBillingEmail(User recipient) {
	}
}
