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
import android.opengl.GLES11Ext
import android.opengl.GLES20
import androidx.annotation.CallSuper
import cn.nekocode.camerafilter.MyGLUtils.buildProgram
import cn.nekocode.camerafilter.R
import cn.nekocode.camerafilter.RenderBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
abstract class CameraFilter(context: Context) {

    companion object {
        val SQUARE_COORDS = floatArrayOf(
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
        )
        val TEXTURE_COORDS = floatArrayOf(
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        )
        var VERTEX_BUF: FloatBuffer? = null
        var TEXTURE_COORD_BUF: FloatBuffer? = null
        var PROGRAM = 0

        private const val BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8
        private var CAMERA_RENDER_BUF: RenderBuffer? = null
        private val ROATED_TEXTURE_COORDS = floatArrayOf(
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
        )
        private var ROATED_TEXTURE_COORD_BUF: FloatBuffer? = null

        fun release() {
            PROGRAM = 0
            CAMERA_RENDER_BUF = null
        }
    }

    private val START_TIME = System.currentTimeMillis()
    private var iFrame = 0

    init {
        // Setup default Buffers
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer
                .allocateDirect(SQUARE_COORDS.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(SQUARE_COORDS)
                    position(0)
                }
        }
        if (TEXTURE_COORD_BUF == null) {
            TEXTURE_COORD_BUF = ByteBuffer
                .allocateDirect(TEXTURE_COORDS.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(TEXTURE_COORDS)
                    position(0)
                }
        }
        if (ROATED_TEXTURE_COORD_BUF == null) {
            ROATED_TEXTURE_COORD_BUF = ByteBuffer
                .allocateDirect(ROATED_TEXTURE_COORDS.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(ROATED_TEXTURE_COORDS)
                    position(0)
                }
        }
        if (PROGRAM == 0) {
            PROGRAM = buildProgram(context, R.raw.vertext, R.raw.original_rtt)
        }
    }

    @CallSuper
    fun onAttach() {
        iFrame = 0
    }

    fun draw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int) {
        // TODO move?
        // Create camera render buffer
        if (CAMERA_RENDER_BUF == null || CAMERA_RENDER_BUF!!.width != canvasWidth || CAMERA_RENDER_BUF!!.height != canvasHeight) {
            CAMERA_RENDER_BUF = RenderBuffer(canvasWidth, canvasHeight, BUF_ACTIVE_TEX_UNIT)
        }

        // Use shaders
        GLES20.glUseProgram(PROGRAM)
        val iChannel0Location = GLES20.glGetUniformLocation(PROGRAM, "iChannel0")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexId)
        GLES20.glUniform1i(iChannel0Location, 0)
        val vPositionLocation = GLES20.glGetAttribLocation(PROGRAM, "vPosition")
        GLES20.glEnableVertexAttribArray(vPositionLocation)
        GLES20.glVertexAttribPointer(
            vPositionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 2,
            VERTEX_BUF
        )
        val vTexCoordLocation = GLES20.glGetAttribLocation(PROGRAM, "vTexCoord")
        GLES20.glEnableVertexAttribArray(vTexCoordLocation)
        GLES20.glVertexAttribPointer(
            vTexCoordLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 2,
            ROATED_TEXTURE_COORD_BUF
        )

        // Render to texture
        CAMERA_RENDER_BUF!!.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        CAMERA_RENDER_BUF!!.unbind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        onDraw(CAMERA_RENDER_BUF!!.texId, canvasWidth, canvasHeight)
        iFrame++
    }

    abstract fun onDraw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int)
    fun setupShaderInputs(
        program: Int,
        iResolution: IntArray,
        iChannels: IntArray,
        iChannelResolutions: Array<IntArray>
    ) {
        setupShaderInputs(
            program,
            VERTEX_BUF,
            TEXTURE_COORD_BUF,
            iResolution,
            iChannels,
            iChannelResolutions
        )
    }

    fun setupShaderInputs(
        program: Int,
        vertex: FloatBuffer?,
        textureCoord: FloatBuffer?,
        iResolution: IntArray,
        iChannels: IntArray,
        iChannelResolutions: Array<IntArray>
    ) {
        GLES20.glUseProgram(program)
        val iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution")
        GLES20.glUniform3fv(
            iResolutionLocation, 1,
            FloatBuffer.wrap(
                floatArrayOf(
                    iResolution[0].toFloat(), iResolution[1].toFloat(), 1.0f
                )
            )
        )
        val time = (System.currentTimeMillis() - START_TIME).toFloat() / 1000.0f
        val iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime")
        GLES20.glUniform1f(iGlobalTimeLocation, time)
        val iFrameLocation = GLES20.glGetUniformLocation(program, "iFrame")
        GLES20.glUniform1i(iFrameLocation, iFrame)
        val vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(vPositionLocation)
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, vertex)
        val vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord")
        GLES20.glEnableVertexAttribArray(vTexCoordLocation)
        GLES20.glVertexAttribPointer(
            vTexCoordLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 2,
            textureCoord
        )
        for (i in iChannels.indices) {
            val sTextureLocation = GLES20.glGetUniformLocation(program, "iChannel$i")
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iChannels[i])
            GLES20.glUniform1i(sTextureLocation, i)
        }
        val _iChannelResolutions = FloatArray(iChannelResolutions.size * 3)
        for (i in iChannelResolutions.indices) {
            _iChannelResolutions[i * 3] = iChannelResolutions[i][0].toFloat()
            _iChannelResolutions[i * 3 + 1] = iChannelResolutions[i][1].toFloat()
            _iChannelResolutions[i * 3 + 2] = 1.0f
        }
        val iChannelResolutionLocation = GLES20.glGetUniformLocation(program, "iChannelResolution")
        GLES20.glUniform3fv(
            iChannelResolutionLocation,
            _iChannelResolutions.size, FloatBuffer.wrap(_iChannelResolutions)
        )
    }
}