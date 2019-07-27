package io.aurora.spring.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import io.aurora.spring.security.oauth.UserAuthenticationInterceptor;

/**
 * Configured Interceptor for specific endpoints.
 *
 */
@Configuration
public class SecurityConfig extends WebMvcConfigurerAdapter {

	@Value("${security.interceptor.flag}")
	private boolean intercepterFlag;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (this.intercepterFlag) {
			UserAuthenticationInterceptor userAuthIntercept = new UserAuthenticationInterceptor();
			registry.addInterceptor(userAuthIntercept).addPathPatterns("/services/expulse/**");
		}
	}
}