package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class WarehouseVO extends SuperVO {
	
	private UFDateTime account_period;
	private String pk_carrier;
	private String pk_expense_type;
	private String pk_customer;
	private String bala_customer;
	private UFDouble rece_amount;
	private UFDouble pay_amount;
	private String memo;
	

	public UFDateTime getAccount_period() {
		return account_period;
	}

	public void setAccount_period(UFDateTime account_period) {
		this.account_period = account_period;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getPk_expense_type() {
		return pk_expense_type;
	}

	public void setPk_expense_type(String pk_expense_type) {
		this.pk_expense_type = pk_expense_type;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getBala_customer() {
		return bala_customer;
	}

	public void setBala_customer(String bala_customer) {
		this.bala_customer = bala_customer;
	}

	public UFDouble getRece_amount() {
		return rece_amount;
	}

	public void setRece_amount(UFDouble rece_amount) {
		this.rece_amount = rece_amount;
	}

	public UFDouble getPay_amount() {
		return pay_amount;
	}

	public void setPay_amount(UFDouble pay_amount) {
		this.pay_amount = pay_amount;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return null;
	}

	@Override
	public String getTableName() {
		return null;
	}

}
