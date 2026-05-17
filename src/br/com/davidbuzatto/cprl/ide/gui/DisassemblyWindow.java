package br.com.davidbuzatto.cprl.ide.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Disassembly Window.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class DisassemblyWindow extends javax.swing.JFrame {
    
    private RSyntaxTextArea assemblySourceCodeArea;
    private RTextScrollPane assemblySourceCodeAreaSP;
    
    public DisassemblyWindow( String title, Font font ) {
        
        initComponents();
        setTitle( title );
        setIconImage( new ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/firefly-48.png") ).getImage() );
        
        assemblySourceCodeArea = new RSyntaxTextArea();
        assemblySourceCodeArea.setCodeFoldingEnabled( false );
        assemblySourceCodeArea.setFont( font );
        assemblySourceCodeArea.setAntiAliasingEnabled( true );
        assemblySourceCodeArea.setAutoIndentEnabled( false );
        assemblySourceCodeArea.setTabsEmulated( true );
        assemblySourceCodeArea.setTabSize( 4 );
        assemblySourceCodeArea.setSyntaxEditingStyle( "text/cprl" );
        assemblySourceCodeArea.setEditable( false );
        
        assemblySourceCodeAreaSP = new RTextScrollPane( assemblySourceCodeArea );
        
        add( assemblySourceCodeAreaSP, BorderLayout.CENTER );
        
    }

    public RSyntaxTextArea getAssemblySourceCode() {
        return assemblySourceCodeArea;
    }
    
    public RTextScrollPane getAssemblySourceCodeSP() {
        return assemblySourceCodeAreaSP;
    }

    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setSize(new java.awt.Dimension(550, 600));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
