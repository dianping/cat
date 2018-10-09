/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qbao.catagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * @author andersen
 *
 */
public class CatAgent {
	
	private static Instrumentation s_instrumentation;
    
    private static ClassFileTransformer s_transformer;
    
    public static void premain(String options, Instrumentation instrumentation) {
    	/* Handle duplicate agents */
    	if (s_instrumentation != null) {
    		return;
    	}
        s_instrumentation = instrumentation;
        s_transformer = new ClassPathPreSetAgentAdapter(options);
        s_instrumentation.addTransformer(s_transformer);
    }

    
    public static void agentmain(String options, Instrumentation instrumentation) {
    	premain(options, instrumentation);
    }
    
    public static Instrumentation getInstrumentation() {
        if (s_instrumentation == null) {
            throw new UnsupportedOperationException(
                "CatClient agent was started via '-javaagent' (preMain) ");
        }
        return s_instrumentation;
    }
}
