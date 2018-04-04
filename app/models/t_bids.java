package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
import utils.DateUtil;
import utils.Security;
import business.BackstageSet;
import constants.Constants;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:32:02
 */
@Entity()
public class t_bids extends Model {
	
	
	public long user_id;
	public Date time;
	public String bid_no;  //标的编号（资金托管使用）
	public String mer_bill_no;
	public String ips_bill_no;
	public long product_id;
	public String title;
	public String loan_purpose;//借款目的
	public long repayment_type_id;
	public double amount;
	public int period;
	public double min_invest_amount;
	public double has_settle_amount;
	public int invest_period;
	public Date invest_expire_time;//满标时间
	public Date begin_interest;//起息日
	public double apr;//利率
	public long bank_account_id;

	public double max_loan;//贷款限额
	public String description;
	public double service_fees;

	public int status;//标的状态
	public double loan_schedule;
	public double has_invested_amount;

	public Date audit_time;
	public String audit_suggest;
	public Date repayment_time;
	public Date last_repay_time;
    public Integer settlement_count;
	
	public int period_unit;
	public int type;
	public int version;
	public String qr_code;//二维码标识
	public double invest_rate; // 理财管理费，利息费率
	public double overdue_rate; // 逾期费率
	public String pact_no;
	public String pact; // 借款合同
	public String guarantee_no; // 居间服务协议
	public String guarantee; // 保障函
	
	public Date start_time;//发标时间
	/**
	 * 标的号，借款标编号代码 + id
	 */
	@Transient
	public String bid_code;
	public String getBid_code(){
		return BackstageSet.getCurrentBackstageSet().loanNumber + this.id;
	}
	
	
	public t_bids() {

	}

	/**
	 * 我要借款,时间最新的未满借款标 
	 * @param user_id 用户ID
	 * @param name 用户名称
	 * @param id 标ID
	 * @param time 时间
	 * @param amount 金额
	 * @param apr 利率
	 */
	public t_bids(long user_id, long id, Date time, double amount, double apr) {
		this.user_id = user_id;
		this.id = id;
		this.time = time;
		this.amount = amount;
		this.apr = apr;
	}

	

	/**
	 * 放款审核后续处理,部分查询项
	 * @param service_fees 服务费
	 * @param amount 金额
	 */
	public t_bids(double service_fees, double amount) {
		this.service_fees = service_fees;
		this.amount = amount;
		//this.bail = bail;
	}


	
	/**
	 * 账户满标倒计时提醒
	 * @param id 标ID
	 * @param amount 金额
	 * @param time 时间
	 * @param invest_expire_time 满标时间
	 */
	public t_bids(long id, double amount, Date time, Date invest_expire_time) {
		this.id = id;
		this.amount = amount;
		this.time = time;
		this.invest_expire_time = invest_expire_time;
	}
	

	
	/**
	 * 解除秒还标操作
	 * @param amount 金额
	 * @param apr 年利率
	 * @param period 期限
	 * @param service_fees 服务费
	 * @param bail 保证金
	 */
	public t_bids(double amount, double apr, int period, double service_fees) {
		this.amount = amount;
		this.apr = apr;
		this.period = period;
		this.service_fees = service_fees;
	}
	
	/* 辉哥  */
	@Transient
	public String no;
	
	/**
	 * 我的会员账单-借款会员管理
	 * @param id
	 * @param no
	 * @param title
	 * @param amount
	 * @param status
	 */
	public t_bids(long id,String no,String title,double amount,int status){
		this.id = id;
		this.no = no;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	/**
	 * 会员管理，根据用户ID查询数据
	 * @param id ID
	 * @param title 标题
	 * @param amount 金额
	 * @param status 状态
	 */
	public t_bids(long id, String title, double amount, int status) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	@Transient
	public String sign;
	@Transient
	public Date endInterest;//标结束日期
	
	
	
	

	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	@Transient
	public String loanUserName;
	
	
	
	
	
	
}
