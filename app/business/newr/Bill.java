package business.newr;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_system_options;
import models.t_user_details;
import models.newr.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import payment.api.util.GUIDGenerator;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.BackstageSet;
import business.BillInvests;
import business.DealDetail;
import business.StationLetter;
import business.TemplateSms;
import business.TemplateStation;

import com.shove.Convert;

import constants.Constants;
import constants.DealType;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;
import cpcn.institution.tools.util.GUID;

/**
 * 账单
* @author zhs
* @version 6.0
* @created 2014年3月21日 下午2:19:20
 */

public class Bill implements Serializable{
    public long id;
    private long _id;
    
 	public long bidId;
 	public String title;
 	public Date repaymentTime;
 	public double repaymentCorpus;
 	public double repaymentInterest;
 	public int status;
 	public String merBillNo;
 	public int periods;
 	public Date realRepaymentTime;
 	public double realRepaymentCorpus;
 	public double realRepaymentInterest;
 	public int overdueMark;
 	public Date markOverdueTime;
 	public double overdueFine;
 	public Date markBadTime;
 	public int noticeCountMessage;
 	public int noticeCountMail;
 	public int noticeCountTelphone;
 	
 	public User user;
    public Bid bid;
    
    public boolean isRepair;//是否补单
    
    // v7.2.6 add 
    public int ipsStatus;  //资金托管处理状态
    
    public void setId(long id){
    	ErrorInfo error = new ErrorInfo();
    	t_bills bills = null;
    	 try {
    		 bills = t_bills.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致账单详情失败！";
		}

		if(bills.id < 0 || bills == null){
    		 this._id = -1;
    		 return;
    	 }
    	 
    	 this._id = bills.id;
    	 this.bidId = bills.bid_id;
    	 this.title = bills.title;
         this.repaymentTime = bills.repayment_time;
         this.repaymentCorpus = bills.repayment_corpus;
         this.repaymentInterest = bills.repayment_interest;
         this.status = bills.status;
         this.merBillNo = bills.mer_bill_no;
         this.periods = bills.periods;
         this.realRepaymentTime = bills.real_repayment_time;
         this.realRepaymentCorpus = bills.real_repayment_corpus;
         this.realRepaymentInterest = bills.real_repayment_interest;
         this.overdueMark = bills.overdue_mark;
         this.markOverdueTime = bills.mark_overdue_time;
         this.overdueFine = bills.overdue_fine;
         this.markBadTime = bills.mark_bad_time;
         this.noticeCountMessage = bills.notice_count_message;
         this.noticeCountMail = bills.notice_count_mail;
         this.noticeCountTelphone = bills.notice_count_telphone;
         
         this.ipsStatus = bills.ips_status; // v7.2.6 add 
         
    	 bid = new Bid();
    	 bid.id = bills.bid_id;
    	 
         this.user = bid.user;
     }
     
     public long getId() {
 		return _id;
 	}
     
     /**
      * 日期累加
      * @param date
      * @param type
      * @param value
      * @return
      */
     public static Date add(Date date, int type, int value){
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(date);
		 calendar.add(type, value);
		 
		 return calendar.getTime();
	 }
	  
     /**
      * 查询投资管理费率和逾期费率
      * @param info
      * @return
      */
     public static Map<String, Object> checkManagerate(ErrorInfo error){
    	 error.clear();
    	 List<t_system_options> options = null;
    	 
    	 double  investmentRates;
    	 double  overdueRates;
    	 
    	 String sql = "_key = ? or _key = ? order by id";
    	 try {
    		 options = t_system_options.find(sql, "investment_fee","overdue_fee").fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询投资管理费率时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致查询查询投资管理费率失败！";
			
			return null;
		}
		 investmentRates = Double.parseDouble(options.get(0)._value); 
		 overdueRates = Double.parseDouble(options.get(1)._value);
    	 
    	 Map<String,Object> map = new HashMap<String,Object>();
    	 map.put("investmentRates", investmentRates);
    	 map.put("overdueRates", overdueRates);
    	 
    	 return map;
     }
     
	/**
 	 * 保存账单
 	 * @param obj
 	 */
 	public int addBill(Bid bid, Date settleTime ,ErrorInfo error){
 		error.clear();	
 		double monthRate = 0;//月利率
 		int deadline = bid.period; //借款标期限
 		double borrowSum = bid.hasInvestedAmount; //实际借款金额
 		Integer period_unit = bid.periodUnit;//借款期限
 		monthRate = Double.valueOf(bid.apr * 0.01)/12.0;//通过年利率得到月利率
				
       //一次还款
       if(bid.repayment.id == Constants.ONCE_REPAYMENT){
    	   if (period_unit == -1 || period_unit == 0) {
		/*	   if (period_unit == -1) {
					deadline = deadline * 12;
				}
			   
			   bills = new t_bills();
			   monPayInterest = Arith.mul(borrowSum, monthRate);
			   totalInterest = monPayInterest * deadline;
			   totalAmount = borrowSum + totalInterest;
				  
			   bills.bid_id = bid.id;
			   bills.title = bid.title;
			   bills.periods = 1;
			   bills.repayment_time = add(new Date(), Calendar.MONTH, deadline);
			   bills.repayment_corpus = borrowSum;
			   bills.repayment_interest = totalInterest;
			   bills.status = Constants.NO_REPAYMENT;
			   bills.mer_bill_no = User.createBillNo();
				  
			   try {
				    bills.save();
					
			   }catch(Exception e) {
					e.printStackTrace();
					Logger.info("保存投资账单时："+e.getMessage());
					error.code = -4;
					error.msg = "数据库异常，导致保存投资账单失败";
					JPA.setRollbackOnly();
					
					return error.code;
			   }
				   
			   this.addInvestBills(bid.id, bid.repayment.id, bid.userId,  error);//生成投资账单
			   
			   return error.code;*/
			   
    	   } else {
    		   
    		   this.addDayBills(bid.title, deadline, borrowSum, monthRate, 
    				   bid.id, bid.repayment.id, bid.userId,settleTime, error);//生成天标借款账单和投资账单    		   
    		   return error.code;
 			}
	    }
       
       return error.code;
 	}
 	
 	/**
 	 * 生成天标借款账单
 	 * @param deadline 借款天标的天数
 	 * @param borrowSum  借款的总额
 	 * @param monthRate  月利率
 	 * @param bidId   标id
 	 */
 	public int addDayBills(String title, int deadline, double borrowSum, double monthRate,
 			long bidId, long repaymentId, long userId, Date settleTime, ErrorInfo error){
 		error.clear();
 		
	    t_bills bills = new t_bills();
		double monPayInterest = Arith.div(Arith.mul(Arith.mul(borrowSum, monthRate), deadline), 30, 2);//天标的总利息

		try {
			//1.生成借款账单
			bills.bid_id = bidId;
			bills.title = title;
			bills.periods = 1;
			if(settleTime!=null){
				bills.repayment_time = add(settleTime, Calendar.DAY_OF_MONTH, deadline);
			}else{
				bills.repayment_time = add(new Date(), Calendar.DAY_OF_MONTH, deadline);
			}
			
			bid.recentRepayTime = bills.repayment_time;
			bills.repayment_corpus = borrowSum;
			bills.repayment_interest = monPayInterest;
			bills.status = Constants.NO_REPAYMENT;
			bills.mer_bill_no =GUIDGenerator.genGUID();
			bills.save();
			
	  } catch(Exception e) {
			e.printStackTrace();
			Logger.info("生成天标借款账单时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致生成天标借款账单失败";
			JPA.setRollbackOnly();
		}
	  
	   this.addInvestBills(bidId, repaymentId, userId, error);//生成投资账单
	  
	   error.code = 0;
	   return error.code;
 	}
 	
 	/**
 	 * 生成投资账单
 	 * @param deadline 借款期限
 	 * @param borrowSum  借款总额
 	 * @param monPayAmount  月收金额
 	 * @param monPayRate  月利息
 	 * @param bidId  标id
 	 */
 	public int addInvestBills(long bidId, long repaymentId, long userId , ErrorInfo error){
 		  error.clear();
 		  EntityManager em = JPA.em();
 		
 		  //1.生成投资账单
 		  String sql = "insert into t_bill_invests(user_id,invest_id,bid_id,mer_bill_no,periods,title,receive_time,receive_corpus,receive_interest, " +
	  		"status, overdue_fine, real_receive_corpus, real_receive_interest) SELECT a.user_id,a.id, a.bid_id,a.mer_bill_no,b.periods,b.title,b.repayment_time,truncate(((a.amount * b.repayment_corpus)/ c.has_invested_amount),2)," +
	  		"truncate(((a.amount * b.repayment_interest)/ c.has_invested_amount),2), -1, 0.00, 0.00, 0.00 FROM t_bills AS b LEFT JOIN t_invests AS a ON a.bid_id " +
	  		"= b.bid_id LEFT JOIN t_bids AS c ON a.bid_id = c.id AND b.bid_id = c.id WHERE b.bid_id IS NOT NULL AND b.status " +
	  		"= -1 AND b.bid_id = ?";
 		  
		  Query query = em.createNativeQuery(sql).setParameter(1, bidId);
		  int rows = 0;
		    
		  try {
		  	    rows = query.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("添加投资账单时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致添加投资账单失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
			  
			 if(rows == 0){
				error.code = -1;
				error.msg = "添加投资账单操作数据库没有改变";
				JPA.setRollbackOnly();
					
				return error.code;
			}
			 
			 //2.初始化纠偏数据(投资记录表):查询出的理财账单的收取本金总和和利息总和赋值给t_invests标的纠偏字段
		  String correctStartSql = "update t_invests t1, (select a.invest_id, a.bid_id, a.user_id, sum(a.receive_corpus) receive_corpus, sum(a.receive_interest)" +
		  		" receive_interest from t_bill_invests a where a.bid_id = ? group by a.invest_id) t2 set t1.correct_amount = t2.receive_corpus ," +
		  		" t1.correct_interest = t2.receive_interest where t1.bid_id = t2.bid_id and t1.id = t2.invest_id";
		  
		  Query correctStart = em.createNativeQuery(correctStartSql).setParameter(1, bidId);
		    
		  try {
		  	    rows = correctStart.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏数据初始化时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致纠纠偏数据初始化失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
		  
		  //3.纠偏本金利息给第一个投资人(投资记录表)：t_bills表要还款的本金总金额和利息总金额分别减去t_invests纠偏本金总和和纠偏利息总金额得到的差值再分别加到第一个投资人纠偏本金和纠偏利息上
		  String correctCorIntSql = "update t_invests t1, (select t3.min_id, (t4.repayment_corpus - t3.collect_amount) check_amount, " +
		  		"(t4.repayment_interest - t3.collect_interest) check_interest from (select min(a.id) min_id, a.bid_id, sum(a.correct_amount) " +
		  		"collect_amount, sum(a.correct_interest) collect_interest from t_invests a where a.bid_id = ? group by a.bid_id) t3 left join " +
		  		"(select b.bid_id, sum(b.repayment_corpus) repayment_corpus, sum(b.repayment_interest) repayment_interest from  t_bills b where " +
		  		"b.bid_id = ? group by b.bid_id) t4 on t3.bid_id = t4.bid_id) t2 set t1.correct_amount = t1.correct_amount + t2.check_amount, " +
		  		"t1.correct_interest = t1.correct_interest + t2.check_interest where t1.id = t2.min_id";
		  
		  Query correctCorInt = em.createNativeQuery(correctCorIntSql).setParameter(1, bidId).setParameter(2, bidId);
		    
		  try {
		  	    rows = correctCorInt.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏数据初始化时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致纠偏本金利息失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
		  
		  //4.核对纠偏本金(投资记录表)
		  String checkCorrectSql = "update t_invests t1,(select a.id, a.user_id, a.bid_id, (a.amount - a.correct_amount) check_corpus from t_invests a " +
		  		"where a.bid_id = ?) t2 set t1.correct_amount = t1.correct_amount + t2.check_corpus where t1.bid_id = t2.bid_id and t1.user_id = t2.user_id and t2.id = t1.id";			 
		  Query checkCorrect = em.createNativeQuery(checkCorrectSql).setParameter(1, bidId);
		    
		  try {
		  	    rows = checkCorrect.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏数据初始化时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致纠偏本金失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
			
		//5.纠偏利息给第一个投资人(投资记录表)：t_bills里的 和t_invests表的所有本金和本金，利息和利息相减得到的差值给第一个投资人
		  String correctIntSql = "update t_invests t1, (select t3.min_id, (t4.repayment_corpus - t3.collect_amount) check_amount, " +
		  		"(t4.repayment_interest - t3.collect_interest) check_interest from (select min(a.id) min_id, a.bid_id, sum(a.correct_amount) " +
		  		"collect_amount, sum(a.correct_interest) collect_interest from t_invests a where a.bid_id = ? group by a.bid_id) t3 left join " +
		  		"(select b.bid_id, sum(b.repayment_corpus) repayment_corpus, sum(b.repayment_interest) repayment_interest from  t_bills b where " +
		  		"b.bid_id = ? group by b.bid_id) t4 on t3.bid_id = t4.bid_id) t2 set t1.correct_amount = t1.correct_amount + t2.check_amount, " +
		  		"t1.correct_interest = t1.correct_interest + t2.check_interest where t1.id = t2.min_id";
		  
		  Query correctInt = em.createNativeQuery(correctIntSql).setParameter(1, bidId).setParameter(2, bidId);
		    
		  try {
		  	    rows = correctInt.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏数据初始化时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致利息失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
			 
			//6.纠偏投资应收款明细资金(理财账单表)：t_bills和t_bill_invests根据算出每一期每个人的本金利息差值都给到第一个投资人的本金利息上
 		  String updateSql = "update t_bill_invests t1,(select c.minId, (a.repayment_corpus-b.recivedPrincipal) check_corpus," +
	  		"(a.repayment_interest-b.recivedInterest) check_interest from (select id, bid_id,periods," +
	  		"repayment_corpus,repayment_interest from t_bills where bid_id = ?) a left join (select a.id ,a.bid_id, a.periods, " +
	  		"sum(a.receive_corpus) recivedPrincipal, sum(a.receive_interest) recivedInterest from t_bill_invests a where a.bid_id	= ? " +
	  		"group by a.periods) b on a.bid_id = b.bid_id AND a.periods = b.periods  left join (select min(a.id) minId,a.bid_id," +
	  		"a.periods from t_bill_invests a where a.bid_id = ? group by a.periods) c on b.bid_id = c.bid_id AND a.periods = c.periods) " +
	  		"t2 set t1.receive_corpus = t1.receive_corpus + t2.check_corpus, t1.receive_interest = t1.receive_interest + " +
	  		"t2.check_interest where t1.id = t2.minId";
 		  
		  Query update = em.createNativeQuery(updateSql).setParameter(1, bidId).setParameter(2, bidId).setParameter(3, bidId);
		    
		  try {
		  	    rows = update.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏投资应收款明细资金时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致纠偏投资应收款明细资金失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
		  //7.纠偏待收本金和利息(理财账单表)：t_invest 和t_bill_invests把本金和本金，利息和利息的差值都给到第一个投资人那里 ,+
		  
		  
		  String updateCorIntSql = "update t_bill_invests t1,(select t3.id, t3.min_id, t3.user_id, t3.bid_id, (t4.amount - t3.receive_corpus) check_corpus, " +
	  		"(t4.correct_interest - t3.receive_interest) check_interest from (select a.id, min(a.id) as min_id, a.invest_id, a.bid_id, a.user_id, sum(a.receive_corpus) " +
	  		"receive_corpus, sum(a.receive_interest) receive_interest from t_bill_invests a where a.bid_id = ? group by a.invest_id) " +
	  		"t3 left join (select b.user_id, b.id, b.bid_id, b.amount, b.correct_interest from t_invests b where b.bid_id = ? group by" +
	  		" b.id) t4 on t3.bid_id = t4.bid_id and t3.invest_id = t4.id) t2 set t1.receive_corpus = t1.receive_corpus + " +
	  		"t2.check_corpus, t1.receive_interest = t1.receive_interest + t2.check_interest where t1.id = t2.min_id";
	  
	      Query updateCorInt = em.createNativeQuery(updateCorIntSql).setParameter(1, bidId).setParameter(2, bidId);
	    
		  try {
		  	    rows = updateCorInt.executeUpdate();
		  } catch(Exception e) {
				e.printStackTrace();
				Logger.info("纠偏投资应收款明细资金时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致纠偏待收本金和利息失败";
				JPA.setRollbackOnly();
					
				return error.code;
			}
		  
		  if(repaymentId == Constants.PAID_MONTH_ONCE_REPAYMENT){
			   //8.先息后本账单
			  String oprateSql = "update t_bill_invests t1,(select a.receive_corpus, a.id from t_bill_invests a left join t_bids b on a.bid_id = " +
			  		"b.id where a.periods < b.period and a.bid_id = ? and b.period_unit <> ? group by a.id) t2 set t1.receive_corpus = 0.00, t1.receive_interest = " +
			  		"t1.receive_interest + t2.receive_corpus where t1.id = t2.id";
		  
		      Query oprateInvestBill = em.createNativeQuery(oprateSql).setParameter(1, bidId).setParameter(2, Constants.DAY);
		    
			  try {
			  	    rows = oprateInvestBill.executeUpdate();
			  } catch(Exception e) {
					e.printStackTrace();
					Logger.info("纠偏投资应收款明细资金时："+e.getMessage());
					error.code = -3;
					error.msg = "数据库异常，导致纠偏待收本金和利息失败";
					JPA.setRollbackOnly();
						
					return error.code;
				}
		  }  	  
			error.code = 0;
			return error.code;
 	  }
 	
 	
 	

 	/**
 	 * 还款
 	 * @param userId 借款人id
 	 * @param error
 	 * @return
 	 */
 	public Map<String, List<Map<String, Object>>> repayment(long userId, ErrorInfo error) {
 		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
 		Map<String, Object> mapp = new HashMap<String, Object>();
 		Map<String, Object> result = new HashMap<String, Object>();
 		error.clear();
 		
		double repaymentCorpus = 0;// 借款账单本期要付的本金
		double repaymentInterest = 0;// 借款账单本期要付的利息
		double repayOverdueFine = 0;
		int mark = 0;// 是否逾期的标记
		int status = 0;// 账单的还款状态
		int period = 0;
		
		double balance =0.0;  // queryBalance(userId, error);//借款用户可用余额
		
		//1.查询本期借款账单的账单数据
		String sql = "select new Map(user.ips_acct_no as ips_acct_no,user.mobile as mobile,bid.id as bid_id,bid.bid_no as bid_no,bill.overdue_mark as overdue_mark, bill.repayment_corpus as " +
				"repayment_corpus, bill.repayment_interest as repayment_interest, bill.overdue_fine as overdue_fine," +
				" bill.status as status, bill.periods as period) from t_bills as bill,t_bids as bid, t_users as user where bill.bid_id = bid.id and bid.user_id = user.id and bill.id = ?";
		try {
			result = t_bills.find(sql, this._id).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询借款账单信息时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致查询借款账单信息失败";
			JPA.setRollbackOnly();
			
			
			return null;
		}
		
		mark = (Integer)result.get("overdue_mark");
		repaymentCorpus = (Double)result.get("repayment_corpus");
		repaymentInterest = (Double)result.get("repayment_interest");
		status = (Integer)result.get("status");
		repayOverdueFine = (Double)result.get("overdue_fine");
		period = (Integer)result.get("period");
		
		for(Entry<String, Object> entry : result.entrySet()) {
			mapp.put(entry.getKey(), entry.getValue()==null?"":entry.getValue()+"");
		}
		
		//2.从投资记录表查询有哪些用户投资了这个借款标
		String sql3 = "select new Map(invest.id as id, invest.invest_id as investId, user.ips_acct_no as ipsAcctNo, user.mobile as mobile, invest.mer_bill_no as merBillNo, invest.receive_corpus as receive_corpus, invest.receive_interest " +
				"as receive_interest, invest.user_id as user_id, invest.overdue_fine as overdue_fine) "
				+ "from t_bill_invests as invest, t_users as user where invest.user_id = user.id and invest.bid_id = ? and invest.periods = ? and invest.status not in (?,?,?,?)";
		try {
			list = t_bill_invests.find(sql3, this.bidId,this.periods,Constants.FOR_DEBT_MARK, Constants.NORMAL_RECEIVABLES, 
					Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, Constants.OVERDUE_RECEIVABLES).fetch();
			
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询投资账单信息时："+e.getMessage());
			error.code = -3;
			error.msg = "数据库异常，导致查询投资账单信息失败";
			JPA.setRollbackOnly();
			
			return null;
		}
		if (mark == Constants.BILL_NO_OVERDUE) {//如果未标记逾期，则判断为正常还款
			
			double payment = Arith.add(repaymentCorpus, repaymentInterest);
			double lastBalance = Arith.sub(balance, payment);
			
			//1.判断借款人余额是否足够
			if (lastBalance < 0) {
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "余额不足，暂时不能还款，请及时充值";
				
				return null;
			}
			
			//2.判断该借款是否已经还款，防止重复提交
			if(this.status == Constants.NORMAL_REPAYMENT || this.status == Constants.OVERDUE_PATMENT){
				error.code = -1;
				error.msg = "本期账单已还款";
				
				return null;
			}

		
			if (null == list) {
				error.code = -4;
				error.msg = "还款出现异常，导致还款失败";
				
				return null;
			}			
			this.normalPayment(this.bidId, userId, repaymentCorpus, repaymentInterest, balance, list, period, error);
			
		} else {//否则判断为逾期还款
			double payment = repaymentCorpus + repaymentInterest + repayOverdueFine;// 总共要还款的金额
			double lastBalance = Arith.sub(balance, payment);

			//1.判断借款人余额是否足够
			if (lastBalance < 0) {
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "余额不足，暂时不能还款，请及时充值";
				
				return null;
			}
			
			//2.判断该借款是否已经还款，防止重复提交
			if(this.status == Constants.NORMAL_REPAYMENT || this.status == Constants.OVERDUE_PATMENT){
				error.code = -1;
				error.msg = "本期账单已还款";
				
				return null;
			}
			
			if (null == list) {
				error.code = -3;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
				
				return null;
			}
			this.overduePayment(this.bidId, userId, repaymentCorpus, repaymentInterest, balance, list, status, repayOverdueFine, period, error);
			
		}
		
		error.code = 0;
		return null;
 	}
 	
 	/**
 	 * 正常还款
 	 * @param bidId 标的id
 	 * @param userId 借款人id
 	 * @param repaymentCorpus 借款本金
 	 * @param repaymentInterest 借款利息
 	 * @param balance 可用余额
 	 * @param list 
 	 * @param period 账单期数
 	 * @param error
 	 * @return
 	 */
 	public int normalPayment(long bidId, long userId, double repaymentCorpus, double repaymentInterest, double balance, 
		    List<Map<String, Object>> list, int period, ErrorInfo error){
 		error.clear();
 		
 		double managementRate = Bid.queryInvestRate(bidId);
 		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
 		
 		double payment = Arith.add(repaymentCorpus, repaymentInterest);
		EntityManager em = JPA.em();
		int rows = 0;
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		String bidTitleSql = "select title from t_bids where id = ? ";
		String title = null;
		
		try {
			title = t_bids.find(bidTitleSql, bidId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据标id查询时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return error.code;
		}
		
		//判断系统应付账单设置（0为自动付款，1为手动付款）
		if(backstageSet.repayType.equalsIgnoreCase(Constants.AUTO_PAYMENT) || Constants.IPS_ENABLE){

			//5.遍历本期的投资账单，计算并操作对应的还款金额到投资人账上
			for (Map<String, Object> map : list) {
				JSONObject json = JSONObject.fromObject(map);
				long investId = json.getLong("investId");//投资id
				long investBillId = json.getLong("id");//每个投资人的投资账单id
				long investUserId = json.getLong("user_id");//投资人id
				double receiveCorpus = json.getDouble("receive_corpus");//投资本金
				double receiveInterest = json.getDouble("receive_interest");//投资利息
				
				double manageFee = BillInvests.getInvestManagerFee(receiveInterest, managementRate, investUserId);  //投资管理费
		
				double receive = Arith.round(receiveCorpus + receiveInterest, 2);//计算投资人将获得的收益
				
				
				//7. 修改投资账单的收款情况
		 		String updateSql = "update t_bill_invests set status = ?, real_receive_time = ?, real_receive_corpus = ?," +
		 				" real_receive_interest = ? where user_id = ? and bid_id = ? and periods = ? and status not in(?,?) and invest_id = ?";
		 		
		        Query query = em.createQuery(updateSql).setParameter(1, Constants.NORMAL_RECEIVABLES).setParameter(2, DateUtil.currentDate())
		        .setParameter(3, receiveCorpus).setParameter(4, receiveInterest-manageFee).setParameter(5, investUserId).
		        setParameter(6, this.bidId).setParameter(7, this.periods).setParameter(8, Constants.NORMAL_RECEIVABLES).setParameter(9, Constants.FOR_DEBT_MARK).setParameter(10, investId);
		        
		        try {
		        	rows = query.executeUpdate();
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("修改投资账单的收款情况时："+e.getMessage());
					error.code = -6;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				//6.投资人余额增加获取的投资本金和利息
				String balanceSql2 = "update t_users set balance = balance + ? where id = ?";
				
				Query BalanceUpdate2 = em.createQuery(balanceSql2).setParameter(1, receive).setParameter(2, investUserId);
				
				try {
					rows = BalanceUpdate2.executeUpdate();
					
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("返回每个投资用户每期获得的投资本金和利息时："+e.getMessage());
					error.code = -5;
					error.msg = "数据库异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				Map<String, Double> investForDetail = DealDetail.queryUserFund(investUserId, error);
				if(error.code < 0 || investForDetail == null) {
					JPA.setRollbackOnly();
					
					return -1;
				}
				
				double investFreeze = investForDetail.get("freeze");
				double investReceiveAmount = investForDetail.get("receive_amount");
				
				Double investerBalance10 =  0.0;//this.queryBalance(investUserId, error);
				
/*				DealDetail investDetail = new DealDetail(investUserId, DealType.NOMAL_RECEIVE, receive,
						investBillId, investerBalance10, investFreeze, investReceiveAmount, "正常收款获取第"+this._id+"号账单投资金额");
				
				//添加正常收款的交易记录
				investDetail.addDealDetail(error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					
					return -1;
				}*/
				
				//6.投资人可用余额减去投资管理费
				String balanceSql = "update t_users set balance = balance - ? where id = ? and balance >= ?";
				
				Query BalanceUpdate = em.createQuery(balanceSql).setParameter(1, manageFee).setParameter(2, investUserId).setParameter(3, manageFee);
				
				try {
					rows = BalanceUpdate.executeUpdate();
					
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("扣取投资管理费时："+e.getMessage());
					error.code = -5;
					error.msg = "数据库异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				//本次查询用户可用余额是获取最新状态值，避免从缓存里拿数据
				Double investerBalance2 =0.0; //this.queryBalance(investUserId, error);
				
/*				DealDetail investFeeDetail = new DealDetail(investUserId, DealType.CHARGE_INVEST_FEE, manageFee,
						investBillId, investerBalance2, investFreeze, investReceiveAmount, "扣除第"+this._id+"号账单理财管理费");
				
				//添加扣除理财管理费的交易记录
				investFeeDetail.addDealDetail(error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					
					return -1;
				}*/
				
				
				Map<String, Object> userMap = new HashMap<String,Object>();
				
				String userSql = "select new Map(name as name, email as eamil, mobile as mobile) from t_users where id = ? ";
				
				try {
					userMap = null;//t_users.find(userSql, investUserId).first();
				}catch(Exception e) {
					e.printStackTrace();
					Logger.info("根据用户id查询时："+e.getMessage());
					error.code = -1;
					error.msg = "查询用户名失败";
					
					return error.code;
				}
				
				String userName = (String)userMap.get("name");
				String userEamil = (String)userMap.get("eamil");
				String userMobile = (String)userMap.get("mobile");
				
				//发送站内信 尊敬的username:\n 您投资的借款title,第repayPeriod期还款已经完成.<br/>paymentModeStr本期应得总额：
				//￥recivedSum,其中本金部分为：hasP元,利息部分：hasI元,实得逾期罚息：hasLFI元<br/>扣除投资管理费：
				//￥mFee元<br/>实得总额：￥msFee元
				TemplateStation station = new TemplateStation();
				station.id = Templets.M_INVEST_RECEIVE;
				
				if(station.status) {
					String mContent = station.content.replace("userName",userName);
					mContent = mContent.replace("title",title);
					mContent = mContent.replace("repayPeriod",this.periods+"");
					mContent = mContent.replace("recivedSum",  DataUtil.formatString(receive));
					mContent = mContent.replace("hasP",  DataUtil.formatString(receiveCorpus));
					mContent = mContent.replace("hasI",  DataUtil.formatString(receiveInterest));
					mContent = mContent.replace("hasLFI", "0.00");
					mContent = mContent.replace("mFee",  DataUtil.formatString(manageFee));
					mContent = mContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2)));
					
					StationLetter letter = new StationLetter();
					letter.senderSupervisorId = 1;
					letter.receiverUserId = investUserId;
					letter.title = station.title;
					letter.content = mContent;
					 
					letter.sendToUserBySupervisor(error);
				}
				
				//发送邮件 尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
				TemplateEmail email = new TemplateEmail();
				email.id = Templets.E_INVEST_RECEIVE;
				
				if(email.status) {
					String eContent = email.content.replace("userName", userName);
					eContent = eContent.replace("title",title);
					eContent = eContent.replace("repayPeriod",this.periods+"");
					eContent = eContent.replace("recivedSum",  DataUtil.formatString(receive));                        
					eContent = eContent.replace("hasP",  DataUtil.formatString(receiveCorpus));                        
					eContent = eContent.replace("hasI",  DataUtil.formatString(receiveInterest));                      
					eContent = eContent.replace("hasLFI", "0.00");                                                     
					eContent = eContent.replace("mFee",  DataUtil.formatString(manageFee));                            
					eContent = eContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2))); 
					email.addEmailTask(userEamil, email.title, eContent);
				}
				
				//尊敬的userName: 投资的编号bidId借款标repayPeriod期已成功还款，回款金额￥recivedSum元，扣除管理费￥mFee元，实得总额￥msFee元
				TemplateSms sms = new TemplateSms();
				sms.id = Templets.S_RECOVER_ADVANCE_SUCCESS;
				if(sms.status && StringUtils.isNotBlank(userMobile)) {
					String sContent = sms.content.replace("userName", userName);
					sContent = sContent.replace("bidId",backstageSet.loanNumber+bidId);
					sContent = sContent.replace("repayPeriod",this.periods+"");
					sContent = sContent.replace("recivedSum",  DataUtil.formatString(receive));                        
					sContent = sContent.replace("mFee",  DataUtil.formatString(manageFee));                            
					sContent = sContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2)));                                                   
					TemplateSms.addSmsTask(userMobile, sContent);
				}
		     }
		}
		//手动付款模式
		else{
			String updateInvestSql = "update t_bill_invests set status = ? where bid_id = ? and periods = ? and status not in(?,?,?,?,?)";
			
	        Query updateInvest = em.createQuery(updateInvestSql).setParameter(1, Constants.FOR_PAY).setParameter(2, bidId)
	        .setParameter(3, period).setParameter(4, Constants.FOR_PAY).setParameter(5, Constants.NORMAL_RECEIVABLES)
	        .setParameter(6, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES).setParameter(7, Constants.OVERDUE_RECEIVABLES).setParameter(8, Constants.FOR_DEBT_MARK);
			
			try {
				rows = updateInvest.executeUpdate();
			} catch(Exception e) {
				e.printStackTrace();
				Logger.info("修改投资账单状态时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致修改投资账单状态失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
		}
		
		//10.修改借款账单还款情况
		String updateBillSql = "update t_bills set status = ?, real_repayment_time = ?, real_repayment_corpus = ?, real_repayment_interest = ? where id = ? and status = ? ";
			
		Query updateBill = em.createQuery(updateBillSql).setParameter(1, Constants.NORMAL_REPAYMENT).setParameter(2, DateUtil.currentDate())
        .setParameter(3, repaymentCorpus).setParameter(4, repaymentInterest).setParameter(5, this._id).setParameter(6, Constants.NO_REPAYMENT);
		
		try {
			rows = updateBill.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款账单数据时："+e.getMessage());
			error.code = -3;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}finally{
			double Userbalance =0.0; //queryBalance(userId, error);//用户可用余额
			
			if(rows == 0){
				error.code = -1;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(Userbalance < 0){
				error.code = -1;
				error.msg = "资金不足,请充值!";
				JPA.setRollbackOnly();
				
				return error.code;
			}
		}
		
		//扣除借款人还款本期的所需金额
		String userBalanceSql = "update t_users set balance = balance - ? where id = ? and balance >= ?";
		
		Query userBalance = em.createQuery(userBalanceSql).setParameter(1, payment).setParameter(2, userId).setParameter(3, payment);
		
		try {
			rows = userBalance.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款人账户金额时："+e.getMessage());
			error.code = -2;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0){
			error.code = -1;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//9.针对借款账单做交易记录和事件
		Map<String, Double> billForDetail2 = DealDetail.queryUserFund(userId, error);
		if(error.code < 0 || billForDetail2 == null) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		double userFreeze3 = billForDetail2.get("freeze");
		double userReceiveAmount3 = billForDetail2.get("receive_amount");
			
		Double investerBalance10 =0.0; //this.queryBalance(userId, error);
		
/*		DealDetail detail = new DealDetail(userId, DealType.CHARGE_NOMAL_PAY, payment,
				this._id, investerBalance10, userFreeze3, userReceiveAmount3, "第"+this._id+"号账单正常还款扣除还款金额");
		
		//添加正常还款的交易记录
		detail.addDealDetail(error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}*/
		
		
		//添加事件
		DealDetail.userEvent(userId, UserEvent.ADD_NOMAL_PAY, "第"+this._id+"号账单用户正常还款", error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		Map<String, Object> userMap = new HashMap<String,Object>();
		
		String userSql = "select new Map(name as name, email as eamil, mobile as mobile) from t_users where id = ? ";
		
		try {
			userMap = null;//t_users.find(userSql, userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		String userName = (String)userMap.get("name");
		String userEamil = (String)userMap.get("eamil");
		String userMobile = (String)userMap.get("mobile");
		
		//发送站内信  尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_SUCCESS_PAY;
		
		if(station.status) {
			String mContent = station.content.replace("userName", userName);
			mContent = mContent.replace("date", DateUtil.dateToString(new Date()));
			mContent = mContent.replace("title",title);
			mContent = mContent.replace("needSum",  DataUtil.formatString(payment));
			mContent = mContent.replace("creditScore","");
			
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = mContent;
			 
			letter.sendToUserBySupervisor(error);
		}
		
		//发送邮件 尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_SUCCESS_PAY;
		
		if(email.status) {
			String eContent = email.content.replace("userName", userName);
			eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
			eContent = eContent.replace("title",title);
			eContent = eContent.replace("needSum",payment+"");
			eContent = eContent.replace("creditScore","");
			email.addEmailTask(userEamil, email.title, eContent);
		}
		
		//发送短信  尊敬的userName: 你申请的编号bidId借款标repayPeriod期成功还款，还款金额￥needSum元，
		TemplateSms sms = new TemplateSms();
		sms.id = Templets.S_REPAY_SUCCESS;
		if(sms.status && StringUtils.isNotBlank(userMobile)) {
			String eContent = sms.content.replace("userName", userName);
			eContent = eContent.replace("bidId",backstageSet.loanNumber+bidId);
			eContent = eContent.replace("repayPeriod",this.periods+"");
			eContent = eContent.replace("needSum",DataUtil.formatString(payment));
			TemplateSms.addSmsTask(userMobile, eContent);
		}
		
		//11.判断这个借款标是否已还完款，若还完标记这个借款标为已还款完状态
		if(this.isEndPayment(bidId, error) == 0){
			String bidSql = "update t_bids set status = ?, last_repay_time = ? where id = ?";
		
            Query query = em.createQuery(bidSql).setParameter(1, Constants.BID_REPAYMENTS).setParameter(2, DateUtil.currentDate())
            .setParameter(3, bidId);
            
			try {
				rows = query.executeUpdate();;
			
			} catch(Exception e) {
				e.printStackTrace();
				Logger.info("修改借款标为已还款完状态时："+e.getMessage());
				error.code = -7;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(rows == 0){
				error.code = -1;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
			
				return error.code;
			}
		}
		
		error.msg = "还款成功";
		error.code = 0;
		return error.code;
 	}
 	
 	/**
 	 * 逾期还款
 	 * @param userId
 	 * @param repaymentCorpus
 	 * @param repaymentInterest
 	 * @param balance
 	 * @param managementRate
 	 * @param list
 	 * @param info
 	 * @return
 	 * @throws ParseException 
 	 */
 	public int overduePayment(long bidId, long userId, double repaymentCorpus, double repaymentInterest, double balance, 
 			List<Map<String, Object>> investList, int status, double repayOverdueFine, int period, ErrorInfo error) {
 		error.clear();
 		
 		double managementRate = Bid.queryInvestRate(bidId);
 		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
 		
 		double payment = repaymentCorpus + repaymentInterest + repayOverdueFine;// 总共要还款的金额
		EntityManager em = JPA.em();
		int rows = 0;
		
		String bidTitleSql = "select title from t_bids where id = ? ";
		String title = null;
		
		try {
			title = t_bids.find(bidTitleSql, bidId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据标id查询时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return error.code;
		}
		
		//5.非本金垫付还款的操作
		if (status == Constants.NO_REPAYMENT){
			
			BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
			
			//判断系统应付账单设置（0为自动付款，1为手动付款）
			if(backstageSet.repayType.equalsIgnoreCase(Constants.AUTO_PAYMENT)){

			//a.遍历本期的投资账单，计算并操作对应的还款金额到投资人账上

			for (Map<String, Object> map : investList) {
				JSONObject json = JSONObject.fromObject(map);
				long investId = json.getLong("investId");//投资id
				long investBillId = json.getLong("id");//每个投资人的投资账单id
				long investUserId = json.getLong("user_id");//投资人id
				double receiveCorpus = json.getDouble("receive_corpus");// 本期的投资本金
				double receiveInterest = json.getDouble("receive_interest");// 本期的投资利息
				double investOverdueFine = json.getDouble("overdue_fine");// 本期逾期罚款
				
				double manageFee = BillInvests.getInvestManagerFee(receiveInterest, managementRate, investUserId);  //投资管理费

				double receive = Arith.round(receiveCorpus + receiveInterest + investOverdueFine, 2);//计算投资人总共将获取的金额
	
				
				// c.填写投资账单的收款情况
				String updateSql = "update t_bill_invests set status = ?, real_receive_time = ?, real_receive_corpus = ?," +
						" real_receive_interest = ?, overdue_fine = ? where user_id = ? and bid_id = ? and periods = ? and status not in(?,?) and invest_id = ?";
				
		        Query query = em.createQuery(updateSql).setParameter(1, Constants.OVERDUE_RECEIVABLES).setParameter(2, DateUtil.currentDate())
		        .setParameter(3, receiveCorpus).setParameter(4, receiveInterest - manageFee).setParameter(5, investOverdueFine)
		        .setParameter(6, investUserId).setParameter(7, this.bidId).setParameter(8, this.periods)
		        .setParameter(9, Constants.OVERDUE_RECEIVABLES).setParameter(10, Constants.FOR_DEBT_MARK).setParameter(11, investId);
		        
		        try {
					rows = query.executeUpdate();
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("填写投资账单的收款情况时："+e.getMessage());
					error.code = -5;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
					
				}
				
				// b.重新计算每个投资用户获取本期还款(本金+利息)金额
				String balanceSql = "update t_users set balance = balance + ? where id =?";
				
				Query BalanceUpdate = em.createQuery(balanceSql).setParameter(1, receiveCorpus + receiveInterest).setParameter(2, investUserId);
				
				try {
					rows = BalanceUpdate.executeUpdate();
					
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("返回每个投资用户每期获得的投资本金和利息以及罚息时："+e.getMessage());
					error.code = -5;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				//d.开始添加投资人交易记录和事件
				Map<String, Double> investForDetail = DealDetail.queryUserFund(investUserId, error);
				if(error.code < 0 || investForDetail == null) {
					JPA.setRollbackOnly();
					
					return -1;
				}
				
				double investFreeze = investForDetail.get("freeze");
				double investReceiveAmount = investForDetail.get("receive_amount");
				
				Double investBalance10 =0.0; //this.queryBalance(investUserId, error);
				
/*				DealDetail investDetail = new DealDetail(investUserId, DealType.OVER_RECEIVE, receiveCorpus + receiveInterest,
						investBillId, investBalance10, investFreeze, investReceiveAmount, "逾期收款获取第"+this._id+"号账单投资金额");
				
				//添加逾期收款的交易记录
				investDetail.addDealDetail(error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					
					return -1;
				}*/
				
				// c.投资人减去管理费
				String balanceSql2 = "update t_users set balance = balance - ? where id =? and balance >= ?";
				
				Query BalanceUpdate2 = em.createQuery(balanceSql2).setParameter(1, manageFee).setParameter(2, investUserId).setParameter(3, manageFee);
				
				try {
					rows = BalanceUpdate2.executeUpdate();
					
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("投资人减去管理费："+e.getMessage());
					error.code = -5;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				Double investBalance2 =0.0; //this.queryBalance(investUserId, error);
				
/*				DealDetail investFeeDetail = new DealDetail(investUserId, DealType.CHARGE_INVEST_FEE, manageFee,
						investBillId, investBalance2, investFreeze, investReceiveAmount, "扣除第"+this._id+"号账单理财管理费");
				
				//添加扣除理财管理费的交易记录
				investFeeDetail.addDealDetail(error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					
					return -1;
				}*/
				
				
				// d.投资人添加逾期费
				String balanceSql3 = "update t_users set balance = balance + ? where id =?";
				
				Query BalanceUpdate3 = em.createQuery(balanceSql3).setParameter(1, investOverdueFine).setParameter(2, investUserId);
				
				try {
					rows = BalanceUpdate3.executeUpdate();
					
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("投资人添加逾期费："+e.getMessage());
					error.code = -5;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0){
					error.code = -1;
					error.msg = "还款出现异常，导致还款失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				Double investBalance3 =0.0; //this.queryBalance(investUserId, error);
			
/*				DealDetail investOverdueFeeDetail = new DealDetail(investUserId, DealType.ADD_OVERDUE_FEE, investOverdueFine,
						investBillId, investBalance3, investFreeze, investReceiveAmount, "获取第"+this._id+"号账单逾期费");
				
				//添加获取逾期费用的交易记录
				investOverdueFeeDetail.addDealDetail(error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					
					return -1;
				}*/
	
				
                Map<String, Object> userMap = new HashMap<String,Object>();
				
				String userSql = "select new Map(name as name, email as eamil, mobile as mobile) from t_users where id = ? ";
				
				try {
					userMap =null; //t_users.find(userSql, investUserId).first();
				}catch(Exception e) {
					e.printStackTrace();
					Logger.info("根据用户id查询时："+e.getMessage());
					error.code = -1;
					error.msg = "查询用户名失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				String userName = (String)userMap.get("name");
				String userEamil = (String)userMap.get("eamil");
				String userMobile = (String)userMap.get("mobile");
				
				//发送站内信 尊敬的username:\n 您投资的借款title,第repayPeriod期还款已经完成.<br/>paymentModeStr本期应得总额：
				//￥recivedSum,其中本金部分为：hasP元,利息部分：hasI元,实得逾期罚息：hasLFI元<br/>扣除投资管理费：
				//￥mFee元<br/>实得总额：￥msFee元
				TemplateStation station = new TemplateStation();
				station.id = Templets.M_INVEST_RECEIVE;
				
				if(station.status) {
					String mContent = station.content.replace("userName",userName);
					mContent = mContent.replace("title",title);
					mContent = mContent.replace("repayPeriod",this.periods+"");
					mContent = mContent.replace("recivedSum",  DataUtil.formatString(receive));
					mContent = mContent.replace("hasP",  DataUtil.formatString(receiveCorpus));
					mContent = mContent.replace("hasI",  DataUtil.formatString(receiveInterest));
					mContent = mContent.replace("hasLFI",  DataUtil.formatString(investOverdueFine));
					mContent = mContent.replace("mFee",  DataUtil.formatString(manageFee));
					mContent = mContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2)));
					
					StationLetter letter = new StationLetter();
					letter.senderSupervisorId = 1;
					letter.receiverUserId = investUserId;
					letter.title = station.title;
					letter.content = mContent;
					 
					letter.sendToUserBySupervisor(error);
				}
				
				//发送邮件 尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
				TemplateEmail email = new TemplateEmail();
				email.id = Templets.E_INVEST_RECEIVE;
				
				if(email.status) {
					String eContent = email.content.replace("userName", userName);
					eContent = eContent.replace("title",title);
					eContent = eContent.replace("repayPeriod",this.periods+"");
					eContent = eContent.replace("recivedSum",  DataUtil.formatString(receive));                       
					eContent = eContent.replace("hasP",  DataUtil.formatString(receiveCorpus));                       
					eContent = eContent.replace("hasI",  DataUtil.formatString(receiveInterest));                     
					eContent = eContent.replace("hasLFI",  DataUtil.formatString(investOverdueFine));                 
					eContent = eContent.replace("mFee",  DataUtil.formatString(manageFee));                           
					eContent = eContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2)));
					email.addEmailTask(userEamil, email.title, eContent);
				}
				
				//尊敬的userName: 投资的编号bidId借款标repayPeriod期已成功还款，回款金额￥recivedSum元，扣除管理费￥mFee元，实得总额￥msFee元
				TemplateSms sms = new TemplateSms();
				sms.id = Templets.S_RECOVER_ADVANCE_SUCCESS;
				if(sms.status && StringUtils.isNotBlank(userMobile)) {
					String sContent = sms.content.replace("userName", userName);
					sContent = sContent.replace("bidId",backstageSet.loanNumber+bidId);
					sContent = sContent.replace("repayPeriod",this.periods+"");
					sContent = sContent.replace("recivedSum",  DataUtil.formatString(receive));                        
					sContent = sContent.replace("mFee",  DataUtil.formatString(manageFee));                            
					sContent = sContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - manageFee, 2)));                                                   
					TemplateSms.addSmsTask(userMobile, sContent);
				}
			 }
			}
			//手动付款模式
			else{
				String updateInvestSql = "update t_bill_invests set status = ? where bid_id = ? and periods = ? and status not in(?,?,?,?,?,?)";
				
		        Query updateInvest = em.createQuery(updateInvestSql).setParameter(1, Constants.FOR_OVERDUE_PAY).setParameter(2, bidId)
		        .setParameter(3, period).setParameter(4, Constants.FOR_PAY).setParameter(5, Constants.FOR_OVERDUE_PAY).setParameter(6, Constants.NORMAL_RECEIVABLES)
		        .setParameter(7, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES).setParameter(8, Constants.OVERDUE_RECEIVABLES).setParameter(9, Constants.FOR_DEBT_MARK);
				
				try {
					rows = updateInvest.executeUpdate();
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("修改投资账单状态时："+e.getMessage());
					error.code = -3;
					error.msg = "数据库异常，导致修改投资账单状态失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
			}
			
		}
		
		
		//7.修改借款账单还款情况
		String updateBillSql = "update t_bills set status = ?, real_repayment_time = ?, real_repayment_corpus = ?, real_repayment_interest = ?, overdue_fine = ? where id = ? and status in (?,?)";
			
		Query updateBill = em.createQuery(updateBillSql).setParameter(1, Constants.OVERDUE_PATMENT).setParameter(2, DateUtil.currentDate())
        .setParameter(3, repaymentCorpus).setParameter(4, repaymentInterest).setParameter(5, repayOverdueFine)
        .setParameter(6, this._id).setParameter(7, Constants.NO_REPAYMENT).setParameter(8, Constants.ADVANCE_PRINCIIPAL_REPAYMENT);
		
		try {
			rows = updateBill.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款账单数据时："+e.getMessage());
			error.code = -3;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}finally{
			double Userbalance =0.0; //queryBalance(userId, error);//用户可用余额
			
			if(rows == 0){
				error.code = -1;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(Userbalance < 0){
				error.code = -1;
				error.msg = "资金不足,请充值!";
				JPA.setRollbackOnly();
				
				return error.code;
			}
		}
		
		//8.判断这个借款标是否已还完款，如果还完后给这个借款标标记为已还完状态
		if(this.isEndPayment(bidId, error) == 0){
			String bidSql = "update t_bids set status = ?, last_repay_time = ? where id = ?";
		
            Query query = em.createQuery(bidSql).setParameter(1, Constants.BID_REPAYMENTS).setParameter(2, DateUtil.currentDate())
            .setParameter(3, bidId);
            
			try {
				rows = query.executeUpdate();;
			
			} catch(Exception e) {
				e.printStackTrace();
				Logger.info("修改借款标为已还款完状态时："+e.getMessage());
				error.code = -7;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(rows == 0){
				error.code = -1;
				error.msg = "还款出现异常，导致还款失败";
				JPA.setRollbackOnly();
			
				return error.code;
			}
		}
		
		//4.扣除借款人还款本期的本金和利息
		String userBalanceSql = "update t_users set balance = balance - ? where id = ? and balance >= ?";
		
		Query userBalanceUpdate = em.createQuery(userBalanceSql).setParameter(1, Arith.add(repaymentCorpus, repaymentInterest)).setParameter(2, userId).setParameter(3, Arith.add(repaymentCorpus, repaymentInterest));
		
		try {
			rows = userBalanceUpdate.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款人账户金额时："+e.getMessage());
			error.code = -2;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0){
			error.code = -1;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//9.添加有关借款账单还款的交易记录和事件
		Map<String, Double> billForDetail = DealDetail.queryUserFund(userId, error);
		if(error.code < 0 || billForDetail == null) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		double userFreeze = billForDetail.get("freeze");
		double userReceiveAmount = billForDetail.get("receive_amount");
		
		Double investBalance15 =0.0; //this.queryBalance(userId, error);
		
/*		DealDetail detail = new DealDetail(userId, DealType.CHARGE_OVER_PAY, payment - repayOverdueFine,
				this._id, investBalance15, userFreeze, userReceiveAmount, "第"+this._id+"号账单逾期还款扣除还款金额");
		
		//添加逾期还款的交易记录
		detail.addDealDetail(error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}*/
		
		//4.扣除借款人还款本期的逾期费用
		String userBalanceSql2 = "update t_users set balance = balance - ? where id = ? and balance >= ?";
		
		Query userBalanceUpdate2 = em.createQuery(userBalanceSql2).setParameter(1, repayOverdueFine).setParameter(2, userId).setParameter(3, repayOverdueFine);
		
		try {
			rows = userBalanceUpdate2.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款人账户金额时："+e.getMessage());
			error.code = -2;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0){
			error.code = -1;
			error.msg = "还款出现异常，导致还款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//本次查询用户可用余额是获取最新状态值，避免从缓存里拿数据
		Double userBalance2 = 0.0;//this.queryBalance(userId, error);
		
/*		DealDetail overdueDetail = new DealDetail(userId, DealType.CHARGE_OVERDUE_FEE, repayOverdueFine,
				this._id, userBalance2, userFreeze, userReceiveAmount, "第"+this._id+"号账单逾期还款扣除逾期费");
		
		//添加逾期还款扣除逾期费的交易记录
		overdueDetail.addDealDetail(error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}*/
		
		
		//添加事件
		DealDetail.userEvent(userId, UserEvent.ADD_NOMAL_PAY, "第"+this._id+"号账单用户逾期还款", error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
        Map<String, Object> userMap = new HashMap<String,Object>();
		
		String userSql = "select new Map(name as name, email as eamil, mobile as mobile) from t_users where id = ? ";
		
		try {
			userMap = null;//t_users.find(userSql, userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询时："+e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";
			
			return error.code;
		}
		
		String userName = (String)userMap.get("name");
		String userEamil = (String)userMap.get("eamil");
		String userMobile = (String)userMap.get("mobile");
		
		//发送站内信  尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_SUCCESS_PAY;
		
		if(station.status) {
			String mContent = station.content.replace("userName", userName);
			mContent = mContent.replace("date",DateUtil.dateToString(new Date()));
			mContent = mContent.replace("title",title);
			mContent = mContent.replace("needSum",  DataUtil.formatString(payment));
			mContent = mContent.replace("creditScore","");
			
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = mContent;
			 
			letter.sendToUserBySupervisor(error);
		}
		
		//发送邮件 尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_SUCCESS_PAY;

		if(email.status) {
			String eContent = email.content.replace("userName", userName);
			eContent = eContent.replace("date",DateUtil.dateToString(new Date()));
			eContent = eContent.replace("title",title);
			eContent = eContent.replace("needSum",  DataUtil.formatString(payment));
			eContent = eContent.replace("creditScore","");
			email.addEmailTask(userEamil, email.title, eContent);
		}
		
		//发送短信  尊敬的userName: 你申请的编号bidId借款标repayPeriod期成功还款，还款金额￥needSum元，
		TemplateSms sms = new TemplateSms();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		sms.id = Templets.S_REPAY_SUCCESS;
		if(sms.status && StringUtils.isNotBlank(userMobile)) {
			String eContent = sms.content.replace("userName", userName);
			eContent = eContent.replace("bidId",backstageSet.loanNumber+bidId);
			eContent = eContent.replace("repayPeriod",this.periods+"");
			eContent = eContent.replace("needSum",DataUtil.formatString(payment));
			TemplateSms.addSmsTask(userMobile, eContent);
		}				
		error.code = 0;
		error.msg = "还款成功";
		return error.code;
 	}
 	
 	/**
 	 * 判断借款账单是否已还完款
 	 * @param bidId
 	 * @param error
 	 * @return
 	 */
 	public int isEndPayment(long bidId, ErrorInfo error){
 		error.clear();
 		Object count = 0;
 		String sql = "select count(1) from t_bills where bid_id = ? and status = ?";
 		Query query = JPA.em().createNativeQuery(sql);
 		query.setParameter(1, bidId);
 		query.setParameter(2, Constants.ADVANCE_PRINCIIPAL_REPAYMENT);
 		
 		try {
 			count = query.getResultList().get(0);
		} catch (Exception e) {
			return -1;
		}
		
		if(null == count || Integer.parseInt(count.toString()) > 0) {
			return -1;
		}
		
		query = JPA.em().createNativeQuery(sql);
 		query.setParameter(1, bidId);
 		query.setParameter(2, Constants.NO_REPAYMENT);
		
 		try {
 			count = query.getResultList().get(0);
		} catch(Exception e) {
			return -1;
		}
			
		if(null == count) {
			return -1;
		}
		
 		return Integer.parseInt(count.toString());
 	}
 	
 	/**
 	 * 校验是否本金垫付已经垫付完了
 	 * @param bidId
 	 * @return
 	 */
 	public int isEndPayment(long bidId){
 		int count = 0;
 		
 		try {
 			count = (int)t_bills.count("status = ? and bid_id = ?", Constants.NO_REPAYMENT, bidId);
		} catch(Exception e) {
			return -1;
		}
			
 		return count;
 	}
 	
 	/**
 	 * 标记逾期
 	 * @param obj
 	 */
 	public static int markOverdue(long supervisorId, long billIdStr, ErrorInfo error){
 		error.clear();
 		
 		long billId = billIdStr;
 		
 		EntityManager em = JPA.em();
 		Query query = em.createQuery("update t_bills set overdue_mark = ?, mark_overdue_time = ? where id = ? and overdue_mark not in(?) ").
 		setParameter(1, Constants.BILL_OVERDUE).setParameter(2, new Date()).setParameter(3, billId).setParameter(4, Constants.BILL_OVERDUE);
 		int rows = 0;
 		
 		try {
 			rows = query.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("管理员手动标记借款账单为逾期时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致管理员手动标记借款账单为逾期失败";
			
			return error.code;
		}
		
		if(rows == 0){
			error.code = -1;
			error.msg = "标记逾期操作没有执行";
			JPA.setRollbackOnly();
			
			return error.code;
			
		}
 	  
		//添加管理员操作日志
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.MAKE_BILL_OVER, "标记"+billId+"号账单为逾期", error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		error.code = 0;
 		error.msg = "标记逾期成功！";
		
		return error.code;
		
 	}
 	
 	/**
 	 * 查询即将预期的标的
 	 */
 	public List<t_bills> systemMakeOverdue(ErrorInfo error){
 		error.clear();
		EntityManager em = JPA.em();
         List<t_bills>  overBillList = null;
         overBillList = t_bills.find("status = - 1 AND overdue_mark = 0 AND DATEDIFF(repayment_time,now()) < 6  and DATEDIFF(repayment_time,now()) > 0 ").fetch();
		try {
		
		} catch (Exception e) {		
			error.msg = "查询预期失败";
			error.code =-2;
			return overBillList;
		}
		return overBillList;
 	}
 	
 	/**
 	 *  2015-1-6 限制逾期费的上限 
 	 */
 	public static int countOverdueFine() {
 		EntityManager em = JPA.em();
		String sql = "select bid_id from t_bills where status in(-1,-2) and overdue_mark in (-1, -2, -3) group by bid_id";
		Query query = em.createNativeQuery(sql);
		List<Object> ids = null; // 逾期标记总和
		  
		try {
			ids = query.getResultList();
		} catch (Exception e) {
			Logger.info("限制逾期费的上限，错误MSG001");
			
			return -1;
		}
		
		if(null == ids || ids.size() == 0 || null == ids.get(0)){
			Logger.info("限制逾期费的上限，错误MSG002");
			
			return -1;
		}
		
		DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(2);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.UP); // 不四舍五入
        
		Double sumInterest = 0d; // 利息总和
		Double sumOverdueFine = 0d; // 罚息总和
		Double sumOverdueFineN = 0d; // 未还罚息总和
		List<Object[]> overdueFine = null;
		double remain = 0; // 剩余的钱
		double sumInterestScale = 0;
		
		String sql1 = "select SUM(repayment_interest) from t_bills where bid_id = ?";
		String sql2 = "select SUM(overdue_fine) from t_bills where bid_id = ?";
		String sql3 = "select id, overdue_fine from t_bills where bid_id = ? and status in(-1,-2) and overdue_mark in (-1, -2, -3)";
		String sql4 = "update t_bills set overdue_fine = overdue_fine - ? where id = ?";
		String sql5 = "select SUM(overdue_fine) from t_bills where bid_id = ? and status in(-1,-2) and overdue_mark in (-1, -2, -3)";
		long bidId = 0;
		
		/* 已标的为主，循环处理 */
		for (Object id : ids) {
			bidId = Long.parseLong(id.toString());
			
			try {
				sumInterest = t_bills.find(sql1, bidId).first();
				sumOverdueFine = t_bills.find(sql2, bidId).first();
			} catch (Exception e) {
				Logger.info("限制逾期费的上限，错误MSG003");
				
				return -1;
			}
			
			if(null == sumInterest || null == sumOverdueFine) {
				Logger.info("限制逾期费的上限，错误MSG004");
				
				return -1;
			}
			
			/* 如果超出了利息的2.5倍 */
			sumInterestScale = sumInterest * Constants.OF_AMOUNT;
			
			if(sumOverdueFine <= sumInterestScale)
				continue ;
			
			remain = sumOverdueFine - sumInterestScale; // 求出剩下的那点钱
			
			query = em.createNativeQuery(sql3);
			query.setParameter(1, bidId);
			
			try {
				sumOverdueFineN = t_bills.find(sql5, bidId).first(); 
				overdueFine = query.getResultList(); // 以标为单位得出未还逾期中的账单列表
			} catch (Exception e) {
				Logger.info("限制逾期费的上限，错误MSG005");
				
				return -1;
			}
			
			double amount = 0;
			long billId = 0;
			double of = 0;
			
			/* 按比例扣除账单中的逾期罚息 */
			for (Object[] obj : overdueFine) {
				if(null == obj[0] || null == obj [1]){
					Logger.info("限制逾期费的上限，错误MSG006");
					
					return -1;
				}
				
				billId = Long.parseLong(obj[0].toString()); // 得到账单ID
				
				if(billId <= 0) {
					Logger.info("限制逾期费的上限，错误MSG007");
					
					return -1;
				}
				
				amount = Double.parseDouble(obj[1].toString()); // 得到这条记录的逾期费
				of = amount / sumOverdueFineN * remain; // 根据比例求出需要扣除的罚息
		        
				if(amount <= 0 || of <= 0) 
					continue ;
				
				of = Double.parseDouble(formater.format(of)); // 不四舍五入向上取整，这样就不会少扣了，且不用纠偏
				int row = 0;
				query = em.createNativeQuery(sql4);
				query.setParameter(1, of); 
				query.setParameter(2, billId);
				
				try {
					row = query.executeUpdate(); // 减去多出的逾期罚息
				} catch (Exception e) {
					Logger.info("限制逾期费的上限，错误MSG008");
					
					return -1;
				}
				
				if(row < 1) {
					Logger.info("限制逾期费的上限，错误MSG009");
					
					return -1;
				}
			}
		}
		
		return 1;
 	}
 	
 	/**
 	 * 标记坏账
 	 * @param obj
 	 */
 	public static int markBad(long supervisorId, long billId, ErrorInfo error){
 		error.clear();
 		EntityManager em = JPA.em();
 		
 		String sql = "update t_bills bill set bill.overdue_mark = ?, mark_bad_time = ? where bill.id = ? and bill.overdue_mark not in(?)";
 		Query query = em.createQuery(sql).setParameter(1, Constants.BILL_BAD_DEBTS).
  	    setParameter(2, DateUtil.currentDate()).setParameter(3, billId).setParameter(4, Constants.BILL_BAD_DEBTS);
 		int rows = 0;
  	    
  	   try {
 			rows = query.executeUpdate();
 		} catch(Exception e) {
 			e.printStackTrace();
			Logger.info("修改借款账单数据时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致修改借款账单数据失败";
			
			return 0;
 		}
 		
 		if(rows == 0){
			error.code = -1;
			error.msg = "修改借款账单数据操作没有执行";
			JPA.setRollbackOnly();
			
			return error.code;
			
		}
		
 		//添加管理员操作日志
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.MAKE_BILL_BAD, "标记账单为坏账", error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}
 		
		error.code = 0;
		error.msg = "标记坏账成功";
		return error.code;
 	}
 	

 	
 	/**
 	 * 用户的待收金额
 	 * @return
 	 */
	public static double forReceive(long userId, ErrorInfo error) {
		String sql = "select SUM(a.receive_corpus + a.receive_interest + IFNULL(a.overdue_fine,0.00))"
				+ " as totalreceive from t_bill_invests as a where a.user_id = ? and a.status in (-1,-2,-5,-6)";
		Double receive = 0.0;
		
		try {
			receive = t_bill_invests.find(sql, userId).first();
		} catch (Exception e) {
			Logger.info("用户的待收金额:" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户待还金额出现异常";
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		if(null == receive || receive == 0){
			receive = 0.0;
		}
		
		return receive;
	}
     
 	/**
 	 * 查询借款账单信息并计算借款人实际的逾期罚款
 	 * @return
 	 * @throws ParseException
 	 */
 	public Map<String,Object> billOverdueFee(long billId, ErrorInfo error){
 		error.clear();
 		
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String, Object> billMap = new HashMap<String,Object>();
 		
 		String billSql = "select new Map(bill.repayment_corpus as repayment_corpus, bill.repayment_interest as" +
			" repayment_interest, bill.repayment_time as repayment_time) from t_bills as bill where bill.id = ?";
 		
 		try {
 			billMap = t_bills.find(billSql, billId).first();
		 } catch(Exception e) {
				e.printStackTrace();
				Logger.info("查询借款账单信息时："+e.getMessage());
				error.code = -1;
				error.msg = "数据库异常，导致查询借款账单信息失败";
				JPA.setRollbackOnly();
				
				return null;
			}
		 
		double payCorpus = (Double)billMap.get("repayment_corpus");
		double payInterest = (Double)billMap.get("repayment_interest");
		Date payTime = (Date)billMap.get("repayment_time");
		
		double overdueRates = Bid.queryRateByBillId(billId, 2);
		
		if(overdueRates != 0){
			overdueRates = overdueRates / 100;
		}
		
		int days = DateUtil.daysBetween(payTime, DateUtil.currentDate());
		double payOverdueFee = Arith.mul(Arith.mul(Arith.add(payCorpus, payInterest), overdueRates), days);//借款人本期应还的逾期罚息
		
		map.put("repayment_corpus", payCorpus);
		map.put("repayment_interest", payInterest);
		map.put("payOverdueFee", payOverdueFee);
		
		return map;
 	}
 	/**
 	 * 本金垫付账单
 	 * @param supervisorId 操作员id
 	 * @param billId 账单id
 	 * @param error
 	 * @return
 	 */
 	public int principalAdvancePayment(long supervisorId, long billId, ErrorInfo error){
 		error.clear();
 		Map<String, Object> billMap = new HashMap<String,Object>();
 		 int rows = 0;
 		
 		List<Map<String, Object>> investList = new ArrayList<Map<String,Object>>();
 		EntityManager em = JPA.em();
 		
 		String billSql = "select new Map(bill.status as status, bill.bid_id as bid_id, bill.periods as periods, bill.repayment_corpus as repayment_corpus, bill.repayment_interest as" +
		" repayment_interest, bill.repayment_time as repayment_time, bill.overdue_fine as overdue_fine) from t_bills as bill where bill.id = ? and bill.status not in (?,?,?)";
		
		try {
			billMap = t_bills.find(billSql, billId, Constants.NORMAL_REPAYMENT, 
					Constants.ADVANCE_PRINCIIPAL_REPAYMENT, Constants.OVERDUE_PATMENT).first();
	 } catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询借款账单信息时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(null == billMap){
			error.code = -1;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		int status = (Integer) billMap.get("status");
		long bidId = (Long) billMap.get("bid_id");
		int period = (Integer) billMap.get("periods");
		
 		if(status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT){
 			error.code = -1;
			error.msg = "本期账单已还款";
			
			return error.code;
 		}
	
 		double payCorpus = (Double)billMap.get("repayment_corpus");
 		double payInterest = (Double)billMap.get("repayment_interest");
 		
 		double investmentRate = Bid.queryInvestRate(bidId);
 		
 		if(investmentRate != 0){
 			investmentRate = investmentRate /100;
 		}
 		
 		String sql = "select new Map(invest.id as id, invest.invest_id as investId, invest.receive_corpus as receive_corpus,invest.receive_interest as " +
 				"receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, invest.overdue_fine) "
			+ "from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? and invest.status not in (?,?,?,?)";
 		try {
 			investList = t_bill_invests.find(sql, bidId, period, Constants.FOR_DEBT_MARK, Constants.NORMAL_RECEIVABLES, 
					Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, Constants.OVERDUE_RECEIVABLES).fetch();
		 } catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询投资账单信息时："+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
 		
 		if(null == investList){
			error.code = -1;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
	
		for (Map<String, Object> map : investList) {
			long investBillId = (Long) map.get("id");
			long investId = (Long) map.get("investId");
			double receiveInterest = (Double) map.get("receive_interest");// 本期的投资利息
			double receiveCorpus = (Double) map.get("receive_corpus");
			double receiveFees = (Double) map.get("overdue_fine");
			long investUserId = (Long) map.get("user_id");
			String investSql = "select balance from t_users where id = ? ";
			Double investerBalance = null;
			
			try {
				investerBalance = t_users.find(investSql, investUserId).first();//获取投资用户的余额
			 } catch(Exception e) {
					e.printStackTrace();
					Logger.info("查询投资用户的可用金额时："+e.getMessage());
					error.code = -2;
					error.msg = "数据库异常，导致本金垫付账单失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
			
			investerBalance = null == investerBalance ? 0 : investerBalance.doubleValue();
			double investManageFee = BillInvests.getInvestManagerFee(receiveInterest, investmentRate, investUserId);// 投资管理费
			double receive = Arith.round(receiveCorpus + receiveInterest + receiveFees, 2);
	
			
			// 改变投资账单的收款状态为本金垫付
		    String updateSql = "update t_bill_invests set status = ?, real_receive_time = ?, real_receive_corpus = ?," +
		  		" overdue_fine = ?, real_receive_interest = ? where user_id = ? and bid_id = ? and periods = ? and invest_id = ?";
		  
	        Query query = em.createQuery(updateSql).setParameter(1, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES).setParameter
	        (2, DateUtil.currentDate()).setParameter(3, receiveCorpus).setParameter(4, receiveFees).setParameter(5, receiveInterest - investManageFee)
	        .setParameter(6, investUserId).setParameter(7, bidId).setParameter(8, period).setParameter(9, investId);
	      
	        try {
				rows = query.executeUpdate();
		    } catch(Exception e) {
				e.printStackTrace();
				Logger.info("修改投资账单的状态时："+e.getMessage());
				error.code = -3;
				error.msg = "数据库异常，导致本金垫付账单失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
		  
		    if(rows == 0){
				error.code = -1;
				error.msg = "数据库异常，导致本金垫付账单失败";
				JPA.setRollbackOnly();
				
				return error.code;
				
			}
		  
			// 返回每个投资用户每期获得的投资本金和利息
			String userBalanceSql = "update t_users set balance = balance + ? where id = ?";
			Query userBalance = em.createQuery(userBalanceSql).setParameter(1, receiveInterest + receiveCorpus).setParameter(2, investUserId);
			
			 try {
				 rows = userBalance.executeUpdate();
			 } catch(Exception e) {
					e.printStackTrace();
					Logger.info("修改投资用户的金额时："+e.getMessage());
					error.code = -2;
					error.msg = "数据库异常，导致本金垫付账单失败";
					
					return error.code;
				}
			 
			 if(rows == 0){
					error.code = -1;
					error.msg = "数据库异常，导致本金垫付账单失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
			 
			Map<String, Double> billForDetail = DealDetail.queryUserFund(investUserId, error);
			if(error.code < 0 || billForDetail == null) {
				JPA.setRollbackOnly();
				
				return -1;
			}
			
			double userFreeze = billForDetail.get("freeze");
			double userReceiveAmount = billForDetail.get("receive_amount");
			Double investBalance10 = 0.0;//this.queryBalance(investUserId, error);
			
/*			DealDetail investDetail = new DealDetail(investUserId, DealType.OVER_RECEIVE, receive - receiveFees,
					investBillId, investBalance10, userFreeze, userReceiveAmount, "逾期收款获取第"+billId+"号账单投资金额");
			
			//添加逾期收款的交易记录
			investDetail.addDealDetail(error);
			if(error.code < 0) {
				JPA.setRollbackOnly();
				
				return -1;
			}*/
			
			// 返回每个投资用户每期获得的罚息
			String userBalanceSql2 = "update t_users set balance = balance + ? where id = ?";
			Query userBalance2 = em.createQuery(userBalanceSql2).setParameter(1, receiveFees).setParameter(2, investUserId);
			
			 try {
				 rows = userBalance2.executeUpdate();
			 } catch(Exception e) {
					e.printStackTrace();
					Logger.info("修改投资用户的金额时："+e.getMessage());
					error.code = -2;
					error.msg = "数据库异常，导致本金垫付账单失败";
					
					return error.code;
				}
			 
			 if(rows == 0){
					error.code = -1;
					error.msg = "数据库异常，导致本金垫付账单失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
			 
			Double investBalance3 = 0.0;//this.queryBalance(investUserId, error);
	/*		DealDetail investOverdueFeeDetail = new DealDetail(investUserId, DealType.ADD_OVERDUE_FEE, receiveFees,
					investBillId, investBalance3, userFreeze, userReceiveAmount, "获取第"+billId+"号账单逾期费");
			
			//添加获取逾期费用的交易记录
			investOverdueFeeDetail.addDealDetail(error);
			if(error.code < 0) {
				JPA.setRollbackOnly();
				
				return -1;
			}*/
			
			// 减去每个投资用户每期的管理费
			String userBalanceSql3 = "update t_users set balance = balance - ? where id = ? and balance >= ?";
			Query userBalance3 = em.createQuery(userBalanceSql3).setParameter(1, investManageFee).setParameter(2, investUserId).setParameter(3, investManageFee);
			
			 try {
				 rows = userBalance3.executeUpdate();
			 } catch(Exception e) {
					e.printStackTrace();
					Logger.info("修改投资用户的金额时："+e.getMessage());
					error.code = -2;
					error.msg = "数据库异常，导致本金垫付账单失败";
					
					return error.code;
				}
			 
			 if(rows == 0){
					error.code = -1;
					error.msg = "数据库异常，导致本金垫付账单失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
			 
			Double investBalance4 =0.0; //this.queryBalance(investUserId, error);
			
		/*	DealDetail investFeeDetail = new DealDetail(investUserId, DealType.CHARGE_INVEST_FEE, investManageFee,
					investBillId, investBalance4, userFreeze, userReceiveAmount, "扣除"+billId+"号 账单理财管理费");
			
			//添加扣除理财管理费的交易记录
			investFeeDetail.addDealDetail(error);
			if(error.code < 0) {
				JPA.setRollbackOnly();
				
				return -1;
			}*/

			
			Map<String, Object> userMap = new HashMap<String,Object>();
				
			String userSql = "select new Map(name as name, email as eamil, mobile as mobile) from t_users where id = ? ";
			
			try {
				userMap = t_users.find(userSql, investUserId).first();
			}catch(Exception e) {
				e.printStackTrace();
				Logger.info("根据用户id查询时："+e.getMessage());
				error.code = -1;
				error.msg = "查询用户名失败";
				
				return error.code;
			}
			
			String bidSql = "select title from t_bids where id = ? ";
			String title = null;
			
			try {
				title = t_bids.find(bidSql, bidId).first();
			}catch(Exception e) {
				e.printStackTrace();
				Logger.info("根据用户id查询时："+e.getMessage());
				error.code = -1;
				error.msg = "查询用户名失败";
				
				return error.code;
			}
			
			String userName = (String)userMap.get("name");
			String userEamil = (String)userMap.get("eamil");
			String userMobile = (String)userMap.get("mobile");
			
			//发送站内信 尊敬的username:\n 您投资的借款title,第repayPeriod期还款已经完成.<br/>paymentModeStr本期应得总额：
			//￥recivedSum,其中本金部分为：hasP元,利息部分：hasI元,实得逾期罚息：hasLFI元<br/>扣除投资管理费：
			//￥mFee元<br/>实得总额：￥msFee元
			TemplateStation station = new TemplateStation();
			station.id = Templets.M_INVEST_RECEIVE;
			
			if(station.status) {
				String mContent = station.content.replace("userName",userName);
				mContent = mContent.replace("title",title);
				mContent = mContent.replace("repayPeriod",period+"");
				mContent = mContent.replace("recivedSum",  DataUtil.formatString(receive));
				mContent = mContent.replace("hasP",  DataUtil.formatString(receiveCorpus));
				mContent = mContent.replace("hasI",  DataUtil.formatString(receiveInterest));
				mContent = mContent.replace("hasLFI",  DataUtil.formatString(receiveFees));
				mContent = mContent.replace("mFee",  DataUtil.formatString(investManageFee));
				mContent = mContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - investManageFee, 2)));
				
				StationLetter letter = new StationLetter();
				letter.senderSupervisorId = 1;
				letter.receiverUserId = investUserId;
				letter.title = station.title;
				letter.content = mContent;
				 
				letter.sendToUserBySupervisor(error);
			}
			
			//发送邮件 尊敬的username:\n [date] 您在晓风安全网贷系统6对标的【title】还款了￥needSum 元
			TemplateEmail email = new TemplateEmail();
			email.id = Templets.E_INVEST_RECEIVE;
			
			if(email.status) {
				String eContent = email.content.replace("userName", userName);
				eContent = eContent.replace("title",title);
				eContent = eContent.replace("repayPeriod",period+"");
				eContent = eContent.replace("recivedSum",  DataUtil.formatString(receive));                                
				eContent = eContent.replace("hasP",  DataUtil.formatString(receiveCorpus));                                
				eContent = eContent.replace("hasI",  DataUtil.formatString(receiveInterest));                              
				eContent = eContent.replace("hasLFI",  DataUtil.formatString(receiveFees));                                
				eContent = eContent.replace("mFee",  DataUtil.formatString(investManageFee));                              
				eContent = eContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - investManageFee, 2)));   
				email.addEmailTask(userEamil, email.title, eContent);
			}
		  
			//尊敬的userName: 投资的编号bidId借款标repayPeriod期已成功还款，回款金额￥recivedSum元，扣除管理费￥mFee元，实得总额￥msFee元
			TemplateSms sms = new TemplateSms();
			BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
			sms.id = Templets.S_RECOVER_ADVANCE_SUCCESS;
			if(sms.status && StringUtils.isNotBlank(userMobile)) {
				String sContent = sms.content.replace("userName", userName);
				sContent = sContent.replace("bidId",backstageSet.loanNumber+bidId);
				sContent = sContent.replace("repayPeriod",this.periods+"");
				sContent = sContent.replace("recivedSum",  DataUtil.formatString(receive));                        
				sContent = sContent.replace("mFee",  DataUtil.formatString(investManageFee));                            
				sContent = sContent.replace("msFee",  DataUtil.formatString(Arith.round(receive - investManageFee, 2)));                                                   
				TemplateSms.addSmsTask(userMobile, sContent);
			}
		}
		
		//改变借款账单的状态为本金垫付还款
		
		String updateBillSql = "update t_bills set status = ?, real_repayment_time = ?, real_repayment_corpus = ?, " + "real_repayment_interest = ? , mer_bill_no = ? where id = ? and status not in(?) ";

		Query updateBill = em.createQuery(updateBillSql).setParameter(1, Constants.ADVANCE_PRINCIIPAL_REPAYMENT).setParameter(2, DateUtil.currentDate()).setParameter(3, payCorpus)
														.setParameter(4, payInterest).setParameter(5, this.merBillNo).setParameter(6, billId).setParameter(7,Constants.ADVANCE_PRINCIIPAL_REPAYMENT);
		try {
			rows = updateBill.executeUpdate();
	    } catch(Exception e) {
			e.printStackTrace();
			Logger.info("修改借款账单的状态时："+e.getMessage());
			error.code = -3;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
	  
	    if(rows == 0){
			error.code = -1;
			error.msg = "数据库异常，导致本金垫付账单失败";
			JPA.setRollbackOnly();
			
			return error.code;
			
		}
	    
	    /* 本金垫付如果垫付完毕至为：本金垫付还款中(14) */
	    if(this.isEndPayment(bidId) == 0) {
		    sql = "update t_bids set status = ?, last_repay_time = ? where id = ? and status <> ?";
		    Query query = JPA.em().createQuery(sql).setParameter(1, Constants.BID_COMPENSATE_REPAYMENT)
					.setParameter(2, DateUtil.currentDate()).setParameter(3, bidId)
					.setParameter(4, Constants.BID_COMPENSATE_REPAYMENT);
	             
			try {
				rows = query.executeUpdate(); 
			} catch(Exception e) {
				Logger.info(e.getMessage());
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();
				
				return error.code;
			}
	    }
		
		//添加管理员操作日志
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.PRINCIPAL_PAY, "管理员对"+billId+"号 账单本金垫付", error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		error.code = 0;
		error.msg = "本金垫付成功";
 		return error.code;
 	}
 	



 	
 

 	/**
 	 * 在投资账单里复制符合条件的记录
 	 * @param bidId
 	 * @param userId
 	 * @param error
 	 * @return
 	 */
 	public static int investBillsTransfer(long bidId, long userId,long investId,ErrorInfo error){
 		error.clear();
 		
 		String sql = "INSERT INTO t_bill_invests(user_id,invest_id, bid_id,mer_bill_no,periods,title,receive_time,receive_corpus," +
 				"receive_interest,status,overdue_fine,real_receive_time,real_receive_corpus,real_receive_interest" +
 				") SELECT user_id,invest_id, bid_id,mer_bill_no,periods,title,receive_time,receive_corpus,receive_interest,-7," +
 				"overdue_fine,real_receive_time,real_receive_corpus,real_receive_interest FROM t_bill_invests " +
 				"WHERE user_id = ? AND bid_id = ? AND invest_id = ? and status not in (?,?,?)";
 		
 		try {
 			JPA.em().createNativeQuery(sql).setParameter(1, userId).setParameter(2, bidId).setParameter(3, investId).setParameter(4, Constants.NORMAL_RECEIVABLES).
 			setParameter(5, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES).setParameter(6, Constants.OVERDUE_RECEIVABLES).executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("更新投资账单id时："+e.getMessage());
			error.code = -1;
			error.msg = "债权转让成功后，数据库异常导致投资账单转换为新债权人失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		return 0;
 	}
		
	
	/**
	 * 还款计算器明细
	 */
	public static List<Map<String, Object>> repaymentCalculate(double amount, double apr, int period, int periodUnit, int repaymentType){
		double monthRate = 0;//月利率
 		double monPay = 0;//每个月要还的金额
 		double monPayInterest = 0;// 每个月利息（如果是天标的话，就是所有的利息）
 		double monPayAmount = 0;//每个月本金
 		double totalAmount = 0;//总共要还的金额
 		double payRemain = 0;//剩余要还的金额
 		double payAmount = 0;//加起来付了多少钱
 		double totalInterest = 0;//总利息
 		double payAmountAdd = 0;
 		double stillPay = 0;
 		
 		int deadline = period; //借款标期限
 		double borrowSum = amount; //借款金额
 		monthRate = Double.valueOf(apr * 0.01)/12.0;//通过年利率得到月利率
 		Map<String, Object> payMap =null;
 		List<Map<String, Object>> payList = null;
 		
 		 //按日结算
        if(periodUnit == 1){
 			   
 		  monPayInterest = Arith.mul(borrowSum, monthRate);
 		  totalInterest = monPayInterest * deadline / 30;
 		  totalAmount = borrowSum + totalInterest;
 		  payList = new ArrayList<Map<String, Object>>();	
 		  payMap = new HashMap<String, Object>();
 		  
 		  payMap.put("period", 1);
 		  payMap.put("monPay", Arith.round(totalAmount, 2));
 		  payMap.put("monPayAmount", Arith.round(borrowSum, 2));
 		  payMap.put("monPayInterest", Arith.round(totalInterest, 2));
 		  payMap.put("stillPay", Arith.round(stillPay, 2));
 		  payList.add(payMap);
 	   
 	      return payList;
 	    }else{
			//按月还款、等额本息
			if (repaymentType == Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST) {
	
				if (periodUnit == -1 || periodUnit == 0) {//判断标类(年标，月标，天标)
	
					if (periodUnit == -1) {//如果为年标，那么传过来的借款期限都乘以12
						deadline = deadline * 12;
					}
	
					monPay = Double.valueOf(Arith.mul(borrowSum, monthRate) * Math.pow((1 + monthRate), deadline))/ 
					Double.valueOf(Math.pow((1 + monthRate), deadline) - 1);//每个月要还的本金和利息
					monPay = Arith.round(monPay, 2);
					amount = borrowSum;
					totalAmount = Arith.mul(monPay, deadline);//总共要还的金额
					payRemain = Arith.round(totalAmount, 2);
					payList = new ArrayList<Map<String, Object>>();
					
					for (int n = 1; n <= deadline; n++) {
						monPayInterest = Arith.round(Arith.mul(amount, monthRate), 2);// 每个月利息
						monPayAmount = Arith.round(Arith.sub(monPay, monPayInterest), 2);// 每个月本金
						amount = Arith.round(Arith.sub(amount, monPayAmount), 2);
						
						if (n == deadline) {
							monPay = payRemain;
							monPayAmount = borrowSum - payAmount;
							monPayInterest = monPay - monPayAmount;
						}
						
						payAmount += monPayAmount;
						payRemain = Arith.sub(payRemain, monPay);
	
						payAmountAdd += monPayAmount + monPayInterest;
						stillPay = totalAmount - payAmountAdd;
						payMap = new HashMap<String, Object>();
						
						payMap.put("period", n);
						payMap.put("monPay", monPay);
						payMap.put("monPayAmount", Arith.round(monPayAmount, 2));
						payMap.put("monPayInterest", Arith.round(monPayInterest, 2));
						payMap.put("stillPay", Arith.round(stillPay, 2));
						payList.add(payMap);
				} 
			}
				return payList;
		}
			
	 		//按月付息、一次还款
	        if(repaymentType == Constants.PAID_MONTH_ONCE_REPAYMENT){
	        	monPayInterest = Arith.round(Arith.mul(borrowSum, monthRate), 2);
	        	double payInterestAdd = 0;
	        	  
	        	  if (periodUnit == -1 || periodUnit == 0) {
	
	  				  if (periodUnit == -1) {
	  					deadline = deadline * 12;
	  				  }
	  				  
	  				 totalInterest =  Arith.round(Arith.mul(Arith.mul(borrowSum, monthRate),deadline),2);
	  				 payList = new ArrayList<Map<String, Object>>();
	  				  
					  for(int n = 1;n<=deadline;n++){
						  
						  if(n == deadline){
							  monPayAmount = borrowSum;
							  double realPayInterest = Arith.round(Arith.mul(Arith.mul(borrowSum, monthRate), deadline), 2);//正真要还的利息金额 
							  monPayInterest = realPayInterest - payInterestAdd;//最后一期纠偏要还的利息 
						  }
						  else{
							  monPayAmount = 0.00;
						  }
						  
						  payInterestAdd += monPayInterest; 
						  payAmountAdd += monPayAmount + monPayInterest;
						  stillPay = totalInterest + amount - payAmountAdd;
						  monPay = monPayAmount + monPayInterest;
						  payMap = new HashMap<String, Object>();
							
						  payMap.put("period", n);
						  payMap.put("monPay",Arith.round(monPay, 2));
						  payMap.put("monPayAmount", Arith.round(monPayAmount, 2));
						  payMap.put("monPayInterest", Arith.round(monPayInterest, 2));
						  payMap.put("stillPay", Arith.round(stillPay, 2));
						  payList.add(payMap);
					  }
	        	  }
	        	  
	        	  return payList;
	  			}
	
	        //一次还款
	       if(repaymentType == Constants.ONCE_REPAYMENT){
	    	   if (periodUnit == -1 || periodUnit == 0) {
				   if (periodUnit == -1) {
						deadline = deadline * 12;
					}
				   
				  monPayInterest = Arith.mul(borrowSum, monthRate);
				  totalInterest = monPayInterest * deadline;
				  totalAmount = borrowSum + totalInterest;
				  payList = new ArrayList<Map<String, Object>>();	
				  payMap = new HashMap<String, Object>();
				  
				  payMap.put("period", 1);
				  payMap.put("monPay", Arith.round(totalAmount, 2));
				  payMap.put("monPayAmount", Arith.round(borrowSum, 2));
				  payMap.put("monPayInterest", Arith.round(totalInterest, 2));
				  payMap.put("stillPay", Arith.round(stillPay, 2));
				  payList.add(payMap);
	    	   } 
	    	   
	    	   return payList;
		    }
 	  }
       
        return null;
	}
	
	/**
	 * 计算标的已还本金
	 */
	public static double queryHasPayed(long bidId, ErrorInfo error){
		error.clear();
		
		String sql = "select sum(repayment_corpus+repayment_interest+overdue_mark) from t_bills where status in(-3,0) and bid_id = ?";
		Double amount = null;
		try {
			amount = t_bills.find(sql, bidId).first();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询标的已还本金情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询标的已还本金情况失败";
			
			return 0;
		}
		
		return amount = null == amount ? 0 : amount.doubleValue();
	}
	
	/**
	 * 计算债权转让的债权额（汇付用到）
	 * @param userId 用户id
	 * @param investId 理财账单标的invest_id
	 * @param error
	 * @return
	 */
	public static double queryDebtAmount(long userId,  long investId, ErrorInfo error){
		error.clear();
	    
		/**
		 * 债权面额 = 投资本金 - 已收金额 
		 * 
		 * 
		 */
		String sql = "SELECT (invest.amount - ifnull((SELECT SUM(t.receive_corpus + t.receive_interest + t.overdue_fine) "
				+ "FROM t_bill_invests t WHERE t.invest_id = ? AND status in (?, ?, ?)) ,0))"
				+ "FROM t_invests AS invest  WHERE id = ?";

		Query query = JPA.em().createNativeQuery(sql)
				.setParameter(1, investId)
				.setParameter(2, Constants.NORMAL_RECEIVABLES) 
				.setParameter(3, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES)
				.setParameter(4, Constants.OVERDUE_RECEIVABLES)
				.setParameter(5, investId); 
		Object obj = null;
	
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("计算债权转让的债权额时："+e.getMessage());
			error.code = -1;
			error.msg = "计算债权转让的债权额异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	public static void updateMerBillNo(long id, String merBillNo, ErrorInfo error) {
		error.clear();
		String sql = "update t_bills set mer_bill_no = ? where id = ?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, merBillNo);
		query.setParameter(2, id);
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
		}
		
		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
		}
	}
	
	
	/**
	 * 2014-12-29 限制还款需要从第一期逐步开始还款 
	 */
	public static int checkPeriod(long bidId, int periods) {
		try {
			return (int) t_bills.count("status in(?, ?) and bid_id = ? and periods < ?",
					Constants.NO_REPAYMENT, Constants.ADVANCE_PRINCIIPAL_REPAYMENT,
					bidId, periods);
		} catch (Exception e) {
			return 1;
		}
	}
	

	
	/**
	 * 获取还款订单号
	 * @param error
	 * @param id
	 * @return
	 */
	public static String getRepaymentBillNo(ErrorInfo error, long id) {
		error.code = -1;
		Query query = JpaHelper.execute("select repayment_bill_no from t_bills where id = ?", id);
		String pRepaymentBillNo = null;
		
		try {
			pRepaymentBillNo = (String) query.getSingleResult();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
		
		//定单号存在，则直接返回
		if (StringUtils.isNotBlank(pRepaymentBillNo)) {
			error.code = 1;
			
			return pRepaymentBillNo;
		}
		
		//pRepaymentBillNo = User.createBillNo();
		JPAUtil.executeUpdate(error, "update t_bills set repayment_bill_no = ? where id = ?", pRepaymentBillNo, id);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return null;
		}
		
		error.code = 1;
		
		return pRepaymentBillNo;
	}
	
	/**
	 * 查询标的本金利息合计
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static double queryBidPrincipal (long bidId, ErrorInfo error){
		String sql = "select sum(repayment_corpus + repayment_interest + overdue_fine) as principal from t_bills where bid_id = ?";
		
		List<Map<String, Object>> list = null;
		
		list = JPAUtil.getList(error, sql, bidId);
		
		if(list.size() == 0){
			return 0;
		}
		
		Object obj = list.get(0).get("principal");
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 更新资金托管交易状态
	 * @param billId  借款账单id
	 * @param ipsStatus  交易状态
	 * @param currentStatus  当前必须是currentStatus状态，才能执行更新
	 */
	public static void updateIPSStatusByID(long billId, int ipsStatus, int currentStatus){
		String sql = "update t_bills set ips_status = ? where id = ? and ips_status = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, ipsStatus).setParameter(2, billId).setParameter(3, currentStatus);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error("更新资金托管交易(bills)状态("+ currentStatus +"-->"+ ipsStatus +")时，%s", e.getMessage());
		}
	}
	
	/**
	 * 查询资金托管交易状态
	 * @param billId  借款账单id
	 */
	public static int QueryIPSStatusByID(long billId){
		String sql = "select ips_status from t_bills where id = ?";
		
		int ipsStatus = 0;
		
		try {
			ipsStatus = t_bills.find(sql, billId).first();
		} catch (Exception e) {
			Logger.error("更新资金托管交易(bills)状态时，%s", e.getMessage());
		}
		
		return ipsStatus;
	}
	
	/**
	 * 平台累计收益
	 */
	public static double  sumIncome() {
		// 投资收益(收益+逾期)
		String sql = "select ifnull(sum(repayment_interest),0) + ifnull(sum(overdue_fine),0) from t_bills where  status <> -1 ";
		double d = t_bills.find(sql).first();
		// cps收益 + 投标奖励
		sql = "select ifnull(sum(amount),0) + 0.0 from t_user_details where operation in (51,52,107)";
		double d1 = t_user_details.find(sql).first();
		return d + d1;
	}

	/**
	 * 查询到期还款金额
	 * @param error
	 * @return
	 */
	public static double queryNextMonthRepaymentSum(ErrorInfo error){
		error.clear();
		
		Object result = null;
		
		String sql = "select SUM(repayment_corpus) + SUM(repayment_interest) + SUM(overdue_fine) AS repayment from t_bills where status=-1 AND (YEAR (repayment_time) = YEAR (now())) AND (MONTH (repayment_time) = MONTH (now())) ";
		Query query = JPA.em().createNativeQuery(sql);
		
		try {
            result = query.getSingleResult();
            
		} catch (Exception e) {
			Logger.error("本月到期还款金额:" + e.getMessage());
			error.code = -1;
			error.msg = "查询本月到期还款金额";
			
			return 0;
		}
		
		return result==null?0:(Double)result;
	}
}