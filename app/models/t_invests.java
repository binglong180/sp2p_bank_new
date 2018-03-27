package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:27:06
 */
@Entity
public class t_invests extends Model {
	public long user_id;
	public Date time;
	public String mer_bill_no;
	public String ips_bill_no;
	public long bid_id;
	public double amount;
	public double fee;//理财管理费
	public String status="0";
	public double correct_amount;
	public double correct_interest;
	public String fail_reason;
	public String pact;
	public String intermediary_agreement;
	public String guarantee_invest;	
	
	public t_invests(long user_id,long bid_id){
		this.bid_id = bid_id;
		this.user_id = user_id;
	}
	
	public t_invests() {
		
	}
	
	public t_invests(long user_id,long bid_id,String ips_bill_no,double amount){
		this.user_id=user_id;
		this.bid_id=bid_id;
		this.ips_bill_no=ips_bill_no;
		this.amount=amount;
	}

}
