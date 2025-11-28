package sad.storereg.services.auth;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.auth.GetOtpResponseDTO;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.logs.AuditService;
import sad.storereg.models.auth.Otp;
import sad.storereg.models.auth.User;
import sad.storereg.repo.auth.OtpRepository;
import sad.storereg.services.appdata.CoreServices;
import sad.storereg.services.appdata.ServiceNotification;

@Service
@RequiredArgsConstructor
public class OtpService {
	
	private final OtpRepository otpRepo;
	private final ServiceNotification serviceNotification;
	private final CoreServices coreServices;
	private final AuditService serviceAuditTrail;
	
	private static final Integer EXPIRE_MIN = 60;
	
	private static String generateOTP() {
		int otpLength = 6;

		SecureRandom random = new SecureRandom();

		StringBuilder otp = new StringBuilder(otpLength);
		for (int i = 0; i < otpLength; i++) {
			int digit = random.nextInt(10);
			otp.append(digit);
		}
		return otp.toString();
	}

	
	public boolean lastGeneratedOTP(String username, int isSignUp) {
		Optional<Otp> optOtp = otpRepo.findByUsername(username);
		if(isSignUp==1)
			optOtp = otpRepo.findByUsernameAndIsSignUpEquals(username, 1);
		else if(isSignUp==2)
			optOtp = otpRepo.findByUsernameAndForgotPasswordEquals(username, 1);
		if(optOtp.isEmpty()) {
			throw new UnauthorizedException("Not authorized");
		}
		Instant instant = optOtp.get().getGeneratedAt().toInstant();
        LocalDateTime timestamp = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        
		long differenceInMinutes = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now());

        return differenceInMinutes <= 60?true:false;
	}
	
	public GetOtpResponseDTO sendOtpSignUp(HttpServletRequest httpRequest, String mobileNo, String param) {
		String otp = generateOTP();
		otp = "123123";
		String[] params = { otp, param};
		CompletableFuture<Void> otpFuture = serviceNotification.sendOnlySms(httpRequest, "OTP_SP", mobileNo,
				params);
		Integer code = param.equals("sign up")? 1: 2; 
		Optional<Otp> optOtp = otpRepo.findByUsernameAndIsSignUpEquals(mobileNo,code);
		if(optOtp.isPresent())
		{
			otpRepo.delete(optOtp.get());
		}
		String otpToken = UUID.randomUUID().toString();
		Otp otpObj = Otp.builder().otp(otp).username(mobileNo)
				.generatedAt(Timestamp.valueOf(LocalDateTime.now())).count(0).isSignUp(code).otpToken(otpToken).build();
		otpRepo.save(otpObj);

		// Here send the OTP to mobile. SMS Service
		return GetOtpResponseDTO.builder().otp(otp).expiry(EXPIRE_MIN.toString()).otpToken(otpToken).build();
	}
	
	public boolean verifyOTP(String otp, String mobileNo, HttpServletRequest httpRequest, int isSignUp, String otpToken) {
		if (verifyOTP(mobileNo, otp, isSignUp, otpToken)) {
			
			//userService.createPasswordResetTokenForUser(user, token);
			Instant now = Instant.now();

	        // Add minutes to the current timestamp
	        Instant futureTime = now.plus(10, ChronoUnit.MINUTES);

	        // Convert future time to Timestamp
	        Timestamp expiry = Timestamp.from(futureTime);
			//PasswordResetToken myToken = PasswordResetToken.builder()
			//		.user(user.getId()).expiry(expiry).token(token).build();
		    //passwordTokenRepo.save(myToken);

			serviceAuditTrail.saveAuditTrail("Verify OTP",mobileNo, "/verify-otp-signup", "OTP Verified","Success",
					coreServices.getClientIp(httpRequest),null);
			return true;
			//return AuthenticationResponseDTO.builder().accessToken(jwtToken).refreshToken(refreshToken)
			//		.role(user.getRole().toString()).build();
		} else {
			serviceAuditTrail.saveAuditTrail("Verify OTP",mobileNo, "/verify-otp-signup", "OTP verification failed", "Failed",
					coreServices.getClientIp(httpRequest),null);
			return false;
		}
		//throw new UnauthorizedException("Invalid OTP");
	}
	
	private boolean verifyOTP(String username, String otp, int forgotPassword, String otpToken) {
		//System.out.println("Username: "+username+" otp: "+otp+" fogot password: "+forgotPassword);
		//Optional<Otp> optOtp = otpRepo.findByUsername(username);
		Optional<Otp> optOtp;
		if(forgotPassword==0)
			optOtp = otpRepo.findByUsernameAndIsSignUpEqualsAndOtpTokenEquals(username, 0, otpToken);
		else if(forgotPassword==1)
		{
			optOtp = otpRepo.findByUsernameAndIsSignUpEqualsAndOtpTokenEquals(username, 1, otpToken);
		}
		else if(forgotPassword==2)
		{
			optOtp = otpRepo.findByUsernameAndIsSignUpEqualsAndOtpTokenEquals(username, 2, otpToken);
		}
		else
			optOtp = otpRepo.findByUsernameAndForgotPasswordEqualsAndOtpTokenEquals(username, 1, otpToken);
		if (optOtp.isPresent()) {
			if (optOtp.get().getOtp().equals(otp) && forgotPassword==0) {

				Duration duration = Duration.between(optOtp.get().getGeneratedAt().toInstant(),
						Timestamp.valueOf(LocalDateTime.now()).toInstant());
				otpRepo.deleteById(optOtp.get().getId());
				if (duration.toMinutes() > EXPIRE_MIN)
					throw new UnauthorizedException("OTP Expired");

				return true;
			}
			else if(optOtp.get().getOtp().equals(otp) && forgotPassword==1)
			{
				otpRepo.deleteById(optOtp.get().getId());
				return true;
			}
			else if(optOtp.get().getOtp().equals(otp) && forgotPassword==2)
			{
				otpRepo.deleteById(optOtp.get().getId());
				return true;
			}
			else
				return false;
		} else
			return false;
	}

	
	public GetOtpResponseDTO sendOtpUpdateMobile(HttpServletRequest httpRequest, User user, String mobileNo) {
		String otp = generateOTP();
		otp = "123123";
		String[] params = { otp, "update no." };
		CompletableFuture<Void> otpFuture = serviceNotification.sendOnlySms(httpRequest, "OTP_SP", mobileNo,
				params);
		Optional<Otp> optOtp = otpRepo.findByUsernameAndIsSignUpEquals(mobileNo,0);
		if(optOtp.isPresent())
		{
			otpRepo.delete(optOtp.get());
		}
		String otpToken=UUID.randomUUID().toString();
		Otp otpObj = Otp.builder().otp(otp).username(mobileNo)
				.generatedAt(Timestamp.valueOf(LocalDateTime.now())).count(0).isSignUp(0).otpToken(otpToken).build(); //0 is for update mobile no
		otpRepo.save(otpObj);

		// Here send the OTP to mobile. SMS Service
		return GetOtpResponseDTO.builder().otp(otp).expiry(EXPIRE_MIN.toString()).otpToken(otpToken).build();
	}
	
	public GetOtpResponseDTO sendOtpFP(HttpServletRequest httpRequest, String mobileNo) {
		String otp = generateOTP();
		otp = "123123";
		String[] params = { otp, "reset password" };
		CompletableFuture<Void> otpFuture = serviceNotification.sendOnlySms(httpRequest, "OTP_SP", mobileNo,
				params);
		Optional<Otp> optOtp = otpRepo.findByUsernameAndForgotPasswordEquals(mobileNo,1);
		if(optOtp.isPresent())
		{
			otpRepo.delete(optOtp.get());
		}
		String otpToken = UUID.randomUUID().toString();
		Otp otpObj = Otp.builder().otp(otp).username(mobileNo)
				.generatedAt(Timestamp.valueOf(LocalDateTime.now())).count(0).forgotPassword(1).otpToken(otpToken).build();
		otpRepo.save(otpObj);

		// Here send the OTP to mobile. SMS Service
		return GetOtpResponseDTO.builder().otp(otp).expiry(EXPIRE_MIN.toString()).otpToken(otpToken).build();
	}
}
