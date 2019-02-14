package com.dianping.cat.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

import junit.framework.Assert;

public class TestHelper {
	/**
	 * Logger for this class
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TestHelper.class.getName());

	// public static void main(String[] args) throws Exception {
	// Diff diff = XMLUnit.compareXML(toInputSource(new File("R:/t1.xml")), toInputSource(new File("R:/t2.xml")));
	//
	// diff.overrideElementQualifier(new ElementNameAndTextQualifier());
	//
	// System.out.println("diff1:"+diff);
	// System.out.println(diff.similar());
	//
	//
	// }
	public static <T> void assertEquals(String expectedXml, T input) throws SAXException, IOException {
		Assert.assertTrue(isEquals(expectedXml, input));
	}

	public static <T> void assertEquals(String messaage, String expectedXml, T input) throws SAXException, IOException {
		Assert.assertTrue(messaage, isEquals(expectedXml, input));
	}

	public static <T> void assertEquals(T input, String expectedXml) throws SAXException, IOException {
		Assert.assertTrue(isEquals(expectedXml, input));
	}

	public static <T> void assertEquals(String messaage, T input, String expectedXml) throws SAXException, IOException {
		Assert.assertTrue(messaage, isEquals(expectedXml, input));
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
		boolean similar = diff.similar();
		if (!similar) {
			LOG.info("diff:{}", diff);
		}
		return similar;
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
			if (startTime1 == null || startTime2 == null || !startTime1.toString().equals(startTime2.toString())) {
				return false;
			}

			Object endTime1 = MethodUtils.invokeExactMethod(expected, "getEndTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			Object endTime2 = MethodUtils.invokeExactMethod(input, "getEndTime", ArrayUtils.EMPTY_OBJECT_ARRAY);
			if (endTime1 == null || endTime2 == null || !endTime1.toString().equals(endTime2.toString())) {
				return false;
			}

			// Collection<String> ips1 = (Collection<String>)MethodUtils.invokeExactMethod(expected, "getIps",
			// ArrayUtils.EMPTY_OBJECT_ARRAY);
			// Collection<String> ips2 = (Collection<String>) MethodUtils.invokeExactMethod(input, "getIps",
			// ArrayUtils.EMPTY_OBJECT_ARRAY);
			// if(!CollectionUtils.isEqualCollection(ips1, ips2)){
			// return false;
			// }
			String childKey = "Machine";
			if (expected.getClass().getSimpleName().equals("MatrixReport")) {
				childKey = "Matrix";
			}

			String getKey = "get" + childKey + "s";
			Map<String, Object> machines = invokeExactMethod(expected, getKey);
			Map<String, Object> machines2 = invokeExactMethod(input, getKey);
			if (machines.size() != machines2.size()) {
				return false;
			}
			if (!CollectionUtils.isEqualCollection(machines.keySet(), machines2.keySet())) {
				return false;
			}
			String findKey = "find" + childKey;

			for (Map.Entry<String, Object> entry : machines.entrySet()) {
				String key = entry.getKey();
				Object m2 = MethodUtils.invokeExactMethod(input, findKey, new String[] { key });
				if (m2 == null) {
					return false;
				}
				Object m1 = entry.getValue();
				if (m1 instanceof com.dianping.cat.consumer.problem.model.entity.Machine) {
					if (!isEqualsProblemMachine((com.dianping.cat.consumer.problem.model.entity.Machine) m1,
					      (com.dianping.cat.consumer.problem.model.entity.Machine) m2)) {
						return false;
					}
				} else if (m1 instanceof com.dianping.cat.consumer.event.model.entity.Machine) {

					if (!isEqualsEventMachine((com.dianping.cat.consumer.event.model.entity.Machine) m1,
					      (com.dianping.cat.consumer.event.model.entity.Machine) m2)) {
						return false;
					}
				} else if (m1 instanceof com.dianping.cat.consumer.transaction.model.entity.Machine) {
					if (!isEquals4transactionMachine((com.dianping.cat.consumer.transaction.model.entity.Machine) m1,
					      (com.dianping.cat.consumer.transaction.model.entity.Machine) m2)) {
						return false;
					}

				} else if (m1 instanceof Matrix ? !isEqualsMatrix((Matrix) m1, (Matrix) m2) : !isXmlEquals(m1.toString(),
				      m2.toString())) {
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

	public static boolean isEqualsMatrix(Matrix a, Matrix b) throws SAXException, IOException {
		if (a.getCount() != b.getCount()) {
			return false;
		}

		if (!a.getName().equals(b.getName())) {
			return false;
		}

		if (a.getTotalTime() != b.getTotalTime()) {
			return false;
		}

		if (!a.getType().equals(b.getType())) {
			return false;
		}
		if (!a.getUrl().equals(b.getUrl())) {
			return false;
		}
		Map<String, Ratio> ratios1 = a.getRatios();

		Map<String, Ratio> ratios2 = b.getRatios();
		if (!CollectionUtils.isEqualCollection(ratios1.keySet(), ratios2.keySet())) {
			return false;
		}
		for (Map.Entry<String, Ratio> entry : ratios1.entrySet()) {
			String key = entry.getKey();
			Ratio m2 = ratios2.get(key);
			if (m2 == null) {
				return false;
			}
			Ratio m1 = entry.getValue();
			if (!isXmlEquals(m1.toString(), m2.toString())) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unused")
   public static boolean isEqualsProblemMachine(com.dianping.cat.consumer.problem.model.entity.Machine a,
	      com.dianping.cat.consumer.problem.model.entity.Machine b) throws SAXException, IOException {

		if (!a.getIp().equals(b.getIp())) {
			return false;
		}

		Map<String, Entity> ratios1 = a.getEntities();
		Map<String, Entity> ratios2 = b.getEntities();
		if (false && !CollectionUtils.isEqualCollection(ratios1.keySet(), ratios2.keySet())) {
			return false;
		}
		for (Map.Entry<String, Entity> entry : ratios1.entrySet()) {
			String key = entry.getKey();
			Entity m2 = ratios2.get(key);
			if (m2 == null) {
				return false;
			}
			Entity m1 = entry.getValue();
			if (!isXmlEquals(m1.toString(), m2.toString())) {
				return false;
			}
		}

		return true;
	}

	public static boolean isEquals4transactionMachine(com.dianping.cat.consumer.transaction.model.entity.Machine a,
	      com.dianping.cat.consumer.transaction.model.entity.Machine b) throws SAXException, IOException {

		if (!a.getIp().equals(b.getIp())) {
			return false;
		}

		Map<String, TransactionType> ratios1 = a.getTypes();
		Map<String, TransactionType> ratios2 = b.getTypes();
		if (!CollectionUtils.isEqualCollection(ratios1.keySet(), ratios2.keySet())) {
			return false;
		}
		for (Map.Entry<String, TransactionType> entry : ratios1.entrySet()) {
			String key = entry.getKey();
			TransactionType m2 = ratios2.get(key);
			if (m2 == null) {
				return false;
			}
			TransactionType m1 = entry.getValue();

			Map<String, TransactionName> names1 = m1.getNames();
			Map<String, TransactionName> names2 = m2.getNames();
			if (!CollectionUtils.isEqualCollection(names1.keySet(), names2.keySet())) {
				return false;
			}
			for (Map.Entry<String, TransactionName> entry1 : names1.entrySet()) {
				TransactionName t1 = entry1.getValue();
				TransactionName t2 = names2.get(entry1.getKey());
				if (!isXmlEquals(t1.toString(), t2.toString())) {
					return false;
				}
			}
			names1.clear();
			names2.clear();
			m1.getRange2s().clear();
			m2.getRange2s().clear();
			if (!isXmlEquals(m1.toString(), m2.toString())) {
				return false;
			}
		}

		return true;
	}

	public static boolean isEqualsEventMachine(com.dianping.cat.consumer.event.model.entity.Machine a,
	      com.dianping.cat.consumer.event.model.entity.Machine b) throws SAXException, IOException {

		if (!a.getIp().equals(b.getIp())) {
			return false;
		}

		Map<String, EventType> ratios1 = a.getTypes();

		for (Map.Entry<String, EventType> entry : ratios1.entrySet()) {
			EventType m2 = entry.getValue();
			if (m2 == null) {
				return false;
			}
			EventType m1 = entry.getValue();
			if (!isXmlEquals(m1.toString(), m2.toString())) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokeExactMethod(Object object, String methodName) throws NoSuchMethodException,
	      IllegalAccessException, InvocationTargetException {
		return (T) MethodUtils.invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
}
