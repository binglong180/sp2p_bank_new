package APP;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.shove.gateway.GeneralRestGateway;

import constants.Constants;

public class BaseTest {
	
	public static  String urlEncoded="UTF-8";
//	public static  String url="http://localhost:9000/app/";
	public static  String url="http://bin.tunnel.mobi/app/";
	// 创建默认的httpClient客户端
	private HttpClient httpClient = null;

	// get模式
	private HttpGet httpGet = null;

	// post模式
	private HttpPost httpPost = null;

	// 执行请求，获取服务器响应
	private HttpResponse response = null;

	// 请求的实体
	private HttpEntity entity = null;
	
	public String sendHttp(Map<String,String> parameters) throws Exception{
		
		String buildUrl = GeneralRestGateway.buildUrl(url,"MP3PtLpkqQYFOIei", parameters);
		System.out.println(buildUrl);
		String jsonString=byGetMethodToHttpEntity(buildUrl);
		return jsonString;
	}
	
	/***
	 * 
	 * @Title: byGetMethodToHttpEntity 
	 * @Description: get方式提交并且返回Entity字符串
	 * @param:@param url
	 * @param:@return
	 * @author: bin.w
	 * @date: 2015-9-11 下午2:06:06
	 */
	public String byGetMethodToHttpEntity(String url) {
		StringBuffer buff = new StringBuffer();
		// 创建线程安全的httpClient
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
		// 创建一个HttpGet请求，作为目标地址。
		httpGet = new HttpGet(url);

		try {
			response = httpClient.execute(httpGet);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				buff.append(EntityUtils.toString(entity)); 
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			releaseSource(httpGet, null, httpClient);
		}
		return buff.toString();
	}
	
	/**
	 * @Title: PostMethodToHttpEntity 
	 * @Description:  Post方式提交并且返回Entity字符串
	 * @param:@param url
	 * @param:@param params
	 * @param:@param urlEncoded
	 * @param:@return
	 * @author: bin.w
	 * @date: 2015-9-11 上午9:08:59
	 */
	public String PostMethodToHttpEntity(String url,List<NameValuePair> params,String urlEncoded){
		StringBuffer buff = new StringBuffer();
		// 创建线程安全的httpClient
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
		// 创建一个HttpGet请求，作为目标地址。
		httpPost = new HttpPost(url);
		try {
			if(params != null){
				// 格式化参数列表并提交
				UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(params,
						urlEncoded);
				httpPost.setEntity(uefEntity);
			}
			response = httpClient.execute(httpPost);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				buff.append(EntityUtils.toString(entity)); 
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			releaseSource(null, httpPost, httpClient);
		}
		return buff.toString();
	}
	

	/**
	 * 释放资源
	 * @param httpGet
	 * @param httpPost
	 * @param httpClient
	 */
	public void releaseSource(HttpGet httpGet,
			HttpPost httpPost, HttpClient httpClient) {
		if (httpGet != null) {
			httpGet.abort();
		}
		if (httpPost != null) {
			httpPost.abort();
		}
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}
}
