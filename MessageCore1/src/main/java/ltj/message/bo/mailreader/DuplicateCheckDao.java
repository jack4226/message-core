package ltj.message.bo.mailreader;

import ltj.jbatch.app.Processor;

public interface DuplicateCheckDao extends Processor {
	public boolean isDuplicate(String msg_id);
	public void purge(int hours);
}
