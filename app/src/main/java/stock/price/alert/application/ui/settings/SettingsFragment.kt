package stock.price.alert.application.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import stock.price.alert.application.R
import stock.price.alert.application.service.RealTimePriceAlert.RealTimePriceAlertService

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)
        val preferencesManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferencesManager.registerOnSharedPreferenceChangeListener(priceAlertPrefListener)
        preferencesManager.registerOnSharedPreferenceChangeListener(priceAlertIntervalListener)
        val listPreference : ListPreference = findPreference("PricecCheckInterval")!!
        listPreference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    }

    private val priceAlertPrefListener =
        SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences: SharedPreferences, key: String ->
            if (key == requireContext().getString(R.string.setting_price_check_alert)) {
                val isAlertOn = sharedPreferences.getBoolean(requireContext().getString(R.string.setting_price_check_alert), false)
                if (isAlertOn) {
                    Log.d("SETTING", "price alert turned on")
                    val interval : Int =
                        sharedPreferences.getString(requireContext().getString(R.string.setting_price_check_interval), "")!!
                            .split(" ")[0].toInt()
                    Log.d("SETTING", interval.toString())
                    RealTimePriceAlertService.SetServiceAlarm(requireContext(), interval)
                } else {
                    Log.d("SETTING", "price alert turned off")
                    RealTimePriceAlertService.UnsetServiceAlarm(requireContext())
                }
            }
        }

    private val priceAlertIntervalListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
        if (key == requireContext().getString(R.string.setting_price_check_interval)) {
            val interval : Int = sharedPreferences.getString(
                requireContext().getString(R.string.setting_price_check_interval), "")!!
                .split(" ")[0].toInt()
            Log.d("SETTING", interval.toString())
            RealTimePriceAlertService.SetServiceAlarm(requireContext(), interval)
        }
    }
}

    //override fun onCreateView(
    //        inflater: LayoutInflater,
    //        container: ViewGroup?,
    //        savedInstanceState: Bundle?
    //): View? {
    //    settingsViewModel =
    //            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    //    val root = inflater.inflate(R.layout.fragment_settings, container, false)
    //    val textView: TextView = root.findViewById(R.id.text_notifications)
    //    settingsViewModel.text.observe(viewLifecycleOwner, Observer {
    //        textView.text = it
    //    })
    //    return root
    //}
