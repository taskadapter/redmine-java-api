package org.alskor.redmine.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LicenseManager {
	// TODO this is not very secure, but should be OK for the prototype
	public static String PASSWORD = "z823nv_sz84";
	public static String LINE_DELIMITER = "\n";
	public static final String KEY_STR = "-----Key-----" + LINE_DELIMITER;

	public static boolean checkLicense() {

		try {
			String licenseText = MyIOUtils
					.getResourceAsString("redmineapi.license");
			return checkLicense(licenseText);
		} catch (IOException e) {
			throw new RuntimeException("Problem loading redmine API license: "
					+ e);
		}
	}

	public static boolean checkLicense(String licenseText) {
		boolean valid = false;
		try {
			int i = licenseText.indexOf(KEY_STR);
			String partBeforeKey = licenseText.substring(0, i - 1);
			String partKey = licenseText.substring(i + KEY_STR.length());
			// System.out.println("--" + partBeforeKey);
			// System.out.println("--" + partKey);
			String xoredText = xor(partKey, PASSWORD);
			if (partBeforeKey.equals(xoredText)) {
				valid = true;
			}
			// System.out.println("--" + xoredText);
		} catch (Exception e) {
			System.err.println("Can't validate license.");
		}
		return valid;
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
