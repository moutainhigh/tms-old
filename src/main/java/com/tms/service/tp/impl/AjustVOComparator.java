package com.tms.service.tp.impl;

import java.util.Comparator;

/**
 * 
 * @author xuqc
 * @date 2014-10-11 上午12:29:37
 */
public class AjustVOComparator implements Comparator<AjustVO> {
	public int compare(AjustVO vo1, AjustVO vo2) {
		return vo1.getAddr_flag().compareTo(vo2.getAddr_flag());
	}
}
