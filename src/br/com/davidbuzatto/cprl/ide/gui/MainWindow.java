package br.com.davidbuzatto.cprl.ide.gui;

import br.com.davidbuzatto.cprl.custom.Compiler;
import br.com.davidbuzatto.cprl.custom.CVM;
import br.com.davidbuzatto.cprl.custom.Assembler;
import br.com.davidbuzatto.cprl.custom.Disassembler;
import com.formdev.flatlaf.FlatDarkLaf;
/*import edu.citadel.compiler.Compiler;
import edu.citadel.cvm.CVM;
import edu.citadel.cvm.assembler.Assembler;*/
import edu.citadel.cvm.assembler.ast.Instruction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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

    // -------------------------------------------------------------------------
    // Colors used in the internal console
    // -------------------------------------------------------------------------
    private static final Color CONSOLE_STDOUT_COLOR = new Color( 0xE8EFF1, false );
    private static final Color CONSOLE_STDERR_COLOR = new Color( 0xFF6060, false );
    private static final Color CONSOLE_ECHO_COLOR   = new Color( 0xA0D0FF, false );
    private static final Color CONSOLE_BG_COLOR     = new Color( 0x1E1E1E, false );

    // -------------------------------------------------------------------------
    // Custom OutputStream that appends styled text to a JTextPane
    // -------------------------------------------------------------------------

    /**
     * An {@link OutputStream} that forwards every write to a {@link JTextPane}
     * using a fixed foreground colour. All Swing updates are posted via
     * {@link SwingUtilities#invokeLater} so the stream is safe to use from
     * any thread (e.g. a {@link SwingWorker} background thread).
     */
    private static class ConsoleOutputStream extends OutputStream {

        private final JTextPane textPane;
        private final Color color;

        /**
         * Creates a new {@code ConsoleOutputStream}.
         *
         * @param textPane the target pane where text will be appended
         * @param color    the foreground colour for the appended text
         */
        ConsoleOutputStream( JTextPane textPane, Color color ) {
            this.textPane = textPane;
            this.color = color;
        }

        /**
         * Writes a single byte by delegating to {@link #write(byte[], int, int)}.
         *
         * @param b the byte to write
         * 
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void write( int b ) throws IOException {
            write( new byte[]{ (byte) b }, 0, 1 );
        }

        /**
         * Converts the byte range to a {@code String} and appends it to the
         * console pane on the Event Dispatch Thread.
         *
         * @param b   the data buffer
         * @param off offset in {@code b} of the first byte to write
         * @param len number of bytes to write
         * 
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void write( byte[] b, int off, int len ) throws IOException {
            String text = new String( b, off, len );
            SwingUtilities.invokeLater( () -> appendToConsole( textPane, text, color ) );
        }

    }

    // -------------------------------------------------------------------------
    // Records
    // -------------------------------------------------------------------------

    /**
     * Holds file-system information for a CPRL source file that is currently
     * open in an editor tab.
     *
     * @param file               the {@link File} handle pointing to the source
     * @param parentDirPath      absolute path of the directory that contains the file
     * @param fileNameWithoutExt file name without extension (used to derive
     *                           {@code .asm}, {@code .obj} and {@code .dis} paths)
     */
    private static record SourceFileInfo(
        File file,
        String parentDirPath,
        String fileNameWithoutExt
    ) {};

    /**
     * Aggregates every UI component and mutable state belonging to a single
     * editor tab.  The record itself is immutable; mutable values are wrapped
     * in {@link AtomicBoolean} or {@link AtomicReference} so they can be
     * updated safely from any thread.
     *
     * @param sourceCodeArea     the CPRL source editor
     * @param consoleTextPane    the read-only console output pane
     * @param assemblySourceCode the read-only assembly output editor
     * @param horizontalSplit    the horizontal {@link JSplitPane} (source | assembly)
     * @param verticalSplit      the vertical {@link JSplitPane} (source / console)
     * @param fileInfoRef        atomic reference to the associated {@link SourceFileInfo};
     *                           {@code null} for untitled files
     * @param isDirty            {@code true} when the editor content differs from
     *                           the last saved version
     * @param titleLabel         the tab header label (updated when dirty state changes)
     * @param consoleInputField  text field used to send input to the running CVM
     * @param consoleEnterButton button that submits the console input field
     * @param activePipedOut     atomic reference to the {@link PipedOutputStream}
     *                           connected to {@code System.in} during CVM execution;
     *                           {@code null} when the CVM is not running
     */
    private static record EditorTab(
        RSyntaxTextArea sourceCodeArea,
        JTextPane consoleTextPane,
        RSyntaxTextArea assemblySourceCode,
        JSplitPane horizontalSplit,
        JSplitPane verticalSplit,
        AtomicReference<SourceFileInfo> fileInfoRef,
        AtomicBoolean isDirty,
        JLabel titleLabel,
        JTextField consoleInputField,
        JButton consoleEnterButton,
        AtomicReference<PipedOutputStream> activePipedOut
    ) {};

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private static final boolean LOAD_TEST_FILES = true;
    private static final boolean DEBUG_ARTEFACTS_DELETION = true;
    
    public static final Font DEFAULT_FONT = new Font( "Consolas", Font.PLAIN, 20 );
    private final AbstractTokenMakerFactory ATMF;

    private Map<JComponent, EditorTab> editorTabs;
    private EditorTab activeTab;
    private Set<String> openedFilePaths;
    private boolean skipInitialTabChange;
    private int untitledCounter;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates the IDE main window, registers the CPRL token maker, and
     * initialises all internal state.
     */
    public MainWindow() {

        initComponents();
        initKeyboardShortcuts();
        setIconImage( new ImageIcon( 
            getClass().getResource( 
                "/br/com/davidbuzatto/cprl/ide/gui/icons/firefly-48.png"
            )
        ).getImage() );

        ATMF = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        ATMF.putMapping( "text/cprl", "br.com.davidbuzatto.cprl.ide.gui.CPRLTokenMaker" );

        editorTabs = new HashMap<>();
        activeTab = null;
        openedFilePaths = new HashSet<>();
        skipInitialTabChange = true;
        untitledCounter = 0;
        
        if ( LOAD_TEST_FILES ) {
            try {
                openFile( new File( "cprl-examples/HelloWorld.cprl" ) );
                //openFile( new File( "cprl-examples/Optimizations.cprl" ) );
                //openFile( new File( "cprl-examples/MultiplicationTable.cprl" ) );
            } catch ( IOException exc ) {
                showErrorMessage( exc );
            }
        }

    }

    /**
     * Registers application-wide keyboard shortcuts on the root pane using
     * {@link JComponent#WHEN_IN_FOCUSED_WINDOW} so that the bindings fire
     * regardless of which component currently holds keyboard focus.
     *
     * <ul>
     *   <li><b>Ctrl+S</b> — Save the active file.</li>
     *   <li><b>Ctrl+Shift+S</b> — Save all open files.</li>
     *   <li><b>F6</b> — Compile and run the active file.</li>
     * </ul>
     */
    private void initKeyboardShortcuts() {

        InputMap inputMap = getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        ActionMap actionMap = getRootPane().getActionMap();

        // Ctrl+S - Save
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK ), "save" );
        actionMap.put( "save", new AbstractAction() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                btnSave.doClick();
            }
        } );

        // Ctrl+Shift+S - Save All
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK ), "saveAll" );
        actionMap.put( "saveAll", new AbstractAction() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                btnSaveAll.doClick();
            }
        } );

        // F6 - Compile and Run
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK ), "compileAndRun" );
        actionMap.put( "compileAndRun", new AbstractAction() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                btnCompileAndRun.doClick();
            }
        } );

    }

    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnSaveAs = new javax.swing.JButton();
        btnSaveAll = new javax.swing.JButton();
        sep01 = new javax.swing.JToolBar.Separator();
        btnCompile = new javax.swing.JButton();
        btnCompileAndRun = new javax.swing.JButton();
        sep02 = new javax.swing.JToolBar.Separator();
        btnDisassembly = new javax.swing.JButton();
        tabbedPaneSourceCode = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        menuHelp = new javax.swing.JMenu();
        menuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("CPRL IDE");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
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

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/disk.png"))); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(this::btnSaveActionPerformed);
        toolbar.add(btnSave);

        btnSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/disk_add.png"))); // NOI18N
        btnSaveAs.setFocusable(false);
        btnSaveAs.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveAs.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveAs.addActionListener(this::btnSaveAsActionPerformed);
        toolbar.add(btnSaveAs);

        btnSaveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/disk_multiple.png"))); // NOI18N
        btnSaveAll.setToolTipText("Save All Files");
        btnSaveAll.setFocusable(false);
        btnSaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveAll.addActionListener(this::btnSaveAllActionPerformed);
        toolbar.add(btnSaveAll);
        toolbar.add(sep01);

        btnCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/cog.png"))); // NOI18N
        btnCompile.setToolTipText("Compile");
        btnCompile.setFocusable(false);
        btnCompile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCompile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCompile.addActionListener(this::btnCompileActionPerformed);
        toolbar.add(btnCompile);

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
        btnDisassembly.addActionListener(this::btnDisassemblyActionPerformed);
        toolbar.add(btnDisassembly);

        tabbedPaneSourceCode.addChangeListener(this::tabbedPaneSourceCodeStateChanged);

        menuHelp.setText("Help");

        menuItemAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/davidbuzatto/cprl/ide/gui/icons/help.png"))); // NOI18N
        menuItemAbout.setText("About...");
        menuItemAbout.addActionListener(this::menuItemAboutActionPerformed);
        menuHelp.add(menuItemAbout);

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
        if ( activeTab != null ) {
            executePipeline( activeTab, true, false );
        }
    }//GEN-LAST:event_btnCompileAndRunActionPerformed

    private void tabbedPaneSourceCodeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneSourceCodeStateChanged

        // The very first change event is fired during construction - ignore it.
        if ( skipInitialTabChange ) {
            skipInitialTabChange = false;
            return;
        }

        JComponent c = (JComponent) tabbedPaneSourceCode.getSelectedComponent();
        activeTab = editorTabs.get( c );

    }//GEN-LAST:event_tabbedPaneSourceCodeStateChanged

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        newFile();
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
            saveFile( editorTabs.get( c ) );
        }
    }//GEN-LAST:event_btnSaveAllActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        adjustAllSplitPanes();
    }//GEN-LAST:event_formComponentResized

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        for ( int i = tabbedPaneSourceCode.getTabCount() - 1; i >= 0; i-- ) {

            JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( i );
            EditorTab tab = editorTabs.get( c );

            if ( tab != null && tab.isDirty.get() ) {

                tabbedPaneSourceCode.setSelectedIndex( i );

                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "File \"" + tabTitle( tab ) + "\" has unsaved changes. Save before closing?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if ( choice == JOptionPane.YES_OPTION ) {
                    if ( !saveFile( tab ) ) {
                        return;
                    }
                } else if ( choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION ) {
                    return;
                }

            }

        }

        System.exit( 0 );

    }//GEN-LAST:event_formWindowClosing

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if ( activeTab != null ) {
            saveFile( activeTab );
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAsActionPerformed
        if ( activeTab != null ) {
            saveFileAs( activeTab );
        }
    }//GEN-LAST:event_btnSaveAsActionPerformed

    private void btnDisassemblyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisassemblyActionPerformed
        if ( activeTab != null ) {
            executePipeline( activeTab, false, true );
        }
    }//GEN-LAST:event_btnDisassemblyActionPerformed

    private void btnCompileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompileActionPerformed
        if ( activeTab != null ) {
            executePipeline( activeTab, false, false );
        }
    }//GEN-LAST:event_btnCompileActionPerformed

    private void menuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAboutActionPerformed
        JOptionPane.showMessageDialog(
            this,
            """
            This is the CPRL IDE, a simple tool do develop programs using CPRL.

            Developed by Prof. Dr. David Buzatto""",
            "About...",
            JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_menuItemAboutActionPerformed

    // -------------------------------------------------------------------------
    // File management
    // -------------------------------------------------------------------------

    /**
     * Creates a new untitled editor tab and registers its document listener.
     * The tab is immediately selected and ready for editing.
     */
    private void newFile() {
        String title = "Untitled-" + ( ++untitledCounter );
        EditorTab tab = buildEditorTab( title, null );
        registerDocumentListener( tab );
    }

    /**
     * Opens a CPRL source file in a new editor tab.  If the file is already
     * open, the method returns without creating a duplicate tab.
     *
     * @param file the source file to open
     * 
     * @throws IOException if the file cannot be read
     */
    private void openFile( File file ) throws IOException {

        if ( openedFilePaths.contains( file.getAbsolutePath() ) ) {
            return;
        }

        SourceFileInfo fileInfo = getSourceFileInfo( file );
        EditorTab tab = buildEditorTab( file.getName(), fileInfo );
        loadSourceCode( file, tab.sourceCodeArea );
        SwingUtilities.invokeLater( () -> markClean( tab ) );
        registerDocumentListener( tab );

    }

    /**
     * Saves the editor content of {@code tab} to disk.  If the tab has no
     * associated file yet (untitled), a Save As dialog is shown first.
     *
     * @param tab the editor tab to save
     * 
     * @return {@code true} if the file was saved successfully;
     *         {@code false} if the user cancelled the Save As dialog or if
     *         the write operation failed
     */
    private boolean saveFile( EditorTab tab ) {
        if ( tab.fileInfoRef.get() == null ) {
            return saveFileAs( tab );
        }
        return writeFile( tab );
    }

    /**
     * Always opens a Save As dialog, regardless of whether the tab already has
     * an associated file.  Updates {@link #openedFilePaths} accordingly.
     *
     * @param tab the editor tab to save
     * 
     * @return {@code true} if the user confirmed and the write succeeded;
     *         {@code false} if the user cancelled the dialog
     */
    private boolean saveFileAs( EditorTab tab ) {

        JFileChooser jfc = new JFileChooser( "./" );
        jfc.setDialogTitle( "Save As" );
        jfc.setFileFilter( new FileNameExtensionFilter( "CPRL Source Code", "cprl" ) );

        if ( jfc.showSaveDialog( this ) != JFileChooser.APPROVE_OPTION ) {
            return false;
        }

        File file = jfc.getSelectedFile();
        if ( !file.getName().endsWith( ".cprl" ) ) {
            file = new File( file.getAbsolutePath() + ".cprl" );
        }

        SourceFileInfo oldInfo = tab.fileInfoRef.get();
        if ( oldInfo != null ) {
            openedFilePaths.remove( oldInfo.file.getAbsolutePath() );
        }

        SourceFileInfo newInfo = getSourceFileInfo( file );
        tab.fileInfoRef.set( newInfo );
        openedFilePaths.add( file.getAbsolutePath() );

        writeFile( tab );
        return true;

    }

    /**
     * Writes the current editor content of {@code tab} to the file referenced
     * by its {@link SourceFileInfo} and marks the tab as clean.
     *
     * @param tab the editor tab whose content should be persisted
     * 
     * @return {@code true} if the write succeeded; {@code false} if an
     *         {@link IOException} occurred (an error dialog is also shown)
     */
    private boolean writeFile( EditorTab tab ) {
        try ( FileWriter fw = new FileWriter( tab.fileInfoRef.get().file ) ) {
            fw.write( tab.sourceCodeArea.getText() );
            markClean( tab );
            return true;
        } catch ( IOException exc ) {
            showErrorMessage( exc );
            return false;
        }
    }

    /**
     * Reads a file line-by-line and sets the content of {@code sourceCodeArea}.
     * The caret is repositioned to the beginning of the document via
     * {@link SwingUtilities#invokeLater} after the text is set.
     *
     * @param file           the file to read
     * @param sourceCodeArea the text area that will receive the file content
     * 
     * @throws IOException if the file cannot be read
     */
    private void loadSourceCode( File file, RSyntaxTextArea sourceCodeArea ) throws IOException {

        try ( Scanner s = new Scanner( file ) ) {
            StringBuilder sb = new StringBuilder();
            while ( s.hasNextLine() ) {
                sb.append( s.nextLine() ).append( "\n" );
            }
            sourceCodeArea.setText( sb.toString() );
        }

        SwingUtilities.invokeLater( () -> sourceCodeArea.setCaretPosition( 0 ) );

    }

    // -------------------------------------------------------------------------
    // Compiler pipeline
    // -------------------------------------------------------------------------

    /**
     * Compiles the CPRL source file associated with {@code editorTab}.
     * Compiler error messages are written to {@code System.err}, which is
     * redirected to the internal console by {@link #executePipeline}.
     *
     * @param editorTab the tab whose source file should be compiled
     * 
     * @return true if the file was compiled, false otherwise
     */
    private boolean compile( EditorTab editorTab ) {
        try {
            File sourceFile = new File(
                String.format(
                    "%s/%s.cprl",
                    editorTab.fileInfoRef.get().parentDirPath,
                    editorTab.fileInfoRef.get().fileNameWithoutExt
                )
            );
            Compiler c = new Compiler( sourceFile );
            return c.compile();
        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        return false;
    }

    /**
     * Assembles the {@code .asm} file produced by {@link #compile} and loads
     * the resulting assembly text into the assembly pane of {@code editorTab}.
     *
     * @param editorTab the tab whose assembly file should be assembled
     * 
     * @return true if the file was assembled, false otherwise
     */
    private boolean assemble( EditorTab editorTab ) {
        try {
            File asmFile = new File(
                String.format(
                    "%s/%s.asm",
                    editorTab.fileInfoRef.get().parentDirPath,
                    editorTab.fileInfoRef.get().fileNameWithoutExt
                )
            );
            Assembler a = new Assembler( asmFile );
            return a.assemble();
        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }
        return false;
    }

    /**
     * Unified build pipeline for the active editor tab.  The full sequence is:
     * <ol>
     *   <li>Save the source file (aborts if cancelled or write fails).</li>
     *   <li>Clear the internal console.</li>
     *   <li>Delete stale build artefacts ({@code .asm}, {@code .obj},
     *       {@code .dis}) so a failed build can never silently run old code.</li>
     *   <li>Redirect {@code System.out}/{@code System.err} to the console.</li>
     *   <li>Compile; abort if no {@code .asm} was produced.</li>
     *   <li>Assemble; abort if no {@code .obj} was produced.</li>
     *   <li>Depending on the flags: stop, disassemble, or run the CVM.</li>
     * </ol>
     * CVM execution runs inside a {@link SwingWorker} to keep the EDT
     * responsive while the program waits for user input. The original streams
     * are always restored when the pipeline finishes.
     *
     * @param tab             the editor tab to operate on
     * @param runCvm          {@code true} to execute the CVM after a successful build
     * @param showDisassembly {@code true} to disassemble after a successful build
     */
    private void executePipeline( EditorTab tab, boolean runCvm, boolean showDisassembly ) {

        // 1. Save file - if untitled, opens Save As; aborts pipeline on cancel or write failure.
        if ( !saveFile( tab ) ) {
            return;
        }

        SourceFileInfo fi = tab.fileInfoRef.get();
        if ( fi == null ) {
            return; // Guard: should not happen after a successful save.
        }

        // 2. Clear console and assembly output
        tab.consoleTextPane.setText( "" );
        tab.assemblySourceCode.setText( "" );

        // 3. Delete stale build artefacts so a failed build can never run old code.
        for ( String ext : new String[]{ "asm", "obj", "dis" } ) {
            
            File fileToDelete = new File( String.format( "%s/%s.%s", fi.parentDirPath, fi.fileNameWithoutExt, ext ) );
            
            if ( fileToDelete.exists() ) {
                boolean deleted = fileToDelete.delete();
                if ( DEBUG_ARTEFACTS_DELETION ) {
                    System.out.println( fileToDelete + " was deleted? " + deleted );
                }
            } else {
                if ( DEBUG_ARTEFACTS_DELETION ) {
                    System.out.println( fileToDelete + ": does not exist" );
                }
            }
            
        }

        // 4. Save original streams so they can be restored after the pipeline.
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        InputStream origIn  = System.in;

        // 5. Redirect stdout / stderr to the internal console.
        System.setOut( new PrintStream( new ConsoleOutputStream( tab.consoleTextPane, CONSOLE_STDOUT_COLOR ), true ) );
        System.setErr( new PrintStream( new ConsoleOutputStream( tab.consoleTextPane, CONSOLE_STDERR_COLOR ), true ) );

        // 6. Compile.
        if ( !compile( tab ) ) {
            System.err.println( "Compilation error - stopping pipeline!" );
            System.setOut( origOut );
            System.setErr( origErr );
            return;
        }

        // 7. Assemble - only if the compiler produced an .asm file.
        File asmFile = new File( String.format( "%s/%s.asm", fi.parentDirPath, fi.fileNameWithoutExt ) );
        if ( asmFile.exists() ) {
            if ( !assemble( tab ) ) {
                System.err.println( "Assembly error - stopping pipeline!" );
                System.setOut( origOut );
                System.setErr( origErr );
                return;
            }
            try {
                loadSourceCode( asmFile, tab.assemblySourceCode );
            } catch ( IOException exc ) {
                showErrorMessage( exc );
                System.setOut( origOut );
                System.setErr( origErr );
                return;
            }
        } else {
            appendToConsole(
                tab.consoleTextPane,
                "\n[Build stopped: no assembly file produced - fix the compilation errors above.]\n",
                CONSOLE_STDERR_COLOR
            );
            System.setOut( origOut );
            System.setErr( origErr );
            return;
        }

        // ---- Compile-only path: restore streams and return. ----
        if ( !runCvm && !showDisassembly ) {
            System.setOut( origOut );
            System.setErr( origErr );
            return;
        }

        // ---- Disassembly path: run synchronously, then restore streams and return. ----
        if ( showDisassembly ) {
            File objFile = new File( String.format( "%s/%s.obj", fi.parentDirPath, fi.fileNameWithoutExt ) );
            if ( objFile.exists() ) {
                disassemble( tab );
            } else {
                appendToConsole(
                    tab.consoleTextPane,
                    "\n[Disassembly stopped: no object file produced - fix the build errors above.]\n",
                    CONSOLE_STDERR_COLOR
                );
            }
            System.setOut( origOut );
            System.setErr( origErr );
            return;
        }

        // ---- Run-CVM path: verify .obj exists, then run in background. ----
        File objFile = new File( String.format( "%s/%s.obj", fi.parentDirPath, fi.fileNameWithoutExt ) );
        if ( !objFile.exists() ) {
            appendToConsole(
                tab.consoleTextPane,
                "\n[Execution stopped: no object file produced - fix the build errors above.]\n",
                CONSOLE_STDERR_COLOR
            );
            System.setOut( origOut );
            System.setErr( origErr );
            return;
        }

        // Set up stdin pipe for interactive CVM input.
        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedInputStream  pipedIn;
        try {
            pipedIn = new PipedInputStream( pipedOut );
        } catch ( IOException e ) {
            System.setOut( origOut );
            System.setErr( origErr );
            showErrorMessage( e );
            return;
        }
        tab.activePipedOut.set( pipedOut );
        System.setIn( pipedIn );

        tab.consoleInputField.setEnabled( true );
        tab.consoleEnterButton.setEnabled( true );
        tab.consoleInputField.requestFocusInWindow();
        setRunningState( true );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                // Use try-with-resources so the .obj FileInputStream is closed
                // immediately after loadProgram(), releasing the file handle on Windows.
                try ( FileInputStream o = new FileInputStream( objFile ) ) {
                    Instruction.resetMaps();
                    CVM vm = new CVM( 8192 );
                    vm.loadProgram( o );
                    vm.run();
                }
                return null;
            }

            @Override
            protected void done() {

                // Restore original streams.
                System.setOut( origOut );
                System.setErr( origErr );
                System.setIn( origIn );

                // Close the stdin pipe.
                tab.activePipedOut.set( null );
                try {
                    pipedOut.close();
                } catch ( IOException e ) {
                    // Nothing meaningful to do if closing the pipe fails.
                }

                // Re-enable toolbar and disable input controls.
                tab.consoleInputField.setEnabled( false );
                tab.consoleEnterButton.setEnabled( false );
                setRunningState( false );

                // Surface any runtime exception in the console.
                try {
                    get();
                } catch ( Exception e ) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    appendToConsole(
                        tab.consoleTextPane,
                        "\n[Runtime error: " + cause.getMessage() + "]\n",
                        CONSOLE_STDERR_COLOR
                    );
                }

            }

        };

        worker.execute();

    }

    /**
     * Disassembles the {@code .obj} file for {@code editorTab} into a
     * {@code .dis} file and opens the result in a new {@link DisassemblyWindow}.
     * The caller is responsible for verifying that the {@code .obj} file exists
     * before calling this method.
     *
     * @param editorTab the tab whose object file should be disassembled
     */
    private void disassemble( EditorTab editorTab ) {

        try {

            File objFile = new File(
                String.format(
                    "%s/%s.obj",
                    editorTab.fileInfoRef.get().parentDirPath,
                    editorTab.fileInfoRef.get().fileNameWithoutExt
                )
            );

            File disFile = new File(
                String.format(
                    "%s/%s.dis",
                    editorTab.fileInfoRef.get().parentDirPath,
                    editorTab.fileInfoRef.get().fileNameWithoutExt
                )
            );

            Disassembler.disassemble( objFile, disFile );
            DisassemblyWindow dw = new DisassemblyWindow(
                String.format( "Disassembled code from %s to %s", objFile.getName(), disFile.getName() )
            );
            loadSourceCode( disFile, dw.getAssemblySourceCode() );

            SwingUtilities.invokeLater( () -> dw.setVisible( true ) );
            SwingUtilities.invokeLater( () -> dw.getAssemblySourceCode().setCaretPosition( 0 ) );

        } catch ( IOException exc ) {
            showErrorMessage( exc );
        }

    }

    // -------------------------------------------------------------------------
    // Tab management
    // -------------------------------------------------------------------------

    /**
     * Creates and wires all UI components that make up a single editor tab:
     * the CPRL source editor, the console output pane with its input row,
     * the read-only assembly pane, and the split panes that arrange them.
     * The new tab is added to the tabbed pane, selected, and registered in
     * {@link #editorTabs}.
     *
     * @param title    text displayed in the tab header
     * @param fileInfo associated source-file metadata; {@code null} for
     *                 untitled files
     * 
     * @return the newly created {@link EditorTab}
     */
    private EditorTab buildEditorTab( String title, SourceFileInfo fileInfo ) {

        // --- Source code area ---
        RSyntaxTextArea sourceCodeArea = new RSyntaxTextArea( 1, 1 );
        sourceCodeArea.setCodeFoldingEnabled( false );
        sourceCodeArea.setBackground( new Color( 0x3F3F3F, false ) );
        sourceCodeArea.setCurrentLineHighlightColor( Color.BLACK );
        sourceCodeArea.setSelectionColor( Color.BLACK );
        sourceCodeArea.setFont( DEFAULT_FONT );
        sourceCodeArea.setAntiAliasingEnabled( true );
        sourceCodeArea.setAutoIndentEnabled( false );
        sourceCodeArea.setMatchedBracketBGColor( Color.PINK.darker() );
        sourceCodeArea.setTabsEmulated( true );
        sourceCodeArea.setTabSize( 4 );
        sourceCodeArea.setSyntaxEditingStyle( "text/cprl" );
        applyColorScheme( sourceCodeArea );

        RTextScrollPane sp = new RTextScrollPane( sourceCodeArea );

        // --- Console: output pane ---
        JTextPane consoleTextPane = new JTextPane();
        consoleTextPane.setFont( DEFAULT_FONT );
        consoleTextPane.setBackground( CONSOLE_BG_COLOR );
        consoleTextPane.setForeground( CONSOLE_STDOUT_COLOR );
        consoleTextPane.setEditable( false );
        JScrollPane consoleScroll = new JScrollPane( consoleTextPane );

        // --- Console: input row ---
        JLabel inputLabel = new JLabel( "  Input: " );
        JTextField consoleInputField = new JTextField();
        consoleInputField.setFont( DEFAULT_FONT );
        consoleInputField.setEnabled( false );
        JButton consoleEnterButton = new JButton( "Send" );
        consoleEnterButton.setEnabled( false );

        JPanel inputPanel = new JPanel( new BorderLayout( 4, 0 ) );
        inputPanel.add( inputLabel, BorderLayout.WEST );
        inputPanel.add( consoleInputField, BorderLayout.CENTER );
        inputPanel.add( consoleEnterButton, BorderLayout.EAST );
        inputPanel.setBorder( BorderFactory.createEmptyBorder( 4, 0, 4, 4 ) );

        // --- Console: full panel ---
        JPanel consolePanel = new JPanel( new BorderLayout() );
        consolePanel.add( consoleScroll, BorderLayout.CENTER );
        consolePanel.add( inputPanel, BorderLayout.SOUTH );

        // Pipe reference (null when not running)
        AtomicReference<PipedOutputStream> activePipedOut = new AtomicReference<>( null );

        // Runnable that reads the input field and forwards the text to the CVM via the stdin pipe.
        Runnable sendInput = () -> {
            PipedOutputStream pos = activePipedOut.get();
            if ( pos != null ) {
                String text = consoleInputField.getText();
                consoleInputField.setText( "" );
                // Echo the submitted text in a distinct colour so the user can
                // distinguish their input from the program's output.
                appendToConsole( consoleTextPane, text + "\n", CONSOLE_ECHO_COLOR );
                try {
                    pos.write( ( text + "\n" ).getBytes() );
                    pos.flush();
                } catch ( IOException ex ) {
                    // Pipe closed - the CVM already finished; nothing to do.
                }
            }
        };
        consoleInputField.addActionListener( e -> sendInput.run() );
        consoleEnterButton.addActionListener( e -> sendInput.run() );

        // --- Assembly pane ---
        RSyntaxTextArea assemblySourceCode = new RSyntaxTextArea();
        assemblySourceCode.setCodeFoldingEnabled( false );
        assemblySourceCode.setBackground( new Color( 0x3F3F3F, false ) );
        assemblySourceCode.setCurrentLineHighlightColor( Color.BLACK );
        assemblySourceCode.setSelectionColor( Color.BLACK );
        assemblySourceCode.setFont( DEFAULT_FONT );
        assemblySourceCode.setAntiAliasingEnabled( true );
        assemblySourceCode.setAutoIndentEnabled( false );
        assemblySourceCode.setMatchedBracketBGColor( Color.PINK.darker() );
        assemblySourceCode.setTabsEmulated( true );
        assemblySourceCode.setTabSize( 4 );
        assemblySourceCode.setSyntaxEditingStyle( "text/cprl" );
        assemblySourceCode.setEditable( false );
        applyColorScheme( assemblySourceCode );

        RTextScrollPane assemblyScroll = new RTextScrollPane( assemblySourceCode );

        // --- Split panes ---
        JSplitPane verticalSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        verticalSplit.setTopComponent( sp );
        verticalSplit.setBottomComponent( consolePanel );

        JSplitPane horizontalSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        horizontalSplit.setLeftComponent( verticalSplit );
        horizontalSplit.setRightComponent( assemblyScroll );

        JPanel container = new JPanel( new BorderLayout() );
        container.add( horizontalSplit, BorderLayout.CENTER );

        JLabel titleLabel = addClosableTab( title, container );
        tabbedPaneSourceCode.setSelectedComponent( container );

        EditorTab tab = new EditorTab(
            sourceCodeArea,
            consoleTextPane,
            assemblySourceCode,
            horizontalSplit,
            verticalSplit,
            new AtomicReference<>( fileInfo ),
            new AtomicBoolean( false ),
            titleLabel,
            consoleInputField,
            consoleEnterButton,
            activePipedOut
        );

        editorTabs.put( container, tab );
        activeTab = tab;

        if ( fileInfo != null ) {
            openedFilePaths.add( fileInfo.file.getAbsolutePath() );
        }

        adjustSplitPanes( tab );
        return tab;

    }

    /**
     * Adds a tab with a custom header that contains a title label and a close
     * button.  Clicking the close button triggers {@link #closeTab(int)}.
     *
     * @param title   text shown in the tab header
     * @param content the component placed in the tab body
     * 
     * @return the {@link JLabel} used as the tab title, kept so dirty-state
     *         changes can update it later
     */
    private JLabel addClosableTab( String title, Component content ) {

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
        } );

        tabPanel.add( titleLabel );
        tabPanel.add( closeButton );
        tabbedPaneSourceCode.setTabComponentAt( index, tabPanel );

        return titleLabel;

    }

    /**
     * Closes the tab at the given index.  If the tab has unsaved changes a
     * confirmation dialog is shown, giving the user the option to save, discard,
     * or cancel the close operation.
     *
     * @param index zero-based index of the tab to close
     */
    private void closeTab( int index ) {

        JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( index );
        EditorTab tab = editorTabs.get( c );

        if ( tab.isDirty.get() ) {
            tabbedPaneSourceCode.setSelectedIndex( index );
            int choice = JOptionPane.showConfirmDialog(
                this,
                "File \"" + tabTitle( tab ) + "\" has unsaved changes. Save before closing?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if ( choice == JOptionPane.YES_OPTION ) {
                if ( !saveFile( tab ) ) {
                    return;
                }
            } else if ( choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION ) {
                return;
            }
        }

        editorTabs.remove( c );
        SourceFileInfo fi = tab.fileInfoRef.get();
        if ( fi != null ) {
            openedFilePaths.remove( fi.file.getAbsolutePath() );
        }
        tabbedPaneSourceCode.remove( index );

    }

    // -------------------------------------------------------------------------
    // Dirty state tracking
    // -------------------------------------------------------------------------

    /**
     * Attaches a {@link DocumentListener} to the source editor of {@code tab}
     * so that any text insertion or removal automatically marks the tab as dirty.
     *
     * @param tab the editor tab whose source area should be monitored
     */
    private void registerDocumentListener( EditorTab tab ) {
        tab.sourceCodeArea.getDocument().addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate( DocumentEvent e ) {
                markDirty( tab );
            }
            @Override
            public void removeUpdate( DocumentEvent e ) {
                markDirty( tab );
            }
            @Override
            public void changedUpdate( DocumentEvent e ) {
                // Attribute changes (e.g. syntax highlighting) do not affect dirty state.
            }
        } );
    }

    /**
     * Marks {@code tab} as having unsaved changes and prepends {@code "* "} to
     * its title label.  Has no effect if the tab is already dirty.
     *
     * @param tab the editor tab to mark as dirty
     */
    private void markDirty( EditorTab tab ) {
        if ( !tab.isDirty.get() ) {
            tab.isDirty.set( true );
            tab.titleLabel.setText( "* " + tabTitle( tab ) );
        }
    }

    /**
     * Clears the dirty flag of {@code tab} and restores its plain title label.
     *
     * @param tab the editor tab to mark as clean
     */
    private void markClean( EditorTab tab ) {
        tab.isDirty.set( false );
        tab.titleLabel.setText( tabTitle( tab ) );
    }

    /**
     * Returns the clean (non-dirty) title of {@code tab} by stripping the
     * leading {@code "* "} prefix if present.
     *
     * @param tab the editor tab whose title is needed
     * 
     * @return the title string without any dirty-state prefix
     */
    private String tabTitle( EditorTab tab ) {
        String current = tab.titleLabel.getText();
        return current.startsWith( "* " ) ? current.substring( 2 ) : current;
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    /**
     * Resets the divider positions of both split panes in {@code tab} to
     * 70 % / 30 %.  The update is posted via {@link SwingUtilities#invokeLater}
     * so it takes effect after the component has been laid out.
     *
     * @param tab the editor tab whose split panes should be adjusted
     */
    private void adjustSplitPanes( EditorTab tab ) {
        SwingUtilities.invokeLater( () -> {
            tab.horizontalSplit.setDividerLocation( 0.7 );
            tab.verticalSplit.setDividerLocation( 0.7 );
        } );
    }

    /**
     * Calls {@link #adjustSplitPanes(EditorTab)} for every open editor tab.
     * Typically invoked when the main window is resized.
     */
    private void adjustAllSplitPanes() {
        for ( int i = 0; i < tabbedPaneSourceCode.getTabCount(); i++ ) {
            JComponent c = (JComponent) tabbedPaneSourceCode.getComponentAt( i );
            adjustSplitPanes( editorTabs.get( c ) );
        }
    }

    /**
     * Enables or disables the compile, compile-and-run and disassembly toolbar
     * buttons.  Called with {@code true} when the CVM starts and {@code false}
     * when it finishes, preventing the user from launching multiple pipelines
     * simultaneously.
     *
     * @param running {@code true} to disable the buttons; {@code false} to
     *                re-enable them
     */
    private void setRunningState( boolean running ) {
        btnCompile.setEnabled( !running );
        btnCompileAndRun.setEnabled( !running );
        btnDisassembly.setEnabled( !running );
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link SourceFileInfo} from a {@link File} handle by extracting
     * the parent directory path and the file name without its extension.
     *
     * @param file the source file
     * 
     * @return a populated {@link SourceFileInfo} for the given file
     */
    private SourceFileInfo getSourceFileInfo( File file ) {
        String parentDirPath = file.getParentFile().getPath();
        String fileNameWithoutExt = file.getName();
        fileNameWithoutExt = fileNameWithoutExt.substring( 0, fileNameWithoutExt.lastIndexOf( "." ) );
        return new SourceFileInfo( file, parentDirPath, fileNameWithoutExt );
    }

    /**
     * Applies the IDE colour scheme to an {@link RSyntaxTextArea}.  Sets fonts
     * and foreground colours for every relevant token type used by the CPRL
     * language definition.
     *
     * @param sourceCodeArea the text area to style
     */
    public static void applyColorScheme( RSyntaxTextArea sourceCodeArea ) {

        SyntaxScheme scheme = sourceCodeArea.getSyntaxScheme();
        Font plain = DEFAULT_FONT;

        scheme.getStyle( Token.RESERVED_WORD ).font = plain;
        scheme.getStyle( Token.COMMENT_EOL ).font = plain;
        scheme.getStyle( Token.IDENTIFIER ).font = plain;
        scheme.getStyle( Token.DATA_TYPE ).font = plain;
        scheme.getStyle( Token.OPERATOR ).font = plain;

        scheme.getStyle( Token.COMMENT_EOL ).foreground                 = new Color( 0x808080, false );
        scheme.getStyle( Token.IDENTIFIER ).foreground                  = new Color( 0xFFFFFF, false );
        scheme.getStyle( Token.LITERAL_BOOLEAN ).foreground             = new Color( 0x79B8FF, false );
        scheme.getStyle( Token.LITERAL_CHAR ).foreground                = new Color( 0x9ECBFF, false );
        scheme.getStyle( Token.LITERAL_NUMBER_DECIMAL_INT ).foreground  = new Color( 0x79B8FF, false );
        scheme.getStyle( Token.LITERAL_STRING_DOUBLE_QUOTE ).foreground = new Color( 0x9ECBFF, false );
        scheme.getStyle( Token.OPERATOR ).foreground                    = new Color( 0xFF8040, false );
        scheme.getStyle( Token.RESERVED_WORD ).foreground               = new Color( 0xF97583, false );
        scheme.getStyle( Token.DATA_TYPE ).foreground                   = new Color( 0xB392F0, false );
        scheme.getStyle( Token.SEPARATOR ).foreground                   = new Color( 0xFFFFFF, false );

        sourceCodeArea.revalidate();

    }

    /**
     * Appends {@code text} to {@code textPane} using {@code color} as the
     * foreground colour, then scrolls the pane to the end.  Must be called on
     * the Event Dispatch Thread (the {@link ConsoleOutputStream} already
     * guarantees this via {@link SwingUtilities#invokeLater}).
     *
     * @param textPane the console pane to append to
     * @param text     the text to append
     * @param color    the foreground colour for the appended text
     */
    private static void appendToConsole( JTextPane textPane, String text, Color color ) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle( "output", null );
        StyleConstants.setForeground( style, color );
        try {
            doc.insertString( doc.getLength(), text, style );
            textPane.setCaretPosition( doc.getLength() );
        } catch ( BadLocationException e ) {
            // BadLocationException should never occur when appending to the end.
        }
    }

    /**
     * Shows a modal error dialog with the message from {@code exc}.
     *
     * @param exc the exception whose message should be displayed
     */
    private void showErrorMessage( Exception exc ) {
        JOptionPane.showMessageDialog(
            null,
            exc.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Application entry point. Installs the FlatDarkLaf look-and-feel and
     * opens the main window on the Event Dispatch Thread.
     *
     * @param args command-line arguments (not used)
     */
    public static void main( String args[] ) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater( () -> new MainWindow().setVisible( true ) );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCompile;
    private javax.swing.JButton btnCompileAndRun;
    private javax.swing.JButton btnDisassembly;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveAll;
    private javax.swing.JButton btnSaveAs;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuItemAbout;
    private javax.swing.JToolBar.Separator sep01;
    private javax.swing.JToolBar.Separator sep02;
    private javax.swing.JTabbedPane tabbedPaneSourceCode;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
