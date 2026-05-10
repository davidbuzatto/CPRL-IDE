package br.com.davidbuzatto.cprl.ide.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Disassembly Window.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class DisassemblyWindow extends javax.swing.JFrame {
    
    private RSyntaxTextArea assemblySourceCode;
    
    public DisassemblyWindow( String title ) {
        
        initComponents();
        setTitle( title );
        setIconImage( new ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/firefly-48.png") ).getImage() );
        
        assemblySourceCode = new RSyntaxTextArea();
        assemblySourceCode.setCodeFoldingEnabled( false );
        assemblySourceCode.setBackground( new Color( 0x3F3F3F, false ) );
        assemblySourceCode.setCurrentLineHighlightColor( Color.BLACK );
        assemblySourceCode.setSelectionColor( Color.BLACK );
        assemblySourceCode.setFont( MainWindow.DEFAULT_FONT );
        assemblySourceCode.setAntiAliasingEnabled( true );
        assemblySourceCode.setAutoIndentEnabled( false );
        assemblySourceCode.setMatchedBracketBGColor( Color.PINK.darker() );
        assemblySourceCode.setTabsEmulated( true );
        assemblySourceCode.setTabSize( 4 );
        assemblySourceCode.setSyntaxEditingStyle( "text/cprl" );
        assemblySourceCode.setEditable( false );
        MainWindow.applyColorScheme( assemblySourceCode );
        
        RTextScrollPane assemblyScroll = new RTextScrollPane( assemblySourceCode );
        add( assemblyScroll, BorderLayout.CENTER );
        
    }

    public RSyntaxTextArea getAssemblySourceCode() {
        return assemblySourceCode;
    }

    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setSize(new java.awt.Dimension(466, 608));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
