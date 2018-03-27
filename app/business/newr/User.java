package business.newr;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_bill_invests;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_user_details;
import models.newr.t_users;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.mvc.Scope.Session;
import utils.CacheManager;
import utils.CharUtil;
import utils.DataUtil;
import utils.ErrorInfo;
import utils.IDCardValidate;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;
import utils.Security;
import business.DealDetail;
import business.StationLetter;
import business.Supervisor;
import business.TemplateStation;

import com.google.zxing.BarcodeFormat;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import constants.Constants;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;


/**
 * 用户的业务实体
 * @author mzq
 * @version 6.0
 * @created 2016年1月14日 下午4:25:45
 */
public class User implements Serializable{
	
	public long id;

	
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
	
	public void setId(long id) {
	   	
		Model user = null;
			try{
				user = t_users.findById(id);
				Map<String,Object> obj= null;
				obj=queryCorpusAndInterestSum(user.getId(),null,null);
			if(obj!=null){
					this.receiveCorpus = obj.get("receiveCorpus")!=null?(Double)obj.get("receiveCorpus"):0d;
					this.receiveInterest = obj.get("receiveInterest")!=null?(Double)obj.get("receiveInterest"):0d;;
			 }
			} catch(Exception e) {
				Logger.info("用户setId填充时（createBid=true）："+e.getMessage());
				return ;
			}
			
			setInformation(user);		
			return;
				
	}

	public Date time;	
	public String name;	
	public String photo;
	public String realityName;	
	public String password;	
	public String mobile;
	public String idNumber;
	public String postcode;
	public String company;
	public String companyAddress;
	public String officeTelephone;
	public String faxNumber;
    public String role;
    public Long recommendUserId;
    public Date  recommendTime;
	public int masterIdentity;
	public double balance;
	public boolean createBid;	
	/*
	 * 新版用  待收本金',待收利息
	 * */
	public  double receiveCorpus;
	public  double receiveInterest;
	
	public User(){
		
	}

	/**
	 * 注册
	 * @return
	 */
	public int register(int client, ErrorInfo error){ 
		error.clear();	
		t_users user = new t_users();

		user.mobile = this.mobile;
		user.name = this.name;
		user.time = new Date();
		user.password = Encrypt.MD5(this.password+Constants.ENCRYPTION_KEY);		
		user.photo = Constants.DEFAULT_PHOTO;
		user.recommend_user_id = this.recommendUserId;
		user.recommend_time = this.recommendTime;
		String uuid = UUID.randomUUID().toString();
		Qrcode code = new Qrcode();
		try {
			Blob blob = new Blob();
			code.create(Constants.BASE_URL+"m/registerMobile?recommendMobile="+user.mobile, BarcodeFormat.QR_CODE, 175, 175, new File(blob.getStore(), uuid).getAbsolutePath(), "png");
			user.qr_code = uuid;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("创建二维码图片失败"+e.getMessage());
			error.code = -5;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";
			
			return error.code;
		}
		try {
			user.save();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("注册时，保存注册信息时："+e.getMessage());
			error.code = -5;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";
			
			return error.code;
		}
		
		this.id = user.id;
		DealDetail.userEvent(this.id, UserEvent.REGISTER, "注册成功", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		//发送注册站内信
		TemplateStation message = new TemplateStation();
		message.id = Templets.M_REGISTER;
		String mcontent = message.content;
		if(message.status) {
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = this.id;
			letter.title = message.title;
			letter.content = mcontent;
			 
			letter.sendToUserBySupervisor(error);
		}
		
		User.setCurrUser(this);	
		error.code = 0;
		error.msg = "恭喜你，注册成功！";
		
		return error.code;
	}
	
	/**
	 * 登录
	 * @param password
	 * @return
	 */
	public int login(String password,boolean encrypt, int client, ErrorInfo error){
		error.clear();		
		if(!encrypt){
			//密码传入为明文则进行加密处理
			password = Encrypt.MD5(password+Constants.ENCRYPTION_KEY);
		}		
		if(!password.equalsIgnoreCase(this.password)) {
			error.msg = "用户名或密码有误!";
			
			error.code = -7;
			
			return error.code;
		}
		DealDetail.userEvent(this.id, UserEvent.LOGIN, "登录成功", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		setCurrUser(this);
		
		utils.Cache cache = CacheManager.getCacheInfo("online_user_" + this.id + "");
		if (null == cache) {
			cache = new utils.Cache();
			long timeout = 1800000;//单位毫秒
			CacheManager.putCacheInfo("online_user_" + this.id, cache, timeout);
		}
		return error.code;
	}
	/*当用户已经登陆时不允许再次登陆begin
	/**
	 * 编辑信息
	 * @return
	 */
	public void edit(User user, ErrorInfo error){
		error.clear();

		if(StringUtils.isBlank(this.realityName)) {
			error.code = -1;
			error.msg = "请输入真实姓名";
			
			return ;
		}
		
		
		if(StringUtils.isBlank(this.idNumber)) {
			error.code = -1;
			error.msg = "请输入身份证号码";
		}

		
		User.isIDNumberExist(this.idNumber, user.idNumber, error);
			
		if(error.code <0) {
			return ;
		}
		
		EntityManager em = JPA.em();
		
		Query query = em.createQuery("update t_users set "
				+ "reality_name=?,"
				+ "id_number=?,"
				+ "is_add_base_info=? "
				+ "where id = ?")
				.setParameter(1, this.realityName)
				.setParameter(4, this.idNumber)
				.setParameter(10, Constants.TRUE)
				.setParameter(11, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("首次编辑基本信息时,保存用户编辑的信息时："+e.getMessage());
			error.code = -2;
			error.msg = "对不起，由于平台出现故障，此次编辑信息保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(this.id, UserEvent.ADD_BASIC_INFORMATION, "添加用户资料", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}		
		user.realityName = realityName;
		user.idNumber = this.idNumber;		
		error.msg = "保存基本资料成功";
		error.code = 0;
	}
	
	/**
	 * 安全退出
	 * @return
	 */
	public int logout(ErrorInfo error){
		error.clear();	
		
		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_users set last_logout_time = ? where id = ?")
				.setParameter(1, new Date()).setParameter(2, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("安全退出时,保存安全退出的信息时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次安全退出信息保存失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.id, UserEvent.LOGOUT, "安全退出", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		User.removeCurrUser();
		CacheManager.clearByKey("online_user_" + this.id);
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 修改密码
	 * @param oldPassword 旧密码
	 * @param newPassword1 新密码1
	 * @param newPassword2  新密码2
	 * @return
	 */
	public int editPassword(String oldPassword, String newPassword1, String newPassword2, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(oldPassword)) {
			error.code = -1;
			error.msg = "请输入原密码";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2)) {
			error.code = -1;
			error.msg = "密码不能为空";
			
			return error.code;
		}
		
		if(!newPassword1.equals(newPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次密码输入不一致！";
			
			return error.code;
		}
		
		if(!RegexUtils.isValidPassword(newPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";
			
			return error.code;
		}
		
		if(!Encrypt.MD5(oldPassword + Constants.ENCRYPTION_KEY).equalsIgnoreCase(this.password)) {
			error.code = -2;
			error.msg = "密码错误！";
			
			return error.code;
		}		
		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(newPassword1 + Constants.ENCRYPTION_KEY)).setParameter(2, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.id, UserEvent.EDIT_PASSWORD, "修改密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		logout(error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		error.code = 0;
		error.msg = "密码修改成功，请重新登录！";
		
		return error.code;
	}
	
/*add 修改密码时添加idNumber start*/
	public static int checkPass(String value){
		int ls=0;
		if(value.matches("[a-z]+")){
			ls++;
		}
		if(value.matches("[A-Z]+")){
			ls++;
		}
		if(value.matches("[0-9]+")){
			ls++;
		}
		return ls;
	} 
	/**
	 * 通过手机重置用户密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 * @param error
	 * @return
	 */
	public static void updatePasswordByMobileAndIdNumber(String mobile, String code, String password, 
			String confirmPassword, String randomID, String captcha,String idNumber, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";
			
			return ;
		}

		if(StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";
			
			return ;
		}
		
		if(StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";
			
			return ;
		}
		
		if(StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";
			
			return ;
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			
			return ;
		}
		
		if(!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";
			
			return ;
		}
		if(password.length()<6){
			error.code = -1;
			error.msg = "密码长度不够";
			return;
		}
			
		long userId = User.queryIdByMobile(mobile, error);

		if(error.code < 0) {
			error.code = -1;
			error.msg = "该手机号码不存在";
			
			return;
		}
		/*校验身份证号码*/
		User user=new User();
		user.id=userId;
		if(user.idNumber!=null){
			if(idNumber==null || "请输入身份证号".equals(idNumber)){
				error.code = -1;
				error.msg = "身份证号码不能为空";
				return;
			}			
			if(idNumber!=null){
				if(!idNumber.equals(user.idNumber)){
					error.code = -1;
					error.msg = "身份证号码有误";
					return;				
				}
			}
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode1 = Cache.get(mobile)!=null?(Cache.get(mobile)).toString():null;
			if(cCode1 == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return;
			}
			
			if(!code.equals(cCode1)) {
				error.code = -1;
				error.msg = "手机验证错误";
				
				return;
			}
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2, userId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(userId, UserEvent.RESET_PASSWORD_MOBILE, "通过手机重置用户密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		
		error.code = 0;
		error.msg = "密码修改成功，下次登录启用新密码！";
	}
	
	/**
	 * 通过手机重置用户密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 * @param error
	 * @return
	 */
	public static void updatePasswordByMobile(String mobile, String code, String password, 
			String confirmPassword, String randomID, String captcha, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";
			
			return ;
		}
		
		
		
		if(StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";
			
			return ;
		}
		
		if(StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";
			
			return ;
		}
		
		if(StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";
			
			return ;
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			
			return ;
		}
		
		if(!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";
			
			return ;
		}
		
		long userId = User.queryIdByMobile(mobile, error);

		if(error.code < 0) {
			error.code = -1;
			error.msg = "该手机号码不存在";
			
			return;
		}

		if(Constants.CHECK_MSG_CODE) {
			String cCode1 = Cache.get(mobile)!=null?(Cache.get(mobile)).toString():null;
			if(cCode1 == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return;
			}
			
			if(!code.equals(cCode1)) {
				error.code = -1;
				error.msg = "手机验证错误";
				
				return;
			}
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2, userId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(userId, UserEvent.RESET_PASSWORD_MOBILE, "通过手机重置用户密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		
		error.code = 0;
		error.msg = "密码修改成功，下次登录启用新密码！";
	}
	
	public static void updatePasswordByMobileApp(String mobile, String password, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";
			
			return ;
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			
			return ;
		}
		
		long userId = User.queryIdByMobile(mobile, error);
		
		if(error.code < 0) {
			error.code = -1;
			error.msg = "该手机号码不存在";
			
			return;
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2, userId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(userId, UserEvent.RESET_PASSWORD_MOBILE, "通过手机重置用户密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		
		error.code = 0;
		error.msg = "密码修改成功，下次登录启用新密码！";
	}
	
	/**
	 * 通过邮箱重置用户密码
	 * @param password
	 * @param confirmPassword
	 * @param error
	 */
	public void updatePasswordByEmail(String password, String confirmPassword, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";
			
			return ;
		}
		
		if(StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";
			
			return ;
		}
		
		if(!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";
			
			return ;
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(this.id, UserEvent.RESET_PASSWORD_EMAIL, "通过邮箱重置用户密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		
		if(currUser() != null) {
			logout(error);
			
			if(error.code < 0) {
				JPA.setRollbackOnly();
				return;
			}
		}
		
		error.code = 0;
		error.msg = "重置密码成功，下次登录启用新密码！";
	}
	
	/**
	 * 通过邮箱重置用户密码
	 * @param password
	 * @param confirmPassword
	 * @param error
	 */
	public void updatePayPasswordByEmail(String password, String confirmPassword, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";
			
			return ;
		}
		
		if(StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";
			
			return ;
		}
		
		if(!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";
			
			return ;
		}
		
		if(!RegexUtils.isValidPassword(confirmPassword)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";
			
			return ;
		}
		
		

		String payPassword = Encrypt.MD5(confirmPassword+Constants.ENCRYPTION_KEY);
		
		if(this.password.equals(payPassword)){
			error.code = -1;
			error.msg = "交易密码和登录密码不能一样";
			
			return ;
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set pay_password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时："+e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(this.id, UserEvent.RESET_PASSWORD_EMAIL, "通过邮箱重置用户密码", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		
		if(currUser() != null) {
			logout(error);
			
			if(error.code < 0) {
				JPA.setRollbackOnly();
				return;
			}
		}
		
		error.code = 0;
		error.msg = "重置交易密码成功，下次使用启用新密码！";
	}
	
	/**
	 * 修改手机号码
	 * @param mobile 修改后的手机号码
	 * @return 0运行成功
	 */
	public int editMobile(String code, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(this.mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";
			
			return error.code;
		}
		
		if(!RegexUtils.isMobileNum(this.mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			
			return error.code;
		}
		
		if(Constants.CHECK_MSG_CODE) {
//			String cCode = (String) Cache.get(mobile);  
			/* 2014-11-17  */
			String cCode=null;
			try{
				 Object obj = Cache.get(mobile);
				 cCode = obj.toString(); 
			}catch(Exception e){
			}

			
			if(cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return error.code;
			}
			if(!code.equals(cCode)) {
				error.code = -1;
				error.msg = "手机验证错误";
				
				return error.code;
			}
		}
		
		isMobileExist(this.mobile, null, error);
		
		if(error.code < 0) {
			return error.code;
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_users set mobile = ?,is_mobile_verified = 1 where id = ?";
		Query query = em.createQuery(sql).setParameter(1, this.mobile).setParameter(2, this.id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("保存手机号码时："+e.getMessage());
			error.code = -3;
			error.msg = "保存手机号码时出现异常";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.id, UserEvent.EDIT_MOBILE, "修改绑定手机", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		setCurrUser(this);
		
		error.code = 0;
		error.msg = "安全手机绑定成功！";
		
		return 0;
		
	}
	

	
	/**
	 * 管理员激活用户
	 * @param supervisorId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int activeUserBySupervisor(long userId, ErrorInfo error) {
		error.clear();
		
		int code = DataUtil.update("t_users", new String[]{"is_email_verified"}, new String[]{"id"}, 
				new Object[]{Constants.TRUE, userId}, error);
		
		if(code < 0) {
			return code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.VERIFIED_EMAIL, "手动激活用户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		error.code = 0;
		error.msg = "用户激活成功！";
		
		return error.code;
	}
	
	
	
	/**
	 * 实名认证
	 * @param realName
	 * @param idNumber
	 */
	public int checkRealName(String realName, String idNumber, ErrorInfo error) {
		error.clear();
		
		if (!CharUtil.isChinese(realName.trim())) {
			error.code = -1;
			error.msg = "真实姓名必须是中文";
			
			return error.code;
		}
		
		if(!"".equals(IDCardValidate.chekIdCard(0, idNumber))) {
			error.code = -2;
			error.msg = "请输入正确的身份证号";
			
			return error.code;
		}
		
		User.isIDNumberExist(idNumber, null, error);
		
		if(error.code < 0) {
			//error.msg = "此身份证已开户，请重新输入";
			
			return error.code;
		}
		
		String sql = "update t_users set reality_name = ?, id_number = ? where id = ?";
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, realName, idNumber, this.id).executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			Logger.info("实名认证："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次实名验证失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		this.realityName = realName;
		this.idNumber = idNumber;
		setCurrUser(this);
		
		error.clear();
		error.msg = "实名认证成功";
		
		return error.code;
	}
	
	/**
	 * 手机认证
	 * @param mobile
	 * @param code
	 * @param error
	 */
	public int checkMoible(String mobile, String code, ErrorInfo error) {
		error.clear();
		
		if(StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";
			
			return error.code;
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			
			return error.code;
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);
			
			if(cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return error.code;
			}
			
			if(!code.equals(cCode)) {
				error.code = -1;
				error.msg = "验证码错误";
				
				return error.code;
			}
		}
		
		isMobileExist(mobile, null, error);
		
		if(error.code < 0) {
			return error.code;
		}
		
		String sql = "update t_users set mobile = ?, is_mobile_verified = ? where id = ?";
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, mobile, true, this.id).executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			Logger.info("手机认证："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次更新邮箱失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		this.mobile = mobile;

		setCurrUser(this);
		
		error.clear();
		error.msg = "手机认证成功";
		
		return error.code;
	}
	
	/**
	 * 用户名是否存在
	 * @param userName
	 * @param info
	 * @return 0不存在 
	 */
	public static int isNameExist(String userName, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(userName)) {
			error.code = -1;
			error.msg = "用户名不能为空";
			
			return error.code;
		}
		
		String sql = "select name from t_users where name = ?";
		String name = null;
		
		try {
			name = t_users.find(sql, userName).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("判断用户名是否存在时,根据用户名查询数据时："+e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次用户名是否存在的判断失败！";
			
			return error.code;
		}
		
		if(name != null && name.equalsIgnoreCase(userName)) {
			error.code = -2;
			error.msg = "用户名已存在";
			
			return -2;	
		}
		
		error.code = 0;
		error.msg = "用户名不存在";
		
		return error.code;
	}
	
	/**
	 * 身份证是否存在
	 * @param userName
	 * @param info
	 * @return 0不存在 
	 */
	public static int isIDNumberExist(String newIdNumber, String OldIdNumber, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(newIdNumber)) {
			error.code = -2;
			error.msg = "身份证号码不能为空";
			
			return error.code;
		}
		
		String sql = "select id_number from t_users where id_number = ?";
		String name = null;
		
		try {
			name = t_users.find(sql, newIdNumber).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("判断身份证是否存在时,根据身份证查询数据时："+e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次用身份证是否存在的判断失败！";
			
			return error.code;
		}
		
		if(name != null) {
			
			if (null != OldIdNumber && OldIdNumber.equals(name)) {
				
				error.code = 0;
				
				return error.code;
			}
			
			error.code = -2;
			error.msg = "此身份证已开户，请重新输入！";
			
			return -2;	
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	
	/**
	 * 手机号码是否已经存在
	 * @param email
	 * @return
	 */
	public static int isMobileExist(String newUserMobile, String oldUserMobile, ErrorInfo error){
		error.clear();
		
		String sql = "select mobile from t_users where mobile = ?";
		String mobile = null;
		
		try {
			mobile = t_users.find(sql, newUserMobile).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("判手机号码是否已经存在时,根据手机号码查询数据时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次手机号码是否已经存在判断失败！";
			
			return error.code;
		}	
		
		if(mobile != null ) {
			
			if (oldUserMobile != null && oldUserMobile.equals(mobile)) {
				error.code = 0;
				
				return error.code;
			}
			
			error.code = -2;
			error.msg = "号码被使用";
			
			return error.code;	
		}
		
		error.code = 0;
		
		return 0;
	}
	

	/**
	 * 管理员将一个用户添加到黑名单
	 * @param supervisonId 管理员id
	 * @param id 用户id
	 * @param reason 原因
	 * @param info
	 * @return
	 */
	public static int addBlacklistBySupervisor(long userId, String reason, ErrorInfo error) {
		error.clear();
		
		if(StringUtils.isBlank(reason)) {
			error.code = -1;
 			error.msg = "加入黑名单原因不能为空！";
 			
 			return error.code;
		}
		
		if(userId <= 0) {
			error.code = -1;
 			error.msg = "传入参数有误！";
 			
 			return error.code;
		}
		
		/*
		 * 下面注释的内容，应该跳转到编写黑名单理由前判断
		 */
		String sql = "select is_blacklist from t_users where id = ?";
		boolean is_blacklist = false;
		
		try {
			is_blacklist = t_users.find(sql, userId).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("管理员添加黑名单时,根据用户ID查询数据时："+e.getMessage());
			error.msg = "对不起，由于平台出现故障，此次添加黑名单失败！";
			
			return -1;
		}
		if(is_blacklist) {
			error.msg = "该用户已在黑名单中";
			return -1;
		}
		
		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_blacklist=?,joined_time=?,joined_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, true).setParameter(2, new Date()).setParameter(3, reason)
				.setParameter(4, userId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员添加黑名单时,更新用户数据时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次添加黑名单失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ADD_BLACKLIST, "加入黑名单", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		error.code = 0;
		error.msg = "添加黑名单用户成功";
		
		return 0;
	}
	
	/**
	 * 管理员将一个用户解除黑名单
	 * @param supervisonId 管理员id
	 * @param id 用户id
	 * @param info
	 * @return
	 */
	public static long editBlacklist(long userId, ErrorInfo error){
		error.clear();
		
//		String sql = "select is_blacklist from t_users where id = ?";
//		boolean is_blacklist = false;
//		
//		try {
//			is_blacklist = t_users.find(sql, userId).first();
//		} catch(Exception e) {
//			e.printStackTrace();
//			Logger.info("管理员解除黑名单时,根据用户ID查询数据时："+e.getMessage());
//			info.msg = "对不起，由于平台出现故障，此次解除黑名单失败！";
//			
//			return -1;
//		}
//		if(！is_blacklist) {
//			info.msg = "该用户不在黑名单中";
//			return -1;
//		}
		
		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_blacklist=?,joined_time=?,joined_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, false).setParameter(2, null).setParameter(3, null)
				.setParameter(4, userId);

		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员解除黑名单时,更新用户数据时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次解除黑名单失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_BLACKLIST, "解除黑名单", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}	
		
		error.code = 0;
		error.msg = "解除黑名单用户成功";
		
		return 0;
	}
	
	/**
	 * 管理员分配用户
	 * @param supervisonId 分配的管理员
	 * @param toSupervisonId 被分配管理员的id
	 * @param userId 被分配的用户
	 * @param info
	 * @return
	 */
	public static int assignUser(long supervisorId, String typeStr , 
			long bidId, ErrorInfo error){
		error.clear();
		
		long userId = 0;
		
		if(!NumberUtil.isNumericInt(typeStr)) {
 			error.code = -1;
 			error.msg = "传入的类型参数有误";
 			
 			return error.code;
 		}
		
 		String hql = "select user_id from t_bids where id = ?";
 		
 		try {
 			userId = t_bids.find(hql, bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";
			
			return -1;
		}
 		
 		if(userId <= 0) {
 			error.code = -1;
 			error.msg = "传入的类型参数有误";
 			
 			return error.code;
 		}
		
 		/*客服可以更改，所以不需要进行此校验*/
//		String sql = "select assigned_to_supervisor_id from t_users where id = ?";
//		long assignedToSupervisorId = -1;
//		
//		try {
//			assignedToSupervisorId = t_users.find(sql, userId).first();
//		} catch(Exception e) {
//			e.printStackTrace();
//			Logger.info("管理员分配用户时,根据用户ID查询数据时："+e.getMessage());
//			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";
//			
//			return -1;
//		}
//		
//		if(assignedToSupervisorId > 0) {
//			error.msg = "该用户已被分配！";
//			
//			return -1;
//		}
 		
// 		int type = Integer.parseInt(typeStr);
// 		
// 		if(type != 2) {
// 			error.code = -1;
// 			error.msg = "对不起！已有借款标单独分配出去，不能进行分配此会员所有标的操作";
// 			
// 			return error.code;
// 		}
 		
 		String sql = "select sum(manage_supervisor_id) from t_bids where user_id = ? and manage_supervisor_id <> ?";
 		Long temp = 0l;
 		
 		try {
 			temp = t_bids.find(sql, userId, supervisorId).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
 		
		if(temp > 0){//说明该会员已经有单个借款标被分配
			error.code = -1;
 			error.msg = "对不起！已有借款标单独分配出去，不能进行分配此会员所有标的操作";
 			
 			return error.code;
		}
 		
		EntityManager em = JPA.em();
		String updateSql = "update t_users set assigned_time=?, assigned_to_supervisor_id=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, new Date()).setParameter(2, supervisorId)
				.setParameter(3, userId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员分配用户时,更新用户数据时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";
			
			return error.code;
		}
		
		if (rows == 0) {
			JPA.setRollbackOnly();
			
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		updateSql = "update t_bids set manage_supervisor_id = ? where user_id = ?";
		query = em.createQuery(updateSql).setParameter(1, supervisorId).setParameter(2, userId);
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("管理员分配用户时,添加标的分配人员时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.REASSIGN_USER, "分配会员所有标", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		error.code = 0;
		error.msg = "用户分配成功!";
		
		return 0;
	}
	
	/**
	 * 重新分配用户所有的借款标
	 * @param supervisorId
	 * @param typeStr
	 * @param toSupervisorIdStr
	 * @param userIdStr
	 * @param error
	 * @return
	 */
	public static int assignUserAgain(long supervisorId, String typeStr ,String toSupervisorIdStr,String userIdStr,ErrorInfo error){
		error.clear();
		
		long userId = 0;
		long toSupervisorId = 0;
		
		if(!NumberUtil.isNumericInt(userIdStr)) {
 			error.code = -1;
 			error.msg = "传入的借款会员ID有误";
 			
 			return error.code;
 		}
		
		if(!NumberUtil.isNumericInt(toSupervisorIdStr)) {
 			error.code = -2;
 			error.msg = "传入的管理员参数有误";
 			
 			return error.code;
 		}
		
		if(!NumberUtil.isNumericInt(typeStr)) {
 			error.code = -1;
 			error.msg = "传入的类型参数有误";
 			
 			return error.code;
 		}
		
		userId = Long.parseLong(userIdStr);
		toSupervisorId = Long.parseLong(toSupervisorIdStr);
		EntityManager em = JPA.em();
		String updateSql = "update t_users set assigned_time=?, assigned_to_supervisor_id=? where id=?";
		
		Query query = em.createQuery(updateSql).setParameter(1, new Date())
				.setParameter(2, toSupervisorId).setParameter(3, userId);

		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员分配用户时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ASSIGN_USER, "分配会员所有标", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		error.code = 0;
		error.msg = "用户分配成功!";

		return 0;
	}
	
	
	

	
	
	/**
	 * 根据id获得用户名
	 * @return
	 */
	public static String queryUserNameById(long id, ErrorInfo error) {
		error.clear();
		
		String sql = "select name from t_users where id = ? limit 1";
		Object userName = null;
		
		try {
			userName = JPA.em().createNativeQuery(sql).setParameter(1, id).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据id获得用户名时，根据用户ID查询用户名时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return null;
		}
		
		error.code = 0;
		
		return userName == null ? "*" : userName.toString();
	}
	
	/**
	 * 根据id获得用户
	 * @return
	 */
	public static t_users queryUserByUserId(long userId, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_users(name, mobile, email) from t_users where id = ?";
		t_users user = null;
		
		try {
			user = t_users.find(sql, userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询用户时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";
			
			return null;
		}
		
		error.code = 0;
		
		return user;
	}
	
	/**
	 * 根据id获得用户
	 * @return
	 */
	public static t_users queryUser2ByUserId(long userId, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_users(reality_name, id_number, ips_acct_no, ips_bid_auth_no) from t_users where id = ?";
		t_users user = null;
		
		try {
			user = t_users.find(sql, userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询用户时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";
			
			return null;
		}
		
		error.code = 0;
		
		return user;
	}
	
	/**
	 * 根据用户名获得用户
	 * @return
	 */
	public static t_users queryUserByUserName(String name, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_users(name, mobile, email) from t_users where name = ?";
		t_users user = null;
		
		try {
			user = t_users.find(sql, name).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询用户时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";
			
			return null;
		}
		
		error.code = 0;
		
		return user;
	}
	
	/**
	 * 根据email获得用户名
	 * @return
	 */
	public static t_users queryUserByEmail(String email, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_users(id, name) from t_users where email = ?";
		t_users user = null;
		
		try {
			user = t_users.find(sql, email).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据id获得用户名时，根据用户ID查询用户名时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return null;
		}
		
		error.code = 0;
		
		return user;
	}
	
	/**
	 * 根据mobile获得用户名
	 * @return
	 */
	public static t_users queryUserByMobile(String mobile, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_users(id, name) from t_users where mobile = ?";
		t_users user = null;
		
		try {
			user = t_users.find(sql, mobile).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据mobile获得用户名时，根据用户mobile查询用户名时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return null;
		}
		
		error.code = 0;
		
		return user;
	}
	

	
	/**
	 * 根据用户名查询id
	 * @return
	 */
	public static long queryIdByUserName(String userName, ErrorInfo error) {
		error.clear();
		
		String sql = "select id from t_users where name = ?";
		List<Long> ids = null;
		
		try {
			ids = t_users.find(sql, userName).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询id时，根据用户名查询用户ID时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户id失败";
			
			return error.code;
		}
		
		if(ids == null || ids.size() == 0) {
			error.code = -1;
			error.msg = "用户不存在";
			
			return error.code;
		}
		
		error.code = 0;
		
		return ids.get(0);
	}
	
	/**
	 * 根据手机号码查询id
	 * @return
	 */
	public static long queryIdByMobile(String mobile, ErrorInfo error) {
		error.clear();
		
		String sql = "select id from t_users where mobile = ?";
		List<Long> ids = null;
		
		try {
			ids = t_users.find(sql, mobile).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询id时，根据用户名查询用户ID时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户id失败";
			
			return error.code;
		}
		
		if(ids == null || ids.size() == 0) {
			error.code = -1;
			error.msg = "用户不存在";
			
			return error.code;
		}
		
		error.code = 0;
		
		return ids.get(0);
	}	

	

	
	/*********************************************省，市等的查询***********************/
	public static void queryBasic() {
		List<t_dict_ad_provinces> provinces = null;
		List<t_dict_ad_citys> citys = null;
	
		try {
			
			provinces = t_dict_ad_provinces.findAll();
			citys = t_dict_ad_citys.findAll();
			
		
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询基本信息时："+e.getMessage());

			return;
		}

		Cache.set("provinces", provinces);
		Cache.set("citys", citys);
	}
	
	/**
	 * 根据provinceId查询所有的市
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static List<t_dict_ad_citys> queryCity(long provinceId) {
		
		List<t_dict_ad_citys> citys = (List<t_dict_ad_citys>) Cache.get("citys");
		
		if(citys == null) {
			User.queryBasic();
			citys = (List<t_dict_ad_citys>) Cache.get("citys");
		}
		List<t_dict_ad_citys> cityList = new ArrayList<t_dict_ad_citys>();
		
		for(t_dict_ad_citys city : citys) {
			
			if(city.province_id == provinceId) {
				cityList.add(city);
			}
		}
		
		return cityList;
	}
	
	/**
	 * 根据市查询省
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static int queryProvince(long cityId) {
		
		t_dict_ad_citys city = t_dict_ad_citys.findById(cityId);
		
		if(city == null) {
			return -1;
		}
		
		return city.province_id;
	}
	
	/**
	 * 获取App调用的用户信息
	 * @param id
	 * @return
	 */
	public static User currAppUser(String id){
		
		User user = (User) Cache.get("userId_" + id);
		
		if(user == null) {
			
			return null;
		}
		
		return user;
	}
	
	/**
	 * 获得当前缓存中的user
	 * @return
	 */
	public static User currUser() {
		/*定时任务下，无法获取当前登录用户*/
		if (Session.current() == null) {
			return null;
		}
		
		String encryString = Session.current().getId();
		if(StringUtils.isBlank(encryString)) {
			return null;
		}
		
		String userId = Cache.get("front_"+encryString) + "";	
		if(StringUtils.isBlank(userId)){
			
			return null;
		}
		User user = (User)Cache.get("userId_"+userId);
		if(user == null) {
			
			return null;
		}	
		return user;
	}
	
	/**
	 * 添加cookie和cache
	 * @param user
	 */
	public static void setCurrUser(User user) {		
		if(user == null){
			return;
		}		
		/*定时任务下，无法设置当前登录用户*/
		if (Session.current() == null) {
			return;
		}		
		String encryString = Session.current().getId();
		//设置用户凭证为10分钟
		Cache.set("front_"+encryString, user.id, Constants.CACHE_TIME_MINUS_10);
		//设置用户登录成功信息为10分钟
		Cache.set("userId_"+user.id, user, Constants.CACHE_TIME_MINUS_10);
	}
	
	/**
	 * 设置当前用户
	 * @param userId
	 */
	public static void setCurrUser(long userId) {
		if (userId < 1) {
			return;
		}
		
		User user = new User();
		user.id = userId;
		User.setCurrUser(user);
	}
	
	/**
	 * 退出时清除cookie和缓存
	 */
	public static void removeCurrUser() {
		String encryString = Session.current().getId();
		String userId = Cache.get("front_"+encryString) + "";
		Cache.delete("front_"+encryString);
		Cache.delete("userId_"+userId);
	}
	

	
	
	/**
	 * 通过名字查询用户
	 * @param name
	 * @param error
	 * @return
	 */
	public  t_users queryUserByName(String name, ErrorInfo error) {
		error.clear();
		t_users user = null;
		
		try {
			user = t_users.find("name = ?", name).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询用户失败";
			
			return null;
		}
		
		if(user == null ) {
			error.code = -2;
			error.msg = "账号为"+name+"的用户不存在";
			
			return null;
		}
		this.setInformation(user);
		error.code = 0;
		error.msg = "查询管理员成功";
		
		return user;
	}	
	/**
	 * 查询注册会员总数
	 * @param error
	 * @return
	 */
	public static long queryTotalRegisterUserCount(ErrorInfo error) {
		error.clear();
		
		long count = 0;

		try {
			count = t_users.count();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询注册会员总数失败";
			
			return -1;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询今日注册会员总数
	 * @param error
	 * @return
	 */
	public static long queryTodayRegisterUserCount(ErrorInfo error) {
		error.clear();
		
		long count = 0;

		try {
			count = t_users.count("DATEDIFF(NOW(), time) < 1");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询今日注册会员数失败";
			
			return -1;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询在线会员数量
	 * @return
	 */
	public static long queryOnlineUserNum() {
		return CacheManager.getCacheSize("online_user_");
	}


	
	/**
	 * 用户待收金额
	 * @param userId
	 * @param error
	 * @return
	 */
	public static Double receivingAmount(long userId,ErrorInfo error){
		
		Double count = 0.0;
		
		String sql = "select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where user_id = ? and status in (-1,-2)";
		
		try {
			count = t_bill_invests.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == count){
			count = 0.0;
		}
		error.code = 1;
		return count;
	}

	
	/**
	 * 更新用户信息
	 * @param error
	 */
	public void updateIpsAcctNo(long userId, String realName,String idNumber, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_users set reality_name=?,id_number=? where id= ?";
		int rows = 0;
		
		try{
			rows = JpaHelper.execute(sql, realName,idNumber, userId).executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("添加IPS账号时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加IPS失败";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}		
		DealDetail.userEvent(userId, UserEvent.IPS_ACCT_NO, "开户成功", error);		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}		
		User user = new User();
		user.id = userId;
		user.realityName = this.realityName;
		user.idNumber = this.idNumber;	
		setCurrUser(user);		
		error.code = 0;
		error.msg = "开户短信发送成功！";
	}
	/**
	 * 更新企业IPS账号信息
	 * @param error
	 */
	public void updateCompanyIpsAcctNo(long userId, String pIpsAcctNo,String auditStat,String auditDesc,String openBankId,String cardId,  ErrorInfo error) {
		error.clear();
		
		String sql = "update t_users set ips_acct_no = ?,auditStat = ?,auditDesc = ?,openBankId =?,cardId = ? where id= ?";
		int rows = 0;
		
		try{
			rows = JpaHelper.execute(sql, pIpsAcctNo,auditStat,auditDesc,openBankId,cardId, userId).executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("添加企业IPS账号时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加企业IPSIPS失败";
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.userEvent(userId, UserEvent.IPS_ACCT_NO, "企业开户成功", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			return ;
		}
		error.code = 0;
		error.msg = "企业开户成功！";
	}

	/**
	 * 修改手机(原手机为空)
	 * @param id
	 * @param email
	 * @param error
	 */
	public static void updateMobile(long id,String mobile,ErrorInfo error){
		
		User.isMobileExist(mobile, null, error);
		if (0 < error.code) {
			return ;
		}
		
		String sql = "update t_users set mobile = ? where id = ? and mobile is null";
		Query query = JPA.em().createQuery(sql).setParameter(1, mobile).setParameter(2, id);
		int rows = 0;
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}
		if(rows != 1){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}else{
			error.code = 0;
			error.msg = "修改成功！";
		}
	}


	
	/**
	 * 根据mobile，填充数据
	 * @param mobile
	 */
	public void findUserByMobile(String mobile) {		
		t_users user = null;		
		try {
			StringBuffer sql = new StringBuffer();
			List<t_users> t_users_list = null;
			sql.append(" select * from t_users where  t_users.mobile = ?");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),t_users.class);
            query.setParameter(1, mobile);
            query.setMaxResults(1);
            t_users_list = query.getResultList();
            if(t_users_list.size() > 0){
              user =t_users_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("login for mobile ", e);
		}
		this.setInformation(user);
	}
	public Long _id;
	public Long getId(){
		return _id;
	}
	/**
	 * 给所有属性赋值
	 * @param obj
	 */
	public void setInformation(Model model) {
		if(!(model instanceof t_users)) {
			return ;
		}		
		t_users user = (t_users) model;
		this._id = user.id ;
		this.time = user.time;
		this.photo = user.photo;
		this.realityName = user.reality_name;
		this.mobile = user.mobile;
		this.name = user.name;
		this.password = user.password;
		this.idNumber = user.id_number;
		this.postcode = user.postcode;
		this.company = user.company;
		this.companyAddress = user.company_address;
		this.officeTelephone = user.office_telephone;
		this.faxNumber = user.fax_number;
		this.balance = user.balance;
		this.role = user.role;
		this.recommendUserId = user.recommend_user_id;
		this.recommendTime = user.recommend_time;
	}
	
	
	public static String queryUserMobile(long userId) {
		String sql = "SELECT mobile FROM t_users WHERE id = ?";
		
		Object mobile = null;
		
		try {
			mobile = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			
			return "";
		}
		
		return mobile==null?"":mobile.toString();
	}
	
	
    // 查询用户的本息 
    private Map<String,Object> queryCorpusAndInterestSum(Long userId, String month,String day){
    	Map<String,Object> mapdata= new HashMap<String, Object>();
    	if(month == null && day == null){	
    		String userAoutSql = "select user_id id, sum(receive_corpus) receiveCorpus,sum(receive_interest) receiveInterest from t_bill_invests where user_id=? and status=-1 group by user_id";
    		Query query =  JPA.em().createNativeQuery(userAoutSql).setParameter(1, userId); //"2016-04-27"
    		List<Object> rows = query.getResultList();
    		if(rows!=null){
    			for (Object row : rows) {
    				Object[] cells = (Object[]) row;
    				receiveCorpus = ((BigDecimal)cells[1]).doubleValue();
    				receiveInterest = ((BigDecimal)cells[2]).doubleValue();
    				mapdata.put("receiveCorpus" , receiveCorpus);
    				mapdata.put("receiveInterest" , receiveInterest);
    			}
    		}
    	}else if(month != null && day == null){
    		String userAoutSql = "select user_id id, sum(receive_corpus) receiveCorpus,sum(receive_interest) receiveInterest from t_bill_invests where user_id=? and (status=-1 or status=0) and DATE_FORMAT(receive_time,'%Y-%m')=? group by user_id";
    		Query query =  JPA.em().createNativeQuery(userAoutSql).setParameter(1, userId).setParameter(2, month); //"2016-04"
    		List<Object> rows = query.getResultList();
    		if(rows!=null){
    			for (Object row : rows) {
    				Object[] cells = (Object[]) row;
    				receiveCorpus = ((BigDecimal)cells[1]).doubleValue();
    				receiveInterest = ((BigDecimal)cells[2]).doubleValue();
    				mapdata.put("receiveCorpus" , receiveCorpus);
    				mapdata.put("receiveInterest" , receiveInterest);
    			}
    		}
    	}else if(month == null && day != null ){
    		String userAoutSql = "select user_id id, sum(receive_corpus) receiveCorpus,sum(receive_interest) receiveInterest from t_bill_invests where user_id=? and (status=-1 or status=0) and DATE_FORMAT(receive_time,'%Y-%m-%d')=? group by user_id";
    		Query query =  JPA.em().createNativeQuery(userAoutSql).setParameter(1, userId).setParameter(2, day); //"2016-04-27"
    		List<Object> rows = query.getResultList();
    		if(rows!=null){
    			for (Object row : rows) {
    				Object[] cells = (Object[]) row;
    				receiveCorpus = ((BigDecimal)cells[1]).doubleValue();
    				receiveInterest = ((BigDecimal)cells[2]).doubleValue();
    				mapdata.put("receiveCorpus" , receiveCorpus);
    				mapdata.put("receiveInterest" , receiveInterest);
    			}
    		}
    	}else {
    		
    	}
    	return mapdata;
    }
    /**
	 * 查询交易记录
	 * @param userId
	 * @param type 类别
	 * @param beginTime 开始时间
	 * @param endTime   结束时间
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	
	public static PageBean<t_user_details> queryUserDetails(long userId,
		int currPage, int pageSize) {
		if(currPage == 0) {
			currPage = 1;
		}		
		if(pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}		
		StringBuffer sql = new StringBuffer("select * from t_user_details where ");
				
		List<Object> params = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		sql.append(" user_id = ? ");
		params.add(userId);

		
		PageBean<t_user_details> page = new PageBean<t_user_details>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.conditions = conditionMap;
		
		List<t_user_details> userDetails = new ArrayList<t_user_details>();
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),t_user_details.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            userDetails = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询交易记录时："+e.getMessage());

			return null;
		}
		
		page.page = userDetails;
		
		return page;
	}
	/**
	 * 判断是否登录
	 * 
	 */
	public static  int isLogin(){
		int data=UserEvent.LOGOUT;//退出3
		User user = currUser();
		if(user!=null){
			data=UserEvent.LOGIN;//登录2
		}
		return data;
	}
}
