package woowacourse.shopping.data.product.remote.retrofit

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import woowacourse.shopping.data.remote.RetrofitClient.retrofitApi
import woowacourse.shopping.domain.model.Product
import kotlin.concurrent.thread

class RemoteProductRepository2 {
    fun findProducts(
        page: Int,
        pageSize: Int,
        dataCallback: DataCallback<List<Product>>,
    ) {
        retrofitApi.requestProducts(page = page, size = pageSize).enqueue(
            object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>,
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        dataCallback.onSuccess(body.toProductList())
                    }
                }

                override fun onFailure(
                    call: Call<ProductResponse>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    fun getIsPageLast(
        page: Int,
        pageSize: Int,
        dataCallback: DataCallback<Boolean>,
    ) {
        retrofitApi.requestProducts(page = page, size = pageSize).enqueue(
            object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>,
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        dataCallback.onSuccess(body.last)
                    }
                }

                override fun onFailure(
                    call: Call<ProductResponse>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    fun find(
        id: Int,
        dataCallback: DataCallback<Product>,
    ) {
        retrofitApi.requestProduct(id = id).enqueue(
            object : Callback<Content> {
                override fun onResponse(
                    call: Call<Content>,
                    response: Response<Content>,
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        dataCallback.onSuccess(body.toProduct())
                    }
                }

                override fun onFailure(
                    content: Call<Content>,
                    t: Throwable,
                ) {
                    dataCallback.onFailure(t)
                }
            },
        )
    }

    fun syncFind(id: Int): Product {
        var product: Product? = null
        thread {
            val response = retrofitApi.requestProduct(id = id).execute()
            val body = response.body()
            product = body?.toProduct()
        }.join()

        return product!!
    }
}
