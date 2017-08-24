package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill

class BillSummaryActivity : AppCompatActivity() {
    private lateinit var bill: Bill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_summary)
    }

    /**
     *
     */
    fun onConfirmClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onAmendClicked() {
        // TODO implement here
    }
}
