/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */
package com.seagate.kinetic.socketlog;

public class ParseDefaultLogFormatterExample {
	private String className;
	private String method;
	private String status;
	private String errorMsg;

	public static ParseDefaultLogFormatterExample fromMsg(String msg) {
		if (msg == null || msg.isEmpty()) {
			return null;
		}

		String[] vals = msg.split(";");
		if (vals.length < 3) {
			return null;
		}

		String className = vals[0].replaceFirst("class=", "");
		String method = vals[1].replaceFirst("method", "");
		String status = vals[2].replaceFirst("status=", "");

		String errorMsg = "";
		if (vals.length == 4) {
			errorMsg = vals[3].replaceFirst("errormsg=", "");
		}

		ParseDefaultLogFormatterExample logRecord = new ParseDefaultLogFormatterExample();
		logRecord.setClassName(className);
		logRecord.setMethod(method);
		logRecord.setStatus(status);
		logRecord.setErrorMsg(errorMsg);

		return logRecord;

	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}
