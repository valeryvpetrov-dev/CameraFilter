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
import cn.nekocode.camerafilter.MyGLUtils.loadTexture
import cn.nekocode.camerafilter.R

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class RefractionFilter(context: Context) : CameraFilter(context) {
    private val program: Int = buildProgram(context, R.raw.vertext, R.raw.refraction)
    private val texture2Id: Int = loadTexture(context, R.raw.tex11, IntArray(2))
    override fun onDraw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int) {
        setupShaderInputs(
            program,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(cameraTexId, texture2Id),
            arrayOf()
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}