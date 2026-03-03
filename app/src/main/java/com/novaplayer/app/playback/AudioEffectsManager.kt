@file:Suppress("DEPRECATION")

package com.novaplayer.app.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer

class AudioEffectsManager {

    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null

    private var audioSessionId: Int = 0
    private var initialized = false

    fun init(sessionId: Int) {
        if (sessionId == 0) return
        if (initialized && sessionId == audioSessionId) return

        release()
        audioSessionId = sessionId

        try {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        } catch (_: Exception) {}

        try {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
        } catch (_: Exception) {}

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = true }
        } catch (_: Exception) {}

        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
        } catch (_: Exception) {}

        initialized = true
    }

    fun isInitialized(): Boolean = initialized

    // Bass Boost: 0-1000
    fun setBassBoost(strength: Int) {
        try {
            bassBoost?.setStrength(strength.toShort().coerceIn(0, 1000))
        } catch (_: Exception) {}
    }

    fun getBassBoostStrength(): Int = try {
        bassBoost?.roundedStrength?.toInt() ?: 0
    } catch (_: Exception) { 0 }

    // Virtualizer (3D Audio): 0-1000
    fun setVirtualizer(strength: Int) {
        try {
            virtualizer?.setStrength(strength.toShort().coerceIn(0, 1000))
        } catch (_: Exception) {}
    }

    fun getVirtualizerStrength(): Int = try {
        virtualizer?.roundedStrength?.toInt() ?: 0
    } catch (_: Exception) { 0 }

    // Loudness Enhancer: gain in millibels (0-1000 mapped to 0-1000 mB)
    fun setLoudnessGain(gainMb: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMb)
        } catch (_: Exception) {}
    }

    fun getLoudnessGain(): Int = try {
        loudnessEnhancer?.targetGain?.toInt() ?: 0
    } catch (_: Exception) { 0 }

    // Equalizer access
    fun getEqualizer(): Equalizer? = equalizer

    fun getEqualizerBandCount(): Int = try {
        equalizer?.numberOfBands?.toInt() ?: 0
    } catch (_: Exception) { 0 }

    fun getEqualizerBandLevelRange(): Pair<Int, Int> = try {
        val range = equalizer?.bandLevelRange ?: shortArrayOf(0, 0)
        (range[0].toInt() / 100) to (range[1].toInt() / 100)
    } catch (_: Exception) { 0 to 0 }

    fun getEqualizerBandLevel(band: Int): Float = try {
        (equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0) / 100f
    } catch (_: Exception) { 0f }

    fun setEqualizerBandLevel(band: Int, levelDb: Float) {
        try {
            equalizer?.setBandLevel(band.toShort(), (levelDb * 100).toInt().toShort())
        } catch (_: Exception) {}
    }

    fun getEqualizerCenterFreq(band: Int): Int = try {
        (equalizer?.getCenterFreq(band.toShort())?.toInt() ?: 0) / 1000
    } catch (_: Exception) { 0 }

    fun getEqualizerPresetNames(): List<String> = try {
        val count = equalizer?.numberOfPresets?.toInt() ?: 0
        (0 until count).map { equalizer?.getPresetName(it.toShort()) ?: "" }
    } catch (_: Exception) { emptyList() }

    fun applyEqualizerPreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
        } catch (_: Exception) {}
    }

    fun release() {
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        try { equalizer?.release() } catch (_: Exception) {}
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        equalizer = null
        initialized = false
    }
}
