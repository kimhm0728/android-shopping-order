package woowacourse.shopping.domain.model

data class CartItem(
    val id: Int = 0,
    val productId: Int,
    var quantity: Quantity,
)
