package models.newr;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

@Entity
public class v_userEventsList extends Model{
   /**
	 * 
	 */
   private static final long serialVersionUID = 1L;
   public String name;
   public Date time;
   public String ip;
   public String descrption;
   public String type_name;
}
