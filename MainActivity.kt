package com.example.calci
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calci.ui.theme.CalciTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalciTheme {
                AppCalcu(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

val factorial = object : Function("fact", 1) {
    override fun apply(vararg args: Double): Double {
        val n = args[0].toInt()
        if (n < 0 || n != args[0].toInt()) {
            throw IllegalArgumentException("Argument for factorial must be a non-negative integer")
        }
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor) = when (text) {
        "AC", "⌫", "%" -> Color(0xFFA2BBCF) to Color.Color.Black
        "÷", "×", "-", "+", "=" -> Color.Red to Color.White
        "inv" -> if (text == "inv") Color.Red to Color.White else Color.Red to Color.Color.Black
        else -> Color(0xFF2E2E2E) to Color.Color.White
    }

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = CircleShape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AppCalcu(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var display by rememberSaveable { mutableStateOf("0") }
    var expression by rememberSaveable { mutableStateOf("") }
    var isInverse by rememberSaveable { mutableStateOf(false) }

    fun evaluateExpression(exp: String): String {
        return try {
            val sanitizedExp = exp
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt")
                .replace("π", Math.PI.toString())

            val expressionBuilder = ExpressionBuilder(sanitizedExp)
                .function(factorial)
                .build()

            val result = expressionBuilder.evaluate()
            val df = DecimalFormat("#.#######")
            df.format(result)
        } catch (e: Exception) {
            "Error"
        }
    }

    val onButtonClick: (String) -> Unit = { buttonText ->
        var currentText = buttonText
        if (isInverse) {
            currentText = when (buttonText) {
                "sin" -> "asin"
                "cos" -> "acos"
                "tan" -> "atan"
                else -> buttonText
            }
        }

        when (currentText) {
            "AC" -> {
                expression = ""
                display = "0"
            }
            "⌫" -> { // Backspace
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    display = if (expression.isEmpty()) "0" else expression
                }
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    val result = evaluateExpression(expression)
                    display = result
                    expression = if (result != "Error") result else ""
                }
            }
            "inv" -> {
                isInverse = !isInverse
            }
            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "√" -> {
                if (expression == "0" || expression == "Error") {
                    expression = "$currentText("
                } else {
                    expression += "$currentText("
                }
                display = expression
            }
            "x!" -> {
                expression += "fact("
                display = expression
            }
            "1/x" -> {
                if (expression == "0" || expression == "Error") {
                    expression = "1/"
                } else {
                    expression = "1/($expression)"
                }
                display = expression
            }
            "xʸ" -> {
                expression += "^"
                display = expression

            }
            else -> {
                if (display == "0" || expression == "Error") {
                    expression = currentText
                } else {
                    expression += currentText
                }
                display = expression
            }
        }
    }

    Box(
        modifier = modifier
            .background(color = Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                fontSize = if (display.length > 9) 48.sp else 72.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.End,
                maxLines = 2
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val buttonRows = listOf(
                    listOf("inv", "sin", "ln", "cos", "log", "tan"),
                    listOf("√", "xʸ", "x!", "(", ")", "π"),
                    listOf("AC", "⌫", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=")
                )

                buttonRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { buttonText ->
                            val weight = if (buttonText == "0") 2.1f else 1f
                            val modifier = Modifier.weight(weight).aspectRatio(if(buttonText == "0") 2f else 1f)

                            val label = when {
                                buttonText == "inv" && isInverse -> "inv"
                                buttonText == "sin" -> if (isInverse) "sin⁻¹" else "sin"
                                buttonText == "cos" -> if (isInverse) "cos⁻¹" else "cos"
                                buttonText == "tan" -> if (isInverse) "tan⁻¹" else "tan"
                                else -> buttonText
                            }

                            CalculatorButton(
                                text = label,
                                modifier = modifier,
                                onClick = { onButtonClick(buttonText) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greetings(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Row(modifier = Modifier.padding(24.dp)) {
        Column(modifier = Modifier.weight(1f)) {

        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingPreview() {
    CalciTheme {
        Greetings()
    }
}