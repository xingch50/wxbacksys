package com.lzs.utils;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
	private static final String IP = ProUtil.getValue("redis.ip", "redis.properties"); // ip
	private static final int PORT = Integer.valueOf(ProUtil.getValue("redis.port", "redis.properties")); // 端口
	private static final String AUTH = ProUtil.getValue("redis.auth", "redis.properties"); // 密码(原始默认是没有密码)
	private static int MAX_ACTIVE = Integer.valueOf(ProUtil.getValue("redis.pool.maxActive", "redis.properties")); // 最大连接数
	private static int MAX_IDLE = Integer.valueOf(ProUtil.getValue("redis.pool.maxIdle", "redis.properties")); // 设置最大空闲数
	private static int MAX_WAIT = Integer.valueOf(ProUtil.getValue("redis.pool.maxWait", "redis.properties")); // 最大连接时间
	private static int TIMEOUT = Integer.valueOf(ProUtil.getValue("redis.pool.timeout", "redis.properties")); // 超时时间
	private static boolean BORROW = true; // 在borrow一个事例时是否提前进行validate操作
	private static JedisPool pool = null;
	private static final Logger logger = Logger.getLogger(RedisUtils.class);

	/**
	 * 初始化线程池
	 */
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(MAX_ACTIVE);
		config.setMaxIdle(MAX_IDLE);
		config.setMaxWaitMillis(MAX_WAIT);
		config.setTestOnBorrow(BORROW);
		pool = new JedisPool(config, IP, PORT, TIMEOUT);
	}

	/**
	 * 获取连接
	 */
	public static synchronized Jedis getJedis() {
		try {
			if (pool != null) {
				return pool.getResource();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.info("连接池连接异常");
			return null;
		}

	}

	/**
	 * @Description:设置失效时间
	 * @param @param
	 *            key
	 * @param @param
	 *            seconds
	 * @param @return
	 * @return boolean 返回类型
	 */
	public static void disableTime(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.expire(key, seconds);

		} catch (Exception e) {
			logger.debug("设置失效失败.");
		} finally {
			getColse(jedis);
		}
	}

	/**
	 * @Description:插入对象
	 * @param @param
	 *            key
	 * @param @param
	 *            obj
	 * @param @return
	 * @return boolean 返回类型
	 */
	public static boolean addObject(String key, Object obj) {

		Jedis jedis = null;
		String value = JsonUtils.objectToJson(obj);
		try {
			jedis = getJedis();
			String code = jedis.set(key, value);
			if (code.equals("ok")) {
				return true;
			}
		} catch (Exception e) {
			logger.debug("插入数据有异常.");
			return false;
		} finally {
			getColse(jedis);
		}
		return false;
	}

	/**
	 * @Description:删除key
	 * @param @param
	 *            key
	 * @param @return
	 * @return boolean 返回类型
	 */
	public static boolean delKey(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long code = jedis.del(key);
			if (code > 1) {
				return true;
			}
		} catch (Exception e) {
			logger.debug("删除key异常.");
			return false;
		} finally {
			getColse(jedis);
		}
		return false;
	}

	/**
	 * @Description: 关闭连接
	 * @param @param
	 *            jedis
	 * @return void 返回类型
	 */

	public static void getColse(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}

//	 public static void main(String[] args) {
//		 IMToken imToken = new IMToken();
//		 imToken.setAccess_token("1111");
//		 imToken.setExpires_in(60);
//		 RedisUtils.addObject("tokenTest", imToken);
//		 RedisUtils.disableTime("tokenTest", imToken.getExpires_in());
//		 String im = RedisUtils.getJedis().get("tokenTest");
//		 System.out.println(im);
//	 }
}
