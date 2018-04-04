package controllers.m.front.invest;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import models.t_bids;
import models.t_bill_invests;
import models.t_dict_bid_repayment_types;
import models.t_invests;
import models.t_user_bank_accounts;
import models.newr.t_ticket;
import models.newr.t_users;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import payment.newr.PaymentProxy;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPABase;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import business.BillInvests;
import business.newr.Bid;
import business.newr.Invest;
import business.newr.User;
import business.newr.UserBankAccounts;

import com.shove.security.Encrypt;
import constants.newr.Constants;
import controllers.m.front.account.LoginAndRegisterAction;
import controllers.newr.BaseController;

/**
 * 
 * @author chencheng
 * 
 */
public class InvestAction extends BaseController {

	public static void investSuccess() {
		render();
	}

	public static void investHome() {

		ErrorInfo error = new ErrorInfo();

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		String apr = params.get("apr");
		String amount = params.get("amount");
		String loanSchedule = params.get("loanSchedule");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String loanType = params.get("loanType");
		String minLevel = params.get("minLevel");
		String maxLevel = params.get("maxLevel");
		String orderType = params.get("orderType");
		String keywords = params.get("keywords");

		/*
		 * PageBean<v_front_all_bids> pageBean = new
		 * PageBean<v_front_all_bids>(); pageBean =
		 * Invest.queryAllBids(Constants.SHOW_TYPE_1, currPage, pageSize, apr,
		 * amount, loanSchedule, startDate, endDate, loanType, minLevel,
		 * maxLevel, orderType, keywords, error);
		 */

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		render(null, null, null, null);
	}

	/**
	 * 投资详情
	 */
	public static void investDetail(long bidId, String showBox) {

		ErrorInfo error = new ErrorInfo();

		Bid bid = new Bid();
		bid.id = bidId;
		// 票号
		// bid.get
		// SELECT ticket_no FROM `t_ticket` where id=1;
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		User user = User.currUser();

		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		boolean flag = false;

		if (StringUtils.isNotBlank(showBox)) {
			showBox = Encrypt.decrypt3DES(showBox, bidId + Constants.ENCRYPTION_KEY);

			if (showBox.equals(Constants.SHOW_BOX))
				flag = true;
		}

		int status = Constants.INVEST_DETAIL;

		PageBean<t_invests> pageBean = new PageBean<t_invests>();
		pageBean = Invest.queryBidInvestRecords(bidId, error);
		render(bid, null, null, user, status, pageBean, null, uuid);
	}

	/**
	 * 投资列表
	 */
	public static void investList() {
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		int pageSize = 15;
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		PageBean<t_bids> pageBean = new PageBean<t_bids>();
		pageBean = Invest.queryAllBids(currPage, pageSize, error);
		render(pageBean);
	}

	/**
	 * 查询列表Ajax方法
	 */
	public static void investListAjax() {
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		int pageSize = 15;
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		PageBean<t_bids> pageBean = new PageBean<t_bids>();
		pageBean = Invest.queryAllBids(currPage, pageSize, error);
		renderJSON(pageBean.page);
	}

	/**
	 * 确认投标
	 * 
	 * @param bidId
	 */
	public static void confirmInvest(String sign, String uuid) {
		User user = User.currUser();
		if (null == user)
			LoginAndRegisterAction.login(null);

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if (bidId < 1) {
			flash.error(error.msg);

			investDetail(bidId, "");
		}

		/* 防重复提交 */
		if (!CaptchaUtil.checkUUID(uuid)) {
			flash.error("请求已提交或请求超时!");

			investDetail(bidId, "");
		}

		String investAmountStr = params.get("investAmount").trim();

		if (StringUtils.isBlank(investAmountStr)) {
			error.msg = "投标金额不能为空！";
			flash.error(error.msg);
			investDetail(bidId, "");
		}
		if (Double.parseDouble(investAmountStr) < Constants.MININVESTMONEY) {
			error.msg = "投标金额不能低于" + Constants.MININVESTMONEY + "！";
			flash.error(error.msg);
			investDetail(bidId, "");
		}
		boolean b = investAmountStr.matches("^[1-9][0-9]*$");
		if (!b) {
			error.msg = "对不起！只能输入正整数!";
			flash.error(error.msg);
			investDetail(bidId, "");
		}

		int investAmount = Integer.parseInt(investAmountStr);
		Invest.invest(user.id, bidId, investAmount, Constants.CLIENT_PC, error);

		if (error.code < 0) {
			flash.error(error.msg);
			investDetail(bidId, "");
		}

		Map<String, String> bid = Invest.bidMap(bidId, error);

		if (error.code < 0) {
			flash.error("对不起！系统异常！请您联系平台管理员！");
			investDetail(bidId, "");
		}
		// 调用托管接口
		// 中金-市场订单支付
		Map<String, Object> resultMap = PaymentProxy.getInstance().investSMS(error, Constants.PC,
				t_bids.findById(bidId), user, investAmount);
		if (error.code > 0) {
			Long inveestId = (Long) resultMap.get("investId");
			String investsign = Security.addSign(inveestId, Constants.INVEST_ID_SIGN);
			String investUUid = CaptchaUtil.getUUID();
			JSONObject data = new JSONObject();
			data.put("seccuess", true);
			data.put("investsign", investsign);
			data.put("investUUid", investUUid);
			renderJSON(data);
		}

		if (error.code > 0) {
			flash.put("amount", NumberUtil.amountFormat(investAmount));
			String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);
			investDetail(bidId, showBox);
		} else {
			flash.error(error.msg);
			investDetail(bidId, "");
		}
		JSONObject data = new JSONObject();
		data.put("seccuess", false);
		data.put("errorMsg", error.msg);
		renderJSON(data);
	}

	/**
	 * 投标记录分页ajax方法
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @param bidId
	 */
	public static void viewBidInvestRecords(int pageNum, int pageSize) {

		ErrorInfo error = new ErrorInfo();
		int currPage = pageNum;
		int pageSizeT = pageSize;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		PageBean<models.newr.v_newr_invest_records> pageBean = new PageBean<models.newr.v_newr_invest_records>();
		if (User.currUser() != null) {

			pageBean = Invest.queryBidInvestRecords(currPage, pageSizeT, User.currUser().id, error);

		} else {
			redirect("/m");
		}

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		render(pageBean);
	}

	/**
	 * 投资记录ajax方法 add by lyz4.12
	 * 
	 * @param pageNum
	 * @param pageSize
	 */
	public static void viewBidInvestRecordsAjax(int pageNum, int pageSize) {

		ErrorInfo error = new ErrorInfo();
		int currPage = pageNum;
		int pageSizeT = 10;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		PageBean<models.newr.v_newr_invest_records> pageBean = new PageBean<models.newr.v_newr_invest_records>();
		if (User.currUser() != null) {
			pageBean = Invest.queryBidInvestRecords(currPage, pageSizeT, User.currUser().id, error);
		} else {
			redirect("/m");
		}

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		renderJSON(pageBean.page);
	}

	/**
	 * ##############################中金支付接口begin###############################
	 */
	/**
	 * 1 做防重复提交处理
	 * 
	 */
	public static void investVerifySMS(String sign, String uuid, String smscode) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long investId = Security.checkSign(sign, Constants.INVEST_ID_SIGN, Constants.VALID_TIME, error);
		if (investId < 1) {
			json.put("success", false);
			json.put("message", error.msg);
			renderJSON(json);
		}

		/* 防重复提交 */
		if (!CaptchaUtil.checkUUID(uuid)) {
			json.put("success", false);
			json.put("message", "请求已提交或请求超时!");
			renderJSON(json);
		}
		try {
			Map<String, Object> resultMap = PaymentProxy.getInstance().investVerifySMS(error, Constants.PC, investId,
					smscode);
			t_invests invest = t_invests.findById(Long.valueOf(investId));
			t_bids bid = t_bids.findById(invest.bid_id);
			long bidId = bid.id;

			double has_invested_amount = bid.has_invested_amount + invest.amount;
			int rows = 0;
			//查看是否满标
			String sql = "update t_bids set has_invested_amount=? where id= ? ";
			rows = JpaHelper.execute(sql,has_invested_amount, bidId).executeUpdate();
			if (has_invested_amount >= bid.amount) {
				sql = "update t_bids set invest_expire_time=NOW() where id= ? ";
				rows = JpaHelper.execute(sql, bidId).executeUpdate();
				sql = "update t_bids set status=3 where id= ? ";
				rows = JpaHelper.execute(sql, bidId).executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void investContract() {
		long id = 469;
		Map map = Invest.queryInvestById(id);
		UserBankAccounts account = (UserBankAccounts) map.get("account");
		t_invests invests = (t_invests) map.get("invests");
		t_bids bid = (t_bids) map.get("bid");
		t_dict_bid_repayment_types repaymentTypes = (t_dict_bid_repayment_types) map.get("repaymentTypes");
		t_ticket ticket = (t_ticket) map.get("ticket");
		System.out.println(account);
		render(account, invests, bid, repaymentTypes, ticket);

	}
}
