package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import bean.FullBidsApp;
import constants.Constants;
import constants.SQLTempletes;

/**
 * 优化类，所有优化，冗余都写在这个里面
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-12-25 上午10:08:08
 */
public class Optimization {

	/**
	 * 用户这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class UserOZ implements Serializable{
		public long userId; // 用户ID
        public int auditingCount; // 待审核借款标
        public int repaymentingCount; // 还款中借款标
        public int receivableInvestBidsCount; // 收款中理财标
        public int untreatedBillsCount; // 未处理借款账单
        public int untreatedInvestBillsCount; // 未处理理财账单
        public int overdueBillsCount; // 逾期借款账单
        public int lackSuditItemCount; // 须上传的审核资料
        
        public double user_account;  //账户总额  
        public double freeze;  //冻结金额
        public double user_account2;  //平台账户可用余额（资金托管模式）
        public double invest_amount;  //投标总额
        public int invest_count;  //投标数量
        public double receive_amount;  //应收账款
        public double bid_amount;  //借款总额
        public int bid_count;  //借款标数量
        public double repayment_amount;  //应还账款
        
        public UserOZ(){}
        
        public UserOZ(long userId){
        	this.userId = userId;
        	
        	String sql = "select new map(t.balance as balance, t.freeze as freeze, t.balance2 as balance2) from t_users t where t.id = ?";
        	List<Map<?, ?>> maps = null;
        	
        	EntityManager em = JPA.em();
        	Query query = em.createQuery(sql).setParameter(1, this.userId);
        	
        	try {
				maps = query.getResultList();
			} catch (Exception e) { 
				Logger.error("查询用户信息时：" + e.getMessage());
			}
        	
        	if (maps != null && maps.size() > 0) {
        		
        		Map<?, ?> map = maps.get(0);
        		
        		if (map.size() > 2) {        			
					this.freeze = (Double) map.get("freeze");
					this.user_account = (Double) map.get("balance") + this.freeze;
					this.user_account2 = (Double) map.get("balance2");
				}
			}
        }

		public int getAuditingCount() {
            String sql = "SELECT count(1)  FROM t_bids WHERE user_id = ? AND status = 0";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
             
            if(null == count || count.size() == 0){
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getRepaymentingCount() {
            String sql = "select count(1) from t_bids where user_id = ? and status in(4,14)";  //状态14：本金垫付还款中，但借款人未还款
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getReceivableInvestBidsCount() {
            String sql = "SELECT count(b.id) FROM (t_bids b LEFT JOIN t_invests c ON (b.id = c.bid_id) ) WHERE ( (c.user_id = ?) AND (c.transfer_status IN (0, 1) ) AND (b.status = 4) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getUntreatedBillsCount() {
            String sql = "SELECT count(b.id) FROM ( t_bills b LEFT JOIN t_bids d ON ( (b.bid_id = d.id) ) ) WHERE (  (d.user_id = ?) AND (b.status = -(1) ) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getUntreatedInvestBillsCount() {
            String sql = "SELECT count(b.id) FROM t_bill_invests b WHERE ( (b.status IN (-1 ,-2 ,-5 ,-6)) AND (b.user_id = ?) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        
        }
        
        public int getOverdueBillsCount() {
            String sql = "SELECT count(b.id) FROM ( t_bills b LEFT JOIN t_bids c ON (b.bid_id = c.id)) WHERE ( ( b.overdue_mark IN (-(1) ,-(2) ,-(3)) ) AND (c.user_id = ?) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
       
        public int getLackSuditItemCount() {
            String sql = "SELECT count(b.id) FROM t_user_audit_items b WHERE   (   (b.user_id = ?) AND (b.status = 0) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }

        
        
        
		public Double getUser_account() {
			return user_account;
		}

		public Double getFreeze() {
			return freeze;
		}

		public Double getUser_account2() {
			return user_account2;
		}

		public Double getInvest_amount() {
			if(0 == this.invest_amount){
				String sql = "SELECT SUM(a.amount) AS invest_amount FROM t_invests a,t_bids b WHERE a.bid_id = b.id AND b.`status` IN (?, ?, ?, ?, ?, ?) AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.BID_EAIT_LOAN).setParameter(4, Constants.BID_REPAYMENT).setParameter(5, Constants.BID_REPAYMENTS).setParameter(6, Constants.BID_COMPENSATE_REPAYMENT).setParameter(7, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户投资总额时：" + e.getMessage());
					
					return 0d;
				}
				
				this.invest_amount = null == record ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.invest_amount;
		}

		public Integer getInvest_count() {
			if (0 == this.invest_count) {
				String sql = "SELECT COUNT(DISTINCT(a.bid_id)) AS invest_count FROM t_invests a,t_bids b WHERE a.bid_id = b.id AND b.`status` IN (?, ?, ?, ?, ?, ?) AND a.user_id = ? ";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.BID_EAIT_LOAN).setParameter(4, Constants.BID_REPAYMENT).setParameter(5, Constants.BID_REPAYMENTS).setParameter(6, Constants.BID_COMPENSATE_REPAYMENT).setParameter(7, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户投标数量时：" + e.getMessage());
					
					return 0;
				}
				
				this.invest_count = null == record ? 0 : Integer.parseInt(record.toString());
			}
		
			return this.invest_count;
		}

		public Double getReceive_amount() {
			if (this.receive_amount == 0) {
				String sql = "SELECT SUM(a.receive_corpus + a.receive_interest) FROM t_bill_invests a WHERE a.`status` IN (?, ?) AND  a.user_id = ? ";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.NO_RECEIVABLES).setParameter(2, Constants.OVERDUE_NORECEIVABLES).setParameter(3, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户应收账款时：" + e.getMessage());
					
					return 0d;
				}
				
				this.receive_amount = record == null ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.receive_amount;
		}

		public Double getBid_amount() {
			if (0 == this.bid_amount) {
				String sql = "SELECT SUM(a.amount) FROM t_bids a WHERE a.`status` IN (?, ?, ?) AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_REPAYMENT).setParameter(2, Constants.BID_REPAYMENTS).setParameter(3, Constants.BID_COMPENSATE_REPAYMENT).setParameter(4, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户借款总额时：" + e.getMessage());
					
					return 0d;
				}
				
				this.bid_amount = record == null ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.bid_amount;
		}

		public Integer getBid_count() {
			if (0 == this.bid_count) {
				String sql = "SELECT COUNT(1) FROM t_bids a WHERE a.`status` >= ? AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_AUDIT).setParameter(2, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户借款标数量时：" + e.getMessage());
					
					return 0;
				}
				
				this.bid_count = record == null ? 0 : Integer.parseInt(record.toString());
			}
			
			return this.bid_count;
		}

		public Double getRepayment_amount() {
			if (0 == this.repayment_amount) {
				String sql = "SELECT SUM(a.repayment_corpus + a.repayment_interest) FROM t_bills a LEFT JOIN t_bids b ON a.bid_id = b.id WHERE a.`status` IN (?, ?) AND b.user_id = ? ";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.NO_REPAYMENT).setParameter(2, Constants.ADVANCE_PRINCIIPAL_REPAYMENT).setParameter(3, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户应还账款时：" + e.getMessage());
					
					return 0d;
				}
				
				this.repayment_amount = record == null ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.repayment_amount;
		}
	}
	
	

		
	
	/**
	 * 标这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class BidOZ {
   	
  
    	/**
    	 * APP前台查询3个满表中的借款标
    	 * @param error
    	 * @return
    	 */
    	public static List<FullBidsApp> queryFullBid(ErrorInfo error){
    		error.clear();
    		error.code = -1;
    		
    		List<FullBidsApp> fullBids = new ArrayList<FullBidsApp>();
    		
    		
    		return fullBids;
    	}
	}
	
	/**
	 * 理财这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class InvestOZ {

        
        /**
         * 添加每月统计
         * @param error
         * @return
         */
        public static void add(){
            Calendar cal = Calendar.getInstance();
            
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            
            String sql = "insert into t_statistic_bill_invest(invest_id, user_id, year, month, invest_count, average_loan_amount, average_invest_period, average_invest_amount, invest_fee_back, invest_amount) " +
            		"(select `b`.`id` AS `id`,`b`.`user_id` AS `user_id`,year(`b`.`time`) AS `year`,month(`b`.`time`) AS `month`,(select count(`t`.`id`) from `t_invests` `t` where ((`t`.`user_id` = `b`.`user_id`) and (month(`t`.`time`) = month(`b`.`time`)) and (year(`t`.`time`) = year(`b`.`time`)))) AS `invest_count`,(select round((sum(`t1`.`amount`) / count(`t1`.`id`)),2) AS `ROUND((SUM(t1.amount) / COUNT(t1.id)),2)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_loan_amount`,(select round((sum(`t1`.`period`) / count(`t1`.`id`)),0) AS `ROUND((SUM(t1.period) / COUNT(t1.id)),0)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_invest_period`,(select round((sum(`t2`.`amount`) / count(`t2`.`id`)),0) AS `jj` from `t_invests` `t2` where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_invest_amount`,(select round((sum(`t1`.`apr`) / count(`t1`.`id`)),0) AS `ROUND((SUM(t1.apr) / COUNT(t1.id)),0)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `invest_fee_back`,(select sum(`t`.`amount`) from `t_invests` `t` where ((`t`.`user_id` = `b`.`user_id`) and (month(`t`.`time`) = month(`b`.`time`)) and (year(`t`.`time`) = year(`b`.`time`)))) AS `invest_amount` from (`t_invests` `b` join `t_bids` `a` on((`b`.`bid_id` = `a`.`id`))) where (`a`.`status` in (4,5)) and YEAR(b.time) = ? and MONTH(b.time) = ? group by `b`.`user_id`)";
            Query query = JPA.em().createNativeQuery(sql);
            query.setParameter(1, year);
            query.setParameter(2, month);
            
            try {
                query.executeUpdate();
            } catch (Exception e) {
                Logger.error("添加理财子账户理财情况统计表时：" + e.getMessage());
            }
        }
	}
	
	/**
	 * 账单这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class BillOZ {
		
		/**
		 * 后台->我的会员账单
		 */
		public static final String [] C_TYPE = {"and (concat(`f`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ? or name like ? or concat(`e`.`_value`,cast(`a`.`id` AS CHAR charset utf8)) like ?) ","and concat(`f`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ? ","and name like ? ","and concat(`e`.`_value`,cast(`a`.`id` AS CHAR charset utf8)) like ? "};
	}
}