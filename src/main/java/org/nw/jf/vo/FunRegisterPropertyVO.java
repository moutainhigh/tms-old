package org.nw.jf.vo;

import java.io.Serializable;
import java.util.List;

import org.nw.vo.sys.FunVO;

/**
 * 在NC的功能注册中定义的与生成单据相关的配置项<br/>
 * 目前包括：表头高度，禁用按钮<br/>
 * 在模板VO（UiBillTempletVO）会存在该引用
 * 
 * @author xuqc
 * @date 2012-3-7
 */
public class FunRegisterPropertyVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1267858922647533740L;

	/**
	 * 表头的高度配置，值是一个整数
	 */
	public static final String _HEADERHEIGHT = "_headerHeight";
	/**
	 * 表头是否可以拉伸参数,值是true/false
	 */
	public static final String _HEADERSPLIT = "_headerSplit";
	/**
	 * 是否使用瀑布流的展现方式，值是true/false
	 */
	public static final String _WATERFALLSCENE = "_waterfallScene";
	// 表体是否使用流式布局
	public static final String _BODYWATERFALLSCENE = "_bodyWaterfallScene";
	private Integer headerHeight; // 表头高度
	private Boolean headerSplit; // 表头是否可拉伸
	private Boolean waterfallScene;// 是否使用瀑布流的展现方式
	private List<FunVO> btnArray;// 功能注册中注册的按钮
	// 表体是否使用流式布局，waterfallScene的布局是把表体作为一个整体放到表头，bodyWaterfallScene是把表体的每个表格都当中一个个体进行流式布局
	private Boolean bodyWaterfallScene;
	private boolean simpleUnConfirm;//是否是简单反确认

	public Boolean getBodyWaterfallScene() {
		return bodyWaterfallScene;
	}

	public void setBodyWaterfallScene(Boolean bodyWaterfallScene) {
		this.bodyWaterfallScene = bodyWaterfallScene;
	}

	public List<FunVO> getBtnArray() {
		return btnArray;
	}

	public void setBtnArray(List<FunVO> btnArray) {
		this.btnArray = btnArray;
	}

	public Integer getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(Integer headerHeight) {
		this.headerHeight = headerHeight;
	}

	public Boolean getWaterfallScene() {
		return waterfallScene;
	}

	public void setWaterfallScene(Boolean waterfallScene) {
		this.waterfallScene = waterfallScene;
	}

	public Boolean getHeaderSplit() {
		return headerSplit;
	}

	public void setHeaderSplit(Boolean headerSplit) {
		this.headerSplit = headerSplit;
	}

	public boolean isSimpleUnConfirm() {
		return simpleUnConfirm;
	}

	public void setSimpleUnConfirm(boolean simpleUnConfirm) {
		this.simpleUnConfirm = simpleUnConfirm;
	}

}
