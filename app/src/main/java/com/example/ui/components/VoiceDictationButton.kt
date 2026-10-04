package com.example.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag

@Composable
fun VoiceDictationButton(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "voice_dictation_button"
) {
    val context = LocalContext.current
    var showUnsupportedDialog by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onResult(spokenText.trim())
            }
        }
    }

    IconButton(
        onClick = {
            if (!enabled) {
                Toast.makeText(context, "تم إيقاف الميكروفون من الإعدادات", Toast.LENGTH_SHORT).show()
                return@IconButton
            }
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "ar")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن باللغة العربية...")
                }
                speechLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                showUnsupportedDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر تشغيل الميكروفون: ${e.message}", Toast.LENGTH_LONG).show()
            }
        },
        enabled = enabled,
        modifier = modifier.testTag(testTag)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "إملاء صوتي",
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }

    if (showUnsupportedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsupportedDialog = false },
            title = { Text("الميكروفون والإملاء الصوتي") },
            text = {
                Text(
                    "يبدو أن جهازك أو المتصفح الحالي لا يحتوي على خدمة التعرف على الصوت من Google. يرجى التأكد من تثبيت خدمات Google الصوتية أو استخدام الإدخال اليدوي."
                )
            },
            confirmButton = {
                TextButton(onClick = { showUnsupportedDialog = false }) {
                    Text("حسناً")
                }
            }
        )
    }
}
