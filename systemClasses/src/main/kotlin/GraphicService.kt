import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JFrame

data class Bounds(
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
    val onClick: () -> Unit
)

class Stack<T> {
    private val list = mutableListOf<T>()
    fun push(value: T) {
        list.add(value)
    }

    fun popBackStack() {
        list.removeLast()
    }

    fun peek(): T = list.last()
    fun stackSize(): Int = list.size
}

class GraphicService : GLEventListener, GraphicServiceI {
    private lateinit var canvas: GLCanvas

    private val sheight = 960
    private val swidth = 640

    private val viewTree = mutableListOf<View>()
    private val stack = Stack<MutableList<View>.() -> Unit>()
    private val bounds = mutableListOf<Bounds>()
    private val viewTreeUntilInject = mutableListOf<View>()
    private val lazyColumn = mutableListOf<LazyColumn>()
    private val renderer = Renderer(this, bounds, lazyColumn, sheight, swidth)
    private lateinit var last: MouseEvent
    fun initialize() {
        val frame = JFrame("Graphics")

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(swidth, sheight)

        val profile = GLProfile.get(GLProfile.GL2)
        val capabilities = GLCapabilities(profile)

        capabilities.sampleBuffers = true
        capabilities.numSamples = 4

        canvas = GLCanvas(capabilities)
        canvas.addGLEventListener(this)

        frame.add(canvas)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        frame.background = Color(255, 255, 255)

        canvas.requestFocusInWindow()


        canvas.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (lazyColumn.isNotEmpty()) {
                    val deltay = e.y.toDouble() - last.y.toDouble()
                    last = e
                    lazyColumn[0].offset += deltay
                    redraw()
                }
            }
        })
        canvas.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(p0: MouseEvent?) {
                super.mouseClicked(p0)
                val x = p0!!.x.toDouble()
                val y = p0.y.toDouble()
                handleClick(x, y)
            }

            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                last = e!!
            }
        })
    }

    override fun setContent(itIsNewScreen: Boolean, lambda: MutableList<View>.() -> Unit) {
        viewTree.clear()
        lazyColumn.clear()
        viewTree.lambda()
        if (itIsNewScreen) {
            stack.push(lambda)
        }
    }

    override fun injectUI(lambda: MutableList<View>.() -> Unit) {
        viewTreeUntilInject.clear()
        viewTreeUntilInject.addAll(viewTree)
        viewTree.lambda()
    }

    override fun cancelInject() {
        viewTree.clear()
        bounds.clear()
        viewTree.addAll(viewTreeUntilInject)
    }

    override fun popBackStack() {
        if (stack.stackSize() <= 1) return
        stack.popBackStack()
        viewTree.clear()
        bounds.clear()
        viewTreeUntilInject.clear()
        val lambda = stack.peek()
        viewTree.lambda()
        canvas.display()
    }

    override fun redraw() {
        canvas.display()
    }

    private fun handleClick(x: Double, y: Double) {
        for (bound in bounds.reversed()) {
            if (x in bound.x1..bound.x2 && y in bound.y1..bound.y2) {
                bound.onClick.invoke()
                return
            }
        }
    }

    override fun init(p0: GLAutoDrawable?) {
        val gl = p0!!.gl.gL2
        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glOrtho(0.0, swidth.toDouble(), sheight.toDouble(), 0.0, -1.0, 1.0)
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        gl.glEnable(GL2.GL_BLEND)
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun dispose(p0: GLAutoDrawable?) {}

    override fun display(p0: GLAutoDrawable?) {
        if (viewTree.isEmpty()) return
        val gl = p0!!.gl.gL2
        gl.glClear(GL.GL_COLOR_BUFFER_BIT)
        bounds.clear()
        viewTree.forEach {
            renderer.parse(gl, it)
        }
        navigation(gl)
        gl.glFlush()
    }

    private fun navigation(gl: GL2) {
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
        gl.glBegin(GL2.GL_QUADS)
        gl.glVertex2f(0f, 900f)
        gl.glVertex2f(640f, 900f)
        gl.glVertex2f(640f, 960f)
        gl.glVertex2f(0f, 960f)
        gl.glEnd()
        gl.glColor3f(0.5f, 0.5f, 0.5f)
        gl.glBegin(GL2.GL_QUADS)
        gl.glVertex2f(300f, 910f)
        gl.glVertex2f(340f, 910f)
        gl.glVertex2f(340f, 950f)
        gl.glVertex2f(300f, 950f)
        gl.glEnd()
        bounds.add(Bounds(300.0, 910.0, 340.0, 950.0, { popBackStack() }))
    }

    override fun reshape(
        p0: GLAutoDrawable?,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: Int
    ) {
    }

}