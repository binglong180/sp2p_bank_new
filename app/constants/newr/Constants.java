package constants.newr;

import play.Play;

import com.shove.Convert;

/**
 * 常量值
 * 
 * @author
 */
public class Constants {	
	/**
	 * 汇付:HF
	 */
	public static final String TRUST_FUNDS_HF = "HF";	
	public static final String ROOT = Play.applicationPath.getAbsolutePath();
	
	/**
	 * 中金-机构代码
	 */
	public static final String INSTITUTIONID = Play.configuration.getProperty("InstitutionID");
	public static final String BASE_URL = Play.configuration.getProperty("test.application.baseUrl") + Play.configuration.getProperty("http.path") + "/";
	public static final String HTTP_PATH = Play.configuration.getProperty("http.path");
	public static final String ENCRYPTION_KEY = Play.configuration.getProperty("fixed.secret");			//加密key
	public static final String APP_ENCRYPTION_KEY = Play.configuration.getProperty("app.fixed.secret");			//APP加密key
	public static final String TRUST_FUNDS = "true";			//资金托管模式
	public static final boolean IPS_ENABLE = Convert.strToBoolean(Play.configuration.getProperty("pay.trustFunds"), false);	//是否开启资金托管模式


	public static final String ERROR_PAGE_PATH_FRONT = "@Application.errorFront";
	public static final String ERROR_PAGE_PATH_SUPERVISOR = "@Application.errorSupervisor";
	public static final String ERROR_PAGE_PATH_INJECTION = "@Application.injection";
	public static final String COOKIE_KEY_SUPERVISOR_ID = "something";
	public static final String COOKIE_KEY_SUPERVISOR_ACTION = "supervisor";
	public static final String COOKIE_KEY_USER_ID = "sp2p6";
	public static final String COOKIE_KEY_USER_ACTION = "front";
	public static final String CACHE_TIME_HOURS_12 = "12h";
	public static final String CACHE_TIME_MINUS_30 = "30min";
	public static final String CACHE_TIME_MINUS_10 = "10min";
	public static final String CACHE_TIME_SECOND_60 = "60s";
	public static final String CACHE_TIME_MINUS_2 = "2min";
	public static final String LOCALHOST_IP = "127.0.0.1";
	public static final int PAGE_SIZE = 10;
	public static final int PAGE_SIZE_EIGHT = 8;
	
	public static final String API_KEY = Play.configuration.getProperty("api_key");
	public static final String SECRET_KEY = Play.configuration.getProperty("secret_key");
	
	/**
	 * 不同支付平台差异性融合, 各托管在各自常理类中初始化
	 */
	public static   boolean DEBT_USE = true;				//是否有债权转让，默认普通网管配置
	public static   boolean IS_DEBT_TWO = false;			//是否支持二次债权转让
	public static   boolean IS_LOGIN = false;				//是否需要登录
	public static   boolean IS_SECOND_BID = true;			//是否有秒还标
	public static   boolean IS_FLOW_BID = true;			//是否有自动流标
	public static   boolean IS_WITHDRAWAL_AUDIT;	//提现后是否需要审核
	public static   boolean IS_WITHDRAWAL_INNER;	//是否支持提现内扣
	public static   boolean IS_GUARANTOR;			//是否需要登记担保方
	public static   boolean IS_OFFLINERECEIVE = true;		//是否支持本金垫付、线下收款
	public static   boolean IS_LOCALHOST = Convert.strToBoolean(Play.configuration.getProperty("is.localhost"), true);//是否本地测试，true：测试，false：正式环境		
	public static final int JOB_EMAIL_AMOUNT = 40;  // 邮件定时发送数量
	public static final int JOB_MSG_AMOUNT = 100;   // 短信定时发送数量
	public static final int JOB_STATION_AMOUNT = 100; // 站内信定时发送数量	
	public static final boolean DEV_PROD = Play.configuration.getProperty("application.mode").equals("dev") ? true : false;
	public static final boolean CHECK_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_code"), false);//是否需要检验普通验证码
	public static final boolean CHECK_MSG_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_msg_code"), false);//是否需要检验短信验证码
	public static final boolean CHECK_PIC_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_pic_code"), false);//是否需要检验图形验证码
	
	/**
	 * 加密串有效时间(s)
	 */
	public static final int VALID_TIME = 3600;
	public static final String SHOW_BOX = "show_box"; // 一些弹框的sign加密value
	
	/**
	 * 是否启用密码错误次数超限锁定(0不启用，1启用)
	 */
	public static final int CLOSE_LOCK = 0;
	public static final int OPEN_LOCK = 1;
	
	/**
	 * 缓存时间
	 */
	public static final String CACHE_TIME = "10min";
	
	/**
	 * 邮件发送标识
	 */
	public static final String ACTIVE = "active";
	public static final String PASSWORD = "resetPassword";
	public static final String SECRET_QUESTION = "secretQuestion";
	
	/**
	 * 部分加密action标识
	 */
	public static final String BID_ID_SIGN = "b"; // 标ID
	public static final String BILL_ID_SIGN = "bill"; // 标ID
	public static final String PRODUCT_ID_SIGN = "p"; // 产品ID
	public static final String USER_ID_SIGN = "u"; // 用户ID
	public static final String SUPERVISOR_ID_SIGN = "supervisor_id"; // 管理员ID
	public static final String ITEM_ID_SIGN = "i"; // 资料ID
	public static final String USER_ITEM_ID_SIGN = "ui"; // 用户资料ID
	
	public static final String INVEST_ID_SIGN ="invest"; //投资id
	/**
	 * ajax标识
	 */
	public static final String IS_AJAX = "1";
	
	/**
	 * 进行了加密的方法
	 */
	/*修改手机*/
	public static final String VERIFY_SAFE_QUESTION = "front.account.BasicInformation.verifySafeQuestion";
	public static final String SET_SAFE_QUESTION = "front.account.BasicInformation.resetSafeQuestion";
	public static final String PASSWORD_EMAIL = "front.account.LoginAndRegisterAction.resetSafeQuestion";
	
	/**
	 * 固定的路径
	 */
	public static final String LOGIN = BASE_URL + "newr/login";
	public static final String QUICK_LOGIN = BASE_URL + "quick/login";
	public static final String RESET_PASSWORD_EMAIL = BASE_URL + "front/account/resetPassword?sign=";
	public static final String RESET_PAY_PASSWORD_EMAIL = BASE_URL + "front/account/resetDelPassword?sign=";
	public static final String RESET_QUESTION_EMAIL = BASE_URL + "front/account/resetQuestion?sign=";
	public static final String ACTIVE_EMAIL = BASE_URL+"newr/front/account/accountActivation?sign=";
	


	
	public static final boolean TRUE = true;
	public static final boolean FALSE = false;

	public static final boolean ENABLE = true; // 启用
	public static final boolean NOT_ENABLE = false; // 不启用
	public static final int YEAR = -1;// 年
	public static final int MONTH = 0; // 月
	public static final int DAY = 1; // 日

	public static final int _ONE = -1;
	public static final int _TWO = -2;
	public static final int _THREE = -3;
	public static final int _FOUR = -4;
	public static final int _FIVE = -5;
	public static final int _SIX = -6;
	public static final int _SEVEN = -7;
	public static final int _EIGHT = -8;
	public static final int _NINE = -9;
	public static final int _TEN = -10;

	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;
	public static final int SIX = 6;
	public static final int SEVEN = 7;
	public static final int EIGHT = 8;
	public static final int NINE = 9;
	public static final int TEN = 10;
	public static final int FIFTEEN = 15;

	public static final int INSERT = 1;
	public static final int DELETE = 2;
	public static final int UPDATE = 3;
	public static final int SELECT = 4;

	public static final String MAN = "男";
	public static final String WOMAN = "女";
	public static final String UNKNOWN = "未知";

	public static final int FRONT = 0;// 前台
	public static final int ADMIN = 1;// 后台

	public static final int ADD = 1; // 增加
	public static final int MINUS = -1; // 减少
	
	/**
	 * 增加信用积分记录的类型
	 */
	public static final int AUDIT_ITEM = 1;
	public static final int PAYMENT = 2;
	public static final int BID = 3;
	public static final int INVEST = 4;
	public static final int OVERDUE = -1;
	
	/**
	 * 借款标产品
	 */
	public static final boolean NEED_AUDIT = true; // 必选资料
	public static final boolean NOT_NEED_AUDIT = false; // 可选资料
	public static final int PAID_MONTH_EQUAL_PRINCIPAL_INTEREST = 1; // 按月还款、等额本息
	public static final int PAID_MONTH_ONCE_REPAYMENT = 2; // 按月付息、一次还款
	public static final int ONCE_REPAYMENT = 3; // 一次还款
	//public static final int SECOND_TIME_REPAYMENT = 4;// 秒还还款
	public static final int USER_UPLOAD = 0; // 用户上传(图片)
	public static final int PLATFORM_UPLOAD = 1; // 平台上传(图片)
	public static final int PRODUCT_INTRODUCTION = 1; // 借款产品简介
	public static final int PRODUCT_DETAIL_INTRODUCTION = 2; // 借款产品详细描述
	public static final int APPLICANT_CONDITION = 3; // 申请人条件
	public static final int DAY_INTEREST = 360; // 日利息结算常量
	
	public static final int PC = 1; // PC端
	public static final int APP = 2; // APP端
	public static final int WECHAT = 3; // 微信端


	

	/**
	 * 超额借款
	 */
	public static final String OVER_LOAN_NO_THROUGH = "未通过"; //超额借款审核不通过

	/**
	 *账单
	 */
	public static final String AUTO_PAYMENT = "0"; // 自动付款
	public static final String HANDLE_PAYMENT = "1"; // 手动付款
	
	public static final int NO_REPAYMENT = -1; // 未还款
	public static final int NORMAL_REPAYMENT = 0; // 正常还款






	/**
	 * 标
	 */
	public static final int BALANCE_PAY_ENOUGH = -998; // 资金足够支付
	
	/**资金不够**/
	public static final int BALANCE_NOT_ENOUGH = -999;
	public static final int BAIL_NOT_ENOUGH = -1000; // 发标保证金不够
	public static final int APPLY_NOW_INDEX = 1; // 首页立即申请
	public static final int APPLY_NOW_DETAIL = 2; // 详情立即申请
	public static final int INVEST_DETAIL = 3; //投标详情
	public static final double LOWEST_AMOUNT = 0.01; // 在出现金额算法不匹配的情况下,入库基准数
	

	/**审核中**/
	public static final int BID_AUDIT = 0;
	
	/**借款中(审核通过)**/
	public static final int BID_FUNDRAISE =1;
	/**
	 *待放款
	 */
	public static final int BID_PRE_REPAYMENT=2;
	/**已满标**/
	public static final int BID_REPAYMENT = 3;
	/**借款逾期**/
	public static final int BID_OVERTIME = 4;
	/**已还款**/
	public static final int BID_REPAYMENTS = 5;
	

	


	
	
	


	


	
	/* 所有标相关的下拉搜索数组  */
	public static final String[] BID_SEARCH = { 
		" AND (user_name LIKE ? OR bid_no LIKE ? or product_name LIKE ? or title LIKE ? )",
		" AND bid_no LIKE ?", " AND title LIKE ?", " AND user_name LIKE ?", 
		" AND email LIKE ?" , " AND product_name LIKE ?"};
	
	/* 所有标相关的排序数组 */
	public static final String[] BID_SEARCH_ORDER = {
		" ORDER BY id desc ",
		" ORDER BY (CASE user_item_count_true WHEN 0 then (user_item_count_true + 1) else (user_item_count_true/product_item_count + 1) end)",
		" ORDER BY time ",
		" ORDER BY amount ", " ORDER BY order_sort ", " ORDER BY loan_schedule ",
		" ORDER BY apr ", " ORDER BY product_id ", " ORDER BY mark_overdue_time ", 
		" ORDER BY overdue_count ", " ORDER BY last_repay_time ", " ORDER BY mark_bad_time ",
		" ORDER BY audit_time ", " ORDER BY real_invest_expire_time "};
	



	
	/**
	 * 邮件模板场景
	 */
	public static final String FIND_USERNAME = "找回用户名";
	public static final String ACTIVATE_USER = "注册激活";
	public static final String RESET_PASSWORD = "重置密码";
	public static final String RESET_SECRET_QUESTION = "重置安全问题";

	/**
	 * 邮件模板中被替换的词
	 */
	public static final String EMAIL_NAME = "name";
	public static final String EMAIL_EMAIL = "email";
	public static final String EMAIL_LOGIN = "login";
	public static final String EMAIL_URL = "url";
	public static final String EMAIL_TELEPHONE = "telephone";
	public static final String EMAIL_TIME = "time";
	public static final String EMAIL_PLATFORM = "platform";


	
	/**
	 * 分页主题
	 */
	public static final int PAGE_SIMPLE = 1;
	public static final int PAGE_ASYNCH = 2;
	

	
	/**
	 * 系统管理员supervisor
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public static class SystemSupervisor {
		public static final long ID = 1;
	}
	
	/**
	 * 超级管理员组
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class SystemSupervisorGroup {
		public static final long ID = 1;
		public static final String NAME = "超级管理员组";
	}



	
	/**
	 * 阅读状态
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class ReadStatus {
		public static final int Readed = 1; //已读
		public static final int Unread = 2; //未读
	}
	
	/**
	 * 消息查询关键字类型
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class MessageKeywordType {
		public static final int Title = 1; //标题
		public static final int SenderName = 2; //发信人
	}
	

	
	
	/**
	 * 新闻类别
	 * @author lzp
	 * @version 6.0
	 * @created 2014-6-9
	 */
	public class NewsTypeId {
		public static final int WEALTH_INFOMATION = 1;
		public static final int HELP_CENTER = 2;
		public static final int COMPANY_DESCRIPTION = 3;
		public static final int PRINCIPAL_PROTECTION = 4;
		public static final int GETTINT_STARTED = 5;
		public static final int PLATFORM_AGREEMENT = 6;
		public static final int OFFICIAL_AMMOUNCEMENT = 7;
		public static final int INTERNET_BANKING = 8;
		public static final int LOAN_MONPOLY = 9;
		public static final int BORROWING_TECHNIQUES = 10;
		public static final int MONEY_TIPS = 11;
		public static final int SUCCESS_STROY = 12;
//		public static final int FREQUENTLY_QUESTIONS = 13;
		public static final int COMPANY_DESCRIPTION2 = 16;
		public static final int MANAGEMENT_TEAM = 17;
		public static final int EXPERT_ADVISOR = 18;
		public static final int CAREERS = 19;
		public static final int PARTNER = 20;
		public static final int PRINCIPAL_PLAN = 21;
		public static final int PRINCIPAL_RULE = 22;
		public static final int PAYOUT_PROCESS = 23;
		public static final int INVESTMENT_STRATEGY = 24; 
		public static final int PRINCIPAL_PROTECTION_FAQ = 25;
		public static final int GETTING_STARTED = 26;
		public static final int CREDIT_HELP = 27;
		public static final int PRIVACY_POLICY = 28;
		public static final int SERVICE_FEES = 29;
		public static final int SERVICE_AGREEMENT = 30;
		public static final int REGISTER_AGREEMENT = 31;
		public static final int SERVICE_TERMS = 32;
		
		/* 首页页面固定数据匹配ID */
		public static final long SERVICE_CLAUSE = -1001; // 服务条款
		public static final long CREDIT_EXPLAIN = -1002L; // 信用说明
		public static final long PRIVACY_EXPLAIN = -1003L; // 隐私说明
		public static final long COMPANY_INTRODUCE = -1004L; // 公司介绍
		public static final long MANAGE_GROUP = -1005L; // 管理团队
		public static final long SPECIALIST_COUNSELOR = -1006L; // 专家顾问
		public static final long INVITE_TALENTS = -1007L; // 招贤纳士
		public static final long MANAGE_PARTER = -1008L; // 合作伙伴
		public static final long VIP_AGREEMENT = -1009L; // vip会员协议
		public static final long REGISTER_AGREEMENT2 = -1010L; // 注册协议
//		public static final int SPECIALIST_COUNSELOR = 1006; // 专家顾问
		
	}

	/**
	 * 登录注册标识
	 */
	public static final String LOGIN_AREAL_FLAG = "loginOrRegister";

	/**
	 * 用户默认头像
	 */
	public static final String DEFAULT_PHOTO = "/public/images/userImg.png";


	/**
	 * 注册入口
	 */
	public static final int CLIENT_PC = 1;
	public static final int CLIENT_APP = 2;
	public static final int CLIENT_WECHAT = 3;
	public static final int CLIENT_OTHERS = 4;
	public static final int CLIENT_H5 = 5;
	


	
	/**
	 * 一天两端时间
	 */

	public static final String PAGEINPUTTIP ="选填，推荐人手机号";
	public static final double MININVESTMONEY = 1000.00;
}
