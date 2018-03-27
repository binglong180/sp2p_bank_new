package jobs;

import java.util.List;

import constants.newr.Constants;

import models.t_invests;
import payment.newr.PaymentProxy;
import play.jobs.Job;
import play.jobs.On;
import utils.ErrorInfo;

/**
 * 每隔十分钟查询快捷支付处理结果
 * @author zhs
 * vesion: 6.0 
 * @date 2014-7-25 下午09:23:33
 */
 @On("0 0/10 * * * ?")
public class HandDealingInvestDailyJobs extends Job {
	 public void doJob(){
		 ErrorInfo error = new ErrorInfo();
		 List<t_invests>  invests = t_invests.find(" status='1' ").fetch();
		 for(t_invests t_invests:invests){
			 PaymentProxy.getInstance().queryInvestResult(error, Constants.PC,t_invests.ips_bill_no);
		 }
	 }
}
