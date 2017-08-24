package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.person.BillPerson

class FinalResultActivity : AppCompatActivity() {
    private lateinit var bill: Bill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_result)
    }

    /**
     *
     */
    fun onSplitAgainClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onDoneClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onRenameClicked() {
        // TODO implement here
    }

    /**
     * @param newName
     */
    fun onRenamed(newName: String) {
        // TODO implement here
    }

    /**
     * @param listIndex
     * @param person
     */
    fun onBillPersonClicked(listIndex: Int, person: BillPerson) {
        // TODO implement here
    }
}
