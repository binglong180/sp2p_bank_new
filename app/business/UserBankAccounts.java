package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_user_bank_accounts;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.RegexUtils;
import constants.UserEvent;

/**
 * 银行账户业务实体类
 * @author lwh
 * @version 6.0
 * @created 2014年4月8日 下午4:13:53
 */
public class UserBankAccounts implements Serializable{
	private long _id;
	public long id;
	public long userId;
	public Date time;
	public String bankName;
	public int bankCode;  //支行code
	public int provinceCode;  //支行所在省code
	public int cityCode;  //支行所在市code
	public String branchBankName;  //支行名称
	public String province;  //支行所在省
	public String city;  //支行所在市
	public String account;
	public String subAccount;
	public String accountName;
//	public boolean verified;
//	public Date verifyTime;
//	public long verifySupervisorId;
	
	public UserBankAccounts(){
		
	}

	public long getId() {
		return _id;
	}
	
	/**
	 * 获得隐藏的账号
	 */
	public String getSubAccount() {
		if(StringUtils.isBlank(this.account))
			return "";
		
		int len = this.account.length();
		
		if(len < 16 || len > 19){
			return "卡号有误!";
		}
		
		return this.account.substring(0, 6) + "..." + this.account.substring(len - 4, len);
	}

	public void setId(long id) {
		t_user_bank_accounts userBankAccounts = null;

		try {
			userBankAccounts = t_user_bank_accounts.findById(id);
		} catch (Exception e) {
			this._id = -1;
			
			return;
		}

		if (null == userBankAccounts) {
			this._id = -1;

			return;
		}
		
		this._id = userBankAccounts.id;
		this.userId = userBankAccounts.user_id;
		this.time = userBankAccounts.time;
		this.bankName = userBankAccounts.bank_name;
		this.bankCode = userBankAccounts.bank_code;
		this.provinceCode = userBankAccounts.province_code;
		this.cityCode = userBankAccounts.city_code;
		this.account = userBankAccounts.account;
		this.accountName = userBankAccounts.account_name;
		this.branchBankName = userBankAccounts.branch_bank_name;
		this.province = userBankAccounts.province;
		this.city = userBankAccounts.city;
//		this.verified = userBankAccounts.verified;
//		this.verifyTime = userBankAccounts.verify_time;
//		this.verifySupervisorId = userBankAccounts.verify_supervisor_id;
	}
	
	/**
	 * 用户添加设置银行帐号
	 * @param isNeedBranchBankInfo 是否支持提现银行卡支行信息绑定，true，支持，false，不支持
	 * @param error
	 * @return 返回-1代表绑定失败，返回 1 代表绑定成功
	 */
	public int addUserBankAccount(ErrorInfo error, boolean isNeedBranchBankInfo){
		error.clear();
		
		if(!isNeedBranchBankInfo){  //不支持提现银行卡支行信息时,branchBankName,province,city字段为null
			this.branchBankName = "";
			this.province = "";
			this.city = "";
		}
		
		if(isNeedBranchBankInfo && StringUtils.isBlank(this.branchBankName)) {  //支持提现银行卡支行信息时,branchBankName必填
			error.code = -1;
			error.msg = "支行名称不能为空";
			
			return error.code;
		}
		
		if(!isNeedBranchBankInfo && StringUtils.isBlank(this.bankName)) {  //不支持提现银行卡支行信息时，bankName必填
			error.code = -1;
			error.msg = "银行名称不能为空";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.account)) {
			error.code = -1;
			error.msg = "账号不能为空";
			
			return error.code;
		}
		
		if(!RegexUtils.isBankAccount(this.account)) {
            error.code = -1;
            error.msg = "银行账号格式错误，应该是16-22位数字！";
            
            return error.code;
        }
		
		if(StringUtils.isBlank(this.accountName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		
		boolean flag=this.isReuseBank(userId,account,accountName);
		
		if(flag){
			error.msg = "该银行账户已存在，请重新输入!";
			error.code = -1;
			
			return error.code;
		}
		
		t_user_bank_accounts userBankAccounts = new t_user_bank_accounts();
		
		userBankAccounts.time=new Date();
		userBankAccounts.user_id=this.userId;
		userBankAccounts.bank_name = this.bankName.trim();
		userBankAccounts.bank_code = this.bankCode;
		userBankAccounts.province_code = this.provinceCode;
		userBankAccounts.city_code = this.cityCode;
		userBankAccounts.branch_bank_name = this.branchBankName.trim();
		userBankAccounts.province = this.province;
		userBankAccounts.city = this.city;
		userBankAccounts.account= this.account.trim();
		userBankAccounts.account_name= this.accountName.trim();
		
		try{
			userBankAccounts.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("添加银行卡时：" + e.getMessage());
			error.code = -1;
			error.msg = "银行卡添加失败!";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.ADD_BANK, "添加银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡添加成功!";
		
		return 0;
	}
	
	/**
	 * 修改用户银行帐号
	 * @param countId 帐号ID
	 * @param account
	 * @param accountName
	 */
	public int editUserBankAccount(long accountId, long userId, boolean isNeedBranchBankInfo, ErrorInfo error){
		error.clear();
		
		if(!isNeedBranchBankInfo){  //不支持提现银行卡支行信息时,branchBankName,province,city字段为null
			this.branchBankName = "";
			this.province = "";
			this.city = "";
		}
		
		if(isNeedBranchBankInfo && StringUtils.isBlank(this.branchBankName)) {  //支持提现银行卡支行信息时,branchBankName必填
			error.code = -1;
			error.msg = "支行名称不能为空";
			
			return error.code;
		}
		
		if(!isNeedBranchBankInfo && StringUtils.isBlank(this.bankName)) {  //不支持提现银行卡支行信息时，bankName必填
			error.code = -1;
			error.msg = "银行名称不能为空";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.account)) {
			error.code = -1;
			error.msg = "账号不能为空";
			
			return error.code;
		}
		
		String account = t_user_bank_accounts.find("select account from t_user_bank_accounts where id = ?", accountId).first();
		if(!this.account.equals(account)) {
			error.msg = "银行账号不能修改!";
			error.code = -1;
			
			return error.code;
		}
		
		if(!RegexUtils.isBankAccount(this.account)) {
            error.code = -1;
            error.msg = "银行账号格式错误，应该是16-22位数字！";
            
            return error.code;
        }
		
		if(StringUtils.isBlank(this.accountName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		
		if(!isRightBank(accountId, userId, error)) {
			
			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_user_bank_accounts set bank_name = ?,  bank_code=?, province=?, province_code=?, city=?, city_code=?, branch_bank_name=?, account = ?,account_name = ? where id = ?";
		
		Query query = em.createQuery(sql);
		query.setParameter(1, this.bankName.trim());
		query.setParameter(2, this.bankCode);
		query.setParameter(3, this.province);
		query.setParameter(4, this.provinceCode);
		query.setParameter(5, this.city);
		query.setParameter(6, this.cityCode);
		query.setParameter(7, this.branchBankName.trim());  //
		query.setParameter(8, this.account.trim());
		query.setParameter(9, this.accountName.trim());
		query.setParameter(10, accountId);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡编辑失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.EDIT_BANK, "编辑银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡账户编辑成功！";
		
		return 0;
	}
	
	/**
	 * 删除银行卡
	 * @param accountId
	 * @param error
	 * @return
	 */
	public static int deleteUserBankAccount(long userId, long accountId, ErrorInfo error){
		error.clear();
		
		if(accountId == 0) {
			error.code = -1;
			error.msg = "参数传入有误";
			
			return error.code;
		}
		
		if(!isRightBank(accountId, userId, error)) {
			
			return error.code;
		}
		
		EntityManager em = JPA.em();
		String sql = "delete t_user_bank_accounts where id = ?";
		
		Query query = em.createQuery(sql).setParameter(1, accountId);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("删除银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡删除失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_BANK, "删除银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡账户删除成功！";
		
		return 0;
	}
	
	
	
	/**
	 * 查询用户所有银行帐号信息
	 * @param userId
	 */
	public  static List<UserBankAccounts> queryUserAllBankAccount(long userId){
		
		List<UserBankAccounts> userBankAccountsList = new ArrayList<UserBankAccounts>();
		
		List<Long> idList = null;
		
		try{
			idList = t_user_bank_accounts.find("select id from t_user_bank_accounts where user_id = ?", userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		if(idList != null && idList.size() > 0 ){
			
			UserBankAccounts userBankAccount = null;
			for(Long ids : idList){
				long id = ids;
				userBankAccount = new UserBankAccounts();
				userBankAccount.id = id;
				
				userBankAccountsList.add(userBankAccount);
			}
		}
		
		return userBankAccountsList;
	}

	
	/**
	 * 判断是否重复使用同一银行帐号
	 * @param userId
	 * @param account
	 * @param accountName
	 * @return
	 */
	 
	public boolean isReuseBank(long userId,String account,String accountName){
		
		int count = (int) t_user_bank_accounts.count("account=?  ",account.replaceAll(" ", ""));
		
		if(count > 0){
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 绑定收款帐号
	 * @param accountId
	 * @return
	 */
	public int bindAccount(long accountId,long bidId,ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		String sql = "update t_bids set bank_account_id = ?";
		Query query = em.createQuery(sql);
		query.setParameter(1, accountId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			
			error.msg = "对不起！绑定银行账户失败！请您重试或联系平台管理员！";
			error.code = -1;
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return 1;
	}
	
	/**
	 * 判断一个银行账号是否属于一个用户
	 * @param accountId
	 * @param userId
	 * @param error
	 * @return
	 */
	public static boolean isRightBank(long accountId, long userId, ErrorInfo error) {
		UserBankAccounts account = new UserBankAccounts();
		account.id = accountId;
		
		if(account.id < 0 || account.userId != userId) {
			error.code = -1;
			error.msg = "请选择正确的银行账号";
			
			return false;
		}
		
		return true;
	}
}