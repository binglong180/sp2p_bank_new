package payment.newr.hf.service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.t_bill_invests;
import models.t_mmm_data;
import models.t_return_data;
import models.t_sequences;
import models.newr.t_users;
import net.sf.json.JSONObject;

import org.json.JSONArray;

import payment.newr.PaymentBaseService;
import payment.newr.hf.util.HfConstants;
import payment.newr.hf.util.HfPaymentUtil;
import play.Logger;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.BillInvests;
import business.newr.Invest;
import business.newr.User;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import constants.newr.PayType;
import controllers.BaseController;

/**
 * 汇付接口组参类
 * @author liuwenhui
 *
 */
public class HfPaymentService extends PaymentBaseService {
	
	private final Gson gson = new Gson();

	/**
	 * 开户
	 * @param idNo 证件号码
	 * @param usrName 真实名称
	 * @param usrMp 手机号
	 * @param usrEmail 用户Email
	 * @param merPriv 请求操作唯一标识，通常为交易流水号
	 * @return
	 */
	public LinkedHashMap<String, String> register(String idNo, String usrName, String usrMp, String usrEmail,
			String merPriv, int client) { //2016 add

		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", HfConstants.CMD_USERREGISTER); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 客户商户号
		if(client == 5){
			map.put("BgRetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/userRegisterAyns"); // 商户后台应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/userRegisterSyn"); // 页面返回URL
		}else{
			map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/userRegisterAyns"); // 商户后台应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/userRegisterSyn"); // 页面返回URL
		}
		map.put("IdNo", idNo); // 用户号
		map.put("UsrName", usrName); // 真是名称
		map.put("UsrMp", usrMp); // 手机号
		map.put("UsrEmail", usrEmail); // 用户Email
		map.put("MerPriv", merPriv); // 商户私有域
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 企业开户
	 * @param idNo 证件号码
	 * @param usrName 真实名称
	 * @param usrMp 手机号
	 * @param usrEmail 用户Email
	 * @param merPriv 请求操作唯一标识，通常为交易流水号
	 * @return
	 */
	public LinkedHashMap<String, String> companyRegister(t_users user, String merPriv ) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", "CorpRegister"); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 客户商户号
		map.put("TaxCode", user.tax_no);
		map.put("GuarType","N");
		map.put("MerPriv", merPriv);
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/companyuserRegisterAyns"); // 商户后台应答地址
		return HfPaymentUtil.createSignMap(map);
	}
	
	public LinkedHashMap<String, String> querCompanyAcctStatus(t_users user,
			String orderNo) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", "CorpRegisterQuery"); // 消息类型
	
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 网银充值
	 * @param usrCustId 第三方唯一标示
	 * @param ordId 订单号
	 * @param transAmt 交易金额
	 * @return
	 */
	public LinkedHashMap<String, String> recharge(String usrCustId, String ordId, double transAmt, int client) { //2016 add client cc
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);// 版本号
		map.put("CmdId", HfConstants.CMD_NETSAVE);// 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);// 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		map.put("OrdId", ordId);// 订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd"));// 订单日期
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));// 交易金额
		if(client == 5){
			map.put("BgRetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/netSaveAyns"); // 商户应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/netSaveSyn"); // 页面返回URL
		}else{
			map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/netSaveAyns"); // 商户应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/netSaveSyn"); // 页面返回URL
		}
		map.put("MerPriv", ordId);  //请求操作唯一标识，通常为交易流水号
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 快捷充值
	 * @param usrCustId 第三方唯一标示
	 * @param ordId 订单号
	 * @param transAmt 交易金额
	 * @return
	 */
	public LinkedHashMap<String, String> quickRecharge(String usrCustId, String ordId, double transAmt,String openBankId,String DcFlag, int client) { //2016 add client cc
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);// 版本号
		map.put("CmdId", HfConstants.CMD_NETSAVE);// 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);// 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		map.put("OrdId", ordId);// 订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd"));// 订单日期
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));// 交易金额
		map.put("GateBusiId", "QP");
		map.put("OpenBankId", openBankId);
		map.put("DcFlag", DcFlag);
		
		if(client == 5){
			map.put("BgRetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/netSaveAyns"); // 商户应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/netSaveSyn"); // 页面返回URL
		}else{
			map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/netSaveAyns"); // 商户应答地址
			map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/netSaveSyn"); // 页面返回URL
		}
		map.put("MerPriv", ordId);  //请求操作唯一标识，通常为交易流水号
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 标的信息录入
	 * @param proId 项目ID
	 * @param borrCustId 借款人ID
	 * @param borrTotAmt 借款总金额
	 * @param yearRate 年利率
	 * @param retAmt 应还款总金额
	 * @param retDate 应还款日期
	 * @param reqExt 入参扩展域
	 * @return
	 */
	public LinkedHashMap<String, String> bidCreate(String proId, String borrCustId, double borrTotAmt, String yearRate, 
			double retAmt, String retDate, String reqExt) {
		
		Date now = new Date();
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);  //版本号
		map.put("CmdId", HfConstants.CMD_ADDBIDINFO);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("ProId", proId);  //项目ID
		map.put("BorrCustId", borrCustId);  //借款人ID
		map.put("BorrTotAmt", HfPaymentUtil.formatAmount(borrTotAmt));  //借款总金额
		map.put("YearRate", yearRate);  //年利率
		map.put("RetType", "99");  //还款方式 
		map.put("BidStartDate", HfPaymentUtil.getFormatDate("yyyyMMddhhmmss"));//投标开始时间
		
		Date bidEndDate = DateUtil.dateAddYear(now, 1);
		map.put("BidEndDate", HfPaymentUtil.getFormatDate("yyyyMMddhhmmss", bidEndDate));  //投标截止时间
		map.put("RetAmt", HfPaymentUtil.formatAmount(retAmt));  //应还款总金额
		map.put("RetDate", retDate);  //应还款日期
		map.put("ReqExt", reqExt);  //入参扩展域
		map.put("ProArea", "1100");  //项目所在地
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/addBidInfoAyns");//商户后台应答地址
		map.put("MerPriv", proId);
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 冻结保证金
	 * @param ordId 冻结订单号
	 * @param usrCustId 第三方唯一标示
	 * @param transAmt冻结金额
	 * @param error
	 * @return
	 */
	public LinkedHashMap<String, String> freezeBailAmount(String ordId, String usrCustId, double transAmt, ErrorInfo error){
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();		
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_USRFREEZE);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("UsrCustId", usrCustId);
		map.put("OrdId", ordId);
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd"));
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/freezeBailAmountAyns");
		map.put("MerPriv", ordId);
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 交易类接口-主动投标
	 * @param ordId 订单号 
	 * @param transAmt 交易金额
	 * @param usrCustId 投资人用户客户号
	 * @param borrowerDetails 借款人信息
	 * @param freezeOrdId 冻结订单号
	 * @return
	 */
	public LinkedHashMap<String, String> invest(String ordId, double transAmt, String usrCustId, String borrowerDetails, String freezeOrdId, int client) { //2016 add
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION2);  //版本号
		map.put("CmdId", HfConstants.CMD_INITIATIVETENDER);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("OrdId", ordId);  //订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd")) ; //订单日期
		map.put("TransAmt",  HfPaymentUtil.formatAmount(transAmt));  //交易金额
		map.put("UsrCustId", usrCustId) ; //用户客户号
		map.put("MaxTenderRate", HfConstants.MAXTENDERRATE) ; //最大投资手续费率
		map.put("BorrowerDetails", borrowerDetails)  ;//借款人信息
		
		if(client == 5){
			map.put("RetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/initiativeTender") ; //商户后台应答地址
			map.put("BgRetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/initiativeTenderAyns") ; //页面返回URL
		}else{
			map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/initiativeTender") ; //商户后台应答地址
			map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/initiativeTenderAyns") ; //页面返回URL
		}
		map.put("IsFreeze", "Y");//是否冻结
		map.put("FreezeOrdId", freezeOrdId);  //冻结订单号
		map.put("MerPriv", ordId);
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 交易类接口-自动投标签约参数构造
	 * 
	 * @param usrCustId
	 * @param merPriv
	 * @return
	 */
	public Map<String, String> autoInvestSignature(String usrCustId, String merPriv) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_AUTOTENDERPLAN);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("UsrCustId", usrCustId);
		map.put("TenderPlanType", HfConstants.TENDERPLANTYPE);
		map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/autoInvestSignature");
		map.put("MerPriv", merPriv);

		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 交易类接口-关闭自动投标参数构造
	 * 
	 * @param usrCustId
	 * @param merPriv
	 * @return
	 */
	public Map<String, String> closeAutoInvest(String usrCustId, String merPriv) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_AUTOTENDERPLANCLOSE);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("UsrCustId", usrCustId);
		map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/closeAutoInvest");
		map.put("MerPriv", merPriv);
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 用户绑卡
	 * @param usrCustId 用户唯一标识
	 * @param merPriv 流水号
	 * @return
	 */
	public LinkedHashMap<String,String> userBindCard(String usrCustId, String merPriv) {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION1);  //版本号
		map.put("CmdId", HfConstants.CMD_USERBINDCARD);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //客户商户号
		map.put("UsrCustId", usrCustId); //用户唯一标识
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/userBindCardAyns");  //商户后台应答地址
		map.put("MerPriv", merPriv);  //商户私有域
		return HfPaymentUtil.createSignMap(map);
	}

	/**
	 * 提现
	 * @param usrCustId 用户唯一标识
	 * @param merPriv 流水号
	 * @param transAmt 提现金额
	 * @param servFee 手续费
	 * @return
	 */
	public LinkedHashMap<String, String> withdraw(String usrCustId, String ordId, double transAmt, double servFee,String account, int client) { //2016 add

		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("FeeObjFlag", "M");  //平台垫付取现手续费
		json.put("FeeAcctId", HfConstants.TRANSFEROUTACCTID);
		array.put(json);
		
		String reqExt = array.toString();
		map.put("Version", HfConstants.VERSION2);  //版本号
		map.put("CmdId", HfConstants.CMD_CASH);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("OrdId", ordId); //订单号
		map.put("UsrCustId", usrCustId);  //用户客户号
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt)); //提现金额
		map.put("OpenAcctId", account);
		//map.put("OpenBankId",bankcord );
		map.put("ServFee", HfPaymentUtil.formatAmount(servFee));  //商户收取服务费金额
		map.put("ServFeeAcctId", HfConstants.SERVFEEACCTID);  //商户子账户
		
		if(client == 5){
			map.put("RetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/cash"); //商户回调地址
			map.put("BgRetUrl", BaseController.getBaseURL() + "m/payment/chinapnr/cashAyns");  //商户后台应答地址
		}else{
			map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/cash"); //商户回调地址
			map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/cashAyns");  //商户后台应答地址
		}
		map.put("MerPriv", ordId);  //商户私有域
		map.put("ServFeeAcctId", HfConstants.SERVFEEACCTID); //商户子账户
		map.put("ReqExt", reqExt); //请求扩展参数
		return HfPaymentUtil.createSignMap(map);
	}

	/**
	 *  转账,商户转用户 （商户用）
	 * @param ordId  订单号
	 * @param transAmt  交易金额
	 * @param inCustId  入账账户
	 * @return
	 */
	public LinkedHashMap<String,String> doTransfer(String ordId, double transAmt, String inCustId){
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("OrdId", ordId);
		map.put("CmdId", HfConstants.CMD_TRANSFER);	// 消息类型
		map.put("OutCustId", HfConstants.MERCUSTID);
		map.put("OutAcctId", HfConstants.TRANSFEROUTACCTID);
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));
		map.put("InCustId", inCustId);
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/transferAyns");
		map.put("MerPriv", ordId);  //请求操作唯一标识，通常为交易流水号
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 *  转账,商户转用户 （商户用）
	 * @param ordId  订单号
	 * @param transAmt  交易金额
	 * @param inCustId  入账账户
	 * @return
	 */
	public LinkedHashMap<String,String> doMerchantTransfer(String ordId, double transAmt, String inCustId,String agentPayId){
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("OrdId", ordId);
		map.put("CmdId", HfConstants.CMD_TRANSFER);	// 消息类型
		map.put("OutCustId", HfConstants.MERCUSTID);
		map.put("OutAcctId", HfConstants.TRANSFEROUTACCTID);
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));
		map.put("InCustId", inCustId);
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/transferAyns");
		map.put("MerPriv", agentPayId);  //请求操作唯一标识，通常为交易流水号
		return HfPaymentUtil.createSignMap(map);
	}
	/**
	 * 用户管理类接口-用户登入接口
	 * @param usrCustId 用户客户号
	 * @return
	 */
	public LinkedHashMap<String, String> loginAccount(String usrCustId) {

		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", HfConstants.CMD_USERLOGIN); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 客户商户号
		map.put("UsrCustId", usrCustId); // 用户商户号

		return HfPaymentUtil.createSignMap(map);
	}

	

	/**
	 * 交易类接口-提现请求参数构造
	 * @param ordId 订单号
	 * @param usrCustId 用户客户号
	 * @param transAmt 交易金额
	 * @param servFee 商户收取服务费金额
	 * @return
	 */
	public LinkedHashMap<String, String> merWithdrawal(String ordId, String usrCustId, Double transAmt,
			Double servFee) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

		// [{"FeeObjFlag":"M/U","FeeAcctId":"MDT000001","CashChl":"FAST|GENERAL|IMMEDIATE"}]
		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("FeeObjFlag", "M");
		json.put("FeeAcctId", HfConstants.TRANSFEROUTACCTID);
		array.put(json);

		String ReqExt = array.toString();

		map.put("Version", HfConstants.VERSION2); // 版本号
		map.put("CmdId", HfConstants.CMD_CASH); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 商户客户号
		map.put("OrdId", ordId); // 订单号
		map.put("UsrCustId", usrCustId); // 用户客户号
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt)); // 交易金额
		map.put("ServFee", HfPaymentUtil.formatAmount(servFee)); // 商户收取服务费金额
		map.put("ServFeeAcctId", HfConstants.SERVFEEACCTID); // 商户子账户
		map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/merCash"); // 页面返回URL
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/merCashAyns"); // 商户后台应答地址
		map.put("MerPriv", ordId); //商户私有域
		map.put("ReqExt", ReqExt);
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 交易类接口-网银充值
	 * @param usrCustId  用户客户号
	 * @param ordId  订单号
	 * @param transAmt  交易金额
	 * @return
	 */
	public LinkedHashMap<String, String> merchantRecharge(String ordId, String usrCustId, Double transAmt) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", HfConstants.CMD_NETSAVE); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		map.put("OrdId", ordId); // 订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd")); // 订单日期
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt)); // 交易金额
		map.put("RetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/merNetSave"); // 页面返回URL
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/merNetSaveAyns"); // 商户应答地址
		map.put("MerPriv", ordId);
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 查询充值对账
	 * @return
	 */
	public LinkedHashMap<String, String> querySaveReconciliation(Date beginTime, Date endTime, int pageSize, int pageNum){
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_SAVERECONCILIATION);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("BeginDate",  HfPaymentUtil.getFormatDate("yyyyMMddhhmmss", beginTime));
		map.put("EndDate", HfPaymentUtil.getFormatDate("yyyyMMddhhmmss", endTime));
		map.put("PageNum", pageNum+"");
		map.put("PageSize", pageSize+"");
		
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 查询类接口-余额查询(后台)
	 * @param usrCustId 用户客户号
	 * @return
	 */
	public LinkedHashMap<String, String> queryAmountByMerchant(String usrCustId) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);// 版本号
		map.put("CmdId", HfConstants.CMD_QUERYBALANCEBG);// 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);// 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 查询用户绑定银行卡列表
	 * 
	 * @param usrCustId 用户客户号
	 * @return
	 */
	public LinkedHashMap<String, String> queryBindedBankCard(String usrCustId) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);// 版本号
		map.put("CmdId", HfConstants.CMD_QUERYCARDINFO);// 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);// 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		return HfPaymentUtil.createSignMap(map);
	}
	/* 查询用户余额
	 * */
	
	public LinkedHashMap<String, String> queryBalanceBg(String usrCustId) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);// 版本号
		map.put("CmdId", HfConstants.CMD_QUERYBALANCEBG);// 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);// 商户客户号
		map.put("UsrCustId", usrCustId); // 用户客户号
		return HfPaymentUtil.createSignMap(map);
	}

	/**
	 * 资金解冻
	 * @param ordId 订单号
	 * @param trxId 解冻号
	 * @param error
	 * @return
	 */
	public LinkedHashMap<String, String> userUnFreeze(String ordId, String trxId, ErrorInfo error){
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();		
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_USRUNFREEZE);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("OrdId", ordId);
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd"));
		map.put("TrxId", trxId);
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/usrUnFreezeAyns");
		map.put("MerPriv", ordId);
		return HfPaymentUtil.createSignMap(map);
	}

	
	
	
	/**
	 * 交易类接口-自动投标参数构造
	 * 
	 * @param ordId
	 * @param ordDate
	 * @param transAmt
	 * @param usrCustId
	 * @param maxTenderRate
	 * @param borrowerDetails
	 * @param isFreeze
	 * @param freezeOrdId
	 * @return
	 */
	public Map<String, String> autoInvest(String ordId,double transAmt, String usrCustId, String borrowerDetails, String freezeOrdId) {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION2);  //版本号
		map.put("CmdId", HfConstants.CMD_AUTOTENDER);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("OrdId", ordId);  //订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd")) ; //订单日期
		map.put("TransAmt",  HfPaymentUtil.formatAmount(transAmt));  //交易金额
		map.put("UsrCustId", usrCustId) ; //用户客户号
		map.put("MaxTenderRate", HfConstants.MAXTENDERRATE) ; //最大投资手续费率
		map.put("BorrowerDetails", borrowerDetails)  ;//借款人信息
		map.put("BgRetUrl", BaseController.getBaseURL() + "payment/chinapnr/autoTenderAyns") ; //商户后台应答地址
		map.put("IsFreeze", "Y");//是否冻结
		map.put("FreezeOrdId", freezeOrdId);  //冻结订单号
		map.put("MerPriv", ordId);
		return HfPaymentUtil.createSignMap(map);

	}
	
	/**
	 * 查询余额
	 * @param usrCustId 投资人用户客户号
	 * @return
	 */
	public LinkedHashMap<String, String>  queryAmount(String usrCustId) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1);
		map.put("CmdId", HfConstants.CMD_QUERYBALANCEBG);
		map.put("MerCustId", HfConstants.MERCUSTID);
		map.put("UsrCustId", usrCustId);
		return HfPaymentUtil.createSignMap(map);
	}

	/**
	 * 放款
	 * @param orderNo 订单号
	 * @param outCustId 出账客户号
	 * @param transAmt 交易金额
	 * @param fee 扣款手续费
	 * @param subOrdId 投标订单号
	 * @param subOrdDate 投标订单日期
	 * @param inCustId 入账客户号
	 * @param unFreezeOrdId 解冻号
	 * @param freezeTrxId 冻结号
	 * @param divDetails 分账账户
	 * @param reqExt 扩展
	 * @return
	 */
	public LinkedHashMap<String, String>  bidAuditSucc(String orderNo, String outCustId, double transAmt, double fee, String subOrdId,
			String subOrdDate, String inCustId, String unFreezeOrdId, String freezeTrxId, String divDetails, String reqExt, String proId) {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION2);  //版本号
		map.put("CmdId", HfConstants.CMD_LOANS);  //消息类型
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("OrdId", orderNo);  //订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd", new Date()));  //订单日期
		map.put("OutCustId", outCustId);  //出账客户号
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));  //交易金额
		map.put("Fee", HfPaymentUtil.formatAmount(fee));  //扣款手续费
		map.put("SubOrdId", subOrdId);  //订单号
		map.put("SubOrdDate", subOrdDate);  //订单日期
		map.put("InCustId", inCustId);  //入账客户号
		map.put("IsDefault", "N");  //是否默认Y--默认取现到银行卡 N--默认不取现到银行卡
		map.put("IsUnFreeze", "Y");  //是否解冻
		map.put("UnFreezeOrdId", unFreezeOrdId);  //解冻订单号
		map.put("FreezeTrxId", freezeTrxId);  //冻结标识
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/loansAyns");  //商户后台应答地址
		map.put("ReqExt", reqExt);  //入参扩展域
		if(fee > 0){
			map.put("FeeObjFlag", "I");  //手续费收取对象标志(I/O)
			map.put("DivDetails", divDetails);  //分账客户串		
		}
		map.put("proId", proId);  //标的号
		map.put("MerPriv", orderNo); //私有域
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 用户支付接口
	 * @param ordId		订单号
	 * @param usrCustId	用户商户号
	 * @param transAmt	交易金额
	 * @return
	 */
	public LinkedHashMap<String, String> usrAcctPay(String ordId, String usrCustId, double transAmt) {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION1);  //版本号
		map.put("CmdId", HfConstants.CMD_USRACCPAY);  //交易类型
		map.put("OrdId", ordId);  //订单号
		map.put("UsrCustId", usrCustId);  //用户客户号
		map.put("MerCustId", HfConstants.MERCUSTID);  //商户客户号
		map.put("TransAmt", HfPaymentUtil.formatAmount(transAmt));  //交易金额
		map.put("InAcctId", HfConstants.SERVFEEACCTID);  //入账子账户
		map.put("InAcctType", "MERDT");  //入账账户类型
		map.put("RetUrl", BaseController.getBaseURL() + "payment/chinapnr/usrAcctPay");  //页面返回URL
		map.put("BgRetUrl",BaseController.getBaseURL() + "payment/chinapnr/usrAcctPayAyns");  //商户后台应答地址
		map.put("MerPriv", ordId);
		
		return HfPaymentUtil.createSignMap(map);
	}
	
	/**
	 * 用户批量还款接口
	 * @param outCustId 	出账商户号
	 * @param batchId 		批量还款订单号
	 * @param merOrdDate 	批量还款日期
	 * @param inDetails 	还款账单列表
	 * @param proId 		借款流水号
	 * @return
	 */
	public LinkedHashMap<String, String> repayment(String outCustId,String batchId,String merOrdDate,String inDetails,
			String proId) {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("Version", HfConstants.VERSION2);			// 版本号
		map.put("CmdId", HfConstants.CMD_BATCHREPAYMENT);	// 请求指令
		map.put("MerCustId", HfConstants.MERCUSTID);		// 客户商户号
		map.put("OutCustId", outCustId);					// 出账子账号
		map.put("BatchId", batchId);						// 批量订单流水号
		map.put("MerOrdDate", merOrdDate);					// 操做时间
		map.put("BgRetUrl", BaseController.getBaseURL() + "newr/payment/chinapnr/batchRepaymentAyns");
		map.put("InDetails", inDetails);					// 入账列表
		map.put("ProId", proId); 							//标的号即项目号
		map.put("MerPriv", batchId);						// 订单流水号
		
		return HfPaymentUtil.createSignMap(map);
	}
	
	
	/**
	 * 生成批量还款接口所需的理财账单列表JSON格式参数
	 * @param list				// 账单列表
	 * @param managementRate	// 管理费
	 * @return
	 */
	public String generateBatchRepaymentInDetails(List<t_bill_invests> list, double managementRate){
		JsonObject batchRepaymentJson = new JsonObject();
		JsonArray array = new JsonArray();
		JsonObject json = null;
		Invest investSingle = null;
		for(int i = 0; i< list.size(); i++){
			t_bill_invests invest = list.get(i);
			//投资人收益
			double pInAmt = invest.receive_interest + invest.receive_corpus + invest.overdue_fine;
			//投资管理费
			double pInFee = BillInvests.getInvestManagerFee(invest.receive_interest, managementRate, invest.user_id);  
			
			//初始化投资人
			User invester = new User();
			invester.id = invest.user_id;
			investSingle = new Invest();
			investSingle.id = invest.invest_id;
			json = new JsonObject();
			//json.addProperty("InCustId", invester.ipsAcctNo);
			json.addProperty("OrdId", invest.mer_bill_no + invest.periods);  //需要固定,否则会存在账单多次还款
			json.addProperty("SubOrdId", investSingle.merBillNo);
			
			json.addProperty("TransAmt", HfPaymentUtil.formatAmount(pInAmt));
			
			if(pInFee != 0.0){
				json.addProperty("FeeObjFlag", "I");
				json.addProperty("Fee", HfPaymentUtil.formatAmount(pInFee));
				JsonArray divDetails = new JsonArray();
				JsonObject divJson = new JsonObject();
				divJson.addProperty("DivCustId", HfConstants.MERCUSTID);
				divJson.addProperty("DivAcctId", HfConstants.SERVFEEACCTID);
				divJson.addProperty("DivAmt", HfPaymentUtil.formatAmount(pInFee));
				divDetails.add(divJson);
				json.add("DivDetails", divDetails);
			}
			array.add(json);
			
		}
		
		batchRepaymentJson.add("InDetails", array);
		
		return batchRepaymentJson.toString();
	}
	
	/**
	 * 交易类接口-登记债权转让请求参数构造
	 * @param sellCustId 转让人客户号
	 * @param creditAmt 转让金额
	 * @param creditDealAmt 承接金额
	 * @param bidDetails 债权转让明细
	 * @param fee 扣款手续费
	 * @param buyCustId 承接人客户号
	 * @param ordId 订单号
	 * @return
	 */
	public LinkedHashMap<String,String> debtorTransfer(String sellCustId,double creditAmt,double creditDealAmt,
			String bidDetails, double fee, String buyCustId, String ordId) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Version", HfConstants.VERSION1); // 版本号
		map.put("CmdId", HfConstants.CMD_CREDITASSIGN); // 消息类型
		map.put("MerCustId", HfConstants.MERCUSTID); // 商户客户号
		map.put("SellCustId", sellCustId); // 转让人客户号
		map.put("CreditAmt", HfPaymentUtil.formatAmount(creditAmt)); // 转让金额
		map.put("CreditDealAmt", HfPaymentUtil.formatAmount(creditDealAmt)); // 承接金额
		map.put("BidDetails", bidDetails); // 债权转让明细
		map.put("Fee", HfPaymentUtil.formatAmount(fee)); // 扣款手续费
		map.put("BuyCustId", buyCustId); // 承接人客户号
		map.put("OrdId", ordId); // 订单号
		map.put("OrdDate", HfPaymentUtil.getFormatDate("yyyyMMdd")); // 订单日期
		map.put("BgRetUrl", BaseController.getBaseURL() + "payment/chinapnr/creditAssignAyns"); // 商户后台应答地址
		map.put("RetUrl", BaseController.getBaseURL() + "payment/chinapnr/creditAssign"); // 页面返回URL
		map.put("MerPriv", ordId);  //接口请求唯一标识，用于回调时获取日志参数
		
		if (fee != 0) {
			map.put("DivDetails",
					"[{\"DivAcctId\":\"" + HfConstants.SERVFEEACCTID + "\",\"DivAmt\":\"" + fee + "\"}]");
		}
		
		return HfPaymentUtil.createSignMap(map);
	}

	/**
	 * 生成流水号(最长16位)
	 */
	@Override
	public String createBillNo() {
		SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
		t_sequences sequence = new t_sequences();
		sequence.save();		
		return format.format(new Date()) + sequence.id+"" ;
	}
	
	public String createMerchantTransferUserNo(String userid) {
		SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
		t_sequences sequence = new t_sequences();
		sequence.save();		
		return format.format(new Date()) + sequence.id+userid;
	}

	
	@Override
	public void printRequestData(Map<String, String> param, String mark, PayType payType) {
		Logger.info("******************"+mark+"开始******************");
		for(Entry<String, String> entry : param.entrySet()){			
			Logger.info("***********"+entry.getKey() + "--" + entry.getValue());
		}
		Logger.info("******************"+mark+"结束******************");
		
		if(payType.getIsSaveLog()){		
			JPAUtil.transactionBegin();
			t_mmm_data t_mmm_data = new t_mmm_data();
			t_mmm_data.mmmUserId = param.get("UsrCustId") == null ? "-1" : param.get("UsrCustId");
			t_mmm_data.orderNum = param.get("MerPriv");
			t_mmm_data.parent_orderNum = param.get("parentOrderno");
			t_mmm_data.op_time = new Date();
			t_mmm_data.msg = mark;
			t_mmm_data.data = gson.toJson(param);
			t_mmm_data.status = 1;
			t_mmm_data.type = payType.name();		
			t_mmm_data.url = param.containsKey("BgRetUrl")?param.get("BgRetUrl"):"";
			t_mmm_data.save();
			JPAUtil.transactionCommit();
		}
	}
	
	@Override
	public void printData(Map<String, String> paramMap, String desc, PayType payType){
		
		if(paramMap.containsKey("body")){
			paramMap.remove("body");
		}
		Gson gson = new Gson();
		Logger.info("**********************"+desc+"开始***************************");
		
		for(Entry<String, String> entry : paramMap.entrySet()){
			try {
				Logger.info("***********"+entry.getKey() + "--" + java.net.URLDecoder.decode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Logger.info("**********************"+desc+"结束***************************");
		
		if(payType.getIsSaveLog()){		
			JPAUtil.transactionBegin();			
			Map<String, String> t_mmm_data = this.queryRequestData(paramMap.get("MerPriv"), new ErrorInfo());		
			t_return_data t_return_data = new t_return_data();
			t_return_data.mmmUserId = t_mmm_data.get("UsrCustId") == null ? "" : t_mmm_data.get("UsrCustId").toString();
			t_return_data.orderNum = paramMap.get("MerPriv") ;
			t_return_data.parent_orderNum = paramMap.get("parentOrderno");
			t_return_data.op_time = new Date();
			t_return_data.type = payType.name();
			t_return_data.data = gson.toJson(paramMap);				
			t_return_data.save();
			JPAUtil.transactionCommit();
		}
	}
	
	
	
}
