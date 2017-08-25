package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.person.Person

class BillPersonTraceActivity : AppCompatActivity() {
    private lateinit var bill: Bill
    private lateinit var person: Person

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_person_trace)
    }
}
