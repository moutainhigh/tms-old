package com.tms.vo.inv;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

import java.util.List;
import java.util.Map;

/**
 * Created by XIA on 2016/5/25.
 */
@SuppressWarnings("serial")
public class InvView extends SuperVO {

    private String pk_invoice;
    private String cust_name;
    private String vbillno;
    private String cust_orderno;
    private String orderno;
    private String trans_type_name;
    private String req_deli_date;
    private String req_arri_date;
    private String deli_name;
    private UFDouble deli_longitude;
    private UFDouble deli_latitude;
    private String arri_name;
    private UFDouble arri_longitude;
    private UFDouble arri_latitude;
    private Integer urgent_level;
    private UFDouble weight_count;
    private Integer num_count;
    private UFDouble volume_count;
    private String vbillstatus;
    private String carno;
    private String gps_id;
    private String act_deli_date;
    private String act_arri_date;
    private String driver;
    private String driver_mobile;
    private String carr_name;
    private String create_time;
    private String create_user;
    private String confirm_user;
    private String confirm_time;
    private String deli_time;
    private String arri_time;
    private String sign_time;
    private String back_time;
    private String if_customs_official;
    private String pk_customer;
    private String pk_corp;
    private String memo;
    private String photo;
    private List<Map<String, String>> transbility;
    
	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getConfirm_time() {
		return confirm_time;
	}

	public void setConfirm_time(String confirm_time) {
		this.confirm_time = confirm_time;
	}

	public String getDeli_time() {
		return deli_time;
	}

	public void setDeli_time(String deli_time) {
		this.deli_time = deli_time;
	}

	public String getArri_time() {
		return arri_time;
	}

	public void setArri_time(String arri_time) {
		this.arri_time = arri_time;
	}

	public String getSign_time() {
		return sign_time;
	}

	public void setSign_time(String sign_time) {
		this.sign_time = sign_time;
	}

	public String getBack_time() {
		return back_time;
	}

	public void setBack_time(String back_time) {
		this.back_time = back_time;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getDriver_mobile() {
		return driver_mobile;
	}

	public void setDriver_mobile(String driver_mobile) {
		this.driver_mobile = driver_mobile;
	}

	public String getPk_invoice() {
        return pk_invoice;
    }

    public void setPk_invoice(String pk_invoice) {
        this.pk_invoice = pk_invoice;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getVbillno() {
        return vbillno;
    }

    public void setVbillno(String vbillno) {
        this.vbillno = vbillno;
    }

    public String getCust_orderno() {
        return cust_orderno;
    }

    public void setCust_orderno(String cust_orderno) {
        this.cust_orderno = cust_orderno;
    }

    public String getTrans_type_name() {
        return trans_type_name;
    }

    public void setTrans_type_name(String trans_type_name) {
        this.trans_type_name = trans_type_name;
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

    public String getDeli_name() {
        return deli_name;
    }

    public void setDeli_name(String deli_name) {
        this.deli_name = deli_name;
    }

    public UFDouble getDeli_longitude() {
        return deli_longitude;
    }

    public void setDeli_longitude(UFDouble deli_longitude) {
        this.deli_longitude = deli_longitude;
    }

    public UFDouble getDeli_latitude() {
        return deli_latitude;
    }

    public void setDeli_latitude(UFDouble deli_latitude) {
        this.deli_latitude = deli_latitude;
    }

    public String getArri_name() {
        return arri_name;
    }

    public void setArri_name(String arri_name) {
        this.arri_name = arri_name;
    }

    public UFDouble getArri_longitude() {
        return arri_longitude;
    }

    public void setArri_longitude(UFDouble arri_longitude) {
        this.arri_longitude = arri_longitude;
    }

    public UFDouble getArri_latitude() {
        return arri_latitude;
    }

    public void setArri_latitude(UFDouble arri_latitude) {
        this.arri_latitude = arri_latitude;
    }

    public Integer getUrgent_level() {
        return urgent_level;
    }

    public void setUrgent_level(Integer urgent_level) {
        this.urgent_level = urgent_level;
    }

    public UFDouble getWeight_count() {
        return weight_count;
    }

    public void setWeight_count(UFDouble weight_count) {
        this.weight_count = weight_count;
    }

    public Integer getNum_count() {
        return num_count;
    }

    public void setNum_count(Integer num_count) {
        this.num_count = num_count;
    }

    public UFDouble getVolume_count() {
        return volume_count;
    }

    public void setVolume_count(UFDouble volume_count) {
        this.volume_count = volume_count;
    }

    public String getVbillstatus() {
        return vbillstatus;
    }

    public void setVbillstatus(String vbillstatus) {
        this.vbillstatus = vbillstatus;
    }

    public String getCarno() {
        return carno;
    }

    public void setCarno(String carno) {
        this.carno = carno;
    }

    public String getGps_id() {
        return gps_id;
    }

    public void setGps_id(String gps_id) {
        this.gps_id = gps_id;
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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getCarr_name() {
        return carr_name;
    }

    public void setCarr_name(String carr_name) {
        this.carr_name = carr_name;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public String getConfirm_user() {
        return confirm_user;
    }

    public void setConfirm_user(String confirm_user) {
        this.confirm_user = confirm_user;
    }


    public String getIf_customs_official() {
        return if_customs_official;
    }

    public void setIf_customs_official(String if_customs_official) {
        this.if_customs_official = if_customs_official;
    }

    public List<Map<String, String>> getTransbility() {
        return transbility;
    }

    public void setTransbility(List<Map<String, String>> transbility) {
        this.transbility = transbility;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
    }

    @Override
    public String getPKFieldName() {
        return "pk_invoice";
    }

    @Override
    public String getTableName() {
        return "ts_inv_view";
    }
}
