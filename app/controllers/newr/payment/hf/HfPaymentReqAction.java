package controllers.newr.payment.hf;

import controllers.newr.payment.PaymentBaseAction;

/**
 * 请求环讯资金托管，Action
 * @author xiaoqi
 *
 */
public class HfPaymentReqAction extends PaymentBaseAction{
	
	private static HfPaymentReqAction instance = null;
	
	private HfPaymentReqAction(){
		
	}
	
	public static HfPaymentReqAction getInstance(){
		if(instance == null){
			synchronized (HfPaymentReqAction.class) {
				if(instance == null){
					instance = new HfPaymentReqAction();
				}
			}
		}
		
		return instance;
	}
	
}
