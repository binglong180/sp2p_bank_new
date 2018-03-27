package constants;

/**
 * 数据库S	QL模板
 * @author Administrator
 *
 */
public class SQLTempletes {

	/**
	 * 分页查询前缀
	 */
	public static final String PAGE_SELECT = "select ";
	
	/**
	 * 标别名
	 */
	public static final String TABLE_NAME = " t";
	
	/**
	 * 常规查询前缀
	 */
	public static final String SELECT = "select ";

	/**
	 * 用户贷款账单
	 */
	public static final String V_USER_LOAN_INFO_BILL = "`t_users`.`id` AS `id`,`supervisor`.`id` AS `supervisor_id`,'1' AS `type`,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,((`t_users`.`balance` + `t_users`.`freeze`) + `t_users`.`balance2`) AS `user_amount`,`t_users`.`last_login_time` AS `last_login_time`,(SELECT t_credit_levels.image_filename AS credit_level_image_filename FROM t_credit_levels WHERE (t_credit_levels.id = t_users.credit_level_id)) AS credit_level_image_filename,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.status NOT in(20, 21, 22))) AS `bid_count`,(select ifnull(sum(`t_bids`.`amount`),0) AS `bid_amount` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` in (4,5)))) AS `bid_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_amount`,(select count(0) AS `bid_loaning_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 2))) AS `bid_loaning_count`,(select count(0) AS `bid_repaymenting_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 4))) AS `bid_repaymenting_count`,(select count(0) AS `overdue_bill_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`overdue_mark` <> 0))) AS `overdue_bill_count`,(select count(distinct `t_bids`.`id`) AS `bad_bid_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`overdue_mark` = -(3)))) AS `bad_bid_count` from (`t_supervisors` `supervisor` join `t_users` on((`supervisor`.`id` = `t_users`.`assigned_to_supervisor_id`))) where (`t_users`.`master_identity` in (1,3)) union select distinct `t_users`.`id` AS `id`,`supervisor`.`id` AS `supervisor_id`,'2' AS `type`,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,(`t_users`.`balance` + `t_users`.`freeze`) AS `user_amount`,`t_users`.`last_login_time` AS `last_login_time`,(SELECT t_credit_levels.image_filename AS credit_level_image_filename FROM t_credit_levels WHERE (t_credit_levels.id = t_users.credit_level_id)) AS credit_level_image_filename,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.status NOT in(20, 21, 22))) AS `bid_count`,(select ifnull(sum(`t_bids`.`amount`),0) AS `bid_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` in (4,5)))) AS `bid_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_amount`,(select count(0) AS `bid_loaning_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 2))) AS `bid_loaning_count`,(select count(0) AS `bid_repaymenting_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 4))) AS `bid_repaymenting_count`,(select count(0) AS `overdue_bill_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`overdue_mark` <> 0))) AS `overdue_bill_count`,(select count(distinct `t_bids`.`id`) AS `bad_bid_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`overdue_mark` = -(3)))) AS `bad_bid_count` from ((`t_supervisors` `supervisor` join `t_bids` `bid` on((`supervisor`.`id` = `bid`.`manage_supervisor_id`))) join `t_users` on((`bid`.`user_id` = `t_users`.`id`))) where `t_users`.`master_identity` in (1,3) ";
	

	/**
	 * 用户贷款信息
	 */
	public static final String V_USER_LOAN_INFO = "`t_users`.`id` AS `id`,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,`t_users`.`credit_score` AS `credit_score`,`t_users`.`email` AS `email`,`t_users`.`mobile` AS `mobile`,`t_users`.`mobile1` AS `mobile1`,`t_users`.`mobile2` AS `mobile2`,`t_users`.`is_allow_login` AS `is_allow_login`,t_credit_levels.image_filename AS credit_level_image_filename,((`t_users`.`balance` + `t_users`.`balance2`)) AS `user_amount`,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id`) AND t_bids.`status` IN (4, 5, 14)) AS `bid_count`,(select ifnull(sum(`t_bids`.`amount`),0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id`) AND t_bids.`status` IN (4, 5, 14)) AS `bid_amount`,(select ifnull(round(avg(`t_bids`.`apr`),2),0) AS `avg_apr` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id`) AND t_bids.`status` IN (4, 5, 14)) AS `avg_apr`,(select count(0) AS `bid_loaning_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 2))) AS `bid_loaning_count`,(select count(0) AS `bid_repayment_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` = 4))) AS `bid_repayment_count`,(select count(0) AS `overdue_bill_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`status` < 0) AND t_bills.`overdue_mark` <> 0)) AS `overdue_bill_count`,(select count(distinct `t_bids`.`id`) AS `bad_bid_count` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`overdue_mark` = -(3)))) AS `bad_bid_count`,(select count(0) AS `audit_item_count` from `t_user_audit_items` where ((`t_users`.`id` = `t_user_audit_items`.`user_id`) and (`t_user_audit_items`.`status` in (2, 3, -1)))) AS `audit_item_count`,(select ifnull(sum(((`t_bills`.`repayment_corpus` + `t_bills`.`repayment_interest`) + `t_bills`.`overdue_fine`)),0) AS `repayment_amount` from (`t_bills` join `t_bids` on((`t_bills`.`bid_id` = `t_bids`.`id`))) where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`status` in (-(1),-(2))))) AS `repayment_amount`,(select `t_supervisors`.`name` AS `supervisor_name` from `t_supervisors` where (`t_supervisors`.`id` = `t_users`.`assigned_to_supervisor_id`)) AS `supervisor_name` from `t_users` LEFT JOIN t_credit_levels ON t_users.credit_level_id = t_credit_levels.id where `t_users`.`master_identity` in (1,3) ";
	
	/**
	 * 用户投资信息
	 */
	public static final String V_USER_INVEST_INFO = "`t_users`.`id` AS `id`,t_credit_levels.image_filename AS credit_level_image_filename,t_credit_levels.order_sort AS order_sort,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,`t_users`.`credit_score` AS `credit_score`,`t_users`.`email` AS `email`,`t_users`.`mobile` AS `mobile`,`t_users`.`mobile1` AS `mobile1`,`t_users`.`mobile2` AS `mobile2`,`t_users`.`is_allow_login` AS `is_allow_login`,`t_users`.`master_identity` AS `master_identity`,((`t_users`.`balance` + `t_users`.`balance2`)) AS `user_amount`,(select ifnull(sum(`t_user_details`.`amount`),0) AS `recharge_amount` from `t_user_details` where ((`t_user_details`.`user_id` = `t_users`.`id`) and (`t_user_details`.`operation` in (1,2,3)))) AS `recharge_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests`,t_bids where ((`t_invests`.`user_id` = `t_users`.`id` and t_invests.bid_id = t_bids.id) and (t_bids.`status` IN (4, 5, 14)))) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (4, 5, 14))) AS `invest_amount`,(select count(0) AS `invest_receive_count` from `t_bids` `bid` where ((`bid`.`status` = 4) and `bid`.`id` in (select `inv`.`bid_id` from `t_invests` `inv` where ((`inv`.`user_id` = `t_users`.`id`) and (`inv`.`transfer_status` <> -(1)))))) AS `invest_receive_count`,(select ifnull(sum(((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`) + `t_bill_invests`.`overdue_fine`)),0) AS `receive_amount` from `t_bill_invests` where ((`t_bill_invests`.`user_id` = `t_users`.`id`) and (`t_bill_invests`.`status` in (-(1) ,-(2) ,-(5) ,-(6))))) AS `receive_amount`,(select count(DISTINCT t_invests.bid_id) AS `transfer_count` from `t_invests`,t_invest_transfers where ((`t_invests`.`user_id` = `t_users`.`id` AND t_invest_transfers.invest_id = t_invests.id) and (`t_invests`.`transfer_status` = 1 AND t_invest_transfers.`status` IN (1, 2, 4)))) AS `transfer_count` from `t_users` LEFT JOIN t_credit_levels ON t_users.credit_level_id = t_credit_levels.id where `t_users`.`master_identity` in (2,3) ";
	
	/**
	 * 用户投资金额
	 */
	public static final String V_USER_INVEST_AMOUNT = "`t_users`.`id` AS `id`,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id`)) AS `bid_count`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests` where ((`t_invests`.`user_id` = `t_users`.`id`) and (`t_invests`.`transfers_id` = 0))) AS `invest_count`,(select count(0) AS `count(*)` from `t_invests` where ((`t_invests`.`user_id` = `t_users`.`id`) and (`t_invests`.`transfer_status` = -(1)))) AS `transfer_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_amount`,(select ifnull(sum((`t_bill_invests`.`real_receive_interest` + `t_bill_invests`.`overdue_fine`)),0) AS `invest_interest` from `t_bill_invests` where (`t_bill_invests`.`user_id` = `t_users`.`id`)) AS `invest_interest` from `t_users` where 1=1 ";
	
	/**
	 * 用户信息
	 */
	public static final String V_USER_INFO = "`t_users`.`id` AS `id`,t_credit_levels.image_filename AS credit_level_image_filename,t_credit_levels.order_sort AS order_sort,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,`t_users`.`credit_score` AS `score`,`t_users`.`email` AS `email`,`t_users`.`mobile` AS `mobile`,`t_users`.`mobile1` AS `mobile1`,`t_users`.`mobile2` AS `mobile2`,`t_users`.`is_email_verified` AS `is_activation`,`t_users`.`is_add_base_info` AS `is_add_base_info`,`t_users`.`is_blacklist` AS `is_blacklist`,((`t_users`.`balance` + `t_users`.`balance2`)) AS `user_amount`,`t_users`.`last_login_time` AS `last_login_time`,`t_users`.`is_allow_login` AS `is_allow_login`,(select ifnull(sum(`t_user_details`.`amount`),0) AS `recharge_amount` from (`t_user_details` join `t_user_detail_types` on((`t_user_details`.`operation` = `t_user_detail_types`.`id`))) where ((`t_user_details`.`user_id` = `t_users`.`id`) and (`t_user_detail_types`.`id` in (1,2,3)))) AS `recharge_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (?, ?, ?))) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (?, ?, ?))) AS `invest_amount`,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.`status` IN (?, ?, ?))) AS `bid_count`,(select ifnull(sum(`t_bids`.`amount`),0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.`status` IN (?, ?, ?))) AS `bid_amount`,(select count(0) AS `audit_item_count` from `t_user_audit_items` where ((`t_users`.`id` = `t_user_audit_items`.`user_id`) and (`t_user_audit_items`.`status` = 2))) AS `audit_item_count` from `t_users` LEFT JOIN t_credit_levels ON t_users.credit_level_id = t_credit_levels.id where 1=1 ";
	
	/**
	 * 用户个人信息
	 */
	public static final String V_USER_FOR_PERSONAL = "`t_users`.`id` AS `id`,`t_users`.`name` AS `name`,`t_users`.`time` AS `time`,`t_users`.`photo` AS `photo`,`t_users`.`last_login_time` AS `last_login_time`,(select `city`.`name` AS `name` from `t_dict_ad_citys` `city` where (`city`.`id` = `t_users`.`city_id`)) AS `cityName`,(select `province`.`name` AS `name` from (`t_dict_ad_citys` `city` left join `t_dict_ad_provinces` `province` on((`city`.`province_id` = `province`.`id`))) where (`city`.`id` = `t_users`.`city_id`)) AS `provinceName`,(select `vip`.`expiry_date` AS `expiry_date` from `t_user_vip_records` `vip` where ((`vip`.`user_id` = `t_users`.`id`) and (`vip`.`status` = 1))) AS `expiry_date`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests` where (`t_invests`.`user_id` = `t_users`.`id`)) AS `invest_count`,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id`)) AS `bid_count` from `t_users` where 1=1 ";
	
	/**
	 *  用户消息
	 */
	public static final String V_USER_FOR_MESSAGE = "`attention`.`id` AS `id`,`attention`.`user_id` AS `user_id`,`user`.`name` AS `name`,`user`.`photo` AS `photo`,`attention`.`note_name` AS `note_name` from `t_user_attention_users` `attention` join `t_users` `user` on `attention`.`attention_user_id` = `user`.`id` where 1=1 ";
	
	/**
	 * 用户详情
	 */
	public static final String V_USER_FOR_DETAILS = "`t_users`.`id` AS `id`,`t_users`.`balance` AS `user_amount`,`t_users`.`balance2` AS `user_amount2`,`t_users`.`freeze` AS `freeze`,`t_users`.`credit_line` AS `credit_line`,(select ifnull(sum(((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`) + `t_bill_invests`.`overdue_fine`)),0) AS `receive_amount` from `t_bill_invests` where ((`t_bill_invests`.`user_id` = `t_users`.`id`) and (`t_bill_invests`.`status` in (-(1),-(2),-(5),-(6))))) AS `receive_amount` from `t_users` where 1=1 ";
	
	/**
	 * 用户明细资料
	 */
	public static final String V_USER_DETAILS = "`t_user_details`.`id` AS `id`,`t_users`.`id` AS `user_id`,`t_user_details`.`time` AS `time`,`t_user_details`.`operation` AS `operation`,`t_user_details`.`amount` AS `amount`,sum((`t_user_details`.`balance` + `t_user_details`.`freeze`)) AS `user_balance`,`t_user_details`.`balance` AS `balance`,`t_user_details`.`freeze` AS `freeze`,ifnull(`t_user_details`.`recieve_amount`,0) AS `recieve_amount`,`t_user_details`.`summary` AS `summary`,`t_user_detail_types`.`name` AS `name`,ifnull(`t_user_detail_types`.`type`,0) AS `type` from ((`t_users` join `t_user_details` on((`t_user_details`.`user_id` = `t_users`.`id`))) left join `t_user_detail_types` on((`t_user_details`.`operation` = `t_user_detail_types`.`id`))) where 1 = 1 ";
	

	/**
	 * 用户银行列表
	 */
	public static final String V_USER_BLACKLIST = "`t_user_blacklist`.`id` AS `id`,`t_user_blacklist`.`user_id` AS `user_id`,`t_user_blacklist`.`time` AS `time`,`t_user_blacklist`.`bid_id` AS `bid_id`,`t_user_blacklist`.`black_user_id` AS `black_user_id`,`t_user_blacklist`.`reason` AS `reason`,`t_users`.`name` AS `blacklist_name` from `t_user_blacklist` join `t_users` on`t_user_blacklist`.`black_user_id` = `t_users`.`id` where 1=1 ";
	
	/**
	 * 关注的用户信息
	 */
	public static final String V_USER_ATTENTION_INFO = "`t_user_attention_users`.`id` AS `id`,`t_user_attention_users`.`user_id` AS `user_id`,`t_user_attention_users`.`time` AS `time`,`t_user_attention_users`.`attention_user_id` AS `attention_user_id`,`t_user_attention_users`.`note_name` AS `note_name`,`t_users`.`name` AS `attention_user_name`,`t_users`.`photo` AS `attention_user_photo` from `t_user_attention_users` join `t_users` on  `t_user_attention_users`.`attention_user_id` = `t_users`.`id` where 1=1 ";
	
	/**
	 * 用户账户统计
	 */
	public static final String V_USER_ACCOUNT_STATISTICS = "`a`.`id` AS `id`,(select count(`b`.`id`) AS `auditing_count` from `t_bids` `b` where ((`b`.`user_id` = `a`.`id`) and (`b`.`status` = 0))) AS `auditing_count`,(select count(`b`.`id`) AS `repaymenting_count` from `t_bids` `b` where ((`b`.`user_id` = `a`.`id`) and (`b`.`status` = 4))) AS `repaymenting_count`,(select count(`b`.`id`) AS `count(``b``.``id``)` from (`t_bills` `b` join `t_bids` `d` on((`b`.`bid_id` = `d`.`id`))) where ((`b`.`repayment_time` between now() and (now() + interval 30 day)) and (`a`.`id` = `d`.`user_id`) and (`b`.`status` = -(1)))) AS `untreated_bills_count`,(select count(`b`.`id`) AS `COUNT(b.id)` from `t_bill_invests` `b` where ((`b`.`receive_time` between now() and (now() + interval 30 day)) and (`b`.`user_id` = `a`.`id`))) AS `untreated_invest_bills_count`,(select count(`b`.`id`) AS `count(b.id)` from (`t_bills` `b` join `t_bids` `c`) where ((`b`.`overdue_mark` in (-(1),-(2),-(3))) and (`b`.`bid_id` = `c`.`id`) and (`c`.`user_id` = `a`.`id`))) AS `overdue_bills_count`,(select count(`b`.`id`) AS `COUNT(b.id)` from (`t_bids` `b` join `t_invests` `c`) where ((`b`.`id` = `c`.`bid_id`) and (`a`.`id` = `c`.`user_id`) and (`c`.`transfer_status` in (0,1)) and (`b`.`status` = 4))) AS `receivable_invest_bids_count`,(select count(`b`.`id`) AS `COUNT(b.id)` from `t_user_audit_items` `b` where ((`b`.`user_id` = `a`.`id`) and (`b`.`status` = 0))) AS `lack_audit_item_count` from `t_users` `a` where 1=1 ";
	
	/**
	 * 用户账户
	 */
	public static final String V_USER_ACCOUNT_INFO = "`t_users`.`id` AS `id`,(`t_users`.`balance` + `t_users`.`freeze`) AS `user_account`,`t_users`.`balance2` AS `user_account2`,`t_users`.`freeze` AS `freeze`,ifnull((select sum(`t_invests`.`amount`) AS `sum(t_invests.amount)` from `t_invests` where ((`t_invests`.`user_id` = `t_users`.`id`) and (`t_invests`.`transfer_status` <> -(1)))),0) AS `invest_amount`,ifnull((select count(distinct `t_invests`.`bid_id`) AS `count(DISTINCT ``t_invests``.bid_id)` from `t_invests` where ((`t_invests`.`user_id` = `t_users`.`id`) and (`t_invests`.`transfer_status` <> -(1)))),0) AS `invest_count`,ifnull((select sum(`t_bids`.`amount`) AS `bid_amount` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` in (4,5)))),0) AS `bid_amount`,ifnull((select count(`t_bids`.`id`) AS `bid_count` from `t_bids` where ((`t_bids`.`user_id` = `t_users`.`id`) and (`t_bids`.`status` >= 0))),0) AS `bid_count`,ifnull((select sum((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`)) AS `receive_amount` from `t_bill_invests` where ((`t_bill_invests`.`user_id` = `t_users`.`id`) and (`t_bill_invests`.`status` in (-(1),-(2))))),0) AS `receive_amount`,ifnull((select sum((`t_bills`.`repayment_corpus` + `t_bills`.`repayment_interest`)) AS `repayment_amount` from (`t_bills` join `t_bids`) where ((`t_bills`.`bid_id` = `t_bids`.`id`) and (`t_bids`.`user_id` = `t_users`.`id`) and (`t_bills`.`status` in (-(1),-(2))))),0) AS `repayment_amount` from `t_users` where 1=1 ";
	
	/**
	 * 管理员
	 */
	public static final String V_SUPERVISORS = "`t_supervisors`.`id` AS `id`,`t_supervisors`.`time` AS `time`,`t_supervisors`.`name` AS `name`,`t_supervisors`.`reality_name` AS `reality_name`,`t_supervisors`.`password` AS `password`,`t_supervisors`.`password_continuous_errors` AS `password_continuous_errors`,`t_supervisors`.`is_password_error_locked` AS `is_password_error_locked`,`t_supervisors`.`password_error_locked_time` AS `password_error_locked_time`,`t_supervisors`.`is_allow_login` AS `is_allow_login`,`t_supervisors`.`login_count` AS `login_count`,`t_supervisors`.`last_login_time` AS `last_login_time`,`t_supervisors`.`last_login_ip` AS `last_login_ip`,`t_supervisors`.`last_logout_time` AS `last_logout_time`,`t_supervisors`.`email` AS `email`,`t_supervisors`.`telephone` AS `telephone`,`t_supervisors`.`mobile1` AS `mobile1`,`t_supervisors`.`mobile2` AS `mobile2`,`t_supervisors`.`office_telephone` AS `office_telephone`,`t_supervisors`.`fax_number` AS `fax_number`,`t_supervisors`.`sex` AS `sex`,`t_supervisors`.`birthday` AS `birthday`,`t_supervisors`.`level` AS `level`,`t_supervisors`.`is_erased` AS `is_erased`,`t_supervisors`.`creater_id` AS `creater_id`,`t_supervisors`.`ukey` AS `ukey`,`t_supervisors`.`is_customer` AS `is_customer`,`t_supervisors`.`customer_num` AS `customer_num`,(select cast(group_concat(`rg`.`name` separator ',') as char charset utf8) AS `cast(group_concat(``name``) as char)` from (`t_right_groups` `rg` left join `t_right_groups_of_supervisor` `rgos` on((`rg`.`id` = `rgos`.`group_id`))) where (`rgos`.`supervisor_id` = `t_supervisors`.`id`)) AS `right_group` from `t_supervisors` where (isnull(`t_supervisors`.`is_erased`) or (`t_supervisors`.`is_erased` = 0)) ";
	
	/**
	 * 管理员事件
	 */
	public static final String  V_SUPERVISOR_EVENTS = "`t_supervisor_events`.`id` AS `id`,`t_supervisor_events`.`supervisor_id` AS `supervisor_id`,`t_supervisor_events`.`time` AS `time`,`t_supervisor_events`.`ip` AS `ip`,`t_supervisor_events`.`type_id` AS `type_id`,`t_supervisor_events`.`descrption` AS `descrption`,concat(`t_supervisors`.`reality_name`,`t_supervisor_events`.`descrption`) AS `content`,`t_supervisors`.`name` AS `supervisor_name`,`t_supervisors`.`level` AS `supervisor_level`,`t_supervisor_event_types`.`name` AS `type_name`,`t_supervisor_event_types`.`description` AS `type_description`,`t_supervisors`.`ukey` AS `ukey` from ((`t_supervisor_events` left join `t_supervisor_event_types` on((`t_supervisor_events`.`type_id` = `t_supervisor_event_types`.`id`))) left join `t_supervisors` on((`t_supervisor_events`.`supervisor_id` = `t_supervisors`.`id`))) where 1=1 ";
	
	/**
	 * 权限组
	 */
	public static final String V_RIGHT_GROUPS = "`rg`.`id` AS `id`,`rg`.`name` AS `name`,`rg`.`description` AS `description`,`rg`.`right_modules` AS `right_modules`,(select count(`gos`.`id`) AS `count(gos.id)` from `t_right_groups_of_supervisor` `gos` , t_supervisors s where (`gos`.`group_id` = `rg`.`id` and s.id = gos.supervisor_id and s.is_erased <> 1)) AS `supervisor_count` from `t_right_groups` `rg` where id <> 1  ";
	
	/**
	 * 收款中的理财标的
	 */
	public static final String V_RECEIVING_INVEST_BIDS = "`t_invests`.`id` AS `id`,`t_invests`.`user_id` AS `user_id`,`t_invests`.`bid_id` AS `bid_id`,`t_invests`.`transfer_status` AS `transfer_status`,`t_invests`.`transfers_id` AS `transfers_id`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `bid_amount`,`t_bids`.`apr` AS `apr`,`t_invests`.`amount` AS `invest_amount`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_sec_bid` AS `is_sec_bid`,`t_bids`.`is_agency` AS `is_agency`,`t_users`.`name` AS `name`,concat((select `t_system_options`.`_value` AS `_value` from `t_system_options` where (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,ifnull((select ifnull(sum(((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`) + `t_bill_invests`.`overdue_fine`)),0) AS `has_received_amount` from `t_bill_invests` where ((`t_invests`.`id` = `t_bill_invests`.`invest_id`) and (`t_bill_invests`.`status` in (-(3),-(4),0)))),0) AS `has_received_amount`,(select ifnull(sum(((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`) + `t_bill_invests`.`overdue_fine`)),0) AS `receiving_amount` from `t_bill_invests` where (`t_invests`.`id` = `t_bill_invests`.`invest_id`)) AS `receiving_amount`,(select count(`t_bill_invests`.`id`) AS `dd` from `t_bill_invests` where ((`t_invests`.`id` = `t_bill_invests`.`invest_id`) and (`t_bill_invests`.`status` = -(2)))) AS `overdue_payback_period`,(select count(`t_bill_invests`.`id`) AS `ff` from `t_bill_invests` where ((`t_invests`.`id` = `t_bill_invests`.`invest_id`) and (`t_bill_invests`.`status` in (-(3),-(4),0)))) AS `has_payback_period`,(select count(`t_bill_invests`.`id`) AS `ff` from `t_bill_invests` where (`t_invests`.`id` = `t_bill_invests`.`invest_id`)) AS `period` from ((`t_invests` left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) left join `t_users` on((`t_users`.`id` = `t_bids`.`user_id`))) where ((`t_bids`.`status` in (4,14)) and ((select count(1) from t_bill_invests bi where bi.invest_id = t_invests.id and bi.`status` in (-1,-2,-5,-6)) > 0) and (`t_invests`.`transfer_status` <> -(1)))";
	
	/**
	 * 平台信息详情
	 */
	public static final String V_PLATFORM_DETAIL = "`detail`.`id` AS `id`,`detail`.`operation` AS `operation`,`detail`.`relation_id` AS `relation_id`,`detail`.from_pay_name AS `from_pay`,`detail`.to_receive_name AS `to_receive`,`detail`.`payment` AS `payment`,`detail`.`type` AS `type`,`detail`.`amount` AS `amount`,`detail`.`balance` AS `balance`,`detail`.`summary` AS `summary`,`detail`.`time` AS `time`,`type`.`name` AS `name` from ( `t_platform_details` `detail` JOIN `t_platform_detail_types` `type` ON ((( `detail`.`operation` = `type`.`id` ) AND ( `detail`.`operation` = `type`.`id` )))) where 1 = 1 ";
	
	/**
	 * 新闻类型
	 */
	public static final String V_NEWS_TYPES = "`type`.`id` AS `id`,`type`.`name` AS `name`,`type`.`parent_id` AS `parent_id`,`type`.`status` AS `status`,`type`.`_order` AS `_order`,(select count(0) AS `count(*)` from `t_content_news` `news` where (`news`.`type_id` = `type`.`id`)) AS `counts` from `t_content_news_types` `type` where 1=1 ";
	
	/**
	 * 外部消息
	 */
	public static final String V_MESSAGES_USER_OUTBOX = "`m`.`id` AS `id`,`m`.`sender_user_id` AS `user_id`,if((`m`.`receiver_supervisor_id` > 0),'admin',if((`m`.`receiver_user_id` <> -(10)),(select `t_users`.`name` AS `name` from `t_users` where (`t_users`.`id` = `m`.`receiver_user_id`)),(select concat((select `u`.`name` AS `NAME` from `t_users` `u` where (`u`.`id` = `t_messages_receivers`.`user_id`)),'等',(select count(`t_messages_receivers`.`id`) AS `count(id)` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`)),'人') AS `receiver_name` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`)))) AS `receiver_name`,`m`.`title` AS `title`,`m`.`time` AS `time`,`m`.`content` AS `content` from `t_messages` `m` where ((`m`.`sender_user_id` > 0) and (`m`.`is_erased` = 0)) ";
	
	
	/**
	 * 内部消息
	 */
	public static final  String V_MESSAGES_USER_INBOX = "";

	/**
	 * 系统消息
	 */
	public static final  String V_MESSAGES_SYSTEM = "";
	
	/**
	 * 管理员外部消息
	 */
	public static final  String V_MESSAGES_SUPERVISOR_OUTBOX = "";
	
	public static final  String V_MESSAGES_SUPERVISOR_INBOX = "";
	
	public static final  String V_MESSAGES_SUPERVISOR_DUSTBIN = "";
	
	/**
	 * 投资记录
	 */
	public static final  String V_INVEST_RECORDS = "`t_invests`.`id` AS `id`,`t_bids`.`title` AS `title`,`t_invests`.`time` AS `time`,`t_invests`.`amount` AS `invest_amount`,`t_bids`.`amount` AS `bid_amount`,`t_bids`.`apr` AS `apr`,`t_bids`.`status` AS `status`,`t_invests`.`transfer_status` AS `transfer_status`,`t_users`.`name` AS `name`,`t_invests`.`bid_id` AS `bid_id`,`t_invests`.`user_id` AS `user_id`,`bid_user`.`name` AS `bid_user_name`,concat(`t_system_options`.`_value`,(`t_invests`.`bid_id` + '')) AS `no`,(select count(`t_bid_questions`.`id`) AS `count(id)` from `t_bid_questions` where ((`t_bid_questions`.`bid_id` = `t_invests`.`bid_id`) and (`t_bid_questions`.`user_id` = `t_invests`.`user_id`))) AS `question_count`,(select count(`t_bid_answers`.`id`) AS `count(id)` from `t_bid_answers` where `t_bid_answers`.`bid_question_id` in (select `t_bid_questions`.`id` AS `id` from `t_bid_questions` where ((`t_bid_questions`.`bid_id` = `t_invests`.`bid_id`) and (`t_bid_questions`.`user_id` = `t_invests`.`user_id`)))) AS `answer_count` from ((((`t_invests` left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) join `t_system_options`) left join `t_users` on((`t_users`.`id` = `t_invests`.`user_id`))) left join `t_users` `bid_user` on((`bid_user`.`id` = `t_bids`.`user_id`))) where (`t_system_options`.`_key` = 'loan_number') ";
	
	/**
	 * 前台用户关注的标的
	 */
	public static final String V_FRONT_USER_ATTENTION_BIDS = "`t_user_attention_bids`.`id` AS `id`,`t_credit_levels`.`name` AS `credit_name`,`t_credit_levels`.`image_filename` AS `credit_image_filename`,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`period` AS `period`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,(select `t_bills`.`repayment_time` AS `repayment_time` from `t_bills` where ((`t_bids`.`id` = `t_bills`.`bid_id`) and (`t_bills`.`status` = -(1))) group by `t_bills`.`bid_id` order by `t_bills`.`bid_id`) AS `repayment_time`,concat((select `t_system_options`.`_value` AS `_value` from `t_system_options` where (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_user_attention_bids`.`user_id` AS `user_id`,`t_user_attention_bids`.`bid_id` AS `bid_id`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,`t_users`.`credit_level_id` AS `credit_level_id`,`t_bids`.`time` AS `time` from (`t_user_attention_bids` left join (((((`t_bids` left join `t_products` on((`t_products`.`id` = `t_bids`.`product_id`))) left join `t_users` on((`t_users`.`id` = `t_bids`.`user_id`))) left join `t_credit_levels` on((`t_credit_levels`.`id` = `t_users`.`credit_level_id`))) left join `t_agencies` on((`t_agencies`.`id` = `t_bids`.`agency_id`))) left join `t_dict_bid_repayment_types` on((`t_dict_bid_repayment_types`.`id` = `t_products`.`repayment_type_id`))) on((`t_bids`.`id` = `t_user_attention_bids`.`bid_id`))) where (`t_bids`.`status` in (1,2,3,4,5)) ";
	
    /**
	 * 前台所有的债权转让
	 */
	public static final String V_FRONT_ALL_DEBTS = "`t_invest_transfers`.`id` AS `id`,`t_invest_transfers`.`time` AS `time`,`t_invest_transfers`.`title` AS `title`,`t_invest_transfers`.`transer_reason` AS `transfer_reason`,`t_invest_transfers`.`debt_amount` AS `debt_amount`,`t_invest_transfers`.`status` AS `status`,`t_invest_transfers`.`end_time` AS `end_time`,`t_bids`.`amount` AS `bid_amount`,`t_bids`.`apr` AS `apr`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_bids`.`product_id` AS `product_id`,`t_products`.`name` AS `product_name`,`t_products`.`name_image_filename` AS `product_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_users`.`credit_level_id` AS `credit_level_id`,(select max(`t_invest_transfer_details`.`offer_price`) AS `max_price` from `t_invest_transfer_details` where (`t_invest_transfer_details`.`transfer_id` = `t_invest_transfers`.`id`)) AS `max_price`,(select `t_bills`.`repayment_time` AS `repayment_time` from `t_bills` where ((`t_bills`.`bid_id` = `t_bids`.`id`) and (`t_bills`.`status` = -(1))) group by `t_bills`.`bid_id` order by `t_bills`.`repayment_time`) AS `repayment_time`,`t_invest_transfers`.`is_quality_debt` AS `is_quality_debt`,`t_invest_transfers`.`transfer_price` AS `transfer_price`,`t_credit_levels`.`image_filename` AS `credit_image_filename` ,`t_invest_transfers`.`join_times` AS `join_times` from (((((`t_invest_transfers` left join `t_invests` on((`t_invests`.`id` = `t_invest_transfers`.`invest_id`))) left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) left join `t_products` on((`t_products`.`id` = `t_bids`.`product_id`))) left join `t_users` on((`t_users`.`id` = `t_bids`.`user_id`))) left join `t_credit_levels` on((`t_credit_levels`.`id` = `t_users`.`credit_level_id`))) where (`t_invest_transfers`.`status` in (1,2,3,4)) ";	

	/**
	 * 前台所有的标的 //2016 add chencheng `t_bids`.`product_id`
	 */
	public static final String V_FRONT_ALL_BIDS = "`t_bids`.`product_id` AS `product_id`,`t_bids`.`id` AS `id`,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`show_type` AS `show_type`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`status` AS `status`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`period` AS `period`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,t_bids.repayment_time AS repayment_time,concat (( SELECT `t_system_options`.`_value` AS `_value` FROM `t_system_options` WHERE (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_bids`.`repayment_type_id` AS `repayment_type_id`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,t_users.credit_level_id AS credit_level_id,`t_bids`.`time` AS `time` from `t_bids` LEFT JOIN `t_products` ON `t_products`.`id` = `t_bids`.`product_id` LEFT JOIN t_users ON t_bids.user_id = t_users.id LEFT JOIN `t_agencies` ON `t_agencies`.`id` = `t_bids`.`agency_id` LEFT JOIN `t_dict_bid_repayment_types` ON `t_dict_bid_repayment_types`.`id` = `t_bids`.`repayment_type_id` where `t_bids`.`status` IN (1, 2, 3, 4, 5, 14)";
	
	/**
	 * 数据库操作
	 */
	public static final String V_DB_OPERATIONS = "t_db_operations.id,t_db_operations.time,t_db_operations.ip,t_db_operations.type,t_db_operations.filename,t_supervisors.name as supervisor_name,t_supervisors.ukey AS supervisor_ukey from t_db_operations left join t_supervisors on t_db_operations.supervisor_id = t_supervisors.id order by time desc";
	
	/**
	 * 自动投标确认
	 */
	public static final String V_CONFIRM_AUTOINVEST_BIDS = "`t_bids`.`id` AS `id`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`amount` AS `amount`,`t_bids`.`period` AS `period`,`t_bids`.`min_invest_amount` AS `min_invest_amount`,`t_bids`.`status` AS `status`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`audit_time` AS `audit_time`,`t_users`.`credit_level_id` AS `credit_level_id`,(select `t_credit_levels`.`order_sort` from `t_credit_levels` where (`t_credit_levels`.`id` = `t_users`.`credit_level_id`)) AS `num`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`average_invest_amount` AS `average_invest_amount`,`t_bids`.`apr` AS `apr`,`t_products`.`min_interest_rate` AS `min_interest_rate`,`t_products`.`max_interest_rate` AS `max_interest_rate`,`t_products`.`loan_type` AS `loan_type` from ((`t_bids` left join `t_products` on((`t_bids`.`product_id` = `t_products`.`id`))) left join `t_users` on((`t_bids`.`user_id` = `t_users`.`id`))) where ((`t_bids`.`loan_schedule` < 95) and (`t_products`.`loan_type` <> 3) and (`t_bids`.`status` in (1,2)) and (`t_products`.`is_deal_password` = 0))";
	
	/**
	 * 理财账单详情
	 */
	public static final String V_BILL_INVEST_DETAIL = "`a`.`id` AS `id`,`a`.`user_id` AS `user_id`,`a`.`invest_id` AS `invest_id`,`a`.`periods` AS `current_period`,`e`.`name` AS `name`,`a`.`bid_id` AS `bid_id`,`a`.`title` AS `title`,`b`.`audit_time` AS `audit_time`,`a`.`receive_time` AS `receive_time`,`c`.`name` AS `repayment_type`,`b`.`apr` AS `apr`,`b`.`amount` AS `amount`,(select count(`t`.`id`) AS `COUNT(t.id)` from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `loan_periods`,(select count(`t`.`id`) AS `couont` from `t_bill_invests` `t` where ((`t`.`user_id` = `a`.`user_id`) and (`t`.`bid_id` = `a`.`bid_id`) and (`t`.`status` <> -(7)) and (`t`.`invest_id` = `a`.`invest_id`))) AS `invest_periods`,ifnull((select sum(`t`.`receive_corpus`) AS `amount` from `t_bill_invests` `t` where ((`t`.`user_id` = `a`.`user_id`) and (`t`.`bid_id` = `a`.`bid_id`) and (`t`.`status` in (-(1),-(2),-(5),-(6))) and (`t`.`invest_id` = `a`.`invest_id`))),0) AS `still_receiving_corpus`,`a`.`receive_corpus` AS `receive_corpus`,`d`.`amount` AS `invest_amount`,(select sum((`t`.`receive_corpus` + `t`.`receive_interest`)) AS `ss` from `t_bill_invests` `t` where (`t`.`id` = `a`.`id`)) AS `should_received_amount`,(select sum(((`t`.`receive_corpus` + `t`.`receive_interest`) + `t`.`overdue_fine`)) AS `ss` from `t_bill_invests` `t` where (`t`.`id` = `a`.`id`)) AS `current_receive_amount`,(select sum(((`t`.`receive_corpus` + `t`.`receive_interest`) + `t`.`overdue_fine`)) AS `receive_amounts` from `t_bill_invests` `t` where ((`t`.`user_id` = `a`.`user_id`) and (`t`.`bid_id` = `a`.`bid_id`) and (`t`.`status` <> -(7)) and (`t`.`invest_id` = `a`.`invest_id`))) AS `should_receive_all_amount`,ifnull((select sum(((`t`.`receive_corpus` + `t`.`receive_interest`) + `t`.`overdue_fine`)) AS `amount` from `t_bill_invests` `t` where ((`t`.`user_id` = `a`.`user_id`) and (`t`.`bid_id` = `a`.`bid_id`) and (`t`.`status` in (-(3),-(4),0)) and (`t`.`invest_id` = `a`.`invest_id`))),0) AS `has_received_amount`,(select count(`t`.`id`) AS `couont` from `t_bill_invests` `t` where ((`t`.`user_id` = `a`.`user_id`) and (`t`.`bid_id` = `a`.`bid_id`) and (`t`.`status` in (-(3),-(4),0)) and (`t`.`invest_id` = `a`.`invest_id`))) AS `has_received_periods`,(select count(`t1`.`id`) AS `count(``t1``.``id``)` from `t_bills` `t1` where ((`t1`.`bid_id` = `a`.`bid_id`) and (`t1`.`status` in (-(3),0)))) AS `has_payed_periods`,(select sum(((`t`.`repayment_corpus` + `t`.`repayment_interest`) + `t`.`overdue_fine`)) AS `amount` from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `loan_principal_interest`,concat(`f`.`_value`,cast(`a`.`id` as char charset utf8)) AS `invest_number`, a.status as invest_status from (((((`t_bill_invests` `a` left join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) left join `t_dict_bid_repayment_types` `c` on((`b`.`repayment_type_id` = `c`.`id`))) left join `t_invests` `d` on(((`d`.`bid_id` = `a`.`bid_id`) and (`d`.`user_id` = `a`.`user_id`) and (`d`.`id` = `a`.`invest_id`)))) left join `t_users` `e` on((`a`.`user_id` = `e`.`id`))) join `t_system_options` `f`) where ((`f`.`_key` = 'invests_bill_number') and (`a`.`status` <> -(7))) ";
	
	/**
	 * 理财账单
	 */
	public static final String V_BILL_INVEST = "`a`.`id` AS `id`,`c`.`id` AS `user_id`,`a`.`bid_id` AS `bid_id`,`a`.`title` AS `title`,((`a`.`receive_corpus` + `a`.`receive_interest`) + `a`.`overdue_fine`) AS `income_amounts`,`a`.`status` AS `status`,`a`.`receive_time` AS `repayment_time`,concat(`d`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) AS `bid_no`,`a`.`real_receive_time` AS `real_repayment_time` from ((`t_bill_invests` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`a`.`user_id` = `c`.`id`)) JOIN `t_system_options` `d`) where 1=1 AND `d`.`_key` = 'loan_number' ";
	
	/**
	 * 部门已付账单
	 */
	public static final String V_BILL_DEPARTMENT_HASPAYED = "`a`.`id` AS `id`,`c`.`id` AS `user_id`,year(`a`.`repayment_time`) AS `year`,month(`a`.`repayment_time`) AS `month`,`b`.`id` AS `bid_id`,concat(`e`.`_value`,cast(`a`.`id` as char charset utf8)) AS `bill_no`,`c`.`name` AS `name`,concat(`f`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`amount` AS `amount`,`b`.`apr` AS `apr`,`a`.`title` AS `title`,(select concat(`a`.`periods`,'/',count(`t`.`id`)) from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `period`,`a`.`repayment_time` AS `repayment_time`,(case when ((`a`.`repayment_time` - now()) > 0) then 0 else concat(concat(cast(floor((timestampdiff(MINUTE,`a`.`repayment_time`,now()) / 1440)) as char(10) charset utf8),'天'),concat(cast(floor(((timestampdiff(MINUTE,`a`.`repayment_time`,now()) % 1440) / 60)) as char charset utf8),'\r\n\r\n小时'),concat(cast(((timestampdiff(MINUTE,`a`.`repayment_time`,now()) - (floor((timestampdiff(MINUTE,`a`.`repayment_time`,now()) / 1440)) * 1440)) - (floor(((timestampdiff(MINUTE,`a`.`repayment_time`,now()) % 1440) / 60)) * 60)) as char charset utf8)),'分') end) AS `overdue_time`,`a`.`real_repayment_time` AS `real_repayment_time`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`c`.`assigned_to_supervisor_id` = `a`.`id`)) AS `supervisor_name`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`b`.`manage_supervisor_id` = `a`.`id`)) AS `supervisor_name2` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`b`.`user_id` = `c`.`id`))) join `t_system_options` `f`) join `t_system_options` `e`) where ((`e`.`_key` = 'loan_bill_number') and (`f`.`_key` = 'loan_number') and (`a`.`status` in (0,-(3)))) ";
	
	/**
	 * 账单
	 */
	public static final String V_BILL_BOARD = "`t_bill_invests`.`id` AS `id`,`t_users`.`name` AS `name`,sum(`t_bill_invests`.`receive_corpus`) AS `corpus`,sum(`t_bill_invests`.`receive_interest`) AS `interest`,count(distinct `t_bill_invests`.`bid_id`) AS `bid_count` from (`t_bill_invests` left join `t_users` on((`t_users`.`id` = `t_bill_invests`.`user_id`))) where (`t_bill_invests`.`status` <> -(7)) ";
	
	
	/**
	 * 超额借款排序条件
	 */
	public static final String[] OVER_BORROWS_ORDER_CONDITION = 
		{
			" order by time desc",
			" order by time desc",
			" order by appended_items_count desc",
			" order by appended_items_count",
			" order by passed_items_count desc",
			" order by passed_items_count",
			" order by unpassed_items_count desc",
			" order by unpassed_items_count",
			" order by auditing_items_count desc",
			" order by auditing_items_count"
		};
	
	/**
	 * 锁定用户排序
	 */
	public static final String [] LOCKED_USER_ORDER = {"","recharge_amount asc","recharge_amount desc",
		"lock_time asc","lock_time desc","user_amount asc","user_amount desc"};
	
	/**
	 * 用户详情分组排序
	 */
	public static final String V_USER_DETAILS_GROUP_ORDER = "group by `t_users`.`id`,`t_user_details`.`time`,`t_user_details`.`operation`,`t_user_details`.`amount`,`t_user_details`.`balance`,`t_user_details`.`freeze`,`t_user_details`.`recieve_amount`,`t_user_details`.`summary`,`t_user_detail_types`.`name` order by `t_user_details`.`time` desc,`t_user_details`.`id` desc";

	/**
	 * 查询事件关键字
	 * 0 全部, 1 ip地址, 2 操作内容, 3 管理员名字
	 */
	public static final String[] QUERY_EVENT_KEYWORD = {
		" and (t_supervisor_events.ip like ? or t_supervisor_events.descrption like ? or t_supervisors.name like ?) ",
		" and (t_supervisor_events.ip like ?) ",
		" and (t_supervisor_events.descrption like ?) ",
		" and (t_supervisors.name like ?) "
		};
	
	/**
	 * 前台--我的借款账单
	 */
	public static final String [] LOAN_BILL_REPAYMENT = {"", "and status in (-1, -2) ", "and status in (-3, 0) "};
	public static final String [] LOAN_BILL_OVDUE = {"", "and is_overdue in (0) ", "and is_overdue in (-1, -2, -3) "};
	public static final String [] LOAN_BILL_ALL = {"and title like ? ", "and title like ? "};	
	
	/**
	 * 前台--我的理财账单
	 */
	public static final String [] LOAN_INVESTBILL_RECEIVE = {"", "and a.status in (-1, -2,-5,-6) ", "and a.status in (-3 ,-4, 0) "};
	public static final String [] LOAN_INVESTBILL_OVDUE = {"", "and a.status in (-1, 0,-5) ", "and a.status in (-4, -2, -3,-6) "};
	public static final String [] LOAN_INVESTBILL_ALL = {"and a.title like ? ", "and a.title like ? "};	
	
	public static final String [] DEBT_AMOUNT_CONDITION = {" ","  and  t_invest_transfers.debt_amount <= 1000 ",
		"  and   t_invest_transfers.debt_amount > 1000  and  t_invest_transfers.debt_amount <=5000 ","  and   t_invest_transfers.debt_amount > 5000   and  t_invest_transfers.debt_amount <=10000 ",
		"  and   t_invest_transfers.debt_amount > 10000 and   t_invest_transfers.debt_amount <=30000 ","  and   t_invest_transfers.debt_amount > 30000 "};
	
	public static final String [] BID_APR_CONDITION = {" "," and t_bids.apr < 10 ","  and t_bids.apr >= 10 and t_bids.apr <= 15 ",
		" and t_bids.apr >= 15 and t_bids.apr <= 20  "," and t_bids.apr > 20 "};
	
	public static final String [] DEBT_ORDER_CONITION = {" order by status,is_quality_debt desc"," order by  debt_amount desc"," order by  debt_amount asc",
		" order by  apr desc", " order by  apr asc", " order by  end_time desc", " order by  end_time asc", " order by  repayment_time desc", "order by  repayment_time asc"};

	public static final String [] TRANSFER_MANAGEMENT_STATUS_CONDITION = {" ", " and t_invest_transfers.status = 0 "," and ( t_invest_transfers.status = 1  or t_invest_transfers.status = 2)"," and t_invest_transfers.status = 3 "," and ( t_invest_transfers.status = -2  or  t_invest_transfers.status = -3  or t_invest_transfers.status = -5)   "," and t_invest_transfers.status = -1 "};
	
	public static final String [] TRANSFER_MANAGEMENT_TYPE_CONDITION = {"   ","  and t_bids.title like ? ","  and t_bids.id like ? "};
	
	public static final String [] BID_AMOUNT_CONDITION = {" "," and t_bids.amount < 100000 ","  and t_bids.amount >= 100000 and t_bids.amount <= 500000  ",
        " and t_bids.amount >= 500000 and t_bids.amount <= 1000000 ","  and t_bids.amount >= 1000000 and t_bids.amount <= 3000000 ",
        "  and t_bids.amount > 3000000 "};
	
	public static final String [] BID_LOAN_SCHEDULE_CONDITION = { " "," and t_bids.loan_schedule < 50 ",
		" and t_bids.loan_schedule >= 50  and t_bids.loan_schedule <=80 "," and t_bids.loan_schedule > 80  and t_bids.loan_schedule <= 100 "," and t_bids.loan_schedule = 100 "};
	

	/**
	 * 待验证的借款标 
	 */
	public static final String V_BID_WAIT_VERIFY = "SELECT concat(`e`.`_value`, cast(`b`.`id` AS CHAR charset utf8))AS `bid_no`,`b`.`id` AS `id`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`time` AS `time`,`b`.`amount` AS `amount`,`b`.`status` AS `status`,`b`.`mark` AS `mark`,`f_credit_levels`(`u`.`id`)AS `credit_level_id`,`f_user_audit_item`(`u`.`id`, `b`.`mark`, 2)AS `user_item_count_true`,`f_user_audit_item`(`u`.`id`, `b`.`mark` ,-(1))AS `user_item_count_false`,`b`.`repayment_type_id` AS `repaymentId`,(SELECT count(`pail`.`id`)AS `product_item_count` FROM`t_product_audit_items_log` `pail` WHERE `pail`.`mark` = `b`.`mark` AND `pail`.`type` = 1)AS `product_item_count` FROM `t_bids` `b` LEFT JOIN `t_users` `u` ON `b`.`user_id` = `u`.`id` LEFT JOIN `t_products` `p` ON `b`.`product_id` = `p`.`id` JOIN `t_system_options` `e` WHERE(`b`.`status` = 10 OR b.`status` = 11 OR b.`status` = 12) AND `e`.`_key` = 'loan_number'";
	
	/**
	 * 审核中的借款标
	 */
	public static final String V_BID_AUDITING = "select concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`time` AS `time`,`b`.`amount` AS `amount`,`b`.`status` AS `status`,`b`.`mark` AS `mark`,`c`.`order_sort` AS `order_sort`,`c`.`image_filename` AS `credit_level_image_filename`,`f_user_audit_item` (`u`.`id`, `b`.`mark`,2) AS `user_item_count_true`,`f_user_audit_submit_item` (`u`.`id`, `b`.`mark`) AS `user_item_submit`,`f_user_audit_item`(`u`.`id`,`b`.`mark`,-(1)) AS `user_item_count_false`,`b`.`repayment_type_id` AS `repaymentId`,(select count(`pail`.`id`) AS `product_item_count` from `t_product_audit_items_log` `pail` where ((`pail`.`mark` = `b`.`mark`) and (`pail`.`type` = 1))) AS `product_item_count` from (((((`t_bids` `b` left join `t_users` `u` on((`b`.`user_id` = `u`.`id`))) left join `t_products` `p` on((`b`.`product_id` = `p`.`id`))) LEFT JOIN `t_credit_levels` `c` ON ((`u`.`credit_level_id` = `c`.`id`))) join `t_system_options` `e`)) where ((`b`.`status` = 0) and (`e`.`_key` = 'loan_number'))";
	
	/**
	 * 坏账中的借款标
	 */
	public static final String V_BID_BAD = "select concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`time` AS `time`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`amount` AS `amount`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`status` AS `status`,`b`.`audit_time` AS `audit_time`,`b`.`last_repay_time` AS `last_repay_time`,`b`.`manage_supervisor_id` AS `manage_supervisor_id`,`s`.`name` AS `supervisor_name`,`c`.`image_filename` AS `credit_level_image_filename`,(case when ((`bl`.`repayment_time` - now()) > 0) then '0' else datediff(now() , `bl`.`repayment_time`) end) AS `overdue_length`,max(`bl`.`mark_bad_time`) AS `mark_bad_time`,(select count(`bl`.`id`) AS `repayment_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`status` in (-(2),-(3),0)))) AS `repayment_count`,(select count(`bl`.`id`) AS `overdue_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`overdue_mark` in (-(2),-(3),-(1))))) AS `overdue_count` from ((((((`t_bids` `b` left join `t_users` `u` on((`b`.`user_id` = `u`.`id`))) left join `t_credit_levels` `c` on((`u`.`credit_level_id` = `c`.`id`))) left join `t_products` `p` on((`b`.`product_id` = `p`.`id`))) left join `t_bills` `bl` on((`bl`.`bid_id` = `b`.`id`))) left join `t_supervisors` `s` on((`s`.`id` = `b`.`manage_supervisor_id`))) join `t_system_options` `e`) where ((`b`.`status` in (4,5)) and (`e`.`_key` = 'loan_number') and (`bl`.`overdue_mark` = -(3))) group by `b`.`id`";

	/**
	 * 逾期的借款标
	 */
	public static final String V_BID_OVERDUE = "select concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`time` AS `time`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`amount` AS `amount`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`status` AS `status`,`b`.`audit_time` AS `audit_time`,`b`.`repayment_type_id` AS `repayment_type_id`,`r`.`name` AS `repayment_type_name`,min(`bl`.`mark_overdue_time`) AS `mark_overdue_time`,c.image_filename AS credit_level_image_filename,(select count(`bl`.`id`) AS `repayment_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`status` in (-(3),0)))) AS `repayment_count`,(select count(`bl`.`id`) AS `overdue_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`overdue_mark` in (-(1),-(2),-(3))))) AS `overdue_count` from ((((((`t_bids` `b` left join `t_users` `u` on((`b`.`user_id` = `u`.`id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) left join `t_products` `p` on((`b`.`product_id` = `p`.`id`))) left join `t_dict_bid_repayment_types` `r` on((`r`.`id` = `b`.`repayment_type_id`))) left join `t_bills` `bl` on((`bl`.`bid_id` = `b`.`id`))) join `t_system_options` `e`) where ((`b`.`status` = 4) and (`bl`.`overdue_mark` in (-(1),-(2),-(3))) and (`e`.`_key` = 'loan_number')) group by `b`.`id`";
	
	/**
	 * 标的提问记录
	 */
	public static final String V_BID_QUESTIONS = "SELECT q.id AS id, b.title AS title, q.user_id AS user_id, u.name as user_name, q.time AS time, q.bid_id AS bid_id, q.content AS content, q.is_answer AS is_answer, q.questioned_user_id as questioned_user_id FROM t_bid_questions q LEFT JOIN t_bids b ON q.bid_id = b.id LEFT JOIN  t_users u ON u.id = q.user_id";
	
	/**
	 * 已成功的借款标
	 */
	public static final String V_BID_REPAYMENT = "select concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`b`.`time` AS `time`,`u`.`name` AS `user_name`,`b`.`amount` AS `amount`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`status` AS `status`,`b`.`audit_time` AS `audit_time`,`b`.`last_repay_time` AS `last_repay_time`,`dbrt`.`name` AS `repayment_type_name`,`b`.`manage_supervisor_id` AS `manage_supervisor_id`,`s`.`name` AS `supervisor_name`,c.image_filename AS credit_level_image_filename,(select sum(((`bi`.`repayment_corpus` + `bi`.`repayment_interest`) + `bi`.`overdue_fine`)) AS `capital_interest_sum` from `t_bills` `bi` where (`bi`.`bid_id` = `b`.`id`)) AS `capital_interest_sum`,(select count(`bl`.`id`) AS `repayment_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`status` in (0,-(3))))) AS `repayment_count`,(select count(`bl`.`id`) AS `overdue_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`overdue_mark` in (-(3),-(1),-(2))))) AS `overdue_count` from ((((((`t_bids` `b` left join `t_users` `u` on((`b`.`user_id` = `u`.`id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) left join `t_products` `p` on((`b`.`product_id` = `p`.`id`))) left join `t_dict_bid_repayment_types` `dbrt` on((`dbrt`.`id` = `b`.`repayment_type_id`))) left join `t_supervisors` `s` on((`s`.`id` = `b`.`manage_supervisor_id`))) join `t_system_options` `e`) where ((`b`.`status` = 5) and (`e`.`_key` = 'loan_number'))";
	
	/**
	 * 还款中的借款标
	 */
	public static final String V_BID_REPAYMENTING = "select concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`time` AS `time`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`amount` AS `amount`,`b`.`product_id` AS `product_id`,`p`.`name` AS `product_name`,`p`.`small_image_filename` AS `small_image_filename`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`status` AS `status`,`b`.`audit_time` AS `audit_time`,`b`.`repayment_type_id` AS `repayment_type_id`,`r`.`name` AS `repayment_type_name`,`b`.`manage_supervisor_id` AS `manage_supervisor_id`,`s`.`name` AS `supervisor_name`,c.image_filename AS credit_level_image_filename,c.order_sort AS `order_sort`,(select count(`bl`.`id`) AS `repayment_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`status` in (0,-(3))))) AS `repayment_count`,(select count(`bl`.`id`) AS `overdue_count` from `t_bills` `bl` where ((`bl`.`bid_id` = `b`.`id`) and (`bl`.`overdue_mark` in (-(3),-(1),-(2))))) AS `overdue_count`,(select `bi`.`repayment_time` AS `repayment_time` from `t_bills` `bi` where ((month(now()) <= month(`bi`.`repayment_time`)) and (`bi`.`bid_id` = `b`.`id`)) group by `bi`.`bid_id`) AS `repayment_time` from ((((((`t_bids` `b` left join `t_users` `u` on((`b`.`user_id` = `u`.`id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) left join `t_products` `p` on((`b`.`product_id` = `p`.`id`))) left join `t_dict_bid_repayment_types` `r` on((`r`.`id` = `b`.`repayment_type_id`))) left join `t_supervisors` `s` on((`s`.`id` = `b`.`manage_supervisor_id`))) join `t_system_options` `e`) where ((`b`.`status` = 4 or `b`.`status` = 14) and (`e`.`_key` = 'loan_number'))";

	/**
	 * 会员资料审核管理统计
	 */
	public static final String V_USER_AUDIT_ITEM_STATS = "SELECT a.id, a.`name` as user_name,a.time,a.email,a.mobile,b.sum_count,b.audited_count,b.not_pass_count,b.auditing_count,b.bid_success_count,b.bid_loaning_count,b.invest_count,c.image_filename AS credit_level_image_filename FROM ((t_users a LEFT JOIN t_statistic_user_audit_items b ON (a.id = b.user_id)) LEFT JOIN t_credit_levels c ON ((a.credit_level_id = c.id)))";

	/**
	 * 已激活且不是黑名单用户
	 */
	public static final String V_USER_ACTIVE = "`t_users`.`id` AS `id`,t_credit_levels.image_filename AS credit_level_image_filename,t_credit_levels.order_sort AS order_sort,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,`t_users`.`credit_score` AS `score`,`t_users`.`email` AS `email`,`t_users`.`mobile` AS `mobile`,`t_users`.`mobile1` AS `mobile1`,`t_users`.`mobile2` AS `mobile2`,`t_users`.`is_email_verified` AS `is_activation`,`t_users`.`is_add_base_info` AS `is_add_base_info`,`t_users`.`is_blacklist` AS `is_blacklist`,((`t_users`.`balance` + `t_users`.`balance2`)) AS `user_amount`,`t_users`.`last_login_time` AS `last_login_time`,`t_users`.`is_allow_login` AS `is_allow_login`,(select ifnull(sum(`t_user_details`.`amount`),0) AS `recharge_amount` from (`t_user_details` join `t_user_detail_types` on((`t_user_details`.`operation` = `t_user_detail_types`.`id`))) where ((`t_user_details`.`user_id` = `t_users`.`id`) and (`t_user_detail_types`.`id` in (1,2,3)))) AS `recharge_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (?, ?, ?))) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (?, ?, ?))) AS `invest_amount`,(select count(0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.`status` IN (?, ?, ?))) AS `bid_count`,(select ifnull(sum(`t_bids`.`amount`),0) AS `bid_count` from `t_bids` where (`t_bids`.`user_id` = `t_users`.`id` AND t_bids.`status` IN (?, ?, ?))) AS `bid_amount`,(select count(0) AS `audit_item_count` from `t_user_audit_items` where ((`t_users`.`id` = `t_user_audit_items`.`user_id`) and (`t_user_audit_items`.`status` = 2))) AS `audit_item_count` from `t_users` LEFT JOIN t_credit_levels ON t_users.credit_level_id = t_credit_levels.id where t_users.is_blacklist != 1 and (t_users.is_email_verified = 1 or t_users.is_mobile_verified = 1) ";
	
	/**
	 * 托管日志列表
	 */
	public static final String V_MMM_RETURN_DATA = " select `t_return_data`.`id` AS `id`,`t_return_data`.`orderNum` AS `orderNum`,`t_mmm_data`.`status` AS `status`,`t_users`.`name` AS `user_name`,`t_return_data`.`mmmUserId` AS `user_ips_actno`,`t_return_data`.`op_time` AS `return_time`,`t_mmm_data`.`parent_orderNum` AS `parent_orderNum`,`t_mmm_data`.`msg` AS `msg`,`t_mmm_data`.`op_time` AS `send_time`,`t_return_data`.`id` AS `send_id`,`t_return_data`.`type` AS `type`,`t_mmm_data`.`url` AS `url` from ((`t_return_data` left join `t_mmm_data` on((`t_mmm_data`.`orderNum` = `t_return_data`.`orderNum`))) left join `t_users` on((`t_users`.`ips_acct_no` = `t_mmm_data`.`mmmUserId`))) where 1=1 "; 

}