package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_details extends Model {

	public long user_id;
	
	public Date time;
	
	public int operation;
	
	public double amount;
	
	public long relation_id;
	
	public String summary;
	
	public double balance;
	
	public int status;
	
	public String mer_bill_no;
	@Transient
	public String name;
}