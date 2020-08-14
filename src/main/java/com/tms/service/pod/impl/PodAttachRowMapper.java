package com.tms.service.pod.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.nw.vo.pub.lang.UFDateTime;
import org.springframework.jdbc.core.RowMapper;

import com.tms.vo.pod.PodAttachVO;

/**
 * 
 * @author xuqc
 * @date 2013-4-18 上午01:33:19
 */
public class PodAttachRowMapper implements RowMapper<PodAttachVO> {

	public PodAttachVO mapRow(ResultSet rs, int rowNum) throws SQLException {
		PodAttachVO attachVO = new PodAttachVO();
		attachVO.setPk_pod_attach(rs.getString("pk_pod_attach"));
		attachVO.setDr(rs.getInt("dr"));
		attachVO.setTs(new UFDateTime(rs.getString("ts")));
		attachVO.setPk_invoice(rs.getString("pk_invoice"));
		attachVO.setFile_name(rs.getString("file_name"));
		attachVO.setFile_size(rs.getLong("file_size"));
		attachVO.setSuffix(rs.getString("suffix"));
		attachVO.setMemo(rs.getString("memo"));
		attachVO.setCreate_user(rs.getString("create_user"));
		attachVO.setCreate_time(new UFDateTime(rs.getString("create_time")));
		return attachVO;
	}

}
