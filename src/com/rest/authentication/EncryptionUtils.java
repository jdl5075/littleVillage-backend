package com.rest.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class EncryptionUtils {
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	Logger logger = Logger.getLogger(EncryptionUtils.class);
	
	//Add salt
    public static String getSalt() throws NoSuchAlgorithmException
    {
    	byte[] salt = new byte[32];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(salt);
        return Base64.encodeBase64URLSafeString(salt);
    }
    
	/**
	 * Computes RFC 2104-compliant HMAC signature. * @param data The data to be
	 * signed.
	 * 
	 * @param key
	 *            The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws java.security.SignatureException
	 *             when signature generation fails
	 */
	public static byte[] encrypt(char[] data, String key)
			throws java.security.SignatureException {
		String result;
		byte[] encodedBytes;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(new String(data).getBytes("UTF-8"));

			// base64-encode the hmac
			encodedBytes = Base64.encodeBase64(rawHmac);
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : "
					+ e.getMessage());
		}
		return encodedBytes;
	}
}
