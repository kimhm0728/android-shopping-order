package woowacourse.shopping.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import woowacourse.shopping.common.Event
import woowacourse.shopping.domain.model.DataCallback
import woowacourse.shopping.domain.model.Product
import woowacourse.shopping.domain.repository.CartRepository
import woowacourse.shopping.domain.repository.ProductRepository
import woowacourse.shopping.domain.repository.RecentProductRepository
import woowacourse.shopping.ui.products.adapter.type.ProductUiModel
import woowacourse.shopping.ui.utils.AddCartQuantityBundle

class ProductDetailViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val recentProductRepository: RecentProductRepository,
    private val cartRepository: CartRepository,
    private val lastSeenProductVisible: Boolean,
) : ViewModel() {
    private val _productUiModel = MutableLiveData<ProductUiModel>()
    val productUiModel: LiveData<ProductUiModel> get() = _productUiModel

    private val _productLoadError = MutableLiveData<Event<Unit>>()
    val productLoadError: LiveData<Event<Unit>> get() = _productLoadError

    private val _isSuccessAddCart = MutableLiveData<Event<Boolean>>()
    val isSuccessAddCart: LiveData<Event<Boolean>> get() = _isSuccessAddCart

    val addCartQuantityBundle: LiveData<AddCartQuantityBundle> =
        _productUiModel.map {
            AddCartQuantityBundle(
                productId = it.productId,
                quantity = it.quantity,
                onIncreaseProductQuantity = { increaseQuantity() },
                onDecreaseProductQuantity = { decreaseQuantity() },
            )
        }

    private val _lastRecentProduct = MutableLiveData<LastRecentProductUiModel>()
    val lastRecentProduct: LiveData<LastRecentProductUiModel> get() = _lastRecentProduct

    val isVisibleLastRecentProduct: LiveData<Boolean> =
        _lastRecentProduct.map { !lastSeenProductVisible && it.productId != _productUiModel.value?.productId }

    init {
        saveRecentProduct()
    }

    fun loadProductDetail() {
        loadProduct()
        loadLastRecentProduct()
    }

    private fun loadProduct() {
        productRepository.find(
            productId,
            dataCallback =
                object : DataCallback<Product> {
                    override fun onSuccess(result: Product) {
                        _productUiModel.postValue(result.toProductUiModel())
                    }

                    override fun onFailure(t: Throwable) {
                        setError()
                    }
                },
        )
    }

    private fun Product.toProductUiModel(): ProductUiModel {
        val totalQuantityCount = cartRepository.syncGetTotalQuantity()
        val cartItem =
            cartRepository.syncFindByProductId(id, totalQuantityCount)
                ?: return ProductUiModel.from(this)
        return ProductUiModel.from(this, cartItem.quantity)
    }

    private fun loadLastRecentProduct() {
        val lastRecentProduct = recentProductRepository.findLastOrNull() ?: return
        productRepository.find(
            lastRecentProduct.product.id,
            object : DataCallback<Product> {
                override fun onSuccess(result: Product) {
                    _lastRecentProduct.postValue(LastRecentProductUiModel(result.id, result.name))
                }

                override fun onFailure(t: Throwable) {
                    setError()
                }
            },
        )
    }

    private fun saveRecentProduct() {
        val product = productRepository.syncFind(productId)
        recentProductRepository.save(product)
    }

    private fun increaseQuantity() {
        var quantity = _productUiModel.value?.quantity ?: return
        _productUiModel.value = _productUiModel.value?.copy(quantity = ++quantity)
    }

    private fun decreaseQuantity() {
        var quantity = _productUiModel.value?.quantity ?: return
        _productUiModel.value = _productUiModel.value?.copy(quantity = --quantity)
    }

    fun addCartProduct() {
        val productUiModel = _productUiModel.value ?: return
        val cartTotalCount = cartRepository.syncGetTotalQuantity()
        val cartItem = cartRepository.syncFindByProductId(productUiModel.productId, cartTotalCount)

        val addCartDataCallback =
            object : DataCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    _isSuccessAddCart.postValue(Event(true))
                }

                override fun onFailure(t: Throwable) {
                    _isSuccessAddCart.postValue(Event(false))
                }
            }

        if (cartItem == null) {
            cartRepository.add(
                productId = productId,
                quantity = productUiModel.quantity,
                dataCallback = addCartDataCallback,
            )
            return
        }
        cartRepository.changeQuantity(cartItem.id, productUiModel.quantity, addCartDataCallback)
    }

    private fun setError() {
        _productLoadError.postValue(Event(Unit))
    }
}
