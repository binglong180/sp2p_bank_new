package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import play.Logger;
import play.mvc.Http;

/**
 * 系统流量日志打印加统计
 * @author lijiayu
 * @date 2015年8月20日
 */
public class FlowCount {

	private FlowCount() {}

	private static class Single {

		private final static FlowCount flowcount = new FlowCount();
	}

	public static FlowCount getInstance() {
		return Single.flowcount;
	}

	// 简单日志打印，使用Map做缓存
	private Map<String, Integer> count = new ConcurrentHashMap<String, Integer>();

	// 打印IP数量控制
	private boolean printIpCount = true;

	public void printAcceseInfo() {
		Http.Request request = Http.Request.current();
		if (request.url == null || request.url.equals("") || request.url.contains("images")
				|| request.url.contains("setCode") || request.url.contains("getImg")) {
			return;
		}
		addCount(request.remoteAddress);
		Logger.info("FlowCount ip:[%s] method:[%s] count:[%s] url:[%s]", request.remoteAddress, request.method,
				getCount(request.remoteAddress), request.url);
		if (printIpCount) {
			printIpCount = false;
			// 启动打印IP 总数线程
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							TimeUnit.MINUTES.sleep(10);
							Logger.info("printIpCount:[%s]", count.size());
						}
					} catch (InterruptedException e) {
					}
				}
			}).start();
		}
	}

	/**
	 * 添加计数
	 * @param ip
	 */
	public synchronized void addCount(String ip) {
		if (null == ip || ip.equals("")) {
			return;
		}
		int size = count.get(ip) == null ? 0 : count.get(ip);
		count.put(ip, ++size);
	}

	/**
	 * 获得计数
	 * @param ip
	 * @return
	 */
	public synchronized int getCount(String ip) {
		if (ip == null || ip.equals("")) {
			return 0;
		}
		return count.get(ip) == null ? 0 : count.get(ip);
	}

	/**
	 * 获得总计数
	 * @return
	 */
	public synchronized int getAllCount() {
		return count.size();
	}

	/**
	 * 清理缓存
	 */
	public synchronized void clean() {
		count.clear();
	}

}
