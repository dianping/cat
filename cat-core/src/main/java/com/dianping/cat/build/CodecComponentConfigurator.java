package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.BufferWriter;
import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.HtmlEncodingBufferWriter;
import com.dianping.cat.message.spi.codec.HtmlMessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class CodecComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BufferWriter.class, EscapingBufferWriter.ID, EscapingBufferWriter.class));
		all.add(C(BufferWriter.class, HtmlEncodingBufferWriter.ID, HtmlEncodingBufferWriter.class));

		all.add(C(MessageCodec.class, PlainTextMessageCodec.ID, PlainTextMessageCodec.class) //
		      .req(BufferWriter.class, EscapingBufferWriter.ID));
		all.add(C(MessageCodec.class, HtmlMessageCodec.ID, HtmlMessageCodec.class) //
		      .req(BufferWriter.class, HtmlEncodingBufferWriter.ID));

		return all;
	}
}
