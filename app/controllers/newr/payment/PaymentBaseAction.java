package controllers.newr.payment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import utils.ErrorInfo;
import constants.Constants;
import constants.newr.PayType;
import controllers.newr.BaseController;

/**
 * 资金托管，Action基类
 *
 * @author hys
 * @createDate  2015年8月29日 上午9:24:05
 *
 */
public class PaymentBaseAction extends BaseController{
	
	/**
	 * 表单自动提交，请求第三方
	 * 
	 * @param html
	 */
	public Map<String, Object> submitForm(String html, int client){
		if(client == Constants.APP){
			Map<String, Object> map =new HashMap<String, Object>();
			map.put("htmlParams", html);
			return map;
		}
		renderHtml(html);
		return null;
	}

	/**
	 * 跳转提示页面
	 * 		注意：此提示页面只使用与前台，后台业务请自己编写提示页面
	 */
	public static void payErrorInfo(int code, String msg, PayType payType,int... client){
		
		if(StringUtils.isBlank(payType.getSuccessTip())){
			
			throw new RuntimeException("此交易类型不支持：跳转错误提示页面");
		}
		
		String returnUrl;
		String urlDesc;
		
		if(code >= 0 || code == Constants.ALREADY_RUN){
			msg = payType.getSuccessTip();
			returnUrl = payType.getSuccessUrl();
			urlDesc = payType.getSuccessUrlDesc();
		}else{
			returnUrl = StringUtils.isBlank(payType.getFailedUrl())?payType.getSuccessUrl():payType.getFailedUrl();
			urlDesc = StringUtils.isBlank(payType.getFailedUrl())?payType.getSuccessUrlDesc():payType.getFailedUrlDesc();
		}

		String httpPath = Play.configuration.getProperty("http.path");
		
		if(StringUtils.isNotBlank(httpPath)){
			returnUrl = httpPath + returnUrl;
		}
		
		ErrorInfo error = new ErrorInfo();
		
		error.code = code;
		error.msg = msg;
		error.returnUrl = returnUrl;
		error.returnMsg = urlDesc;
		
		if(client != null && client.length > 0 ){
			if(client[0] == Constants.APP) {
				Map<String, Object> jsonMap = new HashMap<String, Object>();
				  
		        if (0 == error.code) {
	        		jsonMap.put("error", "-1");
	        		jsonMap.put("msg", error.msg);
		        } else {
	              	jsonMap.put("error", "2");
	              	jsonMap.put("msg", error.msg);
		        }
		        Logger.info("*********************APP返回结果**********************"+jsonMap);
		        renderJSON(jsonMap);
			}
		}
		
		render(error);
	}
}
