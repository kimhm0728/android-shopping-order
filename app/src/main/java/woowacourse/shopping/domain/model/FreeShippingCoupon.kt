package woowacourse.shopping.domain.model

import java.lang.IllegalArgumentException
import java.time.LocalDate

class FreeShippingCoupon(
    override val id: Int,
    override val code: String,
    override val description: String,
    override val expirationDate: LocalDate,
    val minimumPrice: Int,
) : Coupon {
    override fun available(cartItems: List<CartItem>): Boolean {
        return totalOrderPrice(cartItems) >= minimumPrice
    }

    override fun discountPrice(cartItems: List<CartItem>): Int {
        if (available(cartItems)) {
            throw IllegalArgumentException(INVALID_DISCOUNT)
        }

        return Coupon.DELIVERY_FEE
    }

    private fun totalOrderPrice(cartItems: List<CartItem>): Int {
        return cartItems.sumOf { it.totalPrice() }
    }

    companion object {
        private const val INVALID_DISCOUNT = "적용할 수 없는 쿠폰입니다."
    }
}
