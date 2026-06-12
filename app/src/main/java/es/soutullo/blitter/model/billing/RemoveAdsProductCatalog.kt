package es.soutullo.blitter.model.billing

object RemoveAdsProductCatalog {
    const val BASIC_MEAL = "es.soutullo.blitter.product.removeads.basicmeal"
    const val SODA = "es.soutullo.blitter.product.removeads.soda"
    const val COFFEE = "es.soutullo.blitter.product.removeads"
    const val NICE_MEAL = "es.soutullo.blitter.product.removeads.nicemeal"
    const val CRY = "es.soutullo.blitter.product.removeads.cry"

    val productIds = listOf(
            BASIC_MEAL,
            SODA,
            COFFEE,
            NICE_MEAL,
            CRY
    )

    fun containsAny(products: List<String>): Boolean {
        return products.any { it in productIds }
    }
}

data class RemoveAdsProductOption(
        val productId: String,
        val name: String,
        val formattedPrice: String,
        val priceAmountMicros: Long
) {
    val displayName: String
        get() = CONTRIBUTION_LABEL_REGEX.find(name)?.groupValues?.get(1) ?: name

    companion object {
        private val CONTRIBUTION_LABEL_REGEX = Regex("\\(([^)]+)\\)")
    }
}
