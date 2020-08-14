package org.nw.service.sys.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.DocumentVO;
import org.springframework.jdbc.core.RowMapper;

/**
 * nw_document于vo的对照关系
 * 
 * @author xuqc
 * @date 2013-4-18 上午01:33:19
 */
public class DocumentRowMapper implements RowMapper<DocumentVO> {

	public DocumentVO mapRow(ResultSet rs, int rowNum) throws SQLException {
		DocumentVO docVO = new DocumentVO();
		docVO.setPk_document(rs.getString(DocumentVO.PK_DOCUMENT));
		docVO.setDr(rs.getInt(DocumentVO.DR));
		docVO.setTs(new UFDateTime(rs.getString(DocumentVO.TS)));
		docVO.setTitle(rs.getString(DocumentVO.TITLE));
		docVO.setSummary(rs.getString(DocumentVO.SUMMARY));
		docVO.setSerialno(rs.getString(DocumentVO.SERIALNO));
		docVO.setPost_org(rs.getString(DocumentVO.POST_ORG));
		docVO.setPost_date(new UFDate(rs.getString(DocumentVO.POST_DATE)));
		docVO.setFileno(rs.getString(DocumentVO.FILENO));
		docVO.setFile_size(rs.getLong(DocumentVO.FILE_SIZE));
		docVO.setFile_name(rs.getString(DocumentVO.FILE_NAME));
		docVO.setDef1(rs.getString(DocumentVO.DEF1));
		docVO.setDef2(rs.getString(DocumentVO.DEF2));
		docVO.setDef3(rs.getString(DocumentVO.DEF3));
		docVO.setDef4(rs.getString(DocumentVO.DEF4));
		docVO.setDef5(rs.getString(DocumentVO.DEF5));
		docVO.setCreate_user(rs.getString(DocumentVO.CREATE_USER));
		docVO.setCreate_time(new UFDateTime(rs.getString(DocumentVO.CREATE_TIME)));
		return docVO;
	}

}
