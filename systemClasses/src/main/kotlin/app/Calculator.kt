package app

import Button
import Column
import Activity
import GraphicServiceI
import StorageServiceI
import DeviceManagerI
import View
import Row
import Text
import background
import fillMaxSize
import height
import onClick
import padding
import paddingBottom
import width
import java.awt.Color

class CalculatorApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {

    private var display = "0"
    private var firstNumber = 0.0
    private var operation: String? = null
    private var waitingForSecond = false

    override fun main() {
        gs.setContent(itIsNewScreen = true) { buildUI() }
        gs.redraw()
    }

    private fun render() {
        gs.setContent(itIsNewScreen = false) { buildUI() }
        gs.redraw()
    }

    private fun MutableList<View>.buildUI() {
        Column(modifier = Modifier.fillMaxSize().background(Color(30, 30, 30)), this).layout {

            Column(
                modifier = Modifier.width(640).height(200)
                    .background(Color(40, 40, 40))
                    ,
                this
            ).layout {
                Text(
                    modifier = Modifier.width(600).height(80),
                    text = display,
                    textSize = 48,
                    parent = this
                )
            }

            Row(modifier = Modifier.width(640).height(140), this).layout {
                calcButton("C", 160, Color(165, 165, 165), Color.BLACK) { onClear() }
                calcButton("+/-", 160, Color(165, 165, 165), Color.BLACK) { onNegate() }
                calcButton("%", 160, Color(165, 165, 165), Color.BLACK) { onPercent() }
                calcButton("/", 160, Color(255, 159, 10), Color.WHITE) { onOperation("/") }
            }

            Row(modifier = Modifier.width(640).height(140), this).layout {
                calcButton("7", 160, Color(51, 51, 51), Color.WHITE) { onDigit("7") }
                calcButton("8", 160, Color(51, 51, 51), Color.WHITE) { onDigit("8") }
                calcButton("9", 160, Color(51, 51, 51), Color.WHITE) { onDigit("9") }
                calcButton("*", 160, Color(255, 159, 10), Color.WHITE) { onOperation("*") }
            }

            Row(modifier = Modifier.width(640).height(140), this).layout {
                calcButton("4", 160, Color(51, 51, 51), Color.WHITE) { onDigit("4") }
                calcButton("5", 160, Color(51, 51, 51), Color.WHITE) { onDigit("5") }
                calcButton("6", 160, Color(51, 51, 51), Color.WHITE) { onDigit("6") }
                calcButton("-", 160, Color(255, 159, 10), Color.WHITE) { onOperation("-") }
            }

            Row(modifier = Modifier.width(640).height(140), this).layout {
                calcButton("1", 160, Color(51, 51, 51), Color.WHITE) { onDigit("1") }
                calcButton("2", 160, Color(51, 51, 51), Color.WHITE) { onDigit("2") }
                calcButton("3", 160, Color(51, 51, 51), Color.WHITE) { onDigit("3") }
                calcButton("+", 160, Color(255, 159, 10), Color.WHITE) { onOperation("+") }
            }

            Row(modifier = Modifier.width(640).height(140).paddingBottom(60), this).layout {
                calcButton("0", 320, Color(51, 51, 51), Color.WHITE) { onDigit("0") }
                calcButton(".", 160, Color(51, 51, 51), Color.WHITE) { onDot() }
                calcButton("=", 160, Color(255, 159, 10), Color.WHITE) { onEquals() }
            }
        }
    }

    private fun MutableList<View>.calcButton(
        label: String,
        w: Int,
        bg: Color,
        textColor: Color,
        action: () -> Unit
    ) {
        Button(
            modifier = Modifier.width(w).height(140).background(bg).padding(2).onClick {
                action()
            },
            this
        ).layout {
            Text(
                modifier = Modifier.width(w - 4).height(136).background(textColor),
                text = label,
                textSize = 32,
                parent = this
            )
        }
    }

    private fun onDigit(digit: String) {
        if (waitingForSecond) {
            display = digit
            waitingForSecond = false
        } else {
            display = if (display == "0") digit else display + digit
        }
        render()
    }

    private fun onDot() {
        if (!display.contains(".")) display += "."
        render()
    }

    private fun onOperation(op: String) {
        firstNumber = display.toDoubleOrNull() ?: 0.0
        operation = op
        waitingForSecond = true
        render()
    }

    private fun onEquals() {
        val second = display.toDoubleOrNull() ?: 0.0

        val result = when (operation) {
            "+" -> firstNumber + second
            "-" -> firstNumber - second
            "*" -> firstNumber * second
            "/" -> if (second != 0.0) firstNumber / second else Double.NaN
            else -> second
        }
        display = formatResult(result)
        operation = null
        waitingForSecond = true
        render()
    }

    private fun onClear() {
        display = "0"
        firstNumber = 0.0
        operation = null
        waitingForSecond = false
        render()
    }

    private fun onNegate() {
        val value = display.toDoubleOrNull() ?: 0.0
        display = formatResult(-value)
        render()
    }

    private fun onPercent() {
        val value = display.toDoubleOrNull() ?: 0.0
        display = formatResult(value / 100.0)
        render()
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}

