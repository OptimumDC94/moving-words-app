package com.personal.movingwords

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        val pad = (20 * resources.displayMetrics.density).toInt()
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val title = TextView(this).apply {
            text = "My Moving Words"
            textSize = 22f
            setPadding(0, 0, 0, pad / 2)
        }

        val subtitle = TextView(this).apply {
            text = "Your words (one per line):"
            setPadding(0, 0, 0, pad / 3)
        }

        val editWords = EditText(this).apply {
            setText(WordStorage.loadWords(this@MainActivity).joinToString("\n"))
            minLines = 6
            gravity = Gravity.TOP or Gravity.START
        }

        val btnSave = Button(this).apply {
            text = "Save Words"
            setOnClickListener {
                WordStorage.saveWords(this@MainActivity, editWords.text.toString())
                Toast.makeText(this@MainActivity, "Words saved!", Toast.LENGTH_SHORT).show()
            }
        }

        val btnStartFloating = Button(this).apply {
            text = "Start Floating Over All Apps"
            setOnClickListener {
                WordStorage.saveWords(this@MainActivity, editWords.text.toString())

                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(
                        this@MainActivity,
                        "Enable 'Display over other apps' for Moving Words",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    startForegroundService(
                        Intent(this@MainActivity, FloatingWordsService::class.java)
                    )
                }
            }
        }

        val btnStopFloating = Button(this).apply {
            text = "Stop Floating"
            setOnClickListener {
                stopService(Intent(this@MainActivity, FloatingWordsService::class.java))
            }
        }

        val btnPreview = Button(this).apply {
            text = "Launch Black-Screen Preview"
            setOnClickListener {
                WordStorage.saveWords(this@MainActivity, editWords.text.toString())
                startActivity(Intent(this@MainActivity, PreviewActivity::class.java))
            }
        }

        val btnSystemSettings = Button(this).apply {
            text = "Set as Phone Screen Saver"
            setOnClickListener {
                WordStorage.saveWords(this@MainActivity, editWords.text.toString())
                try {
                    startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                } catch (_: Exception) {
                    startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                }
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(editWords)
        layout.addView(btnSave)
        layout.addView(btnStartFloating)
        layout.addView(btnStopFloating)
        layout.addView(btnPreview)
        layout.addView(btnSystemSettings)

        scroll.addView(layout)
        setContentView(scroll)
    }
}