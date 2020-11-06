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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.http.proxy.BinderModel
import com.http.proxy.ProxyBinder
import com.http.proxy.R

class ProxyConfigFragment : Fragment() {

    private lateinit var proxyConfigViewModel: ProxyConfigViewModel

    private lateinit var proxy: HookProxy

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        proxyConfigViewModel =
            ViewModelProvider(this).get(ProxyConfigViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_proxy, container, false)

        val proxyList = root.findViewById<RecyclerView>(R.id.rv_proxy_list)

        val adapter = ProxyConfigListAdapter(requireContext(), proxyConfigViewModel)
        proxyConfigViewModel.configList.observe(viewLifecycleOwner,
            Observer<Collection<HookProxy>> { t ->
                adapter.setProxyData(
                    t, null
                )
            })
        proxyList.adapter = adapter

        val tvConfig = root.findViewById<TextView>(R.id.tv_config)
        proxyConfigViewModel.config.observe(viewLifecycleOwner) {
            proxy = it
            tvConfig.text = it.toString()
        }

        val binderModel =
            ViewModelProvider(requireActivity()).get(BinderModel::class.java);
        binderModel.binder.observe(
            viewLifecycleOwner,
            Observer<ProxyBinder> { t ->
                proxyConfigViewModel.setAllProxy(t.getProxyList())
                val proxy = t.getCurrentProxy()
                adapter.setDefault(proxy)
                tvConfig.text = proxy.toString()
            })

        root.findViewById<View>(R.id.bt_sure).setOnClickListener {
            if (binderModel.setProxy(proxy)) {
                Toast.makeText(activity, R.string.setting_success, Toast.LENGTH_LONG).show()
            }
        }
        return root
    }
}