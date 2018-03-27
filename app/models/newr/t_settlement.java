package models.newr;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
@Entity
public class t_settlement extends Model{
   public Double amount;
   public Long user_id;
   public Date time;
   public int settle_type;
   public int status;
   public String fail_reason;
   public Long bid_id;
   public String serial_number;
   @Transient
   public String userName;
}
