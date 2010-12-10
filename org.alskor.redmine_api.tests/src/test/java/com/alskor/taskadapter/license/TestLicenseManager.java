package com.alskor.taskadapter.license;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.alskor.redmine.internal.LicenseManager;
import org.alskor.redmine.internal.MyIOUtils;
import org.junit.Test;


public class TestLicenseManager {

	@Test
	public void testValidLicense() {
		String licenseText;
		try {
			licenseText = MyIOUtils
					.getResourceAsString("taskadapter.license.valid");
			boolean result = LicenseManager.checkLicense(licenseText);
			assertTrue("license should be valid", result);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testInvalidLicense() {
		String licenseText;
		try {
			licenseText = MyIOUtils
					.getResourceAsString("taskadapter.license.invalid");
			boolean result = LicenseManager.checkLicense(licenseText);
			assertFalse("license should be invalid", result);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
