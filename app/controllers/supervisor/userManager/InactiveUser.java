package controllers.supervisor.userManager;

import net.sf.json.JSONObject;
import utils.ErrorInfo;
import utils.Security;
import business.newr.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:InactiveUser
 * 功能:未激活会员列表
 */

public class InactiveUser extends SupervisorController {

	/**
	 * 手动激活
	 * @param id
	 */
	public static void activeUser(String sign) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(error);
		}		
		
		User.activeUserBySupervisor(id, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
}
