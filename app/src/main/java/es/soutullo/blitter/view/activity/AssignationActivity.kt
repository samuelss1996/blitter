package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.person.BillPerson

class AssignationActivity : AppCompatActivity() {
    private lateinit var bill: Bill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignation)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    fun onFinishButtonClicked() {
        // TODO implement here
    }

    /**
     * @param tipPercentage
     */
    fun onTipPercentageConfirmed(tipPercentage: Float) {
        // TODO implement here
    }

    /**
     * @param billLinesToAssign
     */
    fun onAssignClicked(billLinesToAssign: List<BillLine>) {
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
    fun onAddNewPersonClicked() {
        // TODO implement here
    }

    /**
     * @param newPersonName
     */
    fun onNewPersonAdded(newPersonName: String) {
        // TODO implement here
    }

    /**
     * @param affectedBillLines
     * @param assignedPersons
     * @param unassignedPersons
     */
    fun onAssignationDone(affectedBillLines: List<BillLine>, assignedPersons: List<BillPerson>, unassignedPersons: List<BillPerson>) {
        // TODO implement here
    }

    /**
     *
     */
    private fun onTryToFinishWithMissingAssignation() {
        // TODO implement here
    }
}
