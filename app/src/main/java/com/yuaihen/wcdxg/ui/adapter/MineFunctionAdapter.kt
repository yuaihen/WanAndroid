package com.yuaihen.wcdxg.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yuaihen.wcdxg.base.Constants
import com.yuaihen.wcdxg.databinding.MineFunctionItemBinding
import com.yuaihen.wcdxg.net.model.MineMenuModel
import com.yuaihen.wcdxg.ui.activity.MyCollectActivity
import com.yuaihen.wcdxg.utils.ToastUtil
import com.yuaihen.wcdxg.utils.gone
import com.yuaihen.wcdxg.viewbinding.BaseBindingViewHolder
import com.yuaihen.wcdxg.viewbinding.getViewHolder

/**
 * Created by Yuaihen.
 * on 2021/6/15
 * 我的页面 功能菜单列表
 */
class MineFunctionAdapter(private val menuList: MutableList<MineMenuModel>) :
    RecyclerView.Adapter<BaseBindingViewHolder<MineFunctionItemBinding>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseBindingViewHolder<MineFunctionItemBinding> {
        return parent.getViewHolder(MineFunctionItemBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: BaseBindingViewHolder<MineFunctionItemBinding>,
        position: Int
    ) {
        holder.mBinding.apply {
            if (position == menuList.size - 1) {
                viewLine.gone()
            }

            val data = menuList[position]
            tvFunctionName.text = data.menuName
            data.menuIconResId?.let {
                ivIcon.setBackgroundResource(it)
            }

            llRoot.setOnClickListener {
                openPageById(data.menuId, holder.itemView.context)
            }
        }
    }

    private fun openPageById(id: Int, context: Context) {
        when (id) {
            Constants.ID_MY_COLLECT -> context.startActivity(
                Intent(
                    context,
                    MyCollectActivity::class.java
                )
            )
            Constants.ID_MY_COLLECT2 -> ToastUtil.show("开发中")
            Constants.ID_MY_COLLECT3 -> ToastUtil.show("开发中")
            Constants.ID_MY_COLLECT4 -> ToastUtil.show("开发中")
            Constants.ID_MY_COLLECT5 -> ToastUtil.show("开发中")
            Constants.ID_MY_COLLECT6 -> ToastUtil.show("开发中")
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

}