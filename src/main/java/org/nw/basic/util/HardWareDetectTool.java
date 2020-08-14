package org.nw.basic.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class HardWareDetectTool {
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

	/**
	 * 获取widnows网卡的mac地址.
	 * 
	 * @return mac地址
	 */
	public static String getWindowsMACAddress() {
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ipconfig /all");// windows下的命令，显示信息中包含有mac地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				index = line.toLowerCase().indexOf("physical address");// 寻找标示字符串[physical
																		// address]
				if (index >= 0) {// 找到了
					index = line.indexOf(":");// 寻找":"的位置
					if (index >= 0) {
						mac = line.substring(index + 1).trim();// 取出mac地址并去除2边空格
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
	 * 
	 * 获取CPU序列号
	 * 
	 * 
	 * 
	 * @return
	 * 
	 */

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
			System.out.println("操作系统名称：" + System.getProperty("os.name").toLowerCase());
			System.out.println("mac地址1：" + getMACAddress());
			System.out.println("mac地址2：" + getWindowsMACAddress());
			System.out.println("主板信息：" + getMotherboardSN());
			System.out.println("CPU信息：" + getCPUSerial());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
