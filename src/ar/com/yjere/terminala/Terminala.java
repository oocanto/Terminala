package ar.com.yjere.terminala;

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ar.com.yjere.console.ParameterParser;
import ar.com.yjere.exception.YjereException;
import ar.com.yjere.log.Logger;
import ar.com.yjere.service.Constant;
import ar.com.yjere.service.Service;
import ar.com.yjere.terminala.dialog.AboutDialog;
import ar.com.yjere.terminala.panel.TerminalPanel;

public class Terminala extends JFrame {

    private static final long serialVersionUID = 1L;
    private Container currentContainer = null;
    private HashMap<TerminalPanel, Container> terminalParents = new HashMap<TerminalPanel, Container>();
    private TerminalPanel lastActiveTerminal = null;
    private static final String CONFIG_FILE = "Terminala.xml";
    private static final String LOG_FILE = "log/%1$s_Terminala.log";
    String value = null;
    Manifest manifest = null;

    public Terminala() {

        URL iconUrl = Terminala.class.getClassLoader().getResource("icons/terminal_64.png");

        setIconImage(new ImageIcon(iconUrl).getImage());

        // Obtención del Manifest...
        try {
            manifest = Service.getInstance().getManifest(Terminala.class);
        } catch (IOException e) {
        }

        value = Service.getInstance().getApplicationVersion(manifest);

        Logger.getInstance().logInfoMessage(value != null ? value + "." : "Init.");

        setTitle((value != null && !value.trim().equals("") ? (value) : "Terminala") + " · [Ctrl + A → About]");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.currentContainer = getContentPane();

        // Try to load layout, if fails create default terminal
        if (!this.loadLayout()) {
            TerminalPanel initialTerminal = new TerminalPanel(this);
            this.currentContainer.add(initialTerminal);
            this.terminalParents.put(initialTerminal, this.currentContainer);
            this.lastActiveTerminal = initialTerminal;
            initialTerminal.setActive(true);
        }

        // Add keyboard shortcuts
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown()) {
                    // Ctrl + Tab - cycle to next terminal
                    if (e.getKeyCode() == KeyEvent.VK_TAB && !e.isShiftDown()) {
                        cycleToNextTerminal();
                        return true;
                    } else {
                        // Ctrl + A - show about dialog
                        if (e.getKeyCode() == KeyEvent.VK_A && !e.isShiftDown()) {
                            showAboutDialog();
                            return true;
                        } else {
                            // Ctrl + S - save layout
                            if (e.getKeyCode() == KeyEvent.VK_S && !e.isShiftDown()) {
                                saveLayout();
                                return true;
                            } else {
                                // Ctrl + L - load layout
                                if (e.getKeyCode() == KeyEvent.VK_L && !e.isShiftDown()) {
                                    reloadLayout();
                                    return true;
                                } else {
                                    // Ctrl + H - split horizontal
                                    if (e.getKeyCode() == KeyEvent.VK_H && !e.isShiftDown()
                                            && lastActiveTerminal != null) {
                                        splitTerminal(lastActiveTerminal, JSplitPane.VERTICAL_SPLIT);
                                        return true;
                                    } else {
                                        // Ctrl + V - split vertical
                                        if (e.getKeyCode() == KeyEvent.VK_V && !e.isShiftDown()
                                                && lastActiveTerminal != null) {
                                            splitTerminal(lastActiveTerminal, JSplitPane.HORIZONTAL_SPLIT);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        // Timer to poll for focus changes
        Timer focusTimer = new Timer(200, e -> {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null) {
                // Find which TerminalPanel contains this component
                Iterator<TerminalPanel> it = this.terminalParents.keySet().iterator();
                while (it.hasNext()) {
                    TerminalPanel terminalPanel = it.next();
                    if (SwingUtilities.isDescendingFrom(focusOwner, terminalPanel)) {
                        if (terminalPanel != this.lastActiveTerminal) {
                            this.setLastActiveTerminal(terminalPanel);
                        }
                        break;
                    }
                }
            }
        });
        focusTimer.start();

        setVisible(true);
        if (lastActiveTerminal != null) {
            lastActiveTerminal.getTerminal().requestFocusInWindow();
        }
    }

    public void setLastActiveTerminal(TerminalPanel terminal) {

        // Remove active border from previous terminal
        if (this.lastActiveTerminal != null) {
            this.lastActiveTerminal.setActive(false);
        }

        // Set new active terminal
        this.lastActiveTerminal = terminal;

        // Add active border to new terminal
        if (terminal != null) {
            terminal.setActive(true);
        }
    }

    private void cycleToNextTerminal() {

        if (this.terminalParents.isEmpty())
            return;

        // Get all terminals as a list
        List<TerminalPanel> terminals = new ArrayList<TerminalPanel>(this.terminalParents.keySet());

        if (terminals.isEmpty())
            return;

        // Find current terminal index
        int currentIndex = terminals.indexOf(this.lastActiveTerminal);

        // Get next terminal (cycle back to 0 if at end)
        int nextIndex = (currentIndex + 1) % terminals.size();
        TerminalPanel nextTerminal = terminals.get(nextIndex);

        // Set as active and request focus
        setLastActiveTerminal(nextTerminal);
        nextTerminal.getTerminal().requestFocusInWindow();
    }

    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog(this, this.value);
        dialog.setVisible(true);
    }

    public void splitTerminal(TerminalPanel existingTerminal, int orientation) {
        Container parent = this.terminalParents.get(existingTerminal);

        if (parent == null)
            return;

        // Create new terminal
        TerminalPanel newTerminal = new TerminalPanel(this);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(orientation);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(2);

        // Remove existing terminal from parent
        parent.remove(existingTerminal);

        // Add terminals to split pane
        splitPane.setLeftComponent(existingTerminal);
        splitPane.setRightComponent(newTerminal);

        // Add split pane to parent
        parent.add(splitPane);

        // Update parent references
        this.terminalParents.put(existingTerminal, splitPane);
        this.terminalParents.put(newTerminal, splitPane);

        // Refresh UI
        parent.revalidate();
        parent.repaint();

        // Focus new terminal and set as active
        newTerminal.getTerminal().requestFocusInWindow();
        this.lastActiveTerminal = newTerminal;
    }

    public void closeTerminal(TerminalPanel terminal) {
        Container parent = this.terminalParents.get(terminal);

        if (parent == null)
            return;

        terminal.cleanup();
        this.terminalParents.remove(terminal);

        if (terminal == this.lastActiveTerminal) {
            this.lastActiveTerminal = null;
        }

        if (parent instanceof JSplitPane) {

            JSplitPane splitPane = (JSplitPane) parent;

            // Find sibling terminal
            Component sibling = null;
            if (splitPane.getLeftComponent() == terminal) {
                sibling = splitPane.getRightComponent();
            } else {
                if (splitPane.getRightComponent() == terminal) {
                    sibling = splitPane.getLeftComponent();
                }
            }

            if (sibling != null) {

                // Find split pane's parent
                Container splitParent = splitPane.getParent();

                if (splitParent != null) {
                    // Remove split pane
                    splitParent.remove(splitPane);

                    // Add sibling directly to parent
                    splitParent.add(sibling);

                    // Update parent reference for sibling
                    if (sibling instanceof TerminalPanel) {
                        this.terminalParents.put((TerminalPanel) sibling, splitParent);
                        this.lastActiveTerminal = (TerminalPanel) sibling;
                    } else {
                        if (sibling instanceof JSplitPane) {
                            updateSplitPaneParents((JSplitPane) sibling, splitParent);
                        }
                    }

                    // Refresh UI
                    splitParent.revalidate();
                    splitParent.repaint();

                    // Focus sibling if it's a terminal
                    if (sibling instanceof TerminalPanel) {
                        ((TerminalPanel) sibling).getTerminal().requestFocusInWindow();
                    }
                }
            }
        } else {
            // Last terminal closed - exit
            System.exit(0);
        }
    }

    private void updateSplitPaneParents(JSplitPane splitPane, Container newParent) {

        Component left = splitPane.getLeftComponent();
        Component right = splitPane.getRightComponent();

        if (left instanceof TerminalPanel) {
            this.terminalParents.put((TerminalPanel) left, splitPane);
        } else {
            if (left instanceof JSplitPane) {
                updateSplitPaneParents((JSplitPane) left, splitPane);
            }
        }

        if (right instanceof TerminalPanel) {
            this.terminalParents.put((TerminalPanel) right, splitPane);
        } else {
            if (right instanceof JSplitPane) {
                updateSplitPaneParents((JSplitPane) right, splitPane);
            }
        }
    }

    private void saveLayout() {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            Element rootElement = document.createElement("layout");
            document.appendChild(rootElement);

            // Serialize the current container
            Component component = this.currentContainer.getComponent(0);
            this.serializeComponent(document, rootElement, component);

            // Write to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(Constant.BASE_PATH + Terminala.CONFIG_FILE));
            transformer.transform(source, result);

            Logger.getInstance().logInfoMessage("Layout saved to " + Constant.BASE_PATH + Terminala.CONFIG_FILE);

        } catch (Exception e) {
            Logger.getInstance().logErrorMessage(e.getMessage() + System.lineSeparator() + "Stack trace: "
                    + System.lineSeparator() + Logger.getInstance().getStackTrace(e));
        }
    }

    private void serializeComponent(Document document, Element parent, Component component) {

        if (component instanceof JSplitPane) {

            JSplitPane split = (JSplitPane) component;
            Element splitElement = document.createElement("split");

            // Save orientation
            String orientation = (split.getOrientation() == JSplitPane.VERTICAL_SPLIT) ? "vertical" : "horizontal";
            splitElement.setAttribute("orientation", orientation);

            // Save absolute divider location in pixels
            int dividerLocation = split.getDividerLocation();
            splitElement.setAttribute("divider", String.valueOf(dividerLocation));

            parent.appendChild(splitElement);

            // Serialize children
            if (split.getLeftComponent() != null) {
                this.serializeComponent(document, splitElement, split.getLeftComponent());
            }
            if (split.getRightComponent() != null) {
                this.serializeComponent(document, splitElement, split.getRightComponent());
            }

        } else {
            if (component instanceof TerminalPanel) {
                TerminalPanel tp = (TerminalPanel) component;
                Element terminalElement = document.createElement("terminal");

                // Save current directory
                String dir = tp.getCurrentDirectory();
                terminalElement.setAttribute("directory", dir);

                // Save font size
                float fontSize = tp.getFontSize();
                terminalElement.setAttribute("fontSize", String.valueOf(fontSize));

                // Save background color
                Color bgColor = tp.getBackgroundColor();
                String colorHex = String.format("#%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(),
                        bgColor.getBlue());
                terminalElement.setAttribute("backgroundColor", colorHex);

                parent.appendChild(terminalElement);
            }
        }
    }

    private boolean loadLayout() {

        File layoutFile = new File(Constant.BASE_PATH + Terminala.CONFIG_FILE);
        if (!layoutFile.exists()) {
            return false;
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.parse(layoutFile);
            document.getDocumentElement().normalize();

            // Clear current layout
            this.terminalParents.clear();
            this.currentContainer.removeAll();

            // Load layout
            Element root = document.getDocumentElement();
            NodeList children = root.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Component component = deserializeComponent((Element) node, this.currentContainer);
                    if (component != null) {
                        this.currentContainer.add(component);
                    }
                }
            }

            // Set first terminal as active
            if (!this.terminalParents.isEmpty()) {
                this.lastActiveTerminal = this.terminalParents.keySet().iterator().next();
                this.lastActiveTerminal.setActive(true);
            }

            this.currentContainer.revalidate();
            this.currentContainer.repaint();

            Logger.getInstance().logInfoMessage("Layout loaded from " + Constant.BASE_PATH + Terminala.CONFIG_FILE);

            return true;

        } catch (Exception e) {
            Logger.getInstance().logErrorMessage(e.getMessage() + System.lineSeparator() + "Stack trace: "
                    + System.lineSeparator() + Logger.getInstance().getStackTrace(e));
            return false;
        }
    }

    private Component deserializeComponent(Element element, Container parent) {

        String tagName = element.getTagName();

        if (tagName.equals("terminal")) {
            String directory = element.getAttribute("directory");

            // Expandir ~ al home del usuario
            if (directory != null && directory.equals("~")) {
                directory = System.getProperty("user.home");
            } else {
                if (directory != null && directory.startsWith("~/")) {
                    directory = System.getProperty("user.home") + directory.substring(1);
                } else {
                    if (directory == null || directory.isEmpty()) {
                        directory = System.getProperty("user.home");
                    }
                }
            }

            // Load font size
            String fontSizeStr = element.getAttribute("fontSize");
            float fontSize = 14.0f; // default
            if (fontSizeStr != null && !fontSizeStr.isEmpty()) {
                try {
                    fontSize = Float.parseFloat(fontSizeStr);
                } catch (NumberFormatException e) {
                    fontSize = 14.0f;
                }
            }

            // Load background color
            String bgColorStr = element.getAttribute("backgroundColor");
            Color backgroundColor = Color.BLACK; // default
            if (bgColorStr != null && !bgColorStr.isEmpty()) {
                try {
                    backgroundColor = Color.decode(bgColorStr);
                } catch (NumberFormatException e) {
                    backgroundColor = Color.BLACK;
                }
            }

            TerminalPanel terminal = new TerminalPanel(this, directory, fontSize, backgroundColor);
            this.terminalParents.put(terminal, parent);
            return terminal;

        } else {
            if (tagName.equals("split")) {
                String orientation = element.getAttribute("orientation");
                final int dividerLocation = Integer.parseInt(element.getAttribute("divider"));

                int orient = orientation.equals("vertical") ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
                final JSplitPane splitPane = new JSplitPane(orient);
                splitPane.setResizeWeight(0.5);
                splitPane.setDividerSize(2);
                splitPane.setDividerLocation(dividerLocation);

                // Deserialize children
                NodeList children = element.getChildNodes();
                int childIndex = 0;

                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Component child = deserializeComponent((Element) node, splitPane);
                        if (child != null) {
                            if (childIndex == 0) {
                                splitPane.setLeftComponent(child);
                            } else if (childIndex == 1) {
                                splitPane.setRightComponent(child);
                            }
                            childIndex++;
                        }
                    }
                }

                return splitPane;
            }
        }

        return null;
    }

    private void reloadLayout() {

        if (this.loadLayout()) {
            if (this.lastActiveTerminal != null) {
                this.lastActiveTerminal.getTerminal().requestFocusInWindow();
            }
        }
    }

    public static void main(String[] args) {

        SimpleDateFormat sdfCommandLine = null;
        String dateTimeMark = null;
        Map<String, List<String>> params = null;
        PrintStream stream = null;

        sdfCommandLine = new SimpleDateFormat(Constant.DATE_TIME_PATTERN_COMMAND_LINE);
        dateTimeMark = sdfCommandLine.format(new Date());

        // Lectura de parámetros...
        params = ParameterParser.getInstance().parse(args);

        // Inicialización de hook de cierre...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Logger.getInstance().logInfoMessage("Shutdown.");
            }
        });

        // Inicialización de parámetros básicos...
        Iterator<Entry<String, List<String>>> it = params.entrySet().iterator();
        Map.Entry<String, List<String>> entry = null;

        while (it.hasNext()) {

            entry = (Map.Entry<String, List<String>>) it.next();

            if (entry.getKey().equals(Constant.PARAMETER_BASE_PATH_NAME) && entry.getValue().size() > 0) {
                Constant.BASE_PATH = entry.getValue().get(0).trim();
                break;
            }
        }

        // Armado del stream...
        try {
            stream = new PrintStream(
                    new FileOutputStream(Constant.BASE_PATH + String.format(Terminala.LOG_FILE, dateTimeMark)));
        } catch (FileNotFoundException e) {
            throw new YjereException(e);
        }

        // Inicialización de log...
        Logger.getInstance().initialize(stream);

        Logger.getInstance().logInfoMessage("Init.");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Terminala();
            }
        });
    }
}