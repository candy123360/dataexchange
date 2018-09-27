package cn.csservice.dataexchange.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.csservice.dataexchange.Entity.Sql;
import cn.csservice.dataexchange.service.InternalReceiveService;
import cn.csservice.dataexchange.tools.IpUtils;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/internal")
public class InternalReceiveController {
	private static Logger logger = Logger.getLogger(InternalReceiveController.class);
	@Autowired
	InternalReceiveService internalReceiveService;

	@RequestMapping("/receive")
	@ResponseBody
	public void receive(@RequestBody String sql) {
		System.out.println("sql=[" + sql + "]");
	}

	@RequestMapping(value = "/receiveJson", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public Object receiveJson(@RequestBody Sql sql) {
		JSONObject obj = new JSONObject();
		// 获取客户端ip，判断是否在信任列表中，如果不在，则不接收此sql
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String clientIp = IpUtils.getIpAddr(request);
		boolean bTrusted = internalReceiveService.isTrusted(clientIp);
		if (!bTrusted) {
			obj.put("state", "failure");
			obj.put("message", "客户端ip不在信任列表中，暂不接收此客户端发来的数据。");
			logger.error("客户端ip不在信任列表中，暂不接收此客户端发来的数据，客户端ip[" + clientIp + "]");
			return obj.toString();
		}
		boolean bReceivable = internalReceiveService.isReceivableTable(sql.getSql());
		if (!bReceivable) {
			obj.put("state", "failure");
			obj.put("message", "此sql涉及的表不在可接收表名列表中，暂不处理此数据。");
			logger.error("此sql涉及的表不在可接收表名列表中，暂不处理此数据，sql[" + sql.getSql() + "]");
			return obj.toString();
		}
		logger.info("接收到sql并执行[" + sql.getSql() + "]");
		obj.put("state", "success");
		obj.put("message", "成功接收客户端发来的数据，并执行其中的sql。");
		internalReceiveService.processSql(sql.getSql());
		return obj.toString();
	}
}
