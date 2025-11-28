package sad.storereg.services.appdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.config.JwtService;
import sad.storereg.logs.AuditService;
import sad.storereg.logs.LoginRepository;
import sad.storereg.models.master.Notification;
import sad.storereg.repo.auth.PasswordTokenRepository;
import sad.storereg.repo.auth.UserRepository;
import sad.storereg.repo.master.NotificationRepository;

@Service
@RequiredArgsConstructor
public class ServiceNotification {
	
	private final AuditService serviceAuditTrail;
	private final NotificationRepository notificationRepo;
	private final CoreServices coreServices;
	
	private String messageId;
	
	private Notification getNotification(String messageId) {

		this.messageId = messageId;
		Optional<Notification> optNotification = notificationRepo.findByMessageIdEquals(messageId);
		if (optNotification.isPresent())
			return optNotification.get();
		else {
			// Here keep audit trail to log this information
			/// System.out.println("Message ID not found");

			return null;
		}
		// throw new InternalServerError("Message ID doesn't exist");
	}
	
	@Auditable
	@Transactional
	@Async
	public CompletableFuture<Void> sendOnlySms(HttpServletRequest request, String messageId, String recipientMobileNo,
			String[] mobileParams) {
		String mobileMessage = createMessage(getNotification(messageId).getMessage(), Arrays.asList(mobileParams));
		//System.out.println("Message is: "+mobileMessage);
		if (mobileMessage == null) {
			serviceAuditTrail.saveAuditTrail("Send SMS",recipientMobileNo, messageId, "Message ID not found", "Failed",
					coreServices.getClientIp(request),null);
			return CompletableFuture.completedFuture(null);
		}
		try {
			//sendSms(request, messageId, recipientMobileNo, mobileMessage);
			serviceAuditTrail.saveAuditTrail("Send SMS",recipientMobileNo, messageId, "SMS sent to mobile no", "Success",
					coreServices.getClientIp(request),null);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			serviceAuditTrail.saveAuditTrail("Send SMS",recipientMobileNo, messageId, "Message ID not found", "Failed",
					coreServices.getClientIp(request),null);
			// throw e;
			// LOG.log(Level.SEVERE, e.getLocalizedMessage());
		}
		return CompletableFuture.completedFuture(null);
	}

	private String createMessage(String message, List<String> params) {
		String msg = message;
		for (int i = 0; i < params.size(); i++) {
			msg = msg.replace("{" + i + "}", params.get(i));
		}
		return msg;
	}
	
	@Auditable
	private void sendSms(HttpServletRequest request, String messageId, String recipientMobileNo, String message2)
			throws Exception {
		String responseString = "";
		try {
			
			SSLSocketFactory sf = null;
			SSLContext context = null;

			// Create a trust manager that trusts all certificates
			TrustManager[] trustAllCertificates = new TrustManager[] { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			context = SSLContext.getInstance("TLSv1.2");
			context.init(null, trustAllCertificates, new SecureRandom());
			sf = context.getSocketFactory();

			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true; // Allow all hostnames
				}
			});

			// Open connection
			URL url = new URL("https://hydgw.sms.gov.in/failsafe/MLink");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Content-Type", "text/plain");

			// String data =
			// "username=mlagda.sms&pin=fdIDaxVs&mnumber=919774124758&message=123123 is your
			// OTP to Login to Online Portal for Reservation of Meghalaya House
			// Accommodation. Do not share this OTP with anyone for security
			// reasons.Regards,
			// GAD(A)&signature=MLGADA&dlt_entity_id=1401504830000041324&dlt_template_id=1407170108988285837";
			String data = "username=" + getNotification(messageId).getUsername() + "&pin="
					+ getNotification(messageId).getPin() + "&mnumber=91" + recipientMobileNo + "&message=" + message2
					+ "&signature=" + getNotification(messageId).getSignature() + "&dlt_entity_id="
					+ getNotification(messageId).getEntityId() + "&dlt_template_id="
					+ getNotification(messageId).getTemplateId();
			//System.out.println("Data is: "+data);
			byte[] out = data.getBytes(StandardCharsets.UTF_8);
			try (OutputStream stream = http.getOutputStream()) {
				stream.write(out);
			}

			// Get response code
			int responseCode = http.getResponseCode();
			//System.out.println("Response Code: "+responseCode);

			// Read response
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				responseString = response.toString();
				System.out.println("Response String: "+responseString);			 
			}
			http.disconnect();
		} catch (IOException e) {			
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void sendSms(Integer actionCode, String appNo, String mobileNo, HttpServletRequest httpRequest) {
    	String[] params = { appNo };
        String templateCode = actionCode == 6 ? "DECLINED" : actionCode == 4 ? "ALLOTTED" : null;

        if (templateCode != null) {
            sendOnlySms(httpRequest, templateCode, mobileNo, params);
        }
    }
}
