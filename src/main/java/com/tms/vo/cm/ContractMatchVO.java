package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;

/**
 * 合同匹配类，没有具体的实体表，只作为合同匹配使用
 * @author XIA
 *
 */
@SuppressWarnings("serial")
public class ContractMatchVO extends SuperVO {
	
	private Integer contract_type; //合同类型
	private String pk_carrierOrBala_customer;//匹配和承运商或者结算客户
	private String pk_trans_type;//运输方式
	private String start_addr;//起始地址
	private String end_addr;//终点地址
	private String start_city;//起始城市
	private String end_city;//终点城市
	private String pk_corp;//公司
	private String req_arri_date;//要求到达时间，主要用来匹配合同有效期
	private Integer urgent_level;//紧急程度
	private String item_code;//项目编码
	private String pk_trans_line;//运输线路
	private UFBoolean if_return;//是否回城
	private Integer parent_type;//费用大类

	public Integer getContract_type() {
		return contract_type;
	}

	public void setContract_type(Integer contract_type) {
		this.contract_type = contract_type;
	}

	public String getPk_carrierOrBala_customer() {
		return pk_carrierOrBala_customer;
	}

	public void setPk_carrierOrBala_customer(String pk_carrierOrBala_customer) {
		this.pk_carrierOrBala_customer = pk_carrierOrBala_customer;
	}

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public String getStart_addr() {
		return start_addr;
	}

	public void setStart_addr(String start_addr) {
		this.start_addr = start_addr;
	}

	public String getEnd_addr() {
		return end_addr;
	}

	public void setEnd_addr(String end_addr) {
		this.end_addr = end_addr;
	}

	public String getStart_city() {
		return start_city;
	}

	public void setStart_city(String start_city) {
		this.start_city = start_city;
	}

	public String getEnd_city() {
		return end_city;
	}

	public void setEnd_city(String end_city) {
		this.end_city = end_city;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public Integer getUrgent_level() {
		return urgent_level;
	}

	public void setUrgent_level(Integer urgent_level) {
		this.urgent_level = urgent_level;
	}

	public String getItem_code() {
		return item_code;
	}

	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}

	public String getPk_trans_line() {
		return pk_trans_line;
	}

	public void setPk_trans_line(String pk_trans_line) {
		this.pk_trans_line = pk_trans_line;
	}

	public UFBoolean getIf_return() {
		return if_return;
	}

	public void setIf_return(UFBoolean if_return) {
		this.if_return = if_return;
	}
	
	public Integer getParent_type() {
		return parent_type;
	}

	public void setParent_type(Integer parent_type) {
		this.parent_type = parent_type;
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
