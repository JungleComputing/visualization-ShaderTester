package shadertest;

import java.io.File;
import java.io.FileNotFoundException;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import openglCommon.CommonWindow;
import openglCommon.datastructures.FBO;
import openglCommon.datastructures.Material;
import openglCommon.exceptions.CompilationFailedException;
import openglCommon.exceptions.UninitializedException;
import openglCommon.math.Color4;
import openglCommon.math.MatF3;
import openglCommon.math.MatF4;
import openglCommon.math.MatrixFMath;
import openglCommon.math.Point4;
import openglCommon.math.VecF3;
import openglCommon.math.VecF4;
import openglCommon.models.Model;
import openglCommon.models.Text;
import openglCommon.models.base.Quad;
import openglCommon.models.base.Sphere;
import openglCommon.shaders.Program;
import openglCommon.textures.Perlin3D;

public class ShaderTestWindow extends CommonWindow {
    private Program   animatedTurbulenceShader, postprocessShader, textShader;

    private FBO       starFBO, hudFBO;
    private Model     FSQ_postprocess;

    private final int fontSize        = 20;
    private Text      myText;

    private Perlin3D  noiseTex;

    private float     offset          = 0;

    private Model     testModel;

    private boolean   post_processing = false;

    private File      vsFile;
    private MatF4     perspective;

    public ShaderTestWindow(ShaderTestInputHandler inputHandler) {
        super(inputHandler, true);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        try {
            final int status = drawable.getContext().makeCurrent();
            if ((status != GLContext.CONTEXT_CURRENT) && (status != GLContext.CONTEXT_CURRENT_NEW)) {
                System.err.println("Error swapping context to onscreen.");
            }
        } catch (final GLException e) {
            System.err.println("Exception while swapping context to onscreen.");
            e.printStackTrace();
        }

        final GL3 gl = drawable.getContext().getGL().getGL3();
        gl.glViewport(0, 0, canvasWidth, canvasHeight);

        displayContext(starFBO, hudFBO);

        try {
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void displayContext(FBO starFBO, FBO hudFBO) {
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();
        final float aspect = (float) width / (float) height;

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        final Point4 eye = new Point4((float) (radius * Math.sin(ftheta) * Math.cos(phi)), (float) (radius
                * Math.sin(ftheta) * Math.sin(phi)), (float) (radius * Math.cos(ftheta)), 1.0f);
        final Point4 at = new Point4(0.0f, 0.0f, 0.0f, 1.0f);
        final VecF4 up = new VecF4(0.0f, 1.0f, 0.0f, 0.0f);

        MatF4 mv = MatrixFMath.lookAt(eye, at, up);
        mv = mv.mul(MatrixFMath.translate(new VecF3(0f, 0f, inputHandler.getViewDist())));
        mv = mv.mul(MatrixFMath.rotationX(inputHandler.getRotation().get(0)));
        mv = mv.mul(MatrixFMath.rotationY(inputHandler.getRotation().get(1)));

        final MatF3 n = new MatF3();
        final MatF4 p = MatrixFMath.perspective(fovy, aspect, zNear, zFar);

        // Vertex shader variables
        loader.setUniformMatrix("NormalMatrix", n);
        loader.setUniformMatrix("PMatrix", p);
        perspective = p;
        loader.setUniformMatrix("SMatrix", MatrixFMath.scale(1));

        try {
            renderScene(gl, mv, starFBO);
            renderHUDText(gl, mv, hudFBO);

            if (post_processing) {
                renderTexturesToScreen(gl, width, height, starFBO, hudFBO);
            }
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();

        noiseTex.delete(gl);
        starFBO.delete(gl);
        hudFBO.delete(gl);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        // Load and compile shaders, then use program.
        try {
            vsFile = new File("shaders/vs_sunsurface.vp");
            animatedTurbulenceShader = loader.createProgram(gl, vsFile, new File("shaders/fs_animatedTurbulence.fp"));
            textShader = loader.createProgram(gl, new File("shaders/vs_curveShader.vp"), new File(
                    "shaders/fs_curveShader.fp"));
            postprocessShader = loader.createProgram(gl, new File("shaders/vs_postprocess.vp"), new File(
                    "shaders/fs_postprocess.fp"));
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // TEST MODEL
        Material testMaterial = new Material();
        testMaterial.setColor(Color4.red);
        testModel = new Sphere(testMaterial, 3, 50f, new VecF3());
        testModel.init(gl);

        // TEXT
        myText = new Text(new Material(Color4.t_green, Color4.t_green, Color4.t_green));
        myText.init(gl);

        try {
            ((ShaderTestInputHandler) inputHandler).setShaderText(new File("shaders/fs_marble.fp"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // FULL SCREEN QUADS
        FSQ_postprocess = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        FSQ_postprocess.init(gl);

        // TEXTURES
        noiseTex = new Perlin3D(128, GL.GL_TEXTURE0);
        noiseTex.init(gl);

        // Full screen textures (for post processing) done with FBO's
        starFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        hudFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);

        starFBO.init(gl);
        hudFBO.init(gl);
    }

    private void renderHUDText(GL3 gl, MatF4 mv, FBO hudFBO) throws UninitializedException {
        if (post_processing) {
            hudFBO.bind(gl);
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
        }

        final String text = ((ShaderTestInputHandler) inputHandler).getScreenText();
        myText.setString(gl, textShader, font, text, fontSize);

        myText.draw(gl, textShader, Text.getPMVForHUD(canvasWidth, canvasHeight, 30f, 2 * canvasHeight - 40));

        if (post_processing) {
            hudFBO.unBind(gl);
        }
    }

    @Override
    public void renderScene(GL3 arg0, MatF4 arg1) {
        // TODO Auto-generated method stub

    }

    private void renderScene(GL3 gl, MatF4 mv, FBO starFBO) throws UninitializedException {
        if (post_processing) {
            starFBO.bind(gl);
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }

        Program liveShader = animatedTurbulenceShader;
        try {
            liveShader = loader.createProgram(gl, vsFile, ((ShaderTestInputHandler) inputHandler).getShaderText());
            liveShader.init(gl);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CompilationFailedException e) {
            liveShader = animatedTurbulenceShader;
            System.err.println(e.getMessage());
        }
        if (liveShader != animatedTurbulenceShader) {
            System.out.println("New Shader compiled succesfully!");
            animatedTurbulenceShader = liveShader;

            animatedTurbulenceShader.setUniformMatrix("NormalMatrix", new MatF3());
            animatedTurbulenceShader.setUniformMatrix("PMatrix", perspective);
            animatedTurbulenceShader.setUniformMatrix("SMatrix", MatrixFMath.scale(1));
        }

        animatedTurbulenceShader.setUniformVector("HaloColor", Color4.red);
        animatedTurbulenceShader.setUniform("StarDrawMode", 0);

        noiseTex.use(gl);
        animatedTurbulenceShader.setUniform("Noise", noiseTex.getMultitexNumber());
        animatedTurbulenceShader.setUniform("NoiseScale", 1f);

        animatedTurbulenceShader.setUniformMatrix("SMatrix", MatrixFMath.scale(1));
        animatedTurbulenceShader.setUniform("Offset", offset);

        offset += .001f;

        testModel.draw(gl, animatedTurbulenceShader, mv);

        if (post_processing) {
            starFBO.unBind(gl);
        }
    }

    @Override
    public void renderTexturesToScreen(GL3 arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    private void renderTexturesToScreen(GL3 gl, int width, int height, FBO starFBO, FBO hudFBO) {
        postprocessShader.setUniform("starTexture", starFBO.getTexture().getMultitexNumber());
        postprocessShader.setUniform("hudTexture", hudFBO.getTexture().getMultitexNumber());

        postprocessShader.setUniform("starBrightness", 4f);
        postprocessShader.setUniform("hudBrightness", 4f);
        postprocessShader.setUniform("overallBrightness", 4f);

        postprocessShader.setUniformMatrix("PMatrix", new MatF4());
        postprocessShader.setUniform("scrWidth", width);
        postprocessShader.setUniform("scrHeight", height);

        try {
            postprocessShader.use(gl);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            FSQ_postprocess.draw(gl, postprocessShader, new MatF4());
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);
        final GL3 gl = drawable.getGL().getGL3();

        starFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        hudFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);

        starFBO.init(gl);
        hudFBO.init(gl);
    }
}
