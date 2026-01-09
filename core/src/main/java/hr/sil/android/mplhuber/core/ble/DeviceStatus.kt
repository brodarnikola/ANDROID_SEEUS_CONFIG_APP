package hr.sil.android.mplhuber.core.ble




enum class DeviceStatus (val value: Int?, val sortIndex: Int) {
  UNKNOWN (null,999),
  UNREGISTERED (0x00,3),
  REGISTRATION_PENDING (0x01,2),
  REGISTERED (0x02,1),
  DELETE_PENDING(0X03,4),
  REJECTED (0X04,5),
  NEW (0X05,6);

  companion object {
    fun parse( type: Int?) = DeviceStatus.values().firstOrNull{
      it.value== type
    }?: UNKNOWN
  }

}