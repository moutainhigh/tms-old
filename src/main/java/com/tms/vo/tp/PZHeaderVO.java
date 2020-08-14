package com.tms.vo.tp;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 配载页面的表头VO
 * 
 * @author xuqc
 * @date 2012-9-9 下午12:42:58
 */
public class PZHeaderVO extends SuperVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1618858768678841035L;
	private String carno; // 车牌号
	private String gps_id;//
	private String pk_carrier;// 承运商
	private String pk_driver;// 司机
	private String driver_name;
	private String driver_mobile;
	private String pk_car_type;// 车辆类型
	private String pk_trans_type;// 运输方式
	private String pk_flight; // 航班
	private Integer balatype;// 结算方式
	private String memo;// 备注

	private Integer valuation_type;// 计价方式，批量配载时使用

	private UFDouble pack_num_count;// 总件数
	private int num_count;// 总件数
	private UFDouble weight_count;// 总重量
	private UFDouble volume_count;// 总体积
	private UFDouble amount_count;// 总金额
	private UFDouble fee_weight_count;// 总计费重
	private UFDouble volume_weight_count;// 总体积重
	private UFDouble cost_amount;// 总金额
	private String lot;//批次号
	//yaojiie 2016 1 3 增加是否追加字段
	private UFBoolean is_append;
	
	//yaojiie 2016 1 11 增加字段
	private Integer urgent_level;
	private String item_code;
	private String pk_trans_line;
	private UFBoolean if_return;
	
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;
	private String certificate_id;
	
		
	public String getCertificate_id() {
		return certificate_id;
	}

	public void setCertificate_id(String certificate_id) {
		this.certificate_id = certificate_id;
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

	public String getDriver_name() {
		return driver_name;
	}

	public void setDriver_name(String driver_name) {
		this.driver_name = driver_name;
	}

	public String getDriver_mobile() {
		return driver_mobile;
	}

	public void setDriver_mobile(String driver_mobile) {
		this.driver_mobile = driver_mobile;
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

	public UFBoolean getIs_append() {
		return is_append;
	}

	public void setIs_append(UFBoolean is_append) {
		this.is_append = is_append;
	}
	
	
	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}

	public UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count;
	}

	public Integer getValuation_type() {
		return valuation_type;
	}

	public void setValuation_type(Integer valuation_type) {
		this.valuation_type = valuation_type;
	}

	public UFDouble getVolume_weight_count() {
		return volume_weight_count;
	}

	public void setVolume_weight_count(UFDouble volume_weight_count) {
		this.volume_weight_count = volume_weight_count;
	}

	public UFDouble getCost_amount() {
		return cost_amount;
	}

	public void setCost_amount(UFDouble cost_amount) {
		this.cost_amount = cost_amount;
	}

	public UFDouble getFee_weight_count() {
		return fee_weight_count;
	}

	public void setFee_weight_count(UFDouble fee_weight_count) {
		this.fee_weight_count = fee_weight_count;
	}

	public String getPk_flight() {
		return pk_flight;
	}

	public void setPk_flight(String pk_flight) {
		this.pk_flight = pk_flight;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getPk_driver() {
		return pk_driver;
	}

	public void setPk_driver(String pk_driver) {
		this.pk_driver = pk_driver;
	}

	public String getPk_car_type() {
		return pk_car_type;
	}

	public void setPk_car_type(String pk_car_type) {
		this.pk_car_type = pk_car_type;
	}

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public Integer getBalatype() {
		return balatype;
	}

	public void setBalatype(Integer balatype) {
		this.balatype = balatype;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getNum_count() {
		return num_count;
	}

	public void setNum_count(int num_count) {
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

	public UFDouble getAmount_count() {
		return amount_count;
	}

	public void setAmount_count(UFDouble amount_count) {
		this.amount_count = amount_count;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_entrust";
	}

	public String getTableName() {
		return "ts_entrust";
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}
}
