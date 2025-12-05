package sad.storereg.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static sad.storereg.models.auth.Role.ADMIN;
import static sad.storereg.models.auth.Role.CH;
import static sad.storereg.models.auth.Role.EST;
import static sad.storereg.models.auth.Role.USER;
import static sad.storereg.models.auth.Role.SAD;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import sad.storereg.logs.AuditService;
import sad.storereg.services.appdata.CoreServices;
import sad.storereg.services.auth.LogoutService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	// private final LogoutHandler logoutHandler;
	@Autowired
	private final LogoutService logoutService;
	@Autowired
	private Environment env;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf().disable()
		//http.csrf(csrf -> csrf
		//	    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		//	)
//		http.csrf(csrf -> csrf
//				.ignoringRequestMatchers("/auth/authenticate") 
//			    .csrfTokenRepository(csrfTokenRepository())
//			)
//		http.csrf(csrf -> csrf
//	            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // JS can read it
//	            .ignoringRequestMatchers(
//	                    // All POST endpoints
//	                    "/users/change-password",
//	                    "/users/update",
//	                    "/users/verify-otp-update-mobile",
//	                    "/users/send-otp-update-mobile",
//	                    "/quarters",
//	                    "/vacate/**",
//	                    "/occupants/add",
//	                    "/allotment/occupy",
//	                    "/allotment/get-applicant-letter",
//	                    "/vacate-request/accept",
//	                    "/vacate-request/reject",
//	                    "/occupants/add-quarter-occupant",
//	                    "/application/upload-approval-order",
//	                    "/eproposal-request",
//	                    "/application",
//	                    "/application/cancel",
//	                    "/application/generate",
//	                    "/application/upload",
//	                    "/vacate-request",
//	                    "/application/action",
//	                    "/allotment/order",
//	                    "/allotment/cancel",
//	                    "/users/enable-disable/**",
//	                    "/application/all",
//	                    "/allotment/letter",
//	                    "/allotment/completed",
//	                    "/allotment/order-upload",
//	                    "/allotment/order-final-upload",
//	                    "/allotment/upload-decision-letter",
//	                    "/vacate-doucment",
//	                    "/vacate-document/upload**",
//	                    "/auth/**",
//	                    "/auth/authenticate" // optional login endpoint
//	                )
//	                // Add PUT if needed
//	                .ignoringRequestMatchers(
//	                    "/quarters/**",
//	                    "/enable-disable/**"
//	                )
//	            )
		.authorizeHttpRequests(auth -> auth .requestMatchers("/auth/**", "/csrf-token","/api/**","/my-report","/api2").permitAll() 
				.requestMatchers(GET, "/users/get-user-info").hasAnyAuthority(USER.name(), ADMIN.name()) 
				.requestMatchers(GET, "/menu","/status","/year-range","/items/**","/category/**","/firms/**","/unit/**","rates/**","/purchase/**") .hasAnyAuthority(ADMIN.name(), CH.name(), USER.name(), EST.name(), SAD.name()) 
				.requestMatchers(GET, "/users/profile") .hasAnyAuthority(ADMIN.name(), CH.name(), USER.name(), EST.name(), SAD.name()) 
				.requestMatchers(POST, "/users/change-password","/users/update","/users/verify-otp-update-mobile","/users/send-otp-update-mobile","/verify-otp-login") .hasAnyAuthority(ADMIN.name(), CH.name(), USER.name(), EST.name()) 
				.requestMatchers(POST, "/quarters","/vacate/**","/occupants/add","/allotment/occupy","/allotment/get-applicant-letter","/vacate-request/accept","/vacate-request/reject", "/occupants/add-quarter-occupant") .hasAnyAuthority(EST.name()) 
				.requestMatchers(PUT, "/quarters/**","/enable-disable/**") .hasAnyAuthority(EST.name()) 
				.requestMatchers(POST, "/firms/**","/items","/rates","/purchase/create").hasAnyAuthority(SAD.name())
				.requestMatchers(GET, "/vacate-request/pending/**","/vacate-request/completed/**","/vacate-request/stats","/quarter-occupancy-status/**", "/quarters/full-details/**") .hasAnyAuthority(EST.name()) 
				.requestMatchers(POST, "/application/upload-approval-order") .hasAnyAuthority(CH.name()) 
				.requestMatchers(POST,"/eproposal-request","/eproposal-insert-allotment-status").hasAnyAuthority(USER.name(), CH.name()) 
				.requestMatchers(POST, "/application","/application/cancel","/application/generate","/application/upload","/vacate-request").hasAnyAuthority(USER.name()) 
				.requestMatchers(GET, "/application/summary","/vacate-request","/vacate-request/get-available-quarters").hasAnyAuthority(USER.name()) .requestMatchers(GET,"/application","/application/hey", "/application/download/**","/application/remarks/**").hasAnyAuthority(USER.name(), CH.name(),EST.name()) 
				.requestMatchers(GET,"/audit-trail/**","/users/all/**").hasAnyAuthority(ADMIN.name()) .requestMatchers(POST,"/users/enable-disable/**").hasAnyAuthority(ADMIN.name()) .requestMatchers(POST,"/application/action","/allotment/order").hasAnyAuthority(USER.name(), CH.name(), EST.name()) .requestMatchers(POST,"/allotment/cancel").hasAnyAuthority( EST.name()) 
				.requestMatchers(GET,"/departments","/districts","/blocks/**","/villages/**").hasAnyAuthority(CH.name(), EST.name()) .requestMatchers(GET, "/waiting-list","/waiting-list/approved/**","/waiting-list/applications/**","/quarter-types","/quarters/vacant/**","/quarters/**","/allotment/**", "/waiting-list/previous/**","/application/remarks-da/**","/quarters/vacant-reserved","/quarters/stats","/quarter-status") .hasAnyAuthority(CH.name(), EST.name()) 
				.requestMatchers(GET, "/application/qc","/allotment/request/**","/allotment/pending","/application/bck","/eproposal-get-status/**").hasAnyAuthority(CH.name()) 
				.requestMatchers(POST, "/application/all", "/allotment/letter","/allotment/completed","/allotment/order-upload","/allotment/order-final-upload").hasAnyAuthority(CH.name()) 
				.requestMatchers(GET, "/app-history/**").hasAnyAuthority(CH.name()) 
				.requestMatchers(POST, "/allotment/upload-decision-letter","/vacate-doucment","/vacate-document/upload**").hasAnyAuthority(EST.name(),USER.name()) 
				.requestMatchers(GET, "/vacate-document/**").hasAnyAuthority(EST.name(),USER.name()) )
		
				
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				
				//.and().sessionManagement(session -> session
				//	      .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))//.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			    //.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			    //.and()

				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.logout(logout -> logout.logoutUrl("/auth/logout")
						// .addLogoutHandler(logoutHandler)
						.logoutSuccessHandler(logoutService).invalidateHttpSession(true).deleteCookies("JSESSIONID"));
//						.addLogoutHandler((request, response, authentication) -> {
//			                if (authentication != null) {
//			                    String username = authentication.getName();
//			                    String clientIp = coreServices.getClientIp(request);
//
//			                    auditService.saveAuditTrail(
//			                        "logout",
//			                        username,
//			                        request.getRequestURI(),
//			                        "User logged out",
//			                        "Success",
//			                        clientIp,
//			                        200
//			                    );
//			                }
//			            })
//			            .logoutSuccessHandler((request, response, authentication) -> {
//			                response.setStatus(HttpServletResponse.SC_OK);
//			            }).invalidateHttpSession(true).deleteCookies("JSESSIONID"));
				
		
		
		
		
		//.logout(logout -> logout.logoutUrl("/auth/logout")
				//this.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "GET"))
						 //.addLogoutHandler(logoutHandler));
						//this..logoutSuccessHandler(logoutService));
		
						//.logoutSuccessHandler(logoutService).invalidateHttpSession(true).deleteCookies("hey","refresh_token"));
		// .logoutUrl("/auth/logout").addLogoutHandler(logoutHandler)
		// .logoutSuccessHandler((request, response, authentication) ->
		// SecurityContextHolder.clearContext());

		String urls = env.getProperty("cors.urls");

		http.cors(cors -> {
			CorsConfigurationSource cs = resources -> {
				CorsConfiguration corsConfiguration = new CorsConfiguration();
				corsConfiguration.setAllowedOrigins(List.of(urls));
				// corsConfiguration.setAllowedOrigins(List.of("*"));
				corsConfiguration.setAllowedMethods(List.of("POST", "GET", "DELETE", "PUT", "OPTIONS"));
				corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With",
						"Accept", "X-XSRF-TOKEN", "API-Key", "Referrer-Policy", "Referer", "Content-Length",
						"Cache-Control", "Upgrade-Insecure-Requests"
				// Add forwarding-related headers here
				// "X-Forwarded-For",
				// "X-Forwarded-Host",
				// "X-Forwarded-Proto",
				// "X-Forwarded-Port"
				));
				corsConfiguration.setAllowCredentials(true);
				return corsConfiguration;
			};
			cors.configurationSource(cs);
		});
		http.headers(x -> {
			x.xssProtection(y -> y.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));
			x.addHeaderWriter(new XContentTypeOptionsHeaderWriter());
			// x.frameOptions(y -> y.sameOrigin());
//	    	//x.contentTypeOptions().disable();
	    	x.referrerPolicy(y -> y.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
			// x.contentSecurityPolicy(y-> y.policyDirectives("default-src 'self';"
//			+"script-src 'self' frame-src 'self' connect-src 'self' https://192.168.11.2 https://192.168.11.2:8443 http://192.168.11.2 http://192.168.11.2:8080 https://gad.meghalaya.gov.in https://gad.meghalaya.gov.in:8443 http://gad.meghalaya.gov.in http://gad.meghalaya.gov.in:8080 https://localhost https://localhost:8443 http://localhost http://localhost:8080"));
//	x.contentSecurityPolicy(y-> y.policyDirectives("default-src 'self';"
//+"script-src 'self' frame-src 'self' connect-src 'self' https://gad.meghalaya.gov.in https://gad.meghalaya.gov.in:8443"));
			x.contentSecurityPolicy(
					y -> y.policyDirectives("default-src 'self'; object-src https://megepayment.gov.in; "
							+ "script-src 'self'; frame-src 'self'; connect-src 'self' http://10.179.13.183:8084;"));
//	    	 // Add CORS headers manually
//	        x.addHeaderWriter((request, response) -> {
//	            response.setHeader("Access-Control-Allow-Origin", "http://10.179.2.178/");
//	            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//	            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept,X-XSRF-TOKEN, API-Key, Referrer-Policy, Referer,Content-Length, Cache-Control,Upgrade-Insecure-Requests");
//	            response.setHeader("Access-Control-Allow-Credentials", "true");
//	        });
		});

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/quarters/auth/**")
		 .requestMatchers("/v3/api-docs/**")
		 .requestMatchers("configuration/**")
		 .requestMatchers("/swagger*/**")
		 .requestMatchers("/webjars/**")
		 .requestMatchers("/swagger-ui/**");
	}

//	@Bean
//    public LogoutSuccessHandler logoutSuccessHandler() {
//		return new LogoutService();
//        //SimpleUrlLogoutSuccessHandler successHandler = new SimpleUrlLogoutSuccessHandler();
//        //successHandler.setDefaultTargetUrl("/login?logout");
//        //return successHandler;
//    }
//	@Bean
//	public CsrfTokenRepository csrfTokenRepository() {
//	    var repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
//	    repo.setCookiePath("/");  // ensure the cookie path is root (not /quarters)
//	    //repo.setCookieMaxAge(10 * 60);
//	    // Optional: customize:
//	    //repo.setCookieName("XSRF-TOKEN");
//	    //repo.setHeaderName("X-XSRF-TOKEN");
//	     repo.setCookieCustomizer(builder ->
//	         builder.sameSite("Lax").secure(true).httpOnly(false).maxAge(Duration.ofHours(1))
//	     );
//	    return repo;
//	}


}
