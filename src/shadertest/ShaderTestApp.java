package shadertest;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class ShaderTestApp {
    public static void main(String[] arguments) {
        String cmdlnfileName = null;
        String path = "";

        path = System.getProperty("user.dir");

        final JFrame frame = new JFrame("Shader Tester");
        frame.setPreferredSize(new Dimension(1024, 768));

        final ShaderTestWindow window = new ShaderTestWindow(ShaderTestInputHandler.getInstance());
        final ShaderTestPanel panel = new ShaderTestPanel(window, path, cmdlnfileName);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    frame.getContentPane().add(panel);

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            panel.close();
                            System.exit(0);
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });

        // Display the window.
        frame.pack();

        // center on screen
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
