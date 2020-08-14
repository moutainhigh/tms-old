Ext.ns('uft.jf');
/**
 * 对于同一个实体对象，由于每次点击新增按钮时返回的数据都是相同的，
 * 该类用来缓存该实体
 */
uft.jf.CacheMgr=function(){
	//private 存储所有实体按照actionUrl:values的格式存储
	var entitys = {};
	var timestamp = {}; //缓存的时间
	var expired = 600000; //10分钟
	/**
	 * 返回所有缓存对象
	 */
	this.getEntitys = function(){
		return entitys;
	};
	/**
	 * 根据key返回指定的缓存对象
	 */
	this.getEntity = function(key,ts){
		if(new Date().getTime()-timestamp[key] > expired){
			//缓存已经过期
			this.removeEntity(key); 
			return null;
		}
		var entity = entitys[key];
		if(entity && entity.HEADER){
			var _ts = entity.HEADER['ts'];
			if(_ts && ts && _ts != ts){
				//ts已经不一致,属于脏数据
				this.removeEntity(key); 
				return null;
			}
		}
		return entity;
	};
	/**
	 * 加入缓存对象
	 */
	this.addEntity = function(key, value){
		timestamp[key] = new Date().getTime();
		entitys[key] = value;
	};
	/**
	 * 从缓存中删除指定对象
	 */
	this.removeEntity = function(key){
		delete entitys[key];
		delete timestamp[key];
	}
};