package controllers.newr.payment.hf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.t_user_details;
import models.newr.t_settlement;

import org.w3c.dom.Document;

import payment.api.notice.NoticeResponse;
import play.Logger;
import play.db.jpa.JPA;
import utils.DataUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.newr.Bid;
import constants.DealType;
import constants.newr.PayType;
import controllers.newr.payment.PaymentBaseAction;
import cpcn.institution.tools.security.SignatureFactory;
import cpcn.institution.tools.util.Base64;
import cpcn.institution.tools.util.XmlUtil;
/**
 * 中金回调实现类
 * @author
 *
 */
public class HfPaymentCallBackAction extends PaymentBaseAction{
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public static synchronized void cpcnNotify(){
		String responseXMl ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response version=\"2.0\"><Head><Code>2001</Code><Message>fail.</Message></Head></Response>";
		String message = params.get("message");
        String signature = params.get("signature");
        Logger.info("[message]=[" + message + "]");
        Logger.info("[signature]=[" + signature + "]");
        String base64String ="";
        try {
        	 base64String = new String(Base64.encode(responseXMl.getBytes("UTF-8")));
			String plainText = new String(Base64.decode(message), "UTF-8");
			if (!SignatureFactory.getVerifier().verify(message, signature)
					&& !SignatureFactory.getVerifier().verify(plainText,
							signature)) {
				throw new Exception("验证签名失败。");
			} else {
				Document document = XmlUtil.createDocument(plainText);
				String txCode = XmlUtil.getNodeText(document, "TxCode");
				ErrorInfo error = new ErrorInfo();
				if("1348".equals(txCode)){
					settleNotify(plainText, error);
				}
				if(error.code>0){
					responseXMl = new NoticeResponse().getMessage();
					base64String = new String(Base64.encode(responseXMl.getBytes("UTF-8")));
					renderHtml(base64String);
				}		
			}
			 base64String = new String(Base64.encode(responseXMl.getBytes("UTF-8")));
			renderHtml(base64String);
		}  catch (Exception e) {
			e.printStackTrace();			
			renderHtml(base64String);
		}		
	}
	private static void settleNotify(String requestMessage,ErrorInfo error){
		try {
			Document document = XmlUtil.createDocument(requestMessage);
			String institutionID = XmlUtil.getNodeText(document, "InstitutionID");
			String message = XmlUtil.getNodeText(document, "TxCode");
			if("1348".equals(message)){
				 String serialNumber = XmlUtil.getNodeText(document, "SerialNumber");
				 String OrderNo  = XmlUtil.getNodeText(document, "OrderNo");
				 String Amount = XmlUtil.getNodeText(document, "Amount");
				 String Status = XmlUtil.getNodeText(document, "Status");
				 String ErrorMessage =XmlUtil.getNodeText(document, "ErrorMessage"); 
				 if("40".equals(Status)){
					 Map<String, String> paramMap = new HashMap<String, String>();
					 paramMap.put("MerPriv", "OrderNo");
					 paramMap.put("parentOrderno", "serialNumber");
					 paramMap.put("data", requestMessage);
					 DataUtil.printData(paramMap, "结算通知参数", PayType.SETTLENOTIFY);
				     t_settlement settlement = t_settlement.find("serial_number=?", serialNumber).first();
					 Bid bid = new Bid();
					 bid.auditBid=true;
					 bid.id = settlement.bid_id;
					String transferTime  =XmlUtil.getNodeText(document, "TransferTime");
					Date settleTime = dateFormat.parse(transferTime);
					 Logger.info("结算通知", "bid.amount:"+bid.amount+",bid.hasSettleAmount:"+bid.hasSettleAmount+",Amount:"+Amount);
					 if(bid.settlementCount!=null&&bid.settlementCount.intValue()==1){
					    //走放款后续流程 
					    bid.afterReleaseBid(error,settleTime, document);
					 }else{
					   JPA.em().createNativeQuery("update t_settlement set status = ?,real_settle_time = ? where serial_number =? ")
					     .setParameter(1, DealType.DealStatus.SECCUSS.getValue())
					     .setParameter(2, settleTime)
					     .setParameter(3, serialNumber).executeUpdate();
					   JPA.em().createNativeQuery("update t_bids set settlement_count = settlement_count + ?,has_settle_amount =has_settle_amount +? where id =? ")
					     .setParameter(1, 1).setParameter(2, Double.parseDouble(Amount)/100)
					     .setParameter(3, settlement.bid_id).executeUpdate();
					  }	 
				 }else{
					 error.code=-2;
					 error.msg = ErrorMessage;
					 JPA.em().createNativeQuery("update t_settlement set status = ?,fail_reason=? where serial_number =? ")
				     .setParameter(1, DealType.DealStatus.FAIL.getValue())
				     .setParameter(2, ErrorMessage).setParameter(3, serialNumber).executeUpdate();
				 }
			}
		}  catch (Exception e) {
			Logger.error("结算通知回调 ", e.getStackTrace());
			error.code=-2;
			 error.msg ="结算通知回调异常";
		}
	}
}
