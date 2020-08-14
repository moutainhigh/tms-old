/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * <b> �ڴ˴���Ҫ��������Ĺ��� </b>
 * <p>
 * �ڴ˴���Ӵ����������Ϣ
 * </p>
 * ��������:2012-07-28 17:35:46
 * 
 * @author Administrator
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class LineNodeVO extends SuperVO {
	private String pk_corp;
	private UFDouble d_timeline;
	private UFDouble distance;
	private String pk_line_node;
	private String memo;
	private String pk_address;
	private String pk_area;
	private UFBoolean locked_flag;
	private Integer dr;
	private UFDouble u_timeline;
	private Integer serialno;
	private UFDateTime ts;
	private UFDouble s_timeline;
	private String pk_trans_line;
	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private UFDouble def11;
	private UFDouble def12;

	//yaojiie 2015 12 27 添加运力信息字段
	private String line_pk_driver;
	private String line_carno;
	private String line_pk_trans_type;
	private String line_pk_carrier;
	
	public String getLine_pk_driver() {
		return line_pk_driver;
	}

	public void setLine_pk_driver(String line_pk_driver) {
		this.line_pk_driver = line_pk_driver;
	}

	public String getLine_carno() {
		return line_carno;
	}

	public void setLine_carno(String line_carno) {
		this.line_carno = line_carno;
	}

	public String getLine_pk_trans_type() {
		return line_pk_trans_type;
	}

	public void setLine_pk_trans_type(String line_pk_trans_type) {
		this.line_pk_trans_type = line_pk_trans_type;
	}

	public String getLine_pk_carrier() {
		return line_pk_carrier;
	}

	public void setLine_pk_carrier(String line_pk_carrier) {
		this.line_pk_carrier = line_pk_carrier;
	}
	
	public String getPk_area() {
		return pk_area;
	}

	public void setPk_area(String pk_area) {
		this.pk_area = pk_area;
	}

	public UFDouble getDef11() {
		return def11;
	}

	public void setDef11(UFDouble def11) {
		this.def11 = def11;
	}

	public UFDouble getDef12() {
		return def12;
	}

	public void setDef12(UFDouble def12) {
		this.def12 = def12;
	}

	public String getDef10() {
		return def10;
	}

	public void setDef10(String def10) {
		this.def10 = def10;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef9() {
		return def9;
	}

	public void setDef9(String def9) {
		this.def9 = def9;
	}

	public String getDef5() {
		return def5;
	}

	public void setDef5(String def5) {
		this.def5 = def5;
	}

	public String getDef6() {
		return def6;
	}

	public void setDef6(String def6) {
		this.def6 = def6;
	}

	public String getDef7() {
		return def7;
	}

	public void setDef7(String def7) {
		this.def7 = def7;
	}

	public String getDef8() {
		return def8;
	}

	public void setDef8(String def8) {
		this.def8 = def8;
	}

	public static final String PK_CORP = "pk_corp";
	public static final String D_TIMELINE = "d_timeline";
	public static final String DISTANCE = "distance";
	public static final String PK_LINE_NODE = "pk_line_node";
	public static final String MEMO = "memo";
	public static final String PK_ADDRESS = "pk_address";
	public static final String LOCKED_FLAG = "locked_flag";
	public static final String U_TIMELINE = "u_timeline";
	public static final String SERIALNO = "serialno";
	public static final String S_TIMELINE = "s_timeline";
	public static final String PK_TRANS_LINE = "pk_trans_line";

	/**
	 * ����pk_corp��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return String
	 */
	public String getPk_corp() {
		return pk_corp;
	}

	/**
	 * ����pk_corp��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newPk_corp
	 *            String
	 */
	public void setPk_corp(String newPk_corp) {
		this.pk_corp = newPk_corp;
	}

	/**
	 * ����distance��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return UFDouble
	 */
	public UFDouble getDistance() {
		return distance;
	}

	/**
	 * ����distance��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newDistance
	 *            UFDouble
	 */
	public void setDistance(UFDouble newDistance) {
		this.distance = newDistance;
	}

	/**
	 * ����pk_line_node��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return String
	 */
	public String getPk_line_node() {
		return pk_line_node;
	}

	/**
	 * ����pk_line_node��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newPk_line_node
	 *            String
	 */
	public void setPk_line_node(String newPk_line_node) {
		this.pk_line_node = newPk_line_node;
	}

	/**
	 * ����memo��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return String
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * ����memo��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newMemo
	 *            String
	 */
	public void setMemo(String newMemo) {
		this.memo = newMemo;
	}

	/**
	 * ����pk_address��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return String
	 */
	public String getPk_address() {
		return pk_address;
	}

	/**
	 * ����pk_address��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newPk_address
	 *            String
	 */
	public void setPk_address(String newPk_address) {
		this.pk_address = newPk_address;
	}

	/**
	 * ����locked_flag��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return UFBoolean
	 */
	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	/**
	 * ����locked_flag��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newLocked_flag
	 *            UFBoolean
	 */
	public void setLocked_flag(UFBoolean newLocked_flag) {
		this.locked_flag = newLocked_flag;
	}

	/**
	 * ����dr��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return Integer
	 */
	public Integer getDr() {
		return dr;
	}

	/**
	 * ����dr��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newDr
	 *            Integer
	 */
	public void setDr(Integer newDr) {
		this.dr = newDr;
	}

	/**
	 * ����serialno��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return Integer
	 */
	public Integer getSerialno() {
		return serialno;
	}

	/**
	 * ����serialno��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newSerialno
	 *            Integer
	 */
	public void setSerialno(Integer newSerialno) {
		this.serialno = newSerialno;
	}

	/**
	 * ����ts��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return UFDateTime
	 */
	public UFDateTime getTs() {
		return ts;
	}

	/**
	 * ����ts��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newTs
	 *            UFDateTime
	 */
	public void setTs(UFDateTime newTs) {
		this.ts = newTs;
	}

	public UFDouble getD_timeline() {
		return d_timeline;
	}

	public void setD_timeline(UFDouble d_timeline) {
		this.d_timeline = d_timeline;
	}

	public UFDouble getU_timeline() {
		return u_timeline;
	}

	public void setU_timeline(UFDouble u_timeline) {
		this.u_timeline = u_timeline;
	}

	public UFDouble getS_timeline() {
		return s_timeline;
	}

	public void setS_timeline(UFDouble s_timeline) {
		this.s_timeline = s_timeline;
	}

	/**
	 * ����pk_trans_line��Getter����. ��������:2012-07-28 17:35:46
	 * 
	 * @return String
	 */
	public String getPk_trans_line() {
		return pk_trans_line;
	}

	/**
	 * ����pk_trans_line��Setter����. ��������:2012-07-28 17:35:46
	 * 
	 * @param newPk_trans_line
	 *            String
	 */
	public void setPk_trans_line(String newPk_trans_line) {
		this.pk_trans_line = newPk_trans_line;
	}

	/**
	 * <p>
	 * ȡ�ø�VO�����ֶ�.
	 * <p>
	 * ��������:2012-07-28 17:35:46
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getParentPKFieldName() {
		return "pk_trans_line";
	}

	/**
	 * <p>
	 * ȡ�ñ�����.
	 * <p>
	 * ��������:2012-07-28 17:35:46
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getPKFieldName() {
		return "pk_line_node";
	}

	/**
	 * <p>
	 * ���ر����.
	 * <p>
	 * ��������:2012-07-28 17:35:46
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "ts_line_node";
	}

	/**
	 * ����Ĭ�Ϸ�ʽ����������.
	 * 
	 * ��������:2012-07-28 17:35:46
	 */
	public LineNodeVO() {
		super();
	}
}