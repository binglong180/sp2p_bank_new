package business;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import models.t_platform_detail_types;
import models.t_supervisor_events;
import models.t_user_details;
import models.t_user_events;
import net.sf.json.JSONObject;

import org.apache.commons.lang.time.DateUtils;

import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DataUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import constants.Constants;
import constants.DealType;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.SupervisorEvent;

/**
 * 交易记录实体类
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 下午04:07:43
 */
public class DealDetail implements Serializable{
	public long id;
	public long _id;
	public long userId;
	public Date time;
	public int operation;
	public double amount;
	/**
	 * 1 收入2 支出3 冻结4 解冻
	 */
	public int type;
	public long relationId;
	public String summary;
	public double balance;
	public int status;
	public String  mer_bill_no;
	
	public DealDetail() {
		
	}
	
	public DealDetail(long userId, int operation, double amount, long relationId,
			 double balance,String mer_bill_no,int status,  String summary) {
		this.userId = userId;
		this.operation = operation;
		this.amount= amount;
		this.relationId = relationId;
		this.balance = balance;
		this.mer_bill_no =mer_bill_no;
		this.status = status;
		this.summary = summary;
	}
	
	
	
	/**
	 * 添加交易记录
	 * @param error
	 */
	public void addDealDetail(ErrorInfo error) {
		error.clear();
		t_user_details detail = new t_user_details();
		detail.user_id = this.userId;
		detail.time = new Date();
		detail.operation = this.operation;
		detail.amount = this.amount;
		detail.relation_id = this.relationId;
		detail.summary = this.summary;
		detail.balance = this.balance;
		detail.status= this.status;
		detail.mer_bill_no = this.mer_bill_no;				
		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加交易记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加交易记录时出现异常!";
			
			return ;
		}
		error.code = 0;
	}
	
	/**
	 * 更新商户交易状态，防重复回调
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 */
	public static void updateMerDealStatus(long merBillNO, int status, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set status = ? where mer_bill_no = ? and status != ?";
		Query query = JpaHelper.execute(sql, status, merBillNO, status);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("更新商户交易状态时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易状态时出现异常!";
			
			return ;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";
			
			return ;
		}
		
		error.code = 1;
	}
	
	/**
	 * 更新商户交易手续费
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 * @param error
	 */
	public static void updateMerDealFee(long merBillNO, double fee, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set  fee = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql, fee, merBillNO);
		
		try {
			 query.executeUpdate();
		} catch (Exception e) {

			Logger.info("更新商户交易手续费时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易手续费时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新商户交易详情
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 * @param error
	 */
	public static void updateMerDatail(long merBillNO, double fee, double amount, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set balance = balance - ? - ? , fee = ? , amount = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql, fee, amount, fee, amount, merBillNO);
		
		try {
			 query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("更新商户交易详情时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易详情时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	
	/**
	 * 添加用户事件记录
	 * @param userId
	 * @param type  事件类型
	 * @param ip    ip
	 * @param descrption  描述
	 * @param error
	 */
	public static void userEvent(long userId, int type, String descrption, ErrorInfo error) {
		
		
		t_user_events userEvent = new t_user_events();
		
		userEvent.user_id = userId;
		userEvent.time = new Date();
		userEvent.type_id = type;
		userEvent.ip = DataUtil.getIp();
		userEvent.type_id = type;
		userEvent.descrption = descrption;
		if(error.code<0){
			userEvent.descrption="失败";
		}
		error.clear();
		try {
			userEvent.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加用户事件记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加用户事件记录失败!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 添加管理员事件记录
	 * @param userId
	 * @param type  事件类型
	 * @param ip    ip
	 * @param descrption  描述
	 * @param error
	 */
	public static void supervisorEvent(long supervisorId, int type, String descrption, ErrorInfo error) {
		error.clear();
		
		t_supervisor_events supervisorEvent = new t_supervisor_events();
		
		supervisorEvent.supervisor_id = supervisorId;
		supervisorEvent.time = new Date();
		supervisorEvent.type_id = type;
		supervisorEvent.ip = DataUtil.getIp();
		supervisorEvent.type_id = type;
		supervisorEvent.descrption = descrption;
		
		try {
			supervisorEvent.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加管理员事件记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加管理员事件记录失败!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	
	
	
	/**
	 * 删除操作日志
	 * @param type 0 全部、 1 一周前、 2 一月前 
	 * @param error
	 */
	public static int deleteEvents(int type, ErrorInfo error) {
		error.clear();

		if (type < 0 || type > 2) {
			error.code = -1;
			error.msg = "删除操作日志,参数有误";
			
			return error.code;
		}
		
		Date date = null;
		String description = null;
		
		if (1 == type) {
			date = DateUtils.addWeeks(new Date(), -1);
			description = "删除一周前操作日志";
		} else if (2 == type) {
			date = DateUtils.addMonths(new Date(), -1);
			description = "删除一个月前操作日志";
		} else {
			description = "删除全部操作日志";
		}
		
		try {
			if (0 == type) {
				t_supervisor_events.deleteAll();
			} else {
				t_supervisor_events.delete("time < ?", date);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_EVENT, description, error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除操作日志成功";
		
		return error.code;
	}
	
	/**
	 * 本金保障账户概要
	 * @param error
	 * @return
	 */
	public static Map<String, Object> accountSummary(ErrorInfo error) {
		String sql = "select IFNULL(SUM(case when type=1 then amount end),0) as income,"
				+ "IFNULL(SUM(case when type=2 then amount end),0) as expense,"
				+ "count(case when operation = 4 then id end) as advance,"
				+ "IFNULL(SUM(case when operation = 4 then amount end),0) as payment,"
				+ "(IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0)) as balance,"
				+ "(IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0) - "
				+ "IFNULL(SUM(case when operation=1 then amount end),0)) as real_balance from t_platform_details";
		
		Object[] obj = null;
		try{
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障账户概要时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障账户概要";
			
			return null;
		}
		
		if(obj == null) {
			return null;
		}
		
		Map<String, Object> account = new HashMap<String, Object>();
		account.put("income", Double.parseDouble(obj[0].toString()));
		account.put("expense", Double.parseDouble(obj[1].toString()));
		account.put("advance", Integer.parseInt(obj[2].toString()));
		account.put("payment", Double.parseDouble(obj[3].toString()));
		account.put("balance", Double.parseDouble(obj[4].toString()));
		account.put("real_balance", Double.parseDouble(obj[5].toString()));
		
		error.code = 0;
		
		return account;
	}
	
	

		
	/**
	 * 前台--本金保障
	 * @param error
	 * @return
	 */
	public static Map<String, Double> currTotal(ErrorInfo error) {
		error.clear();
		
		String sql = "select (IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0)) as balance, "
				+ "IFNULL(SUM(case when type=1 and TO_DAYS(time)=TO_DAYS(NOW()) then amount end),0) as income, "
				+ "IFNULL(SUM(case when type=2 and TO_DAYS(time)=TO_DAYS(NOW()) then amount end),0) as expense "
				+"from t_platform_details";
		
		Object[] obj = null;
		
		try{
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障账户概要时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障账户概要";
			
			return null;
		}
		
		if(obj == null) {
			return null;
		}
		
		Map<String, Double> account = new HashMap<String, Double>();
		account.put("balance", Double.parseDouble(obj[0].toString()));
		account.put("income", Double.parseDouble(obj[1].toString()));
		account.put("expense", Double.parseDouble(obj[2].toString()));
		
		error.code = 0;
		
		return account;
	}

	/**
	 * 及时查询用户的可用余额、冻结资金、待收金额，避免缓存/视图引起的数据误差
	 */
	public static Map<String, Double> queryUserFund(long userId, ErrorInfo error){
		error.clear();
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_FOR_DETAILS);
		sql.append(" and t_users.id = ?");
		
		JSONObject json = JPAUtil.getJSONObject(error, sql.toString(), userId);
		
		if(json == null) {
			error.code = -1;
			error.msg = "用户id不存在!";
			
			return null;
		}
		
		Map<String, Double> userDeal = new HashMap<String, Double>();
		userDeal.put("user_amount", json.getDouble("user_amount"));
		userDeal.put("user_amount2", json.getDouble("user_amount2"));
		userDeal.put("freeze", json.getDouble("freeze"));
		userDeal.put("receive_amount", json.getDouble("receive_amount"));
	
		error.code = 0;
		
		return userDeal;
	}
	
	/******************************以上为全部过程，下面为另外一种方法****************************/
	
	/**
	 * 增加用户信用积分
	 * @param userId 用户ID 
	 * @param score 分值
	 * @param error 信息值
	 */
	public static void addCreditScore(long userId, String key, ErrorInfo error){
		error.clear();
		String value = OptionKeys.getvalue(key, error);
		
		if(!NumberUtil.isNumericInt(value)){
			Logger.error("交易记录->根据常量查询积分出现错误!");
			error.msg = "增加用户信用积分失败!";
			
			return; 
		}
		
		String hql = "update t_users set credit_score = credit_score + ? where id = ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Double.parseDouble(value));
		query.setParameter(2, userId);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("交易记录->增加用户信用积分:" + e.getMessage());
			error.msg = "增加用户信用积分失败!";
		}
		
		error.msg = error.code > 0 ? "增加成功!" : "增加失败!";
	}
	
	
	/**
	 * 增加用户系统积分
	 * @param userId 用户ID 
	 * @param score 分值
	 * @param error 信息值
	 */
	public static void addSystemScore(long userId, double investAmount,String key, ErrorInfo error){
		error.clear();
		String value = OptionKeys.getvalue(key, error);
		
		if(!NumberUtil.isNumericInt(value)){
			Logger.error("交易记录->根据常量查询积分出现错误!");
			error.msg = "增加用户系统积分失败!";
			
			return; 
		}
		
		String hql = "update t_users set score = score + ? where id = ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Double.parseDouble(value)*investAmount);
		query.setParameter(2, userId);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("交易记录->增加用户系统积分:" + e.getMessage());
			error.code = -1;
			error.msg = "增加用户系统积分失败!";
			
			return;
		}
		
		error.msg = error.code > 0 ? "增加成功!" : "增加失败!";
	}
	
	/**
	 * 冻结用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @return -1:失败; >0:成功
	 */
	public static void freezeFund(long userId, double balance, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_users set balance = balance - ? where id = ? ";

		int row = 0;
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		
		
		
		try {
			 row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "冻结用户资金时异常";
			Logger.error("冻结用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();

			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "冻结用户资金失败";
			Logger.info("冻结用户资金失败，可能是账户余额不足");
			
			JPA.setRollbackOnly();
		}
	}
	
	/**
	 * 解除冻结用户资金
	 * @param userId 用户ID
	 * @param balance 费用
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void relieveFreezeFund(long userId, double balance, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_users set balance = balance + ?, freeze = freeze - ? where id = ? and freeze >= ?";
		
		int row = 0;
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, balance);
		query.setParameter(3, userId);
		query.setParameter(4, balance);

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "解除冻结用户资金时异常";
			Logger.error("解除冻结用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "解除冻结用户资失败";
			Logger.error("解除冻结用户资金失败，可能是冻结余额不足");
			
			JPA.setRollbackOnly();
		}

	}	
	/**
	 * 增加用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void addUserFund(List<t_user_details> details){
	   if(details!=null&& details.size()>0){
		   for(t_user_details detail:details){
			   String hql ="";
			  if(detail.operation==DealType.SETTLE){
				  hql = "update t_users set balance = balance + ? where id = ?";
					
			  }else if(detail.operation==DealType.SETTLEFEE){
			      hql = "update t_bank_accounts set balance = balance + ? where id = ?";
			  }
			  Query query = JPA.em().createQuery(hql);
				query.setParameter(1, detail.amount);
				query.setParameter(2, detail.user_id);
				query.executeUpdate();
		   }
	   }
		
	}
	
	/**
	 * 增加balance2
	 * @param userId 用户ID
	 * @param balance 金额
	 * @return -1:失败;  >0:成功;
	 */
	public static int addUserFund2(long userId, double balance){
		String hql = "update t_users set balance2 = balance2 + ? where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			Logger.error("增加用户资金:" + e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 减少用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void minusUserFund(long userId, double balance, ErrorInfo error){
		error.clear();
		
		int row = 0;
		String hql = "update t_users set balance = balance - ? where id = ? and balance >= ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		query.setParameter(3, balance);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "减少用户资金时数据异常";
			Logger.error("减少用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "减少用户资金失败";
			Logger.info("减少用户资金失败，可能是用于余额不足");
			
			JPA.setRollbackOnly();
			
			return ;
		}
	}
	
	/**
	 * 减少用户冻结资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void minusUserFreezeFund(long userId, double freeze, ErrorInfo error){
		error.clear();
		
		int row = 0;
		String hql = "update t_users set freeze = freeze - ? where id = ? and freeze >= ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, freeze);
		query.setParameter(2, userId);
		query.setParameter(3, freeze);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "减少用户冻结资金时异常";
			Logger.error("减少用户冻结资金时:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "减少用户冻结资金失败";
			Logger.info("减少用户冻结资金失败,可能是用户冻结余额不足");
			
			JPA.setRollbackOnly();
		}
	}
	
	
	
}
