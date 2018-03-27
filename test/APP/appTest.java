package APP;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Test;

import payment.api.system.TxMessenger;
import payment.api.tx.paymentbinding.Tx2531Request;
import payment.api.tx.paymentbinding.Tx2531Response;
import payment.api.util.GUIDGenerator;
import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;

import com.google.gson.JsonObject;

import utils.DateUtil;
import utils.Security;

public class appTest extends BaseTest{

	@Test
	public void appIndex() throws Exception {
		url+="services";
		String OPT="179";
		
		
		System.out.println("------------APP start--------------");
		Map<String,String> parameters =new HashMap<String, String>();
		parameters.put("body", "");
		parameters.put("maxLevelStr", "-1");
		parameters.put("currPage", "1");
		parameters.put("minLevelStr", "-1");
		parameters.put("type", "2");
		parameters.put("orderType", "0");
		parameters.put("userId", addSign(4, "u"));
		parameters.put("OPT", OPT);
		
		if(OPT.equals("176")){
			parameters.put("idNo", "530128197009277733");
			parameters.put("cellPhone1", "15923561245");
			parameters.put("email", "sas@163.com");
			parameters.put("randomCode1", "");
			parameters.put("realName", "华嘉懿");
			String result=sendHttp(parameters);
			System.out.println(result);
			
			JSONObject json=JSONObject.fromObject(result);
			for(Object s:json.keySet()){
				Object v = json.get(s);
				System.out.println(s+"-----"+v);
			}
		}else if(OPT.equals("177")){
			parameters.put("amount", "10000");
		}else if(OPT.equals("144")){
			parameters.put("amount", "1000");
		}else if(OPT.equals("16")){
			parameters.put("amount", "1000");
			parameters.put("borrowId", "3");
		}else if(OPT.equals("89")){
			parameters.put("billId", addSign(4, "bill"));
		}
		
		String result=sendHttp(parameters);
		System.out.println(result);
		
		System.out.println("------------APP   end --------------");
	}
	public static String addSign(long id, String action) {
		String des=com.shove.security.Encrypt.encrypt3DES(id+","+action+","+DateUtil.dateToString(new Date()), 
				"292I3b3F0LPLLX8j");
		String md5=com.shove.security.Encrypt.MD5(des+"292I3b3F0LPLLX8j");
		String sign=des+md5.substring(0, 8);
		return sign;
	}
	@Test
	public  void bindingSMS(){
				try {
					String userId ="123";
					String institutionID = "003597";
					String txCode = "2531";
					String txSNBinding = GUIDGenerator.genGUID();
					String bankID = "105";
					String accountName = "穆照谦";
					String accountNumber = "6217000010046883719";
					String identificationType = "0";
					String identificationNumber = "142229198803013811";
					String phoneNumber = "18401260201";
					String cardType = "10";
					// 如果是信用卡 上送以下参数
					String validDate =  null;
					 String cvn2 =  null;

					TxMessenger txMessenger = new TxMessenger();

					// 创建交易请求对象
					Tx2531Request txRequest = new Tx2531Request();
					txRequest.setInstitutionID(institutionID);
					txRequest.setTxSNBinding(txSNBinding);
					txRequest.setBankID(bankID);
					txRequest.setAccountName(accountName);
					txRequest.setAccountNumber(accountNumber);
					txRequest.setIdentificationNumber(identificationNumber);
					txRequest.setIdentificationType(identificationType);
					txRequest.setPhoneNumber(phoneNumber);
					txRequest.setCardType(cardType);
					 txRequest.setValidDate(validDate);
					 txRequest.setCvn2(cvn2);
						// 3.执行报文处理
				    txRequest.process();
					String[] respMsg  = txMessenger.send(txRequest.getRequestMessage(),
							txRequest.getRequestSignature());
					 Tx2531Response tx2531Response = new Tx2531Response(respMsg[0], respMsg[1]);
		             if ("2000".equals(tx2531Response.getCode())) {
		                 Logger.info("[Tx2531_Message]=[" + tx2531Response.getMessage() + "]");
		             }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	} 

}
