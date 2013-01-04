package com.legacytojava.mailreader;

import com.legacytojava.jbatch.JbMain;

public class MailReaderMain {
	public static void main(String[] args) {
		MailReaderMain reader = new MailReaderMain();
		reader.start();
	}
	
	void start() {
		//Properties props = System.getProperties();
		//props.list(System.out);
		JbMain.main(new String[0]);
	}
 }
