package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.textEntryKey
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

private data class CalcState(
    val display: String = "0",
    val currentInput: String = "",
    val accumulator: Double? = null,
    val pendingOperator: String? = null,
    val justEvaluated: Boolean = true
)

@Composable
fun CalculatorApp() {
    var state by remember { mutableStateOf(CalcState()) }

    fun compute(a: Double, op: String, b: Double): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "×" -> a * b
        "÷" -> a / b
        else -> b
    }

    fun format(v: Double): String {
        if (v.isNaN() || v.isInfinite()) return "Error"
        return if (v == floor(v) && kotlin.math.abs(v) < 1e15) v.toLong().toString() else v.toString()
    }

    fun digit(d: String) {
        state = if (state.justEvaluated) {
            CalcState(currentInput = d, display = d, justEvaluated = false)
        } else if (state.currentInput.isEmpty()) {
            state.copy(currentInput = d, display = d, justEvaluated = false)
        } else {
            val next = state.currentInput + d
            state.copy(currentInput = next, display = next, justEvaluated = false)
        }
    }

    fun decimal() {
        state = if (state.justEvaluated) {
            CalcState(currentInput = "0.", display = "0.", justEvaluated = false)
        } else if (state.currentInput.isEmpty()) {
            state.copy(currentInput = "0.", display = "0.", justEvaluated = false)
        } else if (!state.currentInput.contains(".")) {
            state.copy(
                currentInput = state.currentInput + ".",
                display = state.currentInput + ".",
                justEvaluated = false
            )
        } else {
            state
        }
    }

    fun operator(op: String) {
        if (state.display == "Error") return
        if (state.currentInput.isNotEmpty()) {
            val value = state.currentInput.toDouble()
            val acc = if (state.accumulator != null && state.pendingOperator != null) {
                compute(state.accumulator!!, state.pendingOperator!!, value)
            } else {
                value
            }
            state = state.copy(
                accumulator = acc,
                pendingOperator = op,
                currentInput = "",
                display = format(acc),
                justEvaluated = false
            )
        } else if (state.accumulator != null) {
            state = state.copy(pendingOperator = op)
        }
    }

    fun equals() {
        val acc = state.accumulator ?: return
        val op = state.pendingOperator ?: return
        if (state.currentInput.isEmpty()) return
        val result = compute(acc, op, state.currentInput.toDouble())
        state = CalcState(display = format(result), justEvaluated = true)
    }

    fun clear() {
        state = CalcState()
    }

    fun backspace() {
        if (state.display == "Error") {
            state = CalcState()
            return
        }
        if (state.currentInput.isEmpty()) return
        val next = state.currentInput.dropLast(1)
        state = state.copy(
            currentInput = next,
            display = if (next.isEmpty()) "0" else next
        )
    }

    fun percent() {
        if (state.currentInput.isEmpty()) return
        val v = state.currentInput.toDouble() / 100.0
        val formatted = format(v)
        state = state.copy(currentInput = formatted, display = formatted, justEvaluated = false)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Display(text = state.display, modifier = Modifier.weight(1f))
            Keypad(
                modifier = Modifier.weight(1f),
                onDigit = ::digit,
                onDecimal = ::decimal,
                onOperator = ::operator,
                onEquals = ::equals,
                onClear = ::clear,
                onBackspace = ::backspace,
                onPercent = ::percent
            )
        }
    }
}

@Composable
private fun Display(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Result: $text"
            },
        fontSize = 60.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.End,
        maxLines = 1
    )
}

@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPercent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KeypadRow {
            CalculatorButton("C", "Clear", Modifier.weight(1f), onClick = onClear)
            CalculatorButton("⌫", "Backspace", Modifier.weight(1f), onClick = onBackspace)
            CalculatorButton("%", "Percent", Modifier.weight(1f), onClick = onPercent)
            CalculatorButton("÷", "Divide", Modifier.weight(1f), operator = true) { onOperator("÷") }
        }
        KeypadRow {
            CalculatorButton("7", "", Modifier.weight(1f)) { onDigit("7") }
            CalculatorButton("8", "", Modifier.weight(1f)) { onDigit("8") }
            CalculatorButton("9", "", Modifier.weight(1f)) { onDigit("9") }
            CalculatorButton("×", "Multiply", Modifier.weight(1f), operator = true) { onOperator("×") }
        }
        KeypadRow {
            CalculatorButton("4", "", Modifier.weight(1f)) { onDigit("4") }
            CalculatorButton("5", "", Modifier.weight(1f)) { onDigit("5") }
            CalculatorButton("6", "", Modifier.weight(1f)) { onDigit("6") }
            CalculatorButton("-", "Minus", Modifier.weight(1f), operator = true) { onOperator("-") }
        }
        KeypadRow {
            CalculatorButton("1", "", Modifier.weight(1f)) { onDigit("1") }
            CalculatorButton("2", "", Modifier.weight(1f)) { onDigit("2") }
            CalculatorButton("3", "", Modifier.weight(1f)) { onDigit("3") }
            CalculatorButton("+", "Plus", Modifier.weight(1f), operator = true) { onOperator("+") }
        }
        KeypadRow {
            CalculatorButton("0", "", Modifier.weight(2f)) { onDigit("0") }
            CalculatorButton(".", "Decimal point", Modifier.weight(1f)) { onDecimal() }
            CalculatorButton("=", "Equals", Modifier.weight(1f), operator = true) { onEquals() }
        }
    }
}

@Composable
private fun KeypadRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun CalculatorButton(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    operator: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (operator) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (operator) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .height(76.dp)
            .semantics {
                this.contentDescription = contentDescription
                textEntryKey()
            },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium
        )
    }
}