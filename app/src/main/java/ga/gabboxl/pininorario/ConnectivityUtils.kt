package ga.gabboxl.pininorario

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectivityUtils {
    companion object {
        var isInternetAvailable: MutableLiveData<Boolean> = MutableLiveData(false)

        fun init(context: Context) {
         val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


         val networkCallbacks = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isInternetAvailable.postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isInternetAvailable.postValue(false)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                isInternetAvailable.postValue(false)
            }
        }



            val network =
                connectivityManager.activeNetworkInfo //uso activeNetworkInfo al posto di activeNetwork perche la api minima per cui buildo e minore di 23
            if (network == null) {
                isInternetAvailable.postValue(false)
            }

            val requestBuilder = NetworkRequest.Builder().apply {
                addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            }.build()

            connectivityManager.registerNetworkCallback(requestBuilder, networkCallbacks)
        }

       /* override fun onActive() {
            super.onActive()
            checkInternet()
        }

        override fun onInactive() {
            super.onInactive()
            connectivityManager.unregisterNetworkCallback(networkCallbacks)
        }*/
    }
}

