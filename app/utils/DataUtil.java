package utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_mmm_data;
import models.t_return_data;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Http.Request;
import constants.Constants;
import constants.newr.PayType;

public class DataUtil {
	private static final Gson gson = new Gson();
	/**
	 * 更新数据（只能用于单表，且条件为“=”的更新）
	 * @param table 表名
	 * @param params 更新的字段
	 * @param conditions 条件字段
	 * @param values 所有字段的值 
	 * @param info 错误信息
	 * @return -1异常 0正常
	 */
	public static int update(String table, String[] params, String[] conditions,
			Object[] values, ErrorInfo info) {
		
		info.clear();
		
		StringBuffer sql = new StringBuffer("update ");
		sql.append(table + " set");
		
		for (String param : params) {
			sql.append(" " + param + " = ?,");
		}
		String mysql = sql.substring(0, sql.length() - 1);

		if (conditions != null && conditions.length > 0) {
			sql = new StringBuffer(mysql);
			sql.append(" where");
			
			for (String condition : conditions) {
				sql.append(" " + condition + " = ?,");
			}
			mysql = sql.substring(0, sql.length() - 1);
		}
		
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql.toString());
		
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i + 1, values[i]);
		}

		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			Logger.info("update:%s", e.getMessage());
			info.code = -1;
			info.msg = "更新失败";
			
			return -1;
		}
		
		return rows;
	}
	
	/**
	 * 获取请求的ip地址
	 * @return
	 */
	public static String getIp() {
		Request request = Request.current();
		
		if(null == request) {
			return Constants.LOCALHOST_IP;
		}
		
		return StringUtils.isBlank(request.remoteAddress.toString()) ? Constants.LOCALHOST_IP : request.remoteAddress.toString();
	}
	
	/**
	 * 返回格式化后字符串，保留2位小数，每3位以“,”隔开，如： 80,100.09
	 * 参数如果不是数字类型，则返回其字符串形式
	 * @param obj
	 * @return
	 */
	public static String formatString(Object obj) {
		if (null == obj) {
			return StringUtils.EMPTY;
		}
		try {
			return String.format("%,.2f", Double.valueOf(String.valueOf(obj)));
		} catch (Exception e) {
			return obj.toString();
		}
	}
	public static void printRequestData(Map<String, String> param, String mark, PayType payType) {
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
	

	public static void printData(Map<String, String> paramMap, String desc, PayType payType){
		
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
			Map<String, String> t_mmm_data = queryRequestData(paramMap.get("MerPriv"), new ErrorInfo());		
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
	/**
	 * 根据流水号查询提交参数
	 * @param orderNum
	 * @return
	 */
	public static Map<String, String> queryRequestData(String orderNum, ErrorInfo error){
		error.clear();
		
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find("orderNum = ?", orderNum).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据流水号查询提交参数时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "根据流水号查询提交参数失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -2;
			error.msg = "根据流水号查询提交参数时传入的流水账单号【"+orderNum+"】有误";
			
			return null;
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		
		error.code = 1;
		error.msg = "查询流水号账单时信息成功!";
		
		return map;
		
		
	}
}
