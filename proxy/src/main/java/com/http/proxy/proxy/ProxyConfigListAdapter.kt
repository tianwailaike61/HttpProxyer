/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.http.proxy.proxy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.http.proxy.R

class ProxyConfigListAdapter constructor(
    private val context: Context,
    private val proxyConfigViewModel: ProxyConfigViewModel
) :
    RecyclerView.Adapter<ProxyViewHolder>() {
    private var current = HookProxy.EMPTY

    private val proxyList by lazy {
        ArrayList<HookProxy>()
    }

    private val listener by lazy {
        View.OnClickListener {
            val proxy = it.tag as HookProxy
            if (proxy == current) {
                return@OnClickListener
            }
            current = proxy
            notifyDataSetChanged()
            proxyConfigViewModel.setSelectProxy(current)
        }
    }

    fun setProxyData(proxys: Collection<HookProxy>, default: HookProxy?) {
        proxyList.clear()
        proxyList.addAll(proxys)
        default?.let {
            current = it
        }
        notifyDataSetChanged()
    }

    fun setDefault(default: HookProxy?) {
        default?.let {
            current = it
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_proxy, parent, false)
        return ProxyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProxyViewHolder, position: Int) {
        holder.itemView.setOnClickListener(listener)
        val proxy = proxyList[position]
        holder.itemView.tag = proxy

        holder.contentView.text = proxy.toString()
        if (proxy == current) {
            holder.contentView.setTextColor(context.resources.getColor(R.color.teal_700))
        } else {
            holder.contentView.setTextColor(context.resources.getColor(R.color.black))
        }
    }

    override fun getItemCount(): Int = proxyList.size
}

class ProxyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val contentView: TextView = itemView.findViewById(R.id.tv_proxy)
}