package org.wekit.web.service;

import java.util.List;

import org.wekit.web.IPaginable;
import org.wekit.web.db.Pagination;
import org.wekit.web.db.model.CodeRule;

public interface RuleService {

	/**
	 * 根据ID获取编码规则
	 * @param id
	 * @return
	 */
	public CodeRule getCodeRule(long id);
	
	
	/**
	 * 根据规则编码获取编码规则信息
	 * @param coderule
	 * @return
	 */
	public CodeRule getCodeRule(String coderule);
	
	/**
	 * 获取所有的编码规则
	 * @return
	 */
	public List<CodeRule> getAllCodeRules();
	
	
	/**
	 * 根据分页信息获取规则编码
	 * @param currentPage
	 * @param pagesize
	 * @param isCount
	 * @return
	 */
	public List<CodeRule> queryCodeRules(int currentPage,int pagesize,boolean isCount);
	
	/**
	 * 根据分页信息返回所有的数据
	 * @param paginable
	 * @return
	 */
	public List<CodeRule> queryCodeRules(IPaginable paginable);
	
	
	/**
	 * 根据关键字查找编码规则
	 * @param key
	 * @param state
	 * @param paginable
	 * @return
	 */
	public List<CodeRule> queryCodeRules(String key,int state,IPaginable paginable);
	
	/**
	 * 查找所有的规则名称
	 * @param paginable
	 * @return
	 */
	public List<String> queryCodeRuleNames(IPaginable paginable);
}
