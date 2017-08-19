package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import es.soutullo.blitter.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
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
