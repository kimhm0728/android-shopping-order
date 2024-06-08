package woowacourse.shopping.domain.model

import woowacourse.shopping.domain.model.Coupon.Companion.INVALID_DISCOUNT_MESSAGE
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime

class PercentageCoupon(
    override val id: Int,
    override val code: String,
    override val description: String,
    override val expirationDate: LocalDate,
    private val discount: Int,
    private val availableTime: AvailableTime,
) : Coupon {
    override fun available(
        cartItems: List<CartItem>,
        currentDateTime: LocalDateTime,
    ): Boolean {
        if (isExpired(currentDateTime.toLocalDate())) return false
        return availableTime.available(currentDateTime.toLocalTime())
    }

    override fun discountPrice(cartItems: List<CartItem>): Int {
        if (!available(cartItems)) {
            throw IllegalArgumentException(INVALID_DISCOUNT_MESSAGE)
        }
        return (totalOrderPrice(cartItems) * (discount.toDouble() / 100)).toInt()
    }
}
