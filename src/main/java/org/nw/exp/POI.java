package org.nw.exp;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.nw.basic.util.DateUtils;
import org.nw.jf.group.GroupVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 比较简单的导出Excel
 * 
 * @author xuqc
 * @date 2011-7-18 调用poi.jar 生成导出Excel文件对象
 */

public class POI {
	// 定制浮点数格式
	private static String NUMBER_FORMAT = "#,##0.00";
	private boolean formatNumber = false;// 是否格式化数字
	// 定制日期格式
	private static String DATE_FORMAT = "yyyy/MM/dd";
	// 宽度,使用自适应宽度
	// private int width = 5000;
	// 输出流
	private OutputStream os = null;
	// 工作簿
	private HSSFWorkbook workbook = null;
	// 表
	private HSSFSheet sheet = null;
	// 行
	private HSSFRow row = null;

	// 第一个工作簿的名称
	private String firstSheetName;

	// 标题栏的统一格式
	private HSSFCellStyle headerStyle;

	// 正文的样式
	private HSSFCellStyle intStyle;
	private HSSFCellStyle doubleStyle;
	private HSSFCellStyle stringStyle;
	private HSSFCellStyle dateStyle;

	private boolean groupflag;// 是否使用分组表头

	public POI() {
		this.workbook = new HSSFWorkbook();
	}

	/**
	 * 初始化Excel
	 */
	public POI(OutputStream os) {
		this.os = os;
		this.workbook = new HSSFWorkbook();
	}

	// 设置边框
	private void setCellBorder(HSSFCellStyle style) {
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	}

	private HSSFCellStyle getHeaderStyle() {
		if(headerStyle == null) {
			// 设置默认字体
			HSSFFont font = workbook.createFont();//
			font.setFontHeightInPoints((short) 12);
			font.setFontName("宋体 ");
			font.setBoldweight((short) 1000);
			headerStyle = workbook.createCellStyle();
			headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
			headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
			headerStyle.setFont(font);
			headerStyle.setFillForegroundColor(new HSSFColor.TURQUOISE().getIndex());
			headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			setCellBorder(headerStyle);
		}
		return headerStyle;
	}

	private HSSFCellStyle getContentStyle() {
		// 设置默认字体
		HSSFFont font = workbook.createFont();//
		font.setFontHeightInPoints((short) 10);
		font.setFontName("宋体 ");
		HSSFCellStyle contentStyle = workbook.createCellStyle();
		contentStyle.setFont(font);
		setCellBorder(contentStyle);
		return contentStyle;
	}

	/**
	 * 生成excel文件(文件标题栏与文件内容一定要对应)
	 * 
	 * @param os
	 * @param titleAry
	 *            (excel文件标题栏)
	 * @param dataList
	 *            (excel文件内容),list中还是list对象，每个list是一行记录
	 * @throws IOException
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	@SuppressWarnings("rawtypes")
	public void createExcel(String[] titleAry, List dataList) throws IOException {
		buildExcel(titleAry, dataList);
		if(this.os != null) {
			write();
		}
	}

	/**
	 * 导出，titleAry定义导出的标题信息，dataList对应的是标题下的字段内容，这里没有多余的内容
	 * 
	 * @param titleAry
	 * @param dataList
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public HSSFWorkbook buildExcel(String[] titleAry, List dataList) {
		return buildExcel(titleAry, dataList, null);
	}

	/**
	 * 导出指定的字段，这里的dataList是一个较大的集合，fieldAry定义了哪些字段要导出，而titleAry定义的是导出字段的title信息
	 * 
	 * @param fieldAry
	 *            指定要导出的字段
	 * @param titleAry
	 * @param dataList
	 * @return
	 */
	public HSSFWorkbook buildExcel(String[] fieldAry, String[] titleAry, List<Map<String, Object>> dataList) {
		if(firstSheetName != null) {
			this.sheet = workbook.createSheet(firstSheetName);
		} else {
			this.sheet = workbook.createSheet();
		}
		// 写标题行
		createRow(0);
		buildHeader(titleAry);
		if(dataList != null) {
			// 写数据行
			for(int row = 0; row < dataList.size(); row++) {
				Map<String, Object> oneRowMap = (Map<String, Object>) dataList.get(row);
				createRow(row + 1);
				buildContent(fieldAry, oneRowMap, row);
			}
		}
		// 设置自适应宽度
		for(int i = 0; i < titleAry.length; i++) {
			sheet.autoSizeColumn(i, true);
		}
		return workbook;
	}

	/**
	 * 导出，titleAry定义导出的标题信息，dataList对应的是标题下的字段内容，这里没有多余的内容 groupVOs定义了导出的多表头定义信息
	 * 因为目前只支持两级的多表头，所以这里只取groupVos的第一个list即可
	 * 
	 * @param titleAry
	 * @param dataList
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public HSSFWorkbook buildExcel(String[] titleAry, List dataList, List<List<GroupVO>> groupVOs) {
		if(firstSheetName != null) {
			this.sheet = workbook.createSheet(firstSheetName);
		} else {
			this.sheet = workbook.createSheet();
		}
		// 写标题行
		if(groupVOs != null && groupVOs.size() > 0) {
			List<GroupVO> groupAry = groupVOs.get(0);
			if(groupAry != null && groupAry.size() > 0) {
				groupflag = true;
			}
		}
		if(groupflag) {
			buildHeader(titleAry, groupVOs.get(0));
		} else {
			buildHeader(titleAry);
		}

		if(dataList != null) {
			// 写数据行
			for(int row = 0; row < dataList.size(); row++) {
				List oneRowList = (List) dataList.get(row);
				createRow(groupflag ? (row + 2) : (row + 1));
				buildContent(oneRowList, row);
			}
		}
		for(int i = 0; i < titleAry.length; i++) {
			sheet.autoSizeColumn(i, true);
		}
		return workbook;
	}

	/**
	 * 导出Excel文件
	 * 
	 * @throws IOException
	 */
	private void write() throws IOException {
		workbook.write(os);
		os.flush();
		os.close();
	}

	/**
	 * 增加一行
	 * 
	 * @param rowNum
	 *            行号
	 */
	private void createRow(int rowNum) {
		row = sheet.createRow(rowNum);
		row.setHeightInPoints(20);// 默认行高25px
	}

	/**
	 * 开始写入第一行(即标题栏)
	 * 
	 * @param title
	 */
	private void buildHeader(String[] title) {
		HSSFRow row = sheet.createRow(0);
		row.setHeightInPoints((short) 30);
		for(int j = 0; j < title.length; j++) {
			HSSFCell cell = row.createCell(j);
			cell.setCellStyle(getHeaderStyle());
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(new HSSFRichTextString(title[j]));
		}
	}

	private void buildHeader(String[] title, List<GroupVO> groupVOs) {
		if(groupflag) {
			for(int j = 0; j < 2; j++) {
				HSSFRow row = sheet.createRow(j);
				row.setHeightInPoints((short) 22);// 合并标题栏的高度小点
				for(int i = 0; i < title.length; i++) {
					HSSFCell cell = row.createCell(i);
					cell.setCellStyle(getHeaderStyle());
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					if(j == 1) {
						cell.setCellValue(new HSSFRichTextString(title[i]));
					}
				}
			}
			int index = 0;// 列
			for(int i = 0; i < groupVOs.size(); i++) {
				GroupVO groupVO = groupVOs.get(i);
				if(groupVO.getColspan() != null) {
					// 定位到当前单元格
					HSSFRow firstRow = sheet.getRow(0);// 第一行
					// 列合并
					sheet.addMergedRegion(new CellRangeAddress(0, 0, index, (index + groupVO.getColspan() - 1)));
					HSSFCell cell = firstRow.getCell(index);
					cell.setCellValue(groupVO.getHeader() == null ? "" : groupVO.getHeader());
					index += groupVO.getColspan();
				} else {
					// 行合并
					HSSFRow secondRow = sheet.getRow(1);// 第二行
					HSSFCell cell = secondRow.getCell(index);
					String value = cell.getStringCellValue();
					sheet.addMergedRegion(new CellRangeAddress(0, 1, index, index));
					// 行合并后,此时从第二行变成第一行了
					HSSFRow firstRow = sheet.getRow(0);// 第一行
					cell = firstRow.getCell(index);
					cell.setCellValue(value);
					index++;
				}
			}
		} else {
			HSSFRow row = sheet.createRow(0);
			row.setHeightInPoints((short) (30));
			for(int j = 0; j < title.length; j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellStyle(getHeaderStyle());
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(title[j]));
			}
		}
	}

	/**
	 * 写入行数据
	 * 
	 * @param list
	 * @param row
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	@SuppressWarnings("rawtypes")
	private void buildContent(List list, int row) {
		// 数据是文本时(用label写入到工作表中)
		for(int col = 0; list != null && col < list.size(); col++) {
			// UF数据类型都以String类型存储
			Object obj = list.get(col);
			if(obj == null) {
				obj = "";// 如果是null，那么按照空串处理，因为需要定义到每个单元格的颜色属性等等
			}
			if(obj instanceof UFDate || obj instanceof UFDouble || obj instanceof UFBoolean
					|| obj instanceof UFDateTime) {
				setCell(col, obj.toString());
			} else if(obj instanceof UFDouble) {
				setCell(col, ((UFDouble) obj).doubleValue());
			} else if(obj instanceof Long) {
				setCell(col, (Long) obj);
			} else if(obj instanceof Integer) {
				setCell(col, (Integer) obj);
			} else if(obj instanceof Double || obj instanceof Float) {
				setCell(col, (Double) obj);
			} else if(obj instanceof BigDecimal) {
				BigDecimal bd = (BigDecimal) obj;
				setCell(col, bd.doubleValue());
			} else {
				setCell(col, obj.toString());
			}
		}
	}

	/**
	 * 写入行数据
	 * 
	 * @param list
	 * @param row
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void buildContent(String[] fieldAry, Map<String, Object> rowMap, int row) {
		// 数据是文本时(用label写入到工作表中)
		for(int col = 0; col < fieldAry.length; col++) {
			// UF数据类型都以String类型存储
			Object obj = rowMap.get(fieldAry[col]);
			if(obj == null) {
				obj = "";// 如果是null，那么按照空串处理，因为需要定义到每个单元格的颜色属性等等
			}
			if(obj instanceof UFDate || obj instanceof UFDouble || obj instanceof UFBoolean
					|| obj instanceof UFDateTime) {
				setCell(col, obj.toString());
			} else if(obj instanceof UFDouble) {
				setCell(col, ((UFDouble) obj).doubleValue());
			} else if(obj instanceof Long) {
				setCell(col, (Long) obj);
			} else if(obj instanceof Integer) {
				setCell(col, (Integer) obj);
			} else if(obj instanceof Double || obj instanceof Float) {
				setCell(col, (Double) obj);
			} else if(obj instanceof BigDecimal) {
				BigDecimal bd = (BigDecimal) obj;
				setCell(col, bd.doubleValue());
			} else if(obj instanceof UFDate) {
				setCell(col, (UFDate) obj);
			} else if(obj instanceof UFDateTime) {
				setCell(col, (UFDateTime) obj);
			} else {
				setCell(col, obj.toString());
			}
		}
	}

	/**
	 * 设置单元格
	 * 
	 * @param col
	 *            列号
	 * @param value
	 *            单元格填充值int型
	 */
	private void setCell(int col, Integer value) {
		if(intStyle == null) {
			intStyle = getContentStyle();
			intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		}
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);// 设置数字格式
		cell.setCellValue(value);
		cell.setCellStyle(intStyle);
	}

	/**
	 * 设置单元格
	 * 
	 * @param col
	 *            列号
	 * @param value
	 *            单元格填充值double型
	 */
	private void setCell(int col, double value) {
		if(doubleStyle == null) {
			doubleStyle = getContentStyle();
			doubleStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			if(formatNumber) {
				HSSFDataFormat format = workbook.createDataFormat();
				doubleStyle.setDataFormat(format.getFormat(NUMBER_FORMAT)); //
			}
		}
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(value);
		// 设置cell样式为定制的浮点数格式
		cell.setCellStyle(doubleStyle); // 设置该cell浮点数的显示格式

	}

	/**
	 * 设置单元格
	 * 
	 * @param col
	 *            列号
	 * @param value
	 *            单元格填充值String型
	 */
	private void setCell(int col, String value) {
		if(stringStyle == null) {
			stringStyle = getContentStyle();
			stringStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);// 水平居中
		}
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(value));
		cell.setCellStyle(stringStyle);
	}

	private void setCell(int col, UFDate value) {
		if(dateStyle == null) {
			dateStyle = getContentStyle();
			dateStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);// 水平居中
		}
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(DateUtils.formatDate(value.toDate(), DateUtils.DATEFORMAT_SLASH));
		cell.setCellStyle(stringStyle);
	}

	private void setCell(int col, UFDateTime value) {
		if(dateStyle == null) {
			dateStyle = getContentStyle();
			dateStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);// 水平居中
		}
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(DateUtils.formatDate(value.toString(), DateUtils.DATETIME_FORMAT_SLASH));
		cell.setCellStyle(stringStyle);
	}

	public void setFirstSheetName(String firstSheetName) {
		this.firstSheetName = firstSheetName;
	}

	public void setTitleStyle(HSSFCellStyle titleStyle) {
		this.headerStyle = titleStyle;
	}

	public static String getStringCellValue(Cell cell) {// 获取单元格数据内容为字符串类型的数据
		if(cell == null) {
			return "";
		}
		String strCell = "";
		switch(cell.getCellType()){
		case Cell.CELL_TYPE_STRING:
			strCell = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			strCell = String.valueOf(cell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			strCell = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_BLANK:
			strCell = "";
			break;
		default:
			strCell = "";
			break;
		}
		return strCell;
	}

	public boolean isFormatNumber() {
		return formatNumber;
	}

	public void setFormatNumber(boolean formatNumber) {
		this.formatNumber = formatNumber;
	}
}