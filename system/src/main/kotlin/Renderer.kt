import com.jogamp.opengl.GL2
import com.jogamp.opengl.util.awt.TextRenderer
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import common.Log
import common.Vec2
import java.awt.Color
import java.awt.Font
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderer. Traverses the View tree and renders elements using OpenGL.
 * Caches fonts and textures for performance.
 */
class Renderer(
    val gs: GraphicService,
    val bounds: MutableList<Bounds>,
    val lazyColumn: MutableList<LazyColumn>,
    var screenHeight: Int,
    var screenWidth: Int,
) {
    private val textRenderers = mutableMapOf<Int, TextRenderer>()
    private val textures = mutableMapOf<File, Texture>()
    private val segments = 16
    private val cos = FloatArray(segments+1)
    private val sin = FloatArray(segments+1)
    init {
        for (i in 0..segments) {
            val corner = (i.toFloat() / segments)*(PI/2).toFloat()
            cos[i] = cos(corner)
            sin[i] = sin(corner)
        }

    }
    fun getTexture(file: File): Texture {
        return textures.getOrPut(file) {
            TextureIO.newTexture(file, true)
        }
    }

    fun clearCache() {
        textRenderers.clear()
        textures.clear()
    }

    fun getTextRenderer(size: Int): TextRenderer {
        return textRenderers.getOrPut(size) {
            TextRenderer(Font("SansSerif", Font.PLAIN, size))
        }
    }

    fun getTextAlign(textAlign: Int, x1: Double, y2: Double, offsetx: Double, offsety: Double): Vec2 {
        val x = when(Text.getHorizontalAlign(textAlign)) {
            Text.H_LEFT->x1
            Text.H_CENTER->x1+offsetx/2
            Text.H_RIGHT->x1+offsetx
            else->x1
        }
        val y = when(Text.getVerticalAlign(textAlign)) {
            Text.V_TOP->y2
            Text.V_CENTER->y2-offsety/2
            Text.V_BOTTOM->y2-offsety
            else->y2
        }
        return Vec2(x,y)
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
        val fillMaxWidth = modifiers.get<FillMaxWidth>()
        val fillMaxHeight = modifiers.get<FillMaxHeight>()
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
        }else if (size != null) {
            x2 = x1 + size.toDouble()
            y2 = y1 + size.toDouble()
        }else if (fillMaxSize != null) {
            x2 = avx2
            y2 = avy2
        }else if (fillMaxWidth != null&&height != null) {
            x2 = avx2
            y2 = y1 + height.toDouble()
        }else if (fillMaxHeight != null&&width != null) {
            x2 = x1 + width.toDouble()
            y2 = avy2
        }else{
            Log.warn("$view doesnt have anough size")
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
        } else if (onHold != null) {
            bounds.add(Bounds(x1, y1, x2, y2, onClick, onHold))
        }
        when (view) {
            is Box, is Column, is Row, is LazyColumn -> {
                gl.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex2f(x1.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y1.toFloat())
                gl.glVertex2f(x2.toFloat(), y2.toFloat())
                gl.glVertex2f(x1.toFloat(), y2.toFloat())
                gl.glEnd()
            }

            is Button -> {
                gl.glBegin(GL2.GL_POLYGON)
                if (view.cornerRadius == 0){
                    gl.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    gl.glBegin(GL2.GL_QUADS)
                    gl.glVertex2f(x1.toFloat(), y1.toFloat())
                    gl.glVertex2f(x2.toFloat(), y1.toFloat())
                    gl.glVertex2f(x2.toFloat(), y2.toFloat())
                    gl.glVertex2f(x1.toFloat(), y2.toFloat())
                    gl.glEnd()
                    return
                }
                gl.glBegin(GL2.GL_POLYGON)
                gl.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                val height = y2-y1
                val width = x2-x1
                val r = view.cornerRadius
                val right = x1 + width
                val bottom = y1 + height
                val cx_tl = x1 + r      // center x, top-left
                val cy_tl = y1 + r      // center y, top-left
                val cx_tr = right - r  // center x, top-right
                val cy_tr = y1 + r
                val cx_bl = x1 + r
                val cy_bl = bottom - r
                val cx_br = right - r
                val cy_br = bottom - r

                gl.glBegin(GL2.GL_POLYGON)

                // Верхний левый угол
                for (i in 0..segments) {
                    gl.glVertex2f((cx_tl - r * sin[i]).toFloat(), (cy_tl - r * cos[i]).toFloat())
                }
                // Нижний левый угол
                for (i in 0..segments) {
                    gl.glVertex2f((cx_bl - r * cos[i]).toFloat(), (cy_bl + r * sin[i]).toFloat())
                }
                // Нижний правый угол
                for (i in 0..segments) {
                    gl.glVertex2f((cx_br + r * sin[i]).toFloat(), (cy_br + r * cos[i]).toFloat())
                }
                // Верхний правый угол
                for (i in 0..segments) {
                    gl.glVertex2f((cx_tr + r * cos[i]).toFloat(), (cy_tr - r * sin[i]).toFloat())
                }

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
                var bgColor=view.modifier.get<Background>()?.color
                if(bgColor==null) bgColor = Color(1,1,1,1)
                gl.glColor4f(bgColor.red / 255f, bgColor.green / 255f, bgColor.blue / 255f, bgColor.alpha / 255f)
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
                val alignVec = getTextAlign(view.textAlign, x1, y2, offsetx, offsety)
                textRenderer.draw(view.text, alignVec.x.toInt(), screenHeight-alignVec.y.toInt())
                textRenderer.endRendering()
            }

            is Text -> {
                val textRenderer = getTextRenderer(view.textSize)
                textRenderer.beginRendering(screenWidth, screenHeight)
                textRenderer.setColor(view.textColor)
                val offsetx = (x2 - x1) - textRenderer.getBounds(view.text).bounds.width
                val offsety = (y2 - y1) - textRenderer.getBounds(view.text).bounds.height
                val alignVec = getTextAlign(view.textAlign, x1, y2, offsetx, offsety)
                textRenderer.draw(view.text, alignVec.x.toInt(), screenHeight-alignVec.y.toInt())
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
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (heightChild != null) {
                            childrenHeight += heightChild.toDouble()
                        } else if (sizeChild != null) {
                            childrenHeight += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            childrenHeight += avy2
                        } else if (fillMaxHeightChild != null) {
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
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toDouble()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toDouble()
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = x2 - x1
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = y2 - y1
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }
                        if (fillMaxSizeChild != null) {
                            currentHeight = x2 - x1
                            currentWidth = x2 - x1
                        }
                        when (view.horizontalAlignment) {
                            is HorizontalAlignment.Left -> {
                                currentx1 = x1
                            }

                            is HorizontalAlignment.Right -> {
                                currentx1 = x2 - currentWidth
                            }

                            is HorizontalAlignment.Center -> {
                                currentx1 = x1 + ((x2 - x1) - currentWidth) / 2
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
                    var heightSmallChildren = 0.0
                    var fillMaxHeightChildCount = 0
                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if(heightChild != null) {
                            childrenHeight += heightChild.toDouble()
                            heightSmallChildren += heightChild.toDouble()
                        } else if (sizeChild != null) {
                            childrenHeight += sizeChild.toDouble()
                            heightSmallChildren += sizeChild.toDouble()
                        }else if (fillMaxHeightChild != null) {
                            childrenHeight = y2 - y1
                            fillMaxHeightChildCount++
                        }else if (fillMaxSizeChild != null) {
                            childrenHeight = y2 - y1
                            fillMaxHeightChildCount++
                        }
                    }
                    when (view.verticalArrangement) {
                        is VerticalArrangement.Bottom -> {
                            currenty1 = y2 - childrenHeight
                        }

                        is VerticalArrangement.Top -> {
                            currenty1 = y1
                        }

                        is VerticalArrangement.Center -> {
                            currenty1 = y1 + ((y2 - y1) - childrenHeight) / 2
                        }

                        is VerticalArrangement.SpaceEvenly -> {
                            gap = ((y2 - y1) - childrenHeight) / (view.children.size.toDouble() + 1.0)
                            currenty1 = y1 + gap
                        }

                    }
                    view.children.forEach {
                        var currentx1 = 0.0
                        var currentWidth = 0.0
                        var currentHeight = 0.0
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toDouble()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toDouble()
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = (childrenHeight - heightSmallChildren)/fillMaxHeightChildCount
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = x2 - x1
                        }
                        if (fillMaxSizeChild != null) {
                            currentHeight = (childrenHeight - heightSmallChildren)/fillMaxHeightChildCount
                            currentWidth = x2 - x1
                        }
                        when (view.horizontalAlignment) {
                            is HorizontalAlignment.Left -> {
                                currentx1 = x1
                            }

                            is HorizontalAlignment.Right -> {
                                currentx1 = x2 - currentWidth
                            }

                            is HorizontalAlignment.Center -> {
                                currentx1 = x1 + ((x2 - x1) - currentWidth) / 2
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
                    var widthSmallChildren = 0.0
                    var fillMaxWidthChildCount = 0
                    view.children.forEach {
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            childrenWidth += widthChild.toDouble()
                            widthSmallChildren += widthChild.toDouble()
                        } else if (fillMaxWidthChild != null) {
                            childrenWidth = x2 - x1
                            fillMaxWidthChildCount++
                        }else if (sizeChild != null) {
                            childrenWidth += sizeChild.toDouble()
                            widthSmallChildren += sizeChild.toDouble()
                        } else if (fillMaxSizeChild != null) {
                            childrenWidth = x2 - x1
                            fillMaxWidthChildCount++
                        }
                    }
                    when (view.horizontalArrangement) {
                        is HorizontalArrangement.Left -> {
                            currentx1 = x1
                        }

                        is HorizontalArrangement.Right -> {
                            currentx1 = x2 - childrenWidth
                        }

                        is HorizontalArrangement.Center -> {
                            currentx1 = x1 + ((x2 - x1) - childrenWidth) / 2
                        }

                        is HorizontalArrangement.SpaceEvenly -> {
                            gap = ((x2 - x1) - childrenWidth) / (view.children.size.toDouble() + 1.0)
                            currentx1 = x1 + gap
                        }

                    }
                    view.children.forEach {
                        var currenty1 = 0.0
                        var currentWidth = 0.0
                        var currentHeight = 0.0
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toDouble()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toDouble()
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toDouble()
                            currentHeight = sizeChild.toDouble()
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = (childrenWidth - widthSmallChildren)/fillMaxWidthChildCount
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = y2 - y1
                        }
                        if (fillMaxSizeChild != null) {
                            currentHeight = x2 - x1
                            currentWidth = (childrenWidth - widthSmallChildren)/fillMaxWidthChildCount
                        }
                        when (view.verticalAlignment) {
                            is VerticalAlignment.Top -> {
                                currenty1 = y1
                            }

                            is VerticalAlignment.Bottom -> {
                                currenty1 = y2 - currentHeight
                            }

                            is VerticalAlignment.Center -> {
                                currenty1 = y1 + ((y2 - y1) - currentHeight) / 2
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