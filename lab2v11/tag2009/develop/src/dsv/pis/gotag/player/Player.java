package dsv.pis.gotag.player;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import dsv.pis.gotag.util.Commandline;
import dsv.pis.gotag.util.CmdlnOption;
import dsv.pis.gotag.bailiff.BailiffInterface;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Location;
import net.jini.core.entry.Entry;

public class Player implements Serializable {
	protected static final String bfi = "dsv.pis.gotag.bailiff.BailiffInterface";
	protected transient ServiceDiscoveryManager SDM;
	protected ServiceTemplate bailiffTemplate;
	protected boolean debug = false;
	protected UUID myName;
	protected String currentHost = null;
	protected boolean iAmIt;
	private Random rand;
	
	public Player(boolean iAmIt, boolean debug) throws ClassNotFoundException {
		this.rand = new Random();
		this.debug = debug;
		this.iAmIt = iAmIt;
		this.myName = UUID.randomUUID();
		bailiffTemplate = new ServiceTemplate(null, new Class[] {java.lang.Class.forName(bfi)}, null);
	}
	
	protected void snooze(long ms) {
		try {
			Thread.currentThread().sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public UUID getName() {
		return myName;
	}
	
	/**
	* Called by the bailiff (proxy) to ask if this player is the Tagger (it)
	**/
	public synchronized boolean isIt() {
		return iAmIt;
	}
	
	/**
	* Called by the bailiff (proxy) to tag this player.
	**/
	public synchronized boolean tag() {
//		print("I was tagged!");
		iAmIt = true;
		return iAmIt;
	}
	
	public void topLevel() throws IOException {
		SDM = new ServiceDiscoveryManager(null, null);
		currentHost = InetAddress.getLocalHost().getHostName().toLowerCase();
		List<BailiffInterface> bailiffs = new ArrayList<>();
		
		while(true) {
			ServiceItem[] svcItems;
			long retryInterval = 0;
			snooze(1000+rand.nextInt(4000));
			
			do {
				if(retryInterval > 0) {
					print("No Bailiffs detected. Going to sleep.");
					snooze(retryInterval);
					print("Waking up");
				}
				svcItems = SDM.lookup(bailiffTemplate, 8, null);
				retryInterval = 20*1000;
			} while(svcItems.length == 0);
			
			int nItems = svcItems.length;
			

			for(int i=0; i<nItems; i++) {
				Object obs = svcItems[i].service;
				if(obs instanceof BailiffInterface) {
					BailiffInterface bfi = (BailiffInterface) obs;
//					print("Trying to ping i " + i);
					try {
						String response = bfi.ping();
//						print(response);
						if(!bailiffs.contains(bfi))
							bailiffs.add(bfi);
					} catch(RemoteException e) {
						print("Ping failed..");
					}
				}
			}
			
			//Find my bailiff
			BailiffInterface myBailiff = findBailiff(myName, bailiffs);
			
			if(iAmIt) {
				//Find someone to tag
//				print("I'm it");
				List<UUID> targetsInMyBailiff = new ArrayList<UUID>();
				if(myBailiff != null)
					targetsInMyBailiff = myBailiff.playersInBailiff();
		
				if(targetsInMyBailiff.size() > 1) {
					//There is someone else in my bailiff
					targetsInMyBailiff.remove(myName); //Don't want to target myself..
					UUID target = targetsInMyBailiff.get((new Random()).nextInt(targetsInMyBailiff.size())); //Pick a random target
					boolean successfulTag = myBailiff.tag(target);
					if(successfulTag){
						//I have tagged someone. Now what?
						iAmIt = false;
//						print("Tagged " + target);
						System.out.println(myName + ": TAGGED " + target);
//						//run!
						BailiffInterface dest = leastPopulatedBailiff(bailiffs);
						try {
							print("(not it) moving to " + dest.getRoomName());
							dest.migrate(this, "topLevel", new Object[]{});
							SDM.terminate();
							return;
						} catch (NoSuchMethodException e) {
							print("Failed to jump...");
							e.printStackTrace();
						}
					}
				} else {
					//My bailiff is empty, need to move to another one
					BailiffInterface dest = mostPopulatedBailiff(bailiffs);
					try {
//						print("(it) moving to " + dest.getRoomName());
						dest.migrate(this, "topLevel", new Object[]{});
						SDM.terminate();
						return;
					} catch (NoSuchMethodException e) {
						print("Failed to jump...");
						e.printStackTrace();
					}
				}
			} else {
				//Not it, avoid whoever is
				boolean needToRun = false;
				if(myBailiff == null) {
					needToRun = true;
				} else {
					for(UUID u : myBailiff.playersInBailiff())
						if(myBailiff.isIt(u))
							needToRun = true;
				}
				if(needToRun) {
					BailiffInterface dest = leastPopulatedBailiff(bailiffs);
					try {
						//No need to jump to my own bailiff?
//						print("(not it) moving to " + dest.getRoomName());
						dest.migrate(this, "topLevel", new Object[]{});
						SDM.terminate();
						return;
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	* Find a bailiff that contains a given user (id)
	**/
	private BailiffInterface findBailiff(UUID id, List<BailiffInterface> blist) throws RemoteException {
		BailiffInterface result = null;
		for(BailiffInterface b : blist)
			for(UUID u : b.playersInBailiff())
				if(u.equals(id))
					result = b;
		return result;
	}
	
	/**
	* Find the bailiff with the highest number of players in it. This is a good target for the tagger.
	**/
	private BailiffInterface mostPopulatedBailiff(List<BailiffInterface> bailiffs) throws RemoteException {
		BailiffInterface result = null;
		int max = -1;
		for(BailiffInterface i : bailiffs) {
			int pib = i.playersInBailiff().size();
			if(pib > max) {
				max = pib;
				result = i;
			}
		}
		return result;
	}
	
	/**
	* Find the bailiff with the lowest number of players. A good target to avoid the tagger.
	**/
	private BailiffInterface leastPopulatedBailiff(List<BailiffInterface> bailiffs) throws RemoteException {
		BailiffInterface result = null;
		int min = Integer.MAX_VALUE;
		for(BailiffInterface i : bailiffs) {
			int pib = i.playersInBailiff().size();
			if(pib < min) {
				min = pib;
				result = i;
			}
		}
		return result;
	}
	
	protected void print(String msg) {
		if(debug)
			System.out.println(myName + ": " + msg);
	}
	
	public static void main(String[] argv)
			throws java.lang.ClassNotFoundException, java.io.IOException {
		CmdlnOption helpOption = new CmdlnOption("-help");
		CmdlnOption debugOption = new CmdlnOption("-debug");
		CmdlnOption itOption = new CmdlnOption("-it");

		CmdlnOption[] opts = new CmdlnOption[] { helpOption, debugOption,
				itOption };

		String[] restArgs = Commandline.parseArgs(System.out, argv, opts);

		if (restArgs == null) {
			System.exit(1);
		}

		if (helpOption.getIsSet() == true) {
			System.out.println("Usage: [-help]|[-debug][-it]");
			System.out.println("where -help shows this message");
			System.out.println("      -debug turns on debugging.");
			System.out.println("      -it indicates that this player starts as 'it'.");
			System.exit(0);
		}

		boolean debug = debugOption.getIsSet();
		boolean it = itOption.getIsSet();

		// We will try without it first
		// System.setSecurityManager (new RMISecurityManager ());
//		Dexter dx = new Dexter(debug, noFace);
		Player p = new Player(it, debug);
		p.topLevel();
		System.exit(0);
	}
}
