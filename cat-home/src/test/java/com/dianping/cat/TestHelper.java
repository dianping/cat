package com.dianping.cat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

import junit.framework.Assert;

public class TestHelper {

	public static <T> void assertEquals(String expectedXml, T input) throws SAXException, IOException {
		Assert.assertTrue(isEquals(expectedXml, input));
	}

	public static <T> void assertEquals(String messaage, String expectedXml, T input) throws SAXException, IOException {
		Assert.assertTrue(messaage, isEquals(expectedXml, input));
	}

	public static <T> void assertEquals(T expected, String input) throws SAXException, IOException {
		Assert.assertTrue(isEquals(input, expected));
	}

	public static <T> void assertEquals(String messaage, T expected, String input) throws SAXException, IOException {
		Assert.assertTrue(messaage, isEquals(input, expected));
	}

	protected static InputSource toInputSource(File file) throws FileNotFoundException {
		return new InputSource(new FileInputStream(file));
	}

	public static boolean isXmlEquals(String xml1, String xml2) throws SAXException, IOException {
		// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// dbf.setNamespaceAware(true);
		// dbf.setCoalescing(true);
		// dbf.setIgnoringElementContentWhitespace(true);
		// dbf.setIgnoringComments(true);
		// DocumentBuilder db = dbf.newDocumentBuilder();
		//
		// Document doc1 = db.parse( new InputSource(new StringReader(xml1)));
		// doc1.normalizeDocument();
		//
		// Document doc2 = db.parse( new InputSource(new StringReader(xml2)));
		// doc2.normalizeDocument();
		// boolean isEquals = doc1.isEqualNode(doc2);
		// return isEquals;

		Diff diff = XMLUnit.compareXML(xml1, xml2);
		return diff.similar();
	}

	// public static boolean isEquals(String expected,IEntity input) throws SAXException, IOException {
	// IEntity report1 = parseEntity(input.getClass(),expected);
	// return isEquals(report1,input);
	// }
	//
	// public static boolean isEquals(String expected,EventReport input) throws SAXException, IOException {
	// EventReport report1 = parseEntity(EventReport.class,expected);
	// return isEquals(report1,input);
	// }
	public static <T> boolean isEquals(String expected, T input) throws SAXException, IOException {
		T report1 = parseEntity(input.getClass(), expected);
		return isEquals(report1, input);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends IEntity<?>> T parseEntity(Class type, String xml) throws SAXException, IOException {
		return (T) DefaultSaxParser.parseEntity(type, xml);
	}

	// public static boolean isEquals(TransactionReport expected,TransactionReport input) throws SAXException, IOException {
	// //return XmlHelper.isXmlEquals(expected.toString(), input.toString());
	// if( !expected.getDomain().equals(input.getDomain())) {
	// return false;
	// }
	// if (! expected.getStartTime().equals(input.getStartTime()) ) {
	// return false;
	// }
	// if (! expected.getEndTime().equals(input.getEndTime()) ) {
	// return false;
	// }
	//
	// if(!CollectionUtils.isEqualCollection(expected.getIps(), input.getIps())){
	// return false;
	// }
	//
	// Map<String, Machine> machines = expected.getMachines();
	// for (Map.Entry<String, Machine> entry : machines.entrySet()) {
	// String key = entry.getKey();
	// Machine m2 = input.findMachine(key);
	// if( m2 == null || ! isXmlEquals(entry.getValue().toString(),m2.toString())) {
	// return false;
	// }
	// }
	// return true;
	// }

	@SuppressWarnings("unchecked")
	public static boolean isEquals(Object expected, Object input) throws SAXException, IOException {
		// return XmlHelper.isXmlEquals(expected.toString(), input.toString());
		try {
			String domain1 = BeanUtils.getProperty(expected, "domain");
			String domain2 = BeanUtils.getProperty(input, "domain");
			if (domain1 == null || !domain1.equals(domain2)) {
				return false;
			}

			Object startTime1 = MethodUtils.invokeExactMethod(expected, "getStartTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			Object startTime2 = MethodUtils.invokeExactMethod(input, "getStartTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			if (!(startTime1 == null && startTime2 == null || startTime1.toString().equals(startTime2.toString()))) {
				return false;
			}

			Object endTime1 = MethodUtils.invokeExactMethod(expected, "getEndTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			Object endTime2 = MethodUtils.invokeExactMethod(input, "getEndTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			if (!((endTime1 == null && endTime2 == null) || endTime1.toString().equals(endTime2.toString()))) {
				return false;
			}

			// Collection<String> ips1 = (Collection<String>)MethodUtils.invokeExactMethod(expected, "getIps",
			// ArrayUtils.EMPTY_OBJECT_ARRAY);
			// Collection<String> ips2 = (Collection<String>) MethodUtils.invokeExactMethod(input, "getIps",
			// ArrayUtils.EMPTY_OBJECT_ARRAY);
			// if(!CollectionUtils.isEqualCollection(ips1, ips2)){
			// return false;
			// }

			Collection<String> ips1 = (Collection<String>) MethodUtils.invokeExactMethod(expected, "getDomainNames",
			      ArrayUtils.EMPTY_OBJECT_ARRAY);
			Collection<String> ips2 = (Collection<String>) MethodUtils.invokeExactMethod(input, "getDomainNames",
			      ArrayUtils.EMPTY_OBJECT_ARRAY);
			if (!CollectionUtils.isEqualCollection(ips1, ips2)) {
				return false;
			}

			Map<String, Object> machines = invokeExactMethod(expected, "getMachines");
			Map<String, Object> machines2 = invokeExactMethod(input, "getMachines");
			if (machines.size() != machines2.size()) {
				return false;
			}
			if (!CollectionUtils.isEqualCollection(machines.keySet(), machines2.keySet())) {
				return false;
			}
			for (Map.Entry<String, Object> entry : machines.entrySet()) {
				String key = entry.getKey();
				Object m2 = MethodUtils.invokeExactMethod(input, "findMachine", new String[] { key });
				Object m1 = entry.getValue();
				if (m2 == null || !isXmlEquals(m1.toString(), m2.toString())) {
					return false;
				}
			}

		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokeExactMethod(Object object, String methodName) throws NoSuchMethodException,
	      IllegalAccessException, InvocationTargetException {
		return (T) MethodUtils.invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
}
