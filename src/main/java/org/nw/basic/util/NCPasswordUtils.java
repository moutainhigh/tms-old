package org.nw.basic.util;

/**
 * NC�������<br>
 * String ���� Ŀǰ����һ�����⣬�ַ���С�����ܵĳ��Ⱦ�С <br>
 * Ŀǰֻ�������--fangw
 * 
 * @version 1.0 02/10/99
 * @author wang shuang
 */
public class NCPasswordUtils {
	/**
	 * ����
	 */
	public static String encode(String s) {
		if(s == null)
			return null;
		String res = "";
		NCPasswordDes des = new NCPasswordDes(getKey());
		byte space = 0x20;
		byte[] sBytes = s.getBytes();
		int length = sBytes.length;
		int newLength = length + (8 - length % 8) % 8;
		byte[] newBytes = new byte[newLength];
		for(int i = 0; i < newLength; i++) {
			if(i <= length - 1) {
				newBytes[i] = sBytes[i];
			} else {
				newBytes[i] = space;
			}
		}
		for(int i = 0; i < (newLength / 8); i++) {
			byte[] theBytes = new byte[8];
			for(int j = 0; j <= 7; j++) {
				theBytes[j] = newBytes[8 * i + j];
			}
			long x = des.bytes2long(theBytes);
			byte[] result = new byte[8];
			des.long2bytes(des.encrypt(x), result);
			byte[] doubleResult = new byte[16];
			for(int j = 0; j < 8; j++) {
				doubleResult[2 * j] = (byte) (((((char) result[j]) & 0xF0) >> 4) + 'a');
				doubleResult[2 * j + 1] = (byte) ((((char) result[j]) & 0x0F) + 'a');
			}
			res = res + new String(doubleResult);
		}
		return res;
	}

	/**
	 * �̶��ļ���Key
	 * 
	 * @return long
	 */
	private static long getKey() {
		return 1231234234;
	}

	public static void main(String[] args) {
		System.out.println(NCPasswordUtils.encode("daobanzhewuchi"));
	}
}