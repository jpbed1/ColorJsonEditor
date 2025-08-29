import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON Color Palette Editor with:
 * - Main parameter list (from "Name":"#RRGGBB" / "#RRGGBBAA")
 * - Detail view (preview, hex field, color chooser)
 * - Favorites panel (user palette) with drag & drop onto parameters
 * - User Palette menu: Save… / Load… (JSON array of hex strings)
 * - Auto-load favorites on startup; auto-save favorites on exit
 *
 * Build (JDK 9+ recommended):
 *   javac --release 8 -encoding UTF-8 ColorJsonEditor.java
 *   jar cfe ColorJsonEditor.jar ColorJsonEditor ColorJsonEditor.class
 * Run:
 *   java -jar ColorJsonEditor.jar [path-to-json]
 */
public class ColorJsonEditor extends JFrame {

    // Regex: "name": "#RRGGBB" or "#RRGGBBAA"
    private static final Pattern COLOR_ENTRY =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"(#[0-9a-fA-F]{6}(?:[0-9a-fA-F]{2})?)\"");

    private JTextField searchField;
    private JList<PaletteEntry> list;
    private DefaultListModel<PaletteEntry> listModel;
    private JButton editBtn, saveBtn, saveAsBtn, openBtn, revertBtn;
    private JTextField hexField;
    private JPanel previewPanel;

    // Favorites (User Palette)
    private DefaultListModel<Favorite> favModel;
    private JList<Favorite> favList;
    private JButton addFavBtn, removeFavBtn;

    private Path currentFile;
    private String originalJson;
    private String workingJson;
    private List<PaletteEntry> entries;

    // ===== Startup / Main =====
    public static void main(String[] args) {
        // Show exceptions even when launched via javaw/double-click
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(ex).append("\n");
            for (StackTraceElement ste : ex.getStackTrace()) sb.append("  at ").append(ste).append("\n");
            JOptionPane.showMessageDialog(null, sb.toString(), "Uncaught Error", JOptionPane.ERROR_MESSAGE);
        });

        SwingUtilities.invokeLater(() -> {
            ColorJsonEditor app = new ColorJsonEditor();
            app.setVisible(true);
            if (args.length == 1) app.openFile(Paths.get(args[0]));
        });
    }

    public ColorJsonEditor() {
        super("JSON Color Palette Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 740);
        setLocationRelativeTo(null);
        buildUI();

        // Auto-load favorites on startup (ignore if file missing or invalid)
        tryAutoLoadDefaultUserPalette();

        // Auto-save favorites on exit
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { tryAutoSaveDefaultUserPalette(); }
        });
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ===== Menu bar =====
        JMenuBar mb = new JMenuBar();

        JMenu mFile = new JMenu("File");
        JMenuItem miOpen = new JMenuItem("Open…");
        JMenuItem miSave = new JMenuItem("Save");
        JMenuItem miSaveAs = new JMenuItem("Save As…");
        JMenuItem miRevert = new JMenuItem("Revert");
        mFile.add(miOpen);
        mFile.add(miSave);
        mFile.add(miSaveAs);
        mFile.addSeparator();
        mFile.add(miRevert);
        mb.add(mFile);

        JMenu mUser = new JMenu("User Palette");
        JMenuItem miUserSave = new JMenuItem("Save…");
        JMenuItem miUserLoad = new JMenuItem("Load…");
        mUser.add(miUserSave);
        mUser.add(miUserLoad);
        mb.add(mUser);

        setJMenuBar(mb);

        // ===== Toolbar =====
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        openBtn = new JButton("Open…");
        saveBtn = new JButton("Save");
        saveAsBtn = new JButton("Save As…");
        revertBtn = new JButton("Revert");
        tb.add(openBtn);
        tb.add(saveBtn);
        tb.add(saveAsBtn);
        tb.add(revertBtn);
        tb.add(Box.createHorizontalStrut(12));
        tb.add(new JLabel("Search: "));
        searchField = new JTextField(28);
        tb.add(searchField);
        add(tb, BorderLayout.NORTH);

        // Hook menu+toolbar actions
        miOpen.addActionListener(this::onOpen);
        miSave.addActionListener(this::onSave);
        miSaveAs.addActionListener(this::onSaveAs);
        miRevert.addActionListener(e -> { if (originalJson != null) { workingJson = originalJson; reparse(); } });

        openBtn.addActionListener(this::onOpen);
        saveBtn.addActionListener(this::onSave);
        saveAsBtn.addActionListener(this::onSaveAs);
        revertBtn.addActionListener(e -> { if (originalJson != null) { workingJson = originalJson; reparse(); } });

        miUserSave.addActionListener(e -> onSaveUserPalette());
        miUserLoad.addActionListener(e -> onLoadUserPalette());

        // ===== Left: parameters list =====
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setCellRenderer(new PaletteCellRenderer());
        list.setDropMode(DropMode.ON);
        list.setTransferHandler(new HexImportTransferHandler()); // accept drops
        JScrollPane listScroll = new JScrollPane(list);

        // ===== Right: detail panel =====
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel detail = new JPanel();
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));

        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(140, 90));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));

        JPanel hexRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hexRow.add(new JLabel("Hex:"));
        hexField = new JTextField(12);
        hexRow.add(hexField);
        JButton applyHexBtn = new JButton("Apply Hex");
        hexRow.add(applyHexBtn);

        JButton pickBtn = new JButton("Edit Color…");
        editBtn = pickBtn;

        detail.add(previewPanel);
        detail.add(Box.createVerticalStrut(8));
        detail.add(hexRow);
        detail.add(Box.createVerticalStrut(8));
        detail.add(pickBtn);
        right.add(detail, BorderLayout.NORTH);

        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showSelected(); });

        pickBtn.addActionListener(e -> {
            PaletteEntry sel = list.getSelectedValue();
            if (sel == null) return;
            Color chosen = JColorChooser.showDialog(this, "Pick Color: " + sel.name, sel.getAwtColorRGB());
            if (chosen != null) {
                String newHex = toHexPreservingAlphaFromEntryTarget(chosen, sel.hex);
                updateEntryHex(sel, newHex);
            }
        });

        applyHexBtn.addActionListener(e -> {
            PaletteEntry sel = list.getSelectedValue();
            if (sel == null) return;
            String text = hexField.getText().trim();
            String normalized = normalizeHex(text);
            if (normalized == null) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid hex like #RRGGBB or #RRGGBBAA.",
                        "Invalid Hex", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Treat typed color like a favorite dropped on the target (respect target alpha rules)
            String merged = mergeFavoriteOntoTarget(normalized, sel.hex);
            updateEntryHex(sel, merged);
        });

        // ===== Far Right: Favorites (user palette) =====
        JPanel favPanel = new JPanel(new BorderLayout(8, 8));
        favPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel favTitle = new JLabel("Favorites (drag onto a parameter):");
        favPanel.add(favTitle, BorderLayout.NORTH);

        favModel = new DefaultListModel<>();
        favList = new JList<>(favModel);
        favList.setCellRenderer(new FavoriteCellRenderer());
        favList.setVisibleRowCount(12);
        favList.setDragEnabled(true); // enable drag from favorites
        favList.setTransferHandler(new HexExportTransferHandler());
        JScrollPane favScroll = new JScrollPane(favList);
        favPanel.add(favScroll, BorderLayout.CENTER);

        JPanel favButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addFavBtn = new JButton("Add Selected");
        removeFavBtn = new JButton("Remove");
        favButtons.add(addFavBtn);
        favButtons.add(removeFavBtn);
        favPanel.add(favButtons, BorderLayout.SOUTH);

        addFavBtn.addActionListener(e -> {
            PaletteEntry sel = list.getSelectedValue();
            if (sel != null) {
                favModel.addElement(new Favorite(sel.hex));
            } else {
                String t = normalizeHex(hexField.getText().trim());
                if (t != null) favModel.addElement(new Favorite(t));
            }
        });

        removeFavBtn.addActionListener(e -> {
            int idx = favList.getSelectedIndex();
            if (idx >= 0) favModel.remove(idx);
        });

        // ===== Split panes =====
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        centerSplit.setDividerLocation(480);
        JSplitPane outerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerSplit, favPanel);
        outerSplit.setDividerLocation(900);

        add(outerSplit, BorderLayout.CENTER);

        // ===== Search filter =====
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void refilter() {
                String q = searchField.getText().trim().toLowerCase();
                listModel.clear();
                if (entries != null) {
                    for (PaletteEntry p : entries) {
                        if (q.isEmpty()
                                || p.name.toLowerCase().contains(q)
                                || p.hex.toLowerCase().contains(q)) {
                            listModel.addElement(p);
                        }
                    }
                }
            }
            public void insertUpdate(DocumentEvent e) { refilter(); }
            public void removeUpdate(DocumentEvent e) { refilter(); }
            public void changedUpdate(DocumentEvent e) { refilter(); }
        });

        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean hasDoc) {
        saveBtn.setEnabled(hasDoc);
        saveAsBtn.setEnabled(hasDoc);
        revertBtn.setEnabled(hasDoc);
        editBtn.setEnabled(hasDoc);
        addFavBtn.setEnabled(hasDoc);
    }

    // ===== File operations =====

    private void onOpen(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open Palette JSON");
        int ok = fc.showOpenDialog(this);
        if (ok == JFileChooser.APPROVE_OPTION) {
            openFile(fc.getSelectedFile().toPath());
        }
    }

    private void openFile(Path p) {
        try {
            String txt = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            currentFile = p;
            originalJson = txt;
            workingJson = txt;
            reparse();
            setTitle("JSON Color Palette Editor — " + p.getFileName());
            setButtonsEnabled(true);
        } catch (IOException ex) {
            showError("Failed to open file:\n" + ex.getMessage());
        }
    }

    private void onSave(ActionEvent e) {
        if (currentFile == null) {
            onSaveAs(e);
            return;
        }
        try {
            Files.write(currentFile, workingJson.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING);
            originalJson = workingJson;
            JOptionPane.showMessageDialog(this, "Saved:\n" + currentFile.toAbsolutePath());
        } catch (IOException ex) {
            showError("Failed to save:\n" + ex.getMessage());
        }
    }

    private void onSaveAs(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save As…");
        if (currentFile != null) fc.setSelectedFile(currentFile.getFileName().toFile());
        int ok = fc.showSaveDialog(this);
        if (ok == JFileChooser.APPROVE_OPTION) {
            Path out = fc.getSelectedFile().toPath();
            try {
                Files.write(out, workingJson.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                currentFile = out;
                originalJson = workingJson;
                setTitle("JSON Color Palette Editor — " + out.getFileName());
                JOptionPane.showMessageDialog(this, "Saved:\n" + out.toAbsolutePath());
            } catch (IOException ex) {
                showError("Failed to save:\n" + ex.getMessage());
            }
        }
    }

    // ===== Parsing & list refresh =====

    private void reparse() {
        entries = new ArrayList<PaletteEntry>();
        Matcher m = COLOR_ENTRY.matcher(workingJson);
        while (m.find()) {
            String name = m.group(1);
            String hex = m.group(2);
            int start = m.start(2);
            int end = m.end(2);
            entries.add(new PaletteEntry(name, hex, start, end));
        }
        refreshList();
    }

    private void refreshList() {
        listModel.clear();
        if (entries != null) {
            for (PaletteEntry e : entries) listModel.addElement(e);
        }
        if (!listModel.isEmpty()) list.setSelectedIndex(0);
        else {
            previewPanel.setBackground(UIManager.getColor("Panel.background"));
            hexField.setText("");
        }
    }

    private void showSelected() {
        PaletteEntry sel = list.getSelectedValue();
        if (sel == null) {
            previewPanel.setBackground(UIManager.getColor("Panel.background"));
            hexField.setText("");
            return;
        }
        previewPanel.setBackground(sel.getAwtColorRGB());
        hexField.setText(sel.hex);
    }

    private void updateEntryHex(PaletteEntry entry, String newHex) {
        if (newHex == null || newHex.equalsIgnoreCase(entry.hex)) return;

        StringBuilder sb = new StringBuilder(workingJson);
        sb.replace(entry.start, entry.end, newHex);
        workingJson = sb.toString();

        int delta = newHex.length() - (entry.end - entry.start);
        entry.hex = newHex;
        entry.refreshColor();

        if (delta != 0) {
            boolean past = false;
            for (PaletteEntry pe : entries) {
                if (pe == entry) { past = true; continue; }
                if (past) {
                    pe.start += delta;
                    pe.end += delta;
                }
            }
        }

        list.repaint();
        showSelected();
    }

    // ===== User Palette (Favorites) Save/Load (manual) =====

    private void onSaveUserPalette() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save User Palette (JSON)");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path out = fc.getSelectedFile().toPath();
            try {
                saveUserPaletteTo(out);
                JOptionPane.showMessageDialog(this, "Saved user palette:\n" + out.toAbsolutePath());
            } catch (IOException ex) {
                showError("Failed to save user palette:\n" + ex.getMessage());
            }
        }
    }

    private void onLoadUserPalette() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load User Palette (JSON)");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path in = fc.getSelectedFile().toPath();
            try {
                loadUserPaletteFrom(in, true);
            } catch (Exception ex) {
                showError("Failed to load user palette:\n" + ex.getMessage());
            }
        }
    }

    // ===== Auto-load/save helpers =====

    private void tryAutoLoadDefaultUserPalette() {
        try {
            Path p = getDefaultUserPalettePath();
            if (Files.exists(p)) {
                loadUserPaletteFrom(p, true);
            }
        } catch (Exception ignored) {
            // Quiet on startup
        }
    }

    private void tryAutoSaveDefaultUserPalette() {
        try {
            if (favModel != null && !favModel.isEmpty()) {
                Path p = getDefaultUserPalettePath();
                ensureParentDir(p);
                saveUserPaletteTo(p);
            }
        } catch (Exception ignored) {
            // Don't block exit
        }
    }

    private Path getDefaultUserPalettePath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            if (appdata != null && !appdata.isEmpty()) {
                return Paths.get(appdata, "ColorJsonEditor", "user-palette.json");
            }
        }
        String home = System.getProperty("user.home", ".");
        return Paths.get(home, ".colorjsoneditor", "user-palette.json");
    }

    private void ensureParentDir(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private void saveUserPaletteTo(Path out) throws IOException {
        ensureParentDir(out);
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < favModel.size(); i++) {
            String hex = favModel.get(i).hex;
            sb.append("  \"").append(hex).append("\"");
            if (i < favModel.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void loadUserPaletteFrom(Path in, boolean replace) throws IOException {
        String txt = new String(Files.readAllBytes(in), StandardCharsets.UTF_8).trim();
        if (!txt.startsWith("[") || !txt.endsWith("]")) {
            throw new IllegalArgumentException("Expected JSON array of hex strings.");
        }
        String body = txt.substring(1, txt.length() - 1); // inside brackets
        String[] parts = body.split(",");
        List<String> hexes = new ArrayList<String>();
        for (String p : parts) {
            String t = p.trim();
            if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
                t = t.substring(1, t.length() - 1);
            }
            String norm = normalizeHex(t);
            if (norm != null) hexes.add(norm);
        }
        if (replace) favModel.clear();
        for (String h : hexes) favModel.addElement(new Favorite(h));
    }

    // ===== Helpers =====

    /** Normalize "#RRGGBB" or "#RRGGBBAA"; returns null if invalid. */
    private static String normalizeHex(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        t = t.startsWith("#") ? t.substring(1) : t;
        t = t.toUpperCase();
        if (!(t.length() == 6 || t.length() == 8)) return null;
        if (!t.matches("[0-9A-F]{6}([0-9A-F]{2})?")) return null;
        return "#" + t;
    }

    /** Keep target alpha if target was 8-digit. */
    private static String toHexPreservingAlphaFromEntryTarget(Color c, String targetHex) {
        String rgb = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        if (targetHex != null && targetHex.length() == 9) {
            String aa = targetHex.substring(7, 9);
            return rgb + aa;
        }
        return rgb;
    }

    /** Merge favorite hex onto target hex using alpha rules. */
    private static String mergeFavoriteOntoTarget(String favoriteHex, String targetHex) {
        if (favoriteHex == null || targetHex == null) return favoriteHex;
        boolean favHasAlpha = favoriteHex.length() == 9;
        boolean tgtHasAlpha = targetHex.length() == 9;
        if (favHasAlpha) {
            if (tgtHasAlpha) return favoriteHex;            // RGBA -> RGBA
            return favoriteHex.substring(0, 7);             // RGBA -> RGB (drop AA)
        } else {
            if (tgtHasAlpha) return favoriteHex + targetHex.substring(7, 9); // RGB + keep target AA
            return favoriteHex;                              // RGB -> RGB
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===== Data models & renderers =====

    /** Represents one "name":"#hex" occurrence and its span in the JSON text. */
    private static class PaletteEntry {
        final String name;
        String hex;
        int start, end;
        private transient Color awtColorRGB;

        PaletteEntry(String name, String hex, int start, int end) {
            this.name = name;
            this.hex = hex;
            this.start = start;
            this.end = end;
            refreshColor();
        }

        void refreshColor() {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            awtColorRGB = new Color(r, g, b);
        }

        Color getAwtColorRGB() { return awtColorRGB; }

        public String toString() { return name + "  " + hex; }
    }

    /** Renderer for parameters list (swatch + name + hex). */
    private static class PaletteCellRenderer extends JPanel implements ListCellRenderer<PaletteEntry> {
        private final JPanel swatch = new JPanel();
        private final JLabel name = new JLabel();
        private final JLabel hex = new JLabel();

        PaletteCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(6, 8, 6, 8));
            swatch.setPreferredSize(new Dimension(36, 18));
            JPanel text = new JPanel(new GridLayout(2, 1));
            name.setFont(name.getFont().deriveFont(Font.PLAIN, 14f));
            hex.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            text.add(name);
            text.add(hex);
            add(swatch, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList<? extends PaletteEntry> list,
                                                      PaletteEntry value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                swatch.setBackground(value.getAwtColorRGB());
                name.setText(value.name);
                hex.setText(value.hex);
            } else {
                swatch.setBackground(list.getBackground());
                name.setText("");
                hex.setText("");
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    /** Favorite color (user palette entry). */
    private static class Favorite {
        final String hex;
        final Color color;

        Favorite(String hex) {
            String normalized = normalizeHex(hex);
            if (normalized == null) throw new IllegalArgumentException("Bad hex: " + hex);
            this.hex = normalized;
            int r = Integer.parseInt(this.hex.substring(1, 3), 16);
            int g = Integer.parseInt(this.hex.substring(3, 5), 16);
            int b = Integer.parseInt(this.hex.substring(5, 7), 16);
            this.color = new Color(r, g, b);
        }

        public String toString() { return hex; }
    }

    /** Renderer for favorites list (swatch + hex). */
    private static class FavoriteCellRenderer extends JPanel implements ListCellRenderer<Favorite> {
        private final JPanel swatch = new JPanel();
        private final JLabel label = new JLabel();

        FavoriteCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(6, 8, 6, 8));
            swatch.setPreferredSize(new Dimension(36, 18));
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            add(swatch, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList<? extends Favorite> list,
                                                      Favorite value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                swatch.setBackground(value.color);
                label.setText(value.hex);
            } else {
                swatch.setBackground(list.getBackground());
                label.setText("");
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    // ===== Drag & Drop =====

    /** Export hex string when dragging a favorite out. (Java 8-compatible instanceof) */
    private static class HexExportTransferHandler extends TransferHandler {
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JList) {
                JList<?> jl = (JList<?>) c;
                Object sel = jl.getSelectedValue();
                if (sel instanceof Favorite) {
                    Favorite fav = (Favorite) sel;
                    return new StringSelection(fav.hex);
                }
            }
            return null;
        }
        public int getSourceActions(JComponent c) { return COPY; }
    }

    /** Import hex string when dropping onto the parameter list; update that entry. */
    private class HexImportTransferHandler extends TransferHandler {
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                String text = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                String favHex = normalizeHex(text);
                if (favHex == null) return false;

                DropLocation dlBase = support.getDropLocation();
                if (!(dlBase instanceof JList.DropLocation)) return false;
                JList.DropLocation dl = (JList.DropLocation) dlBase;

                int index = dl.getIndex();
                if (index < 0 || index >= listModel.getSize()) return false;

                PaletteEntry entry = listModel.get(index);
                String merged = mergeFavoriteOntoTarget(favHex, entry.hex);
                updateEntryHex(entry, merged);
                list.setSelectedIndex(index);
                return true;
            } catch (Exception ex) {
                showError("Drop failed:\n" + ex.getMessage());
                return false;
            }
        }
    }
}
