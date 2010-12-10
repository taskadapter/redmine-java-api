package com.alskor.taskadapter.license;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.alskor.redmine.internal.License;
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
			License license = LicenseManager.checkLicense(licenseText);
			assertNotNull("license must be not null - this means it's valid", license);
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
			License license = LicenseManager.checkLicense(licenseText);
			assertNull("license must be NULL - it means it's invalid", license);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
