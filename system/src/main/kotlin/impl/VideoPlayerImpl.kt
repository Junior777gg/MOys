package impl

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.awt.BorderLayout
import javax.swing.JFrame

object VideoPlayerImpl {
    private lateinit var vlcPlayer: EmbeddedMediaPlayerComponent
    var stopped = false
    fun createVideoPlayer(path: String) {
        vlcPlayer = EmbeddedMediaPlayerComponent()
        JFrame("player").apply {
            contentPane.add(vlcPlayer, BorderLayout.CENTER)
            size = java.awt.Dimension(800, 600)
            isVisible = true
        }
        vlcPlayer.mediaPlayer().media().play(path)
    }

    fun stopPlayer() {
        stopped = !stopped
        if (stopped) {
            vlcPlayer.mediaPlayer().controls().stop()
        } else {
            vlcPlayer.mediaPlayer().controls().play()
        }
    }

    fun removeVideoPlayer() {
        vlcPlayer.mediaPlayer().controls().stop()
        vlcPlayer.remove(vlcPlayer)
    }
}