package hr.sil.android.seeusadmin.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.*
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.data.RadiusPolygon
import hr.sil.android.seeusadmin.databinding.MapFragmentBinding
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.AppUtil
import hr.sil.android.seeusadmin.view.activity.MainActivity1
import hr.sil.android.seeusadmin.view.adapter.EpdAdapter
import hr.sil.android.seeusadmin.view.adapter.StationAdapter
import hr.sil.android.seeusadmin.view.adapter.StopPointAdapter
import hr.sil.android.zwicktablet.gps.GpsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.jetbrains.anko.sdk15.coroutines.onClick
//import org.jetbrains.anko.sdk15.coroutines.onItemSelectedListener
//import org.jetbrains.anko.toast
import java.util.*

import hr.sil.android.seeusadmin.util.DroidPermission


class StationSettingsFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, OnMapClickListener, GoogleMap.OnMapLongClickListener {


    private val DOUBLE_FORMAT_PLACES = "%.6f"
    private val log = logger()
    private val DOT: PatternItem = Dot()
    private val DASH: PatternItem = Dash(10f)
    private val PATTERN_GAP_LENGTH_PX = 10f
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX)
    private val PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT)
    private val PATTERN_POLYLINE_DASH = Arrays.asList(GAP, DASH)

    private var polygonPoints = mutableListOf<LatLng>()
    private var stationPolygonPoints = listOf<List<LatLng>>()
    private var systemPolygonPoints = listOf<List<LatLng>>()
    private var radiusPolygons = listOf<RadiusPolygon>()
    private var stationPoint: LatLng? = null
    private var lastUserLocation: Location? = null

    private var selectedItem = RRealStationLocation()
    private var selectedEpd = RAdminEpdInfo()

    private lateinit var mMap: GoogleMap
    private var polyline: Polyline? = null
    private var stationMarker: Marker? = null
    private var circle: Circle? = null
    private var newCirclePosition: Circle? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var stopPoint: String = ""
    private var macAddress: String = ""

    private val droidPermission by lazy { DroidPermission.init(activity as MainActivity1) }

    private var radiusNr: Int = 0
    private val markers = mutableListOf<Marker>()

    private lateinit var binding: MapFragmentBinding

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = droidPermission.link(requestCode, permissions, grantResults)

    var drawingFinished = false

    private var device: Device? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

//        val rootView = inflater.inflate(
//            R.layout.map_fragment, container,
//            false
//        )

        macAddress = arguments?.getString("masterMac", "") ?: ""

        device = DeviceStore.devices[macAddress]
        initializeToolbarUIMainActivity(true, getString(R.string.main_station_settings), false, false, requireContext())

        binding = MapFragmentBinding.inflate(layoutInflater)

        return binding.root // rootView
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (binding.polygonRadio.isChecked) {
            setPosition(latLng)
            stationMarker?.remove()
            stationMarker = mMap.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)))
            enableSaveButton()
        } else {
            Toast.makeText(context, "Long Press is allowed in Polygon option mode only!", Toast.LENGTH_SHORT).show()
            //App.ref.toast("Long Press is allowed in Polygon option mode only!")
        }
    }

    //  var POLYGON_SIZE = 0
    override fun onMapClick(latLng: LatLng) {
        if (binding.circleRadio.isChecked) {
            setPosition(latLng)
            markers.map { it.remove() }
            refreshMap(latLng)
            newCirclePosition?.remove()
            newCirclePosition = drawCircle(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude), radiusNr)
        } else if (binding.polygonRadio.isChecked) {
            if (canClickOnMap()) {
                polygonPoints.add(latLng)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                deleteCircle()
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.addMarker(markerOptions)?.let { markers.add(it) }
                if (polygonPoints.size > 2) {
                    binding.btnDrawChanges.isEnabled = true
                    binding.btnSave.isEnabled = true
                } else {
                    binding.btnDrawChanges.isEnabled = false
                    binding. btnSave.isEnabled = false

                }
            }
        }
    }

    private fun deleteCircle() {
        circle?.remove()
        newCirclePosition?.remove()
    }

    private fun canClickOnMap(): Boolean {
        return !drawingFinished
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        var clickCount: Int? = 0
        if (marker.tag != null) {
            clickCount = marker.tag as Int
        }
        if (clickCount != null) {
            clickCount += 1
            marker.tag = clickCount
        }
        return false
    }

    private suspend fun setStationPoint(stopPoint: String? = "", referenceId: String) {
        lifecycleScope.launch {
            val mapOfPoints = WSSeeUsAdmin.getStopPoints(referenceId) ?: mapOf()
            log.info("Printing station points: ${mapOfPoints.values.joinToString { it }} station ref: $referenceId")
            val resMap = mutableMapOf<String, String>(Pair("null", ""))
            resMap.putAll(mapOfPoints)

            withContext(Dispatchers.Main) {
                binding.spinnerStopPoints.adapter = StopPointAdapter(resMap)
                for (i in 0 until binding.spinnerStopPoints.count) {
                    val selectedPair = binding.spinnerStopPoints.adapter.getItem(i)
                    if (selectedPair != null) {
                        val obj = (selectedPair as Pair<String?, String>).first
                        if (obj != null && obj == stopPoint && obj.isNotEmpty() && stopPoint.isNotEmpty()) {
                            log.info("Found setteled stop position: $obj")
                            binding.spinnerStopPoints.setSelection(i, true)
                            return@withContext
                        }

                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        stationPolygonPoints = getPolygonsForStationId(device?.stationId, device?.masterUnitId)
        systemPolygonPoints = getPolygons(device?.stationId)
        radiusPolygons = getRadiusStations(device?.stationId)
        setPosition(LatLng(device?.latitude ?: 0.0, device?.longitude ?: 0.0))
        selectedItem.latitude = device?.latitude ?: 0.0
        selectedItem.longitude = device?.longitude ?: 0.0
        selectedItem.address = ""
        selectedItem.id = device?.stationId ?: 0
        selectedItem.referenceId = device?.stationReference ?: ""

        binding.etDeviceName.setText(device?.unitName)
        if (device?.polygon?.isNotEmpty() == true) {
            binding.radioGroup.check(R.id.polygonRadio)
            binding.btnDrawChanges.isEnabled = false
            binding.btnSave.isEnabled = false
            if( device != null )
                polygonPoints.addAll(getTransferPositions(device!!.polygon))
            drawingFinished = true
            binding.circleLayout.visibility = View.GONE
            binding.polygonLayout.visibility = View.VISIBLE
        } else {
            binding.btnDrawChanges.isEnabled = false
            binding.btnSave.isEnabled = true
        }
        binding.deviceRadius.setText(device?.radius.toString())
        radiusNr = device?.radius ?: 0
        val positions = polygonPoints.toList().joinToString("\n") { it.latitude.toString() + " " + it.longitude.toString() }
        log.info("Polygon points for station: ${selectedItem.name} ${polygonPoints.size} - Positions $positions")

        getStationsUnitsReal(device)

        getEpdUnits(device)

        if (GpsUtils(requireContext()).turnGPSOn()) {
            implementGoogleMapsAndClickListeners()
        }
    }

    private fun getEpdUnits(device: Device?) {
        lifecycleScope.launch {
            val resList = listOf(RAdminEpdInfo()).plus(WSSeeUsAdmin.getEpdLists()?.toList()
                    ?: listOf())

            withContext(Dispatchers.Main) {
                binding.spinnerEpdSelection.adapter = EpdAdapter(resList)
                if (device?.epdId != null && device.epdId != 0) {
                    val position = (binding.spinnerEpdSelection.adapter as EpdAdapter).getPositionAt(device.epdId)
                    if (position != -1) {
                        selectedEpd = (binding.spinnerEpdSelection.adapter as EpdAdapter).getItem(position)
                        binding.spinnerEpdSelection.setSelection(position, true)
                    }
                }
            }
        }
    }

    private fun getPolygonsForStationId(stationId: Int?, masterUnitId: Int?): List<List<LatLng>> {
        val polygons = mutableListOf<List<LatLng>>()
        DeviceStore.devices.values.filter { it.stationId == stationId && it.masterUnitId != masterUnitId }.map {
            polygons.add(getTransferPositions(it.polygon))
        }
        return polygons
    }

    private fun getPolygons(stationId: Int?): List<List<LatLng>> {
        val polygons = mutableListOf<List<LatLng>>()
        DeviceStore.devices.values.filter { it.stationId != stationId }.map {
            polygons.add(getTransferPositions(it.polygon))
        }
        return polygons
    }

    private fun getRadiusStations(stationId: Int?): List<RadiusPolygon> {
        val polygons = mutableListOf<RadiusPolygon>()
        DeviceStore.devices.values.filter { it.stationId != stationId && it.polygon.isEmpty() && it.radius ?: 0 > 0 }.map {
            val radiusPolygon = RadiusPolygon()
            radiusPolygon.radius = it.radius ?: 0
            radiusPolygon.latitude = it.latitude
            radiusPolygon.longitude = it.longitude
            polygons.add(radiusPolygon)
        }
        return polygons

    }

    private fun enableDrawButton() {
        binding.btnSave.isEnabled = false
        binding.btnDrawChanges.isEnabled = true
    }


    private fun enableSaveButton() {
        binding.btnSave.isEnabled = true
        binding.btnDrawChanges.isEnabled = false
    }

    private fun implementGoogleMapsAndClickListeners() {

        val device = DeviceStore.devices[macAddress]
        val mapFragment = childFragmentManager.findFragmentById(R.id.g_map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

//        binding.spinnerStationSelection.onItemSelectedListener {
//             onItemSelected { adapterView, _, position, _ ->
//                selectedItem = adapterView?.getItemAtPosition(position) as RRealStationLocation
//                if (device != null) {
//                    if (binding.spinnerStopPoints.selectedItem != null) {
//                        val stop = (binding.spinnerStopPoints.selectedItem as Pair<String?, String>).first
//                        log.info("Stop Point is: $stop")
//                        setStationPoint(
//                                stopPoint = stop,
//                                referenceId = selectedItem.referenceId
//                        )
//                        enableSaveButton()
//                    } else {
//                        setStationPoint(device.stopPoint, selectedItem.referenceId)
//                    }
//                }
//            }
//        }

//        binding.spinnerStopPoints.onItemSelectedListener {
//            onItemSelected { adapterView, item, position, _ ->
//                val selectedPair = adapterView?.getItemAtPosition(position)
//                if (selectedPair != null) {
//                    stopPoint = (selectedPair as Pair<String, String>).first
//                    log.info("Poistion : $item")
//                    if (stopPoint != device?.stopPoint && stopPoint.isNotBlank()) {
//                        enableSaveButton()
//                    } else {
//                        if (binding.polygonRadio.isChecked && selectedItem.id == device?.stationId) {
//                            binding.btnDrawChanges.isEnabled = false
//                            binding.btnSave.isEnabled = false
//
//                        }
//                    }
//                }
//
//            }
//        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (binding.circleRadio.isChecked) {
                binding.circleLayout.visibility = View.VISIBLE
                binding.polygonLayout.visibility = View.GONE
                enableSaveButton()
                radiusNr = device?.radius ?: 0
                binding.deviceRadius.setText(radiusNr.toString())
                newCirclePosition?.remove()
                polyline?.remove()
                polygonPoints.clear()
                markers.forEach { it.remove() }
                markers.clear()
                circle?.remove()
                circle = drawCircle(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude), radiusNr, R.color.colorPrimaryTransparent)
            } else if (binding.polygonRadio.isChecked) {
                newCirclePosition?.remove()
                binding.circleLayout.visibility = View.GONE
                binding.polygonLayout.visibility = View.VISIBLE
                binding.deviceRadius.setText("0")
                deleteCircle()

                log.info("Radius Number: $radiusNr")
                if (device?.polygon?.isNotEmpty() == true) {
                    val pointsToDraw = getTransferPositions(device.polygon).toMutableList()
                    if (polygonPoints.isNotEmpty())
                        pointsToDraw.add(polygonPoints[0])
                    val colorStationPolygon = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    polyline = mMap.addPolyline((PolylineOptions()).clickable(true).addAll(pointsToDraw).color(colorStationPolygon))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsToDraw[0], 19.0f))
                }
                if (drawingFinished) {
                    enableSaveButton()
                } else {
                    if (polygonPoints.isNotEmpty())
                        enableDrawButton()
                }

            }
        }

//        binding.spinnerEpdSelection.onItemSelectedListener {
//            onItemSelected { adapterView, _, position, _ ->
//                selectedEpd = adapterView?.getItemAtPosition(position) as RAdminEpdInfo
//            }
//        }

        binding.btnResetChanges.setOnClickListener {
            binding.deviceRadius.setText("0")
            radiusNr = 0
            polygonPoints = mutableListOf()
            markers.forEach { it.remove() }
            polyline?.remove()
            deleteCircle()
            enableSaveButton()
            if (!binding.circleRadio.isChecked) {
                binding.btnDrawChanges.isEnabled = false
                drawingFinished = false
            }
        }

        binding.btnDrawChanges.setOnClickListener {
            if (polygonPoints.isNotEmpty()) {
                deleteCircle()
                val colorStationPolygon = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                polyline = mMap.addPolyline((PolylineOptions()).clickable(true).addAll(polygonPoints).add(polygonPoints[0]).color(colorStationPolygon))
                enableSaveButton()
                drawingFinished = true
            }
        }

        binding.btnSave.setOnClickListener {
            if (AppUtil.isInternetAvailable()) {
                val request = RStationUnitRequest()
                if (binding.circleRadio.isChecked) {
                    if (binding.deviceLatitude.text.isNullOrBlank()) {
                        binding.deviceLatitude.error =
                                this@StationSettingsFragment.getString(R.string.locker_settings_error_name_empty_warning)
                        return@setOnClickListener
                    }
                    if (binding.deviceLongitude.text.isNullOrBlank()) {
                        binding.deviceLongitude.error =
                                this@StationSettingsFragment.getString(R.string.locker_settings_error_address_empty_warning)
                        return@setOnClickListener
                    }
                    if (binding.deviceRadius.text.isNullOrBlank()) {
                        binding.deviceRadius.error =
                                this@StationSettingsFragment.getString(R.string.locker_settings_error_radius_empty_warning)
                        return@setOnClickListener
                    }

                    if (binding.etDeviceName.text.isNullOrBlank()) {
                        binding.etDeviceName.error =
                                this@StationSettingsFragment.getString(R.string.locker_settings_error_radius_empty_warning)
                        return@setOnClickListener
                    }
                    polygonPoints.clear()
                    polyline?.remove()

                    getSystemService(requireContext(), Context.LOCATION_SERVICE::class.java)

                    request.latitude = parseLocaleDouble(binding.deviceLatitude)
                    request.longitude = parseLocaleDouble(binding.deviceLongitude)
                    request.radiusMeters = radiusNr
                } else {

                    if (stationPoint?.latitude != null && stationPoint?.longitude != null) {
                        request.radiusMeters = 0
                        radiusNr = 0
                        binding.deviceRadius.setText("0")
                        request.latitude = stationPoint?.latitude
                        request.longitude = stationPoint?.longitude
                        request.polygon = polygonPoints
                    } else {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.error_station_location), Toast.LENGTH_SHORT).show()

                        //App.ref.toast(R.string.error_station_location)
                        return@setOnClickListener
                    }
                }
                if (binding.spinnerStopPoints.selectedItem != null) {
                    if ((binding.spinnerStopPoints.selectedItem as Pair<String, String>).first == "0")
                        request.stopPoint = null
                    else
                        request.stopPoint = (binding.spinnerStopPoints.selectedItem as Pair<String, String>).first

                }

                lifecycleScope.launch {
                    request.epdTypeId = selectedEpd.id
                    request.name = binding.etDeviceName.text.toString()
                    request.stationId = selectedItem.id
                    request.networkConfigurationId = device?.networkConfigurationId ?: 0
                    request.modemWorkingType = (device?.modemWorkingType ?: RPowerType.BATTERY).name
                    if (WSSeeUsAdmin.modifyStationUnit(
                            macAddress.macRealToClean(),
                            request
                        ) != null
                    ) {
                        AppUtil.refreshCache()
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.successfully_updated),
                            Toast.LENGTH_SHORT
                        ).show()

                        //App.ref.toast(R.string.successfully_updated)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.error_updating_mpl),
                            Toast.LENGTH_SHORT
                        ).show()

                        // App.ref.toast(R.string.error_updating_mpl)
                    }
                }
            } else {
                Toast.makeText(requireContext(), requireContext().getString(R.string.app_common_internet_connection), Toast.LENGTH_SHORT).show()

                //App.ref.toast(this@StationSettingsFragment.getString(R.string.app_common_internet_connection))
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as MainActivity1)
    }

    private fun getStationsUnitsReal(device: Device?) {
        lifecycleScope.launch {
            val resList = listOf(RRealStationLocation()).plus(WSSeeUsAdmin.getStationUnitsReal()?.toList()
                    ?: listOf())

            withContext(Dispatchers.Main) {
                binding.spinnerStationSelection.adapter = StationAdapter(resList)
                if (device?.stationId != null && device.stationId != 0) {
                    val position = (binding.spinnerStationSelection.adapter as StationAdapter).getPositionAt(device.stationId)
                    if (position != -1) {
                        binding.spinnerStationSelection.setSelection(position, true)
                        selectedItem = (binding.spinnerStationSelection.adapter as StationAdapter).getItem(position)
                        setStationPoint(device.stopPoint, selectedItem.referenceId)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                lifecycleScope.launch {
                    delay(5000)
                    withContext(Dispatchers.Main) {

                        implementGoogleMapsAndClickListeners()
                    }
                }
            }
        } else
            activity?.finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        log.info("Map is ready")
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        val device = DeviceStore.devices[macAddress]
        if (device != null) {
            val cameraMovePosition: LatLng
            if (device.polygon.isNotEmpty()) {
                val colorStationPolygon = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                val pointsToDraw = arrayListOf<LatLng>()
                pointsToDraw.addAll(polygonPoints)
                pointsToDraw.add(polygonPoints[0])
                polyline = mMap.addPolyline((PolylineOptions()).clickable(true).addAll(pointsToDraw).color(colorStationPolygon))
                binding.btnDrawChanges.isEnabled = false
                drawingFinished = true
            } else {
                circle = drawCircle(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude), radiusNr, R.color.colorPrimaryTransparent)
            }
            cameraMovePosition = LatLng(device.latitude, device.longitude)
            stationMarker = mMap.addMarker(MarkerOptions().position(cameraMovePosition).title(device.unitName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)))

            val colorStationPolygon = ContextCompat.getColor(requireContext(), R.color.colorPrimaryTransparent)

            if (stationPolygonPoints.isNotEmpty()) {
                stationPolygonPoints.forEach {
                    if (it.isNotEmpty()) {
                        mMap.addPolygon(PolygonOptions().addAll(it).strokeColor(colorStationPolygon).strokePattern(PATTERN_POLYLINE_DASH))

                    }
                }
            }
            val colorPolygon = ContextCompat.getColor(requireContext(), R.color.colorCyenTransparent)
            if (systemPolygonPoints.isNotEmpty()) {
                systemPolygonPoints.forEach {
                    if (it.isNotEmpty())
                        mMap.addPolygon(PolygonOptions().addAll(it).strokeColor(colorPolygon).strokePattern(PATTERN_POLYLINE_DOTTED))
                }
            }

            if (radiusPolygons.isNotEmpty()) {
                radiusPolygons.forEach {
                    if (it.latitude != 0.0) {
                        mMap.addCircle(CircleOptions()
                                .center(LatLng(it.latitude, it.longitude))
                                .radius(it.radius.toDouble())
                                .strokeWidth(0f)
                                .fillColor(ContextCompat.getColor(requireContext(), R.color.colorBlue30PercentTransparency)))
                    }
                }
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraMovePosition, 17.0f))
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                droidPermission
                        .request(Manifest.permission.ACCESS_FINE_LOCATION)
                        .done { _, deniedPermissions ->
                            if (deniedPermissions.isNotEmpty()) {
                                log.info("Permissions were denied!")
                            } else {

                            }
                        }
                        .execute()// 1

            } else {
                mMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lastUserLocation = location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        //     mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17.0f))
                    }
                }
            }

            binding.deviceRadius.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {

                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                    val latLng = LatLng(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude))
                    refreshMap(latLng)
                    if (binding.deviceRadius.text.toString().isNotEmpty()) {
                        radiusNr = binding.deviceRadius.text.toString().toInt()
                        if (radiusNr > 0) {
                            if (newCirclePosition != null) {
                                newCirclePosition?.remove()
                                newCirclePosition = drawCircle(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude), radiusNr)
                            } else {
                                circle?.remove()
                                circle = drawCircle(parseLocaleDouble(binding.deviceLatitude), parseLocaleDouble(binding.deviceLongitude), radiusNr, R.color.colorPrimaryTransparent)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun parseLocaleDouble(positionText: EditText): Double = positionText.text.toString().replace(',', '.').toDouble()

    private fun setPosition(latLng: LatLng) {
        binding.deviceLatitude.setText(String.format(DOUBLE_FORMAT_PLACES, latLng.latitude))
        binding.deviceLongitude.setText(String.format(DOUBLE_FORMAT_PLACES, latLng.longitude))
        stationPoint = latLng
    }

    private fun refreshMap(latLng: LatLng, addMarker: Boolean = true) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(String.format(DOUBLE_FORMAT_PLACES, latLng.latitude) + " : " + String.format(DOUBLE_FORMAT_PLACES, latLng.longitude))
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        if (addMarker) {
            mMap.addMarker(markerOptions)?.let { markers.add(it) }
        }
    }

    private fun drawCircle(latitude: Double, longitude: Double, radius: Int, color: Int = R.color.colorWhite30PercentTransparency): Circle? {

        if (radius > 0) {
            val circle = mMap.addCircle(CircleOptions()
                    .center(LatLng(latitude, longitude))
                    .radius(radiusNr.toDouble())
                    .strokeWidth(0f)
                    .fillColor(ContextCompat.getColor(requireContext(), color)))

            log.info("Setteled circle")
            return circle
        }
        return null
    }

    private fun getTransferPositions(polygonData: List<RLatLng>): List<LatLng> {
        val mutbleList = mutableListOf<LatLng>()
        polygonData.forEach {
            val poistion = LatLng(it.latitude, it.longitude)

            mutbleList.add(poistion)
        }
        return mutbleList.toList()
    }


}