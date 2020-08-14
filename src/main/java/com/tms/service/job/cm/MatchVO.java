/**
 * 
 */
package com.tms.service.job.cm;

import java.util.List;
import java.util.Map;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.cm.PackInfo;

/**
 * @author xuqc
 * @Date 2015年6月3日 下午4:38:21
 */
public class MatchVO {

	private String invoice_vbillno;
	private String pk_customer;
	private String bala_customer;
	private String pk_trans_type;
	private String orderno;

	private String rd_vbillno;// 应收明细单号
	private String pk_receive_detail;//

	// 发货单的相应信息
	private String pk_delivery;
	private String deli_city;
	private String pk_arrival;
	private String arri_city;
	private String act_deli_date;
	private String act_arri_date;
	private String req_deli_date;
	private String req_arri_date;
	
	// 辅助表的相应信息
	private String pk_supplier_ass;
	private java.lang.String pk_delivery_ass;
	private java.lang.String deli_city_ass;
	private java.lang.String pk_arrival_ass;
	private java.lang.String arri_city_ass;
	private java.lang.String req_deli_date_ass;
	private java.lang.String act_deli_date_ass;
	private java.lang.String req_arri_date_ass;
	private java.lang.String act_arri_date_ass;

	private UFDouble pack_num_count;
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;
	private UFDouble fee_weight_count;
	private List<PackInfo> packInfos;
	private String pk_corp;
	
	private String pk_supplier;

	//2015-10-19  songf 增加def6字段
	private String def6;
	
	//yaojiie 2016 1 11 增加字段
	private Integer urgent_level;
	private String item_code;
	private String pk_trans_line;
	private UFBoolean if_return;
	
	public List<PackInfo> getPackInfos() {
		return packInfos;
	}

	public void setPackInfos(List<PackInfo> packInfos) {
		this.packInfos = packInfos;
	}

	public UFBoolean getIf_return() {
		return if_return;
	}

	public void setIf_return(UFBoolean if_return) {
		this.if_return = if_return;
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

	public String getDef6() {
		return def6;
	}

	public void setDef6(String def6) {
		this.def6 = def6;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count;
	}

	public String getPk_receive_detail() {
		return pk_receive_detail;
	}

	public void setPk_receive_detail(String pk_receive_detail) {
		this.pk_receive_detail = pk_receive_detail;
	}

	public String getRd_vbillno() {
		return rd_vbillno;
	}

	public void setRd_vbillno(String rd_vbillno) {
		this.rd_vbillno = rd_vbillno;
	}

	public Integer getNum_count() {
		return num_count;
	}

	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}

	public UFDouble getWeight_count() {
		return weight_count;
	}

	public void setWeight_count(UFDouble weight_count) {
		this.weight_count = weight_count;
	}

	public UFDouble getVolume_count() {
		return volume_count;
	}

	public void setVolume_count(UFDouble volume_count) {
		this.volume_count = volume_count;
	}

	public UFDouble getFee_weight_count() {
		return fee_weight_count;
	}

	public void setFee_weight_count(UFDouble fee_weight_count) {
		this.fee_weight_count = fee_weight_count;
	}

	public String getInvoice_vbillno() {
		return invoice_vbillno;
	}

	public void setInvoice_vbillno(String invoice_vbillno) {
		this.invoice_vbillno = invoice_vbillno;
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

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getPk_supplier_ass() {
		return pk_supplier_ass;
	}

	public void setPk_supplier_ass(String pk_supplier_ass) {
		this.pk_supplier_ass = pk_supplier_ass;
	}

	public java.lang.String getPk_delivery_ass() {
		return pk_delivery_ass;
	}

	public void setPk_delivery_ass(java.lang.String pk_delivery_ass) {
		this.pk_delivery_ass = pk_delivery_ass;
	}

	public java.lang.String getDeli_city_ass() {
		return deli_city_ass;
	}

	public void setDeli_city_ass(java.lang.String deli_city_ass) {
		this.deli_city_ass = deli_city_ass;
	}

	public java.lang.String getPk_arrival_ass() {
		return pk_arrival_ass;
	}

	public void setPk_arrival_ass(java.lang.String pk_arrival_ass) {
		this.pk_arrival_ass = pk_arrival_ass;
	}

	public java.lang.String getArri_city_ass() {
		return arri_city_ass;
	}

	public void setArri_city_ass(java.lang.String arri_city_ass) {
		this.arri_city_ass = arri_city_ass;
	}

	public java.lang.String getReq_deli_date_ass() {
		return req_deli_date_ass;
	}

	public void setReq_deli_date_ass(java.lang.String req_deli_date_ass) {
		this.req_deli_date_ass = req_deli_date_ass;
	}

	public java.lang.String getAct_deli_date_ass() {
		return act_deli_date_ass;
	}

	public void setAct_deli_date_ass(java.lang.String act_deli_date_ass) {
		this.act_deli_date_ass = act_deli_date_ass;
	}

	public java.lang.String getReq_arri_date_ass() {
		return req_arri_date_ass;
	}

	public void setReq_arri_date_ass(java.lang.String req_arri_date_ass) {
		this.req_arri_date_ass = req_arri_date_ass;
	}

	public java.lang.String getAct_arri_date_ass() {
		return act_arri_date_ass;
	}

	public void setAct_arri_date_ass(java.lang.String act_arri_date_ass) {
		this.act_arri_date_ass = act_arri_date_ass;
	}

	public String getPk_delivery() {
		return pk_delivery;
	}

	public void setPk_delivery(String pk_delivery) {
		this.pk_delivery = pk_delivery;
	}

	public String getDeli_city() {
		return deli_city;
	}

	public void setDeli_city(String deli_city) {
		this.deli_city = deli_city;
	}

	public String getPk_arrival() {
		return pk_arrival;
	}

	public void setPk_arrival(String pk_arrival) {
		this.pk_arrival = pk_arrival;
	}

	public String getArri_city() {
		return arri_city;
	}

	public void setArri_city(String arri_city) {
		this.arri_city = arri_city;
	}

	public String getAct_deli_date() {
		return act_deli_date;
	}

	public void setAct_deli_date(String act_deli_date) {
		this.act_deli_date = act_deli_date;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public String getAct_arri_date() {
		return act_arri_date;
	}

	public void setAct_arri_date(String act_arri_date) {
		this.act_arri_date = act_arri_date;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public String getPk_supplier() {
		return pk_supplier;
	}

	public void setPk_supplier(String pk_supplier) {
		this.pk_supplier = pk_supplier;
	}
	
	
}
