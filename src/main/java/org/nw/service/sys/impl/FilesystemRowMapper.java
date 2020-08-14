package org.nw.service.sys.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.FilesystemVO;
import org.springframework.jdbc.core.RowMapper;

/**
 * nw_filesystem于vo的对照关系
 * 
 * @author xuqc
 * @date 2013-4-18 上午01:33:19
 */
public class FilesystemRowMapper implements RowMapper<FilesystemVO> {

	public FilesystemVO mapRow(ResultSet rs, int rowNum) throws SQLException {
		FilesystemVO attachVO = new FilesystemVO();
		attachVO.setPk_filesystem(rs.getString(FilesystemVO.PK_FILESYSTEM));
		attachVO.setDr(rs.getInt(FilesystemVO.DR));
		attachVO.setTs(new UFDateTime(rs.getString(FilesystemVO.TS)));
		attachVO.setBilltype(rs.getString(FilesystemVO.BILLTYPE));
		attachVO.setPk_bill(rs.getString(FilesystemVO.PK_BILL));
		attachVO.setFolder_flag(new UFBoolean(rs.getString(FilesystemVO.FOLDER_FLAG)));
		attachVO.setFile_name(rs.getString(FilesystemVO.FILE_NAME));
		attachVO.setFile_size(rs.getLong(FilesystemVO.FILE_SIZE));
		attachVO.setParent_id(rs.getString(FilesystemVO.PARENT_ID));
		attachVO.setMd5(rs.getString(FilesystemVO.MD5));
		attachVO.setCreate_user(rs.getString(FilesystemVO.CREATE_USER));
		attachVO.setCreate_time(new UFDateTime(rs.getString(FilesystemVO.CREATE_TIME)));
		return attachVO;
	}

}
