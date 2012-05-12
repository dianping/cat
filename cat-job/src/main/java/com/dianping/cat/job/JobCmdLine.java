package com.dianping.cat.job;

import java.util.ArrayList;
import java.util.List;

public class JobCmdLine {
	private String m_jobletName = "help";

	private List<String> m_args = new ArrayList<String>();

	public JobCmdLine(String[] args) {
		parse(args);

		if (m_args.size() > 0) {
			m_jobletName = m_args.remove(0);
		}
	}

	public String getJobletName() {
		return m_jobletName;
	}

	private String getArg(int index) {
		if (index >= 0 && index < m_args.size()) {
			return m_args.get(index);
		} else {
			return null;
		}
	}

	public String getArg(String name, int index, String defaultValue) {
		String value = getArg(index);

		if (value != null) {
			return value;
		}

		return defaultValue;
	}

	public boolean getArgBoolean(String name, int index, boolean defaultValue) {
		String value = getArg(index);

		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format(
				      "Invalid value(%s) of argument(%s) at %s, boolean expected!", value, name, index), e);
			}
		}

		return defaultValue;
	}

	public double getArgDouble(String name, int index, double defaultValue) {
		String value = getArg(index);

		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format(
				      "Invalid value(%s) of argument(%s) at %s, double expected!", value, name, index), e);
			}
		}

		return defaultValue;
	}

	public int getArgInt(String name, int index, int defaultValue) {
		String value = getArg(index);

		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of argument(%s) at %s, int expected!",
				      value, name, index), e);
			}
		}

		return defaultValue;
	}

	public long getArgLong(String name, int index, long defaultValue) {
		String value = getArg(index);

		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of argument(%s) at %s, long expected!",
				      value, name, index), e);
			}
		}

		return defaultValue;
	}

	public String getProperty(String name, String defaultValue) {
		return System.getProperty(name, defaultValue);
	}

	public boolean getPropertyBoolean(String name, boolean defaultValue) {
		String value = getProperty(name, null);

		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of property(%s), boolean expected!",
				      value, name), e);
			}
		}

		return defaultValue;
	}

	public double getPropertyDouble(String name, double defaultValue) {
		String value = getProperty(name, null);

		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of property(%s), double expected!",
				      value, name), e);
			}
		}

		return defaultValue;
	}

	public int getPropertyInt(String name, int defaultValue) {
		String value = getProperty(name, null);

		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of property(%s), int expected!", value,
				      name), e);
			}
		}

		return defaultValue;
	}

	public long getPropertyLong(String name, long defaultValue) {
		String value = getProperty(name, null);

		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid value(%s) of property(%s), long expected!",
				      value, name), e);
			}
		}

		return defaultValue;
	}

	private void parse(String[] args) {
		int len = args.length;

		for (int i = 0; i < len; i++) {
			String arg = args[i];

			if (arg.startsWith("-D")) {
				if (arg.length() == 2) {
					if (i + 1 < len) {
						arg = args[i + 1];
						i++;
					} else {
						m_args.add(arg);
						break;
					}
				} else {
					arg = arg.substring(2);
				}

				int pos = arg.indexOf('=');

				if (pos > 0) {
					String name = arg.substring(0, pos);
					String value = arg.substring(pos + 1);

					setProperty(name, value);
				} else {
					setProperty(arg, "");
				}
			} else {
				m_args.add(arg);
			}
		}
	}

	public void setProperty(String name, String value) {
		System.setProperty(name, value);
	}
}
