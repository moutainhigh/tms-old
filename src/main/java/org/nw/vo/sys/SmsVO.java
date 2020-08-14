package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * 
 * @author xuqc
 * @date 2013-7-1 上午11:25:43
 */
public class SmsVO extends SuperVO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UFDateTime ts;
	private Integer dr;
	private String pk_sms;
	private String title;
	private String content;
	private String billids;
	private String billnos;
	private String fun_code;
	private String sender;
	private String receiver;
	private UFDateTime post_date;
	private UFBoolean upload_flag;
	private UFBoolean read_flag;
	private UFDateTime read_date;
	private String pk_corp;
	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	
	public String getBillnos() {
		return billnos;
	}

	public void setBillnos(String billnos) {
		this.billnos = billnos;
	}

	public UFBoolean getUpload_flag() {
		return upload_flag;
	}

	public void setUpload_flag(UFBoolean upload_flag) {
		this.upload_flag = upload_flag;
	}

	public String getBillids() {
		return billids;
	}

	public void setBillids(String billids) {
		this.billids = billids;
	}

	public String getFun_code() {
		return fun_code;
	}

	public void setFun_code(String fun_code) {
		this.fun_code = fun_code;
	}

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}

	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
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

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public String getPk_sms() {
		return pk_sms;
	}

	public void setPk_sms(String pk_sms) {
		this.pk_sms = pk_sms;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public UFDateTime getPost_date() {
		return post_date;
	}

	public void setPost_date(UFDateTime post_date) {
		this.post_date = post_date;
	}

	public UFBoolean getRead_flag() {
		return read_flag;
	}

	public void setRead_flag(UFBoolean read_flag) {
		this.read_flag = read_flag;
	}

	public UFDateTime getRead_date() {
		return read_date;
	}

	public void setRead_date(UFDateTime read_date) {
		this.read_date = read_date;
	}

	public String getParentPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPKFieldName() {
		return "pk_sms";
	}

	public String getTableName() {
		return "nw_sms";
	}

}
