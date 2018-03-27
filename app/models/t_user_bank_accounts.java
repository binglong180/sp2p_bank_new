package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_bank_accounts extends Model {

	public long user_id;
	public Date time;
	public String bank_name;
	public int bank_code;
	public int province_code;
	public int city_code;
	public String branch_bank_name;  //支行名称
	public String province;  //支行所在省
	public String city;  //支行所在市
	public String account;
	public String account_name;
	public String mobile;
	public String card_type;
    public String txSNBinding;
    public String verify_code;
    public String identificationNumber;

}
