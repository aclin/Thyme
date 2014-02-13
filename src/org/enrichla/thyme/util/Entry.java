package org.enrichla.thyme.util;

import java.util.Comparator;

public class Entry {
	public String fname;
	public String lname;
	public String email;
	public String site;
	public String number;
	
	public Entry() {
		// Empty constructor
	}

	public static Comparator<Entry> COMPARE_FNAME = new Comparator<Entry>() {

		@Override
		public int compare(Entry arg0, Entry arg1) {
			return arg0.fname.compareToIgnoreCase(arg1.fname);
		}
		
	};
}
