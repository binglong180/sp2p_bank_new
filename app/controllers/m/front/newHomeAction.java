package controllers.m.front;

import models.t_bids;
import utils.ErrorInfo;
import utils.PageBean;
import business.newr.Invest;
import business.newr.User;
import constants.Constants;
import controllers.newr.BaseController;

/**
 * @author chencheng
 *
 */
public class newHomeAction extends BaseController {
	
	// wap网站首页
	public static void home() {
		int currPage = Constants.ONE;
		int pageSize = Constants.THREE;
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		User tempUser = User.currUser();
		PageBean<t_bids>  pageBean = new PageBean<t_bids>();
		pageBean= Invest.queryAllBids(currPage,pageSize,error);
		if(tempUser!=null){
			user.id = User.currUser().id;
			render(user,pageBean);
		}else{
			render(user,pageBean);
		}
		
	}
}