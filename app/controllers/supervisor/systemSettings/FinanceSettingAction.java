package controllers.supervisor.systemSettings;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import business.BackstageSet;
import business.newr.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 财务设置
 * 
 * @author bsr
 * 
 */
public class FinanceSettingAction extends SupervisorController {
	
	/**
	 * 服务费设置
	 */
	public static void serviceFees() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int day_interest = Constants.DAY_INTEREST;
		
		render(backstageSet,day_interest);
	}

	/**
	 * 保存服务费设置
	 */
	public static void saveServiceFees(Double borrowFee, Double borrowFeeDay, Integer borrowFeeMonth, Double borrowFeeRate, 
			Double investmentFee, Double debtTransferFee, Double overdueFee, Double withdrawFee, 
			Double withdrawRate, Double rechargeFee, Double vipFee, Integer vipTimeType, Integer vipTimeLength, 
			Integer vipMinTimeType, Integer vipMinTimeLength, Integer vipDiscount, Integer vipAuditPeriod, String withdrawBase, String rechargeLowest,String rechargeHighest) {
		
		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = new BackstageSet();
		
		if(borrowFee == null){
			error.code = -1;
			error.msg = "借款管理费本金百分比输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(borrowFeeDay == null){
            error.code = -1;
            error.msg = "天标借款管理费本金百分比输入框必须输入数字";
            
            renderJSON(error);
        }
		
		if(borrowFeeRate == null){
			error.code = -1;
			error.msg = "借款管理费利率输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(investmentFee == null){
			error.code = -1;
			error.msg = "理财管理费输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(debtTransferFee == null){
			error.code = -1;
			error.msg = "债权转让管理费输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(borrowFeeMonth == null){
			error.code = -1;
			error.msg = "借款管理费月输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(overdueFee == null){
			error.code = -1;
			error.msg = "逾期管理费输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(withdrawFee == null){
			error.code = -1;
			error.msg = "提现管理基础金额输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(withdrawRate == null){
			error.code = -1;
			error.msg = "超出金额收取的百分比输入框必须输入数字";
			
			renderJSON(error);
		}
		
//		if(rechargeFee == null){
//			error.code = -1;
//			error.msg = "充值手续费输入框必须输入数字";
//			
//			renderJSON(error);
//		}
		
		if(vipAuditPeriod == null){
			error.code = -1;
			error.msg = "VIP审核周期输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(vipDiscount == null){
			error.code = -1;
			error.msg = "VIP折扣输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(vipMinTimeLength == null){
			error.code = -1;
			error.msg = "VIP最少开通时长输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(vipMinTimeType == null){
			error.code = -1;
			error.msg = "VIP最少开通时间类型输入框必须输入数字";
			
			renderJSON(error);
		}
		
//		if(vipTimeLength == null){
//			error.code = -1;
//			error.msg = "VIP服务时长输入框必须输入数字";
//			
//			renderJSON(error);
//		}
		
		if(vipTimeType == null){
			error.code = -1;
			error.msg = "VIP服务时间类型(年 0, 月 1)输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if(vipFee == null){
			error.code = -1;
			error.msg = "VIP服务费输入框必须输入数字";
			
			renderJSON(error);
		}
		
		if (StringUtils.isEmpty(withdrawBase)){
			withdrawBase = "0";
		}
		
		if (!withdrawBase.matches("^[0-9]\\d*|0*$")){
			error.code = -1;
			error.msg = "提现手续固定金额费用只能为非负整数";
			renderJSON(error);
		}
		
		if (StringUtils.isBlank(rechargeLowest)){
			error.code = -1;
			error.msg = "最低充值金额不能为空";
			renderJSON(error);
		}
		if (!rechargeLowest.matches("^[0-9]\\d*|0*$")){
			error.code = -1;
			error.msg = "最低充值金额只能为非负整数";
			renderJSON(error);
		}
		
		if (StringUtils.isBlank(rechargeHighest)){
			error.code = -1;
			error.msg = "最高充值金额不能为空";
			renderJSON(error);
		}
		if (!rechargeHighest.matches("^[0-9]\\d*|0*$")){
			error.code = -1;
			error.msg = "最高充值金额只能为非负整数";
			renderJSON(error);
		}
		
		/* 借款管理费 */
		backstageSet.borrowFee = borrowFee;
		backstageSet.borrowFeeMonth = borrowFeeMonth;
		backstageSet.borrowFeeRate = borrowFeeRate;
		backstageSet.borrowFeeDay = borrowFeeDay;
		
		backstageSet.investmentFee = investmentFee;
		backstageSet.debtTransferFee = debtTransferFee;
		backstageSet.overdueFee = overdueFee;
		backstageSet.withdrawFee = withdrawFee;
		backstageSet.withdrawRate = withdrawRate;
//		backstageSet.rechargeFee = rechargeFee;
		backstageSet.vipFee = vipFee;
		backstageSet.vipTimeType = vipTimeType;
//		backstageSet.vipTimeLength = vipTimeLength;
		backstageSet.vipMinTimeType = vipMinTimeType;
		backstageSet.vipMinTimeLength = vipMinTimeLength;
		backstageSet.vipDiscount = vipDiscount;
		backstageSet.vipAuditPeriod = vipAuditPeriod;
		backstageSet.withdrawBase = Integer.valueOf(withdrawBase);
		backstageSet.rechargeLowest = Integer.valueOf(rechargeLowest);
		backstageSet.rechargeHighest = Integer.valueOf(rechargeHighest);
		backstageSet.setPlatformFee(error);
		
		renderJSON(error);
	}
	




	/**
	 * 应付账单管理
	 */
	public static void payableBills() {
		String repayType = BackstageSet.getCurrentBackstageSet().repayType;
		
		render(repayType);
	}
	
	/**
	 * 保存应付账单设置
	 */
	public static void saveBillsPayable(String repayType) {
		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = new BackstageSet();
		backstageSet.repayType = repayType;
		backstageSet.setBillsRepayType(error);
		
		renderJSON(error);
	}

	/**
	 * 资金托管账户设置
	 */
	public static void managedFunds() {
		renderTemplate("Application/developing.html");
	}

	/**
	 * 保存资金托管账户设置
	 */
	public static void saveManagedFunds() {
		render();
	}
	
	/**
	 * 系统积分规则
	 */
	public static void systemScoreRule() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int moneyToSystemScore = backstageSet.moneyToSystemScore;
		
		render(moneyToSystemScore);
	}
	
	/**
	 * 系统积分规则设置
	 */
	public static void setSystemScoreRule(Integer moneyToSystemScore) {
		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = new BackstageSet();
		backstageSet.moneyToSystemScore = moneyToSystemScore;
		backstageSet.setSystemScoreRule(error);
		
		renderJSON(error);
	}
}
