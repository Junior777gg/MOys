import com.jogamp.opengl.GL2
import com.jogamp.opengl.util.awt.TextRenderer
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import java.awt.Color
import java.awt.Font
import java.io.File

/**
 * Renderer. Traverses the View tree and renders elements using OpenGL.
 * Caches fonts and textures for performance.
 */
class Renderer(
    val gs: GraphicService,
    val bounds: MutableList<Bounds>,
    val lazyColumn: MutableList<LazyColumn>,
    val screenHeight: Int,
    val screenWidth: Int,
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

    /**
     * Recursively traverses the View-tree, calculates the coordinates (layout),
     * and renders each element through OpenGL.
     *
     *  avx1, avy1, avx2, avy2 — the available area for placing the View
     *  centeringWidth — the horizontal alignment of children (LEFT, CENTER, RIGHT)
     *  centeringHeight — the vertical alignment of children (TOP, CENTER, BOTTOM)
     */
    fun parse(
        gl: GL2,
        view: View,
        avx1: Double = 0.0,
        avy1: Double = 0.0,
        avx2: Double = screenWidth.toDouble(),
        avy2: Double = screenHeight.toDouble(),
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
        val onClick = modifiers.get<OnClick>()?.onClick
        val onHold = modifiers.get<OnHold>()?.onHold

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
        }else if(width != null) {
            x2 = x1 + width.toDouble()
            y2 = y1 + width.toDouble()
        }else if(height != null) {
            x2 = x1 + height.toDouble()
            y2 = y1 + height.toDouble()
        }
        if (x2 > avx2) x2 = avx2
        if (y2 > avy2) y2 = avy2

        x1 = x1 + paddingLeft.toDouble() + padding.toDouble()
        y1 = y1 + paddingTop.toDouble() + padding.toDouble()
        x2 = x2 - paddingRight.toDouble() - padding.toDouble()
        y2 = y2 - paddingBottom.toDouble() - padding.toDouble()
        if (view is TextField) {
            bounds.add(Bounds(x1, y1, x2, y2, {
                onClick?.invoke()
                Keyboard(gs, view).main()
            }, null))
        } else if (onClick != null) {
            bounds.add(Bounds(x1, y1, x2, y2, onClick, onHold))
        } else if(onHold!=null) {
            bounds.add(Bounds(x1, y1, x2, y2, onClick, onHold))
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
                gl.glColor4f(0f, 0f, 0f, 1f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f(x1.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y2.toFloat())
                gl.glVertex2f(x1.toFloat(), y2.toFloat())
                gl.glEnd()
                gl.glColor4f(1f, 1f, 1f, 1f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f((x1 + 2).toFloat(), (y1 + 2).toFloat())
                gl.glVertex2f((x2 - 2).toFloat(), (y1 + 2).toFloat())
                gl.glVertex2f((x2 - 2).toFloat(), (y2 - 2).toFloat())
                gl.glVertex2f((x1 + 2).toFloat(), (y2 - 2).toFloat())
                gl.glEnd()
                val textRenderer = getTextRenderer(view.textSize)
                textRenderer.beginRendering(screenWidth, screenHeight)
                textRenderer.setColor(view.textColor)
                val offsetx = (x2 - x1) - textRenderer.getBounds(view.text).bounds.width
                val offsety = (y2 - y1) - textRenderer.getBounds(view.text).bounds.height
                textRenderer.draw(view.text, (x1 + offsetx / 2).toInt(), screenHeight - (y2 - offsety / 2).toInt())
                textRenderer.endRendering()
            }

            is Text -> {
                val textRenderer = getTextRenderer(view.textSize)
                textRenderer.beginRendering(screenWidth, screenHeight)
                textRenderer.setColor(view.textColor)
                val offsetx = (x2 - x1) - textRenderer.getBounds(view.text).bounds.width
                val offsety = (y2 - y1) - textRenderer.getBounds(view.text).bounds.height
                textRenderer.draw(view.text, (x1 + offsetx / 2).toInt(), screenHeight - (y2 - offsety / 2).toInt())
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
                    gl.glScissor(x1.toInt(), screenHeight - y2.toInt(), (x2 - x1).toInt(), (y2 - y1).toInt())
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
                        var currentx1 = 0.0
                        var currentWidth = 0.0
                        var currentHeight = 0.0
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null&&heightChild != null) {
                            currentWidth = widthChild.toDouble()
                            currentHeight = heightChild.toDouble()
                        }else if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }else if (fillMaxSizeChild != null) {
                            currentHeight = x2 - x1
                            currentWidth = x2 - x1
                        }
                        when(view.horizontalAlignment){
                            is HorizontalAlignment.Left ->{
                                currentx1 = x1
                            }
                            is HorizontalAlignment.Right->{
                                currentx1 = x2 - currentWidth
                            }
                            is HorizontalAlignment.Center->{
                                currentx1 = x1 + ((x2 - x1)- currentWidth)/2
                            }
                        }

                        if ((currenty1 + currentHeight) < y1 || currenty1 > y2) {
                            null
                        } else {
                            parse(
                                gl, it, currentx1, currenty1,
                                x2, y2
                            )
                        }
                        currenty1 += currentHeight
                    }
                    gl.glDisable(GL2.GL_SCISSOR_TEST)
                }

                is Column -> {
                    var currenty1 = 0.0
                    var gap = 0.0
                    var childrenHeight = 0.0
                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (heightChild != null) {
                            childrenHeight+=heightChild.toDouble()
                        }else if (sizeChild != null) {
                            childrenHeight+=sizeChild.toDouble()
                        }else if (fillMaxSizeChild != null) {
                            childrenHeight=y2-y1
                        }
                    }
                    when(view.verticalArrangement){
                        is VerticalArrangement.Bottom -> {
                            currenty1 = y2 - childrenHeight
                        }
                        is VerticalArrangement.Top ->{
                            currenty1 = y1
                        }
                        is VerticalArrangement.Center -> {
                            currenty1 = y1 + ((y2 - y1)- childrenHeight)/2
                        }
                        is VerticalArrangement.SpaceEvenly -> {
                            gap = ((y2 - y1) - childrenHeight)/(view.children.size.toDouble() + 1.0)
                           currenty1 = y1 + gap
                        }

                    }
                    view.children.forEach {
                        var currentx1 = 0.0
                        var currentWidth = 0.0
                        var currentHeight = 0.0
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null&&heightChild != null) {
                            currentWidth = widthChild.toDouble()
                            currentHeight = heightChild.toDouble()
                        }else if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }else if (fillMaxSizeChild != null) {
                            currentHeight = y2 - y1
                            currentWidth = x2 - x1
                        }
                        when(view.horizontalAlignment){
                            is HorizontalAlignment.Left ->{
                                currentx1 = x1
                            }
                            is HorizontalAlignment.Right->{
                                currentx1 = x2 - currentWidth
                            }
                            is HorizontalAlignment.Center->{
                                currentx1 = x1 + ((x2 - x1) - currentWidth)/2
                            }
                        }
                        parse(
                            gl, it, currentx1, currenty1,
                            x2, y2
                        )
                        currenty1 += currentHeight + gap
                    }
                }

                //Row
                is Row -> {
                    var gap = 0.0
                    var currentx1 = 0.0
                    var childrenWidth = 0.0
                    view.children.forEach {
                        val widthChild = it.modifier.get<Width>()?.width
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            childrenWidth+=widthChild.toDouble()
                        }else if (sizeChild != null) {
                            childrenWidth+=sizeChild.toDouble()
                        }else if (fillMaxSizeChild != null) {
                            childrenWidth=x2-x1
                        }
                    }
                    when(view.horizontalArrangement){
                        is HorizontalArrangement.Left ->{
                            currentx1 = x1
                        }
                        is HorizontalArrangement.Right->{
                            currentx1 = x2 - childrenWidth
                        }
                        is HorizontalArrangement.Center->{
                            currentx1 = x1 + ((x2-x1)-childrenWidth)/2
                        }
                        is HorizontalArrangement.SpaceEvenly -> {
                            gap = ((x2-x1)-childrenWidth)/(view.children.size.toDouble() + 1.0)
                            currentx1 = x1 + gap
                        }

                    }
                    view.children.forEach {
                        var currenty1 = 0.0
                        var currentWidth = 0.0
                        var currentHeight = 0.0
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null&&heightChild != null) {
                            currentWidth = widthChild.toDouble()
                            currentHeight = heightChild.toDouble()
                        }else if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }else if (fillMaxSizeChild != null) {
                            currentHeight = y2 - y1
                            currentWidth = x2 - x1
                        }
                        when(view.verticalAlignment){
                            is VerticalAlignment.Top ->{
                                currenty1 = y1
                            }
                            is VerticalAlignment.Bottom ->{
                                currenty1 = y2 - currentHeight
                            }
                            is VerticalAlignment.Center ->{
                                currenty1 = y1 + ((y2-y1)-currentHeight)/2
                            }
                        }
                        parse(
                            gl, it, currentx1, currenty1,
                            x2, y2
                        )
                        currentx1 += currentWidth + gap
                    }
                }
                else -> {
                    view.children.forEach {
                        parse(
                            gl, it, x1, y1,
                            x2, y2
                        )

                    }
                }
            }
        }
    }
}