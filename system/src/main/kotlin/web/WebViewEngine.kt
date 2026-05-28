package web

import WebView
import WebViewCommand
import WebViewInputEvent
import impl.GraphicServiceImpl
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import navigation.SystemKeyboard
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.util.concurrent.ConcurrentHashMap

object WebViewEngine {
    private val holders = ConcurrentHashMap<WebView, Holder>()
    @Volatile
    private var fxInitialized = false

    fun getHolder(view: WebView): Holder? = holders[view]

    private fun ensureFx() {
        if (fxInitialized) return

        System.setProperty("prism.order", "sw")
        System.setProperty("prism.vsync", "false")
        System.setProperty("javafx.animation.fullspeed", "true")

        JFXPanel()
        Platform.setImplicitExit(false)
        fxInitialized = true
    }

    fun frame(view: WebView, gs: GraphicServiceImpl, w: Int, h: Int): Image? {
        if (w <= 0 || h <= 0) return null
        ensureFx()

        val holder = holders.getOrPut(view) { Holder(view, gs, w, h) }
        holder.resize(w, h)
        holder.drainCommands()
        holder.drainInputs()
        holder.maybeSnapshot()

        return holder.skiaImage
    }

    fun dispatchClick(view: WebView, localX: Float, localY: Float) {
        holders[view]?.view?.pendingInputs?.add(WebViewInputEvent.Click(localX, localY))
    }

    fun dispatchScroll(view: WebView, lx: Float, ly: Float, deltaY: Float, isWheel: Boolean = false) {
        holders[view]?.let { holder ->
            view.pendingInputs.add(WebViewInputEvent.Scroll(lx, ly, deltaY, isWheel))
        }
    }

    fun disposeAll() {
        holders.values.forEach { it.dispose() }
        holders.clear()
    }

    class Holder(
        val view: WebView,
        val gs: GraphicServiceImpl,
        initialW: Int,
        initialH: Int
    ) {
        @Volatile
        var skiaImage: Image? = null

        @Volatile
        private var w: Int = initialW
        @Volatile
        private var h: Int = initialH

        private val snapshotPending = java.util.concurrent.atomic.AtomicBoolean(false)

        private var fxWebView: javafx.scene.web.WebView? = null
        private var root: javafx.scene.layout.StackPane? = null
        private var scene: Scene? = null
        private var stage: javafx.stage.Stage? = null

        private var lastSnapshot = 0L
        private val targetFps = 20
        private val frameNanos = 1_000_000_000L / targetFps

        @Volatile
        private var dirty = true

        init {
            Platform.runLater {
                //Create web view, root and scene for JavaFX rendering.
                val wv = javafx.scene.web.WebView().apply {
                    isContextMenuEnabled = false
                    isFocusTraversable = true
                    minWidth = w.toDouble()
                    minHeight = h.toDouble()
                    prefWidth = w.toDouble()
                    prefHeight = h.toDouble()
                    maxWidth = w.toDouble()
                    maxHeight = h.toDouble()
                }
                val rootPane = javafx.scene.layout.StackPane(wv).apply {
                    style = "-fx-background-color: white;"
                    minWidth = w.toDouble()
                    minHeight = h.toDouble()
                    prefWidth = w.toDouble()
                    prefHeight = h.toDouble()
                    maxWidth = w.toDouble()
                    maxHeight = h.toDouble()
                }
                val sc = Scene(rootPane, w.toDouble(), h.toDouble(), javafx.scene.paint.Color.WHITE)
                //Create and hide JavaFX browser.
                val st = javafx.stage.Stage(javafx.stage.StageStyle.UTILITY).apply {
                    scene = sc
                    x = -32000.0
                    y = -32000.0
                    width = 1.0
                    height = 1.0
                    opacity = 0.0
                    isAlwaysOnTop = false
                    initOwner(null)
                    title = ""
                    show()
                    toBack()
                }
                //Save and apply web view properties.
                fxWebView = wv
                root = rootPane
                scene = sc
                stage = st
                applySizeNow()
                //Redraw page on address update.
                wv.engine.locationProperty().addListener { _, _, v ->
                    view.updateFromEngineUrl(v ?: "about:blank")
                    dirty = true
                    gs.redraw()
                }
                //Redraw page on title update.
                wv.engine.titleProperty().addListener { _, _, v ->
                    view.updateFromEngineTitle(v ?: "")
                    markDirty()
                }
                //Redraw page on JavaFX updates.
                wv.engine.loadWorker.runningProperty().addListener { _, _, running ->
                    view.updateFromEngineLoading(
                        running,
                        runCatching { wv.engine.history.currentIndex > 0 }.getOrDefault(false),
                        false
                    )
                    markDirty()
                }
                //Redraw page if state changed.
                wv.engine.loadWorker.stateProperty().addListener { _, _, state ->
                    dirty = true
                    if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                        Platform.runLater {
                            Platform.runLater { markDirty() }
                        }
                    }
                }
                //Redraw page if load progress changed.
                wv.engine.loadWorker.progressProperty().addListener { _, _, _ -> markDirty() }
            }
        }

        /**Applies new size to WebView at first viable moment*/
        fun resize(nw: Int, nh: Int) {
            if (nw == w && nh == h) return
            w = nw
            h = nh
            dirty = true
            //Schedule size update.
            Platform.runLater { applySizeNow() }
        }
        /**Applies new size to WebView immediately*/
        private fun applySizeNow() {
            val wv = fxWebView ?: return
            val rootPane = root ?: return
            val st = stage ?: return
            //Get new size.
            val width = w.coerceAtLeast(1).toDouble()
            val height = h.coerceAtLeast(1).toDouble()
            //Translate size to root.
            rootPane.minWidth = width
            rootPane.prefWidth = width
            rootPane.maxWidth = width
            rootPane.minHeight = height
            rootPane.prefHeight = height
            rootPane.maxHeight = height
            //Translate size to web view.
            wv.minWidth = width
            wv.prefWidth = width
            wv.maxWidth = width
            wv.minHeight = height
            wv.prefHeight = height
            wv.maxHeight = height
            //Translate size to stage.
            st.width = width
            st.height = height
            //Apply size to web view and root.
            rootPane.resizeRelocate(0.0, 0.0, width, height)
            wv.resizeRelocate(0.0, 0.0, width, height)
            //Recreate layouts.
            rootPane.applyCss()
            rootPane.layout()
            wv.requestLayout()
            wv.autosize()
        }

        fun drainCommands() {
            if (view.pendingCommands.isEmpty()) return

            val snap = mutableListOf<WebViewCommand>()
            while (true) {
                val cmd = view.pendingCommands.poll() ?: break
                snap += cmd
            }

            Platform.runLater {
                val wv = fxWebView ?: return@runLater
                snap.forEach { c ->
                    when (c) {
                        is WebViewCommand.LoadUrl -> wv.engine.load(c.url)
                        is WebViewCommand.ExecuteJs -> runCatching { wv.engine.executeScript(c.script) }
                        WebViewCommand.GoBack -> runCatching { wv.engine.history.go(-1) }
                        WebViewCommand.GoForward -> runCatching { wv.engine.history.go(1) }
                        WebViewCommand.Reload -> wv.engine.reload()
                    }
                }
                dirty = true
            }
        }

        fun drainInputs() {
            if (view.pendingInputs.isEmpty()) return

            val snap = mutableListOf<WebViewInputEvent>()
            while (true) {
                val ev = view.pendingInputs.poll() ?: break
                snap += ev
            }

            Platform.runLater {
                val wv = fxWebView ?: return@runLater
                val sc = scene ?: return@runLater

                snap.forEach { ev ->
                    when (ev) {
                        is WebViewInputEvent.Click -> {
                            val sceneX = ev.lx.toDouble()
                            val sceneY = ev.ly.toDouble()

                            val pickResult = javafx.scene.input.PickResult(wv, sceneX, sceneY)

                            val pressed = MouseEvent(
                                MouseEvent.MOUSE_PRESSED,
                                sceneX, sceneY,
                                sceneX, sceneY,
                                MouseButton.PRIMARY, 1,
                                false, false, false, false,
                                true, false, false,
                                true, false, true,
                                pickResult
                            )

                            val released = MouseEvent(
                                MouseEvent.MOUSE_RELEASED,
                                sceneX, sceneY,
                                sceneX, sceneY,
                                MouseButton.PRIMARY, 1,
                                false, false, false, false,
                                false, false, false,
                                true, false, true,
                                pickResult
                            )

                            val clicked = MouseEvent(
                                MouseEvent.MOUSE_CLICKED,
                                sceneX, sceneY,
                                sceneX, sceneY,
                                MouseButton.PRIMARY, 1,
                                false, false, false, false,
                                false, false, false,
                                true, false, true,
                                pickResult
                            )

                            javafx.event.Event.fireEvent(sc, pressed)
                            javafx.event.Event.fireEvent(sc, released)
                            javafx.event.Event.fireEvent(sc, clicked)

                            Platform.runLater {
                                Platform.runLater {
                                    if (isTextInputFocused()) {
                                        val contract = WebViewKeyboardContract(this)
                                        SystemKeyboard(gs, contract).main()
                                    }
                                }
                            }
                        }

                        is WebViewInputEvent.Scroll -> {
                            val x = ev.lx.toInt()
                            val y = ev.ly.toInt()
                            val deltaPixels = if (ev.isWheel) {
                                (ev.dy * 50).toInt()
                            } else {
                                (-ev.dy).toInt()
                            }

                            val pickResult = javafx.scene.input.PickResult(
                                wv, ev.lx.toDouble(), ev.ly.toDouble()
                            )

                            val scrollEvent = javafx.scene.input.ScrollEvent(
                                javafx.scene.input.ScrollEvent.SCROLL,
                                ev.lx.toDouble(), ev.ly.toDouble(),
                                ev.lx.toDouble(), ev.ly.toDouble(),
                                false, false, false, false,
                                false, false,
                                0.0, -deltaPixels.toDouble(),
                                0.0, -deltaPixels.toDouble(),
                                javafx.scene.input.ScrollEvent.HorizontalTextScrollUnits.NONE, 0.0,
                                javafx.scene.input.ScrollEvent.VerticalTextScrollUnits.NONE, 0.0,
                                0,
                                pickResult
                            )

                            javafx.event.Event.fireEvent(sc, scrollEvent)

                            // JS fallback
                            wv.engine.executeScript("""
                        (function() {
                            var el = document.elementFromPoint($x, $y);
                            var target = el;
                            while (target && target !== document.body && target !== document.documentElement) {
                                var style = window.getComputedStyle(target);
                                var oy = style.overflowY;
                                if ((oy === 'auto' || oy === 'scroll' || oy === 'overlay')
                                    && target.scrollHeight > target.clientHeight) {
                                    break;
                                }
                                target = target.parentElement;
                            }
                            if (!target || target === document.body) {
                                target = document.scrollingElement || document.documentElement;
                            }
                            target.scrollBy(0, $deltaPixels);
                        })();
                    """.trimIndent())
                        }

                        is WebViewInputEvent.Key -> {
                            val ch = ev.char
                            val code = ev.code
                            val down = ev.down

                            if (down) {
                                when (code) {
                                    java.awt.event.KeyEvent.VK_BACK_SPACE -> {
                                        wv.engine.executeScript("""
                                    (function() {
                                        var el = document.activeElement;
                                        if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA')) {
                                            var start = el.selectionStart;
                                            var end = el.selectionEnd;
                                            if (start !== end) {
                                                el.value = el.value.substring(0, start) + el.value.substring(end);
                                                el.selectionStart = el.selectionEnd = start;
                                            } else if (start > 0) {
                                                el.value = el.value.substring(0, start - 1) + el.value.substring(start);
                                                el.selectionStart = el.selectionEnd = start - 1;
                                            }
                                            el.dispatchEvent(new Event('input', {bubbles: true}));
                                        }
                                    })();
                                """.trimIndent())
                                    }

                                    java.awt.event.KeyEvent.VK_ENTER -> {
                                        wv.engine.executeScript("""
                                    (function() {
                                        var el = document.activeElement;
                                        if (el && el.tagName === 'INPUT') {
                                            var form = el.closest('form');
                                            if (form) {
                                                if (form.requestSubmit) form.requestSubmit();
                                                else form.submit();
                                            }
                                        }
                                        var opts = {bubbles:true, cancelable:true, key:'Enter', code:'Enter', keyCode:13};
                                        if (el) {
                                            el.dispatchEvent(new KeyboardEvent('keydown', opts));
                                            el.dispatchEvent(new KeyboardEvent('keypress', opts));
                                            el.dispatchEvent(new KeyboardEvent('keyup', opts));
                                        }
                                    })();
                                """.trimIndent())
                                    }

                                    else -> {
                                        if (!ch.isISOControl() && ch != '\u0000') {
                                            val escaped = ch.toString()
                                                .replace("\\", "\\\\")
                                                .replace("'", "\\'")
                                                .replace("\n", "\\n")
                                                .replace("\r", "\\r")

                                            wv.engine.executeScript("""
                                        (function() {
                                            var el = document.activeElement;
                                            if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA')) {
                                                var start = el.selectionStart || 0;
                                                var end = el.selectionEnd || 0;
                                                var c = '$escaped';
                                                el.value = el.value.substring(0, start) + c + el.value.substring(end);
                                                el.selectionStart = el.selectionEnd = start + c.length;
                                                el.dispatchEvent(new Event('input', {bubbles: true}));
                                            } else if (el && el.isContentEditable) {
                                                document.execCommand('insertText', false, '$escaped');
                                            }
                                            var opts = {bubbles:true, cancelable:true, key:'$escaped', keyCode:${ch.code}};
                                            if (el) {
                                                el.dispatchEvent(new KeyboardEvent('keydown', opts));
                                                el.dispatchEvent(new KeyboardEvent('keypress', opts));
                                                el.dispatchEvent(new KeyboardEvent('keyup', opts));
                                            }
                                        })();
                                    """.trimIndent())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                dirty = true
                gs.redraw()
            }
        }

        fun executeMouseMove(x: Float, y: Float) {
            Platform.runLater {
                val wv = fxWebView ?: return@runLater
                val ix = x.toInt()
                val iy = y.toInt()
                wv.engine.executeScript("""
            (function() {
                var el = document.elementFromPoint($ix, $iy);
                if (el) {
                    el.dispatchEvent(new MouseEvent('mousemove', {
                        bubbles: true, cancelable: true,
                        clientX: $ix, clientY: $iy
                    }));
                }
            })();
        """.trimIndent())
            }
        }

        /**Checks if the currently focused DOM element is a text input.*/
        fun getFocusedInputValue(): String? {
            val wv = fxWebView ?: return null
            return try {
                val result = wv.engine.executeScript("""
            (function() {
                var el = document.activeElement;
                if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA')) {
                    return el.value || '';
                } else if (el && el.isContentEditable) {
                    return el.innerText || '';
                }
                return null;
            })();
        """.trimIndent())
                result?.toString()
            } catch (e: Exception) {
                null
            }
        }

        /**Returns true if a text-input element is currently focused in the WebView.*/
        fun isTextInputFocused(): Boolean {
            val wv = fxWebView ?: return false
            return try {
                val result = wv.engine.executeScript("""
            (function() {
                var el = document.activeElement;
                if (!el) return false;
                if (el.tagName === 'INPUT') {
                    var type = (el.type || 'text').toLowerCase();
                    return type === 'text' || type === 'password' || type === 'email'
                        || type === 'search' || type === 'url' || type === 'tel'
                        || type === 'number';
                }
                if (el.tagName === 'TEXTAREA') return true;
                if (el.isContentEditable) return true;
                return false;
            })();
        """.trimIndent())
                result == true || result.toString() == "true"
            } catch (e: Exception) {
                false
            }
        }

        fun maybeSnapshot() {
            val now = System.nanoTime()
            if (!dirty && now - lastSnapshot < frameNanos) return
            if (!snapshotPending.compareAndSet(false, true)) return

            lastSnapshot = now

            Platform.runLater {
                val wv = fxWebView
                if (wv == null) {
                    snapshotPending.set(false)
                    return@runLater
                }

                applySizeNow()

                val params = SnapshotParameters().apply {
                    fill = javafx.scene.paint.Color.WHITE
                    viewport = javafx.geometry.Rectangle2D(
                        0.0, 0.0,
                        w.coerceAtLeast(1).toDouble(),
                        h.coerceAtLeast(1).toDouble()
                    )
                }

                val target = WritableImage(
                    w.coerceAtLeast(1),
                    h.coerceAtLeast(1)
                )

                wv.snapshot(
                    { result ->
                        try {
                            val bi = SwingFXUtils.fromFXImage(result.image, null)

                            val newImg = getFrame(bi)
                            val old = skiaImage
                            skiaImage = newImg
                            old?.close()

                            dirty = view.isLoading
                        } finally {
                            snapshotPending.set(false)
                            gs.redraw()
                        }
                        null
                    },
                    params,
                    target
                )
            }
        }

        /**Destroys browser and cached snapshot image*/
        fun dispose() {
            val old = skiaImage
            skiaImage = null
            old?.close()

            Platform.runLater {
                runCatching { fxWebView?.engine?.load("about:blank") }
                runCatching { stage?.hide() }
                stage = null
                scene = null
                root = null
                fxWebView = null
            }
        }

        /**Returns JavaFX WebView*/
        fun getFxWebView(): javafx.scene.web.WebView? {
            return fxWebView
        }

        /**If set, WebView will be redrawn next frame*/
        fun markDirty() {
            dirty = true
            gs.redraw()
        }
    }

    private fun getFrame(src: BufferedImage): Image {
        //Get real BufferedImage.
        val img =
            if (src.type == BufferedImage.TYPE_INT_ARGB) src
            else BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB).also {
                val g = it.createGraphics()
                g.drawImage(src, 0, 0, null)
                g.dispose()
            }
        //Get generic image data.
        val px = (img.raster.dataBuffer as DataBufferInt).data
        val bytes = ByteArray(px.size * 4)
        //Translate to buffer.
        var o = 0
        for (argb in px) {
            val a = (argb ushr 24) and 0xFF
            val r = (argb ushr 16) and 0xFF
            val g = (argb ushr 8) and 0xFF
            val b = argb and 0xFF

            bytes[o++] = r.toByte()
            bytes[o++] = g.toByte()
            bytes[o++] = b.toByte()
            bytes[o++] = a.toByte()
        }
        //Form image data and pass buffer to it.
        val info = ImageInfo(
            img.width,
            img.height,
            ColorType.RGBA_8888,
            ColorAlphaType.UNPREMUL
        )
        return Image.makeRaster(info, Data.makeFromBytes(bytes), img.width * 4)
    }
}