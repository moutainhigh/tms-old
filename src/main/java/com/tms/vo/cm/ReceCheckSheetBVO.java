/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * <b> �ڴ˴���Ҫ�������Ĺ��� </b>
 * <p>
 * �ڴ˴���Ӵ����������Ϣ
 * </p>
 * ��������:2013-03-23 18:52:53
 * 
 * @author Administrator
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class ReceCheckSheetBVO extends SuperVO {
	private String pk_receive_detail;
	private UFDateTime ts;
	private String pk_rece_check_sheet;
	private String pk_rece_check_sheet_b;
	private Integer dr;
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

	public static final String PK_RECEIVE_DETAIL = "pk_receive_detail";
	public static final String PK_RECE_CHECK_SHEET = "pk_rece_check_sheet";
	public static final String PK_RECE_CHECK_SHEET_B = "pk_rece_check_sheet_b";

	public String getPk_receive_detail() {
		return pk_receive_detail;
	}

	public void setPk_receive_detail(String pk_receive_detail) {
		this.pk_receive_detail = pk_receive_detail;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getPk_rece_check_sheet() {
		return pk_rece_check_sheet;
	}

	public void setPk_rece_check_sheet(String pk_rece_check_sheet) {
		this.pk_rece_check_sheet = pk_rece_check_sheet;
	}

	public String getPk_rece_check_sheet_b() {
		return pk_rece_check_sheet_b;
	}

	public void setPk_rece_check_sheet_b(String pk_rece_check_sheet_b) {
		this.pk_rece_check_sheet_b = pk_rece_check_sheet_b;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	/**
	 * <p>
	 * ȡ�ø�VO����ֶ�.
	 * <p>
	 * ��������:2013-03-23 18:52:53
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getParentPKFieldName() {
		return "pk_rece_check_sheet";
	}

	/**
	 * <p>
	 * ȡ�ñ����.
	 * <p>
	 * ��������:2013-03-23 18:52:53
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getPKFieldName() {
		return "pk_rece_check_sheet_b";
	}

	/**
	 * <p>
	 * ���ر����.
	 * <p>
	 * ��������:2013-03-23 18:52:53
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "ts_rece_check_sheet_b";
	}

	/**
	 * ����Ĭ�Ϸ�ʽ����������.
	 * 
	 * ��������:2013-03-23 18:52:53
	 */
	public ReceCheckSheetBVO() {
		super();
	}
}
