package org.nw.exp;

/**
 * Excel的单元格类型，POI会自动识别内容为数字的单元格为数字类型，但是有时候我们需要的是字符型
 * 
 * @author xuqc
 * @date 2014-4-27 上午12:16:44
 */
public class CellTypeConst {

	public static final int STRING = 1;
	public static final int NUMERIC = 0;
	public static final int BOOLEAN = 4;
	public static final int BLANK = 3;
	public static final int FORMULA = 2;

	// 扩展的类型
	public static final int DATE = 10;
	public static final int DATETIME = 11;
}
