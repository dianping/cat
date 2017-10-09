package com.dianping.cat.plugin;

import java.util.List;

public class PropertyProviders {
	public static ConsoleProvider fromConsole() {
		return ConsoleProvider.INSTANCE;
	}

	public static enum ConsoleProvider {
		INSTANCE;

		public String forString(String name, String prompt, List<String> availableValues, String defaultValue,
				IValidator<String> validator) {
			String value = getString(name, prompt, availableValues, defaultValue);

			if (validator != null) {
				while (!validator.validate(value)) {
					value = getString(name, prompt, availableValues, defaultValue);
				}
			}

			return value;
		}

		private String getString(String name, String prompt, List<String> availableValues, String defaultValue) {
			String value = name == null ? null : System.getProperty(name);

			if (value != null) {
				return value;
			}

			StringBuilder sb = new StringBuilder(64);
			byte[] buffer = new byte[256];

			while (value == null) {
				sb.setLength(0);
				sb.append(prompt);

				if (defaultValue != null) {
					sb.append('[').append(defaultValue).append(']');
				}

				boolean withOptions = availableValues != null && !availableValues.isEmpty();
				int count = 0;

				if (withOptions) {
					System.out.println(sb.toString());

					for (String availableValue : availableValues) {
						System.out.println((count++) + ": " + availableValue);
					}

					System.out.print("Please select:");
				} else {
					System.out.print(sb.toString());
				}

				System.out.flush();

				try {
					int size = System.in.read(buffer);

					while (size > 0 && (buffer[size - 1] == '\n' || buffer[size - 1] == '\r')) {
						size--;
					}

					if (size <= 0) {
						value = defaultValue;
					} else {
						value = new String(buffer, 0, size);

						if (withOptions) {
							try {
								int pos = Integer.parseInt(value);

								if (pos >= 0 && pos < count) {
									value = availableValues.get(pos);
								} else {
									value = null;
								}
							} catch (Exception e) {
								// ignore it
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return value;
		}
	}

	public static interface IValidator<T> {
		public boolean validate(T value);
	}
}
