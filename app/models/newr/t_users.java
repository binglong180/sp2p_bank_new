package models.newr;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.t_user_bank_accounts;

import play.db.jpa.Model;
@Entity
public class t_users extends Model{
    public Date time;
	public String name;
	public String password;
	public String photo;	
	public String reality_name;
	public String mobile;
	public String id_number;
	public  double balance;
	public String postcode;
	public String company;
	public String company_address;
	public String office_telephone;
	public String fax_number;
	public String tax_no;
	public String business_registration_money;
	public String role;
	public Date recommend_time;
	public String qr_code; 
	public Long recommend_user_id;
	public String legal_person; 
	public String business_licence_image ;
	public String code_certificate_image ;
	public String certificate_positive_image ;
	public String cerfificate_negative_image ;
	@Transient
	public t_user_bank_accounts bank ;
}
