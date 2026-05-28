interface IKeyboardContract {
    fun onKey(key: Char): Boolean
    fun onText(text: String): Boolean
    fun onErase(amount: Int): Boolean
 }