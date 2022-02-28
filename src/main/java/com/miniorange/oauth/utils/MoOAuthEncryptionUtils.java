package com.miniorange.oauth.utils;

/*
 * NOTICE: This software  source code and any of  its derivatives are the
 * confidential  and  proprietary   information  of  miniOrange Security 
 * Software Pvt. Ltd. Inc. (such source  and its derivatives are hereinafter  
 * referred to as "Confidential Information"). The Confidential Information 
 * is intended to be  used exclusively by individuals or entities that  have 
 * entered into either  a non-disclosure agreement or license  agreement 
 * (or both of  these agreements,  if  applicable) with  miniOrange Security 
 * Software Pvt. Ltd. ("miniOrange") regarding  the  use   of  the   
 * Confidential  Information. Furthermore,  the  Confidential  Information  
 * shall be  used  only  in accordance  with   the  terms   of  such  license   
 * or  non-disclosure agreements.   All  parties using  the  Confidential 
 * Information  shall verify that their  intended use of the Confidential  
 * Information is in compliance  with and  not in  violation of  any applicable  
 * license or non-disclosure  agreements.  Unless expressly  authorized by  
 * miniOrange in writing, the Confidential Information  shall not be printed, 
 * retained, copied, or  otherwise disseminated,  in part or  whole.  
 * Additionally, any party using the Confidential  Information shall be held 
 * liable for any and  all damages incurred  by miniOrange due  to any disclosure  
 * of the Confidential  Information (including  accidental disclosure).   
 * In the event that  the applicable  non-disclosure or license  agreements with
 * miniOrange have  expired, or  if  none  currently  exists, all  copies  of
 * Confidential Information in your  possession, whether in electronic or
 * printed  form, shall be  destroyed or  returned to  miniOrange immediately.
 * miniOrange makes no  representations  or warranties  hereby regarding  the
 * suitability  of  the   Confidential  Information,  either  express  or
 * implied,  including  but not  limited  to  the  implied warranties  of
 * merchantability,    fitness    for    a   particular    purpose,    or
 * non-infringement. miniOrange shall not be liable for  any damages suffered
 * by  licensee as  a result  of  using, modifying  or distributing  this
 * Confidential Information.  Please email [info@miniOrange.co.in]  with any
 * questions regarding the use of the Confidential Information.
 */
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;

public class MoOAuthEncryptionUtils {

	public static final String INIT_VECTOR = generateRandomAlphaNumericKey(16);

	private static final String ENCRYPTION_ALGORITHM = "AES";

	public static String encrypt(String encryptionKey, String data) {
		try {
			byte[] decodedKey = encryptionKey.getBytes();
			byte[] truncatedKey = (decodedKey.length > 16) ? Arrays.copyOf(decodedKey, 16) : decodedKey;

			Key key = new SecretKeySpec(truncatedKey, ENCRYPTION_ALGORITHM);

			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] encVal = cipher.doFinal(data.getBytes());

			String encryptedValue = Base64.encodeBase64String(encVal);
			return encryptedValue;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return StringUtils.EMPTY;
	}

	public static String decrypt(String encryptionKey, String data) {
		try {
			byte[] decodedDataString = Base64.decodeBase64(data);
			byte[] decodedKey = encryptionKey.getBytes();
			byte[] truncatedKey = (decodedKey.length > 16) ? Arrays.copyOf(decodedKey, 16) : decodedKey;

			Key key = new SecretKeySpec(truncatedKey, ENCRYPTION_ALGORITHM);

			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decVal = cipher.doFinal(decodedDataString);

			String decryptedValue = new String(decVal);
			return decryptedValue;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return StringUtils.EMPTY;
	}

	public static String generateRandomAlphaNumericKey(int bytes) {
		String randomString = RandomStringUtils.random(bytes, true, true);
		return randomString;
	}
}