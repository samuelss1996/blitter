package es.soutullo.blitter.view.dialog

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import com.xw.repo.BubbleSeekBar
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.DialogTipBinding
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import es.soutullo.blitter.view.util.BlitterUtils

/** Dialog shown to the user to let him/her choose the tip percent */
class TipDialog(context: Context, handler: IDialogHandler?, private val bill: Bill)
        : CustomLayoutDialog(context, handler, context.getString(R.string.dialog_tip_title)) {
    private lateinit var binding: DialogTipBinding

    override fun getCustomView(): View {
        this.binding = DataBindingUtil.inflate<DialogTipBinding>(LayoutInflater.from(this.context), R.layout.dialog_tip, null, false)

        this.binding.utils = BlitterUtils
        this.binding.bill = this.bill
        this.binding.tipPercent = 0f

        with(this.binding.root.findViewById<BubbleSeekBar>(R.id.dialog_tip_seek_bar)) {
            this.setProgress(this@TipDialog.binding.tipPercent * 100)
            this.onProgressChangedListener = this@TipDialog.createSeekBarListener()
        }

        return this.binding.root
    }

    /** @return The tip percent chosen by the user */
    fun getTipPercent(): Float? = this.binding.tipPercent

    /** Creates the listener which manages changes on the seek bar value */
    private fun createSeekBarListener(): BubbleSeekBar.OnProgressChangedListener {
        return object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(progress: Int, progressFloat: Float) {
                this@TipDialog.binding.tipPercent = progress / 100f
            }

            override fun getProgressOnActionUp(progress: Int, progressFloat: Float) { }
            override fun getProgressOnFinally(progress: Int, progressFloat: Float) { }
        }
    }

    override fun getPositiveText(): String = this.context.getString(R.string.generic_dialog_continue)
    override fun getNegativeText(): String? = this.context.getString(R.string.generic_dialog_cancel)
}