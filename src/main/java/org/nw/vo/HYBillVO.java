package org.nw.vo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;

/**
 * 行业的公共单据VO。
 * 
 */
public class HYBillVO extends AggregatedValueObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 主表VO
	SuperVO m_headVo = null;

	// 子表VO
	SuperVO[] m_itemVos = null;

	// 消息提示
	private String m_hintMessage = null;

	// 是否发送工作流或消息
	private Boolean m_isSendMessage = new Boolean(false);

	// 判定是否进行单据锁定
	private boolean m_isBillLock = true;

	/**
	 * YcBillVO 构造子注解。
	 */
	public HYBillVO() {
		super();
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:36:56)
	 * 
	 * @return nc.vo.pub.ValueObject[]
	 */
	public org.nw.vo.pub.CircularlyAccessibleValueObject[] getChildrenVO() {
		return m_itemVos;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-7-3 8:47:41)
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getMessage() {
		return m_hintMessage;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:32:28)
	 * 
	 * @return nc.vo.pub.ValueObject
	 */
	public CircularlyAccessibleValueObject getParentVO() {
		return m_headVo;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-3-12 10:55:39)
	 * 
	 * @return boolean
	 */
	public boolean isBillLock() {
		return m_isBillLock;
	}

	/**
	 * 是否发送工作流。 默认不进行工作流发送 创建日期：(2003-7-6 12:30:36)
	 * 
	 * @return java.lang.Boolean
	 */
	public java.lang.Boolean isSendMessage() {
		return m_isSendMessage;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:36:56)
	 * 
	 * @return nc.vo.pub.ValueObject[]
	 */
	public void setChildrenVO(org.nw.vo.pub.CircularlyAccessibleValueObject[] children) {
		if(children == null) {
			m_itemVos = null;
		} else if(children.length == 0) {
			try {
				m_itemVos = (SuperVO[]) children;
			} catch(ClassCastException e) {
				m_itemVos = null;
			}
		} else {
			List l = Arrays.asList(children);
			m_itemVos = (SuperVO[]) l.toArray((Object[]) Array.newInstance(children[0].getClass(), 0));
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-3-12 10:55:39)
	 * 
	 * @param newIsBillLock
	 *            boolean
	 */
	public void setIsBillLock(boolean newIsBillLock) {
		m_isBillLock = newIsBillLock;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-7-4 14:37:37)
	 * 
	 * @param msg
	 *            java.lang.String
	 */
	public void setMessage(java.lang.String msg) {
		m_hintMessage = msg;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:32:28)
	 * 
	 * @return nc.vo.pub.ValueObject
	 */
	public void setParentVO(CircularlyAccessibleValueObject parent) {
		m_headVo = (SuperVO) parent;
	}

	/**
	 * 设置是否发送消息标志。 创建日期：(2003-7-6 15:04:37)
	 * 
	 * @param param
	 *            java.lang.Boolean
	 */
	public void setSendMessage(java.lang.Boolean param) {
		m_isSendMessage = param;
	}
}