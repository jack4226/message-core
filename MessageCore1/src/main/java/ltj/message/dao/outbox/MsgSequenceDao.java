package ltj.message.dao.outbox;

public interface MsgSequenceDao {
	public long findNextValue();
}
