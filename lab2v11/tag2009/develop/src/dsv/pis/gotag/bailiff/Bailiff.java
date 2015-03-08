// Bailiff.java -
// Fredrik Kilander, DSV (fk@dsv.su.se)
// 18-nov-2004/FK Adapted for the PRIS course.
// 2000-12-12/FK Rewrite for Java 1.3 and Jini 1.1
// 19 May 1999/FK 

package dsv.pis.gotag.bailiff;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.jini.core.entry.*;
import net.jini.core.lookup.*;
import net.jini.core.discovery.*;
import net.jini.lease.*;
import net.jini.lookup.*;
import net.jini.lookup.entry.*;
import dsv.pis.gotag.player.Player;
import dsv.pis.gotag.util.CmdlnOption;
import dsv.pis.gotag.util.Commandline;
import dsv.pis.gotag.util.Logger;

/**
 * The Bailiff is a Jini service that provides an execution environment for
 * agents. The service it provides is this: A serializable class may call the
 * Bailiff's migrate() method to transfer itself to the JVM of the Bailiff and
 * there gain its own thread of execution in a callback procedure of its own
 * choice.
 *
 * The Bailiff is not mobile. Once started, the Jini JoinManager keeps it alive.
 *
 * [bail-iff n 1 law officer who helps a sheriff in issuing writs and making
 * arrests. 2 (Brit.) landlord's agent or steward; manager of an estate or farm.
 * 3 (US) official in a lawcourt, esp one who takes people to their seats and
 * announces the arrival of the judge.]
 *
 * @author Fredrik Kilander, DSV
 */
public class Bailiff extends java.rmi.server.UnicastRemoteObject // for RMI
		implements dsv.pis.gotag.bailiff.BailiffInterface // for clients
{
	protected boolean debug = false;
	protected Logger log;
	protected String user;
	protected String room;
	protected String host;
	protected Map propertyMap;
	protected JoinManager bf_joinmanager;
	protected InetAddress myInetAddress;
	
	protected BailiffFrame bff;
	protected Map<UUID, Object> registeredPlayers;

	protected void debugMsg(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	/**
	 * Returns the room name string of this Bailiff.
	 * 
	 * @return The room name string.
	 */
	public String getRoom() {
		return room;
	}

	/**
	 * Returns the user name string of this Bailiff.
	 * 
	 * @return The user name string.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Returns the host name string of this Bailiff
	 * 
	 * @return The host name string.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * A helper class to receive callbacks from the JoinManager.
	 *
	 * @author Fredrik Kilander, DSV
	 * @see JoinManager
	 * @see ServiceIDListener
	 */
	private class IDListener implements ServiceIDListener // for JoinManager
	{
		/**
		 * The ServiceID returned by the JoinManager.
		 */
		protected ServiceID myServiceID;

		/**
		 * Creates a new IDListener.
		 */
		public IDListener() {
		}

		/**
		 * This method is called by the JoinManager once it has registered the
		 * service with a Lookup server and obtained a service ID.
		 * 
		 * @param sidIn
		 *            The service's ServiceID.
		 */
		public void serviceIDNotify(ServiceID sidIn) {
			myServiceID = sidIn;
			if (debug) {
				debugMsg("serviceIDNotify sid='" + myServiceID + "'");
				log.entry("<serviceIDNotify sidIn=\"" + sidIn + "\"/>");
			}
		}

		/**
		 * Returns the ServiceID.
		 * 
		 * @return The ServiceID or null if not set.
		 */
		public ServiceID getServiceID() {
			return myServiceID;
		}
	} // IDListener

	/**
	 * This class wraps and encapsulates the remote object to which the Bailiff
	 * lends a thread of execution.
	 */
	private class agitator extends Thread {

		protected Object myObj; // The client object
		protected String myCb; // The name of the entry point method
		protected Object[] myArgs; // Arguments to the entry point method
		protected java.lang.reflect.Method myMethod; // Ref. to entry point
														// method
		protected Class[] myParms; // Class reflection of arguments

		/**
		 * Creates a new agitator by copying th references to the client object,
		 * the name of the entry method and the arguments to the entry method.
		 * 
		 * @param obj
		 *            The client object, holding the method to execute
		 * @param cb
		 *            The name of the entry point method (callback)
		 * @param args
		 *            Arguments to the entry point method
		 */
		public agitator(Object obj, String cb, Object[] args) {
			myObj = obj;
			myCb = cb;
			myArgs = args;

			// If the array of arguments are non-zero we must create an array
			// of Class so that we can match the entry point method's name with
			// the parameter signature. So, the myParms[] array is loaded with
			// the class of each entry point parameter.

			if (0 < args.length) {
				myParms = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					myParms[i] = args[i].getClass();
				}
			} else {
				myParms = null;
			}
		}

		/**
		 * This method locates the method that is the client object's requested
		 * entry point. It also sets the classloader of the current instance to
		 * follow the client's classloader.
		 * 
		 * @throws NoSuchMethodException
		 *             Thrown if the entry point specified in the constructor
		 *             can not be found.
		 */
		public void initialize() throws java.lang.NoSuchMethodException {
//			registeredPlayers.put(((Player)myObj).getName(), myObj);
			
			myMethod = myObj.getClass().getMethod(myCb, myParms);
			setContextClassLoader(myObj.getClass().getClassLoader());
		}

		/**
		 * Overrides the default run() method in class Thread (a superclass to
		 * us). Then we invoke the requested entry point on the client object.
		 */
		public void run() {
			try {
				registeredPlayers.put(((Player)myObj).getName(), myObj);
				//update gui
				bff.updatePlayers(registeredPlayers.size());
				if(((Player)myObj).isIt())
					bff.updateColor(true);
				
//				System.out.println(((Player)myObj).getName() + " arrived.");
				myMethod.invoke(myObj, myArgs);
			} catch (Throwable t) {
				if (debug) {
					log.entry(t);
				}
			}
			registeredPlayers.remove(((Player)myObj).getName());
			//update gui
			bff.updatePlayers(registeredPlayers.size());
			if(((Player)myObj).isIt())
				bff.updateColor(false);
//			System.out.println(((Player)myObj).getName() + " left.");
		}
	} // class agitator

	// In BailiffInterface:

	/**
	 * Returns a string acknowledging the host, IP address, room and user fields
	 * of this Bailiff instance. This method can be used to debug the identity
	 * of the Bailiff from a client and to verify that the connection is still
	 * operational.
	 * 
	 * @returns The ping response.
	 * @throws RemoteException
	 */
	public String ping() throws java.rmi.RemoteException {
		if (debug) {
			log.entry("<ping/>");
		}

		return ("Ping echo from Bailiff on host=" + host + " ["
				+ myInetAddress.getHostAddress() + "] " + " room=" + room
				+ " user=" + user + ".");
	}

	// In BailiffInterface:

	/**
	 * Returns the string property stored under key.
	 * 
	 * @param key
	 *            The key to look up.
	 * @returns The property value.
	 */
	public String getProperty(String key) {
		if (debug) {
			log.entry("<getProperty key=\"" + key + "\"/>");
		}
		return (String) propertyMap.get(key.toLowerCase());
	}

	// In BailiffInterface:

	/**
	 * Sets the property value to be stored under key.
	 * 
	 * @param key
	 *            The name of the property.
	 * @param value
	 *            The value of the property.
	 */
	public void setProperty(String key, String value) {
		if (debug) {
			log.entry("<setProperty key=\"" + key.toLowerCase() + "\" value=\""
					+ value + "\"/>");
		}
		propertyMap.put(key.toLowerCase(), value);
	}

	// In BailiffInterface:

	/**
	 * Entry point for remote clients who want to pass an object to be executed
	 * by the Bailiff. The Bailiff starts a new thread for the object and calls
	 * the specified entry (callback) method. When that method returns, the
	 * thread exits and the object becomes inert.
	 * 
	 * @param obj
	 *            The object to execute.
	 * @param cb
	 *            The name of the entry (callback) method to call.
	 * @param args
	 *            Array of arguments to the entry method. The elements in the
	 *            array must match the entry method's signature.
	 * @throws NoSuchMethodException
	 *             Thrown if the specified entry method does not exist with the
	 *             expected signature.
	 */
	public void migrate(Object obj, String cb, Object[] args)
			throws java.rmi.RemoteException, java.lang.NoSuchMethodException {
		if (debug) {
			log.entry("<migrate obj=\"" + obj + "\" cb=\"" + cb + "\" args=\""
					+ args + "\"/>");
		}
//		System.out.println("TEST");
		agitator agt = new agitator(obj, cb, args);
		agt.initialize();
		agt.start();
	}
	
	@Override
	public List<UUID> playersInBailiff() throws RemoteException {
		List<UUID> result = new ArrayList<UUID>();
		result.addAll(registeredPlayers.keySet());
		return result;
	}
	
	@Override
	public boolean tag(UUID name) throws RemoteException {
		Player target = (Player) registeredPlayers.get(name);
		return target.tag();
	}
	
	@Override
	public boolean isIt(UUID name) throws RemoteException {
		Player p = (Player) registeredPlayers.get(name);
		return p.isIt();
	}
	
	@Override
	public String getRoomName() throws RemoteException {
		return getRoom();
	}

	/**
	 * Creates a new Bailiff service instance.
	 * 
	 * @param room
	 *            Informational text field used to designate the 'room'
	 *            (physical or virtual) the Bailiff is running in.
	 * @param user
	 *            Information text field used to designate the 'user' who is
	 *            associated with the Bailiff instance.
	 * @param debug
	 *            If true, diagnostic messages will be logged to the provided
	 *            Logger instance. This parameter is overridden if the class
	 *            local debug variable is set to true in the source code.
	 * @param log
	 *            If debug is true, this parameter can be a Logger instance
	 *            configured to accept entries. If log is null a default Logger
	 *            instance is created.
	 * @throws RemoteException
	 * @throws UnknownHostException
	 *             Thrown if the local host address can not be determined.
	 * @throws IOException
	 *             Thrown if there is an I/O problem.
	 */
	public Bailiff(String room, String user, boolean debug, Logger log)
			throws java.rmi.RemoteException, java.net.UnknownHostException,
			java.io.IOException {
		this.log = (log == null) ? new Logger() : log;
		this.user = user;
		this.room = room;
		myInetAddress = java.net.InetAddress.getLocalHost();
		host = myInetAddress.getHostName().toLowerCase();
		this.debug = (this.debug == true) ? true : debug;
		this.registeredPlayers = new ConcurrentHashMap<UUID, Object>();

		propertyMap = Collections.synchronizedMap(new HashMap());
		propertyMap.put("hostname", host);
		propertyMap.put("hostaddress", myInetAddress.getHostAddress());

		log.entry("STARTING host=" + host + ", room=" + room + ", user=" + user
				+ ", debug=" + debug + ".");

		// Create Jini service attributes.

		Entry[] bf_attributes = new Entry[] { new Name("Bailiff"),
				new Location(host, room, user)
		// ,
		// new BailiffServiceType (host, room, user)
		};

		// Create a Jini JoinManager that will help us to register ourselves
		// with all discovered Jini lookup servers.

		bf_joinmanager = new JoinManager(this, // the service object
				bf_attributes, // the attribute sets
				new IDListener(), // Service ID callback
				null, // Default Service Discovery Manager
				null // Default Lease Renewal Manager
		);
		this.bff = new BailiffFrame(this);
	}

	/**
	 * Shuts down this Bailiff service.
	 */
	public void shutdown() {
		bf_joinmanager.terminate();
	}

	/**
	 * Returns a string representation of this service instance.
	 * 
	 * @returns A string representing this Bailiff instance.
	 */
	public String toString() {
		return "Bailiff for user " + user + " in room " + room + " on host "
				+ host + ".";
	}

	/**
	 * This is the main program of the Bailiff launcher. It starts the Bailiff
	 * and registers it with the Jini lookup server(s). When the main routine
	 * exits the JVM will keep on running because the JoinManager will be
	 * running and referring to the Bailiff. There may also be agitator threads
	 * active. Some house- holding counters and a shutdown method are attractive
	 * extensions.
	 *
	 * @param args
	 *            The array of commandline strings, Java standard.
	 * @exception java.net.UnknownHostException
	 *                Thrown if the name of the local host cannot be obtained.
	 * @exception java.rmi.RemoteException
	 *                Thrown if there is a RMI problem.
	 * @exception java.io.IOException
	 *                Thrown if discovery/join could not start.
	 * @see BailiffServiceID
	 * @see Bailiff_svc
	 */
	public static void main(String[] argv)
			throws java.net.UnknownHostException, java.rmi.RemoteException,
			java.io.IOException {
		String room = "anywhere";
		String user = System.getProperty("user.name");
		boolean debug = false;

		CmdlnOption helpOption = new CmdlnOption("-help");
		CmdlnOption noFrameOption = new CmdlnOption("-noframe");
		CmdlnOption debugOption = new CmdlnOption("-debug");
		CmdlnOption roomOption = new CmdlnOption("-room", CmdlnOption.OPTIONAL
				| CmdlnOption.PAR_REQ);
		CmdlnOption userOption = new CmdlnOption("-user", CmdlnOption.OPTIONAL
				| CmdlnOption.PAR_REQ);
		CmdlnOption logOption = new CmdlnOption("-log", CmdlnOption.OPTIONAL
				| CmdlnOption.PAR_OPT);

		CmdlnOption[] opts = new CmdlnOption[] { helpOption, debugOption,
				roomOption, userOption, logOption };

		String[] restArgs = Commandline.parseArgs(System.out, argv, opts);

		if (restArgs == null) {
			System.exit(1);
		}

		if (helpOption.getIsSet() == true) {
			System.out
					.println("Usage: [-room room][-user user][-debug][-log [logfile]]");
			System.out.print("Where room is location of the service ");
			if (room == null) {
				System.out.println("(no default).");
			} else {
				System.out.println("(default = '" + room + "').");
			}

			System.out.print("      -user specifies the owning user ");
			if (user == null) {
				System.out.println("(no default available).");
			} else {
				System.out.println("(default = '" + user.toLowerCase() + "').");
			}

			System.out.println("      -debug turns on debugging mode.");
			System.out.println("      -log turns on logging to file.");

			System.exit(0);
		}

		debug = debugOption.getIsSet();

		if (roomOption.getIsSet() == true) {
			room = roomOption.getValue().toLowerCase();
		}

		if (userOption.getIsSet() == true) {
			user = userOption.getValue().toLowerCase();
		}

		Logger log;

		if (logOption.getIsSet() == true) {
			String lg = logOption.getValue();
			log = (lg != null) ? new Logger(lg, true) : new Logger(".",
					"Bailiff");
		} else {
			log = new Logger();
		}

		// Set the RMI security manager.
		System.setSecurityManager(new RMISecurityManager());
		Bailiff bf = new Bailiff(room, user, debug, log);
//		if (noFrameOption.getIsSet() == false) {
//			BailiffFrame bff = new BailiffFrame(bf);
//		}
	} // main

} // public class Bailiff

