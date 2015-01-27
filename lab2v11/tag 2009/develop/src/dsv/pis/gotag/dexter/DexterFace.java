// DexterFace.java
// Fredrik Kilander, DSV
// 2001-02-01/FK

package dsv.pis.gotag.dexter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A graphical representation of the Dexter agent.
 */
public class DexterFace extends JApplet implements ActionListener
{
  final static Color bg    = Color.white;
  final static Color fg    = Color.black;
  final static Color red   = Color.red;
  final static Color white = Color.white;

  final static BasicStroke stroke     = new BasicStroke (2.0f);
  final static BasicStroke wideStroke = new BasicStroke (8.0f);

  protected Timer timer;
  protected int   tix = 0;
  protected Arc2D.Double smile;

  /**
   *
   */
  public void init () {
    setBackground (bg);
    setForeground (fg);
    timer = new Timer (675, this);
    timer.setInitialDelay (0);
    timer.setCoalesce (true);
    smile = new Arc2D.Double ();
  }

  /**
   *
   */
  public void start () {
    startAnimation ();
  }

  /**
   *
   */
  public void stop () {
    stopAnimation ();
  }

  /**
   *
   */
  public synchronized void startAnimation () {
    if (!timer.isRunning ()) {
      timer.start ();
    }
  }

  /**
   *
   */
  public synchronized void stopAnimation () {
    if (timer.isRunning ()) {
      timer.stop ();
    }    
  }

  /**
   *
   */
  public void paint (Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Dimension d = getSize ();
    int x = d.width / 10;
    int y = d.height / 10;
    int rectWidth = (d.width * 9) / 10;
    int rectHeight = (d.height * 9) / 10;
    // Arc


    g2.setStroke (wideStroke);
    g2.setPaint (bg);
    g2.draw (smile);		// undraw previous smile
    g2.setPaint (red);
    smile.setArc (x, y,
		  rectWidth, rectHeight,
		  (tix * 5) % 360, 135,
		  Arc2D.OPEN);

    g2.draw (smile);

  }

  /**
   *
   */
  public void actionPerformed (ActionEvent e) {
    tix += 1;
    repaint ();
  }
}
