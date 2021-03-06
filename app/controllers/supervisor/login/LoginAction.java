package controllers.supervisor.login;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import utils.DataUtil;
import utils.ErrorInfo;
import business.BackstageSet;
import business.Supervisor;
import constants.Constants;
import controllers.BaseController;
import controllers.newr.supervisor.projectManager.ProjectAction;
import controllers.supervisor.managementHome.HomeAction;

/**
 * 登录
 * @author lzp
 * @version 6.0
 * @created 2014-5-29
 */
public class LoginAction extends BaseController {
	
	/**
	 * 登录界面
	 */
	public static void loginInit() {
		String randomID = Codec.UUID();
		String companyName = BackstageSet.getCurrentBackstageSet().companyName;		
		ErrorInfo error = new ErrorInfo();
		render(randomID, companyName);
	}
	
	/**
	 * ip定位
	 */
	public static void ipLocation() {
		renderText(1 + 1);
	}
	
	/**
	 * 云盾登录
	 * @param userName
	 * @param password
	 * @param sign
	 * @throws UnsupportedEncodingException
	 */
	public static void ukeyCheck(String userName, String password, String sign, String time) throws UnsupportedEncodingException{
		ErrorInfo error = new ErrorInfo();
		
		String result = Supervisor.checkUkey(userName, password, sign, time, error);
		ByteArrayInputStream is = new ByteArrayInputStream(result.getBytes("ISO-8859-1"));
		
		renderBinary(is);
	}
	
	/**
	 * 登录
	 * @param name
	 * @param password
	 * @param captcha
	 * @param randomCode
	 */
	public static void login(String name, String password, String captcha, String randomID, String city, String flag) {
		
	   business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet(); 
	   if(null != currBackstageSet){
		   Cache.delete("backstageSet");//清除系统设置缓存
	   }
		
		ErrorInfo error = new ErrorInfo();
		
		flash.put("name", name);
		flash.put("password", password);
		
		if (Constants.CHECK_CODE) {
			
			if (StringUtils.isBlank(captcha)) {
				flash.error("请输入验证码");
				
				loginInit();
			}

			if (StringUtils.isBlank(randomID)) {
				flash.error("请刷新验证码");
				
				loginInit();
			}

			String random = (String) Cache.get(randomID);
			Logger.info("supervisor_[id:%s][random:%s]", randomID,random);
			Cache.delete(randomID);
			if (!captcha.equalsIgnoreCase(random)) {
				flash.error("验证码错误");
				
				loginInit();
			}
		}		
		Supervisor supervisor = new Supervisor();
		supervisor.name = name;
		supervisor.loginIp = DataUtil.getIp();
		supervisor.loginCity = city;		
		supervisor.login(password, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			loginInit();
		}
		//ProjectAction.getloanUser();
		HomeAction.showHome();
	}
	
	public static void logout() {
		ErrorInfo error = new ErrorInfo();
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if (null != supervisor) {
			supervisor.logout(error);
		}
		
		Supervisor.deleteCurrSupervisor();//请除缓存
		redirect(Constants.HTTP_PATH + "/supervisor");
	}

	/**
	 * 跳转到警告页面
	 */
	public static void loginAlert() {
		render();
	}
	
	/**跳转到空白页面
	 */
	public static void toBlank(String sign) {
		if(sign.equals(Constants.CLOUD_SHIELD_NOT_EXIST)){
			flash.error("请插入安全云盾！");
			render();
		}
		
		if(sign.equals(Constants.CLOUD_SHIELD_UN_SYSTEM)){
			flash.error("尊敬的用户，您插入的云盾不支持本系统或者版本过低，请与软件开发商联系！");
			render();
		}
		
		if(sign.equals(Constants.CLOUD_SHIELD_SUPERVISOR)){
			flash.error("尊敬的用户，您插入的云盾不属于当前管理员！");
			render();
		}
		
		render();
	}
}
