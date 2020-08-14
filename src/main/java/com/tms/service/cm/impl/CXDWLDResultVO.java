/**
 * 
 */
package com.tms.service.cm.impl;

import org.nw.vo.pub.lang.UFDouble;

/**
 * 车型吨位+重量算法返回的vo
 * 
 * @author xuqc
 * @Date 2015年6月4日 下午1:56:18
 *
 */
public class CXDWLDResultVO {

	UFDouble amount = UFDouble.ZERO_DBL;// 金额
	UFDouble price = UFDouble.ZERO_DBL;// 单价
	Integer count = 0;// 车辆数量
	UFDouble car_weight = UFDouble.ZERO_DBL;// 车辆吨位
	
	UFDouble cost = UFDouble.ZERO_DBL;// 成本
	UFDouble contract_cost = UFDouble.ZERO_DBL;// 成本

	public UFDouble getAmount() {
		return amount;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
	}

	public UFDouble getPrice() {
		return price;
	}

	public void setPrice(UFDouble price) {
		this.price = price;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public UFDouble getCar_weight() {
		return car_weight;
	}

	public void setCar_weight(UFDouble car_weight) {
		this.car_weight = car_weight;
	}

	public UFDouble getCost() {
		return cost;
	}

	public void setCost(UFDouble cost) {
		this.cost = cost;
	}

	public UFDouble getContract_cost() {
		return contract_cost;
	}

	public void setContract_cost(UFDouble contract_cost) {
		this.contract_cost = contract_cost;
	}
	
}
