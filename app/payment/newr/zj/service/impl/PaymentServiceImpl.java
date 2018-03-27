package payment.newr.zj.service.impl;

import payment.api.system.TxMessenger;
import payment.api.tx.marketorder.Tx1341Request;
import payment.api.tx.marketorder.Tx134xResponse;
import payment.api.tx.marketorder.Tx1372Request;
import payment.api.tx.marketorder.Tx1372Response;
import payment.api.tx.marketorder.Tx1375Request;
import payment.api.tx.marketorder.Tx1375Response;
import payment.api.tx.marketorder.Tx1376Request;
import payment.api.tx.marketorder.Tx1376Response;
import payment.newr.zj.service.PaymentService;
import play.Logger;

public class PaymentServiceImpl implements PaymentService{

	@Override
	public Tx1375Response investSMS(Tx1375Request tx1375Request) {
		Tx1375Response txResponse = null;
		try {
			tx1375Request.process();
			String[] respMsg = null;
			 TxMessenger txMessenger = new TxMessenger();
			 respMsg = txMessenger.send(tx1375Request.getRequestMessage(), tx1375Request.getRequestSignature());
			 txResponse = new Tx1375Response(respMsg[0], respMsg[1]);
			 Logger.info("[investSMS_Message]=[" + txResponse.getResponsePlainText() + "]");
		} catch (Exception e) {
			Logger.error("[investSMS_Message]:"+e.getStackTrace());
			e.printStackTrace();
		}
		return txResponse;
	}

	@Override
	public Tx1376Response investSMSVerify(Tx1376Request tx1376Request) {
		Tx1376Response txResponse = null;
		try {
			tx1376Request.process();
			String[] respMsg = null;
			 TxMessenger txMessenger = new TxMessenger();
			 respMsg = txMessenger.send(tx1376Request.getRequestMessage(), tx1376Request.getRequestSignature());
			 txResponse = new Tx1376Response(respMsg[0], respMsg[1]);
			 Logger.info("[investSMSVerify_Message]=[" + txResponse.getResponsePlainText() + "]");
		} catch (Exception e) {
			Logger.error("[investSMSVerify_Message]:"+e.getStackTrace());
			e.printStackTrace();
		}
		return txResponse;
	}

	@Override
	public Tx134xResponse fullBidRelease(Tx1341Request tx1341Request) {
		Tx134xResponse tx134xResponse = null;
		try {
			 tx1341Request.process();
			 String[] respMsg = null;
			 TxMessenger txMessenger = new TxMessenger();
			 respMsg = txMessenger.send(tx1341Request.getRequestMessage(), tx1341Request.getRequestSignature());
			 tx134xResponse = new Tx134xResponse(respMsg[0], respMsg[1]);
			 Logger.info("[fullBidRelease_Message]=[" + tx134xResponse.getResponsePlainText() + "]");
		} catch (Exception e) {
			Logger.error("[fullBidRelease_Message]:"+e.getStackTrace());
			e.printStackTrace();
		}
		return tx134xResponse;
	}
	@Override
	public Tx1372Response queryInvestResult(Tx1372Request tx1372Request) {
		Tx1372Response tx1372Response = null;
		try {
			tx1372Request.process();
			 String[] respMsg = null;
			 TxMessenger txMessenger = new TxMessenger();
			 respMsg = txMessenger.send(tx1372Request.getRequestMessage(), tx1372Request.getRequestSignature());
			 tx1372Response = new Tx1372Response(respMsg[0], respMsg[1]);
			 Logger.info("[queryInvestResult_Message]=[" + tx1372Response.getResponsePlainText() + "]");
		} catch (Exception e) {
			Logger.error("[queryInvestResult_Message]:"+e.getStackTrace());
			e.printStackTrace();
		}
		return tx1372Response;
	}
}
