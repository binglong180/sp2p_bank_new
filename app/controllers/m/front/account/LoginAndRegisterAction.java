package controllers.m.front.account;

import java.util.Date;

import models.newr.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Http.Request;
import play.mvc.Scope;
import play.mvc.Scope.Session;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import business.newr.User;
import constants.newr.Constants;
import controllers.newr.BaseController;
import controllers.newr.DSecurity;

/**
 * 
 * @author chencheng
 * 
 */
@With(DSecurity.class)
public class LoginAndRegisterAction extends BaseController {

	/**
	 * 跳转到登录页面
	 */
	public static void login(String fromUrl) {
		Scope.Params p = params;
		String encryString = Session.current().getId();
		Integer  loginCount= (Integer)Cache.get("loginCount" + encryString);
		flash.put("loginCount", loginCount == null ? 0 : loginCount);
		if(Request.current().headers.get("referer")!=null){
			String toURl = Request.current().headers.get("referer").values.get(0);
			play.cache.Cache.add("toUrl", toURl, Constants.CACHE_TIME_MINUS_2);	
		}
		
 		render();
	}

	public static void logout() {
		User user = User.currUser();

		if (user == null) {
			LoginAndRegisterAction.login(null);
		}

		ErrorInfo error = new ErrorInfo();

		user.logout(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		login(null);
	}

	/**
	 * 包含登录页面的登录
	 */
	public static void logining() {
		String encryString = Session.current().getId();
		Integer loginCount = (Integer )Cache.get("loginCount" + encryString);
		loginCount = loginCount == null ? 0 : loginCount;
		Cache.set("loginCount"+encryString, ++loginCount, Constants.CACHE_TIME_MINUS_30);
		
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		if(null != currBackstageSet){
		  Cache.delete("backstageSet");//清除系统设置缓存
		}	   
		ErrorInfo error = new ErrorInfo();
		String name = params.get("name");		
		StringBuffer url2 = new StringBuffer();
		String password = params.get("password");
		
		flash.put("name", name);
		
		if (StringUtils.isBlank(name)) {
			flash.error("请输入用户名");

		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");

		}
		String LoginUrl = Play.configuration.getProperty("test.application.baseUrl") + Play.configuration.getProperty("http.path") + "/m/login";
		String url = LoginUrl;
		
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isMobileNum(name)){
			 user.findUserByMobile(name);
		}else{
			user.queryUserByName(name, error);
		}
        
		if (user.id < 0) {
			flash.error("该用户名不存在");
			redirect(url);
		}
		if (user.login(password,false, Constants.CLIENT_H5, error) < 0) {
			flash.error(error.msg);
			redirect(url);
		}
		Cache.delete("loginCount"+encryString);		
		if (LoginUrl.equalsIgnoreCase(url)) {
				String successURl = (String)play.cache.Cache.get("toUrl");				
				if(successURl!=null && (successURl.contains("invest")||successURl.contains("Invest"))){
					redirect(successURl);
				}else{
					redirect("/m/front/invest/investList"); //2016 add 成功登录后跳转至投资列表页
				}
		}		
	}

	
	/**
	 * 手机注册页面
	 */
	public static void registerMobile(String nameForRecommend) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		flash.put("recommendUserName", nameForRecommend);
		
		render(loginOrRegister);
	}
	
	/**
	 * 获取验证码并返回页面
	 */
	public static void codeReturn(String codeImg) {
		String randomID = (String) Cache.get(codeImg);

		JSONObject json = new JSONObject();
		json.put("randomID", randomID);

		renderJSON(json);
	}

	/**
	 * 验证注册
	 * 
	 * @param t_users
	 */
	public static void registering() {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		String name = params.get("userName");
		String mobile = params.get("mobile");
		String smsCode = params.get("smsCode");
		String password = params.get("password");
		String randomID = (String) Cache.get(params.get("randomID"));
		String code = params.get("code");
		flash.put("userName", name);
		flash.put("mobile", mobile);
		flash.put("smsCode", smsCode);
		flash.put("code", code);		
		String reccommendMobile = params.get("recommendId");
		User user = new User();
		if (StringUtils.isBlank(name)) {
			flash.error("请填写用户名");			
	       registerMobile(reccommendMobile);	
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
		    registerMobile(reccommendMobile);			
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			registerMobile(reccommendMobile);
		}

		if (!RegexUtils.isValidUsername(name)) {
			flash.error("请填写符合要求的用户名");
			registerMobile(reccommendMobile);
			
		}

		
		if (!RegexUtils.isMobileNum(mobile)) {
			flash.error("请填写正确的手机号码");
			registerMobile(reccommendMobile);
		}
			
		if (StringUtils.isBlank(smsCode)) {
			flash.error("请输入短信校验码");
			registerMobile(reccommendMobile);
		}
			
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);				
			if(cCode == null) {
				flash.error("短信校验码已失效，请重新点击发送校验码");
				registerMobile(reccommendMobile);
			}				
			if(!smsCode.equals(cCode)) {
				flash.error("短信校验码错误");
				registerMobile(reccommendMobile);
			}
		}
		User.isMobileExist(mobile, null, error);
		    if (error.code < 0) {
				flash.error(error.msg);				
				registerMobile(reccommendMobile);
			}				
		if (!RegexUtils.isValidPassword(password)) {
			flash.error("请填写符合要求的密码");
			registerMobile(reccommendMobile);
			
		}
		if(Constants.CHECK_CODE){			
			if(!code.equalsIgnoreCase(randomID)) {
				flash.error("图片校验码错误");
			}
		}
		
		User.isNameExist(name, error);
		if (error.code < 0) {
			flash.error(error.msg);
		}

		User.isMobileExist(mobile, null, error);
			
		if (error.code < 0) {
			flash.error(error.msg);
			registerMobile(reccommendMobile);
		}
		
			
		if(StringUtils.isNotBlank(reccommendMobile)&&!Constants.PAGEINPUTTIP.equals(reccommendMobile.trim())){
			if (!RegexUtils.isMobileNum(reccommendMobile)) {
				flash.error("请填写正确的手机号码");
				registerMobile(reccommendMobile);
			}

		  t_users reccommendUser = t_users.find("mobile = ? ", reccommendMobile).first();
			user.recommendUserId = reccommendUser.id;
			user.recommendTime = new Date();
		}
	
		if (error.code < 0) {
			flash.error(error.msg);
			registerMobile(reccommendMobile);
		}
			
		user.time = new Date();
		user.name = name;
		user.password = password;
	    user.mobile = mobile;
		user.register(Constants.CLIENT_PC, error);

		if (error.code < 0) {
			flash.error(error.msg);
		}
		AccountHomeAction.accountHome();
	}
	
	/**
	 * 发送短信验证码
	 * @param mobile
	 */
	public static void sendSmsCode(String mobile,boolean send) {
		Logger.debug("\n类：%s\n方法：%s\n参数：%s", Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), JSONObject.fromObject(params.data));
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(mobile) ) {
			error.code = -1;
			error.msg = "手机号码不能为空";
			
			renderJSON(error);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "手机号格式有误";
			
			renderJSON(error);
		}
		
		if(User.isMobileExist(mobile, null, error) < 0){
			renderJSON(error);
		}
		if(send){
			SMSUtil.sendCode(mobile, error);
		}
		
		renderJSON(error);
	}

	/**
	 * 生成验证码图片
	 * 
	 * @param id
	 */
	public static void getImg(String id) {
		Images.Captcha captcha = CaptchaUtil.CaptchaImage(id);
		if(captcha == null){
			return;
		}

		renderBinary(captcha);
	}

	/**
	 * 刷新验证码
	 */
	public static void setCode() {
		String randomID = CaptchaUtil.setCaptcha();

		JSONObject json = new JSONObject();
		json.put("img", randomID);
		renderJSON(json.toString());
	}

	/**
	 * 校验验证码
	 */
	public static void checkCode(String randomId, String code) {

		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";

			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(randomId)) {
			error.code = -1;
			error.msg = "请刷新验证码";

			json.put("error", error);
			renderJSON(json);
		}

		String radomCode = CaptchaUtil.getCode(randomId);

		if (!code.equalsIgnoreCase(radomCode)) {
			error.code = -1;
			error.msg = "验证码错误";

			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 验证用户名是否已存在
	 * 
	 * @param name
	 */
	public static void hasNameExist(String name) {
		ErrorInfo error = new ErrorInfo();
		User.isNameExist(name, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json.toString());  //code>0 用户名不存在。code<0用户名已存在或验证异常
	}

	/**
	 * 验证手机号码是否已存在
	 * 
	 * @param name
	 */
	public static void hasMobileExist(String telephone) {
		ErrorInfo error = new ErrorInfo();

		int nameIsExist = User.isMobileExist(telephone, null, error);

		JSONObject json = new JSONObject();
		json.put("result", nameIsExist);

		renderJSON(json.toString());
	}

	/**
	 * 注册跳转到成功页面
	 */
	public static void registerSuccess() {
		User user = User.currUser();
		if (user == null) {
			login(null);
		}

	    AccountHomeAction.accountHome();
		

	}


	/**
	 * 通过手机重置密码
	 */
	public static void resetPasswordByMobile() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_PIC_CODE;
		
		render(loginOrRegister, randomId, checkMsgCode);
	}

	/**
	 * 保存重设的密码
	 */
	public static void savePasswordByMobile(String mobile, String code,
			String password, String confirmPassword, String randomID, String captcha) {
		ErrorInfo error = new ErrorInfo();
        
		User.updatePasswordByMobile(mobile, code, password, confirmPassword,
				randomID, captcha, error);

		if (error.code < 0) {
			flash.put("mobile", mobile);
			flash.put("code", code);
			flash.put("captcha", captcha);

			flash.error(error.msg);
			
			resetPasswordByMobile();
		}

		flash.error(error.msg);

		login(null);
	}

	
	/**
	 * 跳转到重置密码页面
	 */
	public static void resetPassword(String sign) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login(null);
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login(null);
		}
		
		User user = new User();
		user.id = id;
		user.updatePasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetPassword(sign);
		}
		
		flash.error(error.msg);
		
		login(null);
	}
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_SECOND_60);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != 0) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
    }
    
    
    /**
     * 
     * 手机密码找回
     */
    public static void findPasswordByMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        System.out.println("mobile:"+mobile+" captcha:"+captcha+" randomID:"+randomID);
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
         
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_MINUS_2);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			json.put("error", error);
        			renderJSON(json);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != -2) {
            	error.code = -1;
            	error.msg= "手机号码不存在";
            	
                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);
        
        json.put("error", error);

        renderJSON(json);
    }
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobileForReg(String mobile, String code, String randomID) {
        ErrorInfo error = new ErrorInfo();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_SECOND_60);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        }
        
        if(User.isMobileExist(mobile, null, error) < 0){
			renderJSON(error);
		}
        SMSUtil.sendCode(mobile, error);
        renderJSON(error);
    }
    
	/**
	 * 检验邮件注册图形验证码
	 * @param code
	 * @param randomID
	 */
    public static void verifyEmailForReg(String code, String randomID) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    

    /**
     *
     * 短信校验码检验
     */
    public static void verifySmsCodeForReg(String mobile, String smsCode) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_MSG_CODE) {
	        String codec = (String) Cache.get(mobile);
	        if (!smsCode.equalsIgnoreCase(codec)) {
	        	error.code = -1;
	        	error.msg = "短信校验码输入错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    
    
    /**
	 * 验证手机号码是否已存在
	 * 
	 * @param mobile
	 */
	public static void hasMobileExists(String mobile) {
		ErrorInfo error = new ErrorInfo();
		if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
		User.isMobileExist(mobile, null, error);
		renderJSON(error);
	}
	
    /**
	 * 验证用户名和密码是否正确
	 * 
	 * @param mobile
	 */
	public static void verifyLogin(String name, String password){
//		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isMobileNum(name)){
			 user.findUserByMobile(name);
		}else{
			user.queryUserByName(name, error);
		}
        
		if (user.id < 0) {
			error.code = -1;
			error.msg = "该用户名不存在";
			renderJSON(error);
		}else if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			renderJSON(error);
		}else{
			error.code = 0;
			error.msg = "登录成功";
			renderJSON(error);
		}
		
	}
	
	
	/**
	 * 注册协议
	 */
	public static void agreement() {
		render();
	}
	/**
	 * 判断用户是否登录
	 */
	public static void isLogin(){
		JSONObject data=new JSONObject();
		int result=User.isLogin();
		data.put("result",result);
		renderJSON(data);
	}

}
