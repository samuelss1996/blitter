package es.soutullo.blitter.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import es.soutullo.blitter.R
import es.soutullo.blitter.view.util.EdgeToEdge
import es.soutullo.blitter.view.util.EdgeToEdgeMode
import es.soutullo.blitter.view.util.EdgeToEdgeStatusBarColor

// TODO fix code repetition concerning this and main activities
@EdgeToEdge(mode = EdgeToEdgeMode.APP_INTRO, statusBarColor = EdgeToEdgeStatusBarColor.CAMERA_INTRO)
class CameraIntroActivity : ABlitterIntroActivity() {
    override val titleId = R.string.camera_intro_title
    override val descriptionId = R.string.camera_intro_description
    override val drawableId = R.drawable.ic_camera_alt_white_128dp
    override val mainColorId = R.color.md_teal_600
    override val barColorId = R.color.md_teal_800
    override val preferenceKey = FLAG_CAMERA_INTRO_VIEWED

    companion object {
        val FLAG_CAMERA_INTRO_VIEWED = "FLAG_INTRO_CAMERA"
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            this.startActivity(Intent(this, OcrCaptureActivity::class.java))
            this.finish()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MainActivity.PERMISSIONS_REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            MainActivity.PERMISSIONS_REQUEST_CAMERA -> {
                if(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    this.onDonePressed(null)
                }
            }
        }
    }
}
