package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.SoundEffectConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SoundManager {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = -1
    private var successSoundId: Int = -1
    private var cashSoundId: Int = -1
    private var deleteSoundId: Int = -1
    private var isInitialized = false

    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            // Generate synthetic audio clips for immediate crisp response without external asset dependencies
            CoroutineScope(Dispatchers.IO).launch {
                loadAudioClips(context)
            }
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAudioClips(context: Context) {
        try {
            // Write short synthesized PCM wav files into cache directory
            val cacheDir = context.cacheDir

            val clickFile = java.io.File(cacheDir, "snd_click.wav")
            if (!clickFile.exists()) {
                clickFile.writeBytes(generateToneWav(frequency = 1200.0, durationMs = 45, type = "sine_decay"))
            }

            val successFile = java.io.File(cacheDir, "snd_success.wav")
            if (!successFile.exists()) {
                successFile.writeBytes(generateChimeWav())
            }

            val cashFile = java.io.File(cacheDir, "snd_cash.wav")
            if (!cashFile.exists()) {
                cashFile.writeBytes(generateCashRegisterWav())
            }

            val deleteFile = java.io.File(cacheDir, "snd_delete.wav")
            if (!deleteFile.exists()) {
                deleteFile.writeBytes(generateToneWav(frequency = 320.0, durationMs = 90, type = "low_pop"))
            }

            soundPool?.let { sp ->
                clickSoundId = sp.load(clickFile.absolutePath, 1)
                successSoundId = sp.load(successFile.absolutePath, 1)
                cashSoundId = sp.load(cashFile.absolutePath, 1)
                deleteSoundId = sp.load(deleteFile.absolutePath, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick(view: View? = null, context: Context? = null) {
        if (!isSoundEnabled && !isHapticEnabled) return

        if (isSoundEnabled) {
            if (soundPool != null && clickSoundId != -1) {
                soundPool?.play(clickSoundId, 0.75f, 0.75f, 1, 0, 1.0f)
            } else {
                view?.playSoundEffect(SoundEffectConstants.CLICK)
            }
        }

        if (isHapticEnabled && context != null) {
            triggerVibration(context, 12, 100)
        }
    }

    fun playSuccess(context: Context? = null) {
        if (!isSoundEnabled && !isHapticEnabled) return

        if (isSoundEnabled && soundPool != null && successSoundId != -1) {
            soundPool?.play(successSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
        }

        if (isHapticEnabled && context != null) {
            triggerVibration(context, 35, 200)
        }
    }

    fun playCash(context: Context? = null) {
        if (!isSoundEnabled && !isHapticEnabled) return

        if (isSoundEnabled && soundPool != null && cashSoundId != -1) {
            soundPool?.play(cashSoundId, 0.95f, 0.95f, 1, 0, 1.0f)
        }

        if (isHapticEnabled && context != null) {
            triggerDoubleVibration(context)
        }
    }

    fun playDelete(context: Context? = null) {
        if (!isSoundEnabled && !isHapticEnabled) return

        if (isSoundEnabled && soundPool != null && deleteSoundId != -1) {
            soundPool?.play(deleteSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }

        if (isHapticEnabled && context != null) {
            triggerVibration(context, 25, 140)
        }
    }

    private fun triggerVibration(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }

    private fun triggerDoubleVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 25), intArrayOf(0, 180, 0, 220), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(50)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Generate valid 16-bit PCM WAV bytes for rich UI sounds
    private fun generateToneWav(frequency: Double, durationMs: Int, type: String): ByteArray {
        val sampleRate = 22050
        val totalSamples = (sampleRate * durationMs / 1000.0).toInt()
        val pcmData = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            val envelope = when (type) {
                "sine_decay" -> Math.exp(-progress * 9.0) // sharp snappy tap
                "low_pop" -> Math.exp(-progress * 6.0)
                else -> 1.0 - progress
            }
            val sample = Math.sin(2.0 * Math.PI * frequency * t) * envelope
            pcmData[i] = (sample * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return createWavBytes(pcmData, sampleRate)
    }

    private fun generateChimeWav(): ByteArray {
        val sampleRate = 22050
        val durationMs = 350
        val totalSamples = (sampleRate * durationMs / 1000.0).toInt()
        val pcmData = ShortArray(totalSamples)

        val f1 = 659.25 // E5
        val f2 = 880.00 // A5
        val f3 = 1318.51 // E6

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val note1 = Math.sin(2.0 * Math.PI * f1 * t) * Math.exp(-t * 8.0)
            val note2 = if (t > 0.08) Math.sin(2.0 * Math.PI * f2 * (t - 0.08)) * Math.exp(-(t - 0.08) * 8.0) else 0.0
            val note3 = if (t > 0.16) Math.sin(2.0 * Math.PI * f3 * (t - 0.16)) * Math.exp(-(t - 0.16) * 6.0) else 0.0

            val mixed = (note1 * 0.35 + note2 * 0.4 + note3 * 0.5)
            pcmData[i] = (mixed * Short.MAX_VALUE * 0.8).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return createWavBytes(pcmData, sampleRate)
    }

    private fun generateCashRegisterWav(): ByteArray {
        val sampleRate = 22050
        val durationMs = 280
        val totalSamples = (sampleRate * durationMs / 1000.0).toInt()
        val pcmData = ShortArray(totalSamples)

        val f1 = 987.77 // B5 (bright coin sound)
        val f2 = 1567.98 // G6 (shimmer)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val coin1 = Math.sin(2.0 * Math.PI * f1 * t) * Math.exp(-t * 11.0)
            val coin2 = if (t > 0.06) Math.sin(2.0 * Math.PI * f2 * (t - 0.06)) * Math.exp(-(t - 0.06) * 7.0) else 0.0

            val mixed = coin1 * 0.45 + coin2 * 0.55
            pcmData[i] = (mixed * Short.MAX_VALUE * 0.85).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return createWavBytes(pcmData, sampleRate)
    }

    private fun createWavBytes(pcm: ShortArray, sampleRate: Int): ByteArray {
        val byteData = ByteArray(pcm.size * 2)
        var idx = 0
        for (sample in pcm) {
            byteData[idx++] = (sample.toInt() and 0x00FF).toByte()
            byteData[idx++] = ((sample.toInt() shr 8) and 0x00FF).toByte()
        }

        val totalDataLen = byteData.size + 36
        val header = ByteArray(44)
        val channels = 1
        val byteRate = sampleRate * channels * 2

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte()
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (byteData.size and 0xff).toByte()
        header[41] = ((byteData.size shr 8) and 0xff).toByte()
        header[42] = ((byteData.size shr 16) and 0xff).toByte()
        header[43] = ((byteData.size shr 24) and 0xff).toByte()

        return header + byteData
    }
}
