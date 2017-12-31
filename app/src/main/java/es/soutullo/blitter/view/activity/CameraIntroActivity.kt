package es.soutullo.blitter.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import es.soutullo.blitter.R

// TODO fix code repetition concerning this and main activities
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            MainActivity.PERMISSIONS_REQUEST_CAMERA -> {
                if(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    this.onDonePressed(null)
                }
            }
        }
    }
}