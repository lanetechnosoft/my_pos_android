package com.farsheel.mypos.view.home

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.farsheel.mypos.R
import com.farsheel.mypos.data.model.ProductEntity
import com.farsheel.mypos.data.remote.ApiClient
import com.farsheel.mypos.databinding.HomeFragmentBinding
import com.farsheel.mypos.util.MyBounceInterpolator
import com.farsheel.mypos.util.SpacesItemDecoration
import com.farsheel.mypos.util.Util
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.layout_grid_item.view.*
import kotlinx.android.synthetic.main.layout_recycler_view.view.*
import org.koin.android.viewmodel.ext.android.viewModel


class HomeFragment : Fragment() {

    private var clearMenu: MenuItem? = null
    private lateinit var binding: HomeFragmentBinding
    private val homeViewModel: HomeViewModel by viewModel()

    companion object {
        private val productDiffCallBack = object : DiffUtil.ItemCallback<ProductEntity>() {
            override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
                oldItem.itemId == newItem.itemId

            override fun areContentsTheSame(
                oldItem: ProductEntity,
                newItem: ProductEntity
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        binding = HomeFragmentBinding.bind(
            inflater.inflate(
                R.layout.home_fragment,
                container,
                false
            )
        )
        return binding.root
    }

    fun bounceItem(view: View) {
        val myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce)
        val interpolator = MyBounceInterpolator(0.1, 25.0)
        myAnim.interpolator = interpolator
        view.startAnimation(myAnim)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = homeViewModel

        val gridLayoutManager = GridLayoutManager(context, 3)
        recyclerView.recyclerView.layoutManager = gridLayoutManager

        recyclerView.recyclerView.addItemDecoration(SpacesItemDecoration(3, 10))

        val adapter = ItemListAdapter()
        recyclerView.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("All").setTag(null))
        homeViewModel.selectCategory(null)
        homeViewModel.getCategoryList().observe(viewLifecycleOwner, Observer {
            tabLayout.removeAllTabs()
            tabLayout.addTab(tabLayout.newTab().setText("All").setTag(null))
            Observable.fromIterable(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { item ->
                    tabLayout.addTab(tabLayout.newTab().setTag(item.catId).setText(item.name))
                }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                homeViewModel.selectCategory(tab?.tag?.toString()?.toLong())
            }
        })

        homeViewModel.getProductList().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        homeViewModel.getCartTotal().observe(viewLifecycleOwner, Observer {
            if (it != null){
                cartBtn.text = getString(R.string.total, Util.currencyLocale(it))
            }
        })

        homeViewModel.navigateToCart.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                view?.findNavController()
                    ?.navigate(R.id.action_homeFragment_to_cartFragment)
            }
        })
        homeViewModel.getCartList().observe(viewLifecycleOwner, Observer {
            clearMenu?.isVisible = it.isNotEmpty()
            cartBtn?.isVisible = it.isNotEmpty()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == clearMenu?.itemId) {
            homeViewModel.clearCart()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.menu_clear_cart, menu)
        clearMenu = menu.findItem(R.id.action_clear_cart)
        clearMenu?.isVisible = !homeViewModel.getCartList().value.isNullOrEmpty()
        super.onCreateOptionsMenu(menu, inflater)
    }

    inner class ItemListAdapter :
        PagedListAdapter<ProductEntity, ItemListAdapter.ProductViewHolder>(productDiffCallBack) {

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val productEntity = getItem(position)

            with(holder) {
                bindTo(productEntity)
                productEntity?.let {
                    itemView.setOnClickListener {
                        bounceItem(holder.itemView)
                        homeViewModel.addToCart(productEntity)
                        holder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
            ProductViewHolder(parent)


        inner class ProductViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_grid_item,
                parent,
                false
            )
        ) {
            private var product: ProductEntity? = null

            fun bindTo(product: ProductEntity?) {
                this.product = product
                if (product != null) {
                    itemView.itemTv.text = product.name
                    itemView.itemTotalPriceTv.text = Util.currencyLocale(product.price)
                    if (product.image.isNullOrEmpty()) {
                        itemView.itemIv.visibility = View.GONE
                        itemView.let {
                            Glide.with(it)
                                .clear(itemView.itemIv)
                        }
                    } else {
                        itemView.itemIv.visibility = View.VISIBLE
                        itemView.let {
                            Glide.with(it)
                                .load(ApiClient.IMAGE_URL + product.image)
                                .into(itemView.itemIv)
                        }

                    }
                }
            }
        }
    }
}
