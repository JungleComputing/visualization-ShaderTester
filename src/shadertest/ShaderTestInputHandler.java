package shadertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.media.opengl.GLException;

import openglCommon.math.VecF3;
import openglCommon.util.InputHandler;
import openglCommon.util.Settings;

public class ShaderTestInputHandler extends InputHandler {
    private final Settings settings = Settings.getInstance();
    private final InputHandler superClassInstance = InputHandler.getInstance();

    private final String shaderTextLine0 = "Please load a shader ...";

    private ArrayList<String> shaderLines = new ArrayList<String>();
    private int cursorPosition = shaderTextLine0.length();
    private int linePosition = 0;
    private int screenPosition = 0;

    private final int MAX_SCREEN_POSITION = 30;

    private static class SingletonHolder {
        public static final ShaderTestInputHandler instance = new ShaderTestInputHandler();
    }

    public static ShaderTestInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ShaderTestInputHandler() {
        super();

        shaderLines.add(shaderTextLine0);
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
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (cursorPosition == 0) {
                if (linePosition != 0) {
                    linePosition -= 1;
                    cursorPosition = shaderLines.get(linePosition).length();
                    String start = shaderLines.get(linePosition).substring(0, cursorPosition);
                    String finish = shaderLines.get(linePosition + 1);

                    shaderLines.set(linePosition, start + finish);
                    shaderLines.remove(linePosition + 1);
                }
            } else {
                String start = "", finish = "";
                int pos = 0;
                for (Character c : shaderLines.get(linePosition).toCharArray()) {
                    if (pos < cursorPosition - 1) {
                        start += c;
                    } else if (pos == cursorPosition - 1) {
                    } else {
                        finish += c;
                    }

                    pos++;
                }
                shaderLines.set(linePosition, start + finish);
                cursorPosition -= 1;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            String start = "", finish = "";
            int pos = 0;
            for (Character c : shaderLines.get(linePosition).toCharArray()) {
                if (pos < cursorPosition) {
                    start += c;
                } else if (pos == cursorPosition) {
                } else {
                    finish += c;
                }

                pos++;
            }
            shaderLines.set(linePosition, start + finish);
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {

            String start = "", finish = "";
            int pos = 0;
            for (Character c : shaderLines.get(linePosition).toCharArray()) {
                if (pos < cursorPosition) {
                    start += c;
                } else {
                    finish += c;
                }

                pos++;
            }
            shaderLines.set(linePosition, start + "    " + finish);
            cursorPosition += 4;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String start = "", finish = "";
            int pos = 0;
            for (Character c : shaderLines.get(linePosition).toCharArray()) {
                if (pos < cursorPosition) {
                    start += c;
                } else {
                    finish += c;
                }

                pos++;
            }
            shaderLines.set(linePosition, start);
            shaderLines.add(linePosition + 1, finish);
            linePosition += 1;
            cursorPosition = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            saveShader("FinishedShader");
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            cursorPosition -= 1;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            cursorPosition += 1;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            linePosition -= 1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            linePosition += 1;
        } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
            cursorPosition = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_END) {
            cursorPosition += 100000;
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            screenPosition -= MAX_SCREEN_POSITION;
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            screenPosition += MAX_SCREEN_POSITION;
        } else if (!e.isActionKey()) {
            if (e.getKeyCode() != KeyEvent.VK_SHIFT && e.getKeyCode() != KeyEvent.VK_CONTROL
                    && e.getKeyCode() != KeyEvent.VK_ALT) {
                String start = "", finish = "";
                int pos = 0;
                for (Character c : shaderLines.get(linePosition).toCharArray()) {
                    if (pos < cursorPosition) {
                        start += c;
                    } else {
                        finish += c;
                    }

                    pos++;
                }
                shaderLines.set(linePosition, start + e.getKeyChar() + finish);
                cursorPosition += 1;
            }
        }

        if (linePosition < 0)
            linePosition = 0;
        if (linePosition > shaderLines.size() - 1)
            linePosition = shaderLines.size() - 1;
        if (cursorPosition < 0)
            cursorPosition = 0;
        if (cursorPosition > shaderLines.get(linePosition).length())
            cursorPosition = shaderLines.get(linePosition).length();

        if (linePosition > MAX_SCREEN_POSITION)
            screenPosition = linePosition - MAX_SCREEN_POSITION;

        if (screenPosition < 0)
            screenPosition = 0;
        if (screenPosition > shaderLines.size() - 1)
            screenPosition = shaderLines.size() - 1;

        ShaderTestWindow.setRecompilationFlag();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent e) {
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

    public void setShaderText(File textFile) throws FileNotFoundException {
        linePosition = 0;
        cursorPosition = 0;
        shaderLines = new ArrayList<String>();

        FileInputStream fstream = new FileInputStream(textFile);

        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine;
        try {
            while ((strLine = br.readLine()) != null) {
                String trimmedLine = "";
                for (Character c : strLine.toCharArray()) {
                    if (c.compareTo('\t') == 0) {
                        trimmedLine += "    ";
                    } else {
                        trimmedLine += c;
                    }
                }
                shaderLines.add(trimmedLine);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getShaderText() {
        String result = "";

        for (String line : shaderLines) {
            result += line + "\n";
        }

        return result;
    }

    public String getScreenText() {
        String result = "";

        int linePos = screenPosition;
        for (String line : shaderLines.subList(linePos, shaderLines.size())) {
            if (linePosition < linePos) {
                result += line;
            } else if (linePosition == linePos) {
                String start = "", finish = "";
                int pos = 0;
                for (Character c : line.toCharArray()) {
                    if (pos < cursorPosition) {
                        start += c;
                    } else {
                        finish += c;
                    }

                    pos++;
                }
                result += start + "|" + finish;
            } else {
                result += line;
            }
            result += "\n";
            linePos++;
        }

        return result;
    }

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
                String newName = bareName + " (" + attempt + ").txt";
                newFile = new File(newName);

                attempt++;
            }

            System.out.println("Writing shader: " + newFile.getAbsolutePath());

            FileWriter outFile = new FileWriter(newFile);
            PrintWriter out = new PrintWriter(outFile);

            for (String line : shaderLines) {
                out.println(line + "\n");
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
