package com.tms.service.job.cm;

import java.util.List;
import java.util.Map;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.cm.PackInfo;

@SuppressWarnings("serial")
public class PayDetailMatchVO extends SuperVO {
	
	private String invoice_villno;
	private String pk_entrust;
	private String entrust_villno;
	private String pk_carrier;
	private String pk_trans_type;

	private String pd_vbillno;// 应付明细单号
	private String pk_pay_detail;//

	// 委托单的相应信息
	private String pk_delivery;
	private String deli_city;
	private String pk_arrival;
	private String arri_city;
	private String act_deli_date;
	private String act_arri_date;
	private String req_deli_date;
	private String req_arri_date;
	private String lot;
	private String def5;
	//用作判断该发货单对应的委托单是否需要计算应付费用。2015-10-31 jonathan
	private String def7;
	//增加订单号，用于干线段匹配合同逻辑，干线段匹配合同用订单辅助表上的起始地，目的地，2015-11-6 joanthan
	private String orderno;
	
	private Integer pack_num_count;
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;
	private UFDouble fee_weight_count;

	private String pk_corp;
	
	//yaojiie 2016 1 11 增加字段
	private Integer urgent_level;
	private String item_code;
	private String pk_trans_line;
	private UFBoolean if_return;
	private List<PackInfo> packInfos;
	

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

	public String getInvoice_villno() {
		return invoice_villno;
	}

	public void setInvoice_villno(String invoice_villno) {
		this.invoice_villno = invoice_villno;
	}

	public String getPk_entrust() {
		return pk_entrust;
	}

	public void setPk_entrust(String pk_entrust) {
		this.pk_entrust = pk_entrust;
	}

	public String getEntrust_villno() {
		return entrust_villno;
	}

	public void setEntrust_villno(String entrust_villno) {
		this.entrust_villno = entrust_villno;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public String getPd_vbillno() {
		return pd_vbillno;
	}

	public void setPd_vbillno(String pd_vbillno) {
		this.pd_vbillno = pd_vbillno;
	}

	public String getPk_pay_detail() {
		return pk_pay_detail;
	}

	public void setPk_pay_detail(String pk_pay_detail) {
		this.pk_pay_detail = pk_pay_detail;
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

	public String getAct_arri_date() {
		return act_arri_date;
	}

	public void setAct_arri_date(String act_arri_date) {
		this.act_arri_date = act_arri_date;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	
	
	public String getDef5() {
		return def5;
	}

	public void setDef5(String def5) {
		this.def5 = def5;
	}

	
	
	public String getDef7() {
		return def7;
	}

	public void setDef7(String def7) {
		this.def7 = def7;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}
	
	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public Integer getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(Integer pack_num_count) {
		this.pack_num_count = pack_num_count;
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

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}
	/**
	 * 按照默认方式创建构造子.
	 * 
	 * 创建日期:2015-10-31 
	 */
	public PayDetailMatchVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

}
