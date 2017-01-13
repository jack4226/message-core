package ltj.message.util;

import static org.junit.Assert.*;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

public class EmailAddrUtilTest {

	@Test
	public void test1() {
		String addr = "\"ORCPT jwang@nc.rr.com\" <jwang@nc.rr.com>";
		assertEquals(true, EmailAddrUtil.hasDisplayName(addr));
		assertEquals("\"ORCPT jwang@nc.rr.com\"", EmailAddrUtil.getDisplayName(addr));
		assertEquals("jwang@nc.rr.com", EmailAddrUtil.removeDisplayName(addr));
		assertEquals("nc.rr.com", EmailAddrUtil.getEmailDomainName(addr));
		
		assertEquals(true, EmailAddrUtil.isInternetEmailAddress(addr));
		assertEquals(true, EmailAddrUtil.isRemoteEmailAddress(addr));
		assertEquals(true, EmailAddrUtil.isRemoteOrLocalEmailAddress(addr));
		
		addr = "DirectStarTV <fqusoogd.undlwfeteot@chaffingphotosensitive.com>";
		System.out.println(addr+" --> "+EmailAddrUtil.removeDisplayName(addr));
		assertEquals("fqusoogd.undlwfeteot@chaffingphotosensitive.com", EmailAddrUtil.removeDisplayName(addr));
		
		addr = "TEST@test.com";
		assertEquals(addr, EmailAddrUtil.removeDisplayName(addr, false));
		assertEquals(addr.toLowerCase(), EmailAddrUtil.removeDisplayName(addr));
		
		assertEquals(true, EmailAddrUtil.isRemoteEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC@localhost.us"));
		assertEquals(true, EmailAddrUtil.isRemoteOrLocalEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC"));
		
		String verp1 = "bounce-10.07410251.0-jsmith=test.com@localhost";
		assertEquals(true, EmailAddrUtil.isVERPAddress(verp1));
		assertEquals("jsmith@test.com", EmailAddrUtil.getOrigAddrFromVERP(verp1));
		assertEquals("bounce@localhost", EmailAddrUtil.getDestAddrFromVERP(verp1));
		
		
		String verp2 = "remove-testlist-jsmith=test.com@localhost";
		assertEquals(true, EmailAddrUtil.isVERPAddress(verp2));
		assertEquals("jsmith@test.com", EmailAddrUtil.getOrigAddrFromVERP(verp2));
		assertEquals("remove@localhost", EmailAddrUtil.getDestAddrFromVERP(verp2));
	}
	
	@Test
	public void test2() {
		try {
			Address[] addr1_1 = InternetAddress.parse("test1@test.com");
			Address[] addr1_2 = InternetAddress.parse("<test1@test.com>");
			
			String addr1 = EmailAddrUtil.emailAddrToString(addr1_1);
			String addr2 = EmailAddrUtil.emailAddrToString(addr1_2);
			
			assertEquals(0, EmailAddrUtil.compareEmailAddrs(addr1, addr2));
			
			assertTrue(0 > EmailAddrUtil.compareEmailAddrs("A" + addr1, addr2));
			assertTrue(0 < EmailAddrUtil.compareEmailAddrs(addr1, "A" + addr2));
			
			Address[] addr2_1 = InternetAddress.parse("<test1@test.com>,test2@test.com,\"My book\"<test3@test.com>");
			String addr3 = EmailAddrUtil.emailAddrToString(addr2_1);
			assertEquals("test1@test.com,test2@test.com,test3@test.com", addr3);
			
		} catch (AddressException e) {
			e.printStackTrace();
			fail();
		}
	}
}
