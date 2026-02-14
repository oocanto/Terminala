package ar.com.yjere.terminala.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public AboutDialog(JFrame parent, String version) {
        super(parent, "About", true);

        setSize(350, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Cerrar con tecla Escape
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior con ícono
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        URL iconUrl = getClass().getClassLoader().getResource("icons/terminal_64.png");
        if (iconUrl != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(iconUrl));
            topPanel.add(iconLabel);
        }

        // Panel central con información
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel appNameLabel = new JLabel(version != null && !version.trim().isEmpty() ? version : "Terminala",
                SwingConstants.CENTER);
        appNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel("Copyright © 2026 Terminala", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel websiteLabel = createLinkLabel("https://yjere.com.ar/terminala", "https://yjere.com.ar/terminala");
        JLabel emailLabel = createLinkLabel("yjere.ar@gmail.com", "mailto:yjere.ar@gmail.com");
        JLabel gitHubLabel = createLinkLabel("https://github.com/oocanto/Terminala",
                "https://github.com/oocanto/Terminala");

        // How to use section
        JPanel howToUsePanel = new JPanel();
        howToUsePanel.setLayout(new BoxLayout(howToUsePanel, BoxLayout.Y_AXIS));
        howToUsePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("How to use"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        howToUsePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font monoFont = new Font("Monospaced", Font.PLAIN, 11);

        JLabel shortcutHorizontalSplit = new JLabel("Ctrl + H   → Horizontal split");
        shortcutHorizontalSplit.setFont(monoFont);

        JLabel shortcutVerticalSplit = new JLabel("Ctrl + V   → Vertical split");
        shortcutVerticalSplit.setFont(monoFont);

        JLabel shortcutMaximize = new JLabel("Ctrl + M   → Maximize/Restore");
        shortcutMaximize.setFont(monoFont);

        JLabel shortcutSaveLayout = new JLabel("Ctrl + S   → Save layout");
        shortcutSaveLayout.setFont(monoFont);

        JLabel shortcutLoadLayout = new JLabel("Ctrl + L   → Load layout");
        shortcutLoadLayout.setFont(monoFont);

        JLabel shortcutCycleTerminals = new JLabel("Ctrl + Tab → Cycle terminals");
        shortcutCycleTerminals.setFont(monoFont);

        JLabel shortcutAbout = new JLabel("Ctrl + A   → About");
        shortcutAbout.setFont(monoFont);

        howToUsePanel.add(shortcutHorizontalSplit);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutVerticalSplit);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutMaximize);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutSaveLayout);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutLoadLayout);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutCycleTerminals);
        howToUsePanel.add(Box.createVerticalStrut(3));
        howToUsePanel.add(shortcutAbout);

        centerPanel.add(appNameLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(copyrightLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(websiteLabel);
        centerPanel.add(Box.createVerticalStrut(3));
        centerPanel.add(emailLabel);
        centerPanel.add(Box.createVerticalStrut(3));
        centerPanel.add(gitHubLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(howToUsePanel);

        // Panel inferior con botón cerrar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createLinkLabel(String text, final String url) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.BLUE);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setText(text);
            }
        });

        return label;
    }
}