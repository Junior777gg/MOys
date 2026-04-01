import Modifier.Companion.BOTTOM
import Modifier.Companion.CENTER
import Modifier.Companion.LEFT
import Modifier.Companion.RIGHT
import Modifier.Companion.TOP
import com.jogamp.opengl.GL2
import com.jogamp.opengl.util.awt.TextRenderer
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import java.awt.Color
import java.awt.Font
import java.io.File

class Renderer(
    val gs: GraphicService?,
    val bounds: MutableList<Bounds>?,
    val lazyColumn: MutableList<LazyColumn>,
    val sheight: Int?,
    val swidth: Int?,
) {
    private val textRenderers = mutableMapOf<Int, TextRenderer>()
    private val textures = mutableMapOf<File, Texture>()
    fun getTexture(file: File): Texture {
        return textures.getOrPut(file) {
            TextureIO.newTexture(file, true)
        }
    }

    fun getTextRenderer(size: Int): TextRenderer {
        return textRenderers.getOrPut(size) {
            TextRenderer(Font("SansSerif", Font.PLAIN, size))
        }
    }

    fun parse(
        gl: GL2,
        view: View,
        avx1: Double = 0.0,
        avy1: Double = 0.0,
        avx2: Double = 640.0,
        avy2: Double = 960.0,
        centeringWidth: Int = LEFT,
        centeringHeight: Int = TOP
    ) {
        val modifiers = view.modifier
        val width = modifiers.get<Width>()?.width
        val height = modifiers.get<Height>()?.height
        val size = modifiers.get<Size>()?.size
        val fillMaxSize = modifiers.get<FillMaxSize>()
        val color = modifiers.get<Background>()?.color ?: Color(255, 255, 255, 255)
        val paddingTop = modifiers.get<PaddingTop>()?.top ?: 0
        val paddingLeft = modifiers.get<PaddingLeft>()?.left ?: 0
        val paddingRight = modifiers.get<PaddingRight>()?.right ?: 0
        val paddingBottom = modifiers.get<PaddingBottom>()?.bottom ?: 0
        val padding = modifiers.get<Padding>()?.padding ?: 0
        val widthCentering = modifiers.get<ChildrenWidthCentering>()?.place ?: CENTER
        val heightCentering = modifiers.get<ChildrenHeightCentering>()?.place ?: CENTER
        val onClick = modifiers.get<OnClick>()?.onClick

        if (view is LazyColumn) {
            lazyColumn.add(view)
        }

        var x1 = avx1
        var y1 = avy1
        var x2 = 0.0
        var y2 = 0.0
        if (width != null && height != null) {
            x2 = x1 + width.toDouble()
            y2 = y1 + height.toDouble()
        } else if (size != null) {
            x2 = x1 + size.toDouble()
            y2 = y1 + size.toDouble()
        } else if (fillMaxSize != null) {
            x2 = avx2
            y2 = avy2
        }
        if (x2 > avx2) x2 = avx2
        if (y2 > avy2) y2 = avy2
        when (centeringWidth) {
            LEFT -> {}
            CENTER -> {
                val padx = (avx2 - x2) / 2.0
                x2 = avx2 - padx
                x1 = avx1 + padx
            }

            RIGHT -> {
                val padx = avx2 - x2
                x2 = avx2
                x1 = avx1 + padx
            }
        }
        when (centeringHeight) {
            TOP -> {}
            CENTER -> {
                val pady = (avy2 - y2) / 2.0
                y2 = avy2 - pady
                y1 = avy1 + pady
            }

            BOTTOM -> {
                val pady = avy2 - y2
                y2 = avy2
                y1 = avy1 + pady
            }
        }
        x1 = x1 + paddingLeft.toDouble() + padding.toDouble()
        y1 = y1 + paddingTop.toDouble() + padding.toDouble()
        x2 = x2 - paddingRight.toDouble() - padding.toDouble()
        y2 = y2 - paddingBottom.toDouble() - padding.toDouble()
        if (view is TextField) {
            bounds!!.add(Bounds(x1, y1, x2, y2) {
                onClick?.invoke()
                Keyboard(gs!!, view).main()
            })
        } else if (onClick != null) {
            bounds!!.add(Bounds(x1, y1, x2, y2, onClick))
        }
        when (view) {
            is Button, is Box, is Column, is Row, is LazyColumn-> {
                gl.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f(x1.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y2.toFloat())
                gl.glVertex2f(x1.toFloat(), y2.toFloat())
                gl.glEnd()
            }
            is TextField -> {
                gl.glColor4f(0f, 0f, 0f, 255f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f(x1.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y2.toFloat())
                gl.glVertex2f(x1.toFloat(), y2.toFloat())
                gl.glEnd()
                gl.glColor4f(255f, 255f, 255f, 255f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f((x1 + 2).toFloat(), (y1 + 2).toFloat())
                gl.glVertex2f((x2 - 2).toFloat(), (y1 + 2).toFloat())
                gl.glVertex2f((x2 - 2).toFloat(), (y2 - 2).toFloat())
                gl.glVertex2f((x1 + 2).toFloat(), (y2 - 2).toFloat())
                gl.glEnd()
                val textRenderer = getTextRenderer(view.textSize)
                textRenderer.beginRendering(swidth!!, sheight!!)
                textRenderer.setColor(view.textColor)
                val offsetx = (x2 - x1) - textRenderer.getBounds(view.text).bounds.width
                val offsety = (y2 - y1) - textRenderer.getBounds(view.text).bounds.height
                textRenderer.draw(view.text, (x1 + offsetx / 2).toInt(), sheight - (y2 - offsety / 2).toInt())
                textRenderer.endRendering()
            }

            is Text -> {
                val textRenderer = getTextRenderer(view.textSize)
                textRenderer.beginRendering(swidth!!, sheight!!)
                textRenderer.setColor(view.textColor)
                val offsetx = (x2 - x1) - textRenderer.getBounds(view.text).bounds.width
                val offsety = (y2 - y1) - textRenderer.getBounds(view.text).bounds.height
                textRenderer.draw(view.text, (x1 + offsetx / 2).toInt(), sheight - (y2 - offsety / 2).toInt())
                textRenderer.endRendering()
            }

            is Image -> {
                val texture = getTexture(view.file)
                gl.glColor3f(1f, 1f, 1f)
                gl.glEnable(GL2.GL_TEXTURE_2D)
                texture.bind(gl)
                gl.glBegin(GL2.GL_QUADS)
                gl.glTexCoord2f(0f, 1f)
                gl.glVertex2f(x1.toFloat(), y1.toFloat())
                gl.glTexCoord2f(1f, 1f)
                gl.glVertex2f(x2.toFloat(), y1.toFloat())
                gl.glTexCoord2f(1f, 0f)
                gl.glVertex2f(x2.toFloat(), y2.toFloat())
                gl.glTexCoord2f(0f, 0f)
                gl.glVertex2f(x1.toFloat(), y2.toFloat())
                gl.glEnd()
                gl.glDisable(GL2.GL_TEXTURE_2D)
            }
        }

        if (view.children.isEmpty()) {
            return
        } else {
            when (view) {
                is LazyColumn -> {
                    gl.glEnable(GL2.GL_SCISSOR_TEST)
                    gl.glScissor(x1.toInt(), sheight!! - y2.toInt(), (x2 - x1).toInt(), (y2 - y1).toInt())
                    var currenty1 = y1 + view.offset
                    var childrenHeight = 0.0
                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (heightChild != null) {
                            childrenHeight += heightChild.toDouble()
                        } else if (sizeChild != null) {
                            childrenHeight += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            childrenHeight += avy2
                        }
                    }
                    var minimum = (-childrenHeight + (y2 - y1))
                    if (minimum > 0.0) {
                        minimum = 0.0
                    } else {
                        view.offset = view.offset.coerceIn(minimum, 0.0)
                    }

                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        var shift = 0.0
                        if (heightChild != null) {
                            shift += heightChild.toDouble()
                        } else if (sizeChild != null) {
                            shift += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            shift = avy2
                        }
                        if ((currenty1 + shift) < y1 || currenty1 > y2) {
                            null
                        } else {
                            parse(
                                gl, it, x1, currenty1,
                                x2, currenty1 + shift, widthCentering, heightCentering
                            )
                        }
                        currenty1 += shift
                    }
                    gl.glDisable(GL2.GL_SCISSOR_TEST)
                }

                is Column -> {
                    var currenty1 = y1
                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        var shift = 0.0
                        if (heightChild != null) {
                            shift += heightChild.toDouble()
                        } else if (sizeChild != null) {
                            shift += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            shift = avy2
                        }
                        parse(
                            gl, it, x1, currenty1,
                            x2, currenty1 + shift, widthCentering, heightCentering
                        )
                        currenty1 += shift
                    }
                }

                is Box -> {
                    view.children.forEach {
                        parse(
                            gl, it, x1, y1,
                            x2, y2, widthCentering, heightCentering
                        )

                    }
                }
                //Row
                else -> {
                    var currentx1 = x1
                    view.children.forEach {
                        val widthChild = it.modifier.get<Width>()?.width
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        var shift = 0.0
                        if (widthChild != null) {
                            shift += widthChild.toDouble()
                        } else if (sizeChild != null) {
                            shift += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            shift = avx2
                        }
                        parse(
                            gl, it, currentx1, y1,
                            currentx1 + shift, y2, widthCentering, heightCentering
                        )
                        currentx1 += shift
                    }
                }
            }
        }
    }
}