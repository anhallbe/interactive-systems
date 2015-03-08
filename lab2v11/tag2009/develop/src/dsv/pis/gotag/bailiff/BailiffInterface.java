// BailiffInterface.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for PIS course.
// 2000-12-13/FK Adapted from earlier version.

package dsv.pis.gotag.bailiff;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

/**
 * This interface is for the Bailiff's clients. This is mobile code which move
 * into the Bailiff's JVM for execution.
 */
public interface BailiffInterface extends java.rmi.Remote {
	/**
	 * Returns a string which confirms communication with the Bailiff service
	 * instance.
	 */
	public String ping() throws java.rmi.RemoteException;

	/**
	 * Returns a property of the Bailiff.
	 * 
	 * @param key
	 *            The case-insensitive property key to retrieve.
	 * @return The property string or null.
	 */
	public String getProperty(String key) throws java.rmi.RemoteException;

	/**
	 * The entry point for mobile code. The client sends and object (itself
	 * perhaps), a string naming the callback method and an array of arguments
	 * which must map against the parameters of the callback method.
	 *
	 * @param obj
	 *            The object (to execute).
	 * @param cb
	 *            The name of the method to call as the program of obj.
	 * @param args
	 *            The parameters for the callback method. Note that if the
	 *            method has a signature without arguments the value of args
	 *            should be an empty array. Setting args to null will not work.
	 * @exception java.rmi.RemoteException
	 *                Thrown if there is an RMI problem.
	 * @exception java.lang.NoSuchMethodException
	 *                Thrown if the proposed callback is not found (which happen
	 *                if the name is spelled wrong, the number of arguments is
	 *                wrong or are of the wrong type).
	 * 
	 */
	public void migrate(Object obj, String cb, Object[] args)
			throws java.rmi.RemoteException, java.lang.NoSuchMethodException;

	public List<UUID> playersInBailiff() throws RemoteException;
	
	public boolean isIt(UUID name) throws RemoteException;
	
	public boolean tag(UUID name) throws RemoteException;
	
	public String getRoomName() throws RemoteException;
}
