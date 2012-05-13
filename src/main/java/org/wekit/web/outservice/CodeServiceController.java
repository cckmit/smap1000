package org.wekit.web.outservice;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wekit.web.BaseController;
import org.wekit.web.WekitException;
import org.wekit.web.db.model.Code;
import org.wekit.web.service.CodeService;

@Controller("codeServiceController")
@Scope(org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST)
public class CodeServiceController extends BaseController<Code> {

	@Autowired
	@Qualifier("codeService")
	CodeService				codeService;

	private static Logger	logger	= Logger.getLogger(CodeServiceController.class);	// 日志记录

	/**
	 * 查询已有编码
	 * 
	 * @param extend
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/query.{extend}", method = RequestMethod.GET)
	public String queryCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"查询编码");

			// TODO
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			setState(0);
			setMessage(ex.getMessage());
		}
		return this.displayAPIClient(extend, model);
	}

	/**
	 * 更新编码状态
	 * 
	 * @param extend
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/update.{extend}", method = RequestMethod.POST)
	public String updateCode(@PathVariable("extned") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"更新编码");
			// TODO
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			setState(0);
			setMessage(ex.getMessage());
		}

		return this.displayAPIClient(extend, model);
	}

	/**
	 * 删除编码
	 * 
	 * @param extend
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/delete.{extend}", method = RequestMethod.POST)
	public String deleteCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"删除编码");
			if (StringUtils.isEmpty(key)) {
				throw new WekitException("key参数的值不能为空!");
			}
			this.codeService.deleteCode(Long.parseLong(key), creatername, createrid, ip);
		} catch (Exception e) {
			logger.error(e.getMessage());
			setState(0);
			setMessage(e.getMessage());
		}
		return this.displayAPIClient(extend, model);
	}

	/**
	 * 根据编码ID返回编码信息
	 * 
	 * @param extend
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/find.{extend}", method = RequestMethod.GET)
	public String findCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"查询编码");
			if (StringUtils.isEmpty(key)) {
				throw new WekitException("key的参数不能为空!");
			}
			Code code = this.codeService.getCode(Long.parseLong(key));
			this.addData(code);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			setState(0);
			setMessage(ex.getMessage());
		}
		return this.displayAPIClient(extend, model);
	}

	/**
	 * 创建编码
	 * 
	 * @param extend
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/add.{extend}", method = RequestMethod.GET)
	public String addCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"添加编码");
			// TODO
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			setState(0);
			setMessage(ex.getMessage());
		}
		return this.displayAPIClient(extend, model);
	}

	/**
	 * 获取编码接口
	 * @param extend
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/code/fetch.{extend}", method = RequestMethod.GET)
	public String fetchCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"获取编码");
			this.fetchCode(true);
		} catch (Exception ex) {
			logger.error(ex);
			pagination.setState(0);
			pagination.setMessage(ex.getMessage());
		}
		return displayAPIClient(extend, model);
	}

	@RequestMapping(value = "/code/batch.{extend}", method = RequestMethod.GET)
	public String batchCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {

		try {
			initParam(request,"获取批量编码");
			this.fetchCode(false);
		} catch (Exception ex) {
			logger.error(ex);
			pagination.setState(0);
			pagination.setMessage(ex.getMessage());
		}
		return displayAPIClient(extend, model);
	}

	@RequestMapping(value = "/code/cancel.{extend}", method = RequestMethod.GET)
	public String cancelCode(@PathVariable("extend") String extend, HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			initParam(request,"撤销编码");
			String code = null;
			String creater = null;
			String createrid = null;
			String ip = "";
			String note = "";
			if (parameters.containsKey("code")) {
				code = parameters.get("code");
			}
			if (parameters.containsKey("creater")) {
				creater = parameters.get("creater");
			}
			if (parameters.containsKey("createrid")) {
				createrid = parameters.get("createrid");
			}
			if (parameters.containsKey("ip")) {
				ip = parameters.get("ip");
			}
			if (parameters.containsKey("note")) {
				note = parameters.get(note);
			}
			if (StringUtils.isEmpty(code) || StringUtils.isEmpty(createrid) || StringUtils.isEmpty(creater)) {
				pagination.setState(0);
				pagination.setMessage("传入的参数错误，请检测参数是否正确!");
			} else {

				this.codeService.cancelCode(code, creater, createrid, ip, note);
			}
		} catch (Exception ex) {
			pagination.setState(0);
			pagination.setMessage(ex.getMessage());
		}
		return displayAPIClient(extend, model);
	}
	
	@RequestMapping(value="/extendcode/import.{extend}")
	public String importcode(@PathVariable("extend")String extend,HttpServletRequest request,HttpServletResponse response,Model model){
		try {
			initParam(request,"导入编码");
			
		} catch (Exception ex) {
			pagination.setState(0);
			pagination.setMessage(ex.getMessage());
		}
		return displayAPIClient(extend, model);
	}
	
	
	

	/**
	 * 取号逻辑编写
	 */
	protected void fetchCode(boolean isFetch) {
		if (StringUtils.isEmpty(this.unitCode)||StringUtils.isEmpty(this.locationCode) || StringUtils.isEmpty(this.docCode) || StringUtils.isEmpty(this.createrid) || (this.batchSize == 0 && isFetch == false)) {
			throw new WekitException("传入的参数错误，请检测参数!");
		}
		List<Code> list = null;
		if (isFetch) {
			list = new ArrayList<Code>();
			list.add(this.codeService.fetchCode(this.ruleId, this.unitCode, this.locationCode, this.docCode, this.createrid, this.note,this.filename,this.codeName));
		} else {
			list = this.codeService.batchCode(this.ruleId, this.unitCode, this.locationCode, this.docCode, this.createrid, this.note, this.batchSize,this.filename,this.codeName);
		}
		this.pagination.setDatas(list);
	}

}
