package service

interface NetworkService {
    fun isAddressReachable(address: String, timeOut: Int = 3000): Boolean
}