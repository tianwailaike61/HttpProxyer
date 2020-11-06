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

import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketAddress

open class HookProxy : Proxy, Parcelable {
    @Volatile
    open var type: Type = Type.DIRECT

    @Volatile
    open var sa: SocketAddress? = null

    private constructor() : super(Type.HTTP, InetSocketAddress("127.0.0.1", 8080))

    constructor(ip: String, port: Int): super(Type.HTTP, InetSocketAddress(ip, port)) {
        this.type = Type.HTTP
        this.sa = super.address()
    }

    constructor(proxy: HookProxy) : this() {
        this.type = proxy.type
        this.sa = proxy.sa
    }

    constructor(parcel: Parcel) : this() {
        val i = parcel.readInt()
        type = when (i) {
            Type.DIRECT.ordinal -> {
                Type.DIRECT
            }
            Type.HTTP.ordinal -> {
                Type.HTTP
            }
            Type.SOCKS.ordinal -> {
                Type.SOCKS
            }
            else -> {
                throw UnknownError("")
            }
        }
        val b = parcel.readBoolean()
        if (b) {
            val bytes = parcel.createByteArray()
            ByteArrayInputStream(bytes).use { bais ->
                ObjectInputStream(bais).use {
                    sa = it.readObject() as SocketAddress?
                }
            }
        }
    }

    open fun set(proxy: HookProxy) {
        type = proxy.type()
        sa = proxy.address()
    }

    override fun type(): Type {
        return type
    }

    override fun address(): SocketAddress? {
        return sa
    }

    override fun toString(): String {
        if (type == Type.DIRECT) {
            return "Direct"
        }
        return "${type.name}${sa.toString()}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type.ordinal)
        if (sa == null) {
            parcel.writeBoolean(false)
        } else {
            parcel.writeBoolean(true)
            ByteArrayOutputStream().use { baos ->
                ObjectOutputStream(baos).use { oos ->
                    oos.writeObject(sa)
                }
                parcel.writeByteArray(baos.toByteArray())
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val EMPTY = object : HookProxy() {
            override var type: Type = Type.DIRECT
                set(_) {
                    throw IllegalStateException("can not set type")
                }
            override var sa: SocketAddress? = null
                set(_) {
                    throw IllegalStateException("can not set sa")
                }

            override fun set(proxy: HookProxy) {
                throw IllegalStateException("can not call set method")
            }
        }

        val CREATOR = object : Parcelable.Creator<HookProxy> {
            override fun createFromParcel(parcel: Parcel): HookProxy {
                return HookProxy(parcel)
            }

            override fun newArray(size: Int): Array<HookProxy?> {
                return arrayOfNulls(size)
            }
        }
    }

}
