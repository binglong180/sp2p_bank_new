package controllers.supervisor.userManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.newr.t_users;
import models.newr.v_userEventsList;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import business.BackstageSet;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import business.newr.User;
import constants.Constants;
import constants.Templets;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:AllUser
 * 功能:全部会员列表
 */

public class AllUser extends SupervisorController {

	/**
	 * 所有会员列表
	 */
	public static void allUser() {
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
/*		PageBean<v_user_info> page = User.queryUserBySupervisor(name, email, beginTime, endTime, key, orderType,
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);*/
	}
	
	/**
	 * 详情
	 * @param id
	 */
	public static void detail(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			allUser();
		}
		
		User user = new User();
		user.id = id;

		render(user);

	}
	
	/**
	 * 站内信
	 */
	public static void stationLetter(String sign, String content, String title){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(content.length() > 1000) {
			error.code = -1;
			error.msg = "内容超出字数范围";
			json.put("error", error);
			renderJSON(json);
		}
		
		long receiverUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
		StationLetter message = new StationLetter();
		
		message.senderSupervisorId = Supervisor.currSupervisor().id;
		message.receiverUserId = receiverUserId;
		message.title = title;
		message.content = content;
		
		message.sendToUserBySupervisor(error); 
		
		
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 邮件
	 */
	public static void email(String email, String content){
		ErrorInfo error = new ErrorInfo();
		TemplateEmail.sendEmail(1, email, null, Templets.replaceAllHTML(content), error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 发信息
	 * @param mobile
	 * @param content
	 * @throws InterruptedException 
	 */
	public static void sendMsg(String mobile, String content){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(mobile)){
			error.code = -1;
			error.msg = "请选择正确的手机号码!";
			json.put("error", error);
			renderJSON(json);
		}
		SMSUtil.sendSMS(mobile, content, error);
		json.put("error", error);
		renderJSON(json);
		
	}
	/**
	 * 重置密码
	 */
	public static void resetPassword(String userName, String email){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(userName) || StringUtils.isBlank(email)) {
			error.code = -1;
			error.msg = "参数传入有误";
			json.put("error", error);
			
			renderJSON(json);
		}

		
		t_users user = User.queryUserByEmail(email, error);
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">点击此处重置密码</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(2, email, tEmail.title, content, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 查询用户的操作日志
	 */
	public static void queryAllUserOperationLog(){
		/*条件查询用户的操作记录name endTime beginTime descrption*/
 		Date beginTime = null;
 		Date endTime = null;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		String name=null;
 		String type_name=null;
 		String ip=null;
 		
 		if(RegexUtils.isDate(params.get("beginTime"))) {
 			beginTime = DateUtil.strDateToStartDate(params.get("beginTime"));
 		}
 		
 		if(RegexUtils.isDate(params.get("endTime"))) {
 			endTime = DateUtil.strDateToEndDate(params.get("endTime"));
 		}
 		if(StringUtils.isNotBlank(params.get("name"))){
 			name=params.get("name");
 		}
 		if(StringUtils.isNotBlank(params.get("type_name"))){
 			type_name=params.get("type_name");
 		}
 		if(StringUtils.isNotBlank(params.get("ip"))){
 			ip=params.get("ip");
 		}
 		if(NumberUtil.isNumericInt(params.get("currPage"))) {
 			currPage = Integer.parseInt(params.get("currPage"));
 		}
 		
 		if(NumberUtil.isNumericInt(params.get("pageSize"))) {
 			pageSize = Integer.parseInt(params.get("pageSize"));
 		}
 		StringBuffer sql = new StringBuffer("");
 		sql.append("select `t_user_events`.`id` AS `id`,t_users.`name` AS `name`,`t_user_events`.`time` AS `time`,`t_user_events`.`ip` AS `ip`,`t_user_events`.`descrption` AS `descrption`,t_user_event_types.`name` AS `type_name`  "
 				+" from `t_user_events` " 
 				+"LEFT JOIN t_users ON `t_user_events`.`user_id` = t_users.id  "
 				+"LEFT JOIN t_user_event_types ON `t_user_events`.`type_id`=t_user_event_types.code "
 				+" where 1=1 ");
		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> queryParams = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("name", name);
		conditionMap.put("type_name", type_name);
		conditionMap.put("ip", ip);
		conditionMap.put("beginTime", params.get("beginTime"));
		conditionMap.put("endTime", params.get("endTime"));
		
		if(StringUtils.isNotBlank(name)) {
			sql.append(" and t_users.name like ? ");
			queryParams.add("%" + name + "%");
			paramsCount.add("%"+name+"%");
		}
		if(StringUtils.isNotBlank(type_name)) {
			sql.append(" and t_user_event_types.name like ? ");
			queryParams.add("%" + type_name + "%");
			paramsCount.add("%"+type_name+"%");
		}
		if(StringUtils.isNotBlank(ip)){
			sql.append(" and t_user_events.ip like ? ");
			queryParams.add("%" + ip + "%");
			paramsCount.add("%" + ip + "%");
		}
		if(beginTime != null) {
			sql.append(" and t_user_events.time > ? ");
			queryParams.add(beginTime);
			paramsCount.add(beginTime);
		}
		
		if(endTime != null) {
			sql.append(" and t_user_events.time < ? ");
			queryParams.add(endTime);
			paramsCount.add(endTime);
		}
		sql.append(" order by t_user_events.time desc");
		List<v_userEventsList> userEventsList = new ArrayList<v_userEventsList>();
		int count = 0;
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_userEventsList.class);
            for(int n = 1; n <= queryParams.size(); n++){
                query.setParameter(n, queryParams.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            userEventsList = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);
            Logger.debug("查询所有的会员操作记录时sql："+sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员操作记录时："+e.getMessage());
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		PageBean<v_userEventsList> page = new PageBean<v_userEventsList>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page=userEventsList;
		render(page);
	}
}
