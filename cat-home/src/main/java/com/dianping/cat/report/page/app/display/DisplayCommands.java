package com.dianping.cat.report.page.app.display;

import java.util.LinkedHashMap;
import java.util.Map;

public class DisplayCommands {

	private Map<Integer, DisplayCommand> m_commands = new LinkedHashMap<Integer, DisplayCommand>();

	public DisplayCommand findCommand(int id) {
		return m_commands.get(id);
	}

	public DisplayCommand findOrCreateCommand(int id) {
		DisplayCommand command = m_commands.get(id);

		if (command == null) {
			synchronized (m_commands) {
				command = m_commands.get(id);

				if (command == null) {
					command = new DisplayCommand(id);
					m_commands.put(id, command);
				}
			}
		}

		return command;
	}

	public Map<Integer, DisplayCommand> getCommands() {
		return m_commands;
	}

}
