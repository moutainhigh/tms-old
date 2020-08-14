package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * 
 */
public class RefInfoVO extends SuperVO {

	/**
	 * <code>serialVersionUID</code> 的注释
	 */
	private static final long serialVersionUID = 1L;

	private String pk_refinfo = null;

	private String code = null;

	private String name = null;

	private String residPath = null;

	private String resid = null;

	private String module = null;

	private String refclass = null;

	private UFBoolean isspecialref = null;

	private UFBoolean isneedpara = null;

	private Integer reftype = 0;

	private String para1 = null;

	private String para2 = null;

	private String para3 = null;

	private String refsystem = null;

	private String reserv1 = null;

	private String reserv2 = null;

	private String reserv3 = null;

	private String metadataTypeName = null;

	private Integer dr;
	private UFDateTime ts;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;

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

	public String getPKFieldName() {
		return "pk_refinfo";
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getTableName() {
		return "nw_refinfo";
	}

	/**
	 * @return 返回 reserv3。
	 */
	public String getReserv3() {
		return reserv3;
	}

	/**
	 * @param reserv3
	 *            要设置的 reserv3。
	 */
	public void setReserv3(String reserv3) {
		this.reserv3 = reserv3;
	}

	/**
	 * @return 返回 code。
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            要设置的 code。
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return 返回 isneedpara。
	 */
	public UFBoolean getIsneedpara() {
		return isneedpara;
	}

	/**
	 * @param isneedpara
	 *            要设置的 isneedpara。
	 */
	public void setIsneedpara(UFBoolean isneedpara) {
		this.isneedpara = isneedpara;
	}

	/**
	 * @return 返回 isspecialref。
	 */
	public UFBoolean getIsspecialref() {
		return isspecialref;
	}

	/**
	 * @param isspecialref
	 *            要设置的 isspecialref。
	 */
	public void setIsspecialref(UFBoolean isspecialref) {
		this.isspecialref = isspecialref;
	}

	/**
	 * @return 返回 module。
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param module
	 *            要设置的 module。
	 */
	public void setModule(String module) {
		this.module = module;
	}

	/**
	 * @return 返回 name。
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            要设置的 name。
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return 返回 para1。
	 */
	public String getPara1() {
		return para1;
	}

	/**
	 * @param para1
	 *            要设置的 para1。
	 */
	public void setPara1(String para1) {
		this.para1 = para1;
	}

	/**
	 * @return 返回 para2。
	 */
	public String getPara2() {
		return para2;
	}

	/**
	 * @param para2
	 *            要设置的 para2。
	 */
	public void setPara2(String para2) {
		this.para2 = para2;
	}

	/**
	 * @return 返回 para3。
	 */
	public String getPara3() {
		return para3;
	}

	/**
	 * @param para3
	 *            要设置的 para3。
	 */
	public void setPara3(String para3) {
		this.para3 = para3;
	}

	/**
	 * @return 返回 pk_refinfo。
	 */
	public String getPk_refinfo() {
		return pk_refinfo;
	}

	/**
	 * @param pk_refinfo
	 *            要设置的 pk_refinfo。
	 */
	public void setPk_refinfo(String pk_refinfo) {
		this.pk_refinfo = pk_refinfo;
	}

	/**
	 * @return 返回 refclass。
	 */
	public String getRefclass() {
		return refclass;
	}

	/**
	 * @param refclass
	 *            要设置的 refclass。
	 */
	public void setRefclass(String refclass) {
		this.refclass = refclass;
	}

	/**
	 * @return 返回 refsystem。
	 */
	public String getRefsystem() {
		return refsystem;
	}

	/**
	 * @param refsystem
	 *            要设置的 refsystem。
	 */
	public void setRefsystem(String refsystem) {
		this.refsystem = refsystem;
	}

	/**
	 * @return 返回 reserv1。
	 */
	public String getReserv1() {
		return reserv1;
	}

	/**
	 * @param reserv1
	 *            要设置的 reserv1。
	 */
	public void setReserv1(String reserv1) {
		this.reserv1 = reserv1;
	}

	/**
	 * @return 返回 reserv2。
	 */
	public String getReserv2() {
		return reserv2;
	}

	/**
	 * @param reserv2
	 *            要设置的 reserv2。
	 */
	public void setReserv2(String reserv2) {
		this.reserv2 = reserv2;
	}

	/**
	 * @return 返回 resid。
	 */
	public String getResid() {
		return resid;
	}

	/**
	 * @param resid
	 *            要设置的 resid。
	 */
	public void setResid(String resid) {
		this.resid = resid;
	}

	/**
	 * @return 返回 residPath。
	 */
	public String getResidPath() {
		return residPath;
	}

	/**
	 * @param residPath
	 *            要设置的 residPath。
	 */
	public void setResidPath(String residPath) {
		this.residPath = residPath;
	}

	public Integer getReftype() {
		return reftype;
	}

	public void setReftype(Integer reftype) {
		this.reftype = reftype;
	}

	public String getMetadataTypeName() {
		return metadataTypeName;
	}

	public void setMetadataTypeName(String metadataTypeName) {
		this.metadataTypeName = metadataTypeName;
	}

}
