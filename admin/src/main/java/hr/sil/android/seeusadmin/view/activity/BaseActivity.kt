package hr.sil.android.seeusadmin.view.activity

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroupOverlay
import android.widget.EditText
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout

import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.util.DialogUtil
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.connectivity.BluetoothChecker
import hr.sil.android.seeusadmin.util.connectivity.LocationGPSChecker
import hr.sil.android.seeusadmin.util.connectivity.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk15.coroutines.textChangedListener


open class BaseActivity(noBleViewId: Int = 0, noWifiViewId: Int = 0, noLocationGPSViewId: Int = 0) : AppCompatActivity() {

    private var btCheckerListenerKey: String? = null
    private var networkCheckerListenerKey: String? = null
    private var locationGPSListenerKey: String? = null
    private val frame by lazy { if (noBleViewId != 0) find<FrameLayout>(noBleViewId) else null }
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }
    private val noWifiFrame by lazy { if (noWifiViewId != 0) find<FrameLayout>(noWifiViewId) else null }
    val noLocationGPSFrame by lazy { if (noLocationGPSViewId != 0) find<FrameLayout>(noLocationGPSViewId) else null }


    protected var viewLoaded = false
    protected var networkAvailable: Boolean = true
    protected var bluetoothAvailable: Boolean = true
    var locationGPSAvalilable: Boolean = true

    override fun onResume() {
        super.onResume()
        if (btCheckerListenerKey == null) {
            btCheckerListenerKey = BluetoothChecker.addListener { available ->
                uiHandler.post { onBluetoothStateUpdated(available) }
            }
        }
        if (networkCheckerListenerKey == null) {
            networkCheckerListenerKey = NetworkChecker.addListener { available ->
                uiHandler.post { onNetworkStateUpdated(available) }
            }
        }
        if (locationGPSListenerKey == null) {
            locationGPSListenerKey = LocationGPSChecker(this).addListener { available ->
                uiHandler.post { onLocationGPSStateUpdated(available) }
            }
        }
    }

    fun updateUI() {
        if (frame != null && noWifiFrame != null) {
            frame?.visibility = if (bluetoothAvailable) View.GONE else View.VISIBLE
            noWifiFrame?.visibility = if (networkAvailable) View.GONE else {
                if (!bluetoothAvailable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            noLocationGPSFrame?.visibility = if (locationGPSAvalilable) View.GONE else {
                if (!bluetoothAvailable || !networkAvailable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()

        btCheckerListenerKey?.let { BluetoothChecker.removeListener(it) }
        btCheckerListenerKey = null
        networkCheckerListenerKey?.let { NetworkChecker.removeListener(it) }
        networkCheckerListenerKey = null
        locationGPSListenerKey?.let { LocationGPSChecker(this).removeListener(it) }
        locationGPSListenerKey = null
    }

    fun setNoBleOverLay() {
        val viewGroup = contentView?.overlay as ViewGroupOverlay?
        val overlayView =
                _FrameLayout(this).apply {
                    background = ContextCompat.getDrawable(this@BaseActivity, R.drawable.bg_bluetooth)
                    alpha = 0.8f
                    textView(R.string.app_generic_no_ble) {
                        textColor = Color.WHITE
                        allCaps = true
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                    }
                }

        viewGroup?.add(overlayView)

    }


    open fun onBluetoothStateUpdated(available: Boolean) {}

    open fun onNetworkStateUpdated(available: Boolean) {}

    open fun onLocationGPSStateUpdated(available: Boolean) {}

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingsHelper.setLocale(base))
    }

    enum class ValidationResult(val messageResource: Int?) {
        VALID(null),
        INVALID_OLD_PASSWORD(R.string.edit_user_validation_current_password_invalid),
        INVALID_CURRENT_USER_DATA(R.string.edit_user_validation_current_user_invalid),
        INVALID_PASSWORDS_DO_NOT_MATCH(R.string.edit_user_validation_passwords_do_not_match),
        INVALID_PASSWORD_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_PASSWORD_MIN_6_CHARACTERS(R.string.edit_user_validation_password_min_6_characters),
        INVALID_USERNAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_USERNAME_MIN_4_CHARACTERS(R.string.edit_user_validation_username_min_4_characters),
        INVALID_FIRST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_LAST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_ZIP_CODE_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_CITY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_COUNTRY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_LOCKER_SETTINGS_NAME(R.string.locker_settings_error_name_empty_warning),
        INVALID_LOCKER_SETTINGS_ADDRESS(R.string.locker_settings_error_address_empty_warning),
        INVALID_EMAIL_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_EMAIL(R.string.message_email_invalid),
        INVALID_PHONE_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_TERMS_AND_CONDITIONS_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked),
        INVALID_PRIVACY_POLICY_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked);

        fun getText(context: Context) = if (messageResource != null) {
            context.resources.getString(messageResource)
        } else {
            null
        }

        fun isValid() = this == VALID
    }

    protected fun attachValidator(editText: EditText, validator: () -> Unit) {
        editText.afterTextChangeDelay(500L) {
            validator()
        }
    }


    private fun EditText.afterTextChangeDelay(duration: Long, run: () -> Unit) {
        var job: Job? = null
        this.textChangedListener {
            afterTextChanged {
                job?.cancel()
                job = launch(Dispatchers.Main) {
                    try {
                        delay(duration)
                        run.invoke()
                    } catch (e: Exception) {
                        //ignore
                    }
                }
            }
        }
    }


    protected fun validateSetError(editText: TextInputLayout?, result: ValidationResult): ValidationResult {
        val errorText = if (!result.isValid()) result.getText(this) else null
        editText?.error = errorText
        return result
    }

    fun validateEditText(textInputLayout: TextInputLayout?, editText: EditText, showDialog: Boolean, validate: (value: String) -> ValidationResult): Boolean {
        val result = validate(editText.text.toString())
        validateSetError(textInputLayout, result)

        val isValid = result.isValid()
        if (!isValid && showDialog) validateShowDialog(result)
        return isValid
    }

    private fun validateShowDialog(validationResult: ValidationResult) {
        val messageResource = validationResult.messageResource
                ?: R.string.edit_user_validation_blank_fields_exist
        DialogUtil.messageDialogBuilder(this, resources.getString(messageResource), { }).show()
    }

}