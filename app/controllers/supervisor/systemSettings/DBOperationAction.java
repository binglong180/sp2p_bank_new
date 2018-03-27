package controllers.supervisor.systemSettings;

import utils.ErrorInfo;
import utils.PageBean;
import business.DBOperation;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 数据库备份与还原
 * @author lzp
 * @version 6.0
 * @created 2014-7-22
 */
public class DBOperationAction extends SupervisorController {

	
	/**
	 * 清空数据
	 */
	public static void clearData() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.clearData(error);
		
		renderJSON(error);
	}
	
	/**
	 * 重置(还原出厂设置)
	 */
	public static void reset() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.reset(error);
		
		renderJSON(error);
	}
	
	/**
	 * 从操作记录还原
	 * @param operationId
	 */
	public static void recoverFromOperation(int operationId) {
		ErrorInfo error = new ErrorInfo();
		DBOperation.recoverFromOperation(operationId, error);
		
		renderJSON(error);
	}
	
	/**
	 * 备份
	 */
	public static void backup() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.backup(true, error);
		
		renderJSON(error);
	}
}
