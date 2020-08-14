package org.nw.basic.util;

import java.security.Key;  
import java.security.Security;  
import javax.crypto.Cipher;  

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;


import java.net.SocketException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;



/**
 * DES 加密解密算法  2016-01-11
 * 
 * @author songf
 */

/** 
 * DES加密和解密工具,可以对字符串进行加密和解密操作  。  
 */  
public class DESEncoderUtils {
	 /** 
     * 默认构造方法，使用默认密钥 
     */  
    public DESEncoderUtils() throws Exception {  
        this(strDefaultKey);  
    }  
    
    /** 
     * 指定密钥构造方法 
     * @param strKey  指定的密钥 
     * @throws Exception 
     */  
    public DESEncoderUtils(String strKey) throws Exception {  
        // Security.addProvider(new com.sun.crypto.provider.SunJCE());  
        Key key = getKey(strKey.getBytes());  
        encryptCipher = Cipher.getInstance("DES");  
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);  
        decryptCipher = Cipher.getInstance("DES");  
        decryptCipher.init(Cipher.DECRYPT_MODE, key);  
    }  
    /** 字符串默认键值 */  
    private static String strDefaultKey = "shanghairuzhixinxikejiyouxiangongsi";  
    /** 加密工具 */  
    private Cipher encryptCipher = null;  
    /** 解密工具 */  
    private Cipher decryptCipher = null;  
    /** 
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[] 
     * hexStr2ByteArr(String strIn) 互为可逆的转换过程 
     * @param arrB  需要转换的byte数组 
     * @return 转换后的字符串 
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出 
     */  
    public static String byteArr2HexStr(byte[] arrB) throws Exception {  
        int iLen = arrB.length;  
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍  
        StringBuffer sb = new StringBuffer(iLen * 2);  
        for (int i = 0; i < iLen; i++) {  
            int intTmp = arrB[i];  
            // 把负数转换为正数  
            while (intTmp < 0) {  
                intTmp = intTmp + 256;  
            }  
            // 小于0F的数需要在前面补0  
            if (intTmp < 16) {  
                sb.append("0");  
            }  
            sb.append(Integer.toString(intTmp, 16));  
        }  
        return sb.toString();  
    }  
    /** 
     * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB) 
     * 互为可逆的转换过程 
     * @param strIn 需要转换的字符串 
     * @return 转换后的byte数组 
     */  
    public static byte[] hexStr2ByteArr(String strIn) throws Exception {  
        byte[] arrB = strIn.getBytes();  
        int iLen = arrB.length;  
        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2  
        byte[] arrOut = new byte[iLen / 2];  
        for (int i = 0; i < iLen; i = i + 2) {  
            String strTmp = new String(arrB, i, 2);  
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);  
        }  
        return arrOut;  
    }  
    /** 
     * 加密字节数组 
     * @param arrB  需加密的字节数组 
     * @return 加密后的字节数组 
     */  
    public byte[] encrypt(byte[] arrB) throws Exception {  
        return encryptCipher.doFinal(arrB);  
    }  
    /** 
     * 加密字符串 
     * @param strIn  需加密的字符串 
     * @return 加密后的字符串 
     */  
    public String encrypt(String strIn) throws Exception {  
        return byteArr2HexStr(encrypt(strIn.getBytes()));  
    }  
    /** 
     * 解密字节数组 
     * @param arrB  需解密的字节数组 
     * @return 解密后的字节数组 
     */  
    public byte[] decrypt(byte[] arrB) throws Exception {  
        return decryptCipher.doFinal(arrB);  
    }  
    /** 
     * 解密字符串 
     * @param strIn  需解密的字符串 
     * @return 解密后的字符串 
     */  
    public String decrypt(String strIn) throws Exception {  
        return new String(decrypt(hexStr2ByteArr(strIn)));  
    }  
    /** 
     * 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位 
     * @param arrBTmp  构成该字符串的字节数组 
     * @return 生成的密钥 
     */  
    private Key getKey(byte[] arrBTmp) throws Exception {  
        // 创建一个空的8位字节数组（默认值为0）  
        byte[] arrB = new byte[8];  
        // 将原始字节数组转换为8位  
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {  
            arrB[i] = arrBTmp[i];  
        }  
        // 生成密钥  
        Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");  
        return key;  
    } 
    
    /**
	 * 获取widnows网卡的mac地址.
	 * @return mac地址
	 */
	public static String getWindowsMACAddress() {
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ipconfig /all");// windows下的命令，显示信息中包含有mac地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				index = line.toLowerCase().indexOf("physical address");// 寻找标示字符串[physical address]
				if (index >= 0) {// 找到了
					index = line.indexOf(":");// 寻找":"的位置
					if (index>=0) {
						mac = line.substring(index + 1).trim();//  取出mac地址并去除2边空格
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}
	
	/**
	 * windows 7 专用 获取MAC地址
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getMACAddress() throws Exception {
		
		// 获取本地IP对象
		InetAddress ia = InetAddress.getLocalHost();
		// 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

		// 下面代码是把mac地址拼装成String
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			// mac[i] & 0xFF 是为了把byte转化为正整数
			String s = Integer.toHexString(mac[i] & 0xFF);
			sb.append(s.length() == 1 ? 0 + s : s);
		}

		// 把字符串所有小写字母改为大写成为正规的mac地址并返回
		return sb.toString().toUpperCase();
	}
	

	  public static String getMotherboardSN() {

	        String result = "";

	        try {

	            File file = File.createTempFile("realhowto", ".vbs");

	            file.deleteOnExit();

	            FileWriter fw = new java.io.FileWriter(file);

	 

	            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"

	                    + "Set colItems = objWMIService.ExecQuery _ \n"

	                    + "   (\"Select * from Win32_BaseBoard\") \n"

	                    + "For Each objItem in colItems \n"

	                    + "    Wscript.Echo objItem.SerialNumber \n"

	                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

	 

	            fw.write(vbs);

	            fw.close();

	            Process p = Runtime.getRuntime().exec(

	                    "cscript //NoLogo " + file.getPath());

	            BufferedReader input = new BufferedReader(new InputStreamReader(

	                    p.getInputStream()));

	            String line;

	            while ((line = input.readLine()) != null) {

	                result += line;

	            }

	            input.close();

	        } catch (Exception e) {

	            e.printStackTrace();

	        }

	        return result.trim();

	    }
	  
	  public static String getCPUSerial() {

			String result = "";

			try {

				File file = File.createTempFile("tmp", ".vbs");

				file.deleteOnExit();

				FileWriter fw = new java.io.FileWriter(file);

				String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"

						+ "Set colItems = objWMIService.ExecQuery _ \n"

						+ "   (\"Select * from Win32_Processor\") \n"

						+ "For Each objItem in colItems \n"

						+ "    Wscript.Echo objItem.ProcessorId \n"

						+ "    exit for  ' do the first cpu only! \n" + "Next \n";

				// + " exit for \r\n" + "Next";

				fw.write(vbs);

				fw.close();

				Process p = Runtime.getRuntime().exec(

						"cscript //NoLogo " + file.getPath());

				BufferedReader input = new BufferedReader(new InputStreamReader(

						p.getInputStream()));

				String line;

				while ((line = input.readLine()) != null) {

					result += line;

				}

				input.close();

				file.delete();

			} catch (Exception e) {

				e.fillInStackTrace();

			}

			if (result.trim().length() < 1 || result == null) {

				result = "无CPU_ID被读取";

			}

			return result.trim();

		}
    public static void main(String[] args) {  
        try {  
        	System.out.println(System.getProperty("os.name").toLowerCase());
        	
        
            String test1 = "987654321";  
               //   DESEncoderUtils des1 = new DESEncoderUtils();// 使用默认密钥  
            
        	// 开始执行时间
    		Calendar start = Calendar.getInstance();
        //	System.out.println(getMACAddress());
     //   System.out.println(getCPUSerial());
        	System.out.println(getMotherboardSN());
    	    // test1 = "shanghai" + getMACAddress() +"runzhi"+ getMotherboardSN()+"keji"+"*"+"2016-01-12"+"&"+"2016-02-12"+"6"+"5";
             
    	  //   System.out.println(test1);
        	long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
        	
        	 System.out.println("共执行：" + interval / 1000 + "秒，成功");

      /*      System.out.println("加密前的字符：" + test1);  
            System.out.println("加密后的字符：" + des1.encrypt(test1));  
            System.out.println("解密后的字符：" + des1.decrypt(des1.encrypt(test1)));  
            
            String test2 = "123456789";  
            DESEncoderUtils des2 = new DESEncoderUtils("leeme32nz");// 自定义密钥  
            System.out.println("加密前的字符：" + test2);  
            System.out.println("加密后的字符：" + des2.encrypt(test2));  
            System.out.println("解密后的字符：" + des2.decrypt(des2.encrypt(test2)));  
           */ 
        //    System.out.println("主板：" + getMotherboardSN());    
         //   Enumeration<NetworkInterface> nets = NetworkInterface
        //            .getNetworkInterfaces();
        //    for (NetworkInterface netint : Collections.list(nets))
         //       displayInterfaceInformation(netint);
            
         //   System.out.println("CPU  SN:" + getCPUSerial());

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}
