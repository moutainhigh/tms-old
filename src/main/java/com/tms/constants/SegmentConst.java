package com.tms.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 分段的常量类
 * 
 * @author xuqc
 * @date 2012-8-29 下午05:38:40
 */
public class SegmentConst {

	// 运段类型
	public static final int SECTION = 0; // 分段运段
	public static final int QUANTITY = 1; // 分量运段

	// 运段标识，分段后，表示该运段是上级还是下级，或者还没有经过分段
	public static final int SEG_MARK_NORMAL = 0; // 没有经过分段的原始运段
	public static final int SEG_MARK_PARENT = 1; // 上级运段
	public static final int SEG_MARK_CHILD = 2; // 下级运段

	public static final int SEG_TYPE_THD = 0;// 提货段
	public static final int SEG_TYPE_GXD = 1;// 干线段
	public static final int SEG_TYPE_SHD = 2;// 收货段
	public static final int SEG_TYPE_FX = 3;// 返箱
	public static final int SEG_TYPE_NORMAL = 4;// 正常

	public static final int DELEGATE_STATUS_NEW = 0;// 待委派
	public static final int DELEGATE_STATUS_DO = 1;// 已委派

	public static Map<Integer, String> segtypeMap = new HashMap<Integer, String>();

	static {
		segtypeMap.put(SEG_TYPE_THD, "提货段");
		segtypeMap.put(SEG_TYPE_GXD, "干线段");
		segtypeMap.put(SEG_TYPE_SHD, "送货段");
		segtypeMap.put(SEG_TYPE_FX, "返箱");
		segtypeMap.put(SEG_TYPE_NORMAL, "正常");
	}
}
