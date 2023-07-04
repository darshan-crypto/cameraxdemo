package com.example.cameraguide.ui

import android.media.MediaFormat
import com.example.cameraguide.ui.Mp4FrameMuxer
import com.example.cameraguide.ui.FrameBuilder
import java.io.File



data class MuxerConfig(
        var file: File,
        var videoWidth: Int = 320,
        var videoHeight: Int = 240,
        var mimeType: String = MediaFormat.MIMETYPE_VIDEO_AVC,
        var framesPerImage: Int = 1,
        var framesPerSecond: Float = 10F,
        var bitrate: Int = 1500000,
        var frameMuxer: FrameMuxer = Mp4FrameMuxer(file.absolutePath, framesPerSecond),
        var iFrameInterval: Int = 10
)

interface MuxingCompletionListener {
    fun onVideoSuccessful(file: File)
    fun onVideoError(error: Throwable)
}

interface MuxingResult

data class MuxingSuccess(
        val file: File
): MuxingResult

data class MuxingError(
        val message: String,
        val exception: Exception
): MuxingResult
