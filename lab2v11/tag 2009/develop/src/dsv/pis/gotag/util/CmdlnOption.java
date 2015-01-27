// CmdlnOption.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for Pis course.
// 2000-10-13/FK First version

package dsv.pis.gotag.util;

import java.lang.*;

public class CmdlnOption {
  public static final int OPTIONAL = 0;
  public static final int REQUIRED = 1;
  public static final int PAR_NOT = 0; // Accept no parameter
  public static final int PAR_OPT = 2; // Parameter is optional
  public static final int PAR_REQ = 6; // Parameter is required

  protected String optionKey;
  protected boolean allowArg = false;
  protected boolean expectArg = false;
  protected boolean isOptional = true;
  protected String value;
  protected boolean isSet = false;

  protected void parseFlags (int flags) {
    isOptional = (flags & REQUIRED) == 0;
    allowArg = (flags & PAR_OPT) == PAR_OPT;
    expectArg = (flags & PAR_REQ) == PAR_REQ;
  }

  public CmdlnOption (String key, int flags) {
    optionKey = key;
    parseFlags (flags);
  }

  public CmdlnOption (String key) {
    optionKey = key;
    parseFlags (OPTIONAL|PAR_NOT);
  }
  

  public boolean getIsOptional () {
    return isOptional;
  }

  public boolean getIsSet () {
    return isSet;
  }

  public String getValue () {
    return value;
  }

  public void setValue (String val) {
    value = val;
    isSet = true;
  }

  public String getKey () {
    return optionKey;
  }

  public boolean allowsArg () {
    return allowArg;
  }

  public boolean expectsArg () {
    return expectArg;
  }

  public boolean keyMatches (String arg) {
    return optionKey.equalsIgnoreCase (arg);
  }
}
