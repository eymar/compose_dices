package org.example.skikosize

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        DiceGrid()
    }
}


val slidingAnimationDuration = 100

@Composable
fun DiceCell(
    dice: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    // Initially show the dice label.
    var displayedText by remember { mutableStateOf(WrappedTextValue(dice)) }
    var isRolling by remember { mutableStateOf(false) }
    var isResultSettled by remember { mutableStateOf(true) }

    // Parse the maximum value from the dice label (e.g. "D100" -> 100)
    val maxValue = dice.removePrefix("D").toIntOrNull() ?: 6

    val history = remember { mutableStateListOf<Int>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .combinedClickable(onLongClick = {
                if (!isRolling) history.clear()
            }, onClick = {
                if (!isRolling) {
                    isRolling = true
                }

            }),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = displayedText,
            transitionSpec = {
                // Animate the new text sliding in from the top while the old one slides out downward.
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = slidingAnimationDuration)
                ) togetherWith slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = slidingAnimationDuration)
                )
            }
        ) { targetText ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = targetText.value,
                    style = if (targetText.value == dice || !isResultSettled)
                        MaterialTheme.typography.headlineSmall
                    else
                        MaterialTheme.typography.displayLarge
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.BottomCenter) {
            val text = history.joinToString(", ") { it.toString() }
            GradientAlphaSizeText(
                text = text, baseColor = Color.Black
            )
        }
    }

    if (isRolling) {
        LaunchedEffect(isRolling) {
            isResultSettled = false
            // We'll use 9 iterations for the roll.
            val iterations = 9
            // Define the starting and ending delay (in milliseconds).
            val initialDelay = 20L
            val finalDelay = 100L

            for (i in 0 until iterations) {
                // Update the displayed text with a random number from the dice's range.
                displayedText = WrappedTextValue((1..maxValue).random().toString())
                // Calculate the delay for this iteration by linear interpolation.
                val progress = i / (iterations - 1).toFloat()  // Goes from 0f to 1f.
                val currentDelay = initialDelay + ((finalDelay - initialDelay) * progress).toLong()
                delay(currentDelay)
            }
            // Settle on a final result.
            val result = (1..maxValue).random()
            displayedText = WrappedTextValue(result.toString())
            isResultSettled = true
            delay(1500)
            history.add(result)
            if (history.size > 6) history.removeFirst()
            displayedText = WrappedTextValue(dice)
            isRolling = false
        }
    }
}


class WrappedTextValue(val value: String)

@Composable
fun GradientAlphaSizeText(
    text: String,
    baseColor: Color = Color.Red,
    baseFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = 20.sp
) {
    // Build an AnnotatedString with per-character styling if the text is long enough.
    val annotatedText: AnnotatedString = if (text.length > 3) {
        buildAnnotatedString {
            val words = text.split(" ")
            val n = words.size
            for ((i, word) in words.withIndex()) {
                // Compute progress for this word from 0 (left) to 1 (right).
                val progress = if (n > 1) i / (n - 1).toFloat() else 0f
                // Interpolate alpha: leftmost word is 0.5, rightmost is 1.0.
                val alpha = 0.5f + 0.5f * progress
                // Interpolate font size from baseFontSize to maxFontSize.
                val fontSize = lerp(TextStyle(fontSize = baseFontSize), TextStyle(fontSize = maxFontSize), progress)
                // Apply the per-word style.
                withStyle(SpanStyle(fontSize = fontSize.fontSize, color = baseColor.copy(alpha = alpha))) {
                    append(word)
                }
                // Append a space between words, except after the last word.
                if (i != n - 1) append(" ")
            }
        }
    } else {
        // If the text is short, use a single uniform style.
        buildAnnotatedString {
            append(text)
        }
    }

    BasicText(text = annotatedText)
}

/**
 * Displays a grid of dice cells with 2 columns and 4 rows.
 * Each cell occupies equal space and displays one dice from the list with its unique color.
 */
@Composable
fun DiceGrid(modifier: Modifier = Modifier) {
    // List of dice types to display
    val diceTypes = listOf("D100", "D10", "D12", "D8", "D20", "D6", "D4", "D2")

    // Define a palette of 8 nice background colors (one for each dice)
    val diceColors = listOf(
        Color(0xFFEF5350), // Vibrant Red for D100
        Color(0xFF66BB6A), // Fresh Green for D10
        Color(0xFF42A5F5), // Cool Blue for D12
        Color(0xFFFFCA28), // Warm Amber for D8
        Color(0xFFAB47BC), // Soft Purple for D20
        Color(0xFFFF7043), // Bright Orange for D6
        Color(0xFF26A69A), // Calming Teal for D4
        Color(0xFF8D6E63)  // Earthy Brown for D2
    )

    // Outer Column fills available space
    Column(modifier = modifier.fillMaxSize()) {
        // There are 4 rows in total
        repeat(4) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // Each row gets equal vertical space
            ) {
                // Each row has 2 columns
                repeat(2) { columnIndex ->
                    // Calculate the corresponding dice index
                    val diceIndex = rowIndex * 2 + columnIndex
                    DiceCell(
                        dice = diceTypes[diceIndex],
                        backgroundColor = diceColors[diceIndex],
                        modifier = Modifier
                            .weight(1f)      // Each cell takes equal horizontal space
                            .fillMaxHeight() // Fills the height of the row
                    )
                }
            }
        }
    }
}
