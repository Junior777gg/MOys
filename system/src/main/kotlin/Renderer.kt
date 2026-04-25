import common.Bounds
import common.Log
import common.Vec2
import common.Color
import impl.GraphicServiceImpl
import modifier.Background
import modifier.CornerRadius
import modifier.FillMaxHeight
import modifier.FillMaxSize
import modifier.FillMaxWidth
import modifier.Height
import modifier.HorizontalAlignment
import modifier.HorizontalArrangement
import modifier.OnClick
import modifier.OnHold
import modifier.Padding
import modifier.PaddingBottom
import modifier.PaddingLeft
import modifier.PaddingRight
import modifier.PaddingTop
import modifier.Size
import modifier.TextAlignment
import modifier.VerticalAlignment
import modifier.VerticalArrangement
import modifier.Width
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.toBitmap


/**
 * Renderer. Traverses the View tree and renders elements using OpenGL.
 * Caches fonts and textures for performance.
 */
class Renderer(
    val gs: GraphicServiceImpl,
    val bounds: MutableList<Bounds>,
    val lazyColumn: MutableList<LazyColumn>,
    var screenHeight: Int,
    var screenWidth: Int,
) {
    private fun getTextAlign(textAlign: Int, x1: Double, y2: Double, offsetx: Double, offsety: Double): Vec2 {
        var align = textAlign
        if (!TextAlignment.isValidAlignment(align)) align = TextAlignment.Center()
        val x = when (TextAlignment.getHorizontal(align)) {
            TextAlignment.H_LEFT -> x1
            TextAlignment.H_CENTER -> x1 + offsetx / 2
            TextAlignment.H_RIGHT -> x1 + offsetx
            else -> x1 + offsetx / 2
        }
        val y = when (TextAlignment.getVertical(align)) {
            TextAlignment.V_TOP -> y2
            TextAlignment.V_CENTER -> y2 - offsety / 2
            TextAlignment.V_BOTTOM -> y2 - offsety
            else -> y2 - offsety / 2
        }
        return Vec2(x, y)
    }

    private fun toJavaAwtColor(color: Color): java.awt.Color = java.awt.Color(color.r, color.g, color.b, color.a)

    /**
     * Recursively traverses the View-tree, calculates the coordinates (layout),
     * and renders each element through OpenGL.
     *
     *  avx1, avy1, avx2, avy2 — the available area for placing the View
     *  centeringWidth — the horizontal alignment of children (LEFT, CENTER, RIGHT)
     *  centeringHeight — the vertical alignment of children (TOP, CENTER, BOTTOM)
     */
    fun parse(
        canvas: Canvas,
        view: View,
        avx1: Float = 0.0f,
        avy1: Float = 0.0f,
        avx2: Float = screenWidth.toFloat(),
        avy2: Float = screenHeight.toFloat(),
    ) {
        val modifiers = view.modifier
        val width = modifiers.get<Width>()?.width
        val height = modifiers.get<Height>()?.height
        val size = modifiers.get<Size>()?.size
        val cornerRadius = modifiers.get<CornerRadius>()?.cornerRadius ?: 0
        val fillMaxSize = modifiers.get<FillMaxSize>()
        val fillMaxWidth = modifiers.get<FillMaxWidth>()
        val fillMaxHeight = modifiers.get<FillMaxHeight>()
        val backgroundColor = modifiers.get<Background>()?.color ?: Color.WHITE
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
        var x2 = 0.0f
        var y2 = 0.0f
        if (width != null && height != null) {
            x2 = x1 + width.toFloat()
            y2 = y1 + height.toFloat()
        } else if (size != null) {
            x2 = x1 + size.toFloat()
            y2 = y1 + size.toFloat()
        } else if (fillMaxSize != null) {
            x2 = avx2
            y2 = avy2
        } else if (fillMaxWidth != null && height != null) {
            x2 = avx2
            y2 = y1 + height.toFloat()
        } else if (fillMaxHeight != null && width != null) {
            x2 = x1 + width.toFloat()
            y2 = avy2
        } else {
            Log.warn("$view doesnt have enough size")
        }
        if (x2 > avx2) x2 = avx2
        if (y2 > avy2) y2 = avy2

        x1 = x1 + paddingLeft.toFloat() + padding.toFloat()
        y1 = y1 + paddingTop.toFloat() + padding.toFloat()
        x2 = x2 - paddingRight.toFloat() - padding.toFloat()
        y2 = y2 - paddingBottom.toFloat() - padding.toFloat()
        if (view is TextField) {
            bounds.add(Bounds(x1, y1, x2, y2, {
                onClick?.invoke()
                SystemKeyboard(gs, view).main()
            }, null))
        } else if (onClick != null) {
            bounds.add(Bounds(x1, y1, x2, y2, onClick, onHold))
        } else if (onHold != null) {
            bounds.add(Bounds(x1, y1, x2, y2, onClick, onHold))
        }
        when (view) {
            is Button, is Box, is Column, is Row, is LazyColumn -> {
                canvas.drawRRect(
                    RRect.makeXYWH(x1, y1, x2 - x1, y2 - y1, cornerRadius.toFloat()),
                    paint = Paint().apply {
                        color = org.jetbrains.skia.Color.makeARGB(
                            backgroundColor.a,
                            backgroundColor.r,
                            backgroundColor.g,
                            backgroundColor.b
                        )
                        mode = PaintMode.FILL
                        isAntiAlias = true
                    }
                )
            }

            is TextField -> {
                canvas.drawRRect(
                    RRect.makeXYWH(x1, y1, x2 - x1, y2 - y1, cornerRadius.toFloat()),
                    paint = Paint().apply {
                        color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
                        mode = PaintMode.FILL
                        isAntiAlias = true
                    }
                )
                canvas.drawRRect(
                    RRect.makeXYWH(x1 + 2, y1 + 2, x2 - x1 - 4, y2 - y1 - 4, cornerRadius.toFloat()),
                    paint = Paint().apply {
                        color = org.jetbrains.skia.Color.makeARGB(
                            backgroundColor.a,
                            backgroundColor.r,
                            backgroundColor.g,
                            backgroundColor.b
                        )
                        mode = PaintMode.FILL
                        isAntiAlias = true
                    }
                )
                val textStyle = TextStyle().apply {
                    color = org.jetbrains.skia.Color.makeRGB(
                        view.textColor.r,
                        view.textColor.g,
                        view.textColor.b
                    )
                    fontSize = view.textSize.toFloat()
                    fontFamilies = arrayOf("Arial")
                }
                val paragraphStyle = ParagraphStyle().apply {
                    direction = Direction.LTR
                    alignment = when {
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_LEFT -> Alignment.LEFT
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_CENTER -> Alignment.CENTER
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_RIGHT -> Alignment.RIGHT
                        else -> Alignment.LEFT
                    }
                }
                val paragraphBuilder = ParagraphBuilder(paragraphStyle, FontCollection().apply {
                    setDefaultFontManager(FontMgr.default)
                })
                paragraphBuilder.pushStyle(textStyle)
                paragraphBuilder.addText(view.text)

                val paragraph = paragraphBuilder.build()
                paragraph.layout(x2 - x1)

                val paragraphHeight = paragraph.height
                val offsetY = when (TextAlignment.getVertical(view.textAlign)) {
                    TextAlignment.V_TOP -> y1
                    TextAlignment.V_CENTER -> y1 + (y2 - y1 - paragraphHeight) / 2
                    TextAlignment.V_BOTTOM -> y2 - paragraphHeight
                    else -> y1 + (y2 - y1 - paragraphHeight) / 2
                }

                paragraph.paint(canvas, x1, offsetY)
            }

            is Text -> {
                val textStyle = TextStyle().apply {
                    color = org.jetbrains.skia.Color.makeRGB(
                        view.textColor.r,
                        view.textColor.g,
                        view.textColor.b
                    )
                    fontSize = view.textSize.toFloat()
                    fontFamilies = arrayOf("Arial")
                }
                val paragraphStyle = ParagraphStyle().apply {
                    direction = Direction.LTR
                    alignment = when {
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_LEFT -> Alignment.LEFT
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_CENTER -> Alignment.CENTER
                        TextAlignment.getHorizontal(view.textAlign) == TextAlignment.H_RIGHT -> Alignment.RIGHT
                        else -> Alignment.LEFT
                    }
                }
                val paragraphBuilder = ParagraphBuilder(paragraphStyle, FontCollection().apply {
                    setDefaultFontManager(FontMgr.default)
                })
                paragraphBuilder.pushStyle(textStyle)
                paragraphBuilder.addText(view.text)

                val paragraph = paragraphBuilder.build()
                paragraph.layout(x2 - x1)
                val paragraphHeight = paragraph.height
                val offsetY = when (TextAlignment.getVertical(view.textAlign)) {
                    TextAlignment.V_TOP -> y1
                    TextAlignment.V_CENTER -> y1 + (y2 - y1 - paragraphHeight) / 2
                    TextAlignment.V_BOTTOM -> y2 - paragraphHeight
                    else -> y1 + (y2 - y1 - paragraphHeight) / 2
                }

                paragraph.paint(canvas, x1, offsetY)
            }

            is Image -> {
                if (view.file != null) {
                    canvas.drawImageRect(
                        image = org.jetbrains.skia.Image.makeFromEncoded(view.file!!.readBytes()),
                        dst = Rect.makeXYWH(x1, y1, x2 - x1, y2 - y1),
                    )
                } else if (view.image != null) {
                    canvas.drawImageRect(
                        image = view.image!!as org.jetbrains.skia.Image,
                        dst = Rect.makeXYWH(x1, y1, x2 - x1, y2 - y1),
                    )
                }
            }
        }

        if (view.children.isEmpty()) {
            return
        } else {
            when (view) {
                is LazyColumn -> {
                    //gl.glEnable(GL2.GL_SCISSOR_TEST)
                    //gl.glScissor(x1.toInt(), screenHeight - y2.toInt(), (x2 - x1).toInt(), (y2 - y1).toInt())
                    var currenty1 = y1 + view.offset.toFloat()

                    view.children.forEach {
                        var currentx1 = 0.0f
                        var currentWidth = 0.0f
                        var currentHeight = 0.0f
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toFloat()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toFloat()
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = x2 - x1
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = y2 - y1
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toFloat()
                            currentHeight = sizeChild.toFloat()
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
                                canvas, it, currentx1, currenty1,
                                x2, y2
                            )
                        }
                        currenty1 += currentHeight
                    }
                    //gl.glDisable(GL2.GL_SCISSOR_TEST)
                }

                is Column -> {
                    var currenty1 = 0.0f
                    var gap = 0.0f
                    var childrenHeight = 0.0f
                    var heightSmallChildren = 0.0f
                    var fillMaxHeightChildCount = 0
                    view.children.forEach {
                        val heightChild = it.modifier.get<Height>()?.height
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (heightChild != null) {
                            childrenHeight += heightChild.toFloat()
                            heightSmallChildren += heightChild.toFloat()
                        } else if (sizeChild != null) {
                            childrenHeight += sizeChild.toFloat()
                            heightSmallChildren += sizeChild.toFloat()
                        } else if (fillMaxHeightChild != null) {
                            childrenHeight = y2 - y1
                            fillMaxHeightChildCount++
                        } else if (fillMaxSizeChild != null) {
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
                            gap = ((y2 - y1) - childrenHeight) / (view.children.size.toFloat() + 1.0f)
                            currenty1 = y1 + gap
                        }

                    }
                    view.children.forEach {
                        var currentx1 = 0.0f
                        var currentWidth = 0.0f
                        var currentHeight = 0.0f
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toFloat()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toFloat()
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toFloat()
                            currentHeight = sizeChild.toFloat()
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = (childrenHeight - heightSmallChildren) / fillMaxHeightChildCount
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = x2 - x1
                        }
                        if (fillMaxSizeChild != null) {
                            currentHeight = (childrenHeight - heightSmallChildren) / fillMaxHeightChildCount
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
                            canvas, it, currentx1, currenty1,
                            x2, y2
                        )
                        currenty1 += currentHeight + gap
                    }
                }

                //Row
                is Row -> {
                    var gap = 0.0f
                    var currentx1 = 0.0f
                    var childrenWidth = 0.0f
                    var widthSmallChildren = 0.0f
                    var fillMaxWidthChildCount = 0
                    view.children.forEach {
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            childrenWidth += widthChild
                            widthSmallChildren += widthChild
                        } else if (fillMaxWidthChild != null) {
                            childrenWidth = x2 - x1
                            fillMaxWidthChildCount++
                        } else if (sizeChild != null) {
                            childrenWidth += sizeChild
                            widthSmallChildren += sizeChild
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
                            gap = ((x2 - x1) - childrenWidth) / (view.children.size.toFloat() + 1.0f)
                            currentx1 = x1 + gap
                        }

                    }
                    view.children.forEach {
                        var currenty1 = 0.0f
                        var currentWidth = 0.0f
                        var currentHeight = 0.0f
                        val heightChild = it.modifier.get<Height>()?.height
                        val widthChild = it.modifier.get<Width>()?.width
                        val fillMaxHeightChild = it.modifier.get<FillMaxHeight>()
                        val fillMaxWidthChild = it.modifier.get<FillMaxWidth>()
                        val sizeChild = it.modifier.get<Size>()?.size
                        val fillMaxSizeChild = it.modifier.get<FillMaxSize>()
                        if (widthChild != null) {
                            currentWidth = widthChild.toFloat()
                        }
                        if (heightChild != null) {
                            currentHeight = heightChild.toFloat()
                        }
                        if (sizeChild != null) {
                            currentWidth = sizeChild.toFloat()
                            currentHeight = sizeChild.toFloat()
                        }
                        if (fillMaxWidthChild != null) {
                            currentWidth = (childrenWidth - widthSmallChildren) / fillMaxWidthChildCount
                        }
                        if (fillMaxHeightChild != null) {
                            currentHeight = y2 - y1
                        }
                        if (fillMaxSizeChild != null) {
                            currentHeight = x2 - x1
                            currentWidth = (childrenWidth - widthSmallChildren) / fillMaxWidthChildCount
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
                            canvas, it, currentx1, currenty1,
                            x2, y2
                        )
                        currentx1 += currentWidth + gap
                    }
                }

                else -> {
                    view.children.forEach {
                        parse(
                            canvas, it, x1, y1,
                            x2, y2
                        )

                    }
                }
            }
        }
    }
}