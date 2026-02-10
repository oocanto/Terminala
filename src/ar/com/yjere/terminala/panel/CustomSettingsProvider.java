package ar.com.yjere.terminala.panel;

import java.awt.Color;

import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

public class CustomSettingsProvider extends DefaultSettingsProvider {

    private float fontSize;
    private Color backgroundColor;

    public CustomSettingsProvider(float fontSize, Color backgroundColor) {
        this.fontSize = fontSize;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public TextStyle getDefaultStyle() {
        TerminalColor bgColor = TerminalColor.rgb(this.backgroundColor.getRed(), this.backgroundColor.getGreen(),
                this.backgroundColor.getBlue());
        return new TextStyle(TerminalColor.WHITE, bgColor);
    }

    @Override
    public float getTerminalFontSize() {
        return this.fontSize;
    }
}