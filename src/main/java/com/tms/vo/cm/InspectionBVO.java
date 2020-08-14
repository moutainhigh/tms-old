package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class InspectionBVO extends SuperVO {
	
	private String pk_inspection_b;
	private String pk_inspection;
	private Integer dr;
	private UFDateTime ts;
	private String modifiedvalue;
	private String fee_code;
	private String type;
	
	private String fee_confirmed;
	private UFDateTime finished;
	private String approvedby;
	private String feedback_result;
	private String closed_reason;
	private UFBoolean edi_flag;
	private String edi_msg;
	private Integer inspection_status;
	private String def1;
	private String def2;
	private String def3;
	private String def4;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def9;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;

	public static final String PK_INSPECTION = "pk_inspection";
	public static final String PK_INSPECTION_B = "pk_inspection_b";
	public static final String TS_INSPECTION_B = "ts_inspection_b";
	

	public String getPk_inspection_b() {
		return pk_inspection_b;
	}

	public void setPk_inspection_b(String pk_inspection_b) {
		this.pk_inspection_b = pk_inspection_b;
	}

	public String getPk_inspection() {
		return pk_inspection;
	}

	public void setPk_inspection(String pk_inspection) {
		this.pk_inspection = pk_inspection;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getModifiedvalue() {
		return modifiedvalue;
	}

	public void setModifiedvalue(String modifiedvalue) {
		this.modifiedvalue = modifiedvalue;
	}

	public String getFee_code() {
		return fee_code;
	}

	public void setFee_code(String fee_code) {
		this.fee_code = fee_code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFee_confirmed() {
		return fee_confirmed;
	}

	public void setFee_confirmed(String fee_confirmed) {
		this.fee_confirmed = fee_confirmed;
	}

	public UFDateTime getFinished() {
		return finished;
	}

	public void setFinished(UFDateTime finished) {
		this.finished = finished;
	}

	public String getApprovedby() {
		return approvedby;
	}

	public void setApprovedby(String approvedby) {
		this.approvedby = approvedby;
	}

	public String getFeedback_result() {
		return feedback_result;
	}

	public void setFeedback_result(String feedback_result) {
		this.feedback_result = feedback_result;
	}

	public String getClosed_reason() {
		return closed_reason;
	}

	public void setClosed_reason(String closed_reason) {
		this.closed_reason = closed_reason;
	}

	public UFBoolean getEdi_flag() {
		return edi_flag;
	}

	public void setEdi_flag(UFBoolean edi_flag) {
		this.edi_flag = edi_flag;
	}

	public String getEdi_msg() {
		return edi_msg;
	}

	public void setEdi_msg(String edi_msg) {
		this.edi_msg = edi_msg;
	}

	public Integer getInspection_status() {
		return inspection_status;
	}

	public void setInspection_status(Integer inspection_status) {
		this.inspection_status = inspection_status;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
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

	public String getDef9() {
		return def9;
	}

	public void setDef9(String def9) {
		this.def9 = def9;
	}

	public String getDef10() {
		return def10;
	}

	public void setDef10(String def10) {
		this.def10 = def10;
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

	@Override
	public String getParentPKFieldName() {
		return PK_INSPECTION;
	}

	@Override
	public String getPKFieldName() {
		return PK_INSPECTION_B;
	}

	@Override
	public String getTableName() {
		return TS_INSPECTION_B;
	}

}
