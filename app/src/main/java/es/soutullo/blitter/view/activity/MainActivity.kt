package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import es.soutullo.blitter.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        this.setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_activity_main, menu)
        return true
    }

    fun onManualTranscriptionClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onFromGalleryClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onFromCameraClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onSearchClicked() {
        // TODO implement here
    }

    /**
     * @param newText
     */
    fun onSearchTextChanged(newText: String) {
        // TODO implement here
    }

    /**
     *
     */
    fun onSelectAllClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onDeleteClicked() {
        // TODO implement here
    }
}
