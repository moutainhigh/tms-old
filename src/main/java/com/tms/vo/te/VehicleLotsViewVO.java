package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;

@SuppressWarnings("serial")
public class VehicleLotsViewVO extends SuperVO {
	
	private String carno;
	private String lot;
	private String lot_strat_date;
	private String lot_end_date;
	private String lot_tracking_memo;
	private String lot_strat_addr;
	private String lot_end_addr;
	private String num_count;
	private String weight_count;
	private String volume_count;

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getLot_strat_date() {
		return lot_strat_date;
	}

	public void setLot_strat_date(String lot_strat_date) {
		this.lot_strat_date = lot_strat_date;
	}

	public String getLot_end_date() {
		return lot_end_date;
	}

	public void setLot_end_date(String lot_end_date) {
		this.lot_end_date = lot_end_date;
	}

	public String getLot_tracking_memo() {
		return lot_tracking_memo;
	}

	public void setLot_tracking_memo(String lot_tracking_memo) {
		this.lot_tracking_memo = lot_tracking_memo;
	}


	public String getLot_strat_addr() {
		return lot_strat_addr;
	}

	public void setLot_strat_addr(String lot_strat_addr) {
		this.lot_strat_addr = lot_strat_addr;
	}

	public String getLot_end_addr() {
		return lot_end_addr;
	}

	public void setLot_end_addr(String lot_end_addr) {
		this.lot_end_addr = lot_end_addr;
	}

	public String getNum_count() {
		return num_count;
	}

	public void setNum_count(String num_count) {
		this.num_count = num_count;
	}

	public String getWeight_count() {
		return weight_count;
	}

	public void setWeight_count(String weight_count) {
		this.weight_count = weight_count;
	}

	public String getVolume_count() {
		return volume_count;
	}

	public void setVolume_count(String volume_count) {
		this.volume_count = volume_count;
	}

	public String getParentPKFieldName() {
		return null;
	}
	
	public String getPKFieldName() {
		return "lot";
	}
	
	public String getTableName() {
		return "ts_vehicle_lots_view";
	}
	
	public VehicleLotsViewVO() {
		super();
	}
}
