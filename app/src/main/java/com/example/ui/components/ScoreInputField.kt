package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentRecord

@Composable
fun ScoreInputField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier,
    maxScore: Float = 100f,
    testTag: String = "score_input_field",
    onNext: (() -> Unit)? = null
) {
    var rawText by remember(value) {
        mutableStateOf(if (value != null) StudentRecord.formatScore(value) else "")
    }
    var isError by remember { mutableStateOf(false) }

    fun normalizeArabicDigits(input: String): String {
        return input.map { char ->
            when (char) {
                '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                '٫', '،' -> '.'
                else -> char
            }
        }.joinToString("")
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .background(
                color = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = rawText,
            onValueChange = { newText ->
                val normalized = normalizeArabicDigits(newText.trim())
                // Only allow digits and one decimal dot
                val regex = Regex("^\\d*(\\.\\d{0,1})?$")
                if (normalized.isEmpty()) {
                    rawText = ""
                    isError = false
                    onValueChange(null)
                } else if (regex.matches(normalized)) {
                    val parsed = normalized.toFloatOrNull()
                    if (parsed != null) {
                        if (parsed in 0f..maxScore) {
                            rawText = normalized
                            isError = false
                            onValueChange(parsed)
                        } else {
                            // Exceeded maximum
                            isError = true
                        }
                    } else if (normalized == ".") {
                        rawText = "0."
                        isError = false
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (onNext != null) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            decorationBox = { innerTextField ->
                if (rawText.isEmpty()) {
                    Text(
                        text = "-",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}
