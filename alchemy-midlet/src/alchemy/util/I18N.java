/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package alchemy.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Internationalization support for Alchemy.
 * Every string that needs to be translated should
 * be surrounded with <code>I18N._( )</code>.
 * @author Sergey Basalaev
 */
public class I18N {

	private I18N() { }

	private static Properties strings = null;

	/**
	 * Loads translation bundle for given locale.
	 * @param locale locale string
	 */
	public static void setLocale(String locale) {
		// try to load bundle for specified locale
		InputStream in = I18N.class.getResourceAsStream("/locale/"+locale);
		// try to load bundle for two-letter language code
		if (in == null) in = I18N.class.getResourceAsStream("/locale/"+locale.substring(0, 2));
		// if nothing found fall back to the C locale
		if (in == null) {
			strings = null;
			return;
		}
		try {
			strings = Properties.readFrom(new UTFReader(in));
		} catch (IOException e) {
			strings = null;
		}
	}

	/**
	 * Looks up the translation of the string and returns it.
	 * If translation is not found then the string itself is returned.
	 */
	public static String _(String str) {
		if (strings != null) {
			String ret = strings.get(str);
			if (ret != null) return ret;
			else return str;
		} else {
			return str;
		}
	}
	
	/**
	 * Translates given string and substitues substring {0}
	 * with arg0.
	 */
	public static String _(String str, Object arg0) {
		str = _(str);
		int index = str.indexOf("{0}");
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0,index))
		.append(arg0).append(str.substring(index+3));
		return sb.toString();
	}

	/**
	 * Translates given string and substitues substrings {0}, {1}
	 * with arg0, arg1.
	 */
	public static String _(String str, Object arg0, Object arg1) {
		str = _(str);
		int index0 = str.indexOf("{0}");
		int index1 = str.indexOf("{1}");
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0,index0))
		.append(arg0).append(str.substring(index0+3,index1))
		.append(arg1).append(str.substring(index1+3));
		return sb.toString();
	}

	/**
	 * Translates given string and substitues substrings {0}, {1}, {2}
	 * with arg0, arg1, arg2.
	 */
	public static String _(String str, Object arg0, Object arg1, Object arg2) {
		str = _(str);
		int index0 = str.indexOf("{0}");
		int index1 = str.indexOf("{1}");
		int index2 = str.indexOf("{2}");
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0,index0))
		.append(arg0).append(str.substring(index0+3,index1))
		.append(arg1).append(str.substring(index1+3,index2))
		.append(arg2).append(str.substring(index2+3));
		return sb.toString();
	}

	static {
		String locale = System.getProperty("microedition.locale");
		if (locale != null) I18N.setLocale(locale);
	}

}
