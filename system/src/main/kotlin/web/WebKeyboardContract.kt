package web

import IKeyboardContract
import javafx.application.Platform

class WebViewKeyboardContract(
    private val holder: WebViewEngine.Holder
) : IKeyboardContract {

    override fun onKey(key: Char): Boolean {
        Platform.runLater {
            val wv = holder.getFxWebView() ?: return@runLater

            val escaped = key.toString()
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
                        el.dispatchEvent(new Event('change', {bubbles: true}));
                    } else if (el && el.isContentEditable) {
                        document.execCommand('insertText', false, '$escaped');
                    }
                })();
            """.trimIndent())

            holder.markDirty()
        }
        return true
    }

    override fun onText(text: String): Boolean {
        Platform.runLater {
            val wv = holder.getFxWebView() ?: return@runLater

            val escaped = text
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
                        var t = '$escaped';
                        el.value = el.value.substring(0, start) + t + el.value.substring(end);
                        el.selectionStart = el.selectionEnd = start + t.length;
                        el.dispatchEvent(new Event('input', {bubbles: true}));
                        el.dispatchEvent(new Event('change', {bubbles: true}));
                    } else if (el && el.isContentEditable) {
                        document.execCommand('insertText', false, '$escaped');
                    }
                })();
            """.trimIndent())

            holder.markDirty()
        }
        return true
    }

    override fun onErase(amount: Int): Boolean {
        Platform.runLater {
            val wv = holder.getFxWebView() ?: return@runLater

            wv.engine.executeScript("""
                (function() {
                    var el = document.activeElement;
                    if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA')) {
                        var start = el.selectionStart || 0;
                        var end = el.selectionEnd || 0;
                        var amount = $amount;
                        if (start !== end) {
                            // Delete selection
                            el.value = el.value.substring(0, start) + el.value.substring(end);
                            el.selectionStart = el.selectionEnd = start;
                        } else if (start >= amount) {
                            el.value = el.value.substring(0, start - amount) + el.value.substring(start);
                            el.selectionStart = el.selectionEnd = start - amount;
                        } else {
                            el.value = el.value.substring(start);
                            el.selectionStart = el.selectionEnd = 0;
                        }
                        el.dispatchEvent(new Event('input', {bubbles: true}));
                        el.dispatchEvent(new Event('change', {bubbles: true}));
                    } else if (el && el.isContentEditable) {
                        for (var i = 0; i < $amount; i++) {
                            document.execCommand('delete', false, null);
                        }
                    }
                })();
            """.trimIndent())

            holder.markDirty()
        }
        return true
    }
}