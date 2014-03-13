package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.BufferWriter;
import com.dianping.cat.message.spi.core.HtmlEncodingBufferWriter;
import com.dianping.cat.message.spi.core.HtmlMessageCodec;
import com.dianping.cat.message.spi.core.WaterfallMessageCodec;

class CodecComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BufferWriter.class, HtmlEncodingBufferWriter.ID, HtmlEncodingBufferWriter.class));

		all.add(C(MessageCodec.class, HtmlMessageCodec.ID, HtmlMessageCodec.class) //
		      .req(BufferWriter.class, HtmlEncodingBufferWriter.ID));
		all.add(C(MessageCodec.class, WaterfallMessageCodec.ID, WaterfallMessageCodec.class) //
				.req(BufferWriter.class, HtmlEncodingBufferWriter.ID));

		return all;
	}
}
