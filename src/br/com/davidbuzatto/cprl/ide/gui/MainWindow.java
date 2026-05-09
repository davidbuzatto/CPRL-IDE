package br.com.davidbuzatto.cprl.ide.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import edu.citadel.compiler.Compiler;
import edu.citadel.cvm.CVM;
import edu.citadel.cvm.assembler.Assembler;
import edu.citadel.cvm.assembler.ast.Instruction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * CPRL IDE main window.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class MainWindow extends javax.swing.JFrame {
    
    private static record FileDetails( File file, String parentDirPath, String fileNameWithoutExt ) {};
    
    private static record SourceCodeAreaAndFileDetails( 
        RSyntaxTextArea sourceCodeArea, 
        JTextPane consoleTextPane,
        JTextPane assemblyTextPane,
        JSplitPane horizontalSplit,
        JSplitPane verticalSplit,
        FileDetails fileDetails
    ) {};
    
    private static final Font DEFAULT_FONT = new Font( "Consolas", Font.PLAIN, 20 );
    private final AbstractTokenMakerFactory ATMF;
    
    private Map<JComponent, SourceCodeAreaAndFileDetails> openedFiles;
    private SourceCodeAreaAndFileDetails currentOpenedFile;
    private boolean discardFirstChange;
    
    public MainWindow() {
        
        initComponents();
        
        ATMF = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        ATMF.putMapping( "text/cprl", "br.com.davidbuzatto.cprl.ide.gui.CPRLTokenMaker" );
        
        openedFiles = new HashMap<>();
        currentOpenedFile = null;
        discardFirstChange = true;
        
        try {
            openFile( new File( "C:/Users/David/Desktop/trabalhando/Hanoi.cprl" ) );
            //openFile( new File( "C:/Users/David/Desktop/trabalhando/test.cprl" ) );
        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        
    }

    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSaveAll = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        sep01 = new javax.swing.JToolBar.Separator();
        btnCompile = new javax.swing.JButton();
        btnRun = new javax.swing.JButton();
        tabbedPaneSourceCode = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuEdit = new javax.swing.JMenu();
        menuHelp = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CPRL IDE");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        toolbar.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/page_white_add.png"))); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(this::btnNewActionPerformed);
        toolbar.add(btnNew);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/folder.png"))); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(this::btnOpenActionPerformed);
        toolbar.add(btnOpen);

        btnSaveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/disk_multiple.png"))); // NOI18N
        btnSaveAll.setFocusable(false);
        btnSaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveAll.addActionListener(this::btnSaveAllActionPerformed);
        toolbar.add(btnSaveAll);

        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/cancel.png"))); // NOI18N
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClose.addActionListener(this::btnCloseActionPerformed);
        toolbar.add(btnClose);
        toolbar.add(sep01);

        btnCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/wrench.png"))); // NOI18N
        btnCompile.setFocusable(false);
        btnCompile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCompile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCompile.addActionListener(this::btnCompileActionPerformed);
        toolbar.add(btnCompile);

        btnRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/control_play_blue.png"))); // NOI18N
        btnRun.setFocusable(false);
        btnRun.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(btnRun);

        tabbedPaneSourceCode.addChangeListener(this::tabbedPaneSourceCodeStateChanged);

        menuFile.setText("File");
        menuBar.add(menuFile);

        menuEdit.setText("Edit");
        menuBar.add(menuEdit);

        menuHelp.setText("Help");
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
            .addComponent(tabbedPaneSourceCode)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPaneSourceCode, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCompileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompileActionPerformed
        compile( currentOpenedFile.fileDetails() );
        assemble( currentOpenedFile.fileDetails() );
        run( currentOpenedFile.fileDetails() );
    }//GEN-LAST:event_btnCompileActionPerformed

    private void tabbedPaneSourceCodeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneSourceCodeStateChanged
        
        if ( discardFirstChange ) {
            discardFirstChange = false;
            return;
        }
        
        JComponent c = (JComponent) tabbedPaneSourceCode.getSelectedComponent();
        currentOpenedFile = openedFiles.get( c );
        
    }//GEN-LAST:event_tabbedPaneSourceCodeStateChanged

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle( "Open" );
        jfc.setMultiSelectionEnabled( true );
        jfc.setFileFilter( new FileNameExtensionFilter( "CPRL Source Code" , "cprl" ) );
        
        jfc.showOpenDialog( this );
        
        for ( File selectedFile : jfc.getSelectedFiles() ) {
            if ( selectedFile != null ) {
                try {
                    openFile( selectedFile );
                } catch ( IOException exc ) {
                    showErrorMessage( exc );
                }
            }
        }
        
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnSaveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAllActionPerformed
        
        for ( int i = 0; i < tabbedPaneSourceCode.getTabCount(); i++ ) {
            
            JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( i );
            SourceCodeAreaAndFileDetails fileData = openedFiles.get( c );
            
            try ( FileWriter fw = new FileWriter( fileData.fileDetails.file ) ) {
                fw.write( fileData.sourceCodeArea.getText() );
            } catch ( IOException exc ) {
                showErrorMessage( exc );
            }
            
        }
        
    }//GEN-LAST:event_btnSaveAllActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        adjustAllSplitPanes();
    }//GEN-LAST:event_formComponentResized
    
    private void openFile( File file ) throws IOException {
        
        if ( openedFiles.containsKey( file.getAbsolutePath() ) ) {
            return;
        }
        
        FileDetails fileDetails = getFileDetails( file );
        
        RSyntaxTextArea sourceCodeArea = new RSyntaxTextArea( 1, 1 );
        sourceCodeArea.setCodeFoldingEnabled( true );
        sourceCodeArea.setBackground( new Color( 0x3F3F3F, false ));
        sourceCodeArea.setCurrentLineHighlightColor( Color.BLACK );
        sourceCodeArea.setFont( DEFAULT_FONT );
        sourceCodeArea.setAntiAliasingEnabled( true );
        sourceCodeArea.setCodeFoldingEnabled( false );
        sourceCodeArea.setAutoIndentEnabled( false );
        sourceCodeArea.setMatchedBracketBGColor( Color.PINK.darker() );
        sourceCodeArea.setTabsEmulated( true );
        sourceCodeArea.setTabSize( 4 );
        
        sourceCodeArea.setSyntaxEditingStyle( "text/cprl" );
        applyColorScheme( sourceCodeArea );
        
        RTextScrollPane sp = new RTextScrollPane( sourceCodeArea );
        
        // building tab
        JPanel container = new JPanel();
        container.setLayout( new BorderLayout() );
        
        JSplitPane horizontalSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        JSplitPane verticalSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        
        JTextPane consoleTextPane = new JTextPane();
        consoleTextPane.setFont( DEFAULT_FONT );
        JScrollPane consoleScroll = new JScrollPane( consoleTextPane );
        
        JTextPane assemblyTextPane = new JTextPane();
        assemblyTextPane.setFont( DEFAULT_FONT );
        JScrollPane assemblyScroll = new JScrollPane( assemblyTextPane );
        
        verticalSplit.setTopComponent( sp );
        verticalSplit.setBottomComponent( consoleScroll );
        
        horizontalSplit.setLeftComponent( verticalSplit );
        horizontalSplit.setRightComponent( assemblyScroll );
        
        container.add( horizontalSplit, BorderLayout.CENTER );
        tabbedPaneSourceCode.addTab( fileDetails.file.getName(), container );
        tabbedPaneSourceCode.setSelectedComponent( container );
        
        SourceCodeAreaAndFileDetails sourceDetails = new SourceCodeAreaAndFileDetails( 
            sourceCodeArea, 
            consoleTextPane, 
            assemblyTextPane, 
            horizontalSplit, 
            verticalSplit, 
            fileDetails
        );
        openedFiles.put( container, sourceDetails );
        currentOpenedFile = sourceDetails;
        loadSourceCode( fileDetails.file(), sourceCodeArea );
        
        adjustSplitPanes( currentOpenedFile );
        
    }
    
    private void loadSourceCode( File file, RSyntaxTextArea sourceCodeArea ) throws IOException {
        
        Scanner s = new Scanner( file );
        StringBuilder sb = new StringBuilder();
        
        while ( s.hasNextLine() ) {
            sb.append( s.nextLine() ).append( "\n" );
        }
        
        sourceCodeArea.setText( sb.toString() );
        
    }
    
    private void compile( FileDetails fileDetails ) {

        try {

            Compiler c = new Compiler( new File( String.format( "%s/%s.cprl", fileDetails.parentDirPath, fileDetails.fileNameWithoutExt ) ) );
            c.compile();

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        
    }
    
    private void assemble( FileDetails fileDetails ) {

        try {

            Assembler a = new Assembler( new File( String.format( "%s/%s.asm", fileDetails.parentDirPath, fileDetails.fileNameWithoutExt ) ) );
            a.assemble();

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        
    }
    
    private void run( FileDetails fileDetails ) {

        try {

            FileInputStream o = new FileInputStream( new File( String.format( "%s/%s.obj", fileDetails.parentDirPath, fileDetails.fileNameWithoutExt ) ) );

            Instruction.resetMaps();
            CVM vm = new CVM( 8192 ); // 8KB of memory
            vm.loadProgram( o );
            vm.run();

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        
    }

    private void showErrorMessage( Exception exc ) {
        JOptionPane.showMessageDialog( 
            null, 
            exc.getMessage(), 
            "ERRO", 
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void adjustSplitPanes( SourceCodeAreaAndFileDetails fileData ) {
        SwingUtilities.invokeLater( () -> {
            fileData.horizontalSplit.setDividerLocation( 0.8 );
            fileData.verticalSplit.setDividerLocation( 0.8 );
        });
    }
    
    private void adjustAllSplitPanes() {
        for ( int i = 0; i < tabbedPaneSourceCode.getTabCount(); i++ ) {
            JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( i );
            SourceCodeAreaAndFileDetails fileData = openedFiles.get( c );
            adjustSplitPanes( fileData );
        }
    }
    
    private FileDetails getFileDetails( File file ) {
        String parentDirPath = file.getParentFile().getPath();
        String fileNameWithoudExt = file.getName();
        fileNameWithoudExt = fileNameWithoudExt.substring( 0, fileNameWithoudExt.lastIndexOf( "." ) );
        return new FileDetails( file, parentDirPath, fileNameWithoudExt );
    }
    
    private void applyColorScheme( RSyntaxTextArea sourceCodeArea ) {
        SyntaxScheme scheme = sourceCodeArea.getSyntaxScheme();
        scheme.getStyle( Token.COMMENT_EOL ).foreground = new Color( 0x808080, false );
        scheme.getStyle( Token.IDENTIFIER ).foreground = new Color( 0xFFFFFF, false );
        scheme.getStyle( Token.LITERAL_BOOLEAN ).foreground = new Color( 0x79B8FF, false );
        scheme.getStyle( Token.LITERAL_CHAR ).foreground = new Color( 0x9ECBFF, false );
        scheme.getStyle( Token.LITERAL_NUMBER_DECIMAL_INT ).foreground = new Color( 0x79B8FF, false );
        scheme.getStyle( Token.LITERAL_STRING_DOUBLE_QUOTE ).foreground = new Color( 0x9ECBFF, false );
        scheme.getStyle( Token.OPERATOR ).foreground = new Color( 0xFF8040, false );
        scheme.getStyle( Token.RESERVED_WORD ).foreground = new Color( 0xF97583, false );
        scheme.getStyle( Token.DATA_TYPE ).foreground = new Color( 0xB392F0, false );
        scheme.getStyle( Token.SEPARATOR ).foreground = new Color( 0xFFFFFF, false );
        sourceCodeArea.revalidate();
    }
    
    public static void main( String args[] ) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater( () -> new MainWindow().setVisible( true ) );
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCompile;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSaveAll;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuEdit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JToolBar.Separator sep01;
    private javax.swing.JTabbedPane tabbedPaneSourceCode;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
