package payment.newr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import payment.newr.hf.impl.HfPaymentImpl;
import payment.newr.hf.util.HfConstants;
import play.Logger;
import utils.ErrorInfo;
import constants.Constants;

/**
 * 资金托管接口代理类
 * @author xiaoqi
 *
 */
public class PaymentProxy implements PaymentInterface{
	
	private PaymentInterface payment = new HfPaymentImpl();
	
	private static PaymentProxy proxy = new PaymentProxy();

	private PaymentProxy(){
		
	}
	
	public static PaymentProxy getInstance(){
		return proxy;
	}

	
	@Override
	public Map<String, Object> register(ErrorInfo error,int client,  Object... obj){
		return payment.register(error, client, obj);
	};
	

	
	@Override
	public Map<String, Object> recharge(ErrorInfo error, int client, Object... obj){
		return payment.recharge(error, client, obj);
	}
	
	@Override
	public Map<String, Object> bidCreate(ErrorInfo error, int client, Object... obj){
		
		return payment.bidCreate(error, client, obj);
	}

	@Override
	public Map<String, Object> autoRepayment(ErrorInfo error, int client, Object... obj){
		return payment.autoRepayment(error, client, obj);
	}
	
	@Override
	public Map<String, Object> autoInvest(ErrorInfo error, int client, Object... obj){
		return payment.autoInvest(error, client, obj);
	}
	
	@Override
	public Map<String, Object> withdraw(ErrorInfo error, int client, Object... obj){
		return payment.withdraw(error, client, obj);
	}
	
	@Override
	public Map<String, Object> bidAuditSucc(ErrorInfo error, int client, Object... obj){
		return payment.bidAuditSucc(error, client, obj);
	}
	
	@Override
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client, Object... obj){
		return payment.bidAuditFail(error, client , obj);
	}
	
	@Override
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj){
		return payment.applyVIP(error, client,  obj);
	}
	
	@Override
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj){
		return payment.bidDataAudit(error, client, obj);
	}
	
	@Override
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj){
		return payment.applyCredit(error, client, obj);
	}
	
	@Override
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client, Object... obj){
		return payment.debtorTransfer(error, client, obj);
	}
	
	@Override
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client, LinkedList<Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId, String dealpwd){
		
		return payment.debtorTransferConfirm(error, client, pDetails, pBidNo, parentOrderno, debtId, dealpwd);
	}
	
	@Override
	public Map<String, Object> advance(ErrorInfo error, int client, Object... obj){
		return payment.advance(error, client, obj);
	}
	
	@Override
	public Map<String, Object> offlineRepayment(ErrorInfo error, int client, Object... obj){
		return payment.offlineRepayment(error, client, obj);
	}
	
	@Override
	public Map<String, Object> advanceRepayment(ErrorInfo error, int client, Object... obj) {		
		return payment.advanceRepayment(error, client, obj);
	}
	
	@Override
	public Map<String, Object> repayment(ErrorInfo error, int client, Object... obj){
		return payment.repayment(error, client, obj);
	}
	
	@Override
	public Map<String, Object> queryAmount(ErrorInfo error, int client,  Object... obj){
		return payment.queryAmount(error, client, obj);
	}
	
	@Override
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj){
		return payment.queryBanks(error, client, obj);
	}
	
	@Override
	public Map<String, Object> userBindCard(ErrorInfo error, int client, Object... obj){
		return payment.userBindCard(error, client, obj);
	}
	
	@Override
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj) {
		
		payment.autoRepaymentSignature(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client, Object... obj) {
		
		payment.autoInvestSignature(error, client, obj);
		return null;
	}

	@Override
	public Map<String, Object> grantCps(ErrorInfo error, int client, Object... obj) {
		
		payment.grantCps(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String, Object> grantInvitation(ErrorInfo error, int client, Object... obj) {
		
		payment.grantInvitation(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String, Object> agentRecharge(ErrorInfo error, int client, Object... obj) {
		
		payment.agentRecharge(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String,Object> merchantRecharge(ErrorInfo error, int client, Object... obj){

		payment.merchantRecharge(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String,Object> merWithdrawal(ErrorInfo error, int client, Object... obj){
		
		payment.merWithdrawal(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String,Object> queryAmountByMerchant(ErrorInfo error, int client, Object... obj){
		
		return payment.queryAmountByMerchant(error, client, obj);
	}
	
	@Override
	public Map<String,Object> loginAccount(ErrorInfo error, int client, Object... obj){
		
		payment.loginAccount(error, client, obj);
		return null;
	}
	
	@Override
	public Map<String,Object> queryLog(ErrorInfo error, int client, Object... obj){
		
		return payment.queryLog(error, client, obj);
	}
	
	@Override
	public Map<String,Object> queryBindedBankCard(ErrorInfo error, int client, Object... obj){
		
		return payment.queryBindedBankCard(error, client, obj);
	}
	
	@Override
	public Map<String, Object> investSMS(ErrorInfo error, int client, Object... obj){
		return payment.investSMS(error, client, obj);
	}
	@Override
	public Map<String, Object> investVerifySMS(ErrorInfo error, int client,
			Object... obj) {
		return payment.investVerifySMS(error, client, obj);
	}	
	@Override
	public Map<String, Object> queryInvestResult(ErrorInfo error, int client,
			Object... obj) {
		return payment.queryInvestResult(error, client, obj);
	}


}
