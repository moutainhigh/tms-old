package org.nw.web.utils;

import java.security.NoSuchAlgorithmException;

import org.springframework.dao.DataAccessException;

public class DigestPasswordEncoder extends BasePasswordEncoder {
	private boolean ignorePasswordCase = false;
	private String algorithm = "SHA";

	public String encodePassword(String rawPass, Object salt) throws DataAccessException {
		String saltedPass = mergePasswordAndSalt(rawPass, salt, false);
		try {
			String hashGuess = DigestUtil.digestString(saltedPass, algorithm);
			return hashGuess;
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String encodePassword(String rawPass, Object salt, String algorithm) throws DataAccessException {
		this.algorithm = algorithm;
		return encodePassword(rawPass, salt);
	}

	public boolean isIgnorePasswordCase() {
		return ignorePasswordCase;
	}

	public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException {
		String pass1 = "" + encPass;
		String pass2 = encodePassword(rawPass, salt);
		return pass1.equals(pass2);
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setIgnorePasswordCase(boolean ignorePasswordCase) {
		this.ignorePasswordCase = ignorePasswordCase;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String strTo40(String str) {
		if(str == null)
			return null;
		int strLen = str.length();
		if(str.length() > 40) {
			str = str.substring(0, 40);
		} else {
			int len = 40 - strLen;
			for(int i = 0; i < len; i++) {
				str += " ";
			}
		}

		return str;
	}

	public static void main(String[] args) {
		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		String newPwd = encoder.strTo40("sdfsdfe") + encoder.strTo40("111111");
		System.out.println(encoder.encodePassword("1", null));
	}
}
