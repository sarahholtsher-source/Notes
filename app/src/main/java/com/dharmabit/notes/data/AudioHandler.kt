package com.dharmabit.notes.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioHandler(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    fun startRecording(): String? {
        return try {
            val file = File(context.cacheDir, "audio_note_${System.currentTimeMillis()}.3gp")
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    fun play(path: String, onCompletion: () -> Unit) {
        stopPlayback()
        player = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    stopPlayback()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                stopPlayback()
            }
        }
    }

    fun stopPlayback() {
        player?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        player = null
    }
}