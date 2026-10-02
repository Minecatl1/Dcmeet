package dev.dcmeet.desktop

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

/** The app-facing contract for a bundled, audited Linux runtime. */
class DesktopController(private val context: Context) {
    enum class Engine(val title: String, val detail: String) {
        FEX("FEX-Emu", "Fast x86_64 userspace translation"),
        BOX64("Box64", "Lightweight x86_64 Linux compatibility")
    }

    data class Result(val ok: Boolean, val message: String)

    var engine = Engine.FEX
        private set

    fun selectEngine(value: Engine) { engine = value }

    fun inspect(uri: Uri): Result {
        val name = queryName(uri) ?: return Result(false, "Unable to read the selected file")
        val lower = name.lowercase()
        val kind = when {
            lower.endsWith(".deb") -> "Debian package"
            lower.endsWith(".tar.gz") || lower.endsWith(".tgz") -> "gzip tarball"
            lower.endsWith(".tar.xz") -> "xz tarball"
            lower.endsWith(".tar") -> "tarball"
            else -> return Result(false, "Only .deb, .tar, .tar.gz, .tgz, and .tar.xz are accepted")
        }
        return Result(true, "$kind ready: $name")
    }

    /**
     * A production build supplies this executable in app-private storage after signature
     * verification. We deliberately never execute an arbitrary file selected by the user.
     */
    fun runtimeStatus(): Result {
        val binary = File(context.filesDir, "runtime/${engine.name.lowercase()}")
        return if (binary.canExecute()) Result(true, "${engine.title} runtime is installed")
        else Result(false, "${engine.title} runtime not installed. Add a signed runtime bundle first.")
    }

    /**
     * Creates the only host directory that a production runtime may bind into its guest.
     * App-specific external storage needs no broad storage permission and works on modern
     * Android's scoped-storage model. Its guest mount target is `/mnt/shared`.
     */
    fun prepareSharedFolder(): Result {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return Result(false, "External storage is not mounted")
        }
        val folder = context.getExternalFilesDir("Dcmeet/Shared")
            ?: return Result(false, "No app external-storage directory is available")
        if (!folder.exists() && !folder.mkdirs()) {
            return Result(false, "Could not create the shared folder")
        }
        val runtimeDirectory = File(context.filesDir, "runtime")
        if (!runtimeDirectory.exists() && !runtimeDirectory.mkdirs()) {
            return Result(false, "Shared folder exists, but its runtime mount configuration could not be saved")
        }
        File(runtimeDirectory, "mounts.conf").writeText("${folder.absolutePath}:/mnt/shared:rw\n")
        return Result(true, "Shared folder ready at ${folder.absolutePath}; forwarded to /mnt/shared")
    }

    private fun queryName(uri: Uri): String? = uri.lastPathSegment?.substringAfterLast('/')
}
