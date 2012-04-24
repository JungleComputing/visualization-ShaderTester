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
    private final Settings               settings                     = Settings.getInstance();
    private final InputHandler           superClassInstance           = InputHandler.getInstance();

    private final String                 shaderTextLine0              = "Please load a shader ...";

    private ArrayList<String>            shaderLines                  = new ArrayList<String>();
    private ArrayList<String>            clipBoard                    = new ArrayList<String>();
    private ArrayList<ArrayList<String>> undoSave                     = new ArrayList<ArrayList<String>>();

    private int                          cursorPosition               = shaderTextLine0.length();
    private int                          linePosition                 = 0;
    private int                          screenPosition               = 0;

    private int                          selectionLineStartPosition   = 0;
    private int                          selectionLineStopPosition    = 0;
    private int                          selectionCursorStartPosition = 0;
    private int                          selectionCursorStopPosition  = 0;

    private final int                    MAX_SCREEN_POSITION          = 30;

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

    @SuppressWarnings("unchecked")
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            if (undoSave.size() != 0) {
                shaderLines = undoSave.remove(undoSave.size() - 1);
            }
        } else if (e.getKeyCode() != KeyEvent.VK_CONTROL && !isMovementKey(e)) {
            undoSave.add((ArrayList<String>) shaderLines.clone());
        }

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (isAnythingSelected()) {
                deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                        selectionCursorStopPosition);
            } else {
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
                    replaceCharAt(linePosition, cursorPosition - 1, "");
                    cursorPosition -= 1;
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (isAnythingSelected()) {
                deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                        selectionCursorStopPosition);
            } else {
                if (cursorPosition < shaderLines.get(linePosition).length()) {
                    replaceCharAt(linePosition, cursorPosition, "");
                } else {
                    String start = "", finish = "";
                    start = shaderLines.get(linePosition);
                    if (linePosition + 1 < shaderLines.size()) {
                        finish = shaderLines.get(linePosition + 1);
                        shaderLines.remove(linePosition + 1);
                    } else {
                        finish = "";
                    }
                    shaderLines.set(linePosition, start + finish);
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (isAnythingSelected()) {
                deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                        selectionCursorStopPosition);
            }

            insertStringAt(linePosition, cursorPosition, "    ");
            cursorPosition += 4;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (isAnythingSelected()) {
                deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                        selectionCursorStopPosition);
            }
            String start = shaderLines.get(linePosition).substring(0, cursorPosition);
            String finish = shaderLines.get(linePosition).substring(cursorPosition);

            shaderLines.set(linePosition, start);
            shaderLines.add(linePosition + 1, finish);
            linePosition += 1;
            cursorPosition = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            saveShader("FinishedShader");
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (cursorPosition == 0) {
                linePosition--;
                cursorPosition = 100000;
            } else {
                cursorPosition--;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (cursorPosition == shaderLines.get(linePosition).length()) {
                linePosition++;
                cursorPosition = 0;
            } else {
                cursorPosition++;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            linePosition -= 1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            linePosition += 1;
        } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
            cursorPosition = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_END) {
            cursorPosition = shaderLines.get(linePosition).length();
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            linePosition -= MAX_SCREEN_POSITION;
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            linePosition += MAX_SCREEN_POSITION;
        } else if (!e.isActionKey()) {
            if (e.getKeyCode() != KeyEvent.VK_SHIFT && e.getKeyCode() != KeyEvent.VK_CONTROL
                    && e.getKeyCode() != KeyEvent.VK_ALT && !e.isControlDown()) {
                if (isAnythingSelected()) {
                    deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                            selectionCursorStopPosition);
                }

                insertStringAt(linePosition, cursorPosition, "" + e.getKeyChar());
                cursorPosition += 1;
            }
        }

        // TEST ALL BORDER CASES
        if (linePosition < 0)
            linePosition = 0;
        if (linePosition > shaderLines.size() - 1)
            linePosition = shaderLines.size() - 1;
        if (cursorPosition < 0)
            cursorPosition = 0;
        if (cursorPosition > shaderLines.get(linePosition).length())
            cursorPosition = shaderLines.get(linePosition).length();

        if (linePosition < screenPosition)
            screenPosition = linePosition;

        if (linePosition > MAX_SCREEN_POSITION) {
            if (linePosition - MAX_SCREEN_POSITION > screenPosition) {
                screenPosition = linePosition - MAX_SCREEN_POSITION;
            }
        }

        if (screenPosition < 0)
            screenPosition = 0;
        if (screenPosition > shaderLines.size() - 1)
            screenPosition = shaderLines.size() - 1;

        if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
            if (isAnythingSelected()) {
                clipBoard = getSelection();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_X && e.isControlDown()) {
            clipBoard = getSelection();
            deleteSection(selectionLineStartPosition, selectionLineStopPosition, selectionCursorStartPosition,
                    selectionCursorStopPosition);
        }
        if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown()) {
            pasteClipboard();
        }

        // SELECTION HANDLING
        handleSelection(e);

        ShaderTestWindow.setRecompilationFlag();
    }

    private ArrayList<String> getSelection() {
        // swap order in weird selection cases
        int temp;
        if (selectionLineStopPosition < selectionLineStartPosition) {
            temp = selectionLineStopPosition;
            selectionLineStopPosition = selectionLineStartPosition;
            selectionLineStartPosition = temp;
        } else if (selectionLineStopPosition == selectionLineStartPosition) {
            if (selectionCursorStopPosition < selectionCursorStartPosition) {
                temp = selectionCursorStopPosition;
                selectionCursorStopPosition = selectionCursorStartPosition;
                selectionCursorStartPosition = temp;
            }
        }

        ArrayList<String> newSelectionLines = new ArrayList<String>();

        // select first line, from startCursor to end of line, or to
        // endCursor
        String selectionFirstLine = "";
        int pos = 0;
        for (Character c : shaderLines.get(selectionLineStartPosition).toCharArray()) {
            if (selectionLineStopPosition != selectionLineStartPosition) {
                if (pos >= selectionCursorStartPosition) {
                    selectionFirstLine += c;
                }
            } else {
                if (pos >= selectionCursorStartPosition && pos < selectionCursorStopPosition) {
                    selectionFirstLine += c;
                }
            }
            pos++;
        }
        newSelectionLines.add(selectionFirstLine);

        // Add intermediate lines fully
        for (int lineNR = selectionLineStartPosition + 1; lineNR < selectionLineStopPosition; lineNR++) {
            if (lineNR < selectionLineStopPosition) {
                newSelectionLines.add(shaderLines.get(lineNR));
            }
        }

        // Add the last line until the cursor position
        if (selectionLineStartPosition != selectionLineStopPosition) {
            String selectionLastLine = "";
            pos = 0;
            for (Character c : shaderLines.get(selectionLineStopPosition).toCharArray()) {
                if (pos < selectionCursorStopPosition) {
                    selectionLastLine += c;
                }
                pos++;
            }
            newSelectionLines.add(selectionLastLine);
        }

        return newSelectionLines;
    }

    private void handleSelection(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            if (!isAnythingSelected()) {
                selectionCursorStartPosition = cursorPosition;
                selectionLineStartPosition = linePosition;
            }
        } else if (e.isShiftDown() && isMovementKey(e)) {
            selectionCursorStopPosition = cursorPosition;
            selectionLineStopPosition = linePosition;
        } else if (e.getKeyCode() != KeyEvent.VK_CONTROL && !(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C)) {
            selectionCursorStartPosition = cursorPosition;
            selectionLineStartPosition = linePosition;
            selectionCursorStopPosition = cursorPosition;
            selectionLineStopPosition = linePosition;
        }
    }

    private void pasteClipboard() {
        if (clipBoard.size() > 0) {
            ArrayList<String> result = new ArrayList<String>();

            // First, add all of the lines prior to the line position
            for (int i = 0; i < linePosition; i++) {
                result.add(shaderLines.get(i));
            }

            // Add all of the characters on the current line prior to the cursor
            // position
            // And add all of the characters of the first line of the clipboard
            String startCharactersCurrentline = "", finishCharactersCurrentline = "";
            int pos = 0;
            for (Character c : shaderLines.get(linePosition).toCharArray()) {
                if (pos < cursorPosition) {
                    startCharactersCurrentline += c;
                } else {
                    finishCharactersCurrentline += c;
                }

                pos++;
            }
            if (clipBoard.size() == 1) {
                result.add(startCharactersCurrentline + clipBoard.get(0) + finishCharactersCurrentline);
                cursorPosition += clipBoard.get(clipBoard.size() - 1).length();
            } else {
                result.add(startCharactersCurrentline + clipBoard.get(0));

                // Add all of the intermediate lines on the clipboard
                for (int i = 1; i < clipBoard.size() - 1; i++) {
                    result.add(clipBoard.get(i));
                }

                // Add the remaining characters of the current line after the
                // cursor
                // position
                result.add(clipBoard.get(clipBoard.size() - 1) + finishCharactersCurrentline);
            }

            // Add all of the lines after the current line position
            for (int i = linePosition + 1; i < shaderLines.size(); i++) {
                result.add(shaderLines.get(i));
            }

            shaderLines = result;

            linePosition += clipBoard.size() - 1;
        }
    }

    private int posInScreenString(int lineIndex, int cursorIndex) {
        int result = 0;
        // First add the length of all lines before this one
        for (int lineNR = screenPosition; lineNR < lineIndex; lineNR++) {
            result += shaderLines.get(lineNR).length() + 1;
        }
        // then add the length of this line until the cursor position
        result += cursorIndex;

        return result;
    }

    @Override
    public void keyReleased(KeyEvent e) {
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

            for (String line : shaderLines) {
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

    public boolean isAnythingSelected() {
        if (!(selectionLineStopPosition - selectionLineStartPosition == 0 && selectionCursorStopPosition
                - selectionCursorStartPosition == 0)) {
            return true;
        }
        return false;
    }

    public boolean[] getSelectedMask() {
        // swap order in weird selection cases
        int temp;
        int startLineIndex = selectionLineStartPosition;
        int stopLineIndex = selectionLineStopPosition;
        int startCursorIndex = selectionCursorStartPosition;
        int stopCursorIndex = selectionCursorStopPosition;

        if (stopLineIndex < startLineIndex) {
            temp = stopLineIndex;
            stopLineIndex = startLineIndex;
            startLineIndex = temp;
        } else if (stopLineIndex == startLineIndex) {
            if (stopCursorIndex < startCursorIndex) {
                temp = stopCursorIndex;
                stopCursorIndex = startCursorIndex + 1;
                startCursorIndex = temp;
            }
        }

        boolean[] result = new boolean[getScreenText().length()];
        for (int i = 0; i < result.length; i++) {
            if (i >= posInScreenString(startLineIndex, startCursorIndex)
                    && i < posInScreenString(stopLineIndex, stopCursorIndex)) {
                result[i] = true;
            } else {
                result[i] = false;
            }
        }

        return result;
    }

    public boolean[] getUnSelectedMask() {
        boolean[] result = new boolean[getScreenText().length()];
        boolean[] inverse = getSelectedMask();
        for (int i = 0; i < result.length; i++) {
            result[i] = !inverse[i];
        }

        return result;
    }

    private boolean isMovementKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT
                || code == KeyEvent.VK_PAGE_UP || code == KeyEvent.VK_PAGE_DOWN || code == KeyEvent.VK_HOME
                || code == KeyEvent.VK_END) {
            return true;
        }
        return false;
    }

    private boolean isMovementForward(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_PAGE_DOWN
                || code == KeyEvent.VK_END) {
            return true;
        }
        return false;
    }

    private boolean isMovementBackward(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_PAGE_UP
                || code == KeyEvent.VK_HOME) {
            return true;
        }
        return false;
    }

    private boolean currentPosBeforeSelectionStart() {
        if (linePosition < selectionLineStartPosition
                || (linePosition == selectionLineStartPosition && cursorPosition < selectionCursorStartPosition)) {
            return true;
        }
        return false;
    }

    private boolean currentPosAfterSelectionStop() {
        if (linePosition > selectionLineStopPosition
                || (linePosition == selectionLineStopPosition && cursorPosition > selectionCursorStopPosition)) {
            return true;
        }
        return false;
    }

    private void deleteSection(int startLineIndex, int stopLineIndex, int startCursorindex, int stopCursorindex) {
        // swap order in weird selection cases
        int temp;
        if (stopLineIndex < startLineIndex) {
            temp = stopLineIndex;
            stopLineIndex = startLineIndex;
            startLineIndex = temp;
        } else if (stopLineIndex == startLineIndex) {
            if (stopCursorindex < startCursorindex) {
                temp = stopCursorindex;
                stopCursorindex = startCursorindex;
                startCursorindex = temp;
            }
        }

        String start = shaderLines.get(startLineIndex).substring(0, startCursorindex);
        String finish = shaderLines.get(stopLineIndex).substring(stopCursorindex);

        for (int i = startLineIndex; i < stopLineIndex; i++) {
            shaderLines.remove(i);
        }

        shaderLines.set(startLineIndex, start + finish);

        linePosition = startLineIndex;
        cursorPosition = startCursorindex;
    }

    private void replaceCharAt(int lineIndex, int cursorIndex, String newChar) {
        String start = shaderLines.get(lineIndex).substring(0, cursorIndex);
        String finish = shaderLines.get(lineIndex).substring(cursorIndex + 1);

        shaderLines.set(lineIndex, start + newChar + finish);
    }

    private void insertStringAt(int lineIndex, int cursorIndex, String newChar) {
        String start = shaderLines.get(lineIndex).substring(0, cursorIndex);
        String finish = shaderLines.get(lineIndex).substring(cursorIndex);

        shaderLines.set(lineIndex, start + newChar + finish);
    }
}
