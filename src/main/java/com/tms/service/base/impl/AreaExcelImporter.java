package com.tms.service.base.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.ExcelImporter;
import org.nw.service.IToftService;
import org.nw.utils.CodenoHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.web.utils.WebUtils;

import com.tms.constants.FunConst;
import com.tms.vo.base.AreaVO;

/**
 * 区域档案的导入类
 * 
 * @author xuqc
 * @date 2013-11-20 上午12:11:51
 */
public class AreaExcelImporter extends ExcelImporter {
	Logger logger = Logger.getLogger(AreaExcelImporter.class);

	public String[] titleAry = new String[] { "洲", "国家", "区域", "省", "市", "区" };

	public AreaExcelImporter(ParamVO paramVO, IToftService service) {
		this.paramVO = paramVO;
		this.service = service;
	}

	class TableVO {
		String continent;
		String country;
		String area;
		String province;
		String city;
		String region;

		public String getContinent() {
			return continent;
		}

		public void setContinent(String continent) {
			this.continent = continent;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getArea() {
			return area;
		}

		public void setArea(String area) {
			this.area = area;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

	}

	public List<AggregatedValueObject> resolve(File file) throws Exception {
		if(file == null) {
			throw new BusiException("没有选择要导入的文件！");
		}
		InputStream input = null;
		Workbook wb = null;
		try {
			List<AggregatedValueObject> aggVOs = new ArrayList<AggregatedValueObject>();// excel文件的数据转换成的vo集合
			input = new FileInputStream(file);
			wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(0);
			int rowNum = sheet.getLastRowNum() + 1; // 得到总行数,这里的总行数是从0开始的,+1
			Row headerRow = sheet.getRow(0); // 标题行
			int colNum = headerRow.getPhysicalNumberOfCells();// 列数。
			// 使用set存储，避免重复
			Set<String> continent = new HashSet<String>();// 洲
			Map<String, Set<String>> map = new HashMap<String, Set<String>>();
			for(int i = 1; i < rowNum; i++) {
				Row row = sheet.getRow(i); // 正文行
				int j = 0;
				logBuf.append("开始读取第" + i + "行数据...<br/>");
				logger.info("开始读取第" + i + "行数据...");
				TableVO vo = new TableVO();
				while(j < colNum) {
					String title = getStringCellValue(headerRow.getCell(j), null).trim();// excel文件的标题栏
					if(StringUtils.isBlank(title)) {
						// 如果标题栏为空，忽略该列，比如是序号列
						j++;
						continue;
					}

					String value = getStringCellValue(row.getCell(j), null).trim();// excel文件中的字段的值
					if(title.equals(titleAry[0])) {
						// 洲
						value += "_1";
						continent.add(value);
						vo.setContinent(value);
					} else if(title.equals(titleAry[1])) {
						// 国家
						value += "_2";
						vo.setCountry(value);

						Set<String> set = map.get(vo.getContinent());
						if(set == null) {
							set = new HashSet<String>();
							map.put(vo.getContinent(), set);
						}
						set.add(value);
					} else if(title.equals(titleAry[2])) {
						// 区域
						value += "_3";
						vo.setArea(value);

						Set<String> set = map.get(vo.getCountry());
						if(set == null) {
							set = new HashSet<String>();
							map.put(vo.getCountry(), set);
						}
						set.add(value);
					} else if(title.equals(titleAry[3])) {
						// 省
						value += "_4";
						vo.setProvince(value);

						Set<String> set = map.get(vo.getArea());
						if(set == null) {
							set = new HashSet<String>();
							map.put(vo.getArea(), set);
						}
						set.add(value);
					} else if(title.equals(titleAry[4])) {
						// 市
						value += "_5";
						vo.setCity(value);

						Set<String> set = map.get(vo.getProvince());
						if(set == null) {
							set = new HashSet<String>();
							map.put(vo.getProvince(), set);
						}
						set.add(value);
					} else if(title.equals(titleAry[5])) {
						// 区
						value += "_6";
						vo.setRegion(value);

						Set<String> set = map.get(vo.getCity());
						if(set == null) {
							set = new HashSet<String>();
							map.put(vo.getCity(), set);
						}
						set.add(value);
					}
					j++;
				}
			}
			System.out.println(map.get("上海市_4"));
			System.out.println(map.get("上海市_5"));
			System.out.println(map.get("上海市_6"));
			// 导入所有的洲
			Iterator<String> it = continent.iterator();
			String funCode = FunConst.AREA_FUN_CODE;
			List<AreaVO> areaVOs = new ArrayList<AreaVO>();
			while(it.hasNext()) {
				String title = it.next();
				logBuf.append("开始导入洲[" + title + "]...<br/>");
				logger.info("开始导入洲[" + title + "]...");
				AreaVO vo = getAreaVO(funCode);
				vo.setName(title);
				vo.setArea_level(1);
				areaVOs.add(vo);

				// 导入所有的国家
				Set<String> country = map.get(title);
				if(country != null) {
					Iterator<String> it1 = country.iterator();
					while(it1.hasNext()) {
						String title1 = it1.next();
						logBuf.append("开始导入国家[" + title1 + "]...<br/>");
						logger.info("开始导入国家[" + title1 + "]...");
						AreaVO vo1 = getAreaVO(funCode);
						vo1.setParent_id(vo.getPk_area());

						vo1.setName(title1);
						vo1.setArea_level(2);
						areaVOs.add(vo1);

						// 导入区域
						Set<String> area = map.get(title1);
						if(area != null) {
							Iterator<String> it2 = area.iterator();
							while(it2.hasNext()) {
								String title2 = it2.next();
								logBuf.append("开始导入区域[" + title2 + "]...<br/>");
								logger.info("开始导入区域[" + title2 + "]...");
								AreaVO vo2 = getAreaVO(funCode);
								vo2.setParent_id(vo1.getPk_area());
								vo2.setName(title2);
								vo2.setArea_level(3);
								areaVOs.add(vo2);

								// 导入省
								Set<String> province = map.get(title2);
								if(province != null) {
									Iterator<String> it3 = province.iterator();
									while(it3.hasNext()) {
										String title3 = it3.next();
										logBuf.append("开始导入省[" + title3 + "]...<br/>");
										logger.info("开始导入省[" + title3 + "]...");
										AreaVO vo3 = getAreaVO(funCode);
										vo3.setParent_id(vo2.getPk_area());
										vo3.setName(title3);
										vo3.setArea_level(4);
										areaVOs.add(vo3);

										// 导入市
										Set<String> city = map.get(title3);
										if(city != null) {
											Iterator<String> it4 = city.iterator();
											while(it4.hasNext()) {
												String title4 = it4.next();
												logBuf.append("开始导入市[" + title4 + "]...<br/>");
												logger.info("开始导入市[" + title4 + "]...");
												AreaVO vo4 = getAreaVO(funCode);
												vo4.setParent_id(vo3.getPk_area());
												vo4.setName(title4);
												vo4.setArea_level(5);
												areaVOs.add(vo4);

												// 导入区
												Set<String> region = map.get(title4);
												if(region != null) {
													Iterator<String> it5 = region.iterator();
													while(it5.hasNext()) {
														String title5 = it5.next();
														logBuf.append("开始导入区[" + title5 + "]...<br/>");
														logger.info("开始导入区[" + title5 + "]...");
														AreaVO vo5 = getAreaVO(funCode);
														vo5.setParent_id(vo4.getPk_area());
														vo5.setName(title5);
														vo5.setArea_level(6);
														areaVOs.add(vo5);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			for(AreaVO areaVO : areaVOs) {
				areaVO.setName(areaVO.getName().substring(0, areaVO.getName().length() - 2));
				NWDao.getInstance().saveOrUpdate(areaVO);
			}
			return aggVOs;
		} finally {
			try {
				input.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private AreaVO getAreaVO(String funCode) {
		AreaVO vo1 = new AreaVO();
		vo1.setCode(CodenoHelper.generateCode(funCode));
		vo1.setLocked_flag(UFBoolean.FALSE);
		vo1.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		vo1.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(vo1);
		return vo1;
	}
}
