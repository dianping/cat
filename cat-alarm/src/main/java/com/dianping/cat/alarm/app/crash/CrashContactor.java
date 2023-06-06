/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dianping.cat.alarm.app.crash;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import org.unidal.lookup.annotation.Inject;

public class CrashContactor extends ProjectContactor implements Contactor {
	public static final String ID = AlertType.CRASH.getName();

	@Inject
	protected CrashRuleConfigManager m_crashAlarmRuleManager;

	@Override
	public String getId() {
		return ID;
	}
}
