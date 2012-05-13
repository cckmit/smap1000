package org.wekit.web.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.wekit.web.IPaginable;
import org.wekit.web.MaskParser;
import org.wekit.web.WekitException;
import org.wekit.web.db.dao.CodeDao;
import org.wekit.web.db.dao.CodePoolDao;
import org.wekit.web.db.dao.CodeRuleDao;
import org.wekit.web.db.dao.CodeSequenceDao;
import org.wekit.web.db.dao.TempCodeDao;
import org.wekit.web.db.dao.UserDao;
import org.wekit.web.db.model.Code;
import org.wekit.web.db.model.CodePool;
import org.wekit.web.db.model.CodeRule;
import org.wekit.web.db.model.CodeSequence;
import org.wekit.web.db.model.TempCode;
import org.wekit.web.db.model.User;
import org.wekit.web.service.CodeService;
import org.wekit.web.util.DataWrapUtil;

/**
 * 编码功能申请服务实现
 * 
 * @author huangweili
 * 
 */
@Service("codeService")
public class CodeServiceImpl implements CodeService {

	private static Logger	logger	= Logger.getLogger(CodeServiceImpl.class);

	@Autowired
	@Qualifier("codeDao")
	private CodeDao			codeDao;

	@Autowired
	@Qualifier("tempCodeDao")
	private TempCodeDao		tempCodeDao;

	@Autowired
	@Qualifier("codeSequenceDao")
	private CodeSequenceDao	codeSequenceDao;

	@Autowired
	@Qualifier("codeRuleDao")
	private CodeRuleDao		codeRuleDao;

	@Autowired
	@Qualifier("userDao")
	private UserDao			userDao;

	@Autowired
	@Qualifier("codePoolDao")
	private CodePoolDao		codePoolDao;

	@Override
	public Code getCode(Long id) {
		if (id != null && id > 0) {
			return codeDao.getCode(id);
		}
		return null;
	}

	@Override
	public Code getCode(String code) {
		if (StringUtils.isEmpty(code))
			return null;
		return this.codeDao.getCode(code);
	}

	@Override
	public boolean deleteCode(Code code) {
		return this.codeDao.deleteCode(code);
	}

	@Override
	public boolean deleteCode(Long codeId) {
		return this.codeDao.deleteCode(codeId);
	}

	@Override
	public boolean updateCode(Code code) {
		return this.codeDao.updateCode(code);
	}

	@Override
	public List<Code> getAllCodes() {
		return this.codeDao.getAllCodes();
	}

	@Override
	public List<Code> getCodesWithPagination(IPaginable paginable) {
		if (paginable != null)
			return codeDao.getCodesWithPaginable(paginable);
		else
			return codeDao.getAllCodes();
	}

	@Override
	public Code addCode(Code code) {
		return this.codeDao.addCode(code);
	}

	public CodeDao getCodeDao() {
		return codeDao;
	}

	public void setCodeDao(CodeDao codeDao) {
		this.codeDao = codeDao;
	}

	public TempCodeDao getTempCodeDao() {
		return tempCodeDao;
	}

	public void setTempCodeDao(TempCodeDao tempCodeDao) {
		this.tempCodeDao = tempCodeDao;
	}

	public CodeSequenceDao getCodeSequenceDao() {
		return codeSequenceDao;
	}

	public void setCodeSequenceDao(CodeSequenceDao codeSequenceDao) {
		this.codeSequenceDao = codeSequenceDao;
	}

	public CodeRuleDao getCodeRuleDao() {
		return codeRuleDao;
	}

	public void setCodeRuleDao(CodeRuleDao codeRuleDao) {
		this.codeRuleDao = codeRuleDao;
	}

	/**
	 * 独立取号和批量取号的差别是独立取号有限查找可用的序列号，在生成新的序列号，而批量操作只生成联系的新号而不关心旧的号码
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	@Override
	public Code fetchCode(long ruleId, String unitCode, String locationCode, String docCode, String createrId, String note, String filename, String codeName) {
		CodeRule codeRule = codeRuleDao.getCodeRule(ruleId);
		if (codeRule == null)
			throw new WekitException("对应编码规则ID的信息不存在!");
		User user = userDao.getByID(createrId);
		if (user == null) {
			throw new WekitException("对应的用户信息不存在!");
		}
		List<TempCode> tempCodes = tempCodeDao.queryTempCodes(codeRule.getRule(), unitCode, locationCode, docCode, codeRule.getMinSequence(), codeRule.getMaxSequence(), null);

		if (tempCodes != null && tempCodes.size() > 0) {
			TempCode tempCode = null;
			for (TempCode code : tempCodes) {
				if (!codePoolDao.isExistsed(code.getCode())) {
					tempCode = code;
					break;
				} else {
					tempCodeDao.deleteTempCode(code);
				}
			}
			if (tempCode != null) {
				String uuid = UUID.randomUUID().toString();
				Code code = new Code(codeRule.getRuleName(), codeRule.getRule(), user.getDisplayName(), user.getLoginName(), unitCode, locationCode, docCode, tempCode.getCode(), 1, uuid, System.currentTimeMillis(), note, filename, user.getDeptDisplayName(), codeRule.getFileTypeName(), tempCode.getCodeName());
				code = codeDao.addCode(code);
				tempCodeDao.deleteTempCode(tempCode);
				return code;
			}
		}
		List<Code> codes = createCodes(codeRule, unitCode, locationCode, docCode, user, note, 1, filename, codeName);
		if (codes != null && codes.size() > 0)
			return codes.get(0);
		return null;
	}

	/**
	 * 批量获取编码,会抛出运行时异常
	 * 
	 * @param rule
	 * @param unitCode
	 * @param locationCode
	 * @param docCode
	 * @param creater
	 * @param createId
	 * @param note
	 * @param batchSize
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	@Override
	public List<Code> batchCode(long ruleId, String unitCode, String locationCode, String docCode, String createId, String note, int batchSize, String filename, String codeName) {
		CodeRule codeRule = codeRuleDao.getCodeRule(ruleId);
		if (codeRule == null) {
			throw new WekitException("找不到对应的编码规则!");
		}
		User user = userDao.getByID(createId);
		if (user == null) {
			throw new WekitException("找不到对应的用户信息!");
		}
		return this.createCodes(codeRule, unitCode, locationCode, docCode, user, note, batchSize, filename, codeName);
	}

	/**
	 * 隔离事务单独的函数
	 * 
	 * @param rule
	 * @param unitCode
	 * @param locationCode
	 * @param docCode
	 * @param creater
	 * @param createId
	 * @param note
	 * @param batchSize
	 * @return
	 */
	private List<Code> createCodes(CodeRule codeRule, String unitCode, String locationCode, String docCode, User user, String note, int batchSize, String fileName, String codeName) {
		String mask = checkRule(codeRule.getRule(), unitCode, locationCode, docCode);
		if (mask == null)
			throw new WekitException("规则验证无效!");
		MaskParser maskParser = paserMask(mask);
		List<CodeSequence> codeSequences = codeSequenceDao.queryCodeSequences(codeRule.getRule(), unitCode, locationCode, docCode, maskParser.getParam(),codeRule.getMinSequence(),codeRule.getMaxSequence(), null);
		CodeSequence codeSequence = null;
		int minSeq = codeRule.getMinSequence();
		int maxSeq = codeRule.getMaxSequence();
		if (codeSequences != null && codeSequences.size() > 0) {
			codeSequence = codeSequences.get(0);
			if (maxSeq > 0 && (codeSequence.getSeq() + batchSize) >= maxSeq)
				throw new WekitException("需要生成的编码已经超过了该规则可生成的数量限制!还可以申请" + (maxSeq - codeSequence.getSeq() - 1) + "个编码!");
		} else {
			// 构造新的dequence
			codeSequence = initCodeSequence(codeRule.getRule(), unitCode, locationCode, docCode, maskParser.getParam(), codeRule.getMinSequence(), codeRule.getMaxSequence());
			if (maxSeq > 0 && (maxSeq - minSeq - 1) < batchSize)
				throw new WekitException("需要生成的编码已经超过了该规则可生成的数量限制!还可以申请" + (maxSeq - minSeq - 1) + "个编码!");
		}
		List<String> codes = generationCode(unitCode + "-" + locationCode + "-" + docCode + "-" + maskParser.getMask(), maskParser.getCount(), codeSequence, batchSize, maxSeq);
		List<Code> generationCodes = codeDao.addCodes(codes,codeRule, unitCode, locationCode, docCode,user, note, fileName,codeName);
		if (!codeSequenceDao.updateCodeSequence(codeSequence))
			throw new WekitException("生成编码时发生意外请与管理员联系!");
		return generationCodes;
	}

	/**
	 * 返回[n]中n的长度
	 * 
	 * @param mask
	 * @return
	 */
	public MaskParser paserMask(String mask) {
		Map<String, Integer> param = new HashMap<String, Integer>();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH + 1); // 英文的1月份是0开始的
		int day = calendar.get(Calendar.MONDAY);

		if (mask.indexOf("[yyyy]") > 0) {
			mask = mask.replace("[yyyy]", String.valueOf(year));
			param.put("year", year);
		}
		if (mask.indexOf("[yy]") > 0) {
			mask = mask.replace("[yy]", String.valueOf(year % 100));
			param.put("year", year);
		}
		if (mask.indexOf("[mm]") > 0) {
			if (month > 9)
				mask = mask.replace("[mm]", String.valueOf(month));
			else
				mask = mask.replace("[mm]", "0" + String.valueOf(month));
			param.put("month", month);
		}
		if (mask.indexOf("[dd]") > 0) {
			if (day > 9) {
				mask = mask.replace("[dd]", String.valueOf(day));
			} else {
				mask = mask.replace("[dd]", "0" + String.valueOf(day));
			}
			param.put("day", day);
		}
		int left = mask.indexOf('[');
		if (left < 0) {
			return new MaskParser(param, mask, 0);
		}
		int right = mask.indexOf(']');

		return new MaskParser(param, mask, right - left - 1);
	}

	protected List<String> generationCode(String rule, int count, CodeSequence codeSequence, int batchNums, int maxseq) {
		List<String> result = new ArrayList<String>();
		long seq = codeSequence.getSeq();
		String temp = null;
		String relseq = null;
		int canApply = 0;
		boolean ishave = true;
		Code code = null;
		while (batchNums > 0) {
			ishave = true;
			while (ishave) {
				temp = new String(rule);
				seq++;
				relseq = String.format("%0" + count + "d", seq);
				temp = temp.replaceAll("\\[n*\\]", relseq);
				code = codeDao.getCode(temp);
				if (code == null) {
					result.add(temp);
					ishave = false;
					canApply++;
				}
				if (seq >= maxseq) {
					throw new WekitException("需要生成的编码已经超过了该规则可生成的数量限制 还可以申请" + canApply + "个编码!");
				}
			}
			batchNums--;
		}
		codeSequence.setSeq(seq);
		return result;
	}

	/**
	 * 添加新的序列
	 * 
	 * @param rule
	 * @param unitCode
	 * @param locationCode
	 * @param docCode
	 * @param params
	 * @return
	 */
	private CodeSequence initCodeSequence(String codeRule, String unitCode, String locationCode, String docCode, Map<String, Integer> params, int minSeq, int maxSeq) {
		CodeSequence codeSequence = new CodeSequence(codeRule, unitCode, locationCode, docCode);
		if (params.containsKey("year")) {
			codeSequence.setYear(params.get("year"));
		}
		if (params.containsKey("month")) {
			codeSequence.setMonth(params.get("month"));
		}
		if (params.containsKey("day")) {
			codeSequence.setDay(params.get("day"));
		}
		codeSequence.setMinSequence(minSeq);
		codeSequence.setMaxSequence(maxSeq);
		codeSequence.setSeq(minSeq); // 设置起始的序列
		codeSequenceDao.addCodeSequence(codeSequence);
		if (codeSequence.getCodesequenceId() == 0)
			throw new WekitException("生成编码时发生意外请与管理员联系!");
		return codeSequence;
	}

	/**
	 * 判断传递的参数是否有效
	 * 
	 * @param codeRule
	 * @param unitCode
	 * @param locationCode
	 * @param docCode
	 * @return
	 */
	public String checkRule(String codeRule, String unitCode, String locationCode, String docCode) {
		String[] rules = codeRule.split("-");
		if (rules.length != 4)
			return null;
		if (!(rules[0].endsWith("x") || rules[0].equals(unitCode)))
			return null;
		if (!(rules[1].equals("xxx") || rules[1].equals(locationCode)))
			return null;
		if (!(rules[2].equals("xxx") || rules[2].equals(docCode)))
			return null;
		return rules[3];
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	@Override
	public boolean cancelCode(Long codeId, String creater, String createrid, String ip, String note) {
		Code temp = codeDao.getCode(codeId);
		return cancel(temp, creater, createrid, ip, note);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	@Override
	public boolean cancelCode(String code, String creater, String createrid, String ip, String note) {
		Code temp = codeDao.getCode(code);
		return cancel(temp, creater, createrid, ip, note);

	}

	protected boolean cancel(Code code, String creater, String createrid, String ip, String note) {
		if (code == null)
			throw new WekitException("该编码不存在");
		CodeRule codeRule = codeRuleDao.getCodeRule(code.getRuleName(), code.getRule());
		String oldinfo = null;
		try {
			if (codeRule != null) {
				TempCode tempCode = new TempCode(code.getRule(), creater, createrid, code.getUnitCode(), code.getLocationCode(), code.getDocCode(), 0, code.getCode(), note, code.getCreateTime(), code.getCodeName(), codeRule.getMinSequence(), codeRule.getMaxSequence());
				tempCodeDao.addTempCode(tempCode);
			}
			oldinfo = DataWrapUtil.ObjectToJson(code);
			codeDao.deleteCode(code);
			logger.info("creater:" + creater + "||createrid:" + createrid + "取消:编码 " + oldinfo);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new WekitException(e.getMessage());
		}
		return true;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Code> queryCodes(Map<String, String> map, IPaginable paginable) {
		return this.codeDao.queryCodes(map, paginable);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	@Override
	public boolean deleteCode(Long id, String creatername, String createrid, String ip) throws Exception {
		Code code = codeDao.getCode(id);
		if (code != null) {
			if (codeDao.deleteCode(code)) {
				logger.info(creatername + "(" + createrid + "-- ip:" + ip + ") 删除编码:" + DataWrapUtil.ObjectToJson(code));
			} else {
				throw new WekitException("删除编码失败!");
			}
		}
		return true;
	}

}
