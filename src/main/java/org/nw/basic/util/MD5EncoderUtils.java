package org.nw.basic.util;

import java.security.MessageDigest;

/**
 * MD5编码器 2011-4-9
 * 
 * @author fangw
 */
public class MD5EncoderUtils{
	// 用来将字节转换成 16 进制表示的字xx
	public static char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	/**
	 * 加密算法,调用05MIS的加密算法
	 */
	public String encodePassword(String rawPass,Object salt){
		return MD5Encrypt(rawPass);
	}
	
	/**
	 * 密码验证算法
	 */
	public boolean isPasswordValid(String encPass,String rawPass,Object salt){
		String pass1 = "" + encPass;
		String pass2 = encodePassword(rawPass, salt);
		
		return pass1.equals(pass2);
	}
	
	/**
	 * 进行MD5加密
	 * 
	 * @param inStr
	 * @return
	 */
	public static String MD5Encrypt(String inStr){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(inStr.getBytes());
			return MD5EncoderUtils.bytetoString(digest);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static String bytetoString(byte[] digest){
		char str[] = new char[16 * 2]; // 每个字节xxx16
		int k = 0; // 表示转换结果中对应的字符位置
		for(int i = 0;i < 16;i++){
			byte byte0 = digest[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf]; // 取字节中xxx4 位的数字转换
		}
		
		return new String(str); // 换后的结果转换为字符xxx
	}
	
	public static void main(String[] args){
		// String aa ="7ccb0eea8a706c4c34a16891f84e7b";
		String aa1 = MD5Encrypt("aa");
		System.out.println(aa1.length());
		System.out.println(aa1);
		
		System.out.println(MD5Encrypt("0"));
		System.out.println(MD5Encrypt("1"));
		System.out.println(MD5Encrypt("2"));
	}
}