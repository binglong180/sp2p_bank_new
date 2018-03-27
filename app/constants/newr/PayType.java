package constants.newr;

import play.Logger;

public enum PayType {
	

	/*** 投标*/
	INVESTSMS(41,true,true,"投资验证码发送成功"),
	INVEST(4,true,true,"投标成功"),

	SETTLE (42,true,true,"结算指令发送成功"),
	SETTLENOTIFY (43,true,true,"结算通知指令接受成功"),
	
	/*** 标的审核通过*/
	BID_AUDIT_SUCC(7,true,true),
	


		
	/*** 还款*/
	REPAYMENT(14,true,true),
	
	/*** 余额查询*/
	QUERY_AMOUNT(15,true,false),
	
	/*** 获取银行列表*/
	QUERY_BANKS(16,true,false),
	
	
	/**放款**/
	LOANS(23,true,true),
	

	
	
	/**用户登录**/
	LOGIN_ACCOUNT(34,true,false),
	
	/**查询用户绑定银行卡列表**/
	QUERY_BINDED_CARDS(35,true,false),
	
	/*** 线下收款(登记担保方)*/
	OFFLINE_REPAYMENT(36,true,true);
	
	private int typeNum;  //类型编号

	private boolean isPrintLog;  //是否将接口参数打印到日志文件
	
	private boolean isSaveLog;  //是否将接口参数保存到数据库
	
	private String successUrl;  //交易成功跳转的页面
	private String successUrlDesc;  //交易成功跳转的页面的描述
	private String successTip;  //交易成功提示语
	
	private String failedUrl;  //交易失败跳转的页面
	private String failedUrlDesc;  //交易失败跳转的页面的描述
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog){
		this.typeNum = typeNum;
		this.isPrintLog = isPrintLog;
		this.isSaveLog = isSaveLog;
	}
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog, String successTip){
		this.typeNum = typeNum;
		this.isPrintLog = isPrintLog;
		this.isSaveLog = isSaveLog;
		this.successTip = successTip;
	}
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog,
			String successTip, String successUrl, String successUrlDesc ){
		this.typeNum = typeNum;
		this.isSaveLog = isSaveLog;
		this.successTip = successTip;
		this.successUrl = successUrl;
		this.successUrlDesc = successUrlDesc;
	}
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog, 
			String successTip, String successUrl, String successUrlDesc,
			String failedUrl, String failedUrlDesc){
		this.typeNum = typeNum;
		this.isSaveLog = isSaveLog;
		this.successTip = successTip;
		this.successUrl = successUrl;
		this.successUrlDesc = successUrlDesc;
		this.failedUrl = failedUrl;
		this.failedUrlDesc = failedUrlDesc;
	}
	
	/**
	 * 根据类型编号获取相应枚举名称
	 * 
	 * @param typeNum
	 * @return
	 */
	public static String getNameByTypeNum(String typeNum){
		PayType[] types = values();
		for(PayType p : types){
			if(p.toString().equals(typeNum)){
				return p.name();
			}
		}
		
		Logger.info("编号为%s的枚举不存在", typeNum);
		return null;
	}
	
	public boolean isPrintLog() {
		return isPrintLog;
	}

	public void setPrintLog(boolean isPrintLog) {
		this.isPrintLog = isPrintLog;
	}

	public int getTypeNum() {
		return typeNum;
	}
	
	public boolean getIsSaveLog() {
		return isSaveLog;
	}

	public void setSaveLog(boolean isSaveLog) {
		this.isSaveLog = isSaveLog;
	}

	public void setTypeNum(int typeNum) {
		this.typeNum = typeNum;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getSuccessUrlDesc() {
		return successUrlDesc;
	}

	public void setSuccessUrlDesc(String successUrlDesc) {
		this.successUrlDesc = successUrlDesc;
	}

	public String getSuccessTip() {
		return successTip;
	}

	public void setSuccessTip(String successTip) {
		this.successTip = successTip;
	}

	public String getFailedUrl() {
		return failedUrl;
	}

	public void setFailedUrl(String failedUrl) {
		this.failedUrl = failedUrl;
	}

	public String getFailedUrlDesc() {
		return failedUrlDesc;
	}

	public void setFailedUrlDesc(String failedUrlDesc) {
		this.failedUrlDesc = failedUrlDesc;
	}
}
