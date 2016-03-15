/******************************************************************************
 *                                                                            *
 *                    Copyright 2016 Subterranean Security                    *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *      http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 *                                                                            *
 *****************************************************************************/
package com.subterranean_security.crimson.client;

import java.io.File;
import java.util.List;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.subterranean_security.crimson.client.net.ClientConnector;
import com.subterranean_security.crimson.core.Common;
import com.subterranean_security.crimson.core.proto.net.Gen.NetworkTarget;
import com.subterranean_security.crimson.core.storage.ClientDB;
import com.subterranean_security.crimson.core.util.PlatformInfo;

public class Client {
	private static final Logger log = LoggerFactory.getLogger(Client.class);

	public static ClientConnector connector;
	public static ClientDB clientDB;
	public static int connectionIterations = 0;

	public static void main(String[] args) {

		log.info("Initializing client");

		// Establish the custom fallback exception handler
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

		// Establish the custom shutdown hook
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		// Load native libraries
		File lib = null;
		String arch = null;
		switch (PlatformInfo.jreArch) {
		case ARM:
			arch = "ARM";
			break;
		case X64:
			arch = "64";
			break;
		case X86:
			arch = "32";
			break;
		}
		switch (PlatformInfo.os) {
		case BSD:
			break;
		case LINUX:
			lib = new File(Common.base.getAbsolutePath() + "/lib/jni/lin/crimson" + arch + ".so");
			break;
		case OSX:
			break;
		case SOLARIS:
			break;
		case WINDOWS:
			lib = new File(Common.base.getAbsolutePath() + "/lib/jni/win/crimson" + arch + ".dll");
			break;

		}
		System.out.println("Loading library: " + lib.getAbsolutePath());
		System.load(lib.getAbsolutePath());

		List<NetworkTarget> nts = null;
		try {
			clientDB = new ClientDB(new File(Common.base + "/var/client.db"));
			nts = getExternalNts();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Database error");
		}

		connectionRoutine(nts);

	}

	private static List<NetworkTarget> getExternalNts() throws Exception {

		return (List<NetworkTarget>) clientDB.getObject("nts");
	}

	public static void connectionRoutine(List<NetworkTarget> nt) {
		connectionIterations++;
		for (NetworkTarget n : nt) {
			try {
				log.debug("Attempting connection to: " + n.getServer() + ":" + n.getPort());
				connector = new ClientConnector(n.getServer(), n.getPort());
			} catch (SSLException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
