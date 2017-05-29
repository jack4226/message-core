package ltj.jbatch.app;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ltj.message.util.PrintUtil;

public class HostUtil {
	static final Logger logger = Logger.getLogger(HostUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	/** host IP address and host name */
	private static String hostIPAddr = null;
	private static String hostName = null;

	/**
	 * @return IP address of the machine this program is running on
	 */
	public static String getHostIpAddress() {
		if (hostIPAddr == null) {
			try {
				hostIPAddr = java.net.InetAddress.getLocalHost().getHostAddress();
				if (isDebugEnabled) {
					logger.debug("Host IP Address: " + hostIPAddr);
				}
			}
			catch (UnknownHostException e) {
				logger.warn("UnknownHostException caught, use 127.0.0.1", e);
				hostIPAddr = "127.0.0.1";
			}
		}
		return hostIPAddr;
	}

	/**
	 * @return Host name of the machine this program is running on
	 */
	public static String getHostName() {
		if (hostName == null) {
			try {
				hostName = java.net.InetAddress.getLocalHost().getHostName();
				if (isDebugEnabled) {
					logger.debug("Host Name: " + hostName);
				}
			}
			catch (UnknownHostException e) {
				logger.warn("UnknownHostException caught, use localhost", e);
				hostName = "localhost";
			}
		}
		return hostName;
	}
	
	private static final Pattern IPv4Regex = Pattern.compile(
	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	public static List<String> getIPListByNetworkInterfaces() {
		List<String> ipv4List = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					String ip = addr.getHostAddress();
					String name = addr.getHostName();
					boolean isIpv6 = addr instanceof Inet6Address;
					logger.info("");
					String addrType = "IPv4 address: ";
					if (isIpv6) {
						addrType = "IPv6 address: ";
					}
					else {
						ipv4List.add(ip);
					}
					logger.info(addrType + addr.getHostAddress() + ", name: " + name);
					//logger.debug(StringUtil.prettyPrint(addr));
					if (addr.isLoopbackAddress()) {
						logger.info("Loop back address: " + ip + ", is IPv4? " + IPv4Regex.matcher(ip).find());
					}
					if (addr.isSiteLocalAddress()) {
						logger.info("Site Local address: " + ip);
					}
					if (isIpv6) {
						InetAddress addrv4 = Inet6Address.getLocalHost();
						logger.info("IPv6 - v4 address: " + addrv4.getHostAddress() + ", name: " + addrv4.getHostName()
								+ ", Canonical name: " + addrv4.getCanonicalHostName());
						Inet6Address addrv6 = (Inet6Address) addr;
						NetworkInterface niv6 = addrv6.getScopedInterface();
						if (niv6 != null) {
							logger.info("Hareware - Display name: " + niv6.getDisplayName() + ", name: "
									+ niv6.getName() + ", is up? " + niv6.isUp());
						}
					}
				}
			}
			
		} catch (SocketException | UnknownHostException e) {
			logger.error("SocketException caught", e);
		}
		return ipv4List;
	}
	
	public static List<String> getNetworkInterfaceNames(boolean isUp) {
		List<String> nameList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					InetAddress ia = ias.nextElement();
					if (ia instanceof Inet6Address) {
						Inet6Address ia6 = (Inet6Address) ia;
						if (ia6.getScopedInterface() != null) {
							NetworkInterface ni6 = ia6.getScopedInterface();
							if (isUp) {
								if (ni6.isUp()) {
									logger.debug(PrintUtil.prettyPrint(ni6, 1));
									nameList.add(ni6.getName());
								}
							}
							else {
								nameList.add(ni6.getName());
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			logger.error("SocketException caught", e);
		}
		return nameList;
	}
	
	public static List<String> getByNetworkInterfaceName(String name) {
		List<String> ipList = new ArrayList<>();
		try {
			NetworkInterface ni = NetworkInterface.getByName(name);
			if (ni != null) {
				//logger.info(name + " (Network Interface): " + PrintUtil.prettyPrint(ni, 3));
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if (addr instanceof Inet6Address) {
						addr = Inet6Address.getLocalHost();
					}
					//logger.info(name + " (InetAddress): " + PrintUtil.prettyPrint(addr, 3));
					String addrStr = addr.getHostAddress();
					ipList.add(addrStr);
				}
			}
		} catch (SocketException | UnknownHostException e) {
			logger.error("SocketException caught", e);
		}
		return ipList;
	}

}
