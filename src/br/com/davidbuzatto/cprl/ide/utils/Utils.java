package br.com.davidbuzatto.cprl.ide.utils;

import br.com.davidbuzatto.cprl.ide.gui.MainWindow;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;

/**
 * Utility methods.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Utils {
    
    public static void updateSplashScreen( int millisecondsToWait ) {
        
        SplashScreen sp = SplashScreen.getSplashScreen();
        if ( sp != null ) {
            
            Graphics2D g2d = (Graphics2D) sp.createGraphics();
            g2d.setRenderingHint( 
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON );
            g2d.setColor( new Color( 30, 30, 30 ) );
            
            Font f = new Font( "Century Gothic", Font.BOLD, 40 ) ;
            if ( f.getFamily().equals( Font.DIALOG ) ) {
                f = new Font( Font.MONOSPACED, Font.BOLD, 40 ) ;
            }
            g2d.setFont( f );
            
            FontMetrics fm = g2d.getFontMetrics();
            String v = MainWindow.VERSION;
            int vWidth = fm.stringWidth( v );
            
            g2d.drawString( v, 450 - vWidth, 140 );
            g2d.dispose();
            
            sp.update();
            
            try {
                Thread.sleep( millisecondsToWait );
            } catch ( InterruptedException exc ) {
            }
            
        }
        
    }
    
}
