package org.alskor.redmine.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class LicenseManager {
	// TODO this is not very secure, but should be OK for the prototype
	public static String PASSWORD = "z823nv_sz84";
	public static String LINE_DELIMITER = "\n";
	public static final String KEY_STR = "-----Key-----" + LINE_DELIMITER;
	public static final String FILE_NAME = "taskadapter.license";

	public static final String PREFIX_DATE = "Date: ";
	public static final String PREFIX_EMAIL = "Email: ";
	public static final String PREFIX_REGISTERED_TO = "Registered to: ";

	public static License checkLicense() {

		try {
			String licenseText = MyIOUtils.getResourceAsString(FILE_NAME);
			return checkLicense(licenseText);
		} catch (IOException e) {
			throw new RuntimeException("Problem loading redmine API license: "
					+ e);
		}
	}

	/**
	 * @param licenseText
	 * @return the valid License object or NULL if the license text is invalid
	 */
	public static License checkLicense(String licenseText) {
		License license = null;
		try {
			int i = licenseText.indexOf(KEY_STR);
			String partBeforeKey = licenseText.substring(0, i - 1);
			String partKey = licenseText.substring(i + KEY_STR.length());
			// System.out.println("--" + partBeforeKey);
			// System.out.println("--" + partKey);
			String xoredText = xor(partKey, PASSWORD);
			if (partBeforeKey.equals(xoredText)) {
				license = parseLicenseInfo(partBeforeKey);
			}
			// System.out.println("--" + xoredText);
		} catch (Exception e) {
			System.err.println("Can't validate license.");
		}
		return license;
	}

	private static License parseLicenseInfo(String partBeforeKey) {
// 		format:		
//	  	Registered to: JUnit_autotest
//		Email: noemail@nodomain
//		Date: Fri Dec 10 11:02:34 PST 2010
		StringTokenizer tok = new StringTokenizer(partBeforeKey, LINE_DELIMITER);
		
		String nameLine = tok.nextToken();
		String customerName = nameLine.substring(PREFIX_REGISTERED_TO.length());

		String emailLine = tok.nextToken();
		String email = emailLine.substring(PREFIX_EMAIL.length());

		String dateLine = tok.nextToken();
		String createdOn = dateLine.substring(PREFIX_DATE.length());
		
		return new License(customerName, email, createdOn);
	}

	public static String xor(String str, String key) {
		String result = null;
		byte[] strBuf = str.getBytes();
		byte[] keyBuf = key.getBytes();
		int c = 0;
		int z = keyBuf.length;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(strBuf.length);
		for (int i = 0; i < strBuf.length; i++) {
			byte bS = strBuf[i];
			byte bK = keyBuf[c];
			byte bO = (byte) (bS ^ bK);
			if (c < z - 1) {
				c++;
			} else {
				c = 0;
			}
			baos.write(bO);
		}
		try {
			baos.flush();
			result = baos.toString();
			baos.close();
			baos = null;
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		return result;
	}

}
