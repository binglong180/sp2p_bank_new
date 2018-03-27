package models.newr;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 用户投标记录
* @author lwh
* @version 6.0
* @created 2014年4月24日 下午4:14:14
 */

@Entity
public class v_newr_invest_records extends Model{
	
	public Date time;
	public String title;
	public Double invest_amount;
	public Integer status;
	public Long bidId;
	public Double unreceive;
	public Double receive;
}
