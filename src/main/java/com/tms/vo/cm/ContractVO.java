/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * <b> �ڴ˴���Ҫ��������Ĺ��� </b>
 * <p>
 * �ڴ˴���Ӵ����������Ϣ
 * </p>
 * ��������:2012-08-28 21:28:00
 * 
 * @author Administrator
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class ContractVO extends SuperVO {
	private String pk_corp;
	private String pk_contract;
	private String code;
	private UFDateTime create_time;
	private String memo;
	private UFBoolean locked_flag;
	private Integer contract_type;
	private String currency;
	private Integer dr;
	private String create_user;
	private UFDateTime invalid_date;
	private String bala_customer;
	private String contractno;
	private UFDateTime effective_date;
	private UFDateTime ts;
	private String trans_type;
	private String pk_carrier;
	private String name;
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
	private UFDateTime modify_time;
	private String modify_user;

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
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
	public static final String PK_CONTRACT = "pk_contract";
	public static final String CODE = "code";
	public static final String CREATE_TIME = "create_time";
	public static final String MEMO = "memo";
	public static final String LOCKED_FLAG = "locked_flag";
	public static final String CONTRACT_TYPE = "contract_type";
	public static final String CURRENCY = "currency";
	public static final String CREATE_USER = "create_user";
	public static final String INVALID_DATE = "invalid_date";
	public static final String BALA_CUSTOMER = "bala_customer";
	public static final String CONTRACTNO = "contractno";
	public static final String EFFECTIVE_DATE = "effective_date";
	public static final String TRANS_TYPE = "trans_type";
	public static final String PK_CARRIER = "pk_carrier";
	public static final String NAME = "name";

	/**
	 * ����pk_corp��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getPk_corp() {
		return pk_corp;
	}

	/**
	 * ����pk_corp��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newPk_corp
	 *            String
	 */
	public void setPk_corp(String newPk_corp) {
		this.pk_corp = newPk_corp;
	}

	/**
	 * ����pk_contract��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getPk_contract() {
		return pk_contract;
	}

	/**
	 * ����pk_contract��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newPk_contract
	 *            String
	 */
	public void setPk_contract(String newPk_contract) {
		this.pk_contract = newPk_contract;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * ����create_time��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return UFDateTime
	 */
	public UFDateTime getCreate_time() {
		return create_time;
	}

	/**
	 * ����create_time��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newCreate_time
	 *            UFDateTime
	 */
	public void setCreate_time(UFDateTime newCreate_time) {
		this.create_time = newCreate_time;
	}

	/**
	 * ����memo��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * ����memo��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newMemo
	 *            String
	 */
	public void setMemo(String newMemo) {
		this.memo = newMemo;
	}

	/**
	 * ����locked_flag��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return UFBoolean
	 */
	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	/**
	 * ����locked_flag��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newLocked_flag
	 *            UFBoolean
	 */
	public void setLocked_flag(UFBoolean newLocked_flag) {
		this.locked_flag = newLocked_flag;
	}

	/**
	 * ����contract_type��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return Integer
	 */
	public Integer getContract_type() {
		return contract_type;
	}

	/**
	 * ����contract_type��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newContract_type
	 *            Integer
	 */
	public void setContract_type(Integer newContract_type) {
		this.contract_type = newContract_type;
	}

	/**
	 * ����currency��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * ����currency��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newCurrency
	 *            String
	 */
	public void setCurrency(String newCurrency) {
		this.currency = newCurrency;
	}

	/**
	 * ����dr��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return Integer
	 */
	public Integer getDr() {
		return dr;
	}

	/**
	 * ����dr��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newDr
	 *            Integer
	 */
	public void setDr(Integer newDr) {
		this.dr = newDr;
	}

	/**
	 * ����create_user��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getCreate_user() {
		return create_user;
	}

	/**
	 * ����create_user��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newCreate_user
	 *            String
	 */
	public void setCreate_user(String newCreate_user) {
		this.create_user = newCreate_user;
	}

	/**
	 * ����invalid_date��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return UFDate
	 */
	public UFDateTime getInvalid_date() {
		return invalid_date;
	}

	/**
	 * ����invalid_date��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newInvalid_date
	 *            UFDate
	 */
	public void setInvalid_date(UFDateTime newInvalid_date) {
		this.invalid_date = newInvalid_date;
	}

	/**
	 * ����bala_customer��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getBala_customer() {
		return bala_customer;
	}

	/**
	 * ����bala_customer��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newBala_customer
	 *            String
	 */
	public void setBala_customer(String newBala_customer) {
		this.bala_customer = newBala_customer;
	}

	public String getContractno() {
		return contractno;
	}

	public void setContractno(String contractno) {
		this.contractno = contractno;
	}

	/**
	 * ����effective_date��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return UFDate
	 */
	public UFDateTime getEffective_date() {
		return effective_date;
	}

	/**
	 * ����effective_date��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newEffective_date
	 *            UFDate
	 */
	public void setEffective_date(UFDateTime newEffective_date) {
		this.effective_date = newEffective_date;
	}

	/**
	 * ����ts��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return UFDateTime
	 */
	public UFDateTime getTs() {
		return ts;
	}

	/**
	 * ����ts��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newTs
	 *            UFDateTime
	 */
	public void setTs(UFDateTime newTs) {
		this.ts = newTs;
	}

	/**
	 * ����trans_type��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getTrans_type() {
		return trans_type;
	}

	/**
	 * ����trans_type��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newTrans_type
	 *            String
	 */
	public void setTrans_type(String newTrans_type) {
		this.trans_type = newTrans_type;
	}

	/**
	 * ����pk_carrier��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getPk_carrier() {
		return pk_carrier;
	}

	/**
	 * ����pk_carrier��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newPk_carrier
	 *            String
	 */
	public void setPk_carrier(String newPk_carrier) {
		this.pk_carrier = newPk_carrier;
	}

	/**
	 * ����name��Getter����. ��������:2012-08-28 21:28:00
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * ����name��Setter����. ��������:2012-08-28 21:28:00
	 * 
	 * @param newName
	 *            String
	 */
	public void setName(String newName) {
		this.name = newName;
	}

	/**
	 * <p>
	 * ȡ�ø�VO�����ֶ�.
	 * <p>
	 * ��������:2012-08-28 21:28:00
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getParentPKFieldName() {
		return null;
	}

	/**
	 * <p>
	 * ȡ�ñ�����.
	 * <p>
	 * ��������:2012-08-28 21:28:00
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getPKFieldName() {
		return "pk_contract";
	}

	/**
	 * <p>
	 * ���ر����.
	 * <p>
	 * ��������:2012-08-28 21:28:00
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "ts_contract";
	}

	/**
	 * ����Ĭ�Ϸ�ʽ����������.
	 * 
	 * ��������:2012-08-28 21:28:00
	 */
	public ContractVO() {
		super();
	}
}
