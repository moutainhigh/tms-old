package org.nw.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.json.JacksonUtils;
import org.nw.utils.HttpUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.pub.lang.UFDateTime;


/**
 * 快递查询工具类
 * https://www.kuaidi100.com/openapi/api_post.shtml
 * 
 * @author XIA
 * @date 2016 7 14 
 */
public class ExpressUtils {

	static Logger logger = Logger.getLogger(ExpressUtils.class);
	
	public enum ExpressCooperationUnit {
		KUAIDI100("快递100"), KUAIDIWANG("快递网");
		private String value;

		private ExpressCooperationUnit(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}
	
	public enum ExpressCorp {
		SHENTONG("shentong"), //申通快递
		SHUNFENG("shunfeng"), //顺丰速运
		YUANTONG("yuantong"), //圆通速递
		YUNDA("yunda"), //韵达快运
		ZHONGTONG("zhongtong"), //中通快递
		HUITONGKUAIDI("huitongkuaidi"), //百世汇通快递
		JD("jd"), //京东快递
		NSF("nsf"), //新顺丰（NSF）
		YOUZHENGGUONEI("youzhengguonei"), //邮政国内
		YOUZHENGGUOJI("youzhengguoji"), //邮政国际
		YUANCHENGWULIU("yuanchengwuliu"), //远成物流
		ZHAIJISONG("zhaijisong"), //宅急送
		ZHONGYOUWULIU("zhongyouwuliu"), //中邮物流
		DEBANGWULIU("debangwuliu"), //德邦物流
		EMS("ems"), //EMS快递
		TNT("tnt"), //TNT快递
		TIANTIAN("tiantian"), //天天快递
		TIANDIHUAYU("tiandihuayu"), //天地华宇
		ZHONGTIANWANYUN("zhongtianwanyun"), //中天万运
		ZHONGTIEWULIU("zhongtiewuliu"), //中铁快运
		ZTKY("ztky"), //中铁物流
		JIAJIWULIU("jiajiwuliu"), //佳吉物流
		YUXINWULIU("yuxinwuliu"), //宇鑫物流
		YITONGDA("yitongda"), //易通达
		YOUBIJIA("youbijia"), //邮必佳
		SHENGHUIWULIU("shenghuiwuliu"), //盛辉物流
		ANNENGWULIU("annengwuliu"), //安能物流快递
		ANXL("anxl"), //安迅物流
		BAIFUDONGFANG("baifudongfang"), //百福东方物流
		BANGSONGWULIU("bangsongwuliu"), //邦送物流
		BYHT("byht"), //博源恒通
		IDADA("idada"), //百成大达物流
		CITY100("city100"), //城市100
		DATIANWULIU("datianwuliu"), //大田物流
		DECHUANGWULIU("dechuangwuliu"), //德创物流
		GSM("gsm"), //GSM快递
		GUOTONGKUAIDI("guotongkuaidi"), //国通快递
		HENGLUWULIU("hengluwuliu"), //恒路物流
		HLYEX("hlyex"), //好来运快递
		HAOSHENGWULIU("haoshengwuliu"), //昊盛物流
		HUTONGWULIU("hutongwuliu"), //户通物流
		HZPL("hzpl"), //华航快递
		JIAYIWULIU("jiayiwuliu"), //佳怡物流
		JIXIANDA("jixianda"), //急先达物流
		JIALIDATONG("jialidatong"), //嘉里大通
		JINDAWULIU("jindawuliu"), //金大物流
		KUAIYOUDA("kuaiyouda"), //快优达速递
		LONGBANGWULIU("longbangwuliu"), //龙邦速递
		LIANBANGKUAIDI("lianbangkuaidi"), //联邦快递
		MINGHANGKUAIDI("minghangkuaidi"), //民航快递
		MINBANGSUDI("minbangsudi"), //民邦速递
		PINGANDATENGFEI("pingandatengfei"), //平安达腾飞
		QUANFENGKUAIDI("quanfengkuaidi"), //平安达腾飞
		QUANYIKUAIDI("quanyikuaidi"), //全一快递
		QUANRITONGKUAIDI("quanritongkuaidi"), //全日通快递
		QUANCHENKUAIDI("quanchenkuaidi"), //全晨快递
		SEVENDAYS("sevendays"), //7天连锁物流
		RUFENGDA("rufengda"), //如风达快递
		SHENGFENGWULIU("shengfengwuliu"), //盛丰物流
		SUIJIAWULIU("suijiawuliu"), //穗佳物流
		SHIYUNKUAIDI("shiyunkuaidi"), //世运快递
		NNTENGDA("nntengda"), //腾达速递
		UPS("ups"), //UPS
		YOUSHUWULIU("youshuwuliu"), //UC优速快递
		UPSFREIGHT("upsfreight"), //UPS货运
		WANXIANGWULIU("wanxiangwuliu"), //万象物流
		XINBANGWULIU("xinbangwuliu"), //新邦物流
		XINFENGWULIU("xinfengwuliu"), //信丰物流
		NEWEGGOZZO("neweggozzo");//新蛋物流
		
		private String value;

		private ExpressCorp(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}
	
	
	/**
	 * 根据快递公司编码和快递单号，查询快递当前状态
	 * @author XIA
	 * @param com 快递公司编码
	 * @param nu 快递单号
	 */
	public static List<Map<String,String>> query(String com, String nu) {
		if(StringUtils.isBlank(com) || StringUtils.isBlank(nu)){
			logger.info("-----------查询快递时，快递公司和快递单号都不能为空！----------");
			return null;
		}
		//获取这个公司下所要使用的快递查询合作商
		String expressUnit = ParameterHelper.getExpressCooperationUnit();
		if(StringUtils.isBlank(expressUnit)){
			expressUnit = ExpressCooperationUnit.KUAIDIWANG.toString();//默认使用快递网查询
		}
		if(expressUnit.equals(ExpressCooperationUnit.KUAIDIWANG.toString())){
			//快递网
			return kuaiDiWangQuery(com, nu);
		}
		
		return null;
	}
	
	/**
	 * 快递网查询
	 * 根据快递公司编码和快递单号，查询快递当前状态
	 * @author XIA
	 * @param com 快递公司编码
	 * @param nu 快递单号
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,String>> kuaiDiWangQuery(String com, String nu) {
		if(StringUtils.isBlank(com) || StringUtils.isBlank(nu)){
			logger.info("-----------查询快递时，快递公司和快递单号都不能为空！----------");
			return null;
		}
		final String KUAIDIWANGKEY = "7ec02870634c79d4e95bab0b79507ce6";
		final String KUAIDIWANGURL= "http://highapi.kuaidi.com/openapi-querycountordernumber.html?id="+KUAIDIWANGKEY+"&show=0&muti=0&order=asc";
		String url = KUAIDIWANGURL + "&com="+ com + "&nu=" + nu;
		String responseText = null;
		try {
			responseText = HttpUtils.get(url);
			if(StringUtils.isBlank(responseText)) {
				logger.info("接口调用出错,url:" + url + ",没有返回值。");
				return null;
			}
		} catch (Exception e) {
			logger.info("接口调用出错,url:" + url + ",错误:"+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		//解析json数据
		Map<String,Object> result = JacksonUtils.readValue(responseText, Map.class);
		if(result == null || result.size() == 0){
			logger.info("接口调用出错,url:" + url + ",返回值无法解析。");
			return null;
		}
		boolean success = Boolean.parseBoolean(String.valueOf(result.get("success")));
		if(!success){//调用失败
			logger.info("接口调用失败,url:" + url + ",返回值:"+String.valueOf(result.get("reason")));
			return null;
		}
		List<Map<String,String>> retList = (List<Map<String, String>>) result.get("data");
		sortExpress(retList);
		return retList;
	}
	
	/**
	 * 根据快递时间正向排序
	 * 
	 * @author XIA
	 * 
	 */
	private static void sortExpress(List<Map<String, String>> retList) {
		if (retList == null || retList.size() == 0) {
			return ;
		}
		int len = retList.size();
		// 每次从后往前冒一个最小值，且每次能确定一个数在序列中的最终位置
		for (int i = 0; i < len - 1; i++) { // 比较n-1次
			boolean exchange = true; // 冒泡的改进，若在一趟中没有发生逆序，则该序列已有序
			for (int j = len - 1; j > i; j--) { // 每次从后边冒出一个最小值
				UFDateTime jTime = new UFDateTime(retList.get(j).get("time"));
				UFDateTime j1Time = new UFDateTime(retList.get(j - 1).get("time"));
				if (jTime.before(j1Time)) { // 发生逆序，则交换
					Map<String, String> temp = new HashMap<String, String>();
					temp = retList.get(j);
					retList.set(j, retList.get(j - 1));
					retList.set(j - 1, temp);
					exchange = false;
				}
			}
			if (exchange) {
				return;
			}
		}
		return;
	}

}
