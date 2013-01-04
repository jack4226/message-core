package com.legacytojava.mailsender;

import com.legacytojava.jbatch.JbMain;

public class MailSenderMain {

	public static void main(String[] args) {
		MailSenderMain main = new MailSenderMain();
		main.start();
	}
	
	void start() {
		//Properties props = System.getProperties();
		//props.list(System.out);
		JbMain.main(new String[0]);
	}
}
