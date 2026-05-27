package impl

import service.NetworkService
import java.net.InetAddress

object NetworkServiceImpl : NetworkService {
    override fun isAddressReachable(address: String, timeOut: Int): Boolean {
        try {
            val inet=InetAddress.getByName(address)
            return inet.isReachable(timeOut)
        } catch (e: Exception) {
            return false
        }
    }
}