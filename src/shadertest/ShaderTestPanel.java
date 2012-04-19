package shadertest;

import java.awt.KeyboardFocusManager;
import java.util.Collections;

import openglCommon.CommonPanel;

public class ShaderTestPanel extends CommonPanel {
    private static final long serialVersionUID = 1L;
    private ShaderTestWindow  shaderWindow;

    public ShaderTestPanel(ShaderTestWindow shaderWindow, String path, String cmdlnfileName) {
        super(shaderWindow, ShaderTestInputHandler.getInstance());

        this.shaderWindow = shaderWindow;

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    }

    void close() {
        shaderWindow.dispose(glCanvas);
    }
}
