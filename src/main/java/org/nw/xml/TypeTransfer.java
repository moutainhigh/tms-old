package org.nw.xml;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 * 提供byte,short,int,float,double,String等类型的相互转换的static方法
 * 
 * @author watson chen
 * 
 */
public class TypeTransfer {

	static String Encoding = "UTF-8";

	public TypeTransfer() {
	}

	static Logger log = Logger.getLogger(TypeTransfer.class);

	/**
	 * 存储16进制字符的数组
	 */
	private static String HexCode[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 将16进制的String转换为10进制String
	 * 
	 * @param s
	 * @return String
	 */
	public static String hexString2String(String s) {
		byte[] b = TypeTransfer.hexString2byteArray(s);
		String result = "";
		try {
			result = new String(b, Encoding);
		} catch(Exception e) {
			log.error(e);
		}
		return result;
	}

	/**
	 * 将字符串按指定编码后输出
	 * 
	 * @param s
	 * @return String 编码后的字符串
	 */
	public static String String2EncodingString(String s) {
		byte[] b = null;
		try {
			b = s.getBytes(Encoding);
		} catch(UnsupportedEncodingException e) {
			log.error(e);
		}
		return encodeString(b);
	}

	/**
	 * 将一个String转换为16进制表示的String
	 * 
	 * @param s
	 * @return String
	 */
	public static String String2HexString(String s) {
		if(s == null) {
			return "";
		}
		byte[] b = null;
		try {
			b = s.getBytes(Encoding);
		} catch(Exception e) {
			log.error(e);
		}
		return TypeTransfer.byteArray2HexString(b).toUpperCase();
	}

	/**
	 * 将字节数组按<code>UtilConst.Encoding</code>编码后输出为String
	 * 
	 * @see com.shonetown.common.UtilConst
	 * @param s
	 * @return String 编码后的String
	 */
	public static String encodeString(byte[] s) {
		String result = "";
		try {
			result = new String(s, Encoding);
		} catch(Exception e) {
			log.error(e);
		}
		return result;
	}

	/**
	 * 将一个字符串转换为整数形式,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return int
	 */
	public static int String2int(String sValue) {
		int result;
		try {
			result = Integer.parseInt(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将字符串转换为byte,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return byte
	 */
	public static byte String2byte(String sValue) {
		byte result;
		try {
			result = Byte.parseByte(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将字符串转换为short,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return short
	 */
	public static short String2short(String sValue) {
		short result;
		try {
			result = Short.parseShort(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将字符串转换为long,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return long
	 */
	public static long String2long(String sValue) {
		long result;
		try {
			result = Long.parseLong(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将String转换为float,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return float
	 */
	public static float String2float(String sValue) {
		sValue = sValue.replaceAll(",", "");
		float result;
		try {
			result = Float.parseFloat(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将String转换为double,若抛出异常则返回0
	 * 
	 * @param sValue
	 * @return double
	 */
	public static double String2double(String sValue) {
		sValue = sValue.replaceAll(",", "");
		double result;
		try {
			result = Double.parseDouble(sValue);
		} catch(NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	/**
	 * 将int转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String int2String(int iValue) {
		return Integer.toString(iValue);
	}

	/**
	 * 将short转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String short2String(short iValue) {
		return Short.toString(iValue);
	}

	/**
	 * 将byte转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String byte2String(byte iValue) {
		return Byte.toString(iValue);
	}

	/**
	 * 将float转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String float2String(float iValue) {
		return Float.toString(iValue);
	}

	/**
	 * 将long转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String long2String(long iValue) {
		return Long.toString(iValue);
	}

	/**
	 * 将double转换为String
	 * 
	 * @param iValue
	 * @return String
	 */
	public static String double2String(double iValue) {
		return Double.toString(iValue);
	}

	/**
	 * 返回一个字节的16进制String表达式
	 * 
	 * @param b
	 * @return String
	 */
	public static String byte2HexString(byte b) {
		int n = b;
		if(n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return HexCode[d1] + HexCode[d2];
	}

	/**
	 * 将字节数组转换为String
	 * 
	 * @param b
	 * @return String
	 */
	public static String byteArray2String(byte b[]) {
		String result = "";
		for(int i = 0; i < b.length; i++)
			result = result + byte2String(b[i]);
		return result;
	}

	/**
	 * 返回字节数组的16进制String表达式
	 * 
	 * @param b
	 * @return String
	 */
	public static String byteArray2HexString(byte b[]) {
		String result = "";
		for(int i = 0; i < b.length; i++)
			result = result + byte2HexString(b[i]);
		return result;
	}

	/**
	 * 将String转换为字节数组
	 * 
	 * @param s
	 * @return byte[]
	 */
	public static byte[] String2byteArray(String s) {
		byte b[] = new byte[s.length()];
		for(int i = 0; i < s.length(); i++) {
			String ss = s.substring(i, i + 1);
			b[i] = String2byte(ss);
		}
		return b;
	}

	/**
	 * 将一个16进制的String转换为字节数组
	 * 
	 * @param s
	 * @return byte[]
	 */
	public static byte[] hexString2byteArray(String s) {
		byte b[] = new byte[s.length() / 2];
		for(int i = 0; i < s.length() / 2; i++) {
			String ss = s.substring(2 * i, 2 * i + 2);
			b[i] = (byte) Integer.parseInt(ss, 16);
		}
		return b;
	}

	/**
	 * 将字节数组的第offset+1开始的元素转换为int型
	 * 
	 * @param b
	 * @param offset
	 * @return int
	 */
	public static int byte2int(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16
				| (b[offset] & 0xff) << 24;
	}

	/**
	 * 将字节数组转换为int型
	 * 
	 * @param b
	 * @return int
	 */
	public static int byte2int(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
	}

	/**
	 * 将字节数组转换为long型
	 * 
	 * @param b
	 * @return long
	 */
	public static long byte2long(byte b[]) {
		return (long) b[7] & (long) 255 | ((long) b[6] & (long) 255) << 8 | ((long) b[5] & (long) 255) << 16
				| ((long) b[4] & (long) 255) << 24 | ((long) b[3] & (long) 255) << 32
				| ((long) b[2] & (long) 255) << 40 | ((long) b[1] & (long) 255) << 48 | (long) b[0] << 56;
	}

	/**
	 * 将字节数组从第offset+1位开始转换为long型
	 * 
	 * @param b
	 * @param offset
	 * @return long
	 */
	public static long byte2long(byte b[], int offset) {
		return (long) b[offset + 7] & (long) 255 | ((long) b[offset + 6] & (long) 255) << 8
				| ((long) b[offset + 5] & (long) 255) << 16 | ((long) b[offset + 4] & (long) 255) << 24
				| ((long) b[offset + 3] & (long) 255) << 32 | ((long) b[offset + 2] & (long) 255) << 40
				| ((long) b[offset + 1] & (long) 255) << 48 | (long) b[offset] << 56;
	}

	/**
	 * 将int转换为字节数组
	 * 
	 * @param n
	 * @return byte[]
	 */
	public static byte[] int2byte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

	/**
	 * 将int转换为字节数组存储在buf中,从第offset+1位开始.<br/>
	 * 注：数组的下标是从0位开始,所以实际上是从buf[offset]开始存储
	 * 
	 * @param n
	 * @param buf
	 * @param offset
	 */
	public static void int2byte(int n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 24);
		buf[offset + 1] = (byte) (n >> 16);
		buf[offset + 2] = (byte) (n >> 8);
		buf[offset + 3] = (byte) n;
	}

	/**
	 * 将short转换为字节数组
	 * 
	 * @param n
	 * @return byte[]
	 */
	public static byte[] short2byte(short n) {
		byte b[] = new byte[2];
		b[0] = (byte) (n >> 8);
		b[1] = (byte) n;
		return b;
	}

	/**
	 * 将short转换为字节数组存储在buf中,从第offset+1为开始.<br/>
	 * 注：数组的下标是从0位开始,所以实际上是从buf[offset]开始存储
	 * 
	 * @param n
	 * @param buf
	 * @param offset
	 */
	public static void short2byte(short n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 8);
		buf[offset + 1] = (byte) n;
	}

	/**
	 * 将long型转化为字节数组
	 * 
	 * @param n
	 * @return byte[]
	 */
	public static byte[] long2byte(long n) {
		byte b[] = new byte[8];
		b[0] = (byte) (int) (n >> 56);
		b[1] = (byte) (int) (n >> 48);
		b[2] = (byte) (int) (n >> 40);
		b[3] = (byte) (int) (n >> 32);
		b[4] = (byte) (int) (n >> 24);
		b[5] = (byte) (int) (n >> 16);
		b[6] = (byte) (int) (n >> 8);
		b[7] = (byte) (int) n;
		return b;
	}

	/**
	 * 将long型转换为字节数组并存储在buf中,从第offset+1为开始.<br/>
	 * 注：数组的下标是从0位开始,所以实际上是从buf[offset]开始存储
	 * 
	 * @param n
	 * @param buf
	 * @param offset
	 */
	public static void long2byte(long n, byte buf[], int offset) {
		buf[offset] = (byte) (int) (n >> 56);
		buf[offset + 1] = (byte) (int) (n >> 48);
		buf[offset + 2] = (byte) (int) (n >> 40);
		buf[offset + 3] = (byte) (int) (n >> 32);
		buf[offset + 4] = (byte) (int) (n >> 24);
		buf[offset + 5] = (byte) (int) (n >> 16);
		buf[offset + 6] = (byte) (int) (n >> 8);
		buf[offset + 7] = (byte) (int) n;
	}

}

/*
 * 
 * public static byte[] getBytesRingORPic(String ringOrMap){ int length =
 * ringOrMap.length()/2; byte[] bMsg = new byte[length]; for(int
 * i=0;i<length;i++){ byte x =
 * Byte.parseByte(ringOrMap.substring(2*i,2*i+1),16); byte y =
 * Byte.parseByte(ringOrMap.substring(2*i+1,2*i+2),16); int j = x*16+y;
 * bMsg[i]=(byte)j; } return bMsg; }
 * 
 * public static boolean checkMobile(String sMobile){ String sF6 = "",sB7=
 * "",sF2=""; if(sMobile == null) return false; if(sMobile.length()!= 11) return
 * false; sF6 = sMobile.substring(0,7); sF2 = sMobile.substring(0,2); sB7 =
 * sMobile.substring(7); try { int iT = Integer.valueOf(sF6).intValue() ; iT =
 * Integer.valueOf(sB7).intValue(); if(sF2.equals("13")) return true; else
 * return false; } catch (Exception ex) { return false; } }
 */
