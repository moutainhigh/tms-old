package com.tms.vo.cm;

import java.util.List;
import java.util.Map;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ReceDetailBMatchVO extends SuperVO {
	
	private String bala_customer;//客户
	private UFDouble pack_num_count;//数量
	private Integer num_count;//件数
	private UFDouble fee_weight_count;//计费重
	private UFDouble weight_count;//重量
	private UFDouble volume_count;//体积
	private Integer deli_node_count;//提货点
	private Integer arri_node_count;//到货点
	private List<PackInfo> packInfos;//包装和包装数量
	private String[] pk_car_types;//车型
	private String pk_corp;//公司
	private Integer urgent_level;//紧急程度
	private String item_code;//项目编码
	private String pk_trans_line;//线路
	private UFBoolean if_return;//是否回城
	private List<ContractBVO> contractBVOs;//合同
	


	public String getBala_customer() {
		return bala_customer;
	}

	public void setBala_customer(String bala_customer) {
		this.bala_customer = bala_customer;
	}

	public UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count == null ? UFDouble.ZERO_DBL : pack_num_count;
	}

	public Integer getNum_count() {
		return num_count;
	}

	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}

	public UFDouble getFee_weight_count() {
		return fee_weight_count == null ? UFDouble.ZERO_DBL : fee_weight_count;
	}

	public void setFee_weight_count(UFDouble fee_weight_count) {
		this.fee_weight_count = fee_weight_count;
	}

	public UFDouble getWeight_count() {
		return weight_count == null ? UFDouble.ZERO_DBL : weight_count;
	}

	public void setWeight_count(UFDouble weight_count) {
		this.weight_count = weight_count;
	}

	public UFDouble getVolume_count() {
		return volume_count == null ? UFDouble.ZERO_DBL : volume_count;
	}

	public void setVolume_count(UFDouble volume_count) {
		this.volume_count = volume_count;
	}

	public Integer getDeli_node_count() {
		return deli_node_count;
	}

	public void setDeli_node_count(Integer deli_node_count) {
		this.deli_node_count = deli_node_count;
	}

	public Integer getArri_node_count() {
		return arri_node_count;
	}

	public void setArri_node_count(Integer arri_node_count) {
		this.arri_node_count = arri_node_count;
	}

	public List<PackInfo> getPackInfos() {
		return packInfos;
	}

	public void setPackInfos(List<PackInfo> packInfos) {
		this.packInfos = packInfos;
	}

	public String[] getPk_car_types() {
		return pk_car_types;
	}

	public void setPk_car_types(String[] pk_car_types) {
		this.pk_car_types = pk_car_types;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
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

	public List<ContractBVO> getContractBVOs() {
		return contractBVOs;
	}

	public void setContractBVOs(List<ContractBVO> contractBVOs) {
		this.contractBVOs = contractBVOs;
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
