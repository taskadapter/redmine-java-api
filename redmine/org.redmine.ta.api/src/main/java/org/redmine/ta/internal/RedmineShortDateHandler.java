/*
   Copyright 2010-2011 Alexey Skorokhodov.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.redmine.ta.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * it's easier to create separate classes for short and long dates than create
 * workarounds for crappy CastorXML library
 * 
 * @author ask
 * 
 */
public class RedmineShortDateHandler extends RedmineDateHandler {

	// XXX there's bug in Castor: http://jira.codehaus.org/browse/CASTOR-1878
	private static final String FORMAT = "yyyy-MM-dd";
	SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);

	@Override
	public Date getDate(String str) throws ParseException {
		return formatter.parse(str);
	}

	@Override
	public String getString(Date date) {
		return formatter.format(date);
	}

}
