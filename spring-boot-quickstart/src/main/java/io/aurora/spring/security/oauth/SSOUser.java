package io.aurora.spring.security.oauth;

import java.io.Serializable;

public class SSOUser implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	public static final String USERID  = "userId";
	
	private String userId;
	
	private String email;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return "SSOUser [email=" + email + "]";
	}
	
}
