import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton

class FloatingControlService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_control, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = 100
        params.y = 100

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        // Evento táctil para arrastrar la ventana por la pantalla
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }

        // Vincular clics de los botones a acciones del VPN
        floatingView?.findViewById<ImageButton>(R.id.btnFreezer)?.setOnClickListener {
            sendVpnCommand(MyVpnService.ACTION_TOGGLE_FREEZER)
        }

        floatingView?.findViewById<ImageButton>(R.id.btnTeleport)?.setOnClickListener {
            sendVpnCommand(MyVpnService.ACTION_TOGGLE_TELEPORT)
        }

        floatingView?.findViewById<ImageButton>(R.id.btnBlinker)?.setOnClickListener {
            sendVpnCommand(MyVpnService.ACTION_TOGGLE_BLINKER)
        }

        windowManager?.addView(floatingView, params)
    }

    private fun sendVpnCommand(actionName: String) {
        val intent = Intent(this, MyVpnService::class.java).apply {
            action = actionName
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
    }
}
