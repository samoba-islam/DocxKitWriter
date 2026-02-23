package com.samoba.docxkit_writer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.samoba.docxkit_writer.ui.ComposerScreen
import com.samoba.docxkit_writer.ui.theme.DocxkitwriterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocxkitwriterTheme {
                ComposerScreen()
            }
        }
    }
}