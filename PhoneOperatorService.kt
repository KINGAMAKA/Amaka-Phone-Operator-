package com.amaka.phoneoperator

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.speech.tts.TextToSpeech
import java.util.Locale

class PhoneOperatorService : AccessibilityService() {
    companion object { @Volatile var instance: PhoneOperatorService? = null }
    private var speaker: TextToSpeech? = null
    private var lastCommandAt = 0L

    override fun onServiceConnected() {
        instance = this
        speaker = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) speaker?.language = Locale.getDefault() }
        announce("Amaka phone control is ready.")
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Screen changes are observed by Android; commands are explicit. */ }
    override fun onInterrupt() { announce("Phone control interrupted.") }
    override fun onDestroy() { instance = null; speaker?.shutdown(); super.onDestroy() }

    fun announce(message: String) { speaker?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "amaka_operator") }

    fun executeCommand(raw: String) {
        val now = System.currentTimeMillis()
        if (now - lastCommandAt < 350) return
        lastCommandAt = now
        val command = raw.trim()
        val lower = command.lowercase(Locale.ROOT)
        when {
            lower == "back" || lower == "go back" || lower == "press back" -> {
                performGlobalAction(GLOBAL_ACTION_BACK); announce("Going back.")
            }
            lower == "home" || lower == "go home" -> {
                performGlobalAction(GLOBAL_ACTION_HOME); announce("Going home.")
            }
            lower.contains("scroll down") -> { scroll(true); announce("Scrolling down.") }
            lower.contains("scroll up") -> { scroll(false); announce("Scrolling up.") }
            lower.startsWith("tap ") || lower.startsWith("click ") || lower.startsWith("press ") -> {
                val target = command.substringAfter(" ").trim()
                val found = findAndClick(rootInActiveWindow, target)
                announce(if (found) "Tapped $target." else "I couldn't find $target on this screen.")
            }
            lower.startsWith("type ") -> {
                val text = command.substringAfter("type ").trim()
                val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
                val ok = focused?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args) == true
                announce(if (ok) "Text entered." else "I couldn't find an active text field.")
            }
            else -> announce("I don't know that action yet. Try open an app, back, home, scroll down, tap a button, or type text.")
        }
    }

    private fun findAndClick(node: AccessibilityNodeInfo?, target: String): Boolean {
        if (node == null) return false
        val label = listOfNotNull(node.text?.toString(), node.contentDescription?.toString()).joinToString(" ")
        if (label.contains(target, true) && node.isEnabled && node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        for (i in 0 until node.childCount) if (findAndClick(node.getChild(i), target)) return true
        return false
    }

    private fun scroll(down: Boolean) {
        val root = rootInActiveWindow ?: return
        if (scrollNode(root, down)) return
        val metrics = resources.displayMetrics
        val x = metrics.widthPixels / 2f
        val startY = if (down) metrics.heightPixels * .78f else metrics.heightPixels * .28f
        val endY = if (down) metrics.heightPixels * .28f else metrics.heightPixels * .78f
        val path = Path().apply { moveTo(x, startY); lineTo(x, endY) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 400)).build()
        dispatchGesture(gesture, null, null)
    }

    private fun scrollNode(node: AccessibilityNodeInfo, down: Boolean): Boolean {
        if (node.isScrollable) {
            val action = if (down) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            if (node.performAction(action)) return true
        }
        for (i in 0 until node.childCount) if (scrollNode(node.getChild(i), down)) return true
        return false
    }
}
