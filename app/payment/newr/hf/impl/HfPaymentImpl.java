package payment.newr.hf.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_invests;
import models.t_user_bank_accounts;
import models.newr.t_users;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import payment.api.tx.marketorder.Tx1372Request;
import payment.api.tx.marketorder.Tx1372Response;
import payment.api.tx.marketorder.Tx1375Request;
import payment.api.tx.marketorder.Tx1375Response;
import payment.api.tx.marketorder.Tx1376Request;
import payment.api.tx.marketorder.Tx1376Response;
import payment.api.util.GUIDGenerator;
import payment.newr.PaymentInterface;
import payment.newr.hf.service.HfPaymentCallBackService;
import payment.newr.hf.service.HfPaymentService;
import payment.newr.hf.util.CashEnum;
import payment.newr.hf.util.HfConstants;
import payment.newr.hf.util.HfPaymentUtil;
import payment.newr.hf.util.UsrAcctPayEnum;
import payment.newr.zj.service.impl.PaymentServiceImpl;
import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.BillInvests;
import business.newr.Bid;
import business.newr.Bill;
import business.newr.Invest;
import business.newr.User;
import business.newr.UserBankAccounts;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shove.Convert;

import constants.newr.Constants;
import constants.newr.PayType;
import controllers.newr.payment.hf.HfPaymentReqAction;


/**
 * 支付接口封装实现类
 * @author 
 *
 */
public class HfPaymentImpl implements PaymentInterface {	
	public HfPaymentService hfPaymentService = new HfPaymentService();
	public HfPaymentCallBackService hfPaymentCallBackService = new HfPaymentCallBackService();
	public PaymentServiceImpl paymentServiceImpl = new PaymentServiceImpl();
	public Gson gson = new Gson();
	@Override
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}	
	@Override
	public Map<String, Object> autoRepayment(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj) {
		
		return null;
	}
	public Map<String, Object> unfreezeMoney(ErrorInfo error, int client, Object... obj) {

		return null;
	} 
	@Override
	public Map<String, Object> bidAuditSucc(ErrorInfo error, int client, Object... obj) {
		return null;
	}
	@Override
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client,
			LinkedList<Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId,
			String dealpwd) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> queryAmountByMerchant(ErrorInfo error, int client, Object... obj) {
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.queryAmountByMerchant(HfConstants.MERCUSTID);
		// ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");

		Map<String, String> resultMap = HfPaymentUtil.jsonToMap(result);
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("AcctBal", resultMap.get("AcctBal").replace(",", ""));
		maps.put("AvlBal", resultMap.get("AvlBal").replace(",", ""));
		maps.put("FrzBal", resultMap.get("FrzBal").replace(",", ""));
		maps.put("UsrCustId", resultMap.get("UsrCustId"));
		return maps;
	}
	

	@Override
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj) {
		return HfConstants.getBankList();
	}

	@Override
	public Map<String, Object> queryBindedBankCard(ErrorInfo error, int client, Object... obj) {
       return null;
	}

	@Override
	public Map<String, Object> queryLog(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	public Map<String, Object> repayment(ErrorInfo error, int client, Object... obj) {
		//获取账单信息
		Bill bill = (Bill) obj[0];				
		// 获取借款标信息
		Bid bid = new Bid();
		bid.id = bill.bidId;
		// 获取借款人信息
		User borrower = new User();
		borrower.id = bid.userId;
		// 获取投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
		
 		// 获取理财账单列表
 		List<t_bill_invests> list = t_bill_invests.find(" bid_id = ? and periods = ? and mer_bill_no is not null and status not in (0,-3,-7,-4) ", bill.bidId, bill.periods).fetch();
 		// 拼接接口所需的JSON格式理财账单列表
 		String inDetails = hfPaymentService.generateBatchRepaymentInDetails(list, managementRate);
 		// 获取出账商户号
 		String outCustId = null;//User.queryIpsAcctNo(bid.userId, error);
 		// 获取订单号
		String batchId = hfPaymentService.createBillNo();
		// 获取操作时间
		String merOrdDate = DateUtil.simple(new Date());
 		// 获取借款标发布流水号
		String proId = bid.bidNo;
		
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.repayment(outCustId, batchId, merOrdDate, inDetails, proId);
		// 提交入库参数, 用于回调
		paramMap.put("UBillId", bill.id+"");
		paramMap.put("UUserId", bid.userId + "");
		paramMap.put("UsrCustId", outCustId);
		paramMap.put("OrdId", batchId);
		paramMap.put("client", client+"");

		// 请求日志
		hfPaymentService.printRequestData(paramMap, "还款请求提交参数", PayType.REPAYMENT);	
		
		// 提交请求，获取返回参数
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");

		// 日志打印
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		hfPaymentService.printData(resultMap, "还款同步回调参数", PayType.REPAYMENT);
		
		// 调用回调控制层，由回调控制层跳转响应页面
		hfPaymentCallBackService.batchRepayment(resultMap, "批量还款同步回调", error);
		
		
		return null;
	}
	@Override
	public Map<String, Object> recharge(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> grantCps(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> grantInvitation(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> agentRecharge(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> bidCreate(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> register(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> autoInvest(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> withdraw(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> advance(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> offlineRepayment(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> advanceRepayment(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> userBindCard(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> merchantRecharge(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> merWithdrawal(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> queryAmount(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> loginAccount(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> investSMS(ErrorInfo error, int client, Object... obj) {		
		//投资人
		User user = (User) obj[1];
		//标的
		t_bids bid = (t_bids)obj[0];		
		//绑定流水号
		String bindId = t_user_bank_accounts.find("select txSNBinding from t_user_bank_accounts where user_id=?", user.id).first();		
		//投资金额
		Long transAmt = Long.parseLong(obj[2]+"");
		  // 2.创建交易请求对象
        Tx1375Request txRequest = new Tx1375Request();
        try {
			txRequest.setInstitutionID(Constants.INSTITUTIONID);
			txRequest.setOrderNo(bid.mer_bill_no);
			txRequest.setPaymentNo(GUIDGenerator.genGUID());
			//中金单位(分)
			txRequest.setAmount(transAmt*100);
			txRequest.setTxSNBinding(bindId);
			txRequest.setValidDate(null);
			txRequest.setCvn2(null);
			txRequest.setRemark("");
	        Map<String, String> paramMap = new HashMap<String, String>();
	        paramMap.put("UsrCustId", user.id+"");
	        paramMap.put("MerPriv", bid.mer_bill_no);
	        paramMap.put("paymentNo", txRequest.getPaymentNo());
	        paramMap.put("Amount", transAmt+"");
	        paramMap.put("TxSNBinding", bindId);
	        hfPaymentService.printRequestData(paramMap, "投标发送短信验证码提交参数", PayType.INVESTSMS);
			Tx1375Response txResponse = paymentServiceImpl.investSMS(txRequest);
			 if ("2000".equals(txResponse.getCode())) {
				 t_invests invest = new t_invests();
				 invest.bid_id=bid.id;
				 invest.user_id=user.id;
				 invest.amount=transAmt;
				 invest.time=new Date();
				 invest.mer_bill_no =txRequest.getPaymentNo();
				 invest.ips_bill_no=txRequest.getPaymentNo();
				 invest.save();
				 Map<String, Object> resultMap = new HashMap<String, Object>();
				 resultMap.put("investId", invest.getId());
				 error.code=1;
				 return resultMap;
	         }
		} catch (Exception e) {
            Logger.error("投标发送短信验证:"+e.getStackTrace());
			e.printStackTrace();
			error.code=-1;
		}		
		
		return null;		
	}
	@Override
	public Map<String, Object> investVerifySMS(ErrorInfo error, int client,
			Object... obj) {		
          Long investId =  (Long) obj[0];
          t_invests invest=Invest.queryUserAndBid(investId); 
	     if(invest==null){
	    	 error.code=-2;
	     }
	     t_users user = t_users.findById(invest.user_id);
	     t_bids bid =  t_bids.findById(invest.bid_id);
		//短信验证码
		String smscode = (String) obj[1];
		// 请求参数
        String institutionID = Constants.INSTITUTIONID;
        String orderNo = bid.mer_bill_no;
        String paymentNo = invest.ips_bill_no;
        String smsValidationCode = smscode;
        String validDate =  null;
        String cvn2 = null;

        // 2.创建交易请求对象
        Tx1376Request txRequest = new Tx1376Request();
        txRequest.setInstitutionID(institutionID);
        txRequest.setOrderNo(orderNo);
        txRequest.setPaymentNo(paymentNo);
        txRequest.setSmsValidationCode(smsValidationCode);
        txRequest.setValidDate(validDate);
        txRequest.setCvn2(cvn2);
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("UsrCustId", user.id+"");
        paramMap.put("MerPriv", orderNo);
        paramMap.put("paymentNo", paymentNo);
        paramMap.put("smsValidationCode", smsValidationCode);
        hfPaymentService.printRequestData(paramMap, "手动投标提交参数", PayType.INVEST);
        Tx1376Response response = paymentServiceImpl.investSMSVerify(txRequest);
        if("2000".equals(response.getCode())){
        	 Map<String, String> returnParamMap = new HashMap<String, String>();
             returnParamMap.put("UsrCustId", user.id+"");
             returnParamMap.put("MerPriv", response.getOrderNo());
             returnParamMap.put("paymentNo", response.getPaymentNo());
             returnParamMap.put("VerifyStatus", response.getVerifyStatus()+"");
             returnParamMap.put("Status", response.getStatus()+"");
             returnParamMap.put("ResponseCode", response.getResponseCode());
             returnParamMap.put("ResponseCode", response.getResponseCode());
             returnParamMap.put("ResponseMessage", response.getResponseMessage());
             hfPaymentService.printData(returnParamMap, "投标回调", PayType.INVEST);
             if(Constants.INSTITUTIONID.equals(response.getInstitutionID())&&
            		 orderNo.equals(response.getOrderNo())
            		 &&40==response.getVerifyStatus()
            		 &&20==response.getStatus()){                
         		//调用投标业务
         		Invest.doInvest(user, invest.bid_id, invest.amount,  response.getPaymentNo(),investId, error);
             }else if(Constants.INSTITUTIONID.equals(response.getInstitutionID())&&
            		 orderNo.equals(response.getOrderNo())&&30==response.getStatus()){
            	 String sql = "update t_invests set status=3,fail_reason=? where id=?";
            	 JPA.em().createNativeQuery(sql).setParameter(1, response.getResponseMessage()).executeUpdate();
             }else if(Constants.INSTITUTIONID.equals(response.getInstitutionID())&&
            		 orderNo.equals(response.getOrderNo())&&10==response.getStatus()){
            	 String sql = "update t_invests set status=1,fail_reason=? where id=?";
            	 JPA.em().createNativeQuery(sql).setParameter(1, response.getResponseMessage()).executeUpdate(); 
             }
        }
       
        
		return null;
	}
	
	@Override
	public Map<String, Object> queryInvestResult(ErrorInfo error, int client,
			Object... obj) {
		    String paymentNo = (String) obj[0];
	       Tx1372Request txRequest = new Tx1372Request();
           txRequest.setInstitutionID(Constants.INSTITUTIONID);
           txRequest.setPaymentNo(paymentNo);
           Tx1372Response response = paymentServiceImpl.queryInvestResult(txRequest); 
           if ("2000".equals(response.getCode())&&20==response.getStatus()) {
              t_invests invest = t_invests.find("ips_bill_no", paymentNo).first();
               t_users user = t_users.findById(invest.user_id);
             //调用投标业务
        	 Invest.doInvest(user, invest.bid_id, invest.amount,  response.getPaymentNo(),invest.id, error);
           }
		return null;
	}


}
