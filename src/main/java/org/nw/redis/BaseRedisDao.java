package org.nw.redis;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.service.IToftService;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.TempletHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DataTempletVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.index.PortletConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.tms.service.cm.impl.ContractUtils;
import com.tms.vo.base.AreaVO;
import com.tms.vo.cm.ContractBVO;

/**
 * redis 的使用类，主要用于缓存 模板之类的数据
 * @version 1.0.0
 * @author XIA
 * @date 2016 10 26
 */
public class BaseRedisDao {
	
	protected RedisTemplate<String, Object> redisTemplate = SpringContextHolder.getBean("redisTemplate");
	
	
	public String get(final String key){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
				byte[] keyBytes  = serializer.serialize(key); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null){
					return serializer.deserialize(valueBytes);
				}
				return "";
			}
		});
		
	}
	
	
	public FunVO getFunVO(final String where, final String key){
		return redisTemplate.execute(new RedisCallback<FunVO>() {
			public FunVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = key.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					FunVO funVO = JacksonUtils.readValue(new String(valueBytes), FunVO.class);
					return funVO;
				}else{
					FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, where + "=?", key);
					if(funVO != null){
						//放入缓存
						saveFunVO(where, key, funVO);
					}
					return funVO;
				}
			}
		});
	}
	
	public String saveFunVO(final String where,final String key,final FunVO funVO){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(funVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public FunVO getFunVOByFunCode(final String fun_code){
		return redisTemplate.execute(new RedisCallback<FunVO>() {
			public FunVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = fun_code.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					FunVO funVO = JacksonUtils.readValue(new String(valueBytes), FunVO.class);
					return funVO;
				}else{
					String strWhere = "isnull(dr,0)=0 and fun_code=?";
					FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, strWhere, fun_code);
					//放入缓存
					saveFunVOByFunCode(fun_code, funVO);
					return funVO;
				}
			}
		});
	}
	
	public String saveFunVOByFunCode(final String fun_code,final FunVO funVO){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = fun_code.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(funVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	
	public String getTempletID(final String funCode, final String nodeKey, final int tempstyle){
		String pk_user = WebUtils.getLoginInfo() == null ? "0001" :  WebUtils.getLoginInfo().getPk_user();
		final String key = funCode + ":" + nodeKey + ":" + tempstyle + ":" + pk_user;
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  =key.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					return new String(valueBytes);
				}else{
					String templetID = TempletHelper.getTempletID(funCode, nodeKey, tempstyle);
					if(StringUtils.isNotBlank(templetID)){
						//放入缓存
						saveTempletID(key, templetID);
					}
					return templetID;
				}
			}
		});
	}
	
	public String saveTempletID(final String key,final String value){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				byte[] valueBytes = value.getBytes();
				arg0.set(keyBytes,  valueBytes);
				return "";
			}
		});
	}
	
	
	public UiBillTempletVO getBillTempletVO(final String templateID,final IToftService service){
		return redisTemplate.execute(new RedisCallback<UiBillTempletVO>() {
			public UiBillTempletVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = templateID.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					UiBillTempletVO templetVO = JacksonUtils.readValue(new String(valueBytes), UiBillTempletVO.class);
					return templetVO;
				}else{
					UiBillTempletVO templetVO = service.getBillTempletVO(templateID);
					if(templetVO != null){
						saveBillTempletVO(templateID, templetVO);
					}
					return templetVO;
				}
			}
		});
	}
	
	public String saveBillTempletVO(final String templateID,final UiBillTempletVO templetVO){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = templateID.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(templetVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}

	public UiBillTempletVO getOriginalBillTempletVO(final String templateID){
		return redisTemplate.execute(new RedisCallback<UiBillTempletVO>() {
			public UiBillTempletVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = (templateID+":original").getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					UiBillTempletVO templetVO = JacksonUtils.readValue(new String(valueBytes), UiBillTempletVO.class);
					return templetVO;
				}else{
					UiBillTempletVO templetVO = TempletHelper.getOriginalBillTempletVO(templateID);
					if(templetVO != null){
						saveOriginalBillTempletVO(templateID+":original", templetVO);
					}
					return templetVO;
				}
			}
		});
	}
	
	public String saveOriginalBillTempletVO(final String templateID,final UiBillTempletVO templetVO){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = templateID.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(templetVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public DataTempletVO getDataTempletVO(final String templateID){
		return redisTemplate.execute(new RedisCallback<DataTempletVO>() {
			public DataTempletVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = templateID.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					DataTempletVO templetVO = JacksonUtils.readValue(new String(valueBytes), DataTempletVO.class);
					return templetVO;
				}else{
					DataTempletVO templetVO = TempletHelper.getDataTempletVO(templateID);
					if(templetVO != null){
						saveDataTempletVO(templateID, templetVO);
					}
					return templetVO;
				}
			}
		});
	}
	
	public String saveDataTempletVO(final String templateID,final DataTempletVO templetVO){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = templateID.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(templetVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String[]> getSelectValues(final String reftype){
		//获取当前公司
		final String pk_corp = WebUtils.getLoginInfo() == null ? Constants.SYSTEM_CODE : WebUtils.getLoginInfo().getPk_corp();
		return redisTemplate.execute(new RedisCallback<List<String[]>>() {
			public List<String[]> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = (pk_corp + ":" + reftype).getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					List<String[]> result = new ArrayList<String[]>();
					List<String[]> list = JacksonUtils.readValue(new String(valueBytes), List.class);
					if(list != null && list.size() > 0){
						for(Object unit : list){
							String[] temp = (String[]) ((List)unit).toArray(new String[((List)unit).size()]);
							result.add(temp);
						}
					}
					return result;
				}else{
					List<String[]> list = UiTempletUtils.getSelectValues(reftype);
					if(list != null){
						saveSelectValues(reftype, list);
					}
					return list;
				}
			}
		});
	}
	
	public String saveSelectValues(final String reftype, final List<String[]> list){
		//获取当前公司
		final String pk_corp = WebUtils.getLoginInfo() == null ? Constants.SYSTEM_CODE : WebUtils.getLoginInfo().getPk_corp();
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = (pk_corp + ":" + reftype).getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(list).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public List<FunVO> getBtnRegisterAry(final ParamVO paramVO,final String pk_user){
		return redisTemplate.execute(new RedisCallback<List<FunVO>>() {
			public List<FunVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = (paramVO.getFunCode() + ":" + pk_user).getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					JsonNode nodes = JacksonUtils.readTree(new String(valueBytes));
					List<FunVO> funVOs = new ArrayList<FunVO>();
					for(JsonNode node : nodes){
						funVOs.add(JacksonUtils.readValue(node, FunVO.class));
					}
					return funVOs;
				}else{
					List<FunVO> funVOs = ServiceHelper.getBtnRegisterAry(paramVO);
					if(funVOs != null){
						saveBtnRegisterAry(paramVO.getFunCode(), pk_user,funVOs);
					}
					return funVOs;
				}
			}
		});
	}
	
	public String saveBtnRegisterAry(final String fun_code,final String pk_user,final List<FunVO> funVOs){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = (fun_code+":"+pk_user).getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(funVOs).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public List<CorpVO> getCurrentCorpVOsWithChildren(final String pk_corp){
		return redisTemplate.execute(new RedisCallback<List<CorpVO>>() {
			public List<CorpVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = pk_corp.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					List<CorpVO> corpVOs = new ArrayList<CorpVO>();
					JsonNode nodes = JacksonUtils.readTree(new String(valueBytes));
					for(JsonNode node : nodes){
						corpVOs.add(JacksonUtils.readValue(node, CorpVO.class));
					}
					return corpVOs;
				}else{
					List<CorpVO> corpVOs = CorpHelper.getCurrentCorpVOsWithChildren();
					if(corpVOs != null){
						saveCurrentCorpVOsWithChildren(pk_corp, corpVOs);
					}
					return corpVOs;
				}
			}
		});
	}
	
	public String saveCurrentCorpVOsWithChildren(final String pk_corp, final List<CorpVO> corpVOs){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = pk_corp.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(corpVOs).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public List<FunVO> getFunVOForPermission(final String funCode,final String servletPath){
		final String key = funCode +":"+ servletPath;
		return redisTemplate.execute(new RedisCallback<List<FunVO>>() {
			public List<FunVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes  = key.getBytes(); 
				byte[] valueBytes  = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					List<FunVO> funVOs = new ArrayList<FunVO>();
					JsonNode nodes = JacksonUtils.readTree(new String(valueBytes));
					for(JsonNode node : nodes){
						funVOs.add(JacksonUtils.readValue(node, FunVO.class));
					}
					return funVOs;
				}else{
					String baseSql = "select pk_fun,fun_code,fun_name,class_name,help_name,fun_property,parent_id,isbuttonpower from nw_fun WITH(NOLOCK) "
							+ " where isnull(dr,0)=0 "; // 这里的查询应该包括锁定的资源
					String sql = baseSql;
					if(StringUtils.isNotBlank(funCode)) {
						sql += " and fun_code='" + funCode + "'";
					}
					sql += " and class_name like '" + servletPath + "%'";
					List<FunVO> funVOs = NWDao.getInstance().queryForListWithCache(sql, FunVO.class);
					if(funVOs == null || funVOs.size() == 0) {
						sql = baseSql + " and class_name like '" + servletPath + "%'";
						funVOs = NWDao.getInstance().queryForListWithCache(sql, FunVO.class);
					}
					if(funVOs != null && funVOs.size() > 0){
						saveFunVOForPermission(funCode, servletPath, funVOs);
					}
					return funVOs;
				}
			}
		});
	}
	
	public String saveFunVOForPermission(String funCode,String servletPath, final List<FunVO> funVOs){
		final String key = funCode +":"+ servletPath;
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				byte[] valueBytes = JacksonUtils.writeValueAsString(funVOs).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public boolean getIsPowerByUserFun(final String pk_user,final String pk_fun){
		final String key = pk_user +":"+ pk_fun;
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				byte[] valueBytes = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					String value = new String(valueBytes);
					if(value.equals("true")){
						return true;
					}else{
						return false;
					}
				}else{
					String sql = "select 1 from nw_power_fun "
							+ " WITH(NOLOCK) where pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0) "
							+ "and pk_fun=? and isnull(dr,0)=0";
					List<String> funAry = NWDao.getInstance().queryForList(sql, String.class, pk_user, pk_fun);
					if(funAry != null && funAry.size() > 0) {
						saveIsPowerByUserFun(pk_user, pk_fun, "true");
						return true;
					}else{
						saveIsPowerByUserFun(pk_user, pk_fun, "false");
						return false;
					}
				}
			}
		});
	}
	
	
	
	public String saveIsPowerByUserFun(String pk_user,String pk_fun,final String is){
		final String key = pk_user +":"+ pk_fun;
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				byte[] valueBytes = is.getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	public Map<String,String> getAllUserPortletAndCountSql(){
		return redisTemplate.execute(new RedisCallback<Map<String,String>>() {
			public Map<String,String> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = "userPortletAndCountSql".getBytes(); 
				Map<byte[],byte[]> valueBytes = arg0.hGetAll(keyBytes);
				if(valueBytes == null || valueBytes.size() == 0){
					return null;
				}
				Map<String,String> userPortletAndCountSql = new HashMap<String, String>();
				for(byte[] hmKeys : valueBytes.keySet()){
					String key = new String(hmKeys);
					String value = new String(valueBytes.get(hmKeys));
					userPortletAndCountSql.put(key, value);
				}
				return userPortletAndCountSql;
			}
		});
	}
	
	public String getUserPortletAndCountSql(final String key){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = "userPortletAndCountSql".getBytes(); 
				if (arg0.exists(keyBytes)){
					
				}
				List<byte[]> valueBytes = arg0.hMGet(keyBytes,
						redisTemplate.getStringSerializer().serialize(key));
				if(valueBytes == null || valueBytes.size() == 0 || valueBytes.get(0) == null){
					return null;
				}
				return new String(valueBytes.get(0));
			}
		});
	}
	
	public String saveUserPortletAndCountSql(Map<String,String> userPortletAndCountSql){
		if(userPortletAndCountSql == null || userPortletAndCountSql.size() == 0){
			return null;
		}
		final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
		for(String key : userPortletAndCountSql.keySet()){
			hmMap.put(key.getBytes(),  userPortletAndCountSql.get(key).getBytes());
		}
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = "userPortletAndCountSql".getBytes(); 
				arg0.hMSet(keyBytes, hmMap);
				return "";
			}
		});
	}
	
	public Map<String,Integer> getUserPortletAndTime(){
		return redisTemplate.execute(new RedisCallback<Map<String,Integer>>() {
			public Map<String,Integer> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = "userPortletAndTime".getBytes(); 
				Map<byte[],byte[]> valueBytes = arg0.hGetAll(keyBytes);
				if(valueBytes == null || valueBytes.size() == 0){
					return null;
				}
				Map<String,Integer> userPortletAndCountSql = new HashMap<String, Integer>();
				for(byte[] hmKeys : valueBytes.keySet()){
					String key = new String(hmKeys);
					int value = Integer.parseInt(new String(valueBytes.get(hmKeys)));
					userPortletAndCountSql.put(key, value);
				}
				return userPortletAndCountSql;
			}
		});
	}
	
	public String saveUserPortletAndTime(Map<String,Integer> userPortletAndTime){
		if(userPortletAndTime == null || userPortletAndTime.size() == 0){
			return null;
		}
		final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
		for(String key : userPortletAndTime.keySet()){
			hmMap.put(key.getBytes(),userPortletAndTime.get(key).toString().getBytes());
		}
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = "userPortletAndTime".getBytes(); 
				arg0.hMSet(keyBytes, hmMap);
				return "";
			}
		});
	}
	
	
	public PortletConfigVO getPortlet(final String pk_portlet){
		return redisTemplate.execute(new RedisCallback<PortletConfigVO>() {
			public PortletConfigVO doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = pk_portlet.getBytes(); 
				byte[] valueBytes = arg0.get(keyBytes);
				if(valueBytes != null && valueBytes.length > 0){
					PortletConfigVO portletConfigVO = JacksonUtils.readValue(new String(valueBytes), PortletConfigVO.class);
					return portletConfigVO;
				}
				return null;
			}
		});
	}
	
	public String savePortlet(final PortletConfigVO portletConfigVO){
		if(portletConfigVO == null){
			return null;
		}
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = portletConfigVO.getPk_portlet().getBytes();
				byte[] valueBytes = JacksonUtils.writeValueAsString(portletConfigVO).getBytes();
				arg0.set(keyBytes, valueBytes);
				return "";
			}
		});
	}
	
	
	public Map<String,String> getUserPortletInfo(final String pk_user){
		final String key = pk_user + ":portletInfo";
		return redisTemplate.execute(new RedisCallback<Map<String,String>>() {
			public Map<String,String> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					Map<byte[],byte[]> valueBytes = arg0.hGetAll(keyBytes);
					if(valueBytes == null || valueBytes.size() == 0){
						return null;
					}
					Map<String,String> userPortletInfo = new HashMap<String, String>();
					for(byte[] hmKeys : valueBytes.keySet()){
						String key = new String(hmKeys);
						String value = new String(valueBytes.get(hmKeys));
						userPortletInfo.put(key, value);
					}
					return userPortletInfo;
				}
				return null;
			}
		});
	}
	
	public String saveUserPortletInfo(String pk_user,final String pk_portlet,final String portlet_info){
		final String key = pk_user + ":portletInfo";
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
				hmMap.put(pk_portlet.getBytes(),  portlet_info.getBytes());
				arg0.hMSet(key.getBytes(), hmMap);
				return "";
			}
		});
	}
	
	
	public void saveContractHeads(final Map<String,List<String>> headMap){
		final String key = "contractHeads";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
				for(Entry<String, List<String>> contract : headMap.entrySet()){
					String headKey = contract.getKey();
					List<String> pk_contracts  = contract.getValue();
					hmMap.put(headKey.getBytes(), JacksonUtils.writeValueAsString(pk_contracts).getBytes());
				}
				arg0.hMSet(key.getBytes(), hmMap);
				return null;
			}
		});
	}
	
	public void addContractHeads(final Map<String,List<String>> headMap){
		final String key = "contractHeads";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
				for(Entry<String, List<String>> contract : headMap.entrySet()){
					String headKey = contract.getKey();
					List<String> pk_contracts  = contract.getValue();
					hmMap.put(headKey.getBytes(), JacksonUtils.writeValueAsString(pk_contracts).getBytes());
				}
				arg0.hMSet(key.getBytes(), hmMap);
				return null;
			}
		});
	}
	
	public void removeContractHeads(final List<String> headkeys){
		final String key = "contractHeads";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if(arg0.exists(keyBytes)){
					for(String headkey : headkeys){
						arg0.hDel(keyBytes, headkey.getBytes());
					}
				}
				return null;
			}
		});
	}
	
	public List<String> getContractHeads(final String pk_carrierOrBala_customer,final String pk_trans_type){
		final String key = "contractHeads";
		return redisTemplate.execute(new RedisCallback<List<String>>() {
			public List<String> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					return getContractHeadsInRedis(arg0, keyBytes, (pk_carrierOrBala_customer + "," + pk_trans_type).getBytes());
				}else{
					ContractUtils.preLoad();
					return getContractHeadsInRedis(arg0, keyBytes, (pk_carrierOrBala_customer + "," + pk_trans_type).getBytes());
				}
			}
		});
	}
	
	public List<String> getContractHeadsInRedis(RedisConnection arg0,byte[] key1,byte[] key2){
		if (arg0.exists(key1)){
			byte[] valueBytes =  arg0.hGet(key1, key2);
			if(valueBytes == null || valueBytes.length == 0){
				return null;
			}
			String value = new String(valueBytes);
			JsonNode contractHeads = JacksonUtils.readTree(value);
			List<String> contracts = new ArrayList<String>();
			for(JsonNode contractHead : contractHeads){
				String contract = JacksonUtils.readValue(contractHead, String.class);
				if(!contracts.contains(contract)){
					contracts.add(contract);
				}
			}
			return contracts;
		}
		return null;
	}
	
	public Map<String,List<String>> getAllContractHeads(){
		final String key = "contractHeads";
		return redisTemplate.execute(new RedisCallback<Map<String,List<String>>>() {
			public Map<String,List<String>> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					Map<byte[], byte[]> valueBytes =  arg0.hGetAll(keyBytes);
					if(valueBytes == null || valueBytes.size()== 0){
						return null;
					}
					Map<String,List<String>> headMap = new HashMap<String, List<String>>();
					for(Entry<byte[], byte[]> valueEntry : valueBytes.entrySet()){
						String keyValue = new String(valueEntry.getKey());
						String value = new String(valueEntry.getValue());
						JsonNode contractHeads = JacksonUtils.readTree(value);
						List<String> contracts = new ArrayList<String>();
						for(JsonNode contractHead : contractHeads){
							String contract = JacksonUtils.readValue(contractHead, String.class);
							contracts.add(contract);
						}
						headMap.put(keyValue, contracts);
					}
					return headMap;
				}
				return null;
			}
		});
	}
	
	public void saveContractChilds(final Map<String,List<ContractBVO>> childMap){
		final String key = "contractChilds";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				final Map<byte[],byte[]> hmMap = new HashMap<byte[], byte[]>();
				for(Entry<String, List<ContractBVO>> contractBVO : childMap.entrySet()){
					String childKey = contractBVO.getKey();
					List<ContractBVO> contractBVOs = contractBVO.getValue();
					hmMap.put(childKey.getBytes(), JacksonUtils.writeValueAsString(contractBVOs).getBytes());
				}
				arg0.hMSet(key.getBytes(), hmMap);
				return null;
			}
		});
	}
	
	public List<ContractBVO> getContractChilds(final String childKey){
		final String key = "contractChilds";
		return redisTemplate.execute(new RedisCallback<List<ContractBVO>>() {
			public List<ContractBVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					byte[] valueBytes =  arg0.hGet(keyBytes, childKey.getBytes());
					if(valueBytes == null || valueBytes.length == 0){
						return new ArrayList<ContractBVO>();
					}
					String value = new String(valueBytes);
					JsonNode contractChilds = JacksonUtils.readTree(value);
					List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
					for(JsonNode contractChild : contractChilds){
						ContractBVO contractBVO = JacksonUtils.readValue(contractChild, ContractBVO.class);
						contractBVOs.add(contractBVO);
					}
					return contractBVOs;
				}
				return new ArrayList<ContractBVO>();
			}
		});
	}
	
	public void removeContractChilds(final Set<String> childKeys){
		final String key = "contractChilds";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if(arg0.exists(keyBytes)){
					for(String childKey : childKeys){
						arg0.hDel(keyBytes, childKey.getBytes());
					}
				}
				return null;
			}
		});
	}
	
	public void removeChildsByPkContract(final String pk_contract){
		final String key = "contractChilds";
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if(arg0.exists(keyBytes)){
					Set<byte[]> keys = arg0.hKeys(key.getBytes());
					if(keys != null && keys.size() > 0){
						for(byte[] temp : keys){
							String value = new String(temp);
							if(value.startsWith(pk_contract)){
								arg0.hDel(keyBytes, value.getBytes());
							}
						}
					}
				}
				return null;
			}
		});
	}
	
	public List<AreaVO> getCurrentAreaVOWithParents(final String pk_area) {
		final String key = "currentAreaVOWithParents:" + pk_area;
		return redisTemplate.execute(new RedisCallback<List<AreaVO>>() {
			public List<AreaVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					byte[] valueBytes = arg0.get(keyBytes);
					if(valueBytes == null || valueBytes.length == 0){
						new ArrayList<AreaVO>();
					}
					String value = new String(valueBytes);
					JsonNode areaVONodes = JacksonUtils.readTree(value);
					List<AreaVO> areaVOs = new ArrayList<AreaVO>();
					for(JsonNode areaVONode : areaVONodes){
						AreaVO areaVO = JacksonUtils.readValue(areaVONode, AreaVO.class);
						areaVOs.add(areaVO);
					}
					return areaVOs;
				}else{
					String sql = "WITH tab AS ("
							+ " SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area=? "
							+ " UNION ALL"
							+ " SELECT b.* "
							+ " FROM"
							+ " tab a,"
							+ " ts_area b WITH(NOLOCK)"
							+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
							+ ") SELECT * FROM tab WITH(NOLOCK)";
					List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area);
					if(result != null && result.size() > 0){
						arg0.set(keyBytes, JacksonUtils.writeValueAsString(result).getBytes());
					}
					return result;
				}
			}
		});
	}
	
	public List<AreaVO> getParentAreaVOs(final String pk_area) {
		final String key = "parentAreaVOs:" + pk_area;
		return redisTemplate.execute(new RedisCallback<List<AreaVO>>() {
			public List<AreaVO> doInRedis(RedisConnection arg0) throws DataAccessException {
				byte[] keyBytes = key.getBytes(); 
				if (arg0.exists(keyBytes)){
					byte[] valueBytes = arg0.get(keyBytes);
					if(valueBytes == null || valueBytes.length == 0){
						new ArrayList<AreaVO>();
					}
					String value = new String(valueBytes);
					JsonNode areaVONodes = JacksonUtils.readTree(value);
					List<AreaVO> areaVOs = new ArrayList<AreaVO>();
					for(JsonNode areaVONode : areaVONodes){
						AreaVO areaVO = JacksonUtils.readValue(areaVONode, AreaVO.class);
						areaVOs.add(areaVO);
					}
					return areaVOs;
				}else{
					String sql = "WITH tab AS ("
							+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area=? "
							+ " UNION ALL"
							+ " SELECT b.* "
							+ " FROM"
							+ " tab a,"
							+ " ts_area b WITH(NOLOCK)"
							+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
							+ ") SELECT * FROM tab WITH(NOLOCK) AND　pk_area　<>? ";
					List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area);
					if(result != null && result.size() > 0){
						arg0.set(keyBytes, JacksonUtils.writeValueAsString(result).getBytes());
					}
					return result;
				}
			}
		});
	}
	
	
	public String clearCache(){
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection arg0) throws DataAccessException {
				arg0.flushDb();
				return "";
			}
		});
	}

}
