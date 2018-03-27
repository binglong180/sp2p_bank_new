package utils;

import java.util.Random;
import business.BackstageSet;
import com.shove.gateway.sms.EimsSMS;
import org.apache.commons.lang.StringUtils;

import play.Logger;

public class SMSUtil {

	/**
	 * 发送短信
	 * @param mobile
	 * @param content
	 * @param error
	 */
	public static void sendSMS(String mobile,String content, ErrorInfo error) {
		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入短信内容";
			
			return;
		}
		
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		content="【银信保】"+content;
		EimsSMS.send(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		
		error.msg = "短信发送成功";
	}
	
	/**
	 * 发送校验码
	 * @param mobile
	 * @param error
	 */
	public static void sendCode(String mobile, ErrorInfo error) {
		error.clear();
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		int randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
		String content = "【银信保】"+randomCode+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		Logger.info("sms code:"+randomCode);
		EimsSMS.send(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		play.cache.Cache.set(mobile, randomCode + "", "2min");
		error.msg = "短信验证码发送成功";
	}
	
	public static void main(String[] args) {
		String content = "【银信保】"+1234+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		EimsSMS.send("3SDK-GHJ-0130-OFXSO", "682937", content, "13428934020");
	}
}
