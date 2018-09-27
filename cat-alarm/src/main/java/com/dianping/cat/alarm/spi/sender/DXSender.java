package com.dianping.cat.alarm.spi.sender;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.dx.Pusher;
import com.dianping.cat.alarm.spi.dx.PusherBuilder;

public class DXSender extends AbstractSender implements Initializable {

	private Pusher m_pusher;

	public static final String ID = AlertChannel.DX.getName();

	public static final String APP_ID = "1";

	public static final String PUB_ID = "137438953912";

	public static final String APP_KEY = "261113r52017m011";

	public static final String APP_TOKEN = "90cae3c52970bb90a0e52b8a4b06c649";

	public static final String SENDER = "实时监控CAT";

	public static final String URL = "http://dxw-in.sankuai.com/api/pub/push";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		try {
			List<String> receivers = message.getReceivers();
			String content = message.getTitle() + "\n" + message.getContent();

			m_pusher.push(content, receivers.toArray(new String[receivers.size()]));
			return true;
		} catch (Exception e) {
			m_logger.error(message.toString(), e);
			return false;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_pusher = PusherBuilder.defaultBuilder().withAppkey(APP_KEY).withApptoken(APP_TOKEN).withTargetUrl(URL)
		      .withFromUid(Long.parseLong(PUB_ID)).withFromName(SENDER).withToAppid(Short.parseShort(APP_ID)).build();
	}

}
