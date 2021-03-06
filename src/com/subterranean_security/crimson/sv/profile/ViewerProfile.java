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
package com.subterranean_security.crimson.sv.profile;

import java.io.Serializable;
import java.util.Date;

import com.subterranean_security.crimson.core.proto.Delta.EV_ViewerProfileDelta;
import com.subterranean_security.crimson.sv.permissions.ViewerPermissions;
import com.subterranean_security.crimson.sv.profile.attribute.Attribute;
import com.subterranean_security.crimson.sv.profile.attribute.TrackedAttribute;
import com.subterranean_security.crimson.sv.profile.attribute.UntrackedAttribute;

public class ViewerProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	private int cvid;

	private Attribute user;
	private Attribute ip;
	private ViewerPermissions permissions;

	public ViewerProfile(int cvid) {
		this();
		this.cvid = cvid;
	}

	public ViewerProfile() {
		ip = new TrackedAttribute();
		user = new UntrackedAttribute();
		permissions = new ViewerPermissions();
	}

	public ViewerPermissions getPermissions() {
		return permissions;
	}

	public void setPermissions(ViewerPermissions p) {
		permissions = p;
	}

	public Integer getCvid() {
		return cvid;
	}

	public void setCvid(int cvid) {
		this.cvid = cvid;
	}

	public String getUser() {
		return user.get();
	}

	public void setUser(String user) {
		this.user.set(user);
	}

	public String getIp() {
		return ip.get();
	}

	public Date getLoginTime() {
		return ((TrackedAttribute) ip).getTime(0);
	}

	public void setIp(String ip) {
		((TrackedAttribute) this.ip).set(ip);
	}

	public String getLastLoginIp() {
		TrackedAttribute tr = (TrackedAttribute) ip;
		if (tr.size() < 2) {
			return null;
		}
		return tr.getValue(tr.size() - 1);
	}

	public Date getLastLoginTime() {
		TrackedAttribute tr = (TrackedAttribute) ip;
		if (tr.size() < 2) {
			return null;
		}
		return tr.getTime(tr.size() - 1);
	}

	public void amalgamate(EV_ViewerProfileDelta c) {
		if (c.hasUser()) {
			setUser(c.getUser());
		}

		if (c.hasLastLoginIp() && c.hasLastLoginTime()) {
			((TrackedAttribute) ip).set(c.getLastLoginIp(), new Date(c.getLastLoginTime()));
		}

		if (c.hasLoginIp()) {
			((TrackedAttribute) ip).set(c.getLoginIp(), new Date(c.getLoginTime()));
		}

		if (c.getViewerPermissionsCount() != 0) {
			// append to permissions, overwriting if necessary
			permissions.load(c.getViewerPermissionsList());
		}

	}

}
