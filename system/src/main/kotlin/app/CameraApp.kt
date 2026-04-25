package app

import Activity
import Button
import Column
import Row
import Text
import View
import common.Color
import impl.DeviceManagerImpl
import impl.VideoPlayerImpl
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.VerticalArrangement
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.paddingBottom
import modifier.size
import org.bytedeco.javacv.Java2DFrameConverter
import service.DeviceManager
import service.GraphicService
import service.StorageService
import java.io.File
import javax.imageio.ImageIO

class CameraApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager
) : Activity {
    val player = VideoPlayerImpl(gs)
    init {
        player.createVideoPlayer("http://192.168.0.15:8080/video")
    }
    override fun main() {
        DeviceManagerImpl.Camera.cameraPath = "http://192.168.0.15:8080/video"
        DeviceManagerImpl.Camera.startCamera()
        gs.setContent(true){
            buildGeneralUI()
            buildPhotoUI()
        }
        gs.redraw()
    }

    fun MutableList<View>.buildGeneralUI(){
        player.startVideoPlayer(this)
        Column(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT).paddingBottom(100), verticalArrangement = VerticalArrangement.Bottom(), parent = this).layout {
            Row(modifier = Modifier.fillMaxWidth().height(100).background(Color.WHITE), horizontalArrangement = HorizontalArrangement.SpaceEvenly(), parent = this).layout {
                Button(modifier = Modifier.size(70).background(Color.RED).onClick {
                    gs.setContent {
                        buildPhotoUI()
                    }
                    gs.redraw()
                }, parent = this).layout {
                    Text(modifier = Modifier.fillMaxSize(), text = "Фото", parent = this)
                }
                Button(modifier = Modifier.size(70).background(Color.GREEN).onClick {
                    gs.setContent {
                        buildVideoUI()
                    }
                    gs.redraw()
                }, parent = this).layout {
                    Text(modifier = Modifier.fillMaxSize(), text = "Видео", parent = this)
                }
            }
        }
    }

    fun MutableList<View>.buildPhotoUI(){
        Column(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT), verticalArrangement = VerticalArrangement.Center(), parent = this).layout{
            Button(modifier = Modifier.size(70).background(Color.BLUE).onClick {
                takePhoto()
            }, this).layout {
                Text(modifier = Modifier.fillMaxSize(), text = "Фото!", parent = this)
            }
        }
    }

    fun MutableList<View>.buildVideoUI(){
        Column(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT), verticalArrangement = VerticalArrangement.Center(), parent = this).layout{
            Button(modifier = Modifier.size(70).background(Color.BLUE).onClick {
                //takeVideo()
            }, this).layout {
                Text(modifier = Modifier.fillMaxSize(), text = "Видео!", parent = this)
            }
            Button(modifier = Modifier.size(70).background(Color.BLUE).onClick {
                //stopVideo()
            }, this).layout {
                Text(modifier = Modifier.fillMaxSize(), text = "Стоп!", parent = this)
            }
        }
    }

    fun takePhoto(){
        val frame = DeviceManagerImpl.Camera.cameraDataFrame()
        val converter = Java2DFrameConverter()
        val image = converter.convert(frame)
        val file = File("/mnt/c/Users/MSI/Desktop/image.png")
        ImageIO.write(image, "png", file)
    }

    override fun onDestroy() {
        player.removeVideoPlayer()
    }

    /*fun takeVideo(){
        val player = VideoPlayerImpl
        player.createVideoPlayer(DeviceManagerImpl.Camera.cameraPath)
    }

    fun stopVideo(){
        val player = VideoPlayerImpl
        player.stopPlayer()
    }*/
}