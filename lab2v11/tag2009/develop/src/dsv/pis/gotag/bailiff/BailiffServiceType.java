// BailiffServiceType.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for Pis course.
// 2001-03-28/FK

package dsv.pis.gotag.bailiff;

import java.beans.BeanInfo;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.rmi.*;
import java.util.*;

import javax.swing.ImageIcon;

import net.jini.core.entry.*;
import net.jini.core.lookup.*;
import net.jini.core.discovery.*;
import net.jini.lookup.entry.*;

/**
 * This class subclasses one of the Jini utility classes, intended
 * to make life slightly easier for services.
 */
public class BailiffServiceType extends ServiceType {

  /**
   * The host the Bailiff executes on.
   */
  public String host;

  /**
   * The user that own's the Bailiff.
   */
  public String user;

  /**
   * The room the Bailiff is in, or represents.
   */
  public String room;

  /**
   * The icon that represents this Bailiff.
   */
  public ImageIcon color32x32 = null;

  /**
   * Creates a new instance with default values.
   */
  public BailiffServiceType () {}

  /**
   * Creates a new instance with specified values.
   * @param H The host name.
   * @param R The room name.
   * @param U The user name.
   */
  public BailiffServiceType (String H, String R, String U) {
    host = H;
    room = R;
    user = U;
  }

  /**
   * Returns the instance's icon.
   * @param iconKind The kind of icon requested. @see java.beans.BeanInfo
   * @return The icon, if available as requested, or null otherwise.
   */
  public java.awt.Image getIcon (int iconKind) {
    switch (iconKind) {
    case BeanInfo.ICON_COLOR_32x32:
      if (color32x32 == null) {
	URL u =
	  getClass ().getResource ("/se/su/dsv/fk/bailiff/Bailiff32x32.gif");
	if (u != null) {
	  color32x32 = new ImageIcon (u);
	}
      }

      return (color32x32 != null) ? color32x32.getImage () : null;

    } // switch
    return null;
  }

  /**
   * Returns the display name string of the instance.
   * @return The display string.
   */
  public String getDisplayName () {
    if (user != null) {
      return user + "'s Bailiff";
    }
    else if (host != null) {
      return "Bailiff@" + host;
    }
    else {
      return "Bailiff";
    }
  }

  public String getShortDescription () {
    return
      "Bailiff" +
      ((host != null) ? "@" + host : "") +
      ((room != null) ? " in " + room : "");
  }
}
