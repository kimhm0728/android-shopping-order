package woowacourse.shopping.data.cart.remote

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import woowacourse.shopping.data.product.remote.DataCallback
import woowacourse.shopping.data.remote.RetrofitClient.retrofitApi
import woowacourse.shopping.domain.model.CartItem
import woowacourse.shopping.domain.model.Quantity
import kotlin.concurrent.thread

class RemoteCartRepository {
    fun findByProductId(
        productId: Int,
        totalItemCount: Int,
        dataCallback: DataCallback<CartItem?>,
    ) {
        retrofitApi.requestCartItems(page = 0, size = totalItemCount)
            .enqueue(
                object : Callback<CartResponse> {
                    override fun onResponse(
                        call: Call<CartResponse>,
                        response: Response<CartResponse>,
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body() ?: return
                            dataCallback.onSuccess(
                                body.toCartItems().find {
                                    it.productId == productId
                                },
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<CartResponse>,
                        t: Throwable,
                    ) {
                        dataCallback.onFailure(t)
                    }
                },
            )
    }

    fun getAllCartItem(dataCallback: DataCallback<List<CartItem>>) {
        retrofitApi.requestCartItems(page = 0, size = MAX_CART_ITEM_COUNT)
            .enqueue(
                object : Callback<CartResponse> {
                    override fun onResponse(
                        call: Call<CartResponse>,
                        response: Response<CartResponse>,
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body() ?: return
                            dataCallback.onSuccess(body.toCartItems())
                        }
                    }

                    override fun onFailure(
                        call: Call<CartResponse>,
                        t: Throwable,
                    ) {
                        dataCallback.onFailure(t)
                    }
                },
            )
    }

    fun deleteCartItem(
        id: Int,
        dataCallback: DataCallback<Unit>,
    ) {
        retrofitApi.deleteCartItem(id = id).enqueue(
            object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>,
                ) {
                    if (response.isSuccessful) {
                        dataCallback.onSuccess(Unit)
                    }
                }

                override fun onFailure(
                    call: Call<Unit>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    fun setCartItemQuantity(
        id: Int,
        quantity: Quantity,
        dataCallback: DataCallback<Unit>,
    ) {
        retrofitApi.setCartItemQuantity(id = id, quantity = CartItemQuantityRequest(quantity.count))
            .enqueue(
                object : Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>,
                    ) {
                        if (response.isSuccessful) {
                            dataCallback.onSuccess(Unit)
                            return
                        }
                    }

                    override fun onFailure(
                        call: Call<Unit>,
                        t: Throwable,
                    ) {
                        dataCallback.onFailure(t)
                    }
                },
            )
    }

    fun syncFindByProductId(
        productId: Int,
        totalItemCount: Int,
    ): CartItem? {
        var cartItem: CartItem? = null
        thread {
            val response = retrofitApi.requestCartItems(page = 0, size = totalItemCount).execute()
            val body = response.body()
            cartItem = body?.toCartItems()?.firstOrNull { productId == it.productId }
        }.join()
        return cartItem
    }

    fun syncGetCartQuantityCount(): Int {
        var cartQuantityCount = 0
        thread {
            val response = retrofitApi.requestCartQuantityCount().execute()
            val body = response.body()?.quantity
            cartQuantityCount = body ?: 0
        }.join()
        return cartQuantityCount
    }

    fun getCartQuantityCount(dataCallback: DataCallback<Int>) {
        retrofitApi.requestCartQuantityCount().enqueue(
            object : Callback<CountResponse> {
                override fun onResponse(
                    call: Call<CountResponse>,
                    response: Response<CountResponse>,
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        dataCallback.onSuccess(body.quantity)
                    }
                }

                override fun onFailure(
                    call: Call<CountResponse>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    fun addCartItem(
        productId: Int,
        quantity: Quantity = Quantity(1),
        dataCallback: DataCallback<Unit>,
    ) {
        retrofitApi.requestCartQuantityCount(
            addCartItemRequest =
                AddCartItemRequest(
                    productId,
                    quantity.count,
                ),
        ).enqueue(
            object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>,
                ) {
                    if (response.isSuccessful) {
                        dataCallback.onSuccess(Unit)
                    }
                }

                override fun onFailure(
                    call: Call<Unit>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    companion object {
        private const val MAX_CART_ITEM_COUNT = 9999999
    }
}
