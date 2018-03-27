package controllers.m.front.account;


import play.mvc.With;
import business.newr.User;
import controllers.m.front.mFInterceptor;
import controllers.newr.BaseController;
import controllers.newr.SubmitRepeat;

/**
 * 
 * @author chencheng
 *
 */
@With({mFInterceptor.class, SubmitRepeat.class})
public class AccountHomeAction extends BaseController {
	/**
	 * 账户总览页
	 */
	public static void accountHome() {
		//避免缓存中的数据与数据库一致
		User user = new User();
		user.createBid=true;
		user.id = User.currUser().id;	
		render(user);
	}

	
}
