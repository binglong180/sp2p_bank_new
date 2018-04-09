package controllers.newr.supervisor.projectManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import models.t_bids;
import models.t_dict_banks_col;
import models.t_user_bank_accounts;
import models.newr.t_settlement;
import models.newr.t_ticket;
import models.newr.t_users;
import net.sf.json.JSONObject;
import payment.api.util.GUIDGenerator;
import payment.newr.PaymentProxy;
import play.Logger;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.FileUtil;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;

import business.newr.Bid;
import business.newr.Bill;

import com.shove.Convert;

import constants.Constants;
import constants.newr.SettleType;
import controllers.supervisor.SupervisorController;
import cpcn.institution.tools.util.StringUtil;

/**
 * 
 * 
 * 项目管理
 */
public class ProjectAction extends SupervisorController {
	public static void setProject() {
		render();
	}
	//修改项目
	public static void toUpdateProject(long bidId) {
		boolean flag=false;
		t_bids bid = t_bids.findById(bidId);
		//获取借款人
		if(bid.user_id >0 ){
			flag=true;
			t_users user = t_users.findById(bid.user_id);
		
			//获取银行卡信息
			t_user_bank_accounts bank = t_user_bank_accounts.find("user_id=?", bid.user_id).first();
			if(bid.product_id>0){
				flag=true;
				//获取商票
				t_ticket ticket=t_ticket.findById(bid.product_id);
				render(bid,user,ticket,bank);
			}else{
				render(bid,user);
			}
		}
		
		if(!flag){
			render(bid);
		}
	}
	
	
	// 时间：字符串---->日期
	public static Date parseDate(String s) throws ParseException {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.parse(s);
	}
	//添加项目保存
	public static void saveProject() {
		        //1
				//int  id = 2;				
				//2.
				String pre_year = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 4);
				String last_project_no = null;
				//标题前面
				String titleFirst=null;
				
				String project_no = pre_year + "0001";
				try {
					//获得最新标题
		
					last_project_no=JPA.em().createNativeQuery("select max(title) from t_bids").getSingleResult().toString();
					if(last_project_no!=null||!last_project_no.equals("")){
						//标题前缀
						titleFirst=JPA.em().createNativeQuery("select max(title) from t_bids").getSingleResult().toString().substring(0, 4);
					}
				} catch (Exception e1) {
					last_project_no = "";
					e1.printStackTrace();
				}
				
				//标题与年份
				if(titleFirst!=null){
					if(pre_year.equals(titleFirst)){
						if(last_project_no!=null && last_project_no!=""){
							project_no = (Integer.parseInt(last_project_no)+1)+"";
						}else{
							project_no = pre_year + "0001";
						}
					}else{
						project_no = pre_year + "0001";
					}
				}
				
				// 贷款信息
				String  reality_name = params.get("reality_name");
				String   loanUsage = params.get("loanUsage");
				t_users user = new t_users();
				user =user.find("reality_name=?", reality_name).first();
				String amount = params.get("amount");
				String period = params.get("period");
				
				String  apr = params.get("apr");
				String invest_rate = params.get("invest_rate");
				String bid_type = params.get("type");
				String begin_interest = params.get("begin_interest");
				String start_time=params.get("start_time");
				String invest_period  =  params.get("invest_period");
				String guarantee_no = params.get("guarantee_no");
				String pact_no     = params.get("pact_no");
				String guarantee   = params.get("guarantee");
				String pact        = params.get("pact");
				String  ticket_no = params.get("ticket_no");
				t_ticket ticket = t_ticket.find("ticket_no", ticket_no).first();
				try {
					
					t_bids tbid = new t_bids();
					tbid.time =  new Date(); // 申请时间
					tbid.title = project_no; // 标题
					
					
					tbid.start_time=parseDate(start_time);//发标时间
					
					
					tbid.amount = Convert.strToDouble(amount, 0.00); // 金额  
					tbid.period_unit = Constants.DAY; // 借款期限单位
					tbid.period = Convert.strToInt(period,0); // 期限
					tbid.apr = Convert.strToDouble(apr, 0.00); // 年利率
					tbid.invest_period = Convert.strToInt(invest_period,0); // 募集期
					tbid.description = ""; // 借款描述
					tbid.status = 0;
					tbid.product_id = ticket.getId(); // 产品ID
					tbid.user_id = user.getId(); // 用户ID
					tbid.loan_purpose =loanUsage; // 借款用途
					tbid.repayment_type_id = 3; //还款类型
					tbid.max_loan = 0; // 警戒线
					tbid.min_invest_amount = 1000.0; // 最低金额招标
					tbid.has_settle_amount = 0.00; // 平均金额招标
					tbid.type   = Integer.parseInt(bid_type); 
					tbid.service_fees = 0.00; // 服务费
					tbid.invest_rate = Convert.strToDouble(invest_rate, 0.00); // 理财管理费
					tbid.overdue_rate = 0.00; // 逾期管理费					
					tbid.loan_schedule = 0; // 借款进度比例(默认为0)
					tbid.has_invested_amount = 0; // 已投总额(默认为0)
					tbid.bank_account_id = 0; // 绑定银行卡(默认为0)
					tbid.audit_time = null; // 审核时间(默认为null)
					tbid.audit_suggest = null; // 审核意见(默认为null)
					tbid.last_repay_time = null; // 最后放款时间(默认为null)	
					tbid.begin_interest = parseDate(begin_interest);
				
					tbid.repayment_time=new Date(tbid.endInterest.getTime()+5*1000*60*60*24);
					tbid.pact = pact;
					tbid.pact_no = pact_no;
					tbid.guarantee = guarantee;
					tbid.guarantee_no =guarantee_no; 
					tbid.mer_bill_no=GUIDGenerator.genGUID();
					
					tbid.invest_expire_time=DateUtil.dateAddDay(parseDate(begin_interest), Convert.strToInt(period,0));
					
					tbid.save();
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("提交用户资料时时："+e.getMessage());
					JPA.setRollbackOnly();
				}
		//保存项目信息，跳转项目列表
		getProject();
	}
	
	
		//修改项目保存
		public static void updateProject() {
			        //1
					//int  id = 2;				
					//2.
					String pre_year = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 4);
					String project_no=params.get("title");
					String id=params.get("id");
					// 贷款信息
					String  reality_name = params.get("reality_name");
					String   loanUsage = params.get("loanUsage");
					t_users user = new t_users();
					user =user.find("reality_name=?", reality_name).first();
					String amount = params.get("amount");
					String period = params.get("period");
					
					String  apr = params.get("apr");
					String invest_rate = params.get("invest_rate");
					String bid_type = params.get("type");
					String begin_interest = params.get("begin_interest");
					String start_time=params.get("start_time");
					String invest_period  =  params.get("invest_period");
					String guarantee_no = params.get("guarantee_no");
					String pact_no     = params.get("pact_no");
					String guarantee   = params.get("guarantee");
					String pact        = params.get("pact");
					String  ticket_no = params.get("ticket_no");
					t_ticket ticket = t_ticket.find("ticket_no", ticket_no).first();
					try {
						
						t_bids.delete("id ", Long.valueOf(id));
					
						t_bids tbid = new t_bids();
						tbid.time =  new Date(); // 申请时间
						
						tbid.title = project_no; // 标题
						
						
						tbid.start_time=parseDate(start_time);//发标时间
						
						
						tbid.amount = Convert.strToDouble(amount, 0.00); // 金额  
						tbid.period_unit = Constants.DAY; // 借款期限单位
						tbid.period = Convert.strToInt(period,0); // 期限
						tbid.apr = Convert.strToDouble(apr, 0.00); // 年利率
						tbid.invest_period = Convert.strToInt(invest_period,0); // 募集期
						tbid.description = ""; // 借款描述
						tbid.status = 0;
						tbid.product_id = ticket.getId(); // 产品ID
						tbid.user_id = user.getId(); // 用户ID
						tbid.loan_purpose =loanUsage; // 借款用途
						tbid.repayment_type_id = 3; //还款类型
						tbid.max_loan = 0; // 警戒线
						tbid.min_invest_amount = 1000.0; // 最低金额招标
						tbid.has_settle_amount = 0.00; // 平均金额招标
						tbid.type   = Integer.parseInt(bid_type); 
						tbid.service_fees = 0.00; // 服务费
						tbid.invest_rate = Convert.strToDouble(invest_rate, 0.00); // 理财管理费
						tbid.overdue_rate = 0.00; // 逾期管理费					
						tbid.loan_schedule = 0; // 借款进度比例(默认为0)
						tbid.has_invested_amount = 0; // 已投总额(默认为0)
						tbid.bank_account_id = 0; // 绑定银行卡(默认为0)
						tbid.audit_time = null; // 审核时间(默认为null)
						tbid.audit_suggest = null; // 审核意见(默认为null)
						tbid.last_repay_time = null; // 最后放款时间(默认为null)	
						tbid.begin_interest = parseDate(begin_interest);
						tbid.pact = pact;
						tbid.pact_no = pact_no;
						tbid.guarantee = guarantee;
						tbid.guarantee_no =guarantee_no; 
						tbid.mer_bill_no=GUIDGenerator.genGUID();
						
						tbid.save();
						
					
						
					} catch(Exception e) {
						e.printStackTrace();
						Logger.info("提交用户资料时时："+e.getMessage());
						JPA.setRollbackOnly();
					}
			//保存项目信息，跳转项目列表
			getProject();
		}
	
	/**
	 * 获取项目详情
	 */
	public static void projectDetails(Long bidId){
		boolean flag=false;
		t_bids bid = t_bids.findById(bidId);
		//获取借款人
		if(bid.user_id >0 ){
			flag=true;
			t_users user = t_users.findById(bid.user_id);
		
			//获取银行卡信息
			t_user_bank_accounts bank = t_user_bank_accounts.find("user_id=?", bid.user_id).first();
			if(bid.product_id>0){
				flag=true;
				//获取商票
				t_ticket ticket=t_ticket.findById(bid.product_id);
				render(bid,user,ticket,bank);
			}else{
				render(bid,user);
			}
		}
		
		if(!flag){
			render(bid);
		}
		
	}
	
	
	
	
	
	
	
	
	
	/**
	 * 获取所有项目信息
	 */
	public static void reviewProject() {
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			
 			currPage = Integer.parseInt(currPageStr);
 			
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 			
 		}
 		String sql = "from t_bids  order by id desc ";
		List<t_bids> list= new ArrayList<t_bids>();
		int count = 0;
		try {
			EntityManager em = JPA.em();
			Query query = em.createQuery(sql,t_bids.class);
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            for(t_bids bid:list){
            	t_users user = t_users.find("from t_users where id=?", bid.user_id).first();
               if(user!=null){
            	   bid.loanUserName = user.company; 
               }
                bid.endInterest = DateUtil.dateAddDay(bid.begin_interest, bid.period);
            }
            String countQuerySql= "select count(1) from t_bids"; 		
            Query countQuery = em.createNativeQuery(countQuerySql);
            count = Convert.strToInt(countQuery.getResultList().get(0)+"",0);;
		} catch (Exception e) {
			Logger.error("项目管理->获取项目信息： " + e.getMessage());
			return;
	    }
		PageBean<t_bids> page =new PageBean<t_bids>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.page = list;
		render(page);
	}
	/**
	 * 录入人获取项目信息
	 */
	public static void getProject() {
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			
 			currPage = Integer.parseInt(currPageStr);
 			
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 			
 		}
 		String sql = "from t_bids  order by id desc ";
		List<t_bids> list= new ArrayList<t_bids>();
		int count = 0;
		try {
			EntityManager em = JPA.em();
			Query query = em.createQuery(sql,t_bids.class);
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            for(t_bids bid:list){
            	t_users user = t_users.find("from t_users where id=?", bid.user_id).first();
               if(user!=null){
            	   bid.loanUserName = user.company; 
               }
                bid.endInterest = DateUtil.dateAddDay(bid.begin_interest, bid.period);
            }
            String countQuerySql= "select count(1) from t_bids"; 		
            Query countQuery = em.createNativeQuery(countQuerySql);
            count = Convert.strToInt(countQuery.getResultList().get(0)+"",0);;
		} catch (Exception e) {
			Logger.error("项目管理->获取项目信息： " + e.getMessage());
			return;
	    }
		PageBean<t_bids> page =new PageBean<t_bids>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.page = list;
		render(page);
	}
	

	public static void deleteProject(Long bidId){
		  t_bids.delete("id ", bidId);
		  getProject();
	}
	/**
	 * 项目审核通过
	 */
	public static void approved(long bidId){
		
          String sql = "update t_bids set status='1',audit_time=now() where id= ? ";
         
          
  		int rows = 0;
  		
  		try{
  			rows = JpaHelper.execute(sql,  bidId).executeUpdate();
  		}catch(Exception e) {
  			e.printStackTrace();
  			Logger.error("标的管理->审核： " + e.getMessage());  			
  			 			
  			return;
  		}
          
  		reviewProject();
	}
	/**
	 * 项目审核失败
	 */
	public static void projectFailure(long bidId){
		String audit_suggest = params.get("audit_suggest");
		
		String sql = "update t_bids set status='-1',audit_time=now(),audit_suggest=? where id= ? ";
		int rows = 0;
  		
  		try{
  			rows = JpaHelper.execute(sql,audit_suggest,bidId).executeUpdate();
  		}catch(Exception e) {
  			e.printStackTrace();
  			Logger.error("标的管理->审核失败： " + e.getMessage());		
  			 			
  			return;
  		}
		
  		reviewProject();
	}
	/**
	 * 上传图像/承诺函
	 */

	public static void uploadImg(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> fileInfo = FileUtil.uploadFile(imgFile, 1, error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderText(json.toString());
		}
		
		String fileExt = (String) fileInfo.get("fileName");
		String filename = fileExt.substring(0, fileExt.lastIndexOf("."));
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderText(json.toString());
		}
		
		JSONObject json = new JSONObject();
		json.put("filename", Constants.HTTP_PATH+filename);
		json.put("error", error);
		renderText(json.toString());
	}
	public static void saveProduct(t_ticket ticket ){		
		try {
			ticket.start_time = parseDate(params.get("ticket.start_time"));
			ticket.end_time= parseDate(params.get("ticket.end_time"));
		
			ticket.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getProduct();
	}
	public static void getProduct(){
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 	

 		String sql = "select * from t_ticket ";
 		
		List<t_ticket> list= new ArrayList<t_ticket>();
		int count = 0;
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(),t_ticket.class);
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            String countQuerySql= "select count(1) from t_ticket";    		
            Query countQuery = em.createNativeQuery(countQuerySql);
            count = Convert.strToInt(countQuery.getResultList().get(0)+"",0);;
		} catch (Exception e) {
			Logger.error("项目管理->获取产品信息： " + e.getMessage());
			return;
	    }
		PageBean<t_ticket> page =new PageBean<t_ticket>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.page = list;
		render(page);
	}
	public static void deleteProduct(Long id){
		String sql = "delete  from t_ticket where id =? ";
		JPAUtil.createNativeQuery(sql, id).executeUpdate();
		getProduct();
	}
	public static void setProduct() {
		render();
	}
	public static void getloanUser(){
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		} 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		String sql = "select * from t_users "; 		
		List<t_users> list= new ArrayList<t_users>();
		int count = 0;
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(),t_users.class);
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            for(t_users u : list){
            	t_user_bank_accounts  bank = t_user_bank_accounts.find("user_id=?", u.id).first();
            	u.bank =  bank;
            }
            String countQuerySql= "select count(1) from t_users";    		
            Query countQuery = em.createNativeQuery(countQuerySql);
            count = Convert.strToInt(countQuery.getResultList().get(0)+"",0);;
		} catch (Exception e) {
			Logger.error("项目管理->获取借款人信息： " + e.getMessage());
			return;
	    }
		PageBean<t_users> page =new PageBean<t_users>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.page = list;
		render(page);
	}
	public static void setLoanUser(){
		List<t_dict_banks_col> banks = t_dict_banks_col.findAll();
		render(banks);
	}
	public static void deleteLoanUser(String loanUserId){
		t_user_bank_accounts.delete("user_id=?",new Long(loanUserId));
		t_users.delete("id=? ", new Long(loanUserId));
		getloanUser();
	}
    public static void saveLoanUser(){
    	//借款用户信息
    	String company = params.get("company");
    	String company_address = params.get("company_address");
    	String office_telephone = params.get("office_telephone");
    	String fax_number = params.get("fax_number");
    	String tax_no   = params.get("tax_no");
    	String legal_person = params.get("legal_person");
    	String mobile     = params.get("mobile");
    	String business_licence_image = params.get("business_licence_image");
    	String code_certificate_image = params.get("code_certificate_image");
    	String certificate_positive_image = params.get("certificate_positive_image");
    	String cerfificate_negative_image = params.get("cerfificate_negative_image");
    	// 银行卡信息
    	String  bankID  = params.get("bankID");
    	String account_name = params.get("account_name");
    	String account  = params.get("account");
    	String branch_bank_name = params.get("branch_bank_name");
    	String province  = params.get("province");
    	String city    = params.get("city");
    	t_users user = new t_users();
    	user.company =company;
    	user.reality_name=company;
    	user.company_address = company_address;
    	user.fax_number = fax_number;
    	user.office_telephone = office_telephone;
    	user.tax_no = tax_no;
        user.legal_person =legal_person;
        user.mobile =mobile;
        user.business_licence_image =business_licence_image;
        user.code_certificate_image = code_certificate_image;
        user.certificate_positive_image = certificate_positive_image;
        user.cerfificate_negative_image = cerfificate_negative_image;
    	user.save();
    	//获取银行卡信息

		String sql = "select bank_name from t_dict_banks_col where bank_code=?  limit 1";
		Object bank_name = "";
		
		try {
			bank_name = JPA.em().createNativeQuery(sql).setParameter(1, Integer.parseInt(bankID)).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			bank_name = "";
		}
    	
    	t_user_bank_accounts bank = new t_user_bank_accounts();
    	bank.bank_code = Integer.parseInt(bankID);
    	bank.account = account.replaceAll(" ", "");
    	bank.account_name = account_name;
    	bank.branch_bank_name = branch_bank_name;
    	bank.province = province;
    	bank.city = city;
    	bank.user_id = user.id;
    	bank.bank_name=bank_name.toString();
    	bank.save();
    	getloanUser();
    }
    
	/**
	 * 满标待放款借款标列表
	 */
	public static void fullList() {
		ErrorInfo error = new ErrorInfo();
		String currPageStr = params.get("currPage"); // 当前页
		String pageSizeStr = params.get("pageSize"); // 分页行数
		int currPage = null == currPageStr ? 1 : Integer.parseInt(currPageStr);
		int pageSize = null == pageSizeStr ? 10 : Integer.parseInt(pageSizeStr);
		PageBean<t_bids> pageBean = new PageBean<t_bids>();	
		pageBean.currPage=currPage;
		pageBean.pageSize =pageSize;
		pageBean.page = Bid.queryBidAuditing(pageBean, error);
		render(pageBean);
	}
	/**
	 * 放款
	 */
	public static void fullBid(String bidId){
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isNotEmpty(bidId)){
			 Bid bid = new Bid();
			 bid.auditBid=true;
			 bid.id = Long.parseLong(bidId);
			 List<t_settlement> resultList =sendFullCommand(bid,error);
			 
			renderTemplate("newr/supervisor/projectManager/ProjectAction/querySettlement.html", resultList);		
		}
		
				
	}
	
	//发送满标放款请求
	private static List<t_settlement>  sendFullCommand(Bid bid,ErrorInfo error){
		List<t_settlement> resultList= bid.releaseBid(error);
		Bill bill=new Bill();
		bill.addBill(bid, new Date(), error);
		bill.addInvestBills(bid.id, bid.repayment.id, bid.userId, error);
		
	    return resultList;
	}
    
	public static void querySettlement(Long bid){
		List<t_settlement> resultList = t_settlement.find("order by time desc ", null).fetch();
		settlementWithUserName( resultList);
		render(resultList);
	}
	
	private static void settlementWithUserName(List<t_settlement> resultList){
		for(t_settlement settlement:resultList){
		    if(settlement.settle_type==SettleType.SETTLEUSER){		    	
		    	settlement.userName = ((t_users)t_users.findById(settlement.user_id)).reality_name;
		    }else{
		    	settlement.userName ="洪洞农村商业银行";
		    }
		}
	}
}
