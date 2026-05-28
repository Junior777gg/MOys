import modifier.Modifier
import java.util.concurrent.ConcurrentLinkedQueue

class WebView(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    initialUrl: String = "about:blank"
) : View {
    override val children: MutableList<View> = mutableListOf()
    var lastBounds: FloatArray? = null

    var url: String = initialUrl
        private set
    var title: String = ""
        private set
    var isLoading: Boolean = false
        private set
    var canGoBack: Boolean = false
        private set
    var canGoForward: Boolean = false
        private set

    var onUrlChanged: ((String) -> Unit)? = null
    var onTitleChanged: ((String) -> Unit)? = null
    var onLoadChanged: ((Boolean) -> Unit)? = null

    val pendingCommands = ConcurrentLinkedQueue<WebViewCommand>()
    val pendingInputs = ConcurrentLinkedQueue<WebViewInputEvent>()

    init {
        pendingCommands.add(WebViewCommand.LoadUrl(initialUrl))
        parent.add(this)
    }

    fun loadUrl(url: String) {
        if (this.url == url) return
        this.url = url
        pendingCommands.add(WebViewCommand.LoadUrl(url))
    }
    fun goBack() {
        pendingCommands.add(WebViewCommand.GoBack)
    }
    fun goForward() {
        pendingCommands.add(WebViewCommand.GoForward)
    }
    fun reload() {
        pendingCommands.add(WebViewCommand.Reload)
    }

    fun executeJs(script: String) {
        pendingCommands.add(WebViewCommand.ExecuteJs(script))
    }
    fun injectCSS(css: String) {
        val escapedCSS = css
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        val js = """
            (function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.appendChild(document.createTextNode('$escapedCSS'));
                document.head.appendChild(style);
            })();
        """.trimIndent()
        executeJs(js)
    }

    fun updateFromEngineUrl(url: String) {
        this.url = url
        onUrlChanged?.invoke(url)
    }
    fun updateFromEngineTitle(title: String) {
        this.title = title
        onTitleChanged?.invoke(title)
    }
    fun updateFromEngineLoading(
        loading: Boolean,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        this.isLoading = loading
        this.canGoBack = canGoBack
        this.canGoForward = canGoForward
        onLoadChanged?.invoke(loading)
    }
}

sealed class WebViewCommand {
    data class LoadUrl(val url: String) : WebViewCommand()
    data class ExecuteJs(val script: String) : WebViewCommand()
    object GoBack : WebViewCommand()
    object GoForward : WebViewCommand()
    object Reload : WebViewCommand()
}

sealed class WebViewInputEvent {
    data class Click(val lx: Float, val ly: Float) : WebViewInputEvent()
    data class Scroll(val lx: Float, val ly: Float, val dy: Float, val isWheel: Boolean = false) : WebViewInputEvent()
    data class Key(val char: Char, val code: Int, val down: Boolean) : WebViewInputEvent()
}