package es.soutullo.blitter.view.adapter.generic

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import es.soutullo.blitter.view.adapter.handler.IListHandler

/**
 *
 */
abstract class GenericListAdapter<Item> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val VIEW_TYPE_ITEM = 0
        private val VIEW_TYPE_LOADING = 1
    }

    private val items: List<Item>? = null
    private val handler: IListHandler? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int = 0

    /**
     * @param item
     */
    fun add(item: Item) {
        // TODO implement here
    }

    /**
     * @param index
     * @param item
     */
    fun add(index: Int, item: Item) {
        // TODO implement here
    }

    /**
     * @param index
     * @param newItem
     */
    fun update(index: Int, newItem: Item) {
        // TODO implement here
    }

    /**
     * @param index
     */
    fun remove(index: Int) {
        // TODO implement here
    }

    /**
     *
     */
    fun showLoadingMoreProgressBar() {
        // TODO implement here
    }

    /**
     *
     */
    fun hideLoadingMoreProgressBar() {
        // TODO implement here
    }

    /**
     *
     */
    private fun onScroll() {
        // TODO implement here
    }

    /**
     *
     */
    protected fun onLoadMore() {
        // TODO implement here
    }

    /**
     * @return
     */
    protected abstract fun getItemLayout(): Int

    /**
     * @param view
     * @return
     */
    protected abstract fun getViewHolder(view: View): RecyclerView.ViewHolder

    open inner class GenericListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}