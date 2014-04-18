package com.seagate.kinetic.socketlog;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DefaultLogFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		return "class=" + record.getSourceClassName() + ";method="
				+ record.getSourceMethodName() + ";" + record.getMessage()
				+ "\n";
	}

}
