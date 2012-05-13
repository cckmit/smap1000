package org.wekit.web;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.wekit.security.RC4CipherEntity;
import org.wekit.web.db.Pagination;
import org.wekit.web.db.model.RemoteAcl;
import org.wekit.web.service.LogsService;
import org.wekit.web.service.RemoteAclService;
import org.wekit.web.util.DataWrapUtil;

/**
 * 动作函数的基础类，定义了一些常用的方法
 * 
 * @author HuangWeili
 * 
 */
public abstract class BaseController<T> {

	public final static String		IP				= "ip";

	public final static String		CODE			= "code";

	public final static String		DOCCODE			= "doc";

	public final static String		LOCATIONCODE	= "location";

	public final static String		UNITCODE		= "unit";

	public final static String		CREATER			= "creatername";

	public final static String		CREATERID		= "createrid";

	public final static String		PAGENO			= "pageno";

	public final static String		PAGESIZE		= "pagesize";

	public final static String		BATCHSIZE		= "size";

	public final static String		NOTE			= "note";

	public final static String		USERKEY			= "u";

	public final static String		USERPARAMS		= "p";

	public final static String		KEY				= "key";

	public final static String		STATE			= "state";

	public final static String		TYPEID			= "typeid";

	public final static String		RULENAME		= "rulename";

	public final static String		TOKEN			= "t";

	public final static String		FILENAME		= "filename";

	public final static String		RULE			= "rule";

	public final static String		RULEID			= "ruleid";
	
	public final static String 		CODENAME		="codename";

	protected String				userkey			= "";

	protected String				userparams		= "";

	protected String				creatername		= "";

	protected String				createrid		= "";

	protected String				ip				= "";

	protected String				code			= "";

	protected long					typeId			= 0;

	protected String				docCode			= "";

	protected String				locationCode	= "";

	protected String				unitCode		= "";

	protected int					batchSize		= 0;

	protected String				note			= "";

	protected String				key				= "";

	protected String				token			= "";

	protected String				rulename		= "";

	protected long					ruleId			= 0;

	protected int					state			= -1;

	protected String				filename		= "";

	protected String				rule			= "";
	
	protected String 				codeName="";
	
	@Autowired
	@Qualifier("remoteAclService")
	protected RemoteAclService		remoteAclService;									// 注入远程访问接口

	@Autowired
	@Qualifier("logService")
	protected LogsService			logsService;
	protected Pagination<T>			pagination		= new Pagination<T>();
	protected Map<String, String>	parameters		= new HashMap<String, String>();
	protected RemoteAcl				remoteAcl;

	/**
	 * 获取分页信息
	 * 
	 * @param request
	 * @return
	 */
	protected void paserPaginable() {
		if (parameters.containsKey(PAGENO)) {
			this.pagination.setCurrentPage(Integer.parseInt(parameters.get(PAGENO)));
		}
		if (parameters.containsKey(PAGESIZE)) {
			this.pagination.setPageSize(Integer.parseInt(parameters.get(PAGESIZE)));
		}
	}

	/**
	 * 获取传过来的参数
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected void initParam(HttpServletRequest request, String operate) throws Exception {
		// this.userparams = decode(request,operate);
		this.userparams = URLDecoder.decode(request.getQueryString(), "UTF-8");
		logsService.addRemoteLog(remoteAcl.getAclId(), remoteAcl.getUsername(), operate, this.userparams);// 远程操作日志
		String[] params = this.userparams.split("&");
		if (params != null) {
			for (String param : params) {
				String[] entrys = param.split("=");
				if (entrys.length == 2) {
					this.parameters.put(entrys[0], entrys[1]);
				}
			}
		}
		if (this.parameters.containsKey("t")) {
			long time = Long.parseLong(this.parameters.get("t"));

		} else {
			throw new WekitException("传入的参数错误!");
		}

		paserPaginable();
		initCommonParam();
	}

	/**
	 * 编码解码工作
	 * 
	 * @param request
	 * @return
	 */
	private String decode(HttpServletRequest request, String operate) {
		if (StringUtils.isEmpty(request.getParameter(USERKEY))) {
			throw new WekitException("请输入用户信息");
		}
		this.userkey = request.getParameter(USERKEY);
		if (StringUtils.isEmpty(request.getParameter(USERPARAMS))) {
			throw new WekitException("请输入请求参数");
		}
		remoteAcl = remoteAclService.getRemoteAclByUserName(userkey);
		if (remoteAcl == null)
			throw new WekitException("不存在该授权用户");
		String password = remoteAcl.getPassword();
		String temp = request.getParameter(USERPARAMS).trim();
		temp = request.getParameter(USERPARAMS).trim();
		temp = new String(Base64.decodeBase64(temp));
		temp = RC4CipherEntity.code(temp, password);
		this.userparams = temp;
		return this.userparams;
	}

	protected void initCommonParam() {
		if (parameters.containsKey(CREATER)) {
			this.creatername = parameters.get(CREATER);
		}
		if (parameters.containsKey(CREATERID)) {
			this.createrid = parameters.get(CREATERID);
		}
		if (parameters.containsKey(IP)) {
			this.ip = parameters.get(IP);
		}
		if (parameters.containsKey(CODE)) {
			this.code = parameters.get(CODE);
		}
		if (parameters.containsKey(DOCCODE)) {
			this.docCode = parameters.get(DOCCODE);
		}
		if (parameters.containsKey(LOCATIONCODE)) {
			this.locationCode = parameters.get(LOCATIONCODE);
		}
		
		if (parameters.containsKey(UNITCODE)) {
			this.unitCode = parameters.get(UNITCODE);
		}
		
		if (parameters.containsKey(BATCHSIZE)) {
			try {
				this.batchSize = Integer.parseInt(parameters.get(BATCHSIZE));
			} catch (Exception ex) {
				throw new WekitException("size参数的范围必须是大于零的整数!");
			}
		}
		
		if (parameters.containsKey(NOTE)) {
			this.note = parameters.get(NOTE);
		}
		
		if (parameters.containsKey(KEY)) {
			this.key = parameters.get(KEY);
		}
		
		if (parameters.containsKey(RULENAME))
			this.rulename = parameters.get(RULENAME);
		
		if (parameters.containsKey(FILENAME))
			this.filename = parameters.get(FILENAME);
		
		if(parameters.containsKey(RULEID)){
			try{
				this.ruleId=Long.parseLong(parameters.get(RULEID));
			}catch(Exception ex){
				throw new WekitException("ruleId参数的类型不正确!");
			}
		}
		
		if (parameters.containsKey(STATE)) {
			try {
				this.state = Integer.parseInt(parameters.get(STATE));
			} catch (Exception ex) {
				throw new WekitException("state的参数范围必须是自然数!");
			}
		}
		if (parameters.containsKey(TYPEID)) {
			try {
				this.typeId = Long.parseLong(parameters.get(TYPEID));
			} catch (Exception ex) {
				throw new WekitException("typeid的参数范围必须是自然数!");
			}

		}
		if (parameters.containsKey(RULE)) {
			this.rule = parameters.get(RULE);
		}
		if(parameters.containsKey(CODENAME)){
			this.codeName=parameters.get(CODENAME);
		}
	}

	protected void addData(T data) {
		pagination.getDatas().add(data);
	}

	protected void setState(int state) {
		this.pagination.setState(state);
	}

	protected void setMessage(String message) {
		this.pagination.setMessage(message);
	}

	protected String displayAPIClient(String extend, Model model) {
		try {
			model.addAttribute("content", DataWrapUtil.convertObject(pagination, extend));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "ftl/" + extend;
	}

	protected void setDatas(List<T> datas) {
		this.pagination.setDatas(datas);
	}

}
