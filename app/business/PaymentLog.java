package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_mmm_data;
import models.t_return_data;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shove.Convert;

import constants.Constants;
import constants.SQLTempletes;

public class PaymentLog implements Serializable{

	public static final Gson gson = new Gson();	
	/**
	 * 查找回调参数信息
	 * @param orderNum
	 */
	public static String lookRarkInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_return_data data = null;
		long id = Convert.strToLong(orderNum, -1);
		try {
			data = t_return_data.find(" id = ? ", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return data.data;
	}
	
	/**
	 * 查找发送参数信息
	 * @param orderNum
	 */
	public static Map<String, String> lookRarkSendInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ? ", orderNum).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		map.put("url", data.url);
		return map;
	}
	
	
	
	
	/**
	 * 根据流水号查找异步回调地址
	 * @param orderNum
	 */
	public static String lookForReturnUrl(String id, ErrorInfo error){
		error.clear();
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ?", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		
		Map<String, String> map = new HashMap<String, String>();
		return data.url;
	}
	
	
	/**
	 * 根据流水号查询回调参数
	 */
	public static Map<String,String> getReturnData(long id,ErrorInfo error){
		error.clear();
		t_return_data data = null;
		
		try {
			data = t_return_data.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return map;
	}
	
	
}
