package controllers.interceptor.newr;

import play.mvc.Before;
import utils.ErrorInfo;
import utils.Security;
import business.newr.User;
import constants.Constants;
import controllers.m.front.account.LoginAndRegisterAction;
import controllers.newr.BaseController;

public class FInterceptor extends BaseController{
	
	@Before(unless={"front.account.FundsManage.gCallback",
			"front.account.FundsManage.gCallbackSys",
			"front.account.FundsManage.callback",
			"front.account.FundsManage.callbackSys"
	})
	public static void checkLogin(){		
		String sign = params.get("id");
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, 60*60*12, new ErrorInfo());
		User user = null;
		if(id > 0){
			user = User.currAppUser(id+"");
			//来自于APP的webview访问并设更新户信息
			User.setCurrUser(user);
		}else{
		    user = User.currUser();
			
			if(user == null){
				LoginAndRegisterAction.login(null);
			}
		}
		
		renderArgs.put("user", user);
	}
	

}
