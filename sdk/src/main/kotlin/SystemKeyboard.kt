import common.Color
import modifier.Modifier
import modifier.VerticalAlignment
import modifier.background
import modifier.fillMaxSize
import modifier.height
import modifier.onClick
import modifier.padding
import modifier.paddingBottom
import modifier.width
import service.GraphicService

class SystemKeyboard(
    val gs: GraphicService,
    val contract: IKeyboard
) {
    private val russianAlphabet= listOf("Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х","Ф", "Ы", "В", "А", "П", "Р",
        "О", "Л", "Д", "Ж", "Э","^", "Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю", "<","123", "ABC", ",", " ", ".", "ОК",)
    private val englishAlphabet = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "A", "S", "D", "F", "G",
        "H", "J", "K", "L", ";", "'", "^", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "<",
        "123", "АБВ", ",", " ", ".", "OK")
    private val specialSymbols = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-",
        "@", "#", "$", "%", "&", "*", "(", ")", "/", ";", "'",
        "^", "?", "!", "\"", ":", ";", ",", ".", "+", "=", "<", "\\", "АБВ", ",", " ", ".", "ОК" )

    private var currentAlphabet = russianAlphabet
    private var lowercase: Boolean = false

    fun main() {
        gs.injectUI {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .paddingBottom(60)
                    .background(Color.TRANSPARENT),
                verticalAlignment = VerticalAlignment.Bottom(),
                parent = this
            ).layout {
                Column(
                    modifier = Modifier.height(260).width(640).background(Color(200, 200, 200)),
                    this@layout
                ).layout {

                    // Ряд 1: Й Ц У К Е Н Г Ш Щ З Х
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key(currentAlphabet[0], 58)
                        key(currentAlphabet[1], 58)
                        key(currentAlphabet[2], 58)
                        key(currentAlphabet[3], 58)
                        key(currentAlphabet[4], 58)
                        key(currentAlphabet[5], 58)
                        key(currentAlphabet[6], 58)
                        key(currentAlphabet[7], 58)
                        key(currentAlphabet[8], 58)
                        key(currentAlphabet[9], 58)
                        key(currentAlphabet[10], 58)
                    }

                    // Ряд 2: Ф Ы В А П Р О Л Д Ж Э
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key(currentAlphabet[11], 58)
                        key(currentAlphabet[12], 58)
                        key(currentAlphabet[13], 58)
                        key(currentAlphabet[14], 58)
                        key(currentAlphabet[15], 58)
                        key(currentAlphabet[16], 58)
                        key(currentAlphabet[17], 58)
                        key(currentAlphabet[18], 58)
                        key(currentAlphabet[19], 58)
                        key(currentAlphabet[20], 58)
                        key(currentAlphabet[21], 58)
                    }

                    // Ряд 3: Shift Я Ч С М И Т Ь Б Ю Backspace
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key(currentAlphabet[22], 58)
                        key(currentAlphabet[23], 58)
                        key(currentAlphabet[24], 58)
                        key(currentAlphabet[25], 58)
                        key(currentAlphabet[26], 58)
                        key(currentAlphabet[27], 58)
                        key(currentAlphabet[28], 58)
                        key(currentAlphabet[29], 58)
                        key(currentAlphabet[30], 58)
                        key(currentAlphabet[31], 58)
                        key(currentAlphabet[32], 58)
                    }

                    // Ряд 4: 123 Пробел . Enter
                    Row(modifier = Modifier.width(640).height(60), this).layout {
                        key(currentAlphabet[33], 100)
                        key(currentAlphabet[34], 60)
                        key(currentAlphabet[35],60)
                        key(currentAlphabet[36],260)
                        key(currentAlphabet[37],60)
                        key(currentAlphabet[38], 100)
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
                    if (label=="ОК"||label=="OK"){
                        gs.cancelInject()
                        gs.redraw()
                        return@onClick
                    }
                    if(label=="ABC"||label=="abc"){
                        gs.cancelInject()
                        gs.redraw()
                        currentAlphabet = englishAlphabet
                        main()
                        return@onClick
                    }
                    if(label=="АБВ"||label=="абв"){
                        gs.cancelInject()
                        gs.redraw()
                        currentAlphabet = russianAlphabet
                        main()
                        return@onClick
                    }
                    if(label=="123"){
                        gs.cancelInject()
                        gs.redraw()
                        currentAlphabet = specialSymbols
                        main()
                        return@onClick
                    }
                    if (label=="^"){
                        lowercase = !lowercase
                        if (lowercase) {
                            gs.cancelInject()
                            gs.redraw()
                            val lowAlphabet = mutableListOf<String>()
                            currentAlphabet.forEach {
                                lowAlphabet.add(it.lowercase())
                            }
                            currentAlphabet = lowAlphabet
                            main()
                            return@onClick
                        }else{
                            gs.cancelInject()
                            gs.redraw()
                            val upAlphabet = mutableListOf<String>()
                            currentAlphabet.forEach {
                                upAlphabet.add(it.uppercase())
                            }
                            currentAlphabet = upAlphabet
                            main()
                            return@onClick
                        }
                    }
                    val needUpdate = contract.onKeyPress(label)
                    if (needUpdate) {
                        gs.redraw()
                    }
                },
            parent = this
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