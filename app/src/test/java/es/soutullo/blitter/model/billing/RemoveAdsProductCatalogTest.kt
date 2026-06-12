package es.soutullo.blitter.model.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveAdsProductCatalogTest {
    @Test
    fun productIdsContainsAllPlayConsoleRemoveAdsProducts() {
        assertEquals(
                listOf(
                        "es.soutullo.blitter.product.removeads.basicmeal",
                        "es.soutullo.blitter.product.removeads.soda",
                        "es.soutullo.blitter.product.removeads",
                        "es.soutullo.blitter.product.removeads.nicemeal",
                        "es.soutullo.blitter.product.removeads.cry"
                ),
                RemoveAdsProductCatalog.productIds
        )
    }

    @Test
    fun containsAnyAcceptsAnyRemoveAdsProduct() {
        RemoveAdsProductCatalog.productIds.forEach { productId ->
            assertTrue(RemoveAdsProductCatalog.containsAny(listOf(productId)))
        }
    }

    @Test
    fun containsAnyRejectsUnrelatedProducts() {
        assertFalse(RemoveAdsProductCatalog.containsAny(listOf("es.soutullo.blitter.product.other")))
    }

    @Test
    fun productOptionDisplayNameUsesContributionLabelWhenPresent() {
        val option = RemoveAdsProductOption(
                productId = RemoveAdsProductCatalog.COFFEE,
                name = "Remove all the ads and waits (Buy me a coffee)",
                formattedPrice = "EUR 0.99",
                priceAmountMicros = 990_000L
        )

        assertEquals("Buy me a coffee", option.displayName)
    }

    @Test
    fun productOptionDisplayNameFallsBackToPlayName() {
        val option = RemoveAdsProductOption(
                productId = RemoveAdsProductCatalog.COFFEE,
                name = "Remove ads",
                formattedPrice = "EUR 0.99",
                priceAmountMicros = 990_000L
        )

        assertEquals("Remove ads", option.displayName)
    }
}
