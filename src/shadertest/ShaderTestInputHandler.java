package shadertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.media.opengl.GLException;

import openglCommon.math.Color4;
import openglCommon.math.VecF3;
import openglCommon.util.InputHandler;
import openglCommon.util.Settings;
import openglCommon.util.TextEditorKeyboardHandler;

public class ShaderTestInputHandler extends TextEditorKeyboardHandler {
    private final Settings          settings           = Settings.getInstance();
    private final InputHandler      superClassInstance = InputHandler.getInstance();
    private HashMap<String, Color4> syntaxColors;

    private static class SingletonHolder {
        public static final ShaderTestInputHandler instance = new ShaderTestInputHandler();
    }

    public static ShaderTestInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ShaderTestInputHandler() {
        super();
        syntaxColors = new HashMap<String, Color4>();

        // Vertex shader special outputs
        syntaxColors.put("gl_Position", Color4.sickly_green);
        syntaxColors.put("gl_PointSize", Color4.sickly_green);
        syntaxColors.put("gl_ClipVertex", Color4.sickly_green);

        // Fragment shader special inputs
        syntaxColors.put("gl_FragCoord", Color4.sickly_green);
        syntaxColors.put("gl_FrontFacing", Color4.sickly_green);

        // Fragment shader special outputs
        syntaxColors.put("gl_FragColor", Color4.sickly_green);
        syntaxColors.put("gl_FragData[gl_MaxDrawBuffers]", Color4.sickly_green);
        syntaxColors.put("gl_FragDepth", Color4.sickly_green);

        // keywords
        syntaxColors.put("void", Color4.magenta);
        syntaxColors.put("in", Color4.magenta);
        syntaxColors.put("out", Color4.magenta);
        syntaxColors.put("inout", Color4.magenta);
        syntaxColors.put("uniform", Color4.magenta);
        syntaxColors.put("const", Color4.magenta);
        syntaxColors.put("attribute", Color4.magenta);
        syntaxColors.put("varying", Color4.magenta);

        syntaxColors.put("if", Color4.magenta);
        syntaxColors.put("for", Color4.magenta);
        syntaxColors.put("else", Color4.magenta);

        // Variable types
        syntaxColors.put("int", Color4.blue);
        syntaxColors.put("ivec2", Color4.blue);
        syntaxColors.put("ivec3", Color4.blue);
        syntaxColors.put("ivec4", Color4.blue);
        syntaxColors.put("float", Color4.blue);
        syntaxColors.put("vec2", Color4.blue);
        syntaxColors.put("vec3", Color4.blue);
        syntaxColors.put("vec4", Color4.blue);
        syntaxColors.put("mat3", Color4.blue);
        syntaxColors.put("mat4", Color4.blue);
        syntaxColors.put("bool", Color4.blue);
        syntaxColors.put("bvec2", Color4.blue);
        syntaxColors.put("bvec3", Color4.blue);
        syntaxColors.put("bvec4", Color4.blue);
        syntaxColors.put("sampler1D", Color4.blue);
        syntaxColors.put("sampler2D", Color4.blue);
        syntaxColors.put("sampler3D", Color4.blue);
        syntaxColors.put("samplerCube", Color4.blue);
        syntaxColors.put("sampler1DShadow", Color4.blue);
        syntaxColors.put("sampler2DShadow", Color4.blue);

        // functions
        syntaxColors.put("abs", Color4.sickly_magenta);
        syntaxColors.put("ceil", Color4.sickly_magenta);
        syntaxColors.put("floor", Color4.sickly_magenta);
        syntaxColors.put("fract", Color4.sickly_magenta);
        syntaxColors.put("min", Color4.sickly_magenta);
        syntaxColors.put("max", Color4.sickly_magenta);
        syntaxColors.put("mod", Color4.sickly_magenta);
        syntaxColors.put("clamp", Color4.sickly_magenta);
        syntaxColors.put("sign", Color4.sickly_magenta);
        syntaxColors.put("smoothstep", Color4.sickly_magenta);
        syntaxColors.put("step", Color4.sickly_magenta);
        syntaxColors.put("mix", Color4.sickly_magenta);
        syntaxColors.put("texture", Color4.sickly_magenta);
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

    public HashMap<String, Color4> getSyntaxColors() {
        return syntaxColors;
    }
}
