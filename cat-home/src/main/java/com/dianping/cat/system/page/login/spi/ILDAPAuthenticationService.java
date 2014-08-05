/**
 * Project: lion-service
 * 
 * File Created at 2012-8-20
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.cat.system.page.login.spi;

import com.dianping.cat.system.page.login.service.Token;

public interface ILDAPAuthenticationService {
	public Token authenticate(String userName, String password) throws Exception;
}
