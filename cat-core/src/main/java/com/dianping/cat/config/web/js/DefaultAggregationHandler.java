package com.dianping.cat.config.web.js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.AggregationMessageFormat;
import com.dianping.cat.config.CompositeFormat;
import com.dianping.cat.config.TrieTreeNode;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;

@Named(type=AggregationHandler.class)
public class DefaultAggregationHandler implements AggregationHandler, LogEnabled {

	private Map<Integer, Map<String, TrieTreeNode>> m_formats = new HashMap<Integer, Map<String, TrieTreeNode>>();

	protected Logger m_logger;

	private void buildFormatTree(TrieTreeNode tree, char[] prefix, char[] suffix, AggregationMessageFormat format) {
		if (prefix.length == 0 && suffix.length == 0) {
			tree.addFormat("", format);
			return;
		}
		boolean isPrefix = true;
		int sIndex = 0, eIndex = 0;
		TrieTreeNode sCurrent = tree, eCurrent = tree;
		if (prefix.length != 0) {
			for (int i = 0; i < prefix.length; i++) {
				TrieTreeNode node = sCurrent.getChildNode(prefix[i], true);
				if (node == null) {
					node = new TrieTreeNode();
					sCurrent.addTreeNode(prefix[i], true, node);
					sIndex++;
				}
				sCurrent = node;
			}
		}
		if (suffix.length != 0) {
			for (int i = suffix.length - 1; i >= 0; i--) {
				TrieTreeNode node = eCurrent.getChildNode(suffix[i], false);
				if (node == null) {
					node = new TrieTreeNode();
					eCurrent.addTreeNode(suffix[i], false, node);
					eIndex++;
				}
				eCurrent = node;
			}
		}
		// choose prefix or suffix as trieTree index based on size of tree leaf
		if (sIndex > eIndex) {
			isPrefix = true;
		} else if (sIndex < eIndex) {
			isPrefix = false;
		} else {
			isPrefix = sCurrent.getFormatMap().size() >= eCurrent.getFormatMap().size() ? false : true;
		}
		if (isPrefix) {
			sCurrent.addFormat(String.copyValueOf(suffix), format);
		} else {
			eCurrent.addFormat(String.copyValueOf(prefix), format);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private TrieTreeNode findOrCreateTrieTreeNode(int type, String domain) {
		Map<String, TrieTreeNode> nodes = findOrCreateTrieTreeNodes(type);
		TrieTreeNode node = nodes.get(domain);

		if (node == null) {
			node = new TrieTreeNode();
			nodes.put(domain, node);
		}
		return node;
	}

	private Map<String, TrieTreeNode> findOrCreateTrieTreeNodes(int type) {
		Map<String, TrieTreeNode> nodes = m_formats.get(type);

		if (nodes == null) {
			nodes = new HashMap<String, TrieTreeNode>();
			m_formats.put(type, nodes);
		}

		return nodes;
	}

	private TrieTreeNode getFormatTree(int type, String domain) {
		if (m_formats == null) {
			return null;
		}
		Map<String, TrieTreeNode> domainToFormatMap = m_formats.get(type);
		if (domainToFormatMap == null) {
			return null;
		}
		return domainToFormatMap.get(domain);
	}

	@Override
	public String handle(int type, String domain, String input) {
		TrieTreeNode formatTree = getFormatTree(type, domain);

		if (formatTree == null) {
			return input;
		}
		return parse(formatTree, input);
	}

	private String parse(TrieTreeNode formatTree, String input) {
		char[] cs = input.toCharArray();
		List<Map<String, AggregationMessageFormat>> sformatSet = new ArrayList<Map<String, AggregationMessageFormat>>();
		List<Map<String, AggregationMessageFormat>> eformatSet = new ArrayList<Map<String, AggregationMessageFormat>>();
		TrieTreeNode current = formatTree;
		int i = 0;

		for (; i < cs.length; i++) {
			sformatSet.add(current.getFormatMap());
			TrieTreeNode node = current.getChildNode(cs[i], true);
			if (node == null) {
				i--;
				break;
			}
			current = node;
		}

		current = formatTree;
		int j = cs.length - 1;
		for (; j > 0; j--) {
			eformatSet.add(current.getFormatMap());
			TrieTreeNode node = current.getChildNode(cs[j], false);
			if (node == null) {
				j++;
				break;
			}
			current = node;
		}

		for (Map<String, AggregationMessageFormat> amfMap : sformatSet) {
			for (Entry<String, AggregationMessageFormat> entry : amfMap.entrySet()) {
				String key = entry.getKey();
				if (!input.endsWith(key)) {
					continue;
				}
				AggregationMessageFormat amf = amfMap.get(key);
				CompositeFormat cf = new CompositeFormat(amf);
				String output;
				try {
					output = cf.parse(input);
				} catch (Exception e) {
					continue;
				}
				return output;
			}
		}
		for (Map<String, AggregationMessageFormat> amfMap : eformatSet) {
			for (Entry<String, AggregationMessageFormat> entry : amfMap.entrySet()) {
				String key = entry.getKey();
				if (!input.startsWith(key)) {
					continue;
				}
				AggregationMessageFormat amf = amfMap.get(key);
				CompositeFormat cf = new CompositeFormat(amf);
				String output;
				try {
					output = cf.parse(input);
				} catch (Exception e) {
					continue;
				}
				return output;
			}
		}
		return input;
	}

	@Override
	public void register(List<AggregationRule> rules) {
		m_formats.clear();

		for (AggregationRule rule : rules) {
			String format = rule.getPattern();

			if (format == null || format.isEmpty()) {
				continue;
			}
			int index1 = format.indexOf('{');

			if (index1 == -1 || index1 == format.length() - 1) {
				continue;
			}
			int index2 = format.lastIndexOf('}');

			if (index2 == -1 || index2 < index1) {
				continue;
			}

			String key1 = format.substring(0, index1);
			String key2 = format.substring(index2 + 1);
			AggregationMessageFormat value = new AggregationMessageFormat(format);

			if (rule.getDomain() != null) {
				TrieTreeNode node = findOrCreateTrieTreeNode(rule.getType(), rule.getDomain());

				buildFormatTree(node, key1.toCharArray(), key2.toCharArray(), value);
			}
		}
	}
}
