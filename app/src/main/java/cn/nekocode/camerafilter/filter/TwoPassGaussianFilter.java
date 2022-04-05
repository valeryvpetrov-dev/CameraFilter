package cn.nekocode.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES20;

import cn.nekocode.camerafilter.MyGLUtils;
import cn.nekocode.camerafilter.R;

public class TwoPassGaussianFilter extends CameraFilter {
    private final int program;

    public TwoPassGaussianFilter(Context context) {
        super(context);
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.two_pass_gaussian);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
