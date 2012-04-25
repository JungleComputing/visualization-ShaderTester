package shadertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.media.opengl.GLException;

import openglCommon.math.VecF3;
import openglCommon.util.InputHandler;
import openglCommon.util.Settings;
import openglCommon.util.TextEditorKeyboardHandler;

public class ShaderTestInputHandler extends TextEditorKeyboardHandler {
    private final Settings     settings           = Settings.getInstance();
    private final InputHandler superClassInstance = InputHandler.getInstance();

    private static class SingletonHolder {
        public static final ShaderTestInputHandler instance = new ShaderTestInputHandler();
    }

    public static ShaderTestInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ShaderTestInputHandler() {
        super();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mousePressed(MouseEvent e) {
        superClassInstance.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        superClassInstance.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        superClassInstance.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        superClassInstance.mouseWheelMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        ShaderTestWindow.setRecompilationFlag();
    }

    @Override
    public octants getCurrentOctant() {
        return superClassInstance.getCurrentOctant();
    }

    @Override
    public VecF3 getRotation() {
        return superClassInstance.getRotation();
    }

    @Override
    public float getViewDist() {
        return superClassInstance.getViewDist();
    }

    @Override
    public void setRotation(VecF3 rotation) {
        superClassInstance.setRotation(rotation);
    }

    @Override
    public void setViewDist(float dist) {
        superClassInstance.setViewDist(dist);
    }

    @Override
    public void saveShader(String fileName) {
        String path = settings.getScreenshotPath();
        File newDir = new File(path + "screenshots");
        if (!newDir.exists())
            newDir.mkdir();

        String bareName = path + "screenshots/" + fileName;

        File newFile = new File(bareName + ".txt");
        try {
            int attempt = 1;
            while (newFile.exists()) {
                String newName = bareName + " (" + attempt + ")";

                if (ShaderTestWindow.isFragmentShader()) {
                    newName += ".fp";
                } else {
                    newName += ".vp";
                }
                newFile = new File(newName);

                attempt++;
            }

            System.out.println("Writing shader: " + newFile.getAbsolutePath());

            FileWriter outFile = new FileWriter(newFile);
            PrintWriter out = new PrintWriter(outFile);

            for (String line : getTextlines()) {
                out.println(line);
            }

            out.close();
            outFile.close();
        } catch (GLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
