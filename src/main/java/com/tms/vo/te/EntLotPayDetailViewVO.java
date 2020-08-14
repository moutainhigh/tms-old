package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class EntLotPayDetailViewVO extends SuperVO {
	
	private String lot;
	private String name;
	private Integer valuation_type;
	private Integer price_type;
	private Integer quote_type;
	private UFDouble price;
	private UFDouble amount;
	private UFDouble contract_amount;
	private UFBoolean system_create;
	
	public static final String LOT = "lot";
	public static final String TS_ENT_LOT_PAY_DETAIL_VIEW = "ts_ent_lot_pay_detail_view";

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValuation_type() {
		return valuation_type;
	}

	public void setValuation_type(Integer valuation_type) {
		this.valuation_type = valuation_type;
	}

	public Integer getPrice_type() {
		return price_type;
	}

	public void setPrice_type(Integer price_type) {
		this.price_type = price_type;
	}

	public Integer getQuote_type() {
		return quote_type;
	}

	public void setQuote_type(Integer quote_type) {
		this.quote_type = quote_type;
	}

	public UFDouble getPrice() {
		return price;
	}

	public void setPrice(UFDouble price) {
		this.price = price;
	}

	public UFDouble getAmount() {
		return amount;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
	}

	public UFDouble getContract_amount() {
		return contract_amount;
	}

	public void setContract_amount(UFDouble contract_amount) {
		this.contract_amount = contract_amount;
	}

	public UFBoolean getSystem_create() {
		return system_create;
	}

	public void setSystem_create(UFBoolean system_create) {
		this.system_create = system_create;
	}

	public String getParentPKFieldName() {
		return LOT;
	}
	
	public String getPKFieldName() {
		return LOT;
	}
	
	public String getTableName() {
		return TS_ENT_LOT_PAY_DETAIL_VIEW;
	}
	
	public EntLotPayDetailViewVO() {
		super();
	}
}
