package br.com.davidbuzatto.cprl.ide.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import edu.citadel.compiler.Compiler;
import edu.citadel.cvm.CVM;
import edu.citadel.cvm.assembler.Assembler;
import edu.citadel.cvm.assembler.ast.Instruction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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

    private static record SourceFileInfo(
        File file,
        String parentDirPath,
        String fileNameWithoutExt
    ) {};
    
    private static record EditorTab(
        RSyntaxTextArea sourceCodeArea,
        JTextPane consoleTextPane,
        JTextPane assemblyTextPane,
        JSplitPane horizontalSplit,
        JSplitPane verticalSplit,
        SourceFileInfo fileInfo
    ) {};
    
    private static final Font DEFAULT_FONT = new Font( "Consolas", Font.PLAIN, 20 );
    private final AbstractTokenMakerFactory ATMF;

    private Map<JComponent, EditorTab> editorTabs;
    private EditorTab activeTab;
    private Set<String> openedFilePaths;
    private boolean skipInitialTabChange;

    public MainWindow() {

        initComponents();

        ATMF = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        ATMF.putMapping( "text/cprl", "br.com.davidbuzatto.cprl.ide.gui.CPRLTokenMaker" );

        editorTabs = new HashMap<>();
        activeTab = null;
        openedFilePaths = new HashSet<>();
        skipInitialTabChange = true;

        try {
            openFile( new File( "cprl-sources/Correct_101.cprl" ) );
            openFile( new File( "cprl-sources/Correct_102.cprl" ) );
            openFile( new File( "cprl-sources/Correct_103.cprl" ) );
            openFile( new File( "cprl-sources/Hanoi.cprl" ) );
            openFile( new File( "cprl-sources/test.cprl" ) );
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
        sep01 = new javax.swing.JToolBar.Separator();
        btnCompileAndRun = new javax.swing.JButton();
        sep02 = new javax.swing.JToolBar.Separator();
        btnDisassembly = new javax.swing.JButton();
        tabbedPaneSourceCode = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
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
        btnNew.setToolTipText("New File");
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(this::btnNewActionPerformed);
        toolbar.add(btnNew);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/folder.png"))); // NOI18N
        btnOpen.setToolTipText("Open File");
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(this::btnOpenActionPerformed);
        toolbar.add(btnOpen);

        btnSaveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/disk_multiple.png"))); // NOI18N
        btnSaveAll.setToolTipText("Save All Files");
        btnSaveAll.setFocusable(false);
        btnSaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveAll.addActionListener(this::btnSaveAllActionPerformed);
        toolbar.add(btnSaveAll);
        toolbar.add(sep01);

        btnCompileAndRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/control_play_blue.png"))); // NOI18N
        btnCompileAndRun.setToolTipText("Compile and Run");
        btnCompileAndRun.setFocusable(false);
        btnCompileAndRun.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCompileAndRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCompileAndRun.addActionListener(this::btnCompileAndRunActionPerformed);
        toolbar.add(btnCompileAndRun);
        toolbar.add(sep02);

        btnDisassembly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/arrow_undo.png"))); // NOI18N
        btnDisassembly.setToolTipText("Disassembly");
        btnDisassembly.setFocusable(false);
        btnDisassembly.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDisassembly.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(btnDisassembly);

        tabbedPaneSourceCode.addChangeListener(this::tabbedPaneSourceCodeStateChanged);

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

    private void btnCompileAndRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompileAndRunActionPerformed
        compile( activeTab.fileInfo() );
        assemble( activeTab.fileInfo() );
        run( activeTab.fileInfo() );
    }//GEN-LAST:event_btnCompileAndRunActionPerformed

    private void tabbedPaneSourceCodeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneSourceCodeStateChanged

        if ( skipInitialTabChange ) {
            skipInitialTabChange = false;
            return;
        }

        JComponent c = (JComponent) tabbedPaneSourceCode.getSelectedComponent();
        activeTab = editorTabs.get( c );

    }//GEN-LAST:event_tabbedPaneSourceCodeStateChanged

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed

        JFileChooser jfc = new JFileChooser( "./" );
        jfc.setDialogTitle( "Open" );
        jfc.setMultiSelectionEnabled( true );
        jfc.setFileFilter( new FileNameExtensionFilter( "CPRL Source Code", "cprl" ) );

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
            EditorTab tab = editorTabs.get( c );

            try ( FileWriter fw = new FileWriter( tab.fileInfo.file ) ) {
                fw.write( tab.sourceCodeArea.getText() );
            } catch ( IOException exc ) {
                showErrorMessage( exc );
            }

        }

    }//GEN-LAST:event_btnSaveAllActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        adjustAllSplitPanes();
    }//GEN-LAST:event_formComponentResized

    private void openFile( File file ) throws IOException {

        if ( openedFilePaths.contains( file.getAbsolutePath() ) ) {
            return;
        }

        SourceFileInfo fileInfo = getSourceFileInfo( file );

        RSyntaxTextArea sourceCodeArea = new RSyntaxTextArea( 1, 1 );
        sourceCodeArea.setCodeFoldingEnabled( true );
        sourceCodeArea.setBackground( new Color( 0x3F3F3F, false ) );
        sourceCodeArea.setCurrentLineHighlightColor( Color.BLACK );
        sourceCodeArea.setSelectionColor( Color.BLACK );
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
        addClosableTab( fileInfo.file.getName(), container );
        tabbedPaneSourceCode.setSelectedComponent( container );

        EditorTab tab = new EditorTab(
            sourceCodeArea,
            consoleTextPane,
            assemblyTextPane,
            horizontalSplit,
            verticalSplit,
            fileInfo
        );
        editorTabs.put( container, tab );
        activeTab = tab;
        openedFilePaths.add( tab.fileInfo.file().getAbsolutePath() );
        loadSourceCode( fileInfo.file(), sourceCodeArea );

        adjustSplitPanes( activeTab );

    }

    private void loadSourceCode( File file, RSyntaxTextArea sourceCodeArea ) throws IOException {

        Scanner s = new Scanner( file );
        StringBuilder sb = new StringBuilder();

        while ( s.hasNextLine() ) {
            sb.append( s.nextLine() ).append( "\n" );
        }

        sourceCodeArea.setText( sb.toString() );
        SwingUtilities.invokeLater( () -> sourceCodeArea.setCaretPosition( 0 ) );

    }

    private void compile( SourceFileInfo fileInfo ) {

        try {

            Compiler c = new Compiler( new File( String.format( "%s/%s.cprl", fileInfo.parentDirPath, fileInfo.fileNameWithoutExt ) ) );
            c.compile();

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }

    }

    private void assemble( SourceFileInfo fileInfo ) {

        try {

            Assembler a = new Assembler( new File( String.format( "%s/%s.asm", fileInfo.parentDirPath, fileInfo.fileNameWithoutExt ) ) );
            a.assemble();

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }

    }

    private void run( SourceFileInfo fileInfo ) {

        try {

            FileInputStream o = new FileInputStream( new File( String.format( "%s/%s.obj", fileInfo.parentDirPath, fileInfo.fileNameWithoutExt ) ) );

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

    private void adjustSplitPanes( EditorTab tab ) {
        SwingUtilities.invokeLater( () -> {
            tab.horizontalSplit.setDividerLocation( 0.8 );
            tab.verticalSplit.setDividerLocation( 0.8 );
        } );
    }

    private void adjustAllSplitPanes() {
        for ( int i = 0; i < tabbedPaneSourceCode.getTabCount(); i++ ) {
            JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( i );
            EditorTab tab = editorTabs.get( c );
            adjustSplitPanes( tab );
        }
    }

    private SourceFileInfo getSourceFileInfo( File file ) {
        String parentDirPath = file.getParentFile().getPath();
        String fileNameWithoutExt = file.getName();
        fileNameWithoutExt = fileNameWithoutExt.substring( 0, fileNameWithoutExt.lastIndexOf( "." ) );
        return new SourceFileInfo( file, parentDirPath, fileNameWithoutExt );
    }

    private void applyColorScheme( RSyntaxTextArea sourceCodeArea ) {

        SyntaxScheme scheme = sourceCodeArea.getSyntaxScheme();

        Font plain = DEFAULT_FONT;
        //Font bold = DEFAULT_FONT.deriveFont( Font.BOLD );
        //Font italic = DEFAULT_FONT.deriveFont( Font.ITALIC );

        scheme.getStyle( Token.RESERVED_WORD ).font = plain;
        scheme.getStyle( Token.COMMENT_EOL ).font = plain;
        scheme.getStyle( Token.IDENTIFIER ).font = plain;
        scheme.getStyle( Token.DATA_TYPE ).font = plain;
        scheme.getStyle( Token.OPERATOR ).font = plain;

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

    public void addClosableTab( String title, Component content ) {

        tabbedPaneSourceCode.addTab( title, content );
        int index = tabbedPaneSourceCode.indexOfComponent( content );

        JPanel tabPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        tabPanel.setOpaque( false );

        JLabel titleLabel = new JLabel( title );
        titleLabel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 5 ) );

        JButton closeButton = new JButton( "x" );
        closeButton.setFont( closeButton.getFont().deriveFont( 10f ) );
        closeButton.setFocusable( false );

        closeButton.addActionListener( e -> {
            int i = tabbedPaneSourceCode.indexOfTabComponent( tabPanel );
            if ( i != -1 ) {
                closeTab( i );
            }
        });

        tabPanel.add( titleLabel );
        tabPanel.add( closeButton );

        tabbedPaneSourceCode.setTabComponentAt( index, tabPanel );

    }

    private void closeTab( int index ) {
        JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( index );
        EditorTab tab = editorTabs.remove( c );
        openedFilePaths.remove( tab.fileInfo.file.getAbsolutePath() );
        tabbedPaneSourceCode.remove( index );
    }
    
    public static void main( String args[] ) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater( () -> new MainWindow().setVisible( true ) );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCompileAndRun;
    private javax.swing.JButton btnDisassembly;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSaveAll;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JToolBar.Separator sep01;
    private javax.swing.JToolBar.Separator sep02;
    private javax.swing.JTabbedPane tabbedPaneSourceCode;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
