package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityMainBinding
import io.github.kobakei.materialfabspeeddial.FabSpeedDial


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)

        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_activity_main, menu)
        return true
    }

    /** Gets called when the user clicks the manual transcription mini fab */
    private fun onManualTranscriptionClicked() {
        this.startActivity(Intent(this, ManualTranscriptionActivity::class.java))
    }

    /** Gets called when the user clicks the from gallery mini fab */
    private fun onFromGalleryClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the from camera mini fab */
    private fun onFromCameraClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the search button on the action bar */
    fun onSearchClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the select all checkbox on the action bar */
    fun onSelectAllClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the delete button on the action bar */
    fun onDeleteClicked() {
        // TODO implement here
    }

    /**
     * Gets called when the text present on the search fields changes, due to a user interaction
     * @param newText The new text of the search field
     */
    fun onSearchTextChanged(newText: String) {
        // TODO implement here
    }

    /** Initializes some fields of the activity */
    private fun init() {
        this.findViewById<FabSpeedDial>(R.id.fab).addOnMenuItemClickListener({ miniFab, label, itemId ->
            when(itemId) {
                R.id.fab_mini_transcribe -> this.onManualTranscriptionClicked()
                R.id.fab_mini_gallery -> this.onFromGalleryClicked()
                R.id.fab_mini_camera -> this.onFromCameraClicked()
            }
        })
    }
}
