package business.newr;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bank_accounts;
import models.t_bids;
import models.t_bills;
import models.t_dict_bid_repayment_types;
import models.t_invests;
import models.t_system_options;
import models.t_user_bank_accounts;
import models.t_user_details;
import models.newr.t_settlement;
import models.newr.t_users;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import payment.api.tx.marketorder.Tx1341Request;
import payment.api.tx.marketorder.Tx134xResponse;
import payment.api.util.GUIDGenerator;
import payment.api.vo.BankAccount;
import payment.newr.zj.service.impl.PaymentServiceImpl;
import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;
import business.BackstageSet;
import business.DealDetail;
import business.StationLetter;
import business.Supervisor;
import business.TemplatePact;
import business.TemplateStation;
import constants.DealType;
import constants.IPSConstants;
import constants.OptionKeys;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;
import constants.newr.Constants;
import constants.newr.PayType;
import constants.newr.SettleType;
import cpcn.institution.tools.util.XmlUtil;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年3月9日 上午11:56:44
 */
public class Bid implements Serializable{
	public long id;
	private long _id;
	public String bidNo;
	public String merBillNo;
	public String ipsBillNo;
	public boolean ips;
	public boolean auditBid; 
	public boolean createBid;
	public boolean bidDetail;
	public String sign; // 加密ID
	
	public String no; // 编号
	public Date time; // 发布时间
	public String title; // 标题
	public double amount; // 借款金额
	public int periodUnit; // 借款期限单位
	public String strPeriodUnit; // 期限单位字符串 
	public int period; // 借款期限
	public double apr; // 年利率
	public long bankAccountId; // 借款标绑定的银行卡
	public String imageFilename1; // 借款图片
	public String description; // 借款描述
	public int status; // 审核状态
	public String strStatus; // 0审核中 1借款中（审核通过） 2还款中 3已还款 -1审核不通过 -2流标

	public double hasInvestedAmount; // 已投总额
	public double loanSchedule; // 借款进度比例
	public double minInvestAmount; // 最低金额招标
	public double hasSettleAmount; // 平均金额招标
	public double minAllowInvestAmount;// 最低允许投标金额

	public double maxLoan = 0; // 固定奖金
	public double investBonus;//投标资金
    private double investRate; 
	public int type; // 

	public double serviceFees; // 服务费
	public int investPeriod; // 满标期限
	public Date investExpireTime; // 满标日期
	public Date beginInterest; // 实际满标日期
	
	public Date repaymentTime;  //还款日期
	public Date recentRepayTime;//最近还款时间
	public boolean hasOverdue;//是否有逾期的还款
	
	public long allocationSupervisorId; // 审核人
	public long manageSupervisorId; // 分配审核人
	public Date auditTime; // 审计时间
	public String auditSuggest; // 审核意见
	

	public String qr_code;//二维码标识
	
	public String smallImageFilename; // 产品小图片路径
	
	public long userId; // 用户ID
	private long _userId; // 用户ID
	public String signUserId; // 加密用户ID
	public String userName; // 用户名
	public User user; // 用户实体类


	public Repayment repayment; // 还款类型

	
	public int investCount; // 投标次数
	public int questionCount; // 提问记录次数
	public double userItemPassScale; // 用户资料通过比例(bid详情,在此使用get方法)
	public boolean isReleaseSign = false;
	public boolean isRepair;//是否补单
	
	public boolean auditBidPact; // 借款合同

	public Long productId;
	//v7.2.6 add 
	public int ipsStatus;  //资金托管交易状态：0正常，1标的结束处理中
	public PaymentServiceImpl paymentServiceImpl = new PaymentServiceImpl();

	/**
	 * 获取_id
	 */
	public long getId(){
		return this._id;
	}
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		if(null == this.sign)
			this.sign = Security.addSign(this.id, Constants.BID_ID_SIGN);
		
		return this.sign;
	}

	/**
	 * 获取加密用户ID
	 */
	public String getSignUserId() {
		if(null == this.signUserId) 
			this.signUserId = Security.addSign(this.userId, Constants.USER_ID_SIGN);
		
		return this.signUserId;
	}

	/**
	 * 获取用户ID
	 */
	public long getUserId() {
		return this._userId;
	}
	
	/**
	 * 获取用户名
	 */
	public String getUserName(){
		if(null == this.userName) {
			this.userName = User.queryUserNameById(this.userId, new ErrorInfo());
		}
		
		return this.userName;
	}
	
	/**
	 * 获取编号
	 */
	public String getNo() {
		if(null == this.no) {
			ErrorInfo error = new ErrorInfo();

			String _no = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error); // 编号

			if (null == _no) {
				Logger.error("标->填充自己,获取常量:" + error.msg);
				_no = "JK";
			}
		
			this.no = _no + this.id;
		}
		
		return this.no;
	}
	
	/**
	 * 获取期限单位
	 */
	public String getStrPeriodUnit() {
		if(null == this.strPeriodUnit) {
			switch(this.periodUnit){
				case Constants.YEAR: this.strPeriodUnit = "年"; break;
				case Constants.MONTH: this.strPeriodUnit = "个月"; break;
				case Constants.DAY: this.strPeriodUnit = "天"; break;
				default: this.strPeriodUnit = "有误!"; break;
			}
		}
		
		return this.strPeriodUnit;
	}

	/**
	 * 填充自己的用户对象
	 */
	public void setUserId(long userId) {
		this._userId = userId;
		
		if(null == this.user){
			this.user = new User();
			this.user.createBid = this.createBid; 
			this.user.id = userId; 
		}
	}
	

	/**
	 * 填充自己
	 */
	public void setId(long id) {
		t_bids tbid = null;

		try {
			tbid = t_bids.findById(id);
		} catch (Exception e) {
			Logger.error("标->标填充自己:" + e.getMessage());
			return;
		}

		if (null == tbid) {
			this._id = -1;
			
			return;
		}
		
		this._id = id;
		this.bidNo = tbid.bid_no;
		this.merBillNo = tbid.mer_bill_no;
		this.ipsBillNo = tbid.ips_bill_no;
		if (this.ips) {
			this.amount = tbid.amount;
			this._userId = tbid.user_id;
			this.serviceFees = tbid.service_fees;
			this.apr = tbid.apr;
			this.hasInvestedAmount = tbid.has_invested_amount;
			
			return;
		}
		
		/* 标审核加载项 */
		if(this.auditBid){
			this.title = tbid.title;
			this.amount = tbid.amount;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
			this.status = tbid.status;
			this.investPeriod = tbid.invest_period;
			this.serviceFees= tbid.service_fees;
			this.maxLoan = tbid.max_loan;
			this.apr = tbid.apr;
			this.hasInvestedAmount = tbid.has_invested_amount;
			this.loanSchedule = tbid.loan_schedule;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;
			this.createBid = true; // 加载部分用户项
			this.userId = tbid.user_id;			
			return;
		}
		
		if(this.auditBidPact){
			this.amount = tbid.amount;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
			this.apr = tbid.apr;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;
			this.createBid = true; // 加载部分用户项
			this.userId = tbid.user_id;
		}
		
		/* 标详情加载项 */
		if(this.bidDetail) {
			this.title = tbid.title;
			this.amount = tbid.amount;
			this.apr = tbid.apr;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
	
			this.loanSchedule = tbid.loan_schedule;
			this.description = tbid.description;
			this.maxLoan= tbid.max_loan; // 固定奖金
			this.investExpireTime = tbid.invest_expire_time;
			this.hasInvestedAmount = tbid.has_invested_amount;
			this.bankAccountId = tbid.bank_account_id;
			this.status = tbid.status;
			this.auditSuggest = tbid.audit_suggest;
			this.investPeriod = tbid.invest_period;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;

	
			this.userId = tbid.user_id;
	
			double _amount2 = tbid.amount;
			double _hasInvestedAmount2 = tbid.has_invested_amount;
			
			if (tbid.min_invest_amount > 0) {
				double _minInvestAmount = tbid.min_invest_amount;

				this.minInvestAmount = _minInvestAmount; // 最低金额招标

				/* 算出最低允许投标金额 */
				if (_amount2 - _hasInvestedAmount2 < _minInvestAmount) {
					this.minAllowInvestAmount = _amount2 - _hasInvestedAmount2;
				} else {
					this.minAllowInvestAmount = _minInvestAmount;
				}
			}
			
			return;
		}

		double _amount = tbid.amount;
		double _hasInvestedAmount = tbid.has_invested_amount;
		
		if (tbid.min_invest_amount > 0) {
			double _minInvestAmount = tbid.min_invest_amount;

			this.minInvestAmount = _minInvestAmount; // 最低金额招标

			/* 算出最低允许投标金额 */
			if (_amount - _hasInvestedAmount < _minInvestAmount) {
				this.minAllowInvestAmount = _amount - _hasInvestedAmount;
			} else {
				this.minAllowInvestAmount = _minInvestAmount;
			}
		}
		this.hasSettleAmount= tbid.has_settle_amount; // 已经结算金额
        this.investRate =tbid.invest_rate;	
		this.userId = tbid.user_id; // 用户ID
		this.repayment = new Repayment();
		this.repayment.id = tbid.repayment_type_id; // 返款类型
		this.time = tbid.time; // 发布时间
		this.title = tbid.title; // 标题
		this.amount = _amount; // 借款金额
		this.periodUnit = tbid.period_unit; // 借款期限单位
		this.period = tbid.period; // 借款期限
		this.investPeriod = tbid.invest_period; // 投标期限
		this.investExpireTime = tbid.invest_expire_time; // 满标日期
		this.beginInterest = tbid.begin_interest; // 气息日
		this.apr = tbid.apr; // 年利率
		this.bankAccountId = tbid.bank_account_id; // 借款标绑定的银行卡
		this.serviceFees = tbid.service_fees;//服务费
		this.description = tbid.description; // 借款描述
		this.status = tbid.status; // 状态值
		this.loanSchedule = tbid.loan_schedule; // 借款进度比例
		this.hasInvestedAmount = _hasInvestedAmount; // 已投总额
		this.auditTime = tbid.audit_time; // 审核时间
		this.auditSuggest = tbid.audit_suggest;// 审核意见
		this.type = tbid.type; // 发布方式
		this.qr_code = tbid.qr_code;		
		this.repaymentTime = tbid.repayment_time;
		this.settlementCount = tbid.settlement_count;
		this.beginInterest = tbid.begin_interest;
		this.productId = tbid.product_id;
	}
	
	/**
	 * 修改标状态为放款中状态码 2
	 */
	public static int updateStatus(long bidId) throws SQLException{
		int row=-1;
		String sql = "update t_bids set status=2 where id= ? ";
		JpaHelper.execute(sql, bidId).executeUpdate();
		return row;
	}
	public Integer settlementCount;
	
	 /**
		 * 满标待放款中的标列表
		 * @param pageBean 分页对象
		 * @param error 信息值
		 * @param uid >0:根据用户ID查询,<0:查询所有
		 * @param str 可变参数值
		 * @return List<v_bid_auditing>
		 */
		public static List<t_bids> queryBidAuditing(PageBean<t_bids> pageBean, ErrorInfo error) {
			error.clear();
			int count = -1;			
			List<t_bids> results = t_bids.find(" status = 1  and invest_expire_time<now() ").fetch();
			for (t_bids bid : results) {
				String sql="update t_bids set status=3 where id= ? ";
				JpaHelper.execute(sql, bid.id).executeUpdate();
			}
			StringBuffer sql = new StringBuffer("SELECT count(b.id) FROM  t_bids b	 "
					+ " where b.status = 3 ");
			EntityManager em = JPA.em();
	        Query query = em.createNativeQuery(sql.toString());
	        List<?> list = null;
	        try {
				list = query.getResultList();
			} catch (Exception e) {
				Logger.error("满标待放款,查询总记录数:" + e.getMessage());
				error.msg = "加载满标待放款标列表失败!";
				return null;
			}
			
			count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
			
			if(count < 1)
				return new ArrayList<t_bids>();
				
			pageBean.totalCount = count;
			
		
	        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
	        query.setMaxResults(pageBean.pageSize);
	        
			try {
				List<t_bids> result = 
						t_bids.find(" status = 3  ").fetch(pageBean.currPage , pageBean.pageSize);
				return result;
			} catch (Exception e) {
				Logger.error("标->审核中的标列表,查询列表:" + e.getMessage());
				error.msg = "加载审核中的借款标列表失败!";
				return null;
			}
		}

	/**
	 * 查询是否有逾期还款
	 */
	public boolean isHasOverdue() {
		Long count = null;
		String sql = "select count(1) from t_bills where bid_id = ? and overdue_mark <> 0";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql).setParameter(1, this.id);
		Object record = null;

		try {
			record = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("标->查询是否有逾期还款:" + e.getMessage());
			
			return false;
		}
		
		if(record == null)
			return false;
		
		count = Long.parseLong(record.toString());
		
		return  count > 0 ? true : false;
	}
	
	/**
	 * 获取近期还款时间
	 */
	public Date getRecentRepayTime() {
		if(null == this.recentRepayTime){
			String hql = "select repayment_time from t_bills where bid_id = ? order by repayment_time";

			try {
				this.recentRepayTime = t_bills.find(hql, this.id).first();
			} catch (Exception e) {
				Logger.error("标->获取近期还款时间:" + e.getMessage());
				
				return null;
			}
		}
		
		return this.recentRepayTime;
	}

	/**
	 * 投标次数
	 */
	public int getInvestCount() {
		if(0 == this.investCount) {
			try {
				this.investCount = Integer.parseInt(t_invests.find("SELECT COUNT(DISTINCT t.user_id) FROM t_invests t WHERE t.bid_id = ?", this.id).first().toString());
			} catch (Exception e) {
				Logger.error("标->投标次数:" + e.getMessage());
	
				return 0;
			}
		}
		
		return this.investCount;
	}
	
	/**
	 * 提问次数
	 */
	public int getQuestionCount(){
		return 0;
	}	
	/**
	 * 是否在托管方登记
	 * @param error
	 * @return
	 */
	public boolean isRegisterSuccess(ErrorInfo error) {
		error.clear();
		
		return true;
	}
	/**
	 * 检查当前已投金额和借款金额是否相等
	 */
	private static long checkBidStatus(long bidId){
		Long id = null;
		String hql = "select id from t_bids where id = ? and has_invested_amount = amount and loan_schedule = 100";
		
		try {
			id = t_bids.find(hql, bidId).first();
		} catch (Exception e) {
			return 0;
		}
		 
		if(null == id)
			return 0;
		
		return id;
	}
	
	
	private String auditToadvanceLoanNotice(String content){
		content = content.replace("userName", this.user.name); 
		content = content.replace("date", DateUtil.dateToString((new Date()))); 
		content = content.replace("title", this.title); 
		content = content.replace("status", this.strStatus); 
		
		return content;
	}
		
	/**
	 * 满标->待放款,通知
	 */
	private String fundraiseToEaitLoanNotice(String content){
		content = content.replace("userName", this.user.name); 
		content = content.replace("date", DateUtil.dateToString((new Date()))); 
		content = content.replace("title", this.title); 
		content = content.replace("amount",  DataUtil.formatString(this.amount)); 
		content = content.replace("servicefees",  DataUtil.formatString(this.serviceFees)); 
		
		double excitationSum = this.maxLoan;	   
		
		content = content.replace("excitationSum", excitationSum + "");
		content = content.replace("repaymentType", this.repayment.name);
		content = content.replace("period", this.period + "");
		content = content.replace("unit", this.strPeriodUnit);
		content = content.replace("apr", this.apr + "");
		
		return content;
	}
	
	public void doEaitLoanToRepayment(ErrorInfo error,Double amount){
		error.code = -1;		
		/* 修改状态 */
		String hql = "update t_bids set status=?,settlement_count=settlement_count+?,has_settle_amount =has_settle_amount+? where id=?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.BID_REPAYMENT);// 审核状态为还款中 
		query.setParameter(2, 1);
		query.setParameter(3, amount);
		query.setParameter(4, this.id); // 标ID
		int row = 0;		
		try {
			row = query.executeUpdate();			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->待放款->还款中:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();			
			return;
		}
		/* 生成账单 */
		row = new Bill().addBill(this,this.beginInterest, error);

		if(error.code < 0){
			error.msg = "生成账单失败!";;
			JPA.setRollbackOnly();

			return;
		}
				
		/* 更新标的还款日期*/
		row = 0;
		String sql = "update t_bids set repayment_time = ? where id = ?";
		query = JPA.em().createQuery(sql).setParameter(1, this.recentRepayTime).setParameter(2, this._id);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->待放款->还款中：" + e.getMessage());
			error.msg = "修改标的还款日期失败！";
			JPA.setRollbackOnly();
			
			return;
		}		
		if (row < 1) {
			error.msg = "审核失败！";
			JPA.setRollbackOnly();
			
			return;
		}
		error.code = 1;
		error.msg = "审核成功,已将标置为[还款中]!";
		
	}
	
	

	public void deleteIPSBid(String operation) {
		Cache.delete("bid_"+operation+"_"+this.bidNo);
	}

	
	/**
	 * 修改满标期限
	 */
	private static int addInvestExpireTime(long bid, int investPeriod) {
		String hql = "update t_bids set invest_expire_time = ? where id = ?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, DateUtil.dateAddDay(new Date(), investPeriod));
		query.setParameter(2, bid);

		try {
			return query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->修改满标期限:" + e.getMessage());

			return -1;
		}
	}



	/**--------------------------------------------------还款类型-----------------------------------------------------------------*/
	
	public static class Repayment implements Serializable{
		public long id; // 还款类型ID
		public long _id;
		public String name; // 还款类型名称
		public boolean isUse; // 是否启用
		
		/**
		 * 获取还款类型名称
		 */
		public String getName() {
			if(null == this.name) {
				String hql = "select name from t_dict_bid_repayment_types where id = ?";
				
				try {
					this.name = t_dict_bid_repayment_types.find(hql, this.id).first();
				} catch (Exception e) {
					Logger.error("标->获取还款用途名称:" + e.getMessage());
					
					return null;
				}
			}
			
			return this.name;
		}
		
		
		/**
		 * 获取 还款类型列表
		 * @param info 错误信息
		 * @param id 还款类型ID
		 * @return 标对象集合
		 */
		public static List<Repayment> queryRepaymentType(String [] repaymentTypeId, ErrorInfo error) {
			List<Repayment> repayments = new ArrayList<Repayment>();
			List<t_dict_bid_repayment_types> tbids = null;

			String hql = "select new t_dict_bid_repayment_types(id, name, is_use) "
					+ "from t_dict_bid_repayment_types";

			if (repaymentTypeId != null && repaymentTypeId.length > 0) {
				hql += " where id in(";

				for (String id : repaymentTypeId) {
					hql += id + ","; // 还款类型数量很少，直接用String拼接
				}

				hql = hql.substring(0, hql.length() - 1);
				hql += ")";
			}

			try {
				tbids = t_dict_bid_repayment_types.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取还款类型列表:" + e.getMessage());
				
				error.code = -1;
				error.msg = "标->获取还款类型列表有误！";
				
				return null;
			}
			
			Repayment repayment = null;
			
			for (t_dict_bid_repayment_types type : tbids) {
				repayment = new Repayment();

				repayment.id = type.id;
				repayment.name = type.name;
				repayment.isUse = type.is_use;

				repayments.add(repayment);
			}
			
			return repayments;
		}
		
		/**
		 * 获取 还款类型列表(APP端)
		 * @param info 错误信息
		 * @param id 还款类型ID
		 * @return 标对象集合
		 */
		public static List<Repayment> queryRepaymentTypeApp(ErrorInfo error) {
			error.clear();
			
			List<Repayment> repayments = new ArrayList<Repayment>();
			List<t_dict_bid_repayment_types> tbids = null;

			String hql = "select new t_dict_bid_repayment_types(id, name, is_use) "
					+ "from t_dict_bid_repayment_types where is_use = true";

			try {
				tbids = t_dict_bid_repayment_types.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取还款类型列表:" + e.getMessage());
				
				error.code = -1;
				error.msg = "标->获取还款类型列表有误！";
				
				return null;
			}
			
			Repayment repayment = null;
			
			for (t_dict_bid_repayment_types type : tbids) {
				repayment = new Repayment();

				repayment.id = type.id;
				repayment.name = type.name;
				repayment.isUse = type.is_use;

				repayments.add(repayment);
			}
			
			return repayments;
		}
		
		/**
		 * 显示/隐藏 还款类型
		 * @param id 还款类型ID
		 * @param isUse 状态值
		 * @param error 错误信息
		 */
		public static void editRepaymentType(long rid, boolean isUse, ErrorInfo error) {
			error.clear();
			
			String hql = "update t_dict_bid_repayment_types set is_use=? where id=?";
			Query query = JPA.em().createQuery(hql);
			query.setParameter(1, isUse);
			query.setParameter(2, rid);
			
			try {
				error.code = query.executeUpdate();
			} catch (Exception e) {
				Logger.error("标->显示/隐藏 :" + e.getMessage());
				error.msg = "设置失败!";

				return;
			}
			
			if(error.code < 1){
				error.msg = "设置失败!";
				
				return;
			}
			
			/* 添加事件 */
			if(isUse)
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_REPAYMENT_TYPE, "启用还款类型", error);
			else
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_REPAYMENT_TYPE, "不启用还款类型", error);
			
			if(error.code < 0){
				error.msg = "设置失败!";
				JPA.setRollbackOnly();
				
				return;
			}
		}
	}


	/* 2014-11-14 */
	/**
	 * 借款合同
	 */
	public static String queryPact(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select pact from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * 居间服务协议
	 * @param id
	 * @return
	 */
	public static String queryIntermediaryAgreement(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select intermediary_agreement from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * 保障涵
	 * @param id
	 * @return
	 */
	public static String queryGuaranteeBid(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select guarantee_bid from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}
	
	/**
	 * 生成借款合同
	 */
	public boolean createPact(){
		/*1.生成借款合同*/
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_BID;
		String content = null;
		Date date = new Date();
		
		if(pact.is_use) {
			content = pact.content;
			
			if(StringUtils.isBlank(content))
				return false;
			
			BackstageSet set = BackstageSet.getCurrentBackstageSet();
			DecimalFormat myformat = new DecimalFormat();
			myformat.applyPattern("##,##0.00");
			String pact_no = this.id + DateUtil.simple(date);
			Object _amount = 0;
			
			String sql = "select sum(receive_corpus + receive_interest) from t_bill_invests where bid_id = ?";
			Query query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			
			try {
				_amount = query.getSingleResult();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询借款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == _amount)
				return false;
				
			String repayTime =ServiceFee.repayTime(this.periodUnit,this.period, (int)this.repayment.id);
				
			content = content.replace("pact_no", pact_no).
					  replace("date", DateUtil.dateToString(date)).
					  replace("loan_name", this.user.realityName == null ? "" : this.user.realityName).
					  replace("id_number", this.user.idNumber == null ? "" : this.user.idNumber).
					  replace("company_name", set.companyName).
					  replace("amount", myformat.format(this.amount)).
					  replace("apr", this.apr + "").
					  replace("period", this.period + "").
					  replace("unit", this.strPeriodUnit).
					  replace("capital_interest_sum", _amount.toString()).
					  replace("repayment_name", this.repayment.name == null ? "" : this.repayment.name).
					  replace("repayment_time", repayTime);
			
			/* 查询借款信息，事务还没有提交，一定要用原生SQL查询  */
		
			/* 1-16,问题：一个人对同一标投多次,生成合同的时候金额统计错误 */
			sql = "select u.`name`, i.amount, i.time, tmp.receive_time, tmp.total_amount "
					+ "from t_invests i join(select invest_id, receive_time, "
					+ "sum(receive_interest + receive_corpus + overdue_fine) as total_amount "
					+ "from t_bill_invests where bid_id = ? GROUP BY invest_id) tmp "
					+ "on i.id = tmp.invest_id join t_users u on i.user_id = u.id and i.bid_id = ?";
			
			
			query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			query.setParameter(2, this.id);
			List<Object[]> lists = null;
			
			try {
				lists = query.getResultList();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询借款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == lists || lists.size() == 0)
				return false;
			
			StringBuffer buffer = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
			buffer.append("<tr height=\"36\"><td>投资人名称</td><td>投资金额(RMB)</td><td>年利率</td><td>投资日期</td><td>还款截止日</td><td>本息合计总金额(RMB)</td></tr>");
			
			for (Object[] pa : lists) {
				buffer.append("<tr height=\"30\"><td>")
					  .append(pa[0])
					  .append("</td><td>")
				 	  .append(pa[1])
				 	  .append("</td><td>")
				 	  .append(this.apr)
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[2]))
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[3]))
				 	  .append("</td><td>")
				 	  .append(pa[4])
				  	  .append("</td></tr>");
			}
			
			buffer.append("</table>");
			content = content.replace("invest_list", buffer.toString());
			
			/* 查询还款信息，事务还没有提交，一定要用原生SQL查询  */
			sql = "SELECT (SELECT CONCAT(CONVERT(t.periods, char),'/',CONVERT(max(t2.periods), char)) as period FROM t_bills t2 WHERE t2.bid_id = t.bid_id) as period, "+ 
				  "t.repayment_time as repayment_time, "+
				  "t.repayment_corpus as repayment_corpus, "+
				  "t.repayment_interest as repayment_interest, "+
                  "(SELECT SUM(t2.repayment_corpus + t2.repayment_interest) as total_amount FROM t_bills t2 WHERE t2.bid_id = t.bid_id and t2.id = t.id) as perioda "+
				  "from t_bills t "+
				  "where t.bid_id = ?"; 
			
			query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			
			try {
				lists = query.getResultList();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询还款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == lists || lists.size() == 0)
				return false;
			
			buffer = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tbody>");
			buffer.append("<tr height=\"36\"><td>期数</td><td>年利率</td><td>应还时间</td><td>应还本金</td><td>还款利息</td><td>应还本息合计</td></tr>");
			
			for (Object[] pa : lists) {
				buffer.append("<tr height=\"30\"><td>")
					  .append(pa[0])
					  .append("</td><td>")
				 	  .append(this.apr)
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[1]))
				 	  .append("</td><td>")
				 	  .append(pa[2])
				 	  .append("</td><td>")
				 	  .append(pa[3])
				 	  .append("</td><td>")
				 	  .append(pa[4])
				  	  .append("</td></tr>");
			}
			
		    buffer.append("</tbody></table>");
		    content = content.replace("repayment_list", buffer.toString());
			
			sql = "update from t_bids set pact = ? where id = ?";
			query = JPA.em().createQuery(sql);
			query.setParameter(1, content);
			query.setParameter(2, this.id);
			int row = 0;
			
			try {
				row = query.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(修改)!" + e.getMessage());
				
				return false;
			}
			
			if(row < 1)
				return false;
			
			
		}

		return true;
	}

	/**
	 * 查询理财费率
	 * @param id
	 * @return
	 */
	public static double queryInvestRate(long id) {
		String sql = "select invest_rate from t_bids where id = ?";
		Double fee = null;
		
		try {
			fee = t_bids.find(sql, id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		return fee == null ? 0 : fee;
	}
	
	/**
	 * 查询逾期费率
	 * @param id
	 * @return
	 */
	public static double queryOverdueRate(long id) {
		String sql = "select overdue_rate from t_bids where id = ?";
		Double fee = null;
		
		try {
			fee = t_bids.find(sql, id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		return fee == null ? 0 : fee;
	}
	
	/**
	 * 根据理财ID查询费率
	 * @param id
	 * @param status 1:理财费率；2：逾期费率
	 * @return
	 */
	public static double queryRateByInvestId(long id, int status) {
		String sql = null;
		
		if(1 == status)
			sql = "select invest_rate from t_bids where id = (select bid_id from t_invests where id = ?)";
		else
			sql = "select overdue_rate from t_bids where id = (select bid_id from t_invests where id = ?)";
		
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, id);
		List<?> list = null;
		Object fee = null;
		
		try {
			list = query.getResultList();
			fee = list == null ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		if(null == fee)
			return 0;
		
		return Double.parseDouble(fee.toString());
	}
	
	/**
	 * 根据借款账单ID查询费率
	 * @param id
	 * @param status 1:理财费率；2：逾期费率
	 * @return
	 */
	public static double queryRateByBillId(long id, int status) {
		String sql = null;
		
		if(1 == status)
			sql = "select invest_rate from t_bids where id = (select bid_id from t_bills where id = ?)";
		else
			sql = "select overdue_rate from t_bids where id = (select bid_id from t_bills where id = ?)";
		
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, id);
		List<?> list = null;
		Object fee = null;
		
		try {
			list = query.getResultList();
			fee = list == null ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		if(null == fee)
			return 0;
		
		return Double.parseDouble(fee.toString());
	}
	
	public static boolean queryIsRegisterGuarantor(long id) {
		try {
			return t_bids.find("select is_register_guarantor from t_bids where id = ?", id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return false;
		}
	}
	
	/**
	 * 查询标的信息用于投资
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static Bid queryBidForInvest(long bidId, ErrorInfo error) {
		error.clear();
		String sql = "select id, user_id, bid_no, loan_purpose_id, amount, has_invested_amount, service_fees, "
				+ "apr from t_bids where id = ? limit 1";
		Object[] obj = null;
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();
		} catch (Exception e) {
			Logger.error("查询标的信息失败：%s", e.getMessage());
			error.code = -1;
			error.msg = "查询标的信息失败";
			
			return null;
		}
		
		if(obj == null || obj.length < 8) {
			error.code = -1;
			error.msg = "查询标的信息失败";
			
			return null;
		}
		
		Bid bid = new Bid();
		
		bid.id = ((BigInteger) obj[0]).longValue();
		bid.userId = ((BigInteger) obj[1]).longValue();
		bid.bidNo =  obj[2] == null ? null : obj[2].toString();
		bid.amount =  ((BigDecimal) obj[4]).doubleValue();
		bid.hasInvestedAmount = ((BigDecimal) obj[5]).doubleValue();
		bid.serviceFees = ((BigDecimal) obj[6]).doubleValue();
		bid.apr = ((BigDecimal) obj[7]).doubleValue();
		
		error.code = 1;
		
		return bid;
	}	
	/**
	 * 更新资金托管交易状态
	 * @param bidId  标的id
	 * @param ipsStatus  交易状态
	 * @param currentStatus  当前必须是currentStatus状态，才能执行更新
	 */
	public static void updateIPSStatusByID(long bidId, int ipsStatus, int currentStatus){
		String sql = "update t_bids set ips_status = ? where id = ? and ips_status = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, ipsStatus).setParameter(2, bidId).setParameter(3, currentStatus);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error("更新资金托管交易(bids)状态("+ currentStatus +"-->"+ ipsStatus +")时，%s", e.getMessage());
		}
	}	
	/**
	 * 获取分摊后奖励金额, 及分摊后借款管理费
	 * @param bid Bid
	 * @param money 投资金额
	 * @param error
	 * @return
	 */
	public static Map<String, Double>  queryAwardAndBidFee(Bid bid, double money, ErrorInfo error)
	{
		
		Map<String, Double> map = new HashMap<String, Double>();
		
		double result = 0;  // add v8.0.1  不奖励，扣除金额为0

		map.put("award", result);
		
		//分摊借款管理费
		double bid_fee = (money / bid.amount ) * bid.serviceFees ;
		
		map.put("bid_fee", bid_fee);
		
		return map;
	}
	
	/**
	 * 查询借款标的总借款手续费，及总借款奖励
	 * @param bid
	 * @param error
	 * @return
	 */
	public static Map<String, Double> queryBidAwardAndBidFee(Bid bid, ErrorInfo error){
		
		Map<String, Object> map = JPAUtil.getMap(error, " select SUM(i.award) as award, SUM(i.bid_fee) as bid_fee from t_invests i where i.bid_id = ? ", bid.id);
		Map<String, Double> dataMap = new HashMap<String, Double>();		
		Double  award =map.get("award")==null?0.00:((BigDecimal)map.get("award")).doubleValue();
		Double  bid_fee =map.get("bid_fee")==null?0.00:((BigDecimal)map.get("bid_fee")).doubleValue();
		dataMap.put("award",award );
		dataMap.put("bid_fee", bid_fee);
		
		return dataMap;
		
	}	
	/**
	 * 根据第三方流水号查询标的信息Bean
	 * @date 2015-07-03
	 * @author yangxuan
	 * @param merBillNo
	 * @return
	 */
	public static t_bids queryBidByMerBillNo(String merBillNo){
		t_bids bid = t_bids.find("mer_bill_no=?", merBillNo).first();
		return bid;
	}	
	/**
	 * 累计投资金额
	 * @return
	 */
	public static BigDecimal sumInvest() {
		String sql="select sum(has_invested_amount) from t_bids";
		Query query =  JPA.em().createNativeQuery(sql);

		return query.getSingleResult()==null?new BigDecimal(0):(BigDecimal) query.getSingleResult();
	}	
	/**
	 * 查询借款标期限
	 * 
	 * @param bidId
	 * @return
	 */
	public static Bid queryPeriodByBidId(long bidId){		
		Map<String, Object> bidMap = new HashMap<String, Object>();
		String sql = "select new Map(period as period, period_unit as period_unit) from t_bids where id = ? ";
		try {
			bidMap =null;//t_users.find(sql, bidId).first();
		} catch (Exception e) {
			Logger.error("查询借款标期限时，%s", e.getMessage());

			return null;
		}
		if (null == bidMap || bidMap.size() == 0) {

			return null;
		}
		Bid bid = new Bid();
		bid.period = (Integer) bidMap.get("period");
		bid.periodUnit = (Integer) bidMap.get("period_unit");
		
		return bid;
	}

	public static int queryBidStatus(long id) {
		
		String sql = "SELECT status FROM t_bids WHERE id = ?";
		
		Object status = null;
		
		try {
			status = JPA.em().createNativeQuery(sql).setParameter(1, id).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 404;
		}
		
		return status==null?404:Integer.parseInt(status.toString());
	}
	//满标放款通知
	public void afterReleaseBid(ErrorInfo error,Date settleTime,Document doc){
		 try {
			String serialNumber  = XmlUtil.getNodeText(doc, "SerialNumber");
			String transferTime  =XmlUtil.getNodeText(doc, "TransferTime");
			 String Amount = XmlUtil.getNodeText(doc, "Amount");			
	      JPA.em().createNativeQuery("update t_settlement set status = ? where serial_number =? ")
	     .setParameter(1, DealType.DealStatus.SECCUSS.getValue())
	     .setParameter(2, serialNumber).executeUpdate();	
		doEaitLoanToRepayment(error,Double.parseDouble(Amount));    
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("满标放款通知回调失败", e.getStackTrace());
		}
	}
    //满标放款 (后期需要优化 针对标未满放款 要多一个状态 ，避免和投标产生并发现象)
	public List<t_settlement> releaseBid(ErrorInfo error) {
		//1 分账
		List<t_settlement>  resultList = new ArrayList<t_settlement>();
		try {
			Bid.updateStatus(this.id);
			t_users user = t_users.find("id=?", this.userId).first();
			t_user_bank_accounts accounts = t_user_bank_accounts.find("user_id=?", user.getId()).first();
			t_settlement settleLoan = new t_settlement();
			settleLoan.amount  = calculateLoanerMoney(this.hasInvestedAmount,this.investRate,this.apr,this.period);
			settleLoan.settle_type=SettleType.SETTLEUSER;
			settleLoan.status = DealType.DealStatus.INIT.getValue();
			settleLoan.time = new Date();
			settleLoan.user_id =user.getId();
			settleLoan.bid_id = this.id;
			settleLoan.serial_number = GUIDGenerator.genGUID();
			settleLoan.save();
			resultList.add(settleLoan);
			t_settlement settleBank = new t_settlement();
			settleBank.amount  = this.hasInvestedAmount-settleLoan.amount;
			settleBank.settle_type=SettleType.SETTLEBANK;
			settleBank.status = DealType.DealStatus.INIT.getValue();
			settleBank.time = new Date();
			settleBank.user_id =0l;
			settleLoan.bid_id = this.id;
			settleLoan.serial_number = GUIDGenerator.genGUID();
			settleBank.save();
			resultList.add(settleBank);
			settleBid(error,user,accounts,settleLoan,settleBank);
		} catch (Exception e) {
          Logger.error("结算 -分账", e.getStackTrace());
		}
		return resultList;
	}
	//满标放款结算到借款人账户
	private Long settleBid(ErrorInfo error,t_users user,
			t_user_bank_accounts accounts,t_settlement settleLoan,
			t_settlement settleBank){
		try {
			// 1.取得参数
			String institutionID = Constants.INSTITUTIONID;
			String serialNumber = settleLoan.serial_number;
			String orderNo = this.merBillNo;
			Long amount = settleLoan.amount.longValue();
			int accountType = 12;//TODO 根据 账户类型判断
			String bankID = accounts.bank_code+"";
			String accountName = accounts.account_name;
			String accountNumber =accounts.account; 
			String branchName = accounts.branch_bank_name;
			String province = accounts.province;
			String city = accounts.city;          
			// 2.创建交易请求对象
			Tx1341Request tx1341Request = new Tx1341Request();
			tx1341Request.setInstitutionID(institutionID);
			tx1341Request.setSerialNumber(serialNumber);
			tx1341Request.setOrderNo(orderNo);
			//账户成分
			tx1341Request.setAmount(amount*100);
			tx1341Request.setAccountType(accountType);     
			BankAccount bankAccount = new BankAccount();
			bankAccount.setBankID(bankID);
			bankAccount.setAccountName(accountName);
			bankAccount.setAccountNumber(accountNumber);
			bankAccount.setBranchName(branchName);
			bankAccount.setProvince(province);
			bankAccount.setCity(city);
			tx1341Request.setBankAccount(bankAccount);			
			Tx134xResponse tx134xResponse = settleBid(tx1341Request);
			if("2000".equals(tx134xResponse.getCode())){
				//放款到银行
				t_bank_accounts bankAccounts = (t_bank_accounts) t_bank_accounts.findAll().get(0);				
				Tx1341Request tx1341RequestOfBank = new Tx1341Request();
				tx1341RequestOfBank.setInstitutionID(institutionID);
				tx1341RequestOfBank.setSerialNumber(settleBank.serial_number);
				tx1341RequestOfBank.setOrderNo(this.merBillNo);
				//账户成分
				tx1341RequestOfBank.setAmount(settleBank.amount.longValue()*100);
				tx1341RequestOfBank.setAccountType(accountType);     
				BankAccount bankAccountOfBank = new BankAccount();
				bankAccountOfBank.setBankID(bankAccounts.bank_code+"");
				bankAccountOfBank.setAccountName(bankAccounts.account_name);
				bankAccountOfBank.setAccountNumber(bankAccounts.account);
				bankAccountOfBank.setBranchName(bankAccounts.branch_bank_name);
				bankAccountOfBank.setProvince(bankAccounts.province);
				bankAccountOfBank.setCity(bankAccounts.city);
				tx1341Request.setBankAccount(bankAccountOfBank);
				Tx134xResponse Tx134xResponseOfbank = settleBid(tx1341RequestOfBank);
				if("2000".equals(Tx134xResponseOfbank.getCode())){
					JPA.em().createNativeQuery("update t_bids set status=? where mer_bill_no=?")
					.setParameter(1, Constants.BID_PRE_REPAYMENT).setParameter(1, this.merBillNo).executeUpdate();
					error.code=1;
				}else{
					error.code=-3;
					error.msg=Tx134xResponseOfbank.getMessage();
				}
			}else{
				error.code=-2;
				error.msg=tx134xResponse.getMessage();
			}
		} catch (Exception e) {
			Logger.error("满标放款", e.getStackTrace());	
			error.code=-2;
			error.msg ="";
		}
		return null;
	}
	
	private Tx134xResponse settleBid(Tx1341Request tx1341Request){
		Map<String, String> paramMap = new HashMap<String, String>();
	    paramMap.put("UsrCustId", user.id+"");
	    paramMap.put("parentOrderno", tx1341Request.getSerialNumber());
	    paramMap.put("MerPriv", tx1341Request.getOrderNo());
	    paramMap.put("serialNumber", tx1341Request.getSerialNumber());
	    paramMap.put("Amount", tx1341Request.getAmount()+"");
	    paramMap.put("accountType", tx1341Request.getAccountType()+"");
	    paramMap.put("bankID", tx1341Request.getBankAccount().getBankID()+"");
	    paramMap.put("accountName",tx1341Request.getBankAccount().getAccountName()+"");
	    paramMap.put("accountNumber",tx1341Request.getBankAccount().getAccountNumber());
	    paramMap.put("branchName",tx1341Request.getBankAccount().getBranchName()+"");
	    paramMap.put("province",tx1341Request.getBankAccount().getProvince());
	    paramMap.put("city",tx1341Request.getBankAccount().getCity());
	    DataUtil.printRequestData(paramMap, "结算提交参数", PayType.SETTLE);
	    Tx134xResponse response = paymentServiceImpl.fullBidRelease(tx1341Request);	  
	 return response;   
	}
	
   private Double calculateLoanerMoney(double amount,double investRate,double apr,int period){
	    double fee = amount*(1-(investRate-apr)*0.01*period/360); 
	    return fee;
   }
}