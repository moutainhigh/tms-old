package org.nw.redis;

public class RedisDao extends BaseRedisDao {
	
	public static RedisDao getInstance() {
		return new RedisDao();
	}

}
