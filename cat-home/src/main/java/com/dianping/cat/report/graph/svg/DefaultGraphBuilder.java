/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.graph.svg;

import java.text.DecimalFormat;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = GraphBuilder.class)
public class DefaultGraphBuilder implements GraphBuilder {
	private static final int BAR = 1;

	private static final int LINE = 2;

	@Inject
	private ValueTranslater m_translater;

	private int m_type = BAR;

	@Override
	public String build(GraphPayload payload) {
		double[] values = payload.getValues();
		double maxValue = m_translater.getMaxValue(values);
		XmlBuilder b = new XmlBuilder();

		if (maxValue == 0) {
			maxValue = payload.getRows();
		}

		buildHeader(payload, b, maxValue);
		buildCoordinate(payload, b);
		buildYLabels(payload, b, maxValue);
		buildXLabels(payload, b);
		if (m_type == BAR) {
			buildBars(payload, b, maxValue, values);
		} else if (m_type == LINE) {
			buildLines(payload, b, maxValue, values);
		}
		buildFooter(payload, b);

		return b.getResult().toString();
	}

	protected void buildBars(GraphPayload payload, XmlBuilder b, double maxValue, double[] values) {
		DecimalFormat format = new DecimalFormat("0.#");
		int width = payload.getWidth();
		int height = payload.getHeight();
		int top = payload.getMarginTop();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int right = payload.getMarginRight();
		int h = height - top - bottom;
		int w = width - left - right;
		int cols = payload.getColumns();
		int xstep = w / cols;
		int[] pixels = m_translater.translate(h, maxValue, values);
		String idPrefix = payload.getIdPrefix();

		b.tag1("g", "id", "bar", "fill", "red");

		for (int i = 0; i < cols && i < pixels.length; i++) {
			int pixel = pixels[i];

			if (pixel <= 0) {
				continue;
			}

			int x = left + xstep * i;
			int y = top + h - pixel;

			b.tag("rect", "id", idPrefix + i, "x", x, "y", y, "width", xstep - 1, "height", pixel);
		}

		b.tag2("g");

		b.tag1("g", "id", "label");

		for (int i = 0; i < cols && i < pixels.length; i++) {
			int pixel = pixels[i];

			if (pixel <= 0) {
				continue;
			}

			double value = values[i];
			int x = left + xstep * i;
			int y = top - 6 + h - pixel;
			String tip = format.format(value);

			// adjust
			if (x + tip.length() * 7 > width - right) {
				x = width - right - tip.length() * 7;
			}

			b.tag1("text", "x", x, "y", y, "display", "none");

			b.indent().add(tip).newLine();
			b.tag("set", "attributeName", "display", "from", "none", "to", "block", "begin", idPrefix + i + ".mouseover",	"end",
									idPrefix + i + ".mouseout");
			b.tag2("text");
		}

		b.tag2("g");
	}

	protected void buildCoordinate(GraphPayload payload, XmlBuilder b) {
		int width = payload.getWidth();
		int height = payload.getHeight();
		int top = payload.getMarginTop();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int right = payload.getMarginRight();
		int h = height - top - bottom;
		int w = width - left - right;
		int rows = payload.getRows();
		int cols = payload.getColumns();
		int ystep = h / rows;
		int xstep = w / cols;
		PathBuilder p = new PathBuilder();

		b.tag1("g", "id", "coordinate", "stroke", "#003f7f", "fill", "white");
		b.tag("path", "id", "xy", "d", p.moveTo(left, top + h).h(w).m(-w, 0).v(-h).build());
		b.tag("path", "id", "xy-2", "d", p.moveTo(left, top).m(w, 0).v(h).build(), "stroke-dasharray", "1,5");
		b.tag("path", "id", "lines", "d", p.moveTo(left, top).mark().h(w).m(-w, ystep).repeat(rows - 1).build(),
								"stroke-dasharray", "1,5");

		if (rows >= 8) {
			p.moveTo(left, top).mark().h(-9).m(9, ystep).h(-5).m(5, ystep).repeat(rows / 2 - 1);

			if (rows % 2 == 0) {
				p.h(-9).m(9, ystep);
			}

			b.tag("path", "id", "ys", "d", p.build());
		} else {
			p.moveTo(left, top).mark().h(-7).m(7, ystep).repeat(rows);
			b.tag("path", "id", "ys", "d", p.build());
		}

		if (payload.isAxisXLabelSkipped()) {
			p.moveTo(left, top + h).mark().v(9).m(xstep, -9).v(5).m(xstep, -5).repeat(cols / 2 - 1);

			if (cols % 2 == 0) {
				p.v(9).m(xstep, -9);
			}

			b.tag("path", "id", "xs", "d", p.build());
		} else {
			p.moveTo(left, top + h).mark().v(7).m(xstep, -7).repeat(cols);
			b.tag("path", "id", "xs", "d", p.build());
		}

		b.tag2("g");
	}

	protected void buildFooter(GraphPayload payload, XmlBuilder b) {
		b.tag2("svg");
	}

	protected void buildHeader(GraphPayload payload, XmlBuilder b, double maxValue) {
		int offsetX = payload.getOffsetX();
		int offsetY = payload.getOffsetY();
		int height = payload.getHeight();
		int width = payload.getWidth();
		int top = payload.getMarginTop();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int right = payload.getMarginRight();

		if (payload.isStandalone()) {
			b.add("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\r\n");
		}

		b.tag1("svg", "x", offsetX, "y", offsetY, "width", payload.getDisplayWidth(), "height",	payload.getDisplayHeight(),
								"viewBox", "0,0," + width + "," + height, "xmlns", "http://www.w3.org/2000/svg");

		String title = payload.getTitle();

		if (title != null) {
			b.element("title", title);
		}

		if (payload.getDescription() != null) {
			b.element("description", payload.getDescription());
		}

		b.tag1("g");

		String axisXTitle = payload.getAxisXTitle();

		if (axisXTitle != null) {
			int x = (width - left - right - axisXTitle.length() * 9) / 2 + left;
			int y = height - 4;

			b.tagWithText("text", axisXTitle, "x", x, "y", y, "font-size", "18");
		}

		String axisYTitle = payload.getAxisYTitle();

		if (axisYTitle != null) {
			String maxLabel = toCompactString(maxValue);
			int x = left - 20 - maxLabel.length() * 9;
			int y = (height - top - bottom + axisYTitle.length() * 9) / 2 + top;
			String transform = "rotate(-90," + x + "," + y + ")";

			b.tagWithText("text", axisYTitle, "x", x, "y", y, "font-size", "18", "transform", transform);
		}

		if (title != null) {
			int x = (width - left - right - title.length() * 12) / 2 + left;
			int y = 24;
			b.tagWithText("text", title, "x", x, "y", y, "font-size", "24");
		}

		b.tag2("g");
	}

	protected void buildLines(GraphPayload payload, XmlBuilder b, double maxValue, double[] values) {
		DecimalFormat format = new DecimalFormat("0.#");
		int width = payload.getWidth();
		int height = payload.getHeight();
		int top = payload.getMarginTop();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int right = payload.getMarginRight();
		int h = height - top - bottom;
		int w = width - left - right;
		int cols = payload.getColumns();
		int xstep = w / cols;
		int[] pixels = m_translater.translate(h, maxValue, values);
		String idPrefix = payload.getIdPrefix();

		b.tag1("g", "id", "bar", "fill", "red");

		for (int i = 0; i < cols && i < pixels.length; i++) {
			int pixel = pixels[i];

			if (pixel <= 0) {
				continue;
			}

			int x = left + xstep * i;
			int y = top + h - pixel;

			b.tag("rect", "id", idPrefix + i, "x", x, "y", y, "width", xstep - 1, "height", pixel);
		}

		b.tag2("g");

		b.tag1("g", "id", "label");

		for (int i = 0; i < cols && i < pixels.length; i++) {
			int pixel = pixels[i];

			if (pixel <= 0) {
				continue;
			}

			double value = values[i];
			int x = left + xstep * i;
			int y = top - 6 + h - pixel;
			String tip = format.format(value);

			// adjust
			if (x + tip.length() * 7 > width - right) {
				x = width - right - tip.length() * 7;
			}

			b.tag1("text", "x", x, "y", y, "display", "none");

			b.indent().add(tip).newLine();
			b.tag("set", "attributeName", "display", "from", "none", "to", "block", "begin", idPrefix + i + ".mouseover",	"end",
									idPrefix + i + ".mouseout");
			b.tag2("text");
		}

		b.tag2("g");
	}

	protected void buildXLabels(GraphPayload payload, XmlBuilder b) {
		int height = payload.getHeight();
		int width = payload.getWidth();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int right = payload.getMarginRight();
		int cols = payload.getColumns();
		int w = width - left - right;
		int xstep = w / cols;

		b.tag1("g", "id", "xt");
		boolean rotated = payload.isAxisXLabelRotated();
		boolean skipped = payload.isAxisXLabelSkipped();

		for (int i = 0; i <= cols; ) {
			int x = left + xstep * i - 4;
			int y = height - bottom + 20 + (skipped ? 2 : 0);
			String label = payload.getAxisXLabel(i);

			if (!rotated) {
				if (label.length() > 1) {
					x -= 4 * (label.length() - 1);
				}
			} else {
				y -= 10;
			}

			if (rotated) {
				String transform = "rotate(90," + x + "," + y + ")";

				b.tagWithText("text", label, "x", x, "y", y, "font-size", "14", "transform", transform);
			} else {
				b.tagWithText("text", label, "x", x, "y", y, "font-size", "14");
			}

			if (skipped) {
				i += 2;
			} else {
				i++;
			}
		}

		b.tag2("g");
	}

	protected void buildYLabels(GraphPayload payload, XmlBuilder b, double maxValue) {
		int height = payload.getHeight();
		int top = payload.getMarginTop();
		int left = payload.getMarginLeft();
		int bottom = payload.getMarginBottom();
		int h = height - top - bottom;
		int rows = payload.getRows();
		int ystep = h / rows;

		b.tag1("g", "id", "yt", "direction", "rtl");

		if (rows >= 8) {
			for (int i = 0; i < rows; i += 2) {
				int x = left - 12;
				int y = top + 4 + ystep * i;

				b.tagWithText("text", toCompactString(maxValue - maxValue / rows * i), "x", x, "y", y, "font-size", "14");
			}
		} else {
			for (int i = 0; i < rows; i++) {
				int x = left - 9;
				int y = top + 4 + ystep * i;

				b.tagWithText("text", toCompactString(maxValue - maxValue / rows * i), "x", x, "y", y, "font-size", "14");
			}
		}

		b.tag2("g");
	}

	@Override
	public void setGraphType(int GraphType) {
		// TODO Auto-generated method stub
	}

	private String toCompactString(double value) {
		return new DecimalFormat("0.##").format(value);
	}

	protected static class PathBuilder {
		private int m_marker;

		private StringBuilder m_sb = new StringBuilder(64);

		public String build() {
			String result = m_sb.toString();

			m_sb.setLength(0);
			return result;
		}

		public PathBuilder h(int deltaX) {
			m_sb.append(" h").append(deltaX);
			return this;
		}

		public PathBuilder m(int deltaX, int deltaY) {
			m_sb.append(" m").append(deltaX).append(',').append(deltaY);
			return this;
		}

		public PathBuilder mark() {
			m_marker = m_sb.length();
			return this;
		}

		public PathBuilder moveTo(int x, int y) {
			m_sb.append('M').append(x).append(',').append(y);
			return this;
		}

		public PathBuilder repeat(int count) {
			int pos = m_sb.length();

			for (int i = 0; i < count; i++) {
				m_sb.append(m_sb.subSequence(m_marker, pos));
			}

			return this;
		}

		public PathBuilder v(int deltaY) {
			m_sb.append(" v").append(deltaY);
			return this;
		}
	}

	protected static class XmlBuilder {
		private boolean m_compact;

		private int m_level;

		private StringBuilder m_sb = new StringBuilder(8192);

		public XmlBuilder add(String text) {
			m_sb.append(text);
			return this;
		}

		public XmlBuilder element(String name, String value) {
			indent();
			m_sb.append('<').append(name).append('>');
			m_sb.append(value);
			m_sb.append("</").append(name).append(">");
			newLine();
			return this;
		}

		public StringBuilder getResult() {
			return m_sb;
		}

		public XmlBuilder indent() {
			if (!m_compact) {
				for (int i = m_level - 1; i >= 0; i--) {
					m_sb.append("  ");
				}
			}

			return this;
		}

		public XmlBuilder newLine() {
			m_sb.append("\r\n");
			return this;
		}

		public XmlBuilder tag(String name, Object... attributes) {
			return tagWithText(name, null, attributes);
		}

		public XmlBuilder tag1(String name, Object... attributes) {
			indent();

			m_sb.append('<').append(name);

			int len = attributes.length;
			for (int i = 0; i < len; i += 2) {
				Object key = attributes[i];
				Object val = attributes[i + 1];

				if (val != null) {
					m_sb.append(' ').append(key).append("=\"").append(val).append('"');
				}
			}

			m_sb.append(">");
			newLine();
			m_level++;
			return this;
		}

		public XmlBuilder tag2(String name) {
			m_level--;
			indent();
			m_sb.append("</").append(name).append(">");
			newLine();
			return this;
		}

		public XmlBuilder tagWithText(String name, Object text, Object... attributes) {
			indent();

			m_sb.append('<').append(name);

			int len = attributes.length;
			for (int i = 0; i < len; i += 2) {
				Object key = attributes[i];
				Object val = attributes[i + 1];

				if (val != null) {
					m_sb.append(' ').append(key).append("=\"").append(val).append('"');
				}
			}

			if (text == null) {
				m_sb.append("/>");
			} else {
				m_sb.append('>').append(text).append("</").append(name).append('>');
			}

			newLine();
			return this;
		}
	}
}
