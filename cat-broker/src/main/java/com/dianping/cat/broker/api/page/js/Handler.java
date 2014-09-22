package com.dianping.cat.broker.api.page.js;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class Handler implements PageHandler<Context> {
	@Inject
	private AggregationConfigManager m_manager;

	private static final String ACCESS = "DirectAccess";

	private String m_data;

	private String m_referer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "js")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "js")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		// long timestamp = payload.getTimestamp();
		long timestamp = System.currentTimeMillis();
		String error = payload.getError();
		String host = parseHost();
		String url = payload.getUrl();
		HttpServletResponse response = ctx.getHttpServletResponse();

		if (host.contains("dianping")) {
			if (url == null || url.length() == 0) {
				if (m_referer != null) {
					url = m_referer;
				} else {
					url = "unknown";
				}
			}

			int index = url.indexOf('?');
			if (index > -1) {
				url = url.substring(0, index);
			}
			Cat.logEvent("Error", parseUrl(url), "Error", error);
			Cat.logEvent("Agent", parseValue("Agent", m_data), Message.SUCCESS,
			      new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp)));

			MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();

			tree.setDomain(Constants.FRONT_END);
			tree.setHostName(host);
			tree.setIpAddress(host);
		}
		response.getWriter().write("OK");
	}

	private String parseUrl(String url) {
		String result = m_manager.handle(AggregationConfigManager.PROBLEM_TYPE, Constants.FRONT_END, url);

		if (result.equals(url)) {
			return subUrl(url);
		} else {
			return result;
		}
	}

	private String parseHost() {
		MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
		Message message = tree.getMessage();

		if (message.getType().equals("URL") && message instanceof Transaction) {
			Transaction t = (Transaction) message;
			List<Message> messages = t.getChildren();

			for (Message temp : messages) {
				String name = temp.getName();
				if (name.equals("URL.Server") || name.equals("ClientInfo")) {
					m_data = temp.getData().toString();
					m_referer = parseValue("Referer", m_data);

					if (m_referer != null) {
						try {
							URL u = new URL(m_referer);
							return u.getHost().toLowerCase();
						} catch (MalformedURLException e) {
							break;
						}
					}
				}
			}
		}
		return ACCESS;
	}

	public String parseValue(final String key, final String data) {
		int len = data == null ? 0 : data.length();
		int keyLen = key.length();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean inName = true;

		for (int i = 0; i < len; i++) {
			char ch = data.charAt(i);

			switch (ch) {
			case '&':
				if (name.length() == keyLen && name.toString().equals(key)) {
					return value.toString();
				}
				inName = true;
				name.setLength(0);
				value.setLength(0);
				break;
			case '=':
				if (inName) {
					inName = false;
				} else {
					value.append(ch);
				}
				break;
			default:
				if (inName) {
					name.append(ch);
				} else {
					value.append(ch);
				}
				break;
			}
		}

		if (name.length() == keyLen && name.toString().equals(key)) {
			return value.toString();
		}

		return null;
	}

	private String subUrl(String url) {
		String[] str = url.split("\\/");
		StringBuilder sb = new StringBuilder();

		if (str.length > 4) {
			for (int i = 0; i <= 4; i++) {
				sb.append(str[i]).append("/");
			}

			return sb.toString();
		} else {
			return url;
		}
	}
}