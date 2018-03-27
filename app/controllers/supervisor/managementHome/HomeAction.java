package controllers.supervisor.managementHome;

import java.util.HashMap;
import java.util.Map;

import utils.ErrorInfo;
import business.BackstageSet;
import business.StationLetter;
import business.newr.Bid;
import business.newr.Bill;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 管理首页
 * @author zhs
 *
 */
public class HomeAction  extends SupervisorController {

	/**
	 * 管理首页
	 * @version 8.0.2
	 * @author yaoyi
	 */
	public static void showHome(){
		ErrorInfo error = new ErrorInfo();		
	
		
		
		Map<String, Object>result = new HashMap<String, Object>();
		result.put("waitAuditingBidCount", null);
		result.put("expireBorrowingBidCount", null);
		result.put("fullBidCount", null);
		result.put("transferBidCount", 0);
		result.put("waitLendingBidCount", null);
		result.put("nextMonthRepaymentSum", null);
		result.put("waitReplyMessageCount", null);

		
		render(result);
	}
	/**
	 * 保存首页配置信息
	 * @param display
	 */
	public static void saveIndexSetting(boolean display){
		ErrorInfo error = new ErrorInfo();
		BackstageSet bs = BackstageSet.getCurrentBackstageSet();
		bs.saveIndexSetting(display,error);
		renderJSON(error);
	}
	

}
