package org.nw.jf.group;

/**
 * ������VO������Ĺ�ϵʹ��һ����ṹ���ʾ
 * 
 * @author xuqc
 * @date 2013-3-26 ����04:33:37
 */
public class GroupTreeVO {

	private String header;// ����
	private int level;// ����
	private int count;// ���ֵĴ���

	public final String getHeader() {
		return header;
	}

	public final void setHeader(String header) {
		this.header = header;
	}

	public final int getLevel() {
		return level;
	}

	public final void setLevel(int level) {
		this.level = level;
	}

	public final int getCount() {
		return count;
	}

	public final void setCount(int count) {
		this.count = count;
	}
}
