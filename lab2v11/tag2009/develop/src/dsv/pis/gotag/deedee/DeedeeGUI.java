// DeedeeGUI.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for Pis course.
// 2001-04-02/FK

package dsv.pis.gotag.deedee;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * This class implements a simple emoting GUI for the Deedee agent. In order
 * to animate the image of Deedee's face, each 'mental state' consists of
 * two images which are alternated between each second.
 */
public class DeedeeGUI extends JApplet implements ActionListener {

  protected Timer timer;
  protected int   tix = 0;
  protected Image [] images = new Image [9];
  protected int indexOne;
  protected int indexTwo;
  protected AffineTransform xform;

  protected void loadImages () {
    String pkg = "/dsv/pis/gotag/deedee/";
    images[0] =
      new ImageIcon (getClass ().getResource (pkg + "Angry.gif")).getImage ();
    images[1] =
      new ImageIcon (getClass ().getResource (pkg + "Asleep.gif")).getImage ();
    images[2] =
      new ImageIcon (getClass ().getResource (pkg + "BasicSmile.gif")).getImage ();
    images[3] =
      new ImageIcon (getClass ().getResource (pkg + "Concerned.gif")).getImage ();
    images[4] =
      new ImageIcon (getClass ().getResource (pkg + "HappilySurprised.gif")).getImage ();
    images[5] =
      new ImageIcon (getClass ().getResource (pkg + "Laughing.gif")).getImage ();
    images[6] =
      new ImageIcon (getClass ().getResource (pkg + "Moody.gif")).getImage ();
    images[7] =
      new ImageIcon (getClass ().getResource (pkg + "Waiting.gif")).getImage ();
    images[8] =
      new ImageIcon (getClass ().getResource (pkg + "VeryAngry.gif")).getImage ();
  }

  public void init () {
    loadImages ();
    // Foreground and background colors.
    setBackground (Color.white);
    setForeground (Color.black);
    // Specify a neutral transform.
    xform = new AffineTransform (1f, 0f, 0f, 1f, 0, 0);
    // Set up the animation timer.
    timer = new Timer (1000, this);
    timer.setInitialDelay (0);
    timer.setCoalesce (true);
  }

  public void start () {
    startAnimation ();
  }
  public void stop () {
    stopAnimation ();
  }
  public synchronized void startAnimation () {
    if (!timer.isRunning ()) timer.start ();
  }
  public synchronized void stopAnimation () {
    if (timer.isRunning ()) timer.stop ();
  }

  /**
   * This method is called in response to the repaint() request to draw
   * our graphics. The tix (ticks) counter is used to alternate between
   * the two images arranged for the current 'mental state' of the agent.
   */
  public void paint (Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Image img = images[((tix % 2) == 0) ? indexOne : indexTwo];
    g2.drawImage (img, xform, null);
  }

  /**
   * Called when the animation timer expires. We increment the tix (ticks)
   * counter and ask for a repaint, which will call our own paint() method.
   */
  public void actionPerformed (ActionEvent e) {
    tix += 1;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'giving up'.
   */
  public void showGivingUp () {
    indexOne = 0;
    indexTwo = 8;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'looking up'.
   */
  public void showLookup () {
    indexOne = 2;
    indexTwo = 1;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'waiting'.
   */
  public void showWaiting () {
    indexOne = 7;
    indexTwo = 1;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'deliberation'.
   */
  public void showDeliberation () {
    indexOne = 4;
    indexTwo = 5;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'prepare jump'.
   */
  public void showPrepareJump () {
    indexOne = 2;
    indexTwo = 0;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'gone'.
   */
  public void showGone () {
    indexOne = 2;
    indexTwo = 1;
    repaint ();
  }

  /**
   * Sets the GUI to animate mental state 'pain'.
   */
  public void showPain () {
    indexOne = 8;
    indexTwo = 6;
    repaint ();
  }

  public void showMessage (String msg) {
    JOptionPane.showMessageDialog (this, msg);
  }
}
