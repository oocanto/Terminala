package ar.com.yjere.terminala.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import ar.com.yjere.log.Logger;
import ar.com.yjere.terminala.Terminala;

public class TerminalPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final float DEFAULT_FONT_SIZE = 14.0f;
    private static final Color DEFAULT_BG_COLOR = Color.BLACK;

    private JediTermWidget terminal = null;
    private PtyProcess process = null;
    private Terminala app = null;
    private String currentDirectory = null;
    private float fontSize = DEFAULT_FONT_SIZE;
    private Color backgroundColor = DEFAULT_BG_COLOR;

    public TerminalPanel(Terminala app) {
        this(app, null, DEFAULT_FONT_SIZE, DEFAULT_BG_COLOR);
    }

    public TerminalPanel(Terminala app, String initialDirectory, float fontSize, Color backgroundColor) {
        this.app = app;
        this.currentDirectory = initialDirectory != null ? initialDirectory : System.getProperty("user.home");
        this.fontSize = fontSize;
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_BG_COLOR;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        setBackground(Color.DARK_GRAY);
        createTerminal();
    }

    public String getCurrentDirectory() {
        // Try to get current directory from the shell
        if (this.process != null && this.process.isAlive()) {
            try {
                // Read /proc/<pid>/cwd (Linux only)
                File cwd = new File("/proc/" + this.process.pid() + "/cwd");
                if (cwd.exists()) {
                    String path = cwd.getCanonicalPath();
                    this.currentDirectory = path;
                    return path;
                }
            } catch (Exception e) {
                // Ignore, return last known directory
            }
        }
        return this.currentDirectory;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setActive(boolean active) {
        if (active) {
            this.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        } else {
            this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        }
        repaint();
    }

    private void createTerminal() {
        try {
            this.terminal = new JediTermWidget(new CustomSettingsProvider(this.fontSize, this.backgroundColor));

            // Track keyboard activity
            this.terminal.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    app.setLastActiveTerminal(TerminalPanel.this);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    app.setLastActiveTerminal(TerminalPanel.this);
                }
            });

            // Track which terminal was last clicked
            this.terminal.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    app.setLastActiveTerminal(TerminalPanel.this);
                }
            });

            // Also track mouse entered
            this.terminal.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    app.setLastActiveTerminal(TerminalPanel.this);
                }
            });

            String[] command = { "/bin/bash", "--login" };
            HashMap<String, String> env = new HashMap<String, String>(System.getenv());
            env.put("TERM", "xterm-256color");

            // Start bash in the specified directory
            this.process = new PtyProcessBuilder().setCommand(command).setEnvironment(env)
                    .setDirectory(this.currentDirectory).start();

            this.terminal.setTtyConnector(new PtyProcessTtyConnector(this.process, StandardCharsets.UTF_8));
            this.terminal.start();

            // Monitor process termination
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        process.waitFor();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                app.closeTerminal(TerminalPanel.this);
                            }
                        });
                    } catch (Exception e) {
                        Logger.getInstance().logErrorMessage(e.getMessage() + System.lineSeparator() + "Stack trace: "
                                + System.lineSeparator() + Logger.getInstance().getStackTrace(e));
                    }
                }
            }).start();

            add(this.terminal, BorderLayout.CENTER);

        } catch (Exception e) {
            Logger.getInstance().logErrorMessage(e.getMessage() + System.lineSeparator() + "Stack trace: "
                    + System.lineSeparator() + Logger.getInstance().getStackTrace(e));
        }
    }

    public JediTermWidget getTerminal() {
        return this.terminal;
    }

    public void cleanup() {
        if (this.process != null && this.process.isAlive()) {
            this.process.destroy();
        }
    }
}