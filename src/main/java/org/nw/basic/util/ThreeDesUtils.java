package org.nw.basic.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 字符x DESede(3DES) 加密 <br>
 * 加密算法,可用 DES,DESede,Blowfish
 */
public class ThreeDesUtils{
	/**
	 * 定义 加密算法,可用 DES,DESede,Blowfish
	 */
	private static final String ALGORITHM = "DESede";
	
	/**
	 * @param key
	 *        为加密密钥，长度x4字节
	 * @param src
	 *        被加密的数据缓冲区（源）
	 */
	public static byte[] encryptMode(byte[] key,byte[] src){
		try{
			// 生成密钥
			SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
			// 加密
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(src);
		}catch(java.security.NoSuchAlgorithmException e1){
			e1.printStackTrace();
		}catch(javax.crypto.NoSuchPaddingException e2){
			e2.printStackTrace();
		}catch(java.lang.Exception e3){
			e3.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param key
	 *        为加密密钥，长度x4字节
	 * @param src
	 *        加密后的缓冲x
	 */
	public static byte[] decryptMode(byte[] key,byte[] src){
		try{
			// 生成密钥
			SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
			// 解密
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(src);
		}catch(java.security.NoSuchAlgorithmException e1){
			e1.printStackTrace();
		}catch(javax.crypto.NoSuchPaddingException e2){
			e2.printStackTrace();
		}catch(java.lang.Exception e3){
			e3.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 转换成十六进制字符串
	 */
	public static String byte2hex(byte[] byteArr){
		String hexStr = "";
		String tmpStr = "";
		for(int n = 0;n < byteArr.length;n++){
			tmpStr = (java.lang.Integer.toHexString(byteArr[n] & 0XFF));
			if(tmpStr.length() == 1)
				hexStr = hexStr + "0" + tmpStr;
			else
				hexStr = hexStr + tmpStr;
			if(n < byteArr.length - 1)
				hexStr = hexStr + ":";
		}
		return hexStr.toUpperCase();
	}
	
	public static void main(String[] args){
		// 添加新安全算x如果用JCE就要把它添加进去
		// websphere下可能没有SunJCE
		
		// Security.addProvider(new com.sun.crypto.provider.SunJCE());
		final byte[] keyBytes = {0x11,0x22,0x4F,0x58,(byte)0x88,0x10,0x40,0x38,0x28,0x25,0x79,0x51,(byte)0xCB,
				(byte)0xDD,0x55,0x66,0x77,0x29,0x74,(byte)0x98,0x30,0x40,0x36,(byte)0xE2};
		// 24字节的密x
		String szSrc = "This is a 3DES test. 测试";
		System.out.println("加密前的字符x" + szSrc);
		byte[] encoded = encryptMode(keyBytes, szSrc.getBytes());
		System.out.println("加密后的字符x" + new String(encoded));
		byte[] srcBytes = decryptMode(keyBytes, encoded);
		System.out.println("解密后的字符x" + (new String(srcBytes)));
	}
	
}
