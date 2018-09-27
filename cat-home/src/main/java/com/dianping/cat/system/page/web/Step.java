package com.dianping.cat.system.page.web;

import java.lang.reflect.Method;

import com.dianping.cat.Cat;

public class Step {

	private int m_pageid;

	private String m_page;

	private String m_step1;

	private String m_step2;

	private String m_step3;

	private String m_step4;

	private String m_step5;

	private String m_step6;

	private String m_step7;

	private String m_step8;

	private String m_step9;

	private String m_step10;

	private String m_step11;

	private String m_step12;

	private String m_step13;

	private String m_step14;

	private String m_step15;

	private String m_step16;

	private String m_step17;

	private String m_step18;

	private String m_step19;

	private String m_step20;

	private String m_step21;

	private String m_step22;

	private String m_step23;

	private String m_step24;

	private String m_step25;

	private String m_step26;

	private String m_step27;

	private String m_step28;

	private String m_step29;

	private String m_step30;

	private String m_step31;

	private String m_step32;

	public int getPageid() {
		return m_pageid;
	}

	public void setPageid(int pageid) {
		m_pageid = pageid;
	}

	public String getPage() {
		return m_page;
	}

	public void setPage(String page) {
		m_page = page;
	}

	public String getStep(int i) {
		try {
			Method getPage = this.getClass().getMethod("getStep" + i);
			return (String) getPage.invoke(this);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	public void setStep(int i, String step) {
		try {
			Method setPage = this.getClass().getMethod("setStep" + i, String.class);
			setPage.invoke(this, step);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public String getStep1() {
		return m_step1;
	}

	public void setStep1(String step1) {
		m_step1 = step1;
	}

	public String getStep2() {
		return m_step2;
	}

	public void setStep2(String step2) {
		m_step2 = step2;
	}

	public String getStep3() {
		return m_step3;
	}

	public void setStep3(String step3) {
		m_step3 = step3;
	}

	public String getStep4() {
		return m_step4;
	}

	public void setStep4(String step4) {
		m_step4 = step4;
	}

	public String getStep5() {
		return m_step5;
	}

	public void setStep5(String step5) {
		m_step5 = step5;
	}

	public String getStep6() {
		return m_step6;
	}

	public void setStep6(String step6) {
		m_step6 = step6;
	}

	public String getStep7() {
		return m_step7;
	}

	public void setStep7(String step7) {
		m_step7 = step7;
	}

	public String getStep8() {
		return m_step8;
	}

	public void setStep8(String step8) {
		m_step8 = step8;
	}

	public String getStep9() {
		return m_step9;
	}

	public void setStep9(String step9) {
		m_step9 = step9;
	}

	public String getStep10() {
		return m_step10;
	}

	public void setStep10(String step10) {
		m_step10 = step10;
	}

	public String getStep11() {
		return m_step11;
	}

	public void setStep11(String step11) {
		m_step11 = step11;
	}

	public String getStep12() {
		return m_step12;
	}

	public void setStep12(String step12) {
		m_step12 = step12;
	}

	public String getStep13() {
		return m_step13;
	}

	public void setStep13(String step13) {
		m_step13 = step13;
	}

	public String getStep14() {
		return m_step14;
	}

	public void setStep14(String step14) {
		m_step14 = step14;
	}

	public String getStep15() {
		return m_step15;
	}

	public void setStep15(String step15) {
		m_step15 = step15;
	}

	public String getStep16() {
		return m_step16;
	}

	public void setStep16(String step16) {
		m_step16 = step16;
	}

	public String getStep17() {
		return m_step17;
	}

	public void setStep17(String step17) {
		m_step17 = step17;
	}

	public String getStep18() {
		return m_step18;
	}

	public void setStep18(String step18) {
		m_step18 = step18;
	}

	public String getStep19() {
		return m_step19;
	}

	public void setStep19(String step19) {
		m_step19 = step19;
	}

	public String getStep20() {
		return m_step20;
	}

	public void setStep20(String step20) {
		m_step20 = step20;
	}

	public String getStep21() {
		return m_step21;
	}

	public void setStep21(String step21) {
		m_step21 = step21;
	}

	public String getStep22() {
		return m_step22;
	}

	public void setStep22(String step22) {
		m_step22 = step22;
	}

	public String getStep23() {
		return m_step23;
	}

	public void setStep23(String step23) {
		m_step23 = step23;
	}

	public String getStep24() {
		return m_step24;
	}

	public void setStep24(String step24) {
		m_step24 = step24;
	}

	public String getStep25() {
		return m_step25;
	}

	public void setStep25(String step25) {
		m_step25 = step25;
	}

	public String getStep26() {
		return m_step26;
	}

	public void setStep26(String step26) {
		m_step26 = step26;
	}

	public String getStep27() {
		return m_step27;
	}

	public void setStep27(String step27) {
		m_step27 = step27;
	}

	public String getStep28() {
		return m_step28;
	}

	public void setStep28(String step28) {
		m_step28 = step28;
	}

	public String getStep29() {
		return m_step29;
	}

	public void setStep29(String step29) {
		m_step29 = step29;
	}

	public String getStep30() {
		return m_step30;
	}

	public void setStep30(String step30) {
		m_step30 = step30;
	}

	public String getStep31() {
		return m_step31;
	}

	public void setStep31(String step31) {
		m_step31 = step31;
	}

	public String getStep32() {
		return m_step32;
	}

	public void setStep32(String step32) {
		m_step32 = step32;
	}

}
