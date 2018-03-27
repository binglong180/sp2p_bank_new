package constants;

import play.Play;

import com.shove.security.Encrypt;

public class IPSConstants {

	public static final String ACTION = Play.configuration.getProperty("pay.action");	//资金托管url
	public static final String CALLBACK_URL = "front/PaymentAction/";  //回调地址域名不从配置文件取，修改为从request对象获取，配置（pay.callback.url）废弃
	public static final String DOMAIN = Encrypt.encrypt3DES(Play.configuration.getProperty("pay.domain"), Constants.ENCRYPTION_KEY);//p2p平台域名
	
	public static final String CACHE_TIME = "1h";	//标，投资等信息缓存时间
		
	/**
	 * 资金托管处理状态(环讯)
	 */
	public class IPSDealStatus {
		public static final int NORMAL = 0;  //正常
		
		//标的
		public static final int BID_END_HANDING = 1; //标的投标中
		public static final int LOAN_HANDING = 2; //放款处理中
		
		//借款账单
		public static final int REPAYMENT_HANDING = 1; //还款处理中
		public static final int OFFLINEREPAYMENT_HANDING = 2; //线下收款处理中
		public static final int COMPENSATE_HANDING = 3; //本金垫付处理中

	}
	



}
