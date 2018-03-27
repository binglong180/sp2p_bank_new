package controllers.supervisor.userManager;

import net.sf.json.JSONObject;
import utils.ErrorInfo;
import utils.Security;
import business.newr.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:BlacklistUser
 * 功能:黑名单会员列表
 */

public class BlacklistUser extends SupervisorController {

	
	/**
	 * 解除黑名单
	 */
	public static void removeBlacklist(String sign){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		User.editBlacklist(userId, error);
		
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
}
