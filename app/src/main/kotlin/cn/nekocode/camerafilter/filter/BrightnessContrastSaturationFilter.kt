/*
 * Copyright 2016 winston
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

class BrightnessContrastSaturationFilter(context: Context) : CameraFilter(context) {

    companion object {
        private const val BRIGHTNESS_DEFAULT = 0.15f
        private const val CONTRAST_DEFAULT = 1.2f
        private const val SATURATION_DEFAULT = 1.5f
    }

    var brightness: Float = BRIGHTNESS_DEFAULT
    var contrast: Float = CONTRAST_DEFAULT
    var saturation: Float = SATURATION_DEFAULT

    private val program: Int = buildProgram(
        context, R.raw.vertext, R.raw.brightness_contrast_saturation
    )

    override fun onDraw(cameraTexId: Int, canvasWidth: Int, canvasHeight: Int) {
        setupShaderInputs(
            program,
            intArrayOf(canvasWidth, canvasHeight),
            intArrayOf(cameraTexId),
            arrayOf()
        ) { program ->
            onParamChanges(program, "brightness", brightness)
            onParamChanges(program, "contrast", contrast)
            onParamChanges(program, "saturation", saturation)
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun onParamChanges(program: Int, key: String, value: Float) {
        val keyLocation = GLES20.glGetUniformLocation(program, key)
        GLES20.glUniform1f(keyLocation, value)
    }
}
