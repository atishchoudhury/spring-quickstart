package io.aurora.spring.security.oauth;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Interceptor to get user details from oauth server.
 *
 */
public class UserAuthenticationInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(UserAuthenticationInterceptor.class);
	/**
	 * Overridden method handle handle the Authentication.
	 * 
	 * @param HttpServletRequest - request object
	 * @param HttpServletResponse - response object
	 * @param Object - handler
	 * @return boolean - true 
	 * @throws Exception - If user authentication fails
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		boolean flag=false;;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String user = this.getSSOUserDetails(authentication);
		if(user!=null){
			logger.debug("Interceptor for user: "+user);
			//Always passing the username received from vidm in lowercase to the services.
			request.setAttribute(SSOUser.USERID, user.toLowerCase(Locale.ENGLISH));
			MDC.put("user", user);
			flag=true;
		}
		return flag;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
		MDC.clear();
	}
	/**
	 * Extract userId from AccessToken. 
	 * 
	 * @param authentication - oauth object
	 * @return String - userid
	 */
	private String getSSOUserDetails(Authentication authentication) {
		
		logger.debug("Authentication object is {}",authentication);
		String username = null;
		try {
			
			Object pUser = authentication.getPrincipal();
			String ssouser = null;
			if(pUser instanceof SSOUser) {
				SSOUser user = (SSOUser)pUser;
				ssouser = user.getEmail();
				logger.debug("EMAIL ID GOT FROM OAUTH JWT TRANFORMER -- JWT {}",ssouser);
			
				if (ssouser == null || StringUtils.isEmpty(ssouser)) {
					throw new UsernameNotFoundException("Invalid token");
				} 

				username = ssouser.trim();
				//Get the user id from email id.
				int index = ssouser.indexOf('@');
				username = username.substring(0,index);
				logger.debug("GET THE EMAIL ADDRESS DIRECTLY FROM SSOUSER {} and username", ssouser,username);
			}else if (pUser instanceof String) {
				username = (String)pUser;
			}
		} catch (Exception e) {
			logger.error("Error in getting SSO users", e);

		}
		return username;
	}


}