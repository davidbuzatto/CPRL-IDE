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
    
    private RSyntaxTextArea assemblySourceCode;
    
    public DisassemblyWindow( String title, Font font ) {
        
        initComponents();
        setTitle( title );
        setIconImage( new ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/firefly-48.png") ).getImage() );
        
        assemblySourceCode = new RSyntaxTextArea();
        assemblySourceCode.setCodeFoldingEnabled( false );
        assemblySourceCode.setBackground( new Color( 0x3F3F3F, false ) );
        assemblySourceCode.setCurrentLineHighlightColor( Color.BLACK );
        assemblySourceCode.setSelectionColor( Color.BLACK );
        assemblySourceCode.setFont( font );
        assemblySourceCode.setAntiAliasingEnabled( true );
        assemblySourceCode.setAutoIndentEnabled( false );
        assemblySourceCode.setMatchedBracketBGColor( Color.PINK.darker() );
        assemblySourceCode.setTabsEmulated( true );
        assemblySourceCode.setTabSize( 4 );
        assemblySourceCode.setSyntaxEditingStyle( "text/cprl" );
        assemblySourceCode.setEditable( false );
        MainWindow.applyColorScheme( assemblySourceCode, font );
        
        RTextScrollPane assemblySourceCodeSP = new RTextScrollPane( assemblySourceCode );
        assemblySourceCodeSP.getGutter().setLineNumberFont( font );
        
        add( assemblySourceCodeSP, BorderLayout.CENTER );
        
    }

    public RSyntaxTextArea getAssemblySourceCode() {
        return assemblySourceCode;
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
