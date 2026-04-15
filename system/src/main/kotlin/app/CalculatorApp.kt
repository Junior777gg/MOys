package app

import Button
import Column
import Activity
import service.GraphicService
import service.StorageService
import service.DeviceManager
import View
import Row
import Text
import common.Color
import modifier.Modifier
import modifier.background
import modifier.cornerRadius
import modifier.fillMaxSize
import modifier.height
import modifier.onClick
import modifier.padding
import modifier.paddingBottom
import modifier.width

class CalculatorApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager
) : Activity {

    private var display = "0"
    private var firstNumber = 0.0
    private var operation: String? = null
    private var waitingForSecond = false

    private val OPERATOR_BUTTON_COLOR = Color(255, 159, 10)
    private val CONTEXT_BUTTON_COLOR = Color(165)
    private val NUMBER_BUTTON_COLOR = Color(51)

    override fun main() {
        gs.setContent(itIsNewScreen = true) { buildUI() }
        gs.redraw()
    }

    private fun render() {
        gs.setContent(itIsNewScreen = false) { buildUI() }
        gs.redraw()
    }

    private fun MutableList<View>.buildUI() {
        Column(modifier = Modifier.fillMaxSize().background(Color(30)).paddingBottom(60), this).layout {
            Row(modifier = Modifier.width(640).height(200).background(Color(40)), this).layout {
                Text(
                    modifier = Modifier.width(600).height(80),
                    text = display,
                    textSize = 48,
                    parent = this
                )
            }

            Row(modifier = Modifier.width(640).height(140).background(Color.TRANSPARENT), this).layout {
                calcButton("C", 160, CONTEXT_BUTTON_COLOR, Color.BLACK) { onClear() }
                calcButton("+/-", 160, CONTEXT_BUTTON_COLOR, Color.BLACK) { onNegate() }
                calcButton("%", 160, CONTEXT_BUTTON_COLOR, Color.BLACK) { onPercent() }
                calcButton("/", 160, OPERATOR_BUTTON_COLOR, Color.WHITE) { onOperation("/") }
            }

            Row(modifier = Modifier.width(640).height(140).background(Color.TRANSPARENT), this).layout {
                calcButton("7", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("7") }
                calcButton("8", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("8") }
                calcButton("9", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("9") }
                calcButton("*", 160, OPERATOR_BUTTON_COLOR, Color.WHITE) { onOperation("*") }
            }

            Row(modifier = Modifier.width(640).height(140).background(Color.TRANSPARENT), this).layout {
                calcButton("4", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("4") }
                calcButton("5", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("5") }
                calcButton("6", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("6") }
                calcButton("-", 160, OPERATOR_BUTTON_COLOR, Color.WHITE) { onOperation("-") }
            }

            Row(modifier = Modifier.width(640).height(140).background(Color.TRANSPARENT), this).layout {
                calcButton("1", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("1") }
                calcButton("2", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("2") }
                calcButton("3", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("3") }
                calcButton("+", 160, OPERATOR_BUTTON_COLOR, Color.WHITE) { onOperation("+") }
            }

            Row(modifier = Modifier.width(640).height(140).background(Color.TRANSPARENT), this).layout {
                calcButton("0", 320, NUMBER_BUTTON_COLOR, Color.WHITE) { onDigit("0") }
                calcButton(".", 160, NUMBER_BUTTON_COLOR, Color.WHITE) { onDot() }
                calcButton("=", 160, OPERATOR_BUTTON_COLOR, Color.WHITE) { onEquals() }
            }
        }
    }

    private fun MutableList<View>.calcButton(
        label: String,
        w: Int,
        bg: Color,
        textColor: Color,
        action: () -> Unit,
    ) {
        Button(
            modifier = Modifier.width(w).cornerRadius(20).height(140).background(bg).padding(2).onClick {
                action()
            },
            parent = this
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

