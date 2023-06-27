package com.dianping.cat.alarm.spi.dx.vo;


/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class TextMessage implements XBody {
	private String text;

	private String fontName;

	private int fontSize;

	private boolean bold;

	private short cipherType;

	public TextMessage() {
	}

	public TextMessage(String text, String fontName, int fontSize, boolean bold, short cipherType) {
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.bold = bold;
		this.cipherType = cipherType;
	}

	public String messageType() {
		return MessageType.text.name();
	}

	public boolean checkElementsNotNull() {
		return text != null;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean getBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public short getCipherType() {
		return cipherType;
	}

	public void setCipherType(short cipherType) {
		this.cipherType = cipherType;
	}

	/**
	 * 加密类型
	 */
	public static class CipherType {
		public static final short NO_CIPHER = 0;

		public static final short AES = 1;

		public static final short RSA = 2;

		public static final short RC4 = 3;

		public static final short BASE64 = 4;
	}
}
