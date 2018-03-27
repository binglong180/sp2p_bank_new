package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_messages;
import models.t_messages_accepted;
import models.t_messages_receivers;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import business.newr.User;

import com.shove.Convert;

import constants.Constants.MessageKeywordType;
import constants.Constants.ReadStatus;
import constants.Constants.SystemSupervisor;
import constants.Constants.UserGroupType;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;

/**
 * 站内信
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-25 下午02:27:42
 */
public class StationLetter implements Serializable{

	public long id;
	private long _id = -1;

	public long senderUserId;
	public long senderSupervisorId;

	public long receiverUserId;
	public long receiverSupervisorId;
	
	public String senderUserName;
	public String senderSupervisorName;
	
	public String receiverUserName;
	public String receiverSupervisorName;
	
	private long _senderUserId;
	private long _senderSupervisorId;

	private long _receiverUserId;
	private long _receiverSupervisorId;
	
	private String _senderUserName;
	private String _senderSupervisorName;
	
	private String _receiverUserName;
	private String _receiverSupervisorName;

	public String title;
	public String content;
	public Date time;
	
	public String replyStatus;   //回复状态(详情页面显示)
	
	
	
	public StationLetter() {
		super();
	}
	
	public StationLetter(long id) {
		super();
		this.id = id;
	}

	public void setId(long id) {
		t_messages msg = null;

		try {
			msg = t_messages.findById(id);
		} catch (Exception e) {
			this._id = -1;
			e.printStackTrace();
			Logger.error(e.getMessage());

			return;
		}

		if (null == msg) {
			this._id = -1;

			return;
		}

		setInfomation(msg);
	}

	public long getId() {
		return _id;
	}

	/**
	 * 填充数据
	 * @param msg
	 */
	private void setInfomation(t_messages msg) {
		if (null == msg) {
			this._id = -1;

			return;
		}

		_id = msg.id;
		this.senderUserId = msg.sender_user_id;
		this.senderSupervisorId = msg.sender_supervisor_id;
		this.receiverUserId = msg.receiver_user_id;
		this.receiverSupervisorId = msg.receiver_supervisor_id;

		this.content = msg.content;
		this.title = msg.title;
		this.time = msg.time;
	}
	
	public long getSenderUserId() {
		return _senderUserId;
	}

	public void setSenderUserId(long senderUserId) {
		this._senderUserId = senderUserId;
		this._senderUserName = User.queryUserNameById(senderUserId, new ErrorInfo());
	}

	public long getSenderSupervisorId() {
		return _senderSupervisorId;
	}

	public void setSenderSupervisorId(long senderSupervisorId) {
		this._senderSupervisorId = senderSupervisorId;
		this._senderSupervisorName = Supervisor.queryNameById(senderSupervisorId, new ErrorInfo());
	}

	public long getReceiverUserId() {
		return _receiverUserId;
	}

	public void setReceiverUserId(long receiverUserId) {
		this._receiverUserId = receiverUserId;
		
		if (receiverUserId < 0) {
			return;
		}		
		this._receiverUserName = User.queryUserNameById(receiverUserId, new ErrorInfo());
	}

	public long getReceiverSupervisorId() {
		return _receiverSupervisorId;
	}

	public void setReceiverSupervisorId(long receiverSupervisorId) {
		this._receiverSupervisorId = receiverSupervisorId;
		this._receiverSupervisorName = Supervisor.queryNameById(receiverSupervisorId, new ErrorInfo());
	}

	public String getSenderUserName() {
		return _senderUserName;
	}

	public void setSenderUserName(String senderUserName) {
		this._senderUserName = senderUserName;
		this._senderUserId = User.queryIdByUserName(senderUserName, new ErrorInfo());
	}

	public String getSenderSupervisorName() {
		return _senderSupervisorName;
	}

	public void setSenderSupervisorName(String senderSupervisorName) {
		this._senderSupervisorName = senderSupervisorName;
		this._senderSupervisorId = Supervisor.queryIdByName(senderSupervisorName, new ErrorInfo());
	}



	public void setReceiverUserName(String receiverUserName) {
		this._receiverUserName = receiverUserName;
		this._receiverUserId = User.queryIdByUserName(receiverUserName, new ErrorInfo());
	}

	public String getReceiverSupervisorName() {
		return _receiverSupervisorName;
	}

	public void setReceiverSupervisorName(String receiverSupervisorName) {
		this._receiverSupervisorName = receiverSupervisorName;
		this._receiverSupervisorId = Supervisor.queryIdByName(receiverSupervisorName, new ErrorInfo());
	}
	
	

	/**
	 * 用户给用户发送站内信
	 * @param error
	 * @return
	 */
	public int sendToUserByUser(ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId < 1) {
			error.code = -1;
			error.msg = "收件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId == this.senderUserId) {
			error.code = -1;
			error.msg = "不能给自已发送站内信";

			return error.code;
		}
		
		if(StringUtils.isBlank(this.title)) {
			error.code = -1;
			error.msg = "站内信标题不能为空";

			return error.code;
		}
		
		if(this.title.length()>40) { 
			error.code = -1;
			error.msg = "站内信标题过长";

			return error.code;
		}
		if (StringUtils.isBlank(this.content)) {
			error.code = -1;
			error.msg = "内容不能为空";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_user_id = this.receiverUserId;
		msg.title = this.title;
		msg.content = this.content;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(this.senderUserId, UserEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 用户给系统管理员发送站内信
	 * @param error
	 * @return
	 */
	public int sendToSupervisorByUser(ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		if(StringUtils.isBlank(this.title)) {
			error.code = -1;
			error.msg = "标题不能为空";

			return error.code;
		}
		
		if(this.title.length()>40) {
			error.code = -1;
			error.msg = "站内信标题过长";

			return error.code;
		}
		
		if(StringUtils.isBlank(this.content)){
			error.code = -1;
			error.msg = "给管理员发站内信内容不能为空";

			return error.code;
			
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_supervisor_id = SystemSupervisor.ID;
		msg.title = this.title;
		msg.content = this.content;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(User.currUser().id, UserEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员给用户发送站内信
	 * @param error
	 * @return
	 */
	public int sendToUserBySupervisor(ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId < 1) {
			error.code = -1;
			error.msg = "收件人不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = this.receiverUserId;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员给多个用户发送站内信
	 * @param receiverNames:逗号分隔的用户名
	 * @param error
	 * @return
	 */
	public int sendToUsersBySupervisor(String receiverNames, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (StringUtils.isBlank(receiverNames)) {
			error.code = -1;
			error.msg = "收件人不能为空";
			
			return error.code;
		}
		
		receiverNames = receiverNames.replaceAll("\\s", "");
		String[] arrNames = receiverNames.split(",");
		
		if (arrNames.length == 1) {
			this.receiverUserName = arrNames[0];
			
			return sendToUserBySupervisor(error);
		}
		
		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = UserGroupType.CUSTOM_USERS;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		int totalCount = arrNames.length;
		int successCount = 0;
		long msgId = msg.id;

		for (int i=0;i<totalCount;i++) {
			long userId = User.queryIdByUserName(arrNames[i], error);
			
			if (userId < 1) {
				error.clear();
				continue;
			}
			
			t_messages_receivers mr = new t_messages_receivers();
			mr.message_id = msgId;
			mr.user_id = userId;

			try {
				mr.save();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();

				return error.code;
			}
			
			successCount++;
		}
		
		if (successCount < totalCount) {
			error.code = -1;
			error.msg = "发送成功" + successCount + "条;" + "发送失败" + (totalCount - successCount) + "条";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.GROUP_SEND_MSG, "群发站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员发送快捷站内信
	 * @param usertype
	 * @param error
	 * @return
	 */
	public int sendToUserGroupBySupervisor(long usertype, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (usertype > -1) {
			error.code = -1;
			error.msg = "接收的用户组不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = usertype;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.QUICKLY_SEND_MSG, "发送快捷站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 用户给用户回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToUserByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_user_id = oldMsg.sender_user_id;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(User.currUser().id, UserEvent.REPLY_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}

	/**
	 * 用户给管理员回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToSupervisorByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}

		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_supervisor_id = SystemSupervisor.ID;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(User.currUser().id, UserEvent.REPLY_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}
	
	/**
	 * 用户给某人(用户/管理员)回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToSomebodyByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}
		
		if (oldMsg.sender_user_id > 0) {
			return replyToUserByUser(msgId, error);
		}
		
		if (oldMsg.sender_supervisor_id > 0) {
			return replyToSupervisorByUser(msgId, error);
		}
		
		error.code = 0;
		
		return 0;
	}

	/**
	 * 管理员给用户回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToUserBySupervisor(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}

		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = oldMsg.sender_user_id;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.QUICKLY_SEND_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}

	/**
	 * 用户删除收件箱站内信
	 * @param userId
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteInboxMsgByUser(long userId, long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			tma = new t_messages_accepted();
			tma.user_id = userId;
			tma.time = new Date();
			tma.message_id = msgId;
		}

		tma.is_erased = deleteType;
		tma.delete_time = new Date();

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_INBOX_MSG, "删除收件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 用户删除发件箱站内信
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteOutboxMsgByUser(long userId, long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		msg.is_erased = deleteType;
		msg.delete_time = new Date();

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_OUTBOX_MSG, "删除发件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员删除收件箱站内信
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteInboxMsgBySupervisor(long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id > 0 and message_id = ?", msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			tma = new t_messages_accepted();
			tma.supervisor_id = SystemSupervisor.ID;
			tma.time = new Date();
			tma.message_id = msgId;
		}

		tma.is_erased = deleteType;
		tma.delete_time = new Date();

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_INBOX_MSG, "删除收件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员删除发件箱信息
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteOutboxMsgBySupervisor(long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		msg.is_erased = deleteType;
		msg.delete_time = new Date();

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_OUTBOX_MSG, "删除发件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 用户标记站内信已读
	 * @param userId
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markUserMsgReaded(long userId, long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null != tma) {
			error.code = 0;
			error.msg = "消息已标记为已读";

			return error.code;
		}

		tma = new t_messages_accepted();
		tma.user_id = userId;
		tma.time = new Date();
		tma.message_id = msgId;

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.MARK_MSG_READED, "标记站内信为已读", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 用户标记站内信未读
	 * @param userId
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markUserMsgUnread(long userId, long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			error.code = 0;
			error.msg = "消息已标记为未读";

			return error.code;
		}

		try {
			tma.delete();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.MARK_MSG_UNREAD, "标记站内信为未读", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员标记站内信已读
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markSupervisorMsgReaded(long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id = ? and message_id = ?",
					SystemSupervisor.ID, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null != tma) {
			error.code = 0;
			error.msg = "消息已标记为已读";

			return error.code;
		}

		tma = new t_messages_accepted();
		tma.supervisor_id = SystemSupervisor.ID;
		tma.time = new Date();
		tma.message_id = msgId;

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员标记站内信未读
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markSupervisorMsgUnread(long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id = ? and message_id = ?",
					SystemSupervisor.ID, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			error.code = 0;
			error.msg = "消息已标记为未读";

			return error.code;
		}

		try {
			tma.delete();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}



	
	
	/**
	 * 查询用户未读系统消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadSystemMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}

	/**
	 * 查询用户收件箱未读消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadInboxMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return Convert.strToInt(obj+"", 0);
	}

	/**
	 * 查询用户未读消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))) union select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return Convert.strToInt(obj+"", 0);
	}
	
	
	/**
	 * 查询回复的站内信(已回复站内信详情)
	 * @param id
	 * @param error
	 * @return
	 */
	public static StationLetter queryReplyMessage(long id, ErrorInfo error) {
		Long msgId = null;
		
		try {
			msgId = t_messages.find("select id from t_messages where is_reply = 1 and message_id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		if (null == msgId) {
			error.code = -2;
			error.msg = "没有找到回复站内信";
			
			return null;
		}
		
		error.code = 0;
		
		return new StationLetter(msgId);
	}
	
	/**
	 * 详情
	 * @param id
	 * @return
	 */
	public static StationLetter detail(long id) {
		return new StationLetter(id);
	}
	
	/**
	 * 管理员待回复站内信个数
	 * @param error
	 * @return
	 */
	public static int queryWaitReplyMessageCount(ErrorInfo error){
		error.clear();
		
		Object result = null;
		
		String sql = "select count(1) from v_messages_supervisor_inbox where ISNULL(reply_time)";

		Query query = JPA.em().createNativeQuery(sql);
		
		try{
			result = query.getSingleResult();
		}catch(Exception e){
			Logger.error("待回复站内信个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询待回复站内信个数";
			
			return 0;
		}
		
		return result==null?0:Integer.parseInt(result.toString());
	}
}
