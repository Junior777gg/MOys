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
import java.nio.file.Paths
import javax.swing.JFrame

//The main graphical service. Controls the window, rendering, and input processing.
class GraphicService : GLEventListener, GraphicServiceI {
    companion object {
        const val SCREEN_WIDTH = 640
        const val SCREEN_HEIGHT = 960
    }
    private lateinit var canvas: GLCanvas

    //The current View-element tree that is rendered on the screen
    private val viewTree = mutableListOf<View>()

    //Stack of screens for navigating "back"
    private val stack = Stack<MutableList<View>.() -> Unit>()

    //The list of clickable areas on the current frame
    private val bounds = mutableListOf<Bounds>()

    //Saved tree before calling injectUI (for restoration when cancelInject is called)
    private val viewTreeUntilInject = mutableListOf<View>()

    private val lazyColumn = mutableListOf<LazyColumn>()

    private val renderer = Renderer(this, bounds, lazyColumn, SCREEN_HEIGHT, SCREEN_WIDTH)
    private val navigationLambda = SystemNavigation(this).setUpNavigation()

    private var lastMouseY = 0.0
    fun initialize() {
        Log.dbg("Create JFRAME")
        val frame = JFrame("MOys")

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT)

        val profile = GLProfile.get(GLProfile.GL2)
        val capabilities = GLCapabilities(profile)

        capabilities.sampleBuffers = true
        capabilities.numSamples = 4
        Log.dbg("Done JFRAME")

        Log.dbg("Create GL_CANVAS")
        canvas = GLCanvas(capabilities)
        canvas.addGLEventListener(this)

        frame.add(canvas)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        frame.background = Color(255, 255, 255)
        Log.dbg("Done GL_CANVAS")

        canvas.requestFocusInWindow()

        canvas.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (lazyColumn.isNotEmpty()) {
                    val deltaY = e.y.toDouble() - lastMouseY
                    lastMouseY = e.y.toDouble()
                    lazyColumn[0].offset += deltaY
                    redraw()
                }
            }
        })
        canvas.addMouseWheelListener{ev->
            val rotation=ev.wheelRotation
            if (lazyColumn.isNotEmpty()) {
                lazyColumn[0].offset += -20*rotation
                redraw()
            }
        }
        canvas.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(p0: MouseEvent?) {
                super.mouseClicked(p0)
                val x = p0!!.x.toDouble()
                val y = p0.y.toDouble()
                handleClick(x, y)
            }

            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                lastMouseY = e!!.y.toDouble()
            }
        })

        Log.info("Graphical service initialized")
    }

    fun getSystemResource(path: String):String {
        return Paths.get("").toAbsolutePath().parent.parent.parent.toString()+"/res/"+path
    }

    //Sets the content of the screen. If itIsNewScreen=true, adds the screen to the navigation stack
    override fun setContent(itIsNewScreen: Boolean, lambda: MutableList<View>.() -> Unit) {
        viewTree.clear()
        lazyColumn.clear()
        viewTree.lambda()
        navigationLambda(viewTree)
        if (itIsNewScreen) {
            stack.push(lambda)
        }
    }

    //Adds UI on top of the current screen (such as a keyboard). Preserves the previous state
    override fun injectUI(lambda: MutableList<View>.() -> Unit) {
        viewTreeUntilInject.clear()
        viewTreeUntilInject.addAll(viewTree)
        viewTree.lambda()
    }

    //Removes the injected UI and restores the previous state
    override fun cancelInject() {
        viewTree.clear()
        bounds.clear()
        viewTree.addAll(viewTreeUntilInject)
    }

    //Return to the previous screen in the navigation stack
    override fun popBackStack() {
        if (stack.stackSize() <= 1) return
        stack.popBackStack()
        viewTree.clear()
        bounds.clear()
        viewTreeUntilInject.clear()
        val lambda = stack.peek()
        viewTree.lambda()
        navigationLambda(viewTree)
        canvas.display()
    }

    //Rerender screen must call after setContent or injectUI
    override fun redraw() {
        canvas.display()
    }

    //Searches for a clickable area by coordinates and calls onClick
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
        gl.glOrtho(0.0, SCREEN_WIDTH.toDouble(),SCREEN_HEIGHT.toDouble(),  0.0, -1.0, 1.0)
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
        gl.glFlush()
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