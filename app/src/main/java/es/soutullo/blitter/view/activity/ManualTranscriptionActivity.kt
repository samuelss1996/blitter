package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.view.adapter.data.ManualTranscriptionProduct

class ManualTranscriptionActivity : AppCompatActivity() {
    private lateinit var products: List<ManualTranscriptionProduct>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_transcription)
    }

    /**
     *
     */
    fun onFinishButtonClicked() {
        // TODO implement here
    }

    /**
     *
     */
    fun onAddProductButtonClicked() {
        // TODO implement here
    }

    /**
     * @param listIndex
     */
    fun onEditProductClicked(listIndex: Int) {
        // TODO implement here
    }

    /**
     * @param listIndex
     */
    fun onDeleteProductClicked(listIndex: Int) {
        // TODO implement here
    }

    /**
     * @param listIndex
     * @param editedProduct
     */
    fun onProductEdited(listIndex: Int, editedProduct: ManualTranscriptionProduct) {
        // TODO implement here
    }

    /**
     * @param listIndex
     */
    fun onDeleteProductConfirmed(listIndex: Int) {
        // TODO implement here
    }

    /**
     *
     */
    private fun onTryToAddProductWithEmptyField() {
        // TODO implement here
    }

    /**
     *
     */
    private fun onTryToFinishWithNoProducts() {
        // TODO implement here
    }

    /**
     * @param products
     * @return
     */
    private fun toBill(products: List<ManualTranscriptionProduct>): Bill? {
        // TODO implement here
        return null
    }

    /**
     * @param bill
     * @return
     */
    private fun toProducts(bill: Bill): List<ManualTranscriptionProduct>? {
        // TODO implement here
        return null
    }
}
