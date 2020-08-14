package org.nw.vo.pub.util;

/**
 * 
 * @author xuqc
 * @date 2013-7-27 下午02:19:58
 */
public class StringOperator {
	private byte[] info = new byte[0];

	/**
	 * StringOperator 构造子注解.
	 */
	public StringOperator() {
		super();
	}

	/**
	 * StringOperator 构造子注解.
	 */
	public StringOperator(byte[] strByte) {
		super();
		info = strByte;
	}

	/**
	 * StringOperator 构造子注解.
	 */
	public StringOperator(String str) {
		super();
		if(str != null)
			info = str.getBytes();
		else
			info = "null".getBytes();
	}

	/**
	 * StringOperator 构造子注解.
	 */
	public StringOperator(StringOperator stro) {
		super();
		info = (byte[]) stro.info.clone();
	}

	public StringOperator append(String str) {
		return append(new StringOperator(str));
	}

	public StringOperator append(StringOperator so) {
		byte newByte[] = new byte[info.length + so.info.length];
		System.arraycopy(info, 0, newByte, 0, info.length);
		System.arraycopy(so.info, 0, newByte, info.length, so.info.length);
		info = newByte;
		return this;
	}

	public StringOperator getStr(String strBegin, String strEnd) {
		return getStr(new StringOperator(strBegin), new StringOperator(strEnd));
	}

	/**
	 * 取得StringOperator中的 begin 到 end的内容
	 * 
	 * 
	 */
	public StringOperator getStr(StringOperator strBegin, StringOperator strEnd) {
		int index = indexOf(strBegin);
		int index1 = indexOf(strEnd);
		return substring(index + strBegin.info.length, index1);
	}

	public int indexOf(StringOperator so) {
		int scanLength = info.length - so.info.length;
		boolean found = false;
		for(int i = 0; i <= scanLength; i++) {
			found = true;
			for(int j = 0; j < so.info.length; j++) {
				if(info[i + j] != so.info[j]) {
					found = false;
					break;
				}
			}
			if(found)
				return i;
		}
		return -1;
	}

	public int indexOf(StringOperator so, int begin) {
		int scanLength = info.length - so.info.length;
		boolean found = false;
		for(int i = begin; i <= scanLength; i++) {
			found = true;
			for(int j = 0; j < so.info.length; j++) {
				if(info[i + j] != so.info[j]) {
					found = false;
					break;
				}
			}
			if(found)
				return i;
		}
		return -1;
	}

	/**
	 * 此处插入方法说明. 创建日期:(2001-1-12 9:23:52)
	 * 
	 * @param args
	 *            java.lang.String[]
	 */
	public static void main(String[] args) {
		System.out.println("this is StringOperator test program");
		String source = "This is test replace from %$beReplace$% to ALL ($beReplace$) End";
		System.out.println(">" + source);
		StringOperator so = new StringOperator(source);
		so.replaceAllString("$beReplace$", "--ALL--");
		System.out.println("to> " + so);
		source = "Test get and replace <bb>$begin$Get This Information$end$<ee>....";
		so = new StringOperator(source);
		StringOperator s1 = new StringOperator(so);
		System.out.println(">" + source);
		System.out.println(so.getStr("$begin$", "$end$"));
		System.out.println(so.replaceFromTo("$begin$", "$end$", "replace the get information"));
		System.out.println(s1.removeStr("$begin$", "$end$"));
		System.out.println(so.append(s1));
	}

	private void remove(int loc1, int loc2) {
		if(loc1 < -1 || loc2 < -1 || loc2 < loc1)
			return;
		int removeLength = loc2 - loc1;
		byte newByte[] = new byte[info.length - removeLength];
		System.arraycopy(info, 0, newByte, 0, loc1);
		System.arraycopy(info, loc2, newByte, loc1, info.length - loc2);
		info = newByte;
	}

	public StringOperator removeStr(String strBegin, String strEnd) {
		return removeStr(new StringOperator(strBegin), new StringOperator(strEnd));
	}

	public StringOperator removeStr(StringOperator strBegin, StringOperator strEnd) {
		int index = indexOf(strBegin);
		int index1 = indexOf(strEnd);
		remove(index, index1 + strEnd.info.length);
		return this;
	}

	public void replaceAllString(String beReplace, String newSo) {
		replaceAllString(new StringOperator(beReplace), new StringOperator(newSo));
	}

	public void replaceAllString(String beReplace, StringOperator newSo) {
		replaceAllString(new StringOperator(beReplace), newSo);
	}

	public void replaceAllString(StringOperator beReplace, String newSo) {
		replaceAllString(beReplace, new StringOperator(newSo));
	}

	public void replaceAllString(StringOperator beReplace, StringOperator newSo) {
		if(newSo == null)
			newSo = new StringOperator("null");
		java.util.Vector vctLocations = new java.util.Vector();
		int loc = 0;
		int indexLoc = indexOf(beReplace);
		while(indexLoc >= 0) {
			vctLocations.addElement(new Integer(indexLoc));
			loc = indexLoc + newSo.info.length + 1;
			indexLoc = indexOf(beReplace, loc);
		}
		int oneAppendLength = newSo.info.length - beReplace.info.length;
		int appendLength = vctLocations.size() * (oneAppendLength);
		byte newByte[] = new byte[info.length + appendLength];
		int lastLoc = 0;
		int fittedLoc = 0;
		for(int i = 0; i < vctLocations.size(); i++) {
			Integer locInt = (Integer) vctLocations.elementAt(i);
			int begin = lastLoc;
			int length = locInt.intValue() - begin;
			System.arraycopy(info, begin, newByte, fittedLoc, length);
			fittedLoc += length;
			System.arraycopy(newSo.info, 0, newByte, fittedLoc, newSo.info.length);
			fittedLoc += newSo.info.length;
			lastLoc = locInt.intValue() + beReplace.info.length;
		}
		if(fittedLoc < newByte.length) {
			System.arraycopy(info, lastLoc, newByte, fittedLoc, newByte.length - fittedLoc);
		}
		info = newByte;
	}

	public StringOperator replaceFromTo(String strBegin, String strEnd, String replaced) {
		return replaceFromTo(new StringOperator(strBegin), new StringOperator(strEnd), new StringOperator(replaced));
	}

	public StringOperator replaceFromTo(StringOperator soBegin, StringOperator soEnd, StringOperator replaced) {
		int b = indexOf(soBegin);
		int e = indexOf(soEnd) + soEnd.info.length;
		if(e < b)
			return this;
		int rLength = replaced.info.length;
		byte newByte[] = new byte[info.length + replaced.info.length - (e - b)];
		System.arraycopy(info, 0, newByte, 0, b);
		System.arraycopy(replaced.info, 0, newByte, b, rLength);
		int length = info.length - e;
		System.arraycopy(info, e, newByte, b + rLength, length);
		info = newByte;
		return this;
	}

	/**
	 * 此处插入方法说明. 创建日期:(2001-1-12 8:36:32)
	 * 
	 * @param loc1
	 *            int
	 * @param loc2
	 *            int
	 */
	public StringOperator substring(int index1, int index2) {
		if(index1 == -1 || index2 == -1 || index2 < index1)
			return null;
		byte newByte[] = new byte[index2 - index1];
		System.arraycopy(info, index1, newByte, 0, index2 - index1);
		return new StringOperator(newByte);
	}

	public String toString() {
		return new String(info);
	}
}
