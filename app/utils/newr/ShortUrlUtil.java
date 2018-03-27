package utils.newr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;

import business.BackstageSet;

/**
 * 新浪短链接生成方法
 * @author yxb_liyz
 *
 */
public class ShortUrlUtil {

	private static String urlEncoded="utf-8";

	private static String url="http://api.t.sina.com.cn/short_url/shorten.json?";//sina

	public static String toShortUrl(String string){
		
		String source="31641035";//sina app key 
		String url_long=url+"source="+source+"&url_long="+string;
		String url_short="";
		// 创建线程安全的httpClient
		HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
		// 创建一个HttpGet请求，作为目标地址。
		HttpGet httpGet = new HttpGet(url_long);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				
				String result = EntityUtils.toString(response.getEntity(), urlEncoded);
				JSONArray jsonArray=JSONArray.fromObject(result);
				List<Map<String,Object>> list = (List<Map<String,Object>>)JSONArray.toCollection(jsonArray, Map.class);
				if(list.size()<=0){
					return "获取短链接失败！";
				}
				if(list.get(0).get("type")==null || (Integer)list.get(0).get("type")!=0){
					return (String)list.get(0).get("Error");
				}
				url_short=(String)list.get(0).get("url_short");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.abort();
			httpClient.getConnectionManager().shutdown();
		}
		return url_short;
	}
	//测试生成百度短链接
	public static void main(String[] args) {
//		String tinyUrl = toShortUrl("http://www.yinxinbao.com/m/registerMobile?recommendMobile=15106706561");
		String tinyUrl = toShortUrl("jdlsjdljsljdlksjdlkjslkjlkjsl");
		System.out.println(tinyUrl);
	}
}
