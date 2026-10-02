package dev.dcmeet.desktop

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var controller: DesktopController
    private lateinit var status: TextView
    private val importPackage = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = DesktopController(this)
        setContentView(desktop())
    }

    private fun desktop(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 34, 40, 30)
            setBackgroundColor(Color.rgb(10, 10, 15))
        }
        root.addView(text("DCMEET", 28, Color.WHITE))
        root.addView(text("A contained Linux desktop for Android", 15, Color.rgb(180, 178, 196)).apply {
            setPadding(0, 2, 0, 30)
        })
        root.addView(card("Linux desktop", "Launch a lightweight Wayland/X11 session inside an isolated rootfs."))
        root.addView(space(16))
        root.addView(text("EXECUTION ENGINE", 12, Color.rgb(162, 152, 255)))
        val engines = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setPadding(0, 10, 0, 16)
        }
        DesktopController.Engine.entries.forEachIndexed { index, item ->
            engines.addView(RadioButton(this).apply {
                text = item.title
                setTextColor(Color.WHITE)
                id = 100 + index
                isChecked = index == 0
            })
        }
        engines.setOnCheckedChangeListener { _, id ->
            controller.selectEngine(DesktopController.Engine.entries[id - 100])
            showStatus(controller.runtimeStatus())
        }
        root.addView(engines)
        root.addView(button("Check runtime") { showStatus(controller.runtimeStatus()) })
        root.addView(space(18))
        root.addView(text("SHARED FOLDER", 12, Color.rgb(162, 152, 255)))
        root.addView(text("Create an app-owned folder on external storage and bind it into the Linux desktop as /mnt/shared. Copy files here from Android, then access them from the guest.", 15, Color.rgb(210, 208, 220)).apply { setPadding(0, 10, 0, 12) })
        root.addView(button("Create shared folder") { showStatus(controller.prepareSharedFolder()) })
        root.addView(space(18))
        root.addView(text("PACKAGE CENTER", 12, Color.rgb(162, 152, 255)))
        root.addView(text("Use apt inside the Debian rootfs or Flatpak when the selected image includes it. Import local Debian and tar archives through the signed runtime installer.", 15, Color.rgb(210, 208, 220)).apply { setPadding(0, 10, 0, 12) })
        root.addView(button("Import .deb or tar archive") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }, importPackage)
        })
        status = text("Choose a runtime bundle to begin.", 14, Color.rgb(200, 195, 255)).apply {
            setPadding(0, 28, 0, 0)
        }
        root.addView(status)
        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        root.addView(text("No Android system-call conversion is performed. Apps run in the Linux guest through the selected user-space engine.", 12, Color.rgb(145, 142, 160)))
        return ScrollView(this).apply { addView(root) }
    }

    @Deprecated("Deprecated in Android; retained for API 26 support")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == importPackage && resultCode == RESULT_OK && data?.data != null) showStatus(controller.inspect(data.data!!))
    }

    private fun showStatus(result: DesktopController.Result) {
        status.setTextColor(if (result.ok) Color.rgb(134, 239, 172) else Color.rgb(253, 186, 116))
        status.text = result.message
    }
    private fun text(value: String, size: Int, color: Int) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
    }
    private fun button(label: String, click: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { click() }
    }
    private fun space(height: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, height) }
    private fun card(title: String, body: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(26, 20, 26, 20)
        setBackgroundColor(Color.rgb(28, 27, 38))
        addView(text(title, 18, Color.WHITE))
        addView(text(body, 14, Color.rgb(205, 202, 215)).apply { setPadding(0, 6, 0, 0) })
    }
}
