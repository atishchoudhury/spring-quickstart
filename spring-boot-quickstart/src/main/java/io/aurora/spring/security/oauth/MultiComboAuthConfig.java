package io.aurora.spring.security.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.google.gson.Gson;


@EnableWebSecurity
public class MultiComboAuthConfig {

	public final Logger log = LoggerFactory.getLogger(MultiComboAuthConfig.class);
	
	private static final String HEADER_X_JWT = "X-JWT";

	@Value("${expulse.admin.username}")
	private String adminUser;

	@Value("${expulse.admin.password}")
	private String adminPassword;
	


	/**
	 * Common config across URLs
	 * 
	 */
	
	@Bean
	public CorsFilter corsFilter() {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true); // you USUALLY want this
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	
	
	/**
	 * @author Atish
	 *Security configuration for all Admin urls
	 *Basic Authorization Implementation
	 */
	
	@Bean
	public UserDetailsService userDetailsService() throws Exception {
		//Delegate the user base to database or Cache backed by database
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername(adminUser).password(adminPassword).roles("USER","ADMIN").build());
		return manager;
	}
	

	/**
	 * 
	 * 
	 * @author Atish
	 *
	 */
	@Configuration
	@Order(0)                                                        
	public static class AdminWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
	
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();
			http
			.antMatcher("/admin/**")
			.authorizeRequests()
			.anyRequest().hasRole("ADMIN")
			.and()
			.httpBasic();
		}
		
		/**
		 * This config will allow for swagger resources to be un-protected.
		 */
		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring()
				.antMatchers("/v2/api-docs", 
						"/configuration/**", 
						"/swagger-resources/**",
						"/swagger-ui.html", 
						"/webjars/**",
						"/api-docs/**",
						"/static/**"
						);
		}
	}
	
	
	@Autowired
	OAuthResourceServerSpringConfig oAuthResourceServerSpringConfig;
	
	@Bean
	protected ResourceServerConfiguration oldTokenResource() {

		ResourceServerConfiguration resource = new ResourceServerConfiguration() {	
			// Switch off the Spring Boot @Autowired configurers
			public void setConfigurers(List<ResourceServerConfigurer> configurers) {
				super.setConfigurers(configurers);
			}
		};
		resource.setConfigurers(Arrays.<ResourceServerConfigurer> asList(oAuthResourceServerSpringConfig));
		resource.setOrder(3);

		return resource;

	}


	@Autowired
	OAuthJwtResourceServerSpringConfig oAuthJwtResourceServerSpringConfig;
	
	@Bean
	protected ResourceServerConfiguration newJwtResource() {

		ResourceServerConfiguration resource = new ResourceServerConfiguration() {	
			// Switch off the Spring Boot @Autowired configurers
			public void setConfigurers(List<ResourceServerConfigurer> configurers) {
				super.setConfigurers(configurers);
			}
		};
		resource.setConfigurers(Arrays.<ResourceServerConfigurer> asList(oAuthJwtResourceServerSpringConfig));
		resource.setOrder(2);

		return resource;

	}
	
	
	
	/**
	 * @author Atish
	 *Security configuration for all service urls
	 *oauth2 Authorization Implementation
	 */
	@Configuration     
	public static class OAuthResourceServerSpringConfig extends ResourceServerConfigurerAdapter {

		private final Logger log = LoggerFactory.getLogger(OAuthResourceServerSpringConfig.class);
		
		@Value("${oauth2.server.url}")
		private  String checkTokenUrl;
		
		@Value("${oauth2.server.clientId}") 
		private String clientId;
		
		@Value("${oauth2.server.clientSecret}")
		private String clientSecret;
		
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();
				http
				.requestMatcher(new RequestMatcher() {
					@Override
					public boolean matches(HttpServletRequest request) {
						log.info("matching for OAuthResourceServerSpringConfig and header {}",request.getHeader(HEADER_X_JWT));
						if(request.getHeader(HEADER_X_JWT) == null || !request.getHeader(HEADER_X_JWT).equalsIgnoreCase("yes")) {
							return true;
						}
						return false;
					}
				})
				.authorizeRequests()
				.antMatchers("/api/**").authenticated();
		}
		
		

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
			super.configure(resources);
			resources.tokenServices(remoteTokenServices());
		}
		
		
		/**
		 * Default AccessTokenConverter used.
		 * 
		 * @return DefaultAccessTokenConverter
		 */
		
		public AccessTokenConverter accessTokenConverter() {
			DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
			DefaultUserAuthenticationConverter userTokenConverter = new DefaultUserAuthenticationConverter();
			accessTokenConverter.setUserTokenConverter(userTokenConverter);
			return accessTokenConverter;
		}
		/**
		 * Validate parameters with oauth server.
		 * 
		 * @param checkTokenUrl - oauth server url
		 * @param clientId - id to access server
		 * @param clientSecret - secret to access server
		 * @return
		 */
		
		public RemoteTokenServices remoteTokenServices() {
			final RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
			remoteTokenServices.setCheckTokenEndpointUrl(this.checkTokenUrl);
			remoteTokenServices.setClientId(this.clientId);
			remoteTokenServices.setClientSecret(this.clientSecret);
			remoteTokenServices.setAccessTokenConverter(this.accessTokenConverter());
			return remoteTokenServices;
		}
	}
	
	
	
	@Configuration 
	public static class OAuthJwtResourceServerSpringConfig extends ResourceServerConfigurerAdapter {

		private final Logger log = LoggerFactory.getLogger(OAuthJwtResourceServerSpringConfig.class);
	    
	    RestTemplate restTemplate = new RestTemplate();
	    
	    @Value("${workspace.auth.jwt.public.key.url}")
	    private String oauthServerJwtPublicKeyUrl;
	    
	    @Value("${workspace.auth.jwt.aud.url}")
	    private String resourceID;  	 
	    	     

		 
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();
			http
			.requestMatcher(new RequestMatcher() {
				@Override
				public boolean matches(HttpServletRequest request) {
					log.info("matching for OAuthJwtResourceServerSpringConfig and header {}",request.getHeader(HEADER_X_JWT));
					if( request.getHeader(HEADER_X_JWT) != null && request.getHeader(HEADER_X_JWT).equalsIgnoreCase("yes") ) {
						return true;
					}
					return false;
				}
			})
			.authorizeRequests()		
            .antMatchers("/api/**").authenticated();
	    }
		
		@Override
	    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
	         resources.resourceId(resourceID);
	         resources.tokenStore(tokenStore());
	         
	    }
	    

	    public TokenStore tokenStore() throws Exception {
	    	return new JwtTokenStore(jwtTokenEnhancer());
	    }


	    protected JwtAccessTokenConverter jwtTokenEnhancer() throws Exception {
	        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
	        converter.setVerifierKey(this.getJwtPublicKey());
	        //converter.setSigningKey(this.getJwtPublicKey()); //Only for IDP. Not needed for Res Server
	        converter.setAccessTokenConverter(this.accessTokenConverter());
	        converter.afterPropertiesSet(); //Calling manually as not creating a Bean. It's important

	        return converter;
	    }
	    
	    public AccessTokenConverter accessTokenConverter() {
	    	DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
	    	CustomUserAuthenticationConverter userTokenConverter = new CustomUserAuthenticationConverter();
	    	accessTokenConverter.setUserTokenConverter(userTokenConverter);
	    	return accessTokenConverter;
	    }

	    private String getJwtPublicKey() {
	        JwtPublicKeyResponse pk =null;
			KeyFactory f;
			try {
			    String obj = this.restTemplate.getForObject(this.oauthServerJwtPublicKeyUrl, String.class, new String());
				pk = new Gson().fromJson(obj, JwtPublicKeyResponse.class);
			    f = KeyFactory.getInstance("RSA");
				BigInteger modulus = new BigInteger(pk.getMod());
			    BigInteger exp = new BigInteger(pk.getExp());
			    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exp);
			    PublicKey pub = f.generatePublic(spec);
			    byte[] data = pub.getEncoded();
			    String base64encoded = new String(Base64.getEncoder().encode(data));
			    base64encoded = "-----BEGIN PUBLIC KEY-----\n" + base64encoded + "\n-----END PUBLIC KEY-----";
			    pk.setValue(base64encoded);
			} catch (NoSuchAlgorithmException e) {
				log.error("NoSuchAlgorithmException while validating JWT token" + e);
			} catch (InvalidKeySpecException e) {
				log.error("InvalidKeySpecException while validating JWT token" + e);
			}  catch (Exception e) {
	            log.error("InvalidKeySpecException while validating JWT token" + e);
	        } 
			if (pk.getValue() == null) {
			    throw new RuntimeException(
				    "Error retrieving JWT Public Key. Probably JWT Keystore is not configured in OAuth Server");
			}
			log.debug("JWT Pk:{}", pk.toString());
			return pk.getValue();
	    }

	}
	

}
