package com.twlk.httpserver.utils

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.regex.Pattern

object NetUtils {

    /**
     * Ipv4 address check.
     */
    private val IPV4_PATTERN: Pattern = Pattern.compile(
        "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
    )

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     *
     * @return True if the input parameter is a valid IPv4 address.
     */
    fun isIPv4Address(input: String?): Boolean {
        return IPV4_PATTERN.matcher(input).matches()
    }

    /**
     * Get local Ip address.
     */
    fun getLocalIPAddress(): InetAddress? {
        var enumeration: Enumeration<NetworkInterface?>? = null
        try {
            enumeration = NetworkInterface.getNetworkInterfaces()
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                val nif: NetworkInterface = enumeration.nextElement()!!
                val inetAddresses: Enumeration<InetAddress> = nif.getInetAddresses()
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress: InetAddress = inetAddresses.nextElement()
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress
                        }
                    }
                }
            }
        }
        return null
    }
}