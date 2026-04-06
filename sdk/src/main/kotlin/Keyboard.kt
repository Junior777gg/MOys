import java.awt.Color

class Keyboard(
    val gs: GraphicServiceI,
    val contract: KeyboardInterface
) {

    fun main() {
        gs.injectUI {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .paddingBottom(60)
                    .background(Color(0, 0, 0, 0)),
                verticalAlignment = VerticalAlignment.Bottom(),
                parent = this
            ).layout {
                Column(
                    modifier = Modifier.height(260).width(640).background(Color(200, 200, 200, 255)),
                    this@layout
                ).layout {

                    // Ряд 1: Й Ц У К Е Н Г Ш Щ З Х
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key("Й", 58)
                        key("Ц", 58)
                        key("У", 58)
                        key("К", 58)
                        key("Е", 58)
                        key("Н", 58)
                        key("Г", 58)
                        key("Ш", 58)
                        key("Щ", 58)
                        key("З", 58)
                        key("Х", 58)
                    }

                    // Ряд 2: Ф Ы В А П Р О Л Д Ж Э
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key("Ф", 58)
                        key("Ы", 58)
                        key("В", 58)
                        key("А", 58)
                        key("П", 58)
                        key("Р", 58)
                        key("О", 58)
                        key("Л", 58)
                        key("Д", 58)
                        key("Ж", 58)
                        key("Э", 58)
                    }

                    // Ряд 3: Shift Я Ч С М И Т Ь Б Ю Backspace
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key("^", 58)
                        key("Я", 58)
                        key("Ч", 58)
                        key("С", 58)
                        key("М", 58)
                        key("И", 58)
                        key("Т", 58)
                        key("Ь", 58)
                        key("Б", 58)
                        key("Ю", 58)
                        key("<", 58)
                    }

                    // Ряд 4: 123 Пробел . Enter
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key("123", 100)
                        key(",", 60)
                        key(" ", 320)
                        key(".", 60)
                        key("ОК", 100)
                    }
                }
            }
        }
        gs.redraw()
    }

    private fun MutableList<View>.key(label: String, w: Int) {
        Button(
            modifier = Modifier
                .width(w)
                .height(56)
                .padding(2)
                .background(Color(0, 0, 0, 255))
                .onClick {
                    if (label=="ОК"){
                        gs.cancelInject()
                        gs.redraw()
                        return@onClick
                    }
                    val needUpdate = contract.onKeyPress(label)
                    if (needUpdate) {
                        gs.redraw()
                    }
                },
            this
        ).layout {
            Text(
                modifier = Modifier
                    .width(w - 4)
                    .height(52)
                    .background(Color(255, 255, 255)),
                text = label,
                textSize = 20,
                parent = this
            )
        }
    }
}