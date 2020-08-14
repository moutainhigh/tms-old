package com.tms.vo.tp;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

public class ShipMentVO extends SuperVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ord_lot;//批次号
	String car_type;//车型
	String seg_vbillno;//系统原运段号
	String orderno;//客户订单号
	String ord_orderno;//拆分后的运段号
	String pk_delivery;//提货方
	String pk_arrival;//到货方
	UFDateTime req_deli_date;//提货要求到达时间
	UFDateTime req_deli_time;//提货要求离开时间
	UFDateTime req_arri_date;//到货要求到达时间
	UFDateTime req_arri_time;//到货要求离开时间
	Integer num_count;//托数
	
	UFDouble pack_num_count;//件
	UFDouble weight_count;//重
	UFDouble volume_count;//体
	
	String memo;//备注
	
	Integer seg_id;//拆段，0表示不拆段，1表示第一段，2表示第二段，以此类推
	Integer split;//是否分量，0表示没有分量，1表示分量
	Integer cancle;//1表示删除
	
	public String getOrd_lot() {
		return ord_lot;
	}

	public void setOrd_lot(String ord_lot) {
		this.ord_lot = ord_lot;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String car_type) {
		this.car_type = car_type;
	}

	public String getSeg_vbillno() {
		return seg_vbillno;
	}

	public void setSeg_vbillno(String seg_vbillno) {
		this.seg_vbillno = seg_vbillno;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getOrd_orderno() {
		return ord_orderno;
	}

	public void setOrd_orderno(String ord_orderno) {
		this.ord_orderno = ord_orderno;
	}

	public String getPk_delivery() {
		return pk_delivery;
	}

	public void setPk_delivery(String pk_delivery) {
		this.pk_delivery = pk_delivery;
	}

	public String getPk_arrival() {
		return pk_arrival;
	}

	public void setPk_arrival(String pk_arrival) {
		this.pk_arrival = pk_arrival;
	}

	public UFDateTime getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(UFDateTime req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public UFDateTime getReq_deli_time() {
		return req_deli_time;
	}

	public void setReq_deli_time(UFDateTime req_deli_time) {
		this.req_deli_time = req_deli_time;
	}

	public UFDateTime getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(UFDateTime req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public UFDateTime getReq_arri_time() {
		return req_arri_time;
	}

	public void setReq_arri_time(UFDateTime req_arri_time) {
		this.req_arri_time = req_arri_time;
	}

	public Integer getNum_count() {
		return num_count;
	}

	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}

	public UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Integer getSeg_id() {
		return seg_id;
	}

	public void setSeg_id(Integer seg_id) {
		this.seg_id = seg_id;
	}

	public Integer getSplit() {
		return split;
	}

	public void setSplit(Integer split) {
		this.split = split;
	}

	public Integer getCancle() {
		return cancle;
	}

	public void setCancle(Integer cancle) {
		this.cancle = cancle;
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
