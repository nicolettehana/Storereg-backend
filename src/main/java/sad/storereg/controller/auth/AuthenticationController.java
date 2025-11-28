package sad.storereg.controller.auth;

import static sad.storereg.models.auth.Role.USER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.auth.AuthenticationRequest;
import sad.storereg.dto.auth.AuthenticationResponse;
import sad.storereg.dto.auth.GetOtpRequestDTO;
import sad.storereg.dto.auth.GetOtpResponseDTO;
import sad.storereg.dto.auth.RegisterRequest;
import sad.storereg.dto.auth.ResetPasswordRequest;
import sad.storereg.dto.auth.VerifyOtpRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.repo.auth.UserRepository;
import sad.storereg.services.appdata.ReportsService;
import sad.storereg.services.auth.AuthenticationService;
import sad.storereg.services.auth.CaptchaService;
import sad.storereg.services.auth.OtpService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
	
	private final AuthenticationService authService;
	private final CaptchaService captchaService;
	private final UserRepository userRepo;
	private final OtpService otpService;
	private final ReportsService reportService;
	private final PasswordEncoder passwordEncoder;
	
	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws BadCredentialsException, UsernameNotFoundException, IOException {
//		CaptchaEntry captcha = captchaStore.get(request.getUuid().toString());
//	        
//	        System.out.println(captcha != null && captcha.getCaptcha().equals(request.getCaptcha()));
//	        System.out.println("User captcha is: "+request.getCaptcha()+" and the captcha store captcha is: "+captcha);
//	        System.out.println("The captcha Store is:"+captchaStore);
//	        if (captcha != null && captcha.getCaptcha().equals(request.getCaptcha())) {
//	            captchaStore.remove(request.getUuid().toString());
//	        }
//	        System.out.println("The captcha Store is:"+captchaStore);
		if (captchaService.validateCaptcha(request.getCaptchaToken().toString(), request.getCaptcha())) {
			return ResponseEntity.ok(authService.authenticate2(request, httpRequest, httpResponse));
		
		} else
			throw new UnauthorizedException("Invalid Captcha");
	}
	
	@PostMapping("/authenticate-1")
	public ResponseEntity<?> authenticateStep1(@Valid @RequestBody AuthenticationRequest request,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws BadCredentialsException, UsernameNotFoundException, IOException {
//		CaptchaEntry captcha = captchaStore.get(request.getUuid().toString());
//	        
//	        System.out.println(captcha != null && captcha.getCaptcha().equals(request.getCaptcha()));
//	        System.out.println("User captcha is: "+request.getCaptcha()+" and the captcha store captcha is: "+captcha);
//	        System.out.println("The captcha Store is:"+captchaStore);
//	        if (captcha != null && captcha.getCaptcha().equals(request.getCaptcha())) {
//	            captchaStore.remove(request.getUuid().toString());
//	        }
//	        System.out.println("The captcha Store is:"+captchaStore);
		if (captchaService.validateCaptcha(request.getCaptchaToken().toString(), request.getCaptcha())) {
			return ResponseEntity.ok(authService.authStep1(request, httpRequest));
		
		} else
			throw new UnauthorizedException("Invalid Captcha");
	}

	@GetMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			authService.refreshToken(request, response);
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to refresh token.", ex);
		}
	}

	@GetMapping("/refresh-captcha")
	public ResponseEntity<Map<String, Object>> refreshCaptcha() {
		try {			
			return ResponseEntity.ok(captchaService.generateCaptcha());
		} catch (Exception ex) {
			throw new InternalServerError("Unable to refresh Captcha", ex);
		}
	}

	@GetMapping("/get-public-key")
	public ResponseEntity<Map<String, Object>> getPublicKey() {
		Map<String, Object> map = new HashMap<>();
		try {
			map.put("publicKey", authService.getPublicKey());
			return new ResponseEntity<>(map, HttpStatus.OK);
		} catch (Exception ex) {
			throw new InternalServerError("Unable to get Public Key", ex);
		}
	}
	
	@Transactional
	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest) {
		try {
			Map<String, String> map = new HashMap<>();
			
			request.setRole(USER);
			
			if (captchaService.validateCaptcha(request.getCaptchaToken().toString(), request.getCaptcha())) {
				authService.register(request);
			} else
				throw new UnauthorizedException("Inavlid Captcha");
			
			map.put("detail", "User Registered.");

			return new ResponseEntity<>(map, HttpStatus.OK);

		} catch (UnauthorizedException|InternalServerError ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to register user", ex);
		}
	}
	
	@PostMapping("/send-otp-signup")
	public ResponseEntity<Map<String, Object>> sendOtpSignUp(HttpServletRequest httpRequest, @Valid @RequestBody GetOtpRequestDTO request) {
		Map<String, Object> map = new HashMap<>();
		try {
//			if(request.getCaptcha().length()==0 || request.getCaptcha()==null) {
//				request.setMobileno(authService.decryptPassword(request.getMobileno()));
//				Optional<User> user = userRepo.findByUsername(request.getMobileno());
//
//				if(!otpService.lastGeneratedOTP(user.get().getUsername(), 1)) 
//					throw new UnauthorizedException("Not authorized");
//				otpService.sendOtpSignUp(httpRequest, user.get().getUsername());
//				//if (user.get().getMobileNo() != null)
//				map.put("message", "OTP has been sent to mobile number "+ "******".concat(user.get().getUsername().substring(6)));
//				
//				return new ResponseEntity<>(map, HttpStatus.OK);
//			}
			//else 
			//System.out.println("The captcha is: "+request.getCaptcha());
			if (captchaService.validateCaptcha(request.getUuid().toString(), request.getCaptcha())) {
				request.setMobileno(authService.decryptPassword(request.getMobileno()));
				Optional<User> user = userRepo.findByUsername(request.getMobileno());
				if (user.isPresent()) {
					throw new ObjectNotFoundException("User already registered");
				}
				if(request.getIsSignUp()!=null && request.getIsSignUp()==1) {
					
					GetOtpResponseDTO res = otpService.sendOtpSignUp(httpRequest, request.getMobileno(), "sign up");
					map.put("otpToken", res.getOtpToken());
				}
				map.put("message", "OTP has been sent to mobile number "+ "******".concat(request.getMobileno().substring(6)));
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				throw new UnauthorizedException("Inavlid Captcha");
			}
		} catch (UnauthorizedException|ObjectNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch OTP", ex);
		}	
	}
	
	@Auditable
	@PostMapping("/verify-otp-signup")
	public ResponseEntity<Map<String, String>> verifyOTPSignUp(@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest) {
		try {
			Optional<User> user = userRepo.findByUsername(authService.decryptPassword(request.getMobileNo()));
			//Optional<User> user = userRepo.findByUsername(request.getMobileno());

			if (user.isPresent()) {
				throw new InternalServerError("User already registered");
			}
			Map<String, String> map = new HashMap<>();

			if(otpService.verifyOTP(request.getOtp(),authService.decryptPassword(request.getMobileNo()), httpRequest, 1, request.getOtpToken().toString())) {
				request.setRole(USER);
				
				if (captchaService.validateCaptcha(request.getCaptchaToken().toString(), request.getCaptcha())) {
					authService.register(request);
				} else
					throw new UnauthorizedException("Inavlid Captcha");
				
				map.put("detail", "User Registered.");
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
			else {
				map.put("detail", "User not registered");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to verify OTP", ex);
		}
	}

	
	@Auditable
	@PostMapping("/verify-otp-login")
	public ResponseEntity<?> verifyOTPLogin(@Valid @RequestBody VerifyOtpRequestDTO request,
			HttpServletRequest httpRequest, HttpServletResponse response) {
		try {
			Optional<User> user = userRepo.findByUsername(authService.decryptPassword(request.getMobileno()));
			//Optional<User> user = userRepo.findByUsername(request.getMobileno());

			if (user.isEmpty()) {
				throw new InternalServerError("Invalid User");
			}

			if(otpService.verifyOTP(request.getOtp(),authService.decryptPassword(request.getMobileno()), httpRequest, 2, request.getUuid().toString())) {
				return ResponseEntity.ok(authService.getToken(request, httpRequest, response));
				//return new authService.getToken(request, httpRequest, response);
			}
			else {
				 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not registered");// new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to verify OTP", ex);
		}
	}

	
	@PostMapping("/send-otp-fp")
	public ResponseEntity<Map<String, Object>> sendOtpFP(HttpServletRequest httpRequest, @Valid @RequestBody GetOtpRequestDTO request) {
		Map<String, Object> map = new HashMap<>();
		try {
			if (captchaService.validateCaptcha(request.getUuid().toString(), request.getCaptcha())) {
				request.setMobileno(authService.decryptPassword(request.getMobileno()));
				Optional<User> user = userRepo.findByUsername(request.getMobileno());
				if (user.isEmpty()) {
					throw new ObjectNotFoundException("Invalid Username");
				}
				GetOtpResponseDTO res = otpService.sendOtpFP(httpRequest, request.getMobileno());
				map.put("otpToken", res.getOtpToken());
				map.put("message", "OTP has been sent to mobile number "+ "******".concat(request.getMobileno().substring(6)));
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				throw new UnauthorizedException("Inavlid Captcha");
			}
		} catch (UnauthorizedException|ObjectNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch OTP", ex);
		}	
	}
	
	@Auditable
	@PostMapping("/reset-forgot-password")
	public ResponseEntity<Map<String, String>> resetForgotPassword(@Valid @RequestBody ResetPasswordRequest request,
			HttpServletRequest httpRequest) {
		try {
			Optional<User> user = userRepo.findByUsername(authService.decryptPassword(request.getMobileNo()));

			if (user.isEmpty()) {
				throw new InternalServerError("User not found");
			}
			Map<String, String> map = new HashMap<>();
			if(otpService.verifyOTP(request.getOtp(), authService.decryptPassword(request.getMobileNo()), httpRequest, 2, request.getOtpToken())) {
				
		    	user.get().setPassword( passwordEncoder.encode(authService.decryptPassword(request.getPassword())));
				userRepo.save(user.get());
		    	map.put("message", "Password Reset Successfully");
		    	return new ResponseEntity<>(map, HttpStatus.OK);
			}
		     else {		    	
		    	 map.put("message", "Invalid OTP");
		    	return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
		    }
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch OTP", ex);
		}
	}
	
	
	
}
