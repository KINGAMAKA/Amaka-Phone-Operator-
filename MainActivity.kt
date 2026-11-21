package com.amaka.phoneoperator

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.provider.Settings
import android.widget.*
import android.view.ViewGroup
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var input: EditText
    private lateinit var status: TextView
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFF101014.toInt())
        }
        fun label(text: String, size: Float = 16f) = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(0xFFF4EBDD.toInt())
        }
        layout.addView(label("AMAKA AI", 27f))
        layout.addView(label("PHONE OPERATOR  •  EARLY ACCESS", 12f))
        layout.addView(Space(this).apply { minimumHeight = 24 })
        layout.addView(label("Give Amaka a task"))
        input = EditText(this).apply {
            hint = "Try: open YouTube, go back, scroll down…"
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFAAAAAA.toInt())
            setPadding(16, 14, 16, 14)
            setBackgroundColor(0xFF292930.toInt())
        }
        layout.addView(input, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        val speak = Button(this).apply { text = "🎙  Speak command"; setOnClickListener { startVoiceInput() } }
        layout.addView(speak)
        val run = Button(this).apply {
            text = "Run command"
            setOnClickListener { submitCommand(input.text.toString()) }
        }
        layout.addView(run)
        val enable = Button(this).apply {
            text = "Enable phone-control permission"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        layout.addView(enable)
        status = label("Status: Waiting for your instruction")
        status.setPadding(0, 24, 0, 12)
        layout.addView(status)
        layout.addView(label(
            "FIRST VERSION\n" +
            "• Voice and typed commands\n" +
            "• Open installed apps by name\n" +
            "• Back, Home, scroll and tap visible text\n" +
            "• Runs only after you enable Accessibility\n\n" +
            "Safety: Amaka does not enter passwords, approve payments, send messages, or publish content automatically. Review actions before sensitive steps.",
            14f
        ))
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell Amaka what to do")
        }
        try { startActivityForResult(intent, 1001) }
        catch (_: Exception) { status.text = "Voice recognition is unavailable on this phone." }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            input.setText(spoken)
            submitCommand(spoken)
        }
    }

    private fun submitCommand(raw: String) {
        val command = raw.trim()
        if (command.isBlank()) { status.text = "Please enter a command."; return }
        val prefs = getSharedPreferences("amaka_operator", Context.MODE_PRIVATE)
        prefs.edit().putString("pending_command", command).putLong("command_time", System.currentTimeMillis()).apply()
        val lower = command.lowercase(Locale.ROOT)
        val service = PhoneOperatorService.instance
        if (lower.startsWith("open ")) {
            val appName = command.substringAfter("open ", "").trim()
            val launch = findLaunchIntentForApp(appName)
            if (launch != null) {
                status.text = "Opening $appName…"
                startActivity(launch)
                service?.announce("Opening $appName")
            } else {
                status.text = "I couldn't find an installed app named '$appName'."
                speak("I couldn't find that app.")
            }
        } else if (service == null) {
            status.text = "Enable Amaka Phone Operator in Android Accessibility settings first."
            speak("Please enable phone control permission first.")
        } else {
            service.executeCommand(command)
            status.text = "Command sent: $command"
        }
    }

    private fun findLaunchIntentForApp(name: String): Intent? {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val match = apps.firstOrNull {
            val label = pm.getApplicationLabel(it).toString()
            label.equals(name, true) || label.contains(name, true)
        } ?: return null
        return pm.getLaunchIntentForPackage(match.packageName)
    }

    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "amaka_status") }
    override fun onInit(statusCode: Int) { if (statusCode == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() }
    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}
