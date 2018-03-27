package controllers.m.front.account;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.t_dict_ad_areas;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_banks_col;
import models.t_user_bank_accounts;
import models.t_user_details;
import net.sf.json.JSONObject;
import payment.api.system.TxMessenger;
import payment.api.tx.paymentbinding.Tx2502Request;
import payment.api.tx.paymentbinding.Tx2502Response;
import payment.api.tx.paymentbinding.Tx2503Request;
import payment.api.tx.paymentbinding.Tx2503Response;
import payment.api.tx.paymentbinding.Tx2531Request;
import payment.api.tx.paymentbinding.Tx2531Response;
import payment.api.tx.paymentbinding.Tx2532Request;
import payment.api.tx.paymentbinding.Tx2532Response;
import payment.api.util.GUIDGenerator;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import business.newr.User;
import business.newr.UserBankAccounts;
import constants.UserEvent;
import constants.newr.Constants;
import controllers.newr.BaseController;
import controllers.newr.SubmitRepeat;
import cpcn.institution.tools.util.StringUtil;


/**
 * @author chencheng
 * 
 *我的修改
 *76
 *157
 */
@With({ SubmitRepeat.class})
public class FundsManageAction extends BaseController {
	
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}

	/*金额函数*/
	private static String doubleToStr(double number){
		DecimalFormat format = new DecimalFormat("0.00");
		return format.format(number);
	}
	//交易详情
	public static void dealDetails(){
		render();
	}
	
	/*######## 以下是中金接口##########*/
	/*绑卡 发送验证码*/
	
	
	public static void getSMSCode() {
		// 1.获取参数
		Request request = Request.current.get();
		Params params = request.params;
		boolean  success = false;
		JSONObject data= new JSONObject();
		 t_user_bank_accounts bankaccount = new t_user_bank_accounts();
		try {
			String institutionID = Constants.INSTITUTIONID;
			String txSNBinding = GUIDGenerator.genGUID();
			String bankID = params.get("bankID");
			String accountName = params.get("AccountName");
			String accountNumber = params.get("AccountNumber");
			String identificationType ="0";
			String identificationNumber = params.get("IdentificationNumber");
			String phoneNumber = params.get("PhoneNumber");
			String cardType = "10";
			String validDate =null;
			String cvn2 = null;
            String branch_bank_name =params.get("branch_bank_name");//开户行名称
            String province = params.get("province");
            String city =params.get("city");
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
             if ("2000".equals(tx2531Response.getCode())){
                 Logger.info("[Tx2531_Message]=[" + tx2531Response.getMessage() + "]");
                 bankaccount.bank_code = Integer.parseInt(bankID);
                 bankaccount.account = accountNumber;
                 bankaccount.account_name = accountName;
                 bankaccount.bank_name = ((t_dict_banks_col)(t_dict_banks_col.find("bank_code=?", new Integer(bankID)).first())).bank_name;
                 bankaccount.identificationNumber = identificationNumber;
                 bankaccount.mobile = phoneNumber;
                 bankaccount.card_type = cardType;
                 bankaccount.txSNBinding = txSNBinding;
                 bankaccount.user_id = User.currUser().id;
                 bankaccount.branch_bank_name = branch_bank_name;
                 bankaccount.province = ((t_dict_ad_provinces)(t_dict_ad_provinces.findById((Long)Long.parseLong(province)))).name;
                 bankaccount.city = city;    
                 bankaccount.save();
                 success =true;
             }else{
            	String errorMsg= tx2531Response.getMessage();
            	Logger.info("短信验证码发送失败"+errorMsg);
            	data.put("success", success);
            	data.put("errorMsg", errorMsg);
            	renderJSON(data);
             }
		} catch (Exception e) {
			e.printStackTrace();
			data.put("success", success);
        	data.put("errorMsg", "发送验证码失败");
        	renderJSON(data);
			Logger.error("短信验证码发送失败：", e.getStackTrace());
		}
    	data.put("success", success);
    	data.put("bankaccount", bankaccount);		
		renderJSON(data);
	}
	/**
	 * 修改
	 * 绑卡 验证短信验证码
	 */
	public  static void verifySMS(){
		User user = User.currUser();
		Long id = new Long(params.get("bindId")); 
		String SMSValidationCode = params.get("code");
		boolean success=false;
        try {
        	t_user_bank_accounts bankAccount = t_user_bank_accounts.findById(id);
			// 创建交易请求对象
			Tx2532Request txRequest = new Tx2532Request();
			txRequest.setInstitutionID(Constants.INSTITUTIONID);
			txRequest.setTxSNBinding(bankAccount.txSNBinding);
			txRequest.setSMSValidationCode(SMSValidationCode);
			// 3.执行报文处理
			txRequest.process();
			TxMessenger txMessenger = new TxMessenger();
			String[] respMsg  = txMessenger.send(txRequest.getRequestMessage(),
					txRequest.getRequestSignature());
			Tx2532Response tx2532Response = new Tx2532Response(respMsg[0], respMsg[1]);
			Logger.info("[Tx2532_Message]=[" + tx2532Response.getResponsePlainText() + "]");
            if ("2000".equals(tx2532Response.getCode())&&30==tx2532Response.getStatus()
            		&&40==tx2532Response.getVerifyStatus()) {
                 JPA.em().createNativeQuery("update t_user_bank_accounts set verify_code=? where id=? ")
                 .setParameter(1, SMSValidationCode).setParameter(2, id).executeUpdate();
                success=true;
            }
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("绑卡短信验证失败：", e.getStackTrace());
			success=false;
		}
        render(success);
	}
	/**
	 * 解绑银行卡
	 */
	public static void unbindCard(Long bankId){
		User user = User.currUser();
		t_user_bank_accounts bankAccounts = t_user_bank_accounts.findById(bankId);
		try {
			if(user!=null&&StringUtil.isNotEmpty(bankAccounts.txSNBinding)){            
			    // 创建交易请求对象
			    Tx2503Request tx2503Request = new Tx2503Request();
			    tx2503Request.setInstitutionID(Constants.INSTITUTIONID);
			    tx2503Request.setTxSNBinding(bankAccounts.txSNBinding);
			    tx2503Request.setTxSNUnBinding(GUIDGenerator.genGUID());
			    // 3.执行报文处理
			    tx2503Request.process();
			    TxMessenger txMessenger = new TxMessenger();
				String[] respMsg  = txMessenger.send(tx2503Request.getRequestMessage(),
						tx2503Request.getRequestSignature());
				Tx2503Response tx2503Response = new Tx2503Response(respMsg[0], respMsg[1]);
	            if ("2000".equals(tx2503Response.getCode())&&"20".equals(tx2503Response.getStatus())) {
	                Logger.info("[Tx2503_Message]=[" + tx2503Response.getMessage() + "]");
	                // 处理业务	     
	                ErrorInfo error = new ErrorInfo();
	                user.updateIpsAcctNo(user.id, "","", error);	                
	                UserBankAccounts.deleteUserBankAccount(user.id, bankId, error);
	            } 
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("绑卡短信验证失败：", e.getStackTrace());
			
		}
	}
	/**
	 * 查询绑定关系
	 */
	public static void queryBinding(){
		User user = User.currUser();
		t_user_bank_accounts bankAccounts = t_user_bank_accounts.find("user_id=?", user.id).first();
        try {
			// 创建交易请求对象
			Tx2502Request tx2502Request = new Tx2502Request();
			tx2502Request.setInstitutionID(Constants.INSTITUTIONID);
			tx2502Request.setTxSNBinding(bankAccounts.txSNBinding);
			// 3.执行报文处理
			tx2502Request.process();
			TxMessenger txMessenger = new TxMessenger();
		    String[] respMsg  = txMessenger.send(tx2502Request.getRequestMessage(),
						tx2502Request.getRequestSignature());
			Tx2502Response tx2502Response = new Tx2502Response(respMsg[0], respMsg[1]);
	        if ("2000".equals(tx2502Response.getCode())&&"20".equals(tx2502Response.getStatus())) {
	                Logger.info("[Tx2503_Message]=[" + tx2502Response.getMessage() + "]");
	             
	         } 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 交易记录Ajax方法
	 * @param type
	 * @param beginTime
	 * @param endTime
	 * @param currPage
	 * @param pageSize
	 */
	public static void dealRecordAjax(String beginTime, String endTime, int currPage, int pageSize){
		
		User user = User.currUser();
		PageBean<t_user_details> pageBean = User.queryUserDetails(user.id, currPage, pageSize);
		List<List<Object>> dealRecords=new ArrayList<List<Object>>();
		List<t_user_details> details = pageBean.page;
		for (t_user_details vs : details) {
			List<Object> list=new ArrayList<Object>();
			list.add(0,vs.name);
			list.add(1,vs.time);
			list.add(2, doubleToStr(vs.balance));
			list.add(3, doubleToStr(vs.amount));
			dealRecords.add(list);
		}
		renderJSON(dealRecords);
	}
	/**
	 * 交易记录
	 */
	public static void dealRecord(int type, int currPage, int pageSize){
	
		User user = User.currUser();
		PageBean<t_user_details> pageBean = User.queryUserDetails(user.id,currPage, pageSize);
		
		render(pageBean,type);
	}

	/**
	 * 查询市
	 */
	public static void findCityByProvinceId(String provinceId){
		List<t_dict_ad_citys>  citys = t_dict_ad_citys.find(" province_id=? ",new Integer(provinceId)).fetch();
		List<t_dict_ad_citys>  removeCitys = new ArrayList<t_dict_ad_citys>();
		List<t_dict_ad_areas>  addAreas = new ArrayList<t_dict_ad_areas>();
		for(t_dict_ad_citys cy:citys ){
		  if("市辖区".equals(cy.name)||"县".equals(cy.name)){
			  List<t_dict_ad_areas> areas = t_dict_ad_areas.find("city_id=?", new Integer(cy.id+"")).fetch();
			  addAreas.addAll(areas);
			  removeCitys.add(cy);
		  }
		}
		citys.removeAll(removeCitys);
		citys.addAll(convert(addAreas));
		renderJSON(citys);
	}
	private static List<t_dict_ad_citys> convert(List<t_dict_ad_areas> areas){
		List<t_dict_ad_citys>  citys = new ArrayList<t_dict_ad_citys>();
		for(t_dict_ad_areas area:areas){
			t_dict_ad_citys  city = new t_dict_ad_citys();
			city.name = area.name;
			 citys.add(city);
		}
		return citys;
	}
	/**
	 * 查询银行编号
	 */
	public static void findBankCode(){
		List<t_dict_banks_col> banks = t_dict_banks_col.findAll();
		renderJSON(banks);
	}
	/**
	 * 显示银行卡信息
	 */
	public static void myCard(){
		long userId = User.currUser().id;
		List<UserBankAccounts> banks=UserBankAccounts.queryUserAllBankAccount(userId);
		render(banks);
	}
	public static void addCard(){
		List<t_dict_ad_provinces> provices = t_dict_ad_provinces.findAll(); 
		render(provices);
	}
	/**
	 * 查询用户银行卡绑定情况
	 * niu
	 * 1.绑定
	 * 0.未绑定
	 * 
	 */
	public static void isBindBank(){
		JSONObject data = new JSONObject();
		int result=UserEvent.BIND_BANK_NOT;
		long userId=User.currUser().id;
		if(UserBankAccounts.queryUserAllBankAccount(userId).size()>=1){
			result=UserEvent.BIND_BANK;
		}		
		data.put("result", result);
		renderJSON(data);
	}
}