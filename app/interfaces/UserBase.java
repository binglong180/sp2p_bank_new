package interfaces;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.t_system_options;
import models.newr.t_users;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import utils.ErrorInfo;

import com.shove.security.Encrypt;

import constants.OptionKeys;

public class UserBase implements Serializable{

	public long id;

	public String password;
	protected String _password;
	
	public Date time;
	
	public String name;
	protected String _name;
	
	public String realityName;
	
	public int passwordContinuousErrors;
	public boolean isPasswordErrorLocked;
	public Date passwordErrorLockedTime;

	public boolean isAllowLogin;
	public long loginCount;
	public Date lastLoginTime;
	public String lastLoginIp;
	public Date lastLogoutTime;
	
	public  String email;
	protected String _email;
	
	public boolean isEmailVerified;
	public String telephone;
	public String mobile;
	public String mobile1;
	public boolean isMobileVerified;
	public String mobile2;
	public String idNumber;
	public String address;
	public String postcode;
	public String sex;
	protected int _sex;
	
	public Date birthday;
	protected Date _birthday;
	
	public int age;
	protected int _age;
	
	/**
	 * 登录
	 * @param password
	 * @return
	 */
	public long login(String password){
		/*获取锁定的错误次数*/
		t_system_options option = t_system_options.find("byKey", OptionKeys.SECURITY_IS_USERNAME_LIMIT_WORDS).first();	
		if(this.isPasswordErrorLocked) {
			t_system_options lockTime = t_system_options.find("byKey", OptionKeys.SECURITY_LOCK_TIME).first();
			if(new Date().getTime()-this.passwordErrorLockedTime.getTime() < Long.parseLong(lockTime._value)*1000) {
//				this.errorInfo = "用户已经锁定";
				return -1;//用户已经锁定
				
			}
		}
		
		t_users user = t_users.findById(this.id);

		
		user.save();
		
		setCookie(this.id);
		
		return 0;
	}
	
	public long  logout(){
		Response.current().setCookie(this.getClass().getSimpleName(), "", null, "/", null, false, true);
		return 0;
		
	};
	
	/**
	 * 写入cookie
	 * @param id
	 */
	public void setCookie(long id) {
		String secret = Play.configuration.getProperty("application.secret");
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String cookie = Encrypt.encrypt3DES(id+","+simple.format(new Date()),secret);
//		String cookie = id+"hello";
		String cookieKey = this.getClass().getSimpleName();
		Response.current().setCookie(cookieKey, cookie, null, "/", null, false, true);
	}
	
	public long getCookie(ErrorInfo e) {
		
		Cookie cookie = Request.current().cookies.get(this.getClass().getSimpleName());
		
		if(cookie == null ) {
			e.msg = "cookie不存在";
			return -1;
		}
		
		String secret = cookie.value;
		
		if(secret ==null || secret.equals("")) {
			e.msg = "cookie中的值为空";
			return -1;
		}
		
		secret = Encrypt.decrypt3DES(secret, Play.configuration.getProperty("application.secret"));
		
		if(secret == null) {
			e.msg = "cookie解密后值为空";
			return -1;
		}
		
		String [] secrets = secret.split(",");
		
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		
		try {
			date = simpleDate.parse(secrets[1]);
		} catch (ParseException e1) {
			e.msg = "时间转换异常";
			e1.printStackTrace();
			return -1;
			
		}
		long millisecond = System.currentTimeMillis()-date.getTime();
		
		if(millisecond>48*60*60*1000) {
			e.msg = "cookie已经过期";
			return -1;
			
		}
		
		if (StringUtils.isBlank(secrets[0])) {
			e.msg = "cookie的id为空";
			return -1;
					
		}
		long id = Long.parseLong(secrets[0]);
		
		return id;
		
	}
	
}
