/*
 * Copyright 2016 nekocode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.nekocode.camerafilter.filter

import android.content.Context
import android.opengl.GLES20
import cn.nekocode.camerafilter.MyGLUtils.buildProgram
import cn.nekocode.camerafilter.R
import cn.nekocode.camerafilter.RenderBuffer

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JFAVoronoiFilter(context: Context) : CameraFilter(context) {
    private val programImg: Int = buildProgram(context, R.raw.vertext, R.raw.voronoi)
    private val programA: Int = buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_a)
    private val programB: Int = buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_b)
    private val programC: Int = buildProgram(context, R.raw.vertext, R.raw.voronoi_buf_c)
    private var bufA: RenderBuffer? = null
    private var bufB: RenderBuffer? = null
    private var bufC: RenderBuffer? = null

    override fun onDraw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int) {
        // TODO move?
        if (bufA == null || bufA!!.width != canvasWidth || bufB!!.height != canvasHeight) {
            // Create new textures for buffering
            bufA = RenderBuffer(canvasWidth, canvasHeight, GLES20.GL_TEXTURE4)
            bufB = RenderBuffer(canvasWidth, canvasHeight, GLES20.GL_TEXTURE5)
            bufC = RenderBuffer(canvasWidth, canvasHeight, GLES20.GL_TEXTURE6)
        }

        // Render to buf a
        setupShaderInputs(
            programA,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(cameraTexId, bufA!!.texId),
            arrayOf(
                intArrayOf(canvasWidth, canvasHeight),
                intArrayOf(canvasWidth, canvasHeight)
            )
        )
        bufA!!.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        bufA!!.unbind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        // Render to buf b
        setupShaderInputs(
            programB,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(bufB!!.texId, bufA!!.texId),
            arrayOf(
                intArrayOf(canvasWidth, canvasHeight),
                intArrayOf(canvasWidth, canvasHeight)
            )
        )
        bufB!!.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        bufB!!.unbind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        // Render to buf c
        setupShaderInputs(
            programC,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(bufC!!.texId, bufB!!.texId),
            arrayOf(
                intArrayOf(canvasWidth, canvasHeight),
                intArrayOf(canvasWidth, canvasHeight)
            )
        )
        bufC!!.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        bufC!!.unbind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        // Render to screen
        setupShaderInputs(
            programImg,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(bufC!!.texId, bufA!!.texId),
            arrayOf(
                intArrayOf(canvasWidth, canvasHeight),
                intArrayOf(canvasWidth, canvasHeight)
            )
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}