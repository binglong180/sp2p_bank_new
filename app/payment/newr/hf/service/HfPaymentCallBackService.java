package payment.newr.hf.service;

import java.util.Map;

import payment.newr.hf.util.HfPaymentUtil;
import utils.ErrorInfo;
import business.newr.Bill;

import com.google.gson.Gson;

import constants.newr.PayType;

/**
 * 汇付接口回调，业务类
 * @author liuwenhui
 *
 */
public class HfPaymentCallBackService extends HfPaymentService{
	
	public Gson gson = new Gson();	
	
	/**
	 * 批量还款，回调业务
	 * @param respParams
	 * @param desc
	 * @param error
	 */
	public void batchRepayment(Map<String, String> paramMap, String desc, ErrorInfo error) {
		error.clear();
		
		// 获取请求参数
		Map<String, String> maps = this.queryRequestData(paramMap.get("BatchId"), error);
		long billId = Long.valueOf(maps.get("UBillId").toString());
		long userId = Long.valueOf(maps.get("UUserId").toString());
		
		// 打印返回参数
		this.printData(paramMap, desc, PayType.REPAYMENT);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, PayType.REPAYMENT, error);
		if(error.code < 0){
			
			return;
		}
		
		// 还款逻辑
		Bill bill = new Bill();
		bill.id = billId;
		bill.repayment(userId, error);
		
		if(error.code >= 0) {
			error.msg = "还款成功！";
		}
	}

	


}
