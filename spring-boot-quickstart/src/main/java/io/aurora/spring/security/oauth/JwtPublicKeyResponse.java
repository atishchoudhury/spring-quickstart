package io.aurora.spring.security.oauth;

import java.io.Serializable;


public class JwtPublicKeyResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private String alg;
	private String value;
	private String mod;
	private String exp;
	private String kid;
	
	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMod() {
		return mod;
	}

	public void setMod(String mod) {
		this.mod = mod;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}

	@Override
	public String toString() {
		return "JwtPublicKeyResponse [alg=" + alg + ", value=" + value + ", mod=" + mod + ", exp=" + exp + ", kid="
				+ kid + "]";
	}

}
