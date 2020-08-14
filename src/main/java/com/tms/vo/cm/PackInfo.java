package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class PackInfo extends SuperVO {
	
	private String pack;//委托单
	private Integer num;//委托单号 委托单和委托单PK都可以用来查找委托单
	private UFDouble weight;//承运商
	private UFDouble volume;//数量
	

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

	public Integer getNum() {
		return num == null ? 0 : num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public UFDouble getWeight() {
		return weight == null ? UFDouble.ZERO_DBL : weight;
	}

	public void setWeight(UFDouble weight) {
		this.weight = weight;
	}

	public UFDouble getVolume() {
		return volume == null ? UFDouble.ZERO_DBL : volume;
	}

	public void setVolume(UFDouble volume) {
		this.volume = volume;
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
