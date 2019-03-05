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
package gocat

import "C"
import (
	"github.com/dianping/cat/lib/go/ccat"
)

type Config struct {
	EncoderType     int
	EnableHeartbeat int
	EnableSampling  int
	EnableDebugLog  int
}

func DefaultConfig() Config {
	return Config{
		ENCODER_BINARY,
		1,
		1,
		0,
	}
}

func DefaultConfigForCat2() Config {
	return Config{
		ENCODER_TEXT,
		1,
		0,
		0,
	}
}


func Init(domain string, configs ...Config) {
	var config Config;
	if len(configs) > 1 {
		panic("Only 1 config can be specified while initializing cat.")
	} else if len(configs) == 1 {
		config = configs[0]
	} else {
		config = DefaultConfig()
	}

	ccat.InitWithConfig(domain, ccat.BuildConfig(
		config.EncoderType,
		config.EnableHeartbeat,
		config.EnableSampling,
		config.EnableDebugLog,
	))
	go ccat.Background()
}

func Shutdown() {
	ccat.Shutdown()
}

func Wait() {
	ccat.Wait()
}
