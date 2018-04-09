package business.newr;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_dict_bid_repayment_types;
import models.t_invests;
import models.t_system_options;
import models.t_user_bank_accounts;
import models.newr.t_ticket;
import models.newr.t_user_automatic_invest_options;
import models.newr.t_users;
import models.newr.v_newr_invest_records;
import models.newr.v_recommedFeeList;
import models.newr.v_recommendFeeDetail;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import utils.Arith;
import utils.CnUpperCaser;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.Security;
import utils.ServiceFee;
import business.DealDetail;
import business.StationLetter;
import business.TemplateEmail;
import business.TemplatePact;
import business.TemplateSms;
import business.TemplateStation;

import com.shove.Convert;

import constants.Constants;
import constants.DealType;
import constants.IPSConstants.IPSDealStatus;
import constants.Templets;
import constants.UserEvent;

/**
 * 投资业务实体类
 * 
 * @author lwh
 * @version 6.0
 * @created 2014-3-27 下午03:31:06
 */
public class Invest implements Serializable{
	private long _id;
	public long id;
	public String merBillNo;
	public String ipsBillNo;
	public long userId;
	public String userIdSign; // 加密ID
	public Date time;
	public long bidId;
	public double amount;
	public double fee;
	public String transferStatus;
	public String status;
	public long transfersId;
	public boolean isAutomaticInvest;
	
	
	public User user;
	public Bid bid;
	
	public String statusStr;//状态字符
	
	public String getStatusStr() {
		switch(Integer.valueOf(this.status)){
		case -10:
		case -5:
		case -4:
		case -3:
		case -2:
		case -1:
		case 0:
		case 2:
		case 1:
			this.statusStr="投标中";
			break;
		case 3:
			this.statusStr="已满标";
			break;
		case 4:
		case 5:
			this.statusStr="投资结束";
			break;
		case 6:
			this.statusStr="转出到账";
			break;
		case 31:
			this.statusStr="投资中";
			break;
		case 41:
			this.statusStr="预热中";
			break;
		}
		return statusStr;
	}

	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}

	/**
	 * 通过投资ID查询投资信息
	 */
	public static Map queryInvestById(long id){
		ErrorInfo error =new ErrorInfo();
		t_invests invests = t_invests.findById(id);
		
		String query="where user_id=?";
		
	
	
		
		t_bids bid = t_bids.findById(invests.bid_id);
		
		t_ticket ticket = t_ticket.findById(bid.product_id);
		t_dict_bid_repayment_types repaymentTypes=t_dict_bid_repayment_types.findById(bid.repayment_type_id);
		Map map=new HashMap();
		UserBankAccounts account=UserBankAccounts.queryUserAllBankAccount(invests.user_id).get(0);
		map.put("account", account);
		map.put("invests", invests);
		map.put("bid",bid);
		map.put("repaymentTypes", repaymentTypes);
		map.put("ticket", ticket);
		return map;
		
	}
	
	
	
	/**
	 * 获取加密投资者ID
	 * @return
	 */
	public String getUserIdSign() {
		return Security.addSign(this.userId, Constants.USER_ID_SIGN);
	}

	public void setUserId(long userId) {
		this.userId = userId;
		this.user = new User();
		this.user.id = userId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
		this.bid = new Bid();
		this.bid.id = bidId;
	}


	public long getId() {
		return _id;
	}
	public void setId(long id) {
		
		t_invests invests= null;
		try {
			 invests = t_invests.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
		}
		
		if(null==invests){
			this._id = -1;
			
			return;
		}
		this._id = invests.id;
		this.userId = invests.user_id;
		this.time = invests.time;
		this.bidId = invests.bid_id;
		this.amount = invests.amount;
		this.fee = invests.fee;
		this.transferStatus = invests.status;
		this.merBillNo = invests.mer_bill_no;
		
		if(invests.status == "0"){
			this.status = "正常";
		}
		
		if(invests.status == "-1"){
			this.status = "已转让出";
		}
		
		if(invests.status == "0"){
			this.status = "转让中";
		}
	}
		
	public Invest() {
		
	}
	/**
	 *针对某个标的投标记录
	 * @return
	 */
	public static PageBean<t_invests> queryBidInvestRecords(long bidId,ErrorInfo error){
		
		PageBean<t_invests> pageBean = new PageBean<t_invests>();
		List<t_invests> list = new ArrayList<t_invests>();
		List<Object> params = new ArrayList<Object>();
		params.add(bidId);	  
		try {
	       list = t_invests.find("bid_id = ? order by time desc", bidId).fetch();
            pageBean.totalCount = (int) t_invests.count("bid_id = ? order by time desc", bidId);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return pageBean;
		}
		
		pageBean.page = list;
		error.code = 1;
		return pageBean;
		
	}
	
	public static PageBean<models.newr.v_newr_invest_records> queryBidInvestRecords(int currPage, int pageSize,long userId,ErrorInfo error){
		
		PageBean<models.newr.v_newr_invest_records> pageBean = new PageBean<models.newr.v_newr_invest_records>();
		List<models.newr.v_newr_invest_records> list = new ArrayList<models.newr.v_newr_invest_records>();
		if(currPage<=0) {
 			currPage = Constants.ONE;
 		}	
 		if(pageSize<=0) {
 			pageSize = Constants.TEN;
 		}
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		StringBuffer sql = new StringBuffer("");
		sql.append("select `t_invests`.`id` AS `id`,`t_bids`.`title` AS `title`,`t_bids`.`id` AS `bidId`,`t_invests`.`time` AS `time`,`t_invests`.`amount` AS `invest_amount`,`t_bids`.`status` AS `status`,(select (sum(`receive_corpus`)+sum(`receive_interest`)) AS `unreceive` from `t_bill_invests` where `t_invests`.`id` = `t_bill_invests`.`invest_id` and `t_bids`.`id`=`t_bill_invests`.`bid_id` and `t_bill_invests`.`status`=-(1)) AS `unreceive`,(select (sum(`receive_corpus`)+sum(`receive_interest`)) AS `unreceive` from `t_bill_invests` where `t_invests`.`id` = `t_bill_invests`.`invest_id` and `t_bids`.`id`=`t_bill_invests`.`bid_id` and `t_bill_invests`.`status`=0) AS `receive` from `t_invests` left join `t_bids` on`t_bids`.`id` = `t_invests`.`bid_id` left join `t_users` on `t_invests`.`user_id` = `t_users`.`id` where 1=1");
		sql.append(" and t_invests.user_id = ?");
		sql.append(" order by time desc");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_newr_invest_records.class);
            query.setParameter(1,userId);
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = (List<models.newr.v_newr_invest_records>)query.getResultList();
            
            pageBean.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return pageBean;
		}
		
		pageBean.page = list;
		error.code = 1;
		return pageBean;
		
	}
	/**
	 * 	App投资记录查询一次18条
	 */
	public static PageBean<models.newr.v_newr_invest_records> queryBidInvestRecordsApp(int currPage, int pageSize,long userId,ErrorInfo error){
		
		PageBean<models.newr.v_newr_invest_records> pageBean = new PageBean<models.newr.v_newr_invest_records>();
		List<models.newr.v_newr_invest_records> list = new ArrayList<models.newr.v_newr_invest_records>();
		if(currPage<=0) {
 			currPage = Constants.ONE;
 		}	
 		if(pageSize<=0) {
 			pageSize = Constants.APP_PAGESIZE;
 		}
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		StringBuffer sql = new StringBuffer("");
		sql.append("select `t_invests`.`id` AS `id`,`t_bids`.`title` AS `title`,`t_bids`.`id` AS `bidId`,`t_invests`.`time` AS `time`,`t_invests`.`amount` AS `invest_amount`,`t_bids`.`status` AS `status`,(select (sum(`receive_corpus`)+sum(`receive_interest`)) AS `unreceive` from `t_bill_invests` where `t_invests`.`id` = `t_bill_invests`.`invest_id` and `t_bids`.`id`=`t_bill_invests`.`bid_id` and `t_bill_invests`.`status`=-(1)) AS `unreceive`,(select (sum(`receive_corpus`)+sum(`receive_interest`)) AS `unreceive` from `t_bill_invests` where `t_invests`.`id` = `t_bill_invests`.`invest_id` and `t_bids`.`id`=`t_bill_invests`.`bid_id` and `t_bill_invests`.`status`=0) AS `receive` from `t_invests` left join `t_bids` on`t_bids`.`id` = `t_invests`.`bid_id` left join `t_users` on `t_invests`.`user_id` = `t_users`.`id` where 1=1");
		sql.append(" and t_invests.user_id = ?");
		sql.append(" order by time desc");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_newr_invest_records.class);
            query.setParameter(1,userId);
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = (List<models.newr.v_newr_invest_records>)query.getResultList();
            
            pageBean.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return pageBean;
		}
		
		pageBean.page = list;
		error.code = 1;
		return pageBean;
		
	}

	
	
	
	
	
	
	
	/**
	 * 查询理财交易总数
	 * @param error
	 * @return
	 */
	public static long queryTotalInvestCount(ErrorInfo error) {
		error.clear();

		long count = 0;
		Object transferCount = 0;
		Object investCount = 0;
		
		String sqlTransferCount = "SELECT COUNT(1) FROM t_invest_transfers WHERE status = ?";
		String sqlInvestCount = "SELECT COUNT(1) FROM t_invests LEFT JOIN t_bids ON t_invests.bid_id = t_bids.id WHERE t_bids.status IN (?, ?, ?, ?, ?, ?)";
		
		EntityManager em = JPA.em();

		
		try {
			investCount = em.createNativeQuery(sqlInvestCount)
					.setParameter(1, Constants.BID_ADVANCE_LOAN)
					.setParameter(2, Constants.BID_FUNDRAISE)
					.setParameter(3, Constants.BID_EAIT_LOAN)
					.setParameter(4, Constants.BID_REPAYMENT)
					.setParameter(5, Constants.BID_REPAYMENTS)
					.setParameter(6, Constants.BID_COMPENSATE_REPAYMENT)
					.getSingleResult();
		} catch (Exception e) {
			Logger.info("查询理财交易总数（投资）时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询理财交易总数失败";
			
			return -1;
		}
		
		count = Convert.strToLong(transferCount.toString(), 0) + Convert.strToLong(investCount.toString(), 0);
		
		error.code = 1;
		return count;
	}
	
	
	public static double getUserBalance(long userId){
		
		double balance = 0;
		
		try {
			balance = t_users.find(" select balance from t_users where id = ? ", userId).first();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return balance;
	}

	
	public long investUserId;
	public double investAmount;
	
	/**
	 * 查询对应标的的所有投资者以及投资金额
	 * @param bidId
	 * @return
	 */
	public static List<Invest> queryAllInvestUser(long bidId) {
		List<Map<Long, Object>> tamounts = null;
		List<Invest> amounts = new ArrayList<Invest>();

		String hql = "select new Map(i.user_id as userId, i.amount as amount, i.mer_bill_no as mer_bill_no, i.ips_bill_no as ips_bill_no, i.fee as fee) from t_invests i where i.bid_id=? and i.status='2' order by time";

		try {
			tamounts = t_invests.find(hql, bidId).fetch();
		} catch (Exception e) {
			Logger.error("查询对应标的的所有投资者以及投资金额:" + e.getMessage());

			return null;
		}
		
		if(null == tamounts) 
			return null;
		
		if(tamounts.size() == 0){
			return amounts;
		}
		
		Invest invest = null;

		for (Map<Long, Object> map : tamounts) {
			invest = new Invest();
			invest.investUserId = Long.parseLong(map.get("userId") + "");
			invest.investAmount = Double.parseDouble(map.get("amount") + "");
			invest.merBillNo = (String) map.get("mer_bill_no");
			invest.ipsBillNo = (String) map.get("ips_bill_no");
			invest.fee = Convert.strToDouble(""+map.get("fee"), 0);			
			amounts.add(invest);
		}

		return amounts;
	}
	
	/**
	 * 查询对应标的的所有投资者以及投资金额
	 * @param bidId
	 * @return
	 */
	public static List<Invest> queryAllInvestUserForInvitation(long bidId) {
		List<Map<Long, Object>> tamounts = null;
		List<Invest> amounts = new ArrayList<Invest>();
		
		String hql = "select new Map(i.user_id as userId, i.amount as amount, i.mer_bill_no as mer_bill_no, i.ips_bill_no as ips_bill_no, i.fee as fee) from t_invests i where i.bid_id=?  order by time";
		
		try {
			tamounts = t_invests.find(hql, bidId).fetch();
		} catch (Exception e) {
			Logger.error("查询对应标的的所有投资者以及投资金额:" + e.getMessage());
			
			return null;
		}
		
		if(null == tamounts) 
			return null;
		
		if(tamounts.size() == 0){
			return amounts;
		}
		
		Invest invest = null;
		
		for (Map<Long, Object> map : tamounts) {
			invest = new Invest();
			
			invest.investUserId = Long.parseLong(map.get("userId") + "");
			invest.investAmount = Double.parseDouble(map.get("amount") + "");
			invest.merBillNo = (String) map.get("mer_bill_no");
			invest.ipsBillNo = (String) map.get("ips_bill_no");
			invest.fee = Convert.strToDouble(""+map.get("fee"), 0);
			
			amounts.add(invest);
		}
		
		return amounts;
	}
	
	/**
	 * 查询投标信息
	 * @param bidId
	 * @return
	 */
	public static List<Map<Object, Object>> queryInvestInfo(long bidId, ErrorInfo error) {
		error.clear();
		String sql = "select new Map(u.id as userId, u.ips_acct_no as ipsAcctNo, i.amount as amount) from t_invests i, t_users u where i.bid_id=? and u.id = i.user_id order by i.time";

		try {
			return t_invests.find(sql, bidId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
	}

	/**
	 * 获取t_bids表特定标版本号
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int getBidVersion(long bidId,ErrorInfo error){
		
		int version = 0;
		String sql = "select version from t_bids where id = ?";
		
		try {
			version = t_bids.find(sql, bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -1;
			return -1;
		}
		error.code = 1;
		return version ;
	}
	
	/**
	 * 已投总额增加,投标进度增加
	 * @param bidId
	 * @param amount
	 * @param schedule
	 * @param error
	 * @return
	 */
	public static void updateBidschedule(long bidId,double amount, double schedule,ErrorInfo error){
		EntityManager em = JPA.em();
		int rows = 0;
		
		try {
			rows = em.createQuery("update t_bids set loan_schedule=?,has_invested_amount= has_invested_amount + ? where id=? and amount >= has_invested_amount + ?")
			.setParameter(1, schedule).setParameter(2, amount).setParameter(3, bidId).setParameter(4, amount).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("更新已投总额,投标进度时,%s", e.getMessage());
			
			error.code = -1;
			error.msg = "更新已投总额,投标进度异常";
			
			
			JPA.setRollbackOnly();
			return ;
		}
		
		if(rows == 0){
			Logger.info("更新已投总额,投标进度时,已满标");

			error.code = Constants.OVERBIDAMOUNT;
			error.msg = "超额投资，请解冻投资金额";
			
			JPA.setRollbackOnly();
			return ;
		}
		
		
		error.code = 1;
		
		return ;
	}
	
	/**
	 * 更新借款标满标时间
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int updateBidExpiretime(long bidId, ErrorInfo error){
		
		Bid bid = new Bid();
		bid.id = bidId;	
		EntityManager em = JPA.em();
		try {
			int rows = em.createQuery("update t_bids set invest_expire_time = ? where id=?").setParameter(1, new Date())
																								 .setParameter(2, bidId).executeUpdate();		
			if(rows == 0){
				JPA.setRollbackOnly();
				error.code = -1;
				return error.code;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}
		error.code = 1;
		return 1;
	}
	
	
	/**
	 * 根据投资ID获取对应bidId,userId
	 * @param investId
	 * @param error
	 * @return
	 */
	public static t_invests queryUserAndBid(long investId){
		t_invests invest = null;
		try {
			invest = t_invests.find("select new t_invests(user_id,bid_id,ips_bill_no,amount) from t_invests where id = ? and status = '0' ", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return invest;
	}
	
	/**
	 * 即时查询借款标对象
	 * @param bidId
	 * @return
	 */
	public static Map<String,String> bidMap(long bidId,ErrorInfo error){
		error.clear();
		
		String sql = "select t_bids.id, title, min_invest_amount, has_settle_amount, amount, status, " 
				+ "invest_expire_time, has_invested_amount, user_id, product_id, version,"
				+ "period_unit, period, apr, td.description from t_bids,t_dict_bid_repayment_types td "
				+ "where t_bids.id = ? AND td.id = repayment_type_id limit 1";
		
		Object [] obj = null;
		
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();
					
		} catch (Exception e) {
			
			e.printStackTrace();
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -11;

			JPA.setRollbackOnly();
		}
		
		if(obj == null || obj.length == 0) {
			error.msg = "标的信息不存在";
			error.code = -11;
			
			return null; 
		}
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("id", obj[0]+"");
		map.put("title", obj[1]+"");
		map.put("min_invest_amount", obj[2]+"");
		map.put("has_settle_amount", obj[3]+"");
		map.put("amount", obj[4]+"");
		map.put("status", obj[5]+"");
		map.put("invest_expire_time", obj[6]+"");
		map.put("has_invested_amount", obj[7]+"");
		map.put("user_id", obj[8]+"");
		map.put("product_id", obj[9]+"");
		map.put("version", obj[10]+"");
		map.put("period_unit", obj[11]+"");
		map.put("period", obj[12]+"");
		map.put("apr", DataUtil.formatString(obj[13]));
		map.put("description", obj[14]+"");
		error.code = 1;
		return map;
	}
	/**
	 * 投标操作
	 * @param userId 投资人id
	 * @param bidId 标的id
	 * @param investTotal 投资金额
	 * @param dealpwdStr 交易密码
	 * @param isAuto 是否为自动投标
	 * @param error
	 */
	public static void invest(long userId, long bidId, int investTotal,  int client, ErrorInfo error) {
		error.clear();	
		User user = new User();
    	user.id = userId;
		if (investTotal <= 0) {
			error.msg = "对不起！请输入正确格式的数字!";
			error.code = -10;

			return;
		}
		Map<String, String> bid = bidMap(bidId, error);

		if (error.code < 0) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;
			return;
		}
		
		if(Convert.strToInt(bid.get("status"), IPSDealStatus.NORMAL) != IPSDealStatus.BID_END_HANDING){ 
			error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
			error.code = -3;
			return;
		}
		double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
		double amount = Double.parseDouble(bid.get("amount") + "");
		int status = Integer.parseInt(bid.get("status") + "");
		
		Date invest_expire_time = DateUtil.strToDate(bid.get("invest_expire_time").toString());
		
		double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");
		long bidUserId = Long.parseLong(bid.get("user_id") + "");// 借款者
		if (amount <= hasInvestedAmount) {
			error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
			error.code = -2;
			return;
		}
	

		if (amount - hasInvestedAmount >= minInvestAmount) {
			if (investTotal < minInvestAmount) {
				error.msg = "对不起！您最少要投" + minInvestAmount + "元";
				error.code = -3;
				return;
			}
		 } else {
			if (investTotal < amount - hasInvestedAmount) {
				double money = amount - hasInvestedAmount;
				error.msg = "对不起！您最少要投" + money + "元";
				error.code = -4;
				return;
			 }
		  }
		 if (investTotal > (amount - hasInvestedAmount)) {
				double money = amount - hasInvestedAmount;
				error.msg = "对不起！您的投资金额超过了该标的剩余金额,您最多只能投" + money + "元！";
				error.code = -6;
				return;
			}
		
		if (error.code < 0) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return;
		}
		
	}
	/**
	 *  投标操作(写入数据库 )
	 * @param user1 投资人
	 * @param Bid 标的id
	 * @param investTotal  此次投资金额
	 * @param pMerBillNo投资流水号，第三方返回的
	 * @param pFee 理财管理费
	 * @param client pc/app/微信/手机网站
	 * @param award 投资奖励
	 * @param bid_fee 借款管理费分摊到每次投资的费用
	 * @param error
	 * 有并发问题
	 */
	public static void doInvest(t_users user1, long bidId, double investTotal, String mer_bill_no,Long investId, ErrorInfo error) {
		error.clear();		
		Map<String, String> bid = Invest.bidMap(bidId, error);
		if(error.code < 0){			
			return;
		}		
		long userId = user1.id;
		double amount = Double.parseDouble(bid.get("amount") + "");
		double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");		
		double schedule = Arith.divDown(hasInvestedAmount + investTotal, amount, 4) * 100;//
		/* 已投总额增加,投标进度增加, 超标控制 */
		updateBidschedule(bidId, investTotal, schedule, error); 
		if(error.code < 0){  //超标或更新失败
			return;
		}
		/* 满标 */
		if (amount == (hasInvestedAmount + investTotal)) {

			// 更新满标时间
			int resulta = updateBidExpiretime(bidId, error);

			if (resulta < 0) {
				error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
				error.code = -8;
				JPA.setRollbackOnly();
				Logger.error("-----------111Invest2032-------------");
				
				return;
			}
		}		
		/* 可用金额减少*/
		DealDetail.freezeFund(userId, investTotal, error);
		if (error.code < 0) {
			return;
		}	
		// 添加交易记录
		DealDetail dealDetail = new DealDetail(userId, DealType.INVEST, -(investTotal), 
				bidId,user1.balance, mer_bill_no,DealType.DealStatus.SECCUSS.getValue(), "投标金额");
		dealDetail.addDealDetail(error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			Logger.error("-----------111Invest2322-------------");
			return;
		}		
		// 投标添加用户事件
		DealDetail.userEvent(userId, UserEvent.INVEST, "成功投标", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			Logger.error("-----------111Invest2340-------------");
			return;
		}
         
		String sql = "update t_invests set status='2' where status = '0' and id = ? ";
		try {
			JPA.em().createNativeQuery(sql).setParameter(1, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "对不起！您此次投资失败！请您重试或联系平台管理员！";
			error.code = -10;
			JPA.setRollbackOnly();
			Logger.error("保存投资记录失败，事务回滚");
			return;
		}		
		// 发送消息
		String username = user1.name;
		String title = bid.get("title") + "";
		TemplateEmail email = TemplateEmail.getEmailTemplate(Templets.E_INVEST, error);//发送邮件		
		if(error.code < 0) {
			email = new TemplateEmail();
		}		 
		if(email.status){
			 String econtent = email.content;
			 econtent = econtent.replace("date", DateUtil.dateToString((new Date())));
			 econtent = econtent.replace("userName", username);			
			 econtent = econtent.replace("title", title);
			 econtent = econtent.replace("investAmount",  DataUtil.formatString(investTotal));			 
			 TemplateEmail.addEmailTask("", email.title, econtent);
		 }				 
		TemplateStation station = TemplateStation.getStationTemplate(Templets.M_INVEST, error);//发送站内信
		 
		if(error.code < 0) {
			station = new TemplateStation();
		}		 
		 if(station.status){
			 String stationContent = station.content;
			 stationContent = stationContent.replace("date", DateUtil.dateToString((new Date())));
			 stationContent = stationContent.replace("userName", username);			
			 stationContent = stationContent.replace("title", title);
			 stationContent = stationContent.replace("investAmount",  DataUtil.formatString(investTotal));
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = userId;
			 letter.title = station.title;
			 letter.content = stationContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }
		 String unit = bid.get("period_unit");
		 String date = null;
		 if ("-1".equals(unit)) {
			 date="年";
		 }else if("0".equals(unit)){
			 date="月";
		 }else if("1".equals(unit)){
			 date="日";
		 }
		 String period = bid.get("period");
		 String apr = bid.get("apr");
		 String description = bid.get("description");
		 //尊敬的userName: 恭喜您投资成功！投资金额￥investAmount元，投资期限period date，年化收益率apr%，还款方式description.
		 TemplateSms sms = TemplateSms.getSmsTemplate(Templets.S_TENDER, error);//发送短信
		 if(error.code < 0) {
			 sms = new TemplateSms();
		 }
		 if(sms.status && StringUtils.isNotBlank(user1.mobile)){
			 String smscontent = sms.content;
			 smscontent = smscontent.replace("userName", username);			
			 smscontent = smscontent.replace("investAmount",  DataUtil.formatString(investTotal));
			 smscontent = smscontent.replace("period", period);
			 smscontent = smscontent.replace("date", date);
			 smscontent = smscontent.replace("apr", apr);
			 smscontent = smscontent.replace("description", description);
			 TemplateSms.addSmsTask(user1.mobile, smscontent);
		 }				
		error.msg = "投资成功！";
		error.code = 1;		
	}
	
	/**
	 * 获取用户减掉预留金额后的可用金额
	 * @param userId
	 * @param remandAmount
	 * @return
	 */
	public static double queryAutoUserBalance(long userId, double remandAmount){
		
		Double balance = null;
		String sql = "select balance from t_users where id = ?";
		
		try {
			balance = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		if(null == balance)
			return 0;
		
		return balance < remandAmount ? 0: balance - remandAmount;
	}
	
	/**
	 * 按时间倒序排序查出所有开启了投标机器人的用户ID
	 * @return
	 */
	public static List<Long> queryAllAutoUser(){
		List<Long> list = null;
		
		try {
			 list = t_user_automatic_invest_options.find("select user_id from t_user_automatic_invest_options where status = 1  order by wait_time asc").fetch();
		} catch (Exception e) {
			return null;
		}
		
		return list;
	}
	
	
	
	/**
	 * 将用户排到自动投标队尾
	 * @param user_id
	 */
	public static void updateUserAutoBidTime(long user_id){
		String sql = "update t_user_automatic_invest_options set wait_time = ? where user_id = ?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, new Date());
		query.setParameter(2, user_id);
		
		try {
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 扣除保留金额后，计算最后投标金额
	 * @param bidAmount
	 * @param schedule
	 * @param amount
	 * @param hasInvestedAmount
	 * @return
	 */
	public static double calculateBidAmount(double minAmount,double maxAmount, double schedule,double amount,double hasInvestedAmount,double balance) {
		double baseline = 0.0;
	   if(schedule<90){
		   baseline = amount*0.9-hasInvestedAmount;
		   if(baseline<minAmount){
			   return 0;
		   }else if(baseline>=minAmount && baseline<= maxAmount){
			   if(balance<minAmount){
				   return 0;
			   }else if(balance>=minAmount && balance<= maxAmount){
				    if(balance>=baseline){
				    	 return baseline;
				    }else{
				    	return balance;
				    }
			   }
			  
		   }else {
			   if(balance<minAmount){
				   return 0;
			   }else if(minAmount<=balance && balance<=maxAmount){
				  return balance; 
			   }else{
				   return maxAmount;
			   }
		   }
	   }
	   return (int) 0;
	}
	
	
	/**
	 * 计算自动投标份数
	 * @param amount
	 * @param averageAmount
	 * @return
	 */
	public static int calculateFinalInvestAmount(double amount,double averageAmount){
		int temp = 0;
		temp = (int) (amount/averageAmount);
		return temp;
	}
	

	
	
	
	public Map<String,Object> queryParamByBidId(long bidId){
		String sql="select new Map(user_id as user_id,amount as amount,min_invest_amount as min_invest_amount,average_invest_amount as average_invest_amount," +
				"has_invested_amount as has_invested_amount) from t_bids where id=?";
    	return t_bids.find(sql, bidId).first();
	}
	
	
	
	/**
	 * 查询过期的机器人并关闭。
	 * @return
	 */
	public static void closeAutoUser(){
		EntityManager em = JPA.em();
		
		//查询过期的机器人的用户ids
		String sql = "select user_id from t_user_automatic_invest_options where status = 1 and (case when valid_type = 0 then timestampdiff(DAY,time,NOW()) else timestampdiff(MONTH,time,NOW()) END) > valid_date";
		List<Long> userIds = t_user_automatic_invest_options.find(sql).fetch(); 

		if(userIds == null || userIds.size() < 1){
			return;
		}
		
		String strUserIds = userIds.toString();
		strUserIds = strUserIds.substring(1, strUserIds.length()-1);

		try {
			em.createQuery("update t_user_automatic_invest_options set status = 0 where user_id in ("+ strUserIds+")").executeUpdate();
		} catch (Exception e) {
			Logger.error("关闭过期的投标机器人时(修改t_user_automatic_invest_options.status)：%s", e.getMessage());
		} 
		
		try {
			em.createNativeQuery("update t_users set ips_bid_auth_no = ? where id in ("+ strUserIds+")").setParameter(1, null).executeUpdate();
		} catch (Exception e) {
			Logger.error("关闭过期的投标机器人时(修改t_users.ips_bid_auth_no)：%s", e.getMessage());
		} 

	}
	
	
	/**
	 * 判断该借款标是否超过95%
	 * @param bidId
	 * @return
	 */
	public static boolean judgeSchedule(long bidId){
		Double schedule = 0.0;
		String sql = "select loan_schedule from t_bids where id = ? ";
		
		try {
			schedule = t_bids.find(sql, bidId).first();
		} catch (Exception e) {
			return false;
		}
		
		if(null == schedule){
			return false;
		}
		
		if(schedule >= 90){
			return false;
		}
		
		return true;
	}
	
	
	
	/**
	 * 资金托管模式下自动投标时额外条件判断
	 * @param userIdStr
	 * @param bidIdStr
	 * @return
	 */
	public static boolean additionalJudgment(long userId, long bidId){
		boolean flag = false;
		t_user_automatic_invest_options robot = null;
		try {
			robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
			if(robot==null){
				return flag;
			}
			StringBuffer condition = new StringBuffer();
			condition.append("select user_id,amount,period,min_invest_amount,status,loan_schedule,has_invested_amount,audit_time,apr from v_confirm_autoinvest_bids where id=? and "
					+ "(amount-has_invested_amount)>="+robot.min_amount+" and FIND_IN_SET(period,"+"'"+robot.period_range+"'"+")");
			Map<String,Object> map = JPAUtil.getMap(new ErrorInfo(), condition.toString(), bidId);
			if(map == null){
			    return flag;	
			}else{
				 flag =true;
				return flag;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("机器人投标查询标的出错：%s", e.getMessage());
			return false;
		}
	}

	
	
	

	/**
	 * 根据投资ID查询账单
	 * @param investId
	 * @param error
	 * @return
	 */
	public static Long queryBillByInvestId(long investId,ErrorInfo error){
		
		Long billId = 0l;
		
		try {
			billId = t_bills.find("select id from t_bill_invests where invest_id = ? ", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		error.code = 1;
		return billId;
	}
	

		
	/* 2014-11-15 */
	/**
	 * 借款合同
	 */
	public static String queryPact(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select pact from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 居间服务协议
	 */
	public static String queryIntermediaryAgreement(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select intermediary_agreement from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 保障涵
	 */
	public static String queryGuaranteeBid(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select guarantee_invest from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 生成借款合同（理财人）
	 * @param bidId
	 * @param error
	 */
	public static void creatInvestPact(long bidId,ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_INVEST;
		if(pact.is_use){
			List<Long> investIds = new ArrayList<Long>();
			String sql = "select id from t_invests where bid_id = ? and transfer_status <> -1";
			
			try {
				investIds = t_invests.find(sql, bidId).fetch();
				
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常";
				error.code = -1;
				return;
			}
			
			
			if(investIds.size() > 0){
				for(Long investId : investIds){
					String pact_no = investId + DateUtil.simple(new Date());
					creatSingleInvestPact(investId, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
					creatSingleGuaranteeInvest(investId,pact_no, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
					creatSingleIntermediaryAgreement(investId, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
				}
			}
			error.msg = "创建成功";
			error.code = 1;
			return;
		}else{
			error.msg = "平台协议未开启";
			error.code = 1;
			return;
		}
		
	}
	
	
	/**
	 * 根据单个投资记录生成理财合同
	 * @param investId
	 * @param error
	 */
	public static void creatSingleInvestPact(long investId, ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_INVEST;
		
		t_users investUser = new t_users();
		t_users bidUser = new t_users();
		t_invests invest = new t_invests();
		t_bids bid = new t_bids();
		Double amount = 0.0;
		String company_name = "";
		Double sum = 0.0;

		String hql = "select sum(receive_corpus + receive_interest) from t_bill_invests where invest_id = ?";
		String sql1 = "select _value from t_system_options where _key = ?";
		String sql2 = "select sum(repayment_corpus + repayment_interest) from t_bills where bid_id = ? ";
		try {
			invest = t_invests.findById(investId);
			bid = t_bids.findById(invest.bid_id);
			investUser = t_users.findById(invest.user_id);
			bidUser = t_users.findById(bid.user_id);
			amount = t_bill_invests.find(hql, investId).first();
			company_name = t_system_options.find(sql1, "company_name").first();
			sum = t_bills.find(sql2, bid.id).first();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(null == amount){
			amount = 0.0;
		}
		
		String no = investId + DateUtil.simple(new Date());
		StringBuffer investTable = new StringBuffer(" <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>投资人名称</td><td>投资金额(人民币)</td><td>年利率</td>" +
				"<td>投资日期</td><td>本息合计总金额(人民币)</td></tr>");
		
		investTable.append("<tr height=\"30\">");
		investTable.append("<td>" + investUser.name + "</td>");
		investTable.append("<td>￥" + invest.amount + "</td>");
		investTable.append("<td>" + bid.apr + "%</td>");
		investTable.append("<td>" + DateUtil.dateToString1(invest.time) + "</td>");
		investTable.append("<td>" + amount + "</td>");
		investTable.append("</tr></table>");
		
		String content = pact.content;
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.LOAN_NAME, bidUser.reality_name)
		.replace(Templets.ID_NUMBER, bidUser.id_number)
		.replace(Templets.PACT_NO,no)
		.replace(Templets.COMPANY_NAME,company_name)
		.replace(Templets.DATE,DateUtil.dateToString(new Date()))
		.replace(Templets.INVEST_LIST,investTable.toString());
		
		Bid bidbusiness = new Bid();
		bidbusiness.auditBid = true;
		bidbusiness.id = bid.id;		
		String repayTime = ServiceFee.repayTime(bidbusiness.periodUnit, bidbusiness.period, (int)bidbusiness.repayment.id);		           
		content = content.replace(Templets.PURPOSE_NAME,"")
		.replace(Templets.AMOUNT,bidbusiness.amount + "")
		.replace(Templets.APR,bidbusiness.apr + "%")
		.replace(Templets.PERIOD,bidbusiness.period + "")
		.replace(Templets.PERIOD_UNIT,bidbusiness.strPeriodUnit)
		.replace(Templets.REPAYMENT_NAME,bidbusiness.repayment.name)
		.replace(Templets.CAPITAL_INTEREST_SUM,sum + "")
		.replace(Templets.REPAYMENT_TIME, repayTime);

		StringBuffer billTable = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>期数</td><td>应还时间</td>" +
				"<td>应还本金</td><td>应还利息</td><td>应还本息合计</td></tr>");
		
		List<t_bill_invests> bills = new ArrayList<t_bill_invests>();
		String strsql = " invest_id = ? ";
		long periodCount = 0;
		
		try {
			bills = t_bill_invests.find(strsql, investId).fetch();
			periodCount = t_bill_invests.count(strsql, investId);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("##,##0.00");
		
		if(bills.size() > 0){
			for(t_bill_invests bill : bills){
				billTable.append("<tr height=\"30\">");
				billTable.append("<td>" + bill.periods + "/" + periodCount +"</td>");
				billTable.append("<td>" + DateUtil.dateToString1(bill.receive_time) + "</td>");
				billTable.append("<td>" + myformat.format(bill.receive_corpus)  + "</td>");
				billTable.append("<td>" + myformat.format(bill.receive_interest) + "</td>");
				String temp = myformat.format(bill.receive_corpus + bill.receive_interest);
				billTable.append("<td>" + temp + "</td>");
				billTable.append("</tr>");
			}
			
		}
		billTable.append("</table>");
		
		content = content.replace(Templets.INVEST_BILL_LIST,billTable);
	    hql = "update t_invests set pact = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	
	}
	

	
	
	/**
	 * 针对单挑投标记录创建居间服务协议（投资人）
	 * @param investId
	 * @param error
	 */
	public static void creatSingleIntermediaryAgreement(long investId,ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.INTERMEDIARY_AGREEMENT_INVEST;
		t_users investUser = new t_users();
		t_invests invest = new t_invests();
		
		try {
			invest = t_invests.findById(investId);
			investUser = t_users.findById(invest.user_id);
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		String investRealityName = investUser.reality_name == null ? "" : investUser.reality_name;
		String investRealityIdno = investUser.id_number == null ? "" : investUser.id_number;

		String content = pact.content;
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.ID_NUMBER, investRealityIdno)
		.replace(Templets.INVEST_REALITY_NAME, investRealityName)
		.replace(Templets.DATE, DateUtil.dateToString1(new Date()));
		
		String hql = "update t_invests set intermediary_agreement = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	}

		
	
	/**
	 * 针对单挑记录生成对应保障函
	 * @param investId
	 * @param error
	 */
	public static void creatSingleGuaranteeInvest(long investId,String pact_no, ErrorInfo error){
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.GUARANTEE_INVEST;
		t_users investUser = new t_users();
		t_bids bid = new t_bids();
		t_invests invest = new t_invests();
		String company_name = "";
		String sql1 = "select _value from t_system_options where _key = ?";
		try {
			invest = t_invests.findById(investId);
			bid = t_bids.findById(invest.bid_id);
			investUser = t_users.findById(invest.user_id);
			company_name = t_system_options.find(sql1, "company_name").first();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		int period = bid.period;
		int periodUnit = bid.period_unit;
		String periodStr = "";
		if(periodUnit == -1){
			periodStr = period + "年";
		}else if(periodUnit == 1){
			periodStr = "1个月";
		}else{
			periodStr = period +"个月";
		}
		
		String investRealityName = investUser.reality_name == null ? "" : investUser.reality_name;
		
		String content = pact.content;
		DecimalFormat df = new DecimalFormat();
        df.applyPattern("###.00");
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.INVEST_REALITY_NAME,investRealityName)
		.replace(Templets.CHINESE_AMOUNT, new CnUpperCaser(df.format(invest.amount)).getCnString())
		.replace(Templets.COMPANY_NAME,company_name)
		.replace(Templets.DATE,DateUtil.dateToString(new Date()))
		.replace(Templets.PACT_NO,pact_no)
		.replace(Templets.PERIOD, periodStr);
		
		String hql = "update t_invests set guarantee_invest = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	}
	
	
	/**
	 * 定时执行生成借款合同，理财合同等协议
	 */
	public static void creatBidPactJob(){
		ErrorInfo error = new ErrorInfo();
		List<Object> bidIds = new ArrayList<Object>();
		String sql = "select id from t_bids where status in (4,5) and (ISNULL(pact) and ISNULL(guarantee_bid) and ISNULL(intermediary_agreement))";
		Query query = JPA.em().createNativeQuery(sql);
		
		try {
			bidIds = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if(null == bidIds || bidIds.size() == 0)
			return ;
		
		Bid bid = null;
		long _bidId = 0;
		
		for(Object bidId : bidIds){
			bid = new Bid();
			bid.auditBidPact = true;
			_bidId = Long.parseLong(bidId.toString());
			
			try {
				bid.id = _bidId;
				
				bid.createPact();//生成借款合同
				Invest.creatInvestPact(_bidId, error);//生成理财合同
			} catch (Exception e) {
				continue ;
			}
		}
	}
	

	
	
	/**
	 * 查询前台最新三条理财资讯
	 * @return
	 */
	public static List<Map<String,String>> queryNearlyInvest(ErrorInfo error){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<t_invests> invests = new ArrayList<t_invests>();
		invests = t_invests.find(" order by id desc").fetch(Constants.NEW_FUNDRAISEING_BID);
		
		String userName = "";
		Long count = 0l;
		Double apr = 0.0;
		Map<String,String> map = null;
		for(t_invests invest : invests){
			map = new HashMap<String, String>();
			try {
				userName = t_users.find("select name from t_users where id = ? ", invest.user_id).first();
				apr = t_bids.find("select apr from t_bids where id = ? ", invest.bid_id).first();
				count = t_invests.find("select count(*) from t_invests where user_id = ? ", invest.user_id).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "查询最新理财资讯异常";
				error.code = -1;
				
				return null;
			}
			
			map = new HashMap<String, String>();
			map.put("id", invest.bid_id + "");
			map.put("userName", userName);
			map.put("count", count + "");
			map.put("apr", apr + "");
			map.put("amount", invest.amount + "");
			list.add(map);
		}
		
		return list;
	}
	/**
	 * 前台借款标条件分页查询
	 * @param currPage
	 * @param pageSize
	 * @param _apr
	 * @param _amount
	 * @param _loanSchedule
	 * @param _startDate
	 * @param _endDate
	 * @param _loanType
	 * @param _creditLevel
	 * @return
	 */
	public static PageBean<t_bids> queryAllBids(int currPage,int pageSize, ErrorInfo error){
	
		List<t_bids> bidList = new ArrayList<t_bids>();
		PageBean<t_bids> page = new PageBean<t_bids>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		EntityManager em = JPA.em();
		StringBuffer sql = new StringBuffer("select * from t_bids where `t_bids`.`status` >=1 ");
		//select * from t_bids where `t_bids`.`status` >=1  and `t_bids`.start_time<NOW()
		
		List<Object> params = new ArrayList<Object>();
			try {
				sql.append(" order by loan_schedule,t_bids.id desc ");
				System.out.println("sql-----------------"+sql.toString());
				Query query = em.createNativeQuery(sql.toString(),t_bids.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	           
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            bidList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
	
	}
	/**
	 * 根据订单流水号查询交易记录是否存在
	 * @param pMerBillNo
	 * @param error
	 */
	public static long queryIsInvest(String pMerBillNo, ErrorInfo error) {
		error.clear();
		
		String sql = "select count(1) from t_invests where mer_bill_no = ? limit 1";
		long rows = 0;
		
		try {
			rows = ((BigInteger)JPA.em().createNativeQuery(sql).setParameter(1, pMerBillNo).getSingleResult()).longValue();
		}catch(Exception e) {
			Logger.error("根据订单流水号查询交易记录是否存在时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "根据订单流水号查询交易记录是否存在时";
			
			return -1;
		}
		
		error.code = 1;
		return rows;
	}
	
	

	
	/**
	 * 修改冻结流水号
	 * @author yangxuan
	 * @date 2015-07-06
	 * @param ipsBillNo
	 */
	public static void modifyInvestIpsBillNo(String merBillNo,String ipsBillNo,ErrorInfo error){
		error.clear();
		
		try{
	
			t_invests invest = t_invests.find("mer_bill_no = ?", merBillNo).first();
			if(invest != null){
				invest.ips_bill_no = ipsBillNo;
				invest.save();
			} 
		}catch(Exception e){
			
			Logger.error("####修改冻结流水号时 : %s", e.getMessage());
			error.code = -1;
			error.msg = "系统异常,请联系管理员.";
		}
	}
	
	/**
	 * 通过BidId查询投资记录
	 * @param bidId
	 * @return
	 */
	public static List<t_invests> queryInvestByBidId(long bidId){
		List<t_invests> list = t_invests.find("bid_id = ?", bidId).fetch();
		return list;
	}
	
	/**
	 * 该投资是否是债权转让所得
	 * @param investId  投资id
	 * @return true 是 ， false 否
	 */
	public static boolean isSecondCretansfer(long investId){
		
		String sql = "select it.id from t_invest_transfers it LEFT JOIN t_invests i on it.id = i.transfers_id where status = 3 AND i.id = ?";

		Query query = JPA.em().createNativeQuery(sql).setParameter(1, investId);
		
		List list = null;
		
		try{
			list = query.getResultList();
		}catch(Exception e){
			
			return false;
		}
		
		if(list == null || list.size() <= 0 || list.get(0) == null){
			return false;
		}

		return true;
	}
	// 2016  add  满表放款
	   public static class mytask implements Runnable{
		  private  Long bidid;
		  private ErrorInfo error;
		  public mytask(Long bidid,ErrorInfo error){
			  this.bidid=bidid;
			  this.error=error;
		  }
		 @Override
		 public void run() {

			 if (JPA.local.get() == null) {            
				 EntityManager em = JPA.newEntityManager();              
				 final JPA jpa = new JPA();              
				 jpa.entityManager = em;             
				 JPA.local.set(jpa);
				 JPAUtil.transactionBegin();
		     } 
			 Bid bid = new Bid();
			 bid.auditBid=true;
			 bid.id = bidid;
			if(bid.amount==bid.hasInvestedAmount){
				// 中金 -批量结算
				//PaymentProxy.getInstance().bidAuditSucc(error, Constants.PC, bid);
			}	
			JPAUtil.transactionCommit();
		 }  
	  }

   // 2016  add  obtainReccommedFeeOutLine
   public static Map<String,Object> obtainRecommendFeeOutline(Long recommendUserId,ErrorInfo error){
	   String sql ="SELECT count(DISTINCT u.id) as recommendUserCount,sum(i.amount) as investAccount,"
			  +" sum(i.amount * b.period * 0.0001) as fee,"
			  +" (SELECT sum(ii.amount * bb.period * 0.0001) as toPay"
			  +" FROM t_users uu,t_invests ii,t_bids bb WHERE ii.user_id = uu.id"
			  +" AND ii.bid_id = bb.id and uu.recommend_user_id = ?"
			  +" and month(bb.time)=month(now())) as toPay  FROM"
			  +" t_users u LEFT JOIN t_invests i on u.id = i.user_id LEFT JOIN t_bids b on i.bid_id = b.id"  
			  +" where u.recommend_user_id = ?";
	   Map<String,Object> resultMap = JPAUtil.getMap(error, sql, recommendUserId,recommendUserId);
	   return resultMap;
   }
   // 20i6 add obtainRecommendFeeList 
   public static PageBean<v_recommedFeeList>obtainDecommendFeeList(ErrorInfo error ,int pageSize,int currPage, Long recommendUserId){
	   PageBean<v_recommedFeeList> page = new PageBean<v_recommedFeeList>();
	   page.pageSize = pageSize;
	   page.currPage = currPage;
	   List<v_recommedFeeList> recommendFeeList = new ArrayList<v_recommedFeeList>();
	  String sql = "SELECT `u`.`id` AS `id`,`u`.`reality_name` AS `name`,`u`.`time` AS `time`,"
         +" sum(`i`.`amount`) AS `investAccount`,sum(`i`.`amount` * `b`.`period` * 0.0001) AS `fee`" 
         +" FROM `t_users` `u` LEFT JOIN `t_invests` `i` on `u`.`id` = `i`.`user_id` LEFT JOIN `t_bids` `b` on `i`.`bid_id` = `b`.`id`"
         +" where `u`.`recommend_user_id` = ? group by `u`.`id`";
	  EntityManager em = JPA.em();
	  Query query = em.createNativeQuery(sql.toString(),v_recommedFeeList.class);
	  query.setParameter(1, recommendUserId);
	  query.setFirstResult((currPage - 1) * pageSize);
      query.setMaxResults(pageSize);
      recommendFeeList = query.getResultList();
      String sqlCount = "SELECT count(u.reality_name) FROM t_users u LEFT JOIN t_invests i on u.id = i.user_id LEFT JOIN t_bids b on i.bid_id = b.id where u.recommend_user_id = ?";
      Query queryCount = em.createNativeQuery(sqlCount).setParameter(1, recommendUserId);
      page.totalCount = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
      page.page = recommendFeeList;
      System.out.println(recommendFeeList.size());
	  return page;
   }
   // 2016 add obtainRecommendFeeDetail
    public static List<v_recommendFeeDetail> obtainRecommendFeeDetail(Long recommendUserId,Long userId,ErrorInfo error){
    	List<v_recommendFeeDetail> recommendFeeDetailList = new ArrayList<v_recommendFeeDetail>();
    	String sql = "SELECT i.id AS id,i.time,u.reality_name AS name,b.product_id AS title ,"
                     +" i.amount AS amount,i.amount * b.period * 0.0001 AS fee FROM "
                     +" t_users u LEFT JOIN t_invests i on u.id = i.user_id LEFT JOIN t_bids b on i.bid_id = b.id " +
                     " where u.recommend_user_id = ? and u.id = ? ";
    	EntityManager em = JPA.em();
    	Query query = em.createNativeQuery(sql.toString(), v_recommendFeeDetail.class);
    	query.setParameter(1, recommendUserId).setParameter(2, userId);
    	recommendFeeDetailList = query.getResultList();
    	return recommendFeeDetailList;
    	  
    }
 
}
