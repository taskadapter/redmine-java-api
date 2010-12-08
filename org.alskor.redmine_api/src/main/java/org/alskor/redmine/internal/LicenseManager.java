package org.alskor.redmine.internal;

import java.io.IOException;
import java.util.Calendar;

import org.alskor.redmine.MyIOUtils;

public class LicenseManager {

	public static Calendar checkLicense() {

		try {
			String licenseText = MyIOUtils
					.getResourceAsString("redmineapi.license");
			return checkLicense(licenseText);
		} catch (IOException e) {
			throw new RuntimeException("Problem loading redmine API license: "
					+ e);
		}
	}

	private static Calendar checkLicense(String licenseText) {
		String validTillStr = licenseText.substring(5);
		// System.out.println(validTillStr);
		Calendar validTill = null;
		try {
			long validTillMillis = Long.parseLong(validTillStr);
			validTill = Calendar.getInstance();
			validTill.setTimeInMillis(validTillMillis);
			Calendar now = Calendar.getInstance();
			if (now.after(validTill)) {
				throw new RuntimeException("Redmine API license expired. License used: " + validTillStr);
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException("Redmine API license is invalid. License used: " + validTillStr);
		}
		return validTill;
	}
}
