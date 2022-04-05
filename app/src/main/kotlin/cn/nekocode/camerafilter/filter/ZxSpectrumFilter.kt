package cn.nekocode.camerafilter.filter

import android.content.Context
import android.opengl.GLES20
import cn.nekocode.camerafilter.MyGLUtils.buildProgram
import cn.nekocode.camerafilter.R

class ZxSpectrumFilter(context: Context) : CameraFilter(context) {
    private val program: Int = buildProgram(context, R.raw.vertext, R.raw.zx_spectrum)
    override fun onDraw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int) {
        setupShaderInputs(
            program,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(cameraTexId),
            arrayOf()
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}