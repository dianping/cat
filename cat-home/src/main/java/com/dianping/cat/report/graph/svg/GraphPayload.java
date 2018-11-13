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

public interface GraphPayload {
	public String getAxisXLabel(int index);

	public String getAxisXTitle();

	public String getAxisYTitle();

	public int getColumns();

	public String getDescription();

	public int getDisplayHeight();

	public int getDisplayWidth();

	public int getHeight();

	public String getIdPrefix();

	public int getMarginBottom();

	public int getMarginLeft();

	public int getMarginRight();

	public int getMarginTop();

	public int getOffsetX();

	public int getOffsetY();

	public int getRows();

	public String getTitle();

	public double[] getValues();

	public int getWidth();

	public boolean isAxisXLabelRotated();

	public boolean isAxisXLabelSkipped();

	public boolean isStandalone();
}
