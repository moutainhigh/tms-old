package org.nw.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.SecurityUtils;
import org.nw.vo.pub.SuperVO;

/**
 * 该类提供了一些系统级别的静态方法，没有区分是什么样的 方法，其实该类的方法应该逐步放到各种类别的Utils类中
 * 
 * @author xuqc
 * @date 2012-8-1 下午04:51:32
 */
public class NWUtils {

	/**
	 * Email校验
	 * 
	 * @param mail
	 * @return
	 */
	public static boolean validateEmail(String mail) {
		String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(mail);
		return m.find();
	}

	/**
	 * 返回随机的密码
	 * 
	 * @param range
	 *            密码的位数
	 * @return
	 */
	public static String getRandomPass(int range) {
		StringBuffer buffer = new StringBuffer("_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		for(int i = 0; i < range; i++) {
			sb.append(buffer.charAt(r.nextInt(buffer.length())));
		}
		return sb.toString();
	}

	public static String getRandomSuffix(int range) {
		StringBuffer buffer = new StringBuffer("0123456789");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		for(int i = 0; i < range; i++) {
			sb.append(buffer.charAt(r.nextInt(buffer.length())));
		}
		return sb.toString();
	}

	/**
	 * 生成文件名
	 * 
	 * @param prefix
	 *            前缀
	 * @return
	 */
	public static String generateFileName(String prefix) {
		StringBuffer fileName = new StringBuffer();
		if(StringUtils.isNotBlank(prefix)) {
			fileName.append(prefix);
		}
		String dateStr = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		fileName.append(dateStr);
		// 加入随机的4位数
		fileName.append(getRandomSuffix(4));
		return fileName.toString();
	}

	/**
	 * 返回经过截取小数点后的数值，小数点位数从系统变量中读取
	 * 
	 * @param value
	 * @return
	 */
	public static double getRoundValue(double value) {
		int precision = ParameterHelper.getPrecision();
		return new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 返回经过截取小数点后的数值
	 * 
	 * @param value
	 * @param precision
	 *            小数点位数
	 * @return
	 */
	public static double getRoundValue(double value, int precision) {
		return new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 将strAry使用s连接起来
	 * 
	 * @param strAry
	 * @param s
	 * @return
	 */
	public static String join(String[] strAry, String s) {
		if(strAry == null || strAry.length == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		int len = strAry.length;
		for(int i = 0; i < len; i++) {
			if(StringUtils.isBlank(strAry[i])) {
				continue;
			}
			buf.append(strAry[i]);
			if(i != len - 1) {
				buf.append(",");
			}
		}
		return buf.toString();
	}
	
	public static String join(List<String> strAry, String s) {
		if(strAry == null || strAry.size() == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		int len = strAry.size();
		for(int i = 0; i < len; i++) {
			if(StringUtils.isBlank(strAry.get(i))) {
				continue;
			}
			buf.append(strAry.get(i));
			if(i != len - 1) {
				buf.append(",");
			}
		}
		return buf.toString();
	}

	/**
	 * 这里提供个方法，将数组转换成in查询操作，如： ('123','456','789')
	 * 
	 * @param strAry
	 * @return
	 */
	public static String buildConditionString(String[] strAry) {
		if(strAry == null || strAry.length == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < strAry.length; i++) {
			buf.append("'");
			buf.append(strAry[i]);
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}
	
	public static String buildConditionString(List<String> strAry) {
		if(strAry == null || strAry.size() == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < strAry.size(); i++) {
			buf.append("'");
			buf.append(strAry.get(i));
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}
	
	public static String buildConditionString(Set<String> strAry) {
		if(strAry == null || strAry.size() == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(String str : strAry){
			buf.append("'");
			buf.append(str);
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}

	/**
	 * 文件下载的文件名设置，解决不同浏览器，不同系统的乱码问题
	 * 
	 * @param fileName
	 * @param userAgent
	 * @return
	 * @throws UnsupportedEncodingException
	 * @author xuqc
	 * @date 2012-11-20
	 * 
	 */
	public static String getDownloadFileName(String fileName, String userAgent) throws UnsupportedEncodingException {
		String newFileName = fileName;
		userAgent = userAgent.toLowerCase();
		if(userAgent.indexOf("chrome") != -1) {
			// XXX
			// 这里注意chrome浏览器的user-agent包括safari字符，所以这个判断需要放在前面，why会有safari字符？
			newFileName = MimeUtility.encodeText(fileName, "UTF8", "B");
		} else if(userAgent.indexOf("safari") != -1) {
			// safari浏览器
			newFileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
		} else if(userAgent.indexOf("firefox") != -1) {
			// firefox浏览器
			newFileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
		} else {
			newFileName = URLEncoder.encode(fileName, "utf-8");
		}
		return newFileName;
	}

	public static String escape(String arg) {
		if(StringUtils.isNotBlank(arg)) {
			arg = arg.replace("<", "&lt;");
			arg = arg.replace(">", "&gt;");
			// arg = arg.replace("&", "&amp;");
			// arg = arg.replace("\"", "&quot;");// 不替换，页面提交的是json字符串已经包含了“
		}
		return arg;
	}

	/**
	 * 将arg中的特殊字符转成相应的转义字符
	 * 
	 * @param arg
	 */
	public static String unescape(String arg) {
		if(StringUtils.isNotBlank(arg)) {
			arg = arg.replace("&lt;", "<");
			arg = arg.replace("&gt;", ">");
			// arg = arg.replace("&amp;", "&");
			// arg = arg.replace("\"", "&quot;");// 不替换，页面提交的是json字符串已经包含了“
		}
		return arg;
	}

	/**
	 * superVO转换成map
	 * 
	 * @param superVOs
	 * @return
	 */
	public static List<Map<String, Object>> convertVO2Map(List<? extends SuperVO> superVOs) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOs.size());
		for(SuperVO vo : superVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				Object obj = vo.getAttributeValue(key);
				if(obj != null) {
					map.put(key, SecurityUtils.escape(obj.toString()));
				} else {
					map.put(key, obj);
				}
			}
			mapList.add(map);
		}
		return mapList;
	}

	/**
	 * 将数组的顺序颠倒
	 * 
	 * @param a
	 */
	public static void reverse(Object a[]) {
		int len = a.length;
		for(int i = 0; i < len / 2; i++) {
			Object tmp = a[i];
			a[i] = a[len - 1 - i];
			a[len - 1 - i] = tmp;
		}
	}

	public static void main(String[] args) {
		List<Integer> ary = new ArrayList<Integer>();
		ary.add(1);
		ary.add(2);

		Integer[] it = ary.toArray(new Integer[0]);
		for(int i = 0; i < 120; i++)
			System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
	}
}
