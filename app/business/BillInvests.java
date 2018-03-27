package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bill_invests;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import business.newr.Bid;
import business.newr.User;
import constants.Constants;
import constants.SQLTempletes;
public class BillInvests implements Serializable{

	public long id;
	private long _id;
	
	public long userId;
	public long bidId;
	public int period;
	public String title;
	public Date receiveTime;
	public double receiveCorpus;
	public double receiveInterest;
	public int status;
	public double overdueFine;
	public Date realReceiveTime;
	public double realReceiveCorpus;
	public double realReceiveInterest;
	
	public Bid bid;
	
	public void setId(long id){
		t_bill_invests invest = t_bill_invests.findById(id);
		
		if(invest.id < 0 || invest == null){
			this._id = -1;
			return;
		}
		
		this._id = invest.id;
		this.userId = invest.user_id;
		this.bidId = invest.bid_id;
		this.period = invest.periods;
		this.title = invest.title;
		this.receiveTime = invest.receive_time;
		this.receiveCorpus = invest.receive_corpus;
		this.receiveInterest = invest.receive_interest;
		this.status = invest.status;
		this.overdueFine = invest.overdue_fine;
		this.realReceiveCorpus = invest.real_receive_corpus;
		this.realReceiveInterest = invest.real_receive_interest;
		
		bid = new Bid();
   	    bid.id = invest.bid_id;
	}
	
	public long getId(){
		
		return this._id;
	}
	
 	/**
 	 * 我的理财账单——-历史收款情况
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static PageBean<t_bill_invests> queryMyInvestBillReceivables(long bidId,long userId, long investId, int currPage, int pageSize, ErrorInfo error){
 		error.clear();
 		
 		String sql = "select new t_bill_invests(id as id,title as title, SUM(receive_corpus + receive_interest + ifnull(overdue_fine,0)) as receive_amount, " +
 				"status as status, receive_time as  receive_time, real_receive_time as real_receive_time )" +
 				"from t_bill_invests where bid_id = ? and user_id = ? and invest_id = ? group by id";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();
		PageBean<t_bill_invests> page = new PageBean<t_bill_invests>();
		page.pageSize = Constants.FIVE;
		page.currPage = Constants.ONE;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(pageSize != 0){
			page.pageSize = pageSize;
		}
		
		try {
			page.totalCount = (int) t_bill_invests.count("bid_id = ? and user_id = ? and invest_id = ?", bidId, userId, investId);
			investBills = t_bill_invests.find(sql, bidId, userId, investId).fetch(page.currPage, page.pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		
		page.page = investBills;

		return page;
 	}
 	
 	/**
 	 * 我的理财账单——-根据标的ID和投资人ID查询还款记录
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static List<t_bill_invests> queryMyInvestBillReceivablesBid(long bidId,long userId, ErrorInfo error){
 		error.clear();
 		String sql = "SELECT new t_bill_invests(id AS id,title AS title,status AS status, receive_time AS  receive_time,(receive_corpus+receive_interest) AS receive_amount,real_receive_time AS real_receive_time)" +
 				" FROM t_bill_invests WHERE bid_id = ? AND user_id = ? order by receive_time asc";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();

		try {
			investBills = t_bill_invests.find(sql, bidId, userId).fetch();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		

		return investBills;
 	}
 	
 	/**
 	 * 查询累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double querySumIncome(long userId){
 		String sql ="select sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一月累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryMonthSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine)  from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 month)" ;
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一年累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryYearSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 year)";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 计算理财管理费
 	 * @param receiveInterest  本期应收利息
 	 * @param managementRate 费率
 	 * @param investUserId  投资者id
 	 * @return
 	 */
 	public static double getInvestManagerFee(double receiveInterest, double managementRate, long investUserId){
 		
 		double manageFee = Arith.round(Arith.mul(receiveInterest, managementRate), 2);  //投资管理费;
 
		return manageFee;
		
 	}
 	
 	/**
 	 * 获取投资账单列表
 	 * @param bidId		借款标ID
 	 * @param periods	期数
 	 * @return
 	 */
 	public static List findBillInvestsByBidIdAndPeriods(long bidId, int periods) {
 		List<Map<String, Object>> billInvestList = null;
 		
 		String sql = " select new Map(invest.id as id, invest.invest_id as investId, "
 				+ " invest.receive_corpus as receive_corpus,invest.receive_interest as " +
 				" receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, "
 				+ " invest.overdue_fine) "
				+ " from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? "
				+ "and invest.status not in (?,?,?,?)";
 		try {
 			billInvestList = t_bill_invests.find(sql, bidId, periods, 
 					Constants.FOR_DEBT_MARK, 
 					Constants.NORMAL_RECEIVABLES, 
 					Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, 
 					Constants.OVERDUE_RECEIVABLES).fetch();
 			
		} catch (Exception e) {
			Logger.error("------- 获取投资账单列表失败：", e);
			e.printStackTrace();
			return null;
		}
 		
		return billInvestList;
 	}
 	
}
