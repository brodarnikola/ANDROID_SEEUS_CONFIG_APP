package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RMessageLog
import kotlinx.coroutines.launch
import java.text.ParseException
import java.util.*

import kotlinx.coroutines.launch
import kotlin.collections.first
import kotlin.text.uppercase

import hr.sil.android.seeusadmin.R
import hr.sil.android.mplhuber.core.util.formatFromStringToDate
import hr.sil.android.mplhuber.core.util.formatToViewDateTimeDefaults
import hr.sil.android.seeusadmin.compose_ui.components.ProgressIndicatorSize
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont

@Composable
fun AlertsScreen(
    //onShowDeleteAllDialog: (List<RMessageDataLog>, () -> Unit) -> Unit,
   // onShowDeleteOneDialog: (RMessageDataLog, () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    //var alarmMessageList by remember { mutableStateOf<List<RMessageLog>>(emptyList()) }

    val alarmMessageList = remember {
        mutableStateListOf<RMessageLog>()
    }

    var isLoading by remember { mutableStateOf(false) }

    val showDeleteAllDialog = remember { mutableStateOf(false) }
    val showSingleDeleteItemDialog = remember { mutableStateOf(false) }

    val messageId = rememberSaveable { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val resultBackend = WSSeeUsAdmin.getMessageLog() ?: listOf()
            alarmMessageList.addAll(resultBackend)
            isLoading = false
        }
    }

    if(showDeleteAllDialog.value) {
        DeleteAllAlertsDialog(
            messages = alarmMessageList,
            onConfirm = {
                scope.launch {
                    WSSeeUsAdmin.deleteAll()
                }
                showDeleteAllDialog.value = false
            },
            onDismiss = {
                showDeleteAllDialog.value = false
            }
        )
    }

    if(showSingleDeleteItemDialog.value) {
        DeleteOneAlertDialog(
            message = alarmMessageList.first { it.id == messageId.value },
            onConfirm = {
                scope.launch {
                    WSSeeUsAdmin.deleteMessageItem(messageId.value)
                }
                showSingleDeleteItemDialog.value = false
            },
            onDismiss = {
                showSingleDeleteItemDialog.value = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Clear All Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        //.background(
                           // color = brandColors.mainButtonBackgroundColor,
                        //    shape = RoundedCornerShape(4.dp)
                       // )
                        .clickable {
                            showDeleteAllDialog.value = true
                        }
                        .padding(horizontal = 5.dp, vertical = 13.dp)
                ) {

                    TextViewWithFont(
                        text = stringResource(id = R.string.app_generic_clear_all).uppercase(),
                        color = colorResource(R.color.colorWhite),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alerts List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(alarmMessageList) { messageLog ->
                    AlertListItem(
                        messageLog = messageLog,
                        onDeleteClick = {
                            showDeleteAllDialog.value = true
//                            onShowDeleteOneDialog(messageLog) {
//                                scope.launch {
//                                    WSAdmin.deleteMessageItem(messageLog.id)
//                                    alarmMessageList = alarmMessageList.filter { it.id != messageLog.id }
//                                }
//                            }
                        },
                        onItemClick = {
                            messageId.value = messageLog.id
                            showSingleDeleteItemDialog.value = true
//                            if (messageLog.body == null) {
//                                onNavigateToDetails(messageLog.id)
//                            }
                        }
                    )
                }
            }
        }
        // Loading indicator
        if (isLoading) {
            RotatingRingIndicator(
                modifier = Modifier
                    .size(ProgressIndicatorSize) // 40.dp
                    .padding(top = 10.dp)
            )
        }
    }
}

/**
 * Simple Delete All Dialog (replace with your actual dialog)
 */
@Composable
fun DeleteAllAlertsDialog(
    messages: List<RMessageLog>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All Alerts") },
        text = { Text("Are you sure you want to delete all ${messages.size} alerts?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Simple Delete One Dialog (replace with your actual dialog)
 */
@Composable
fun DeleteOneAlertDialog(
    message: RMessageLog,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Alert") },
        text = { Text("Are you sure you want to delete this alert?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AlertListItem(
    messageLog: RMessageLog,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .background(colorResource(R.color.colorGray)) // thmAlertsListRowBackgroundColor
            .clickable(enabled = messageLog.body != "") { onItemClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Message content section (7.8 weight)
        Column(
            modifier = Modifier
                .weight(7.8f)
                .padding(end = 10.dp)
        ) {

            TextViewWithFont(
                text = formatCorrectDate(messageLog.dateCreated.toString()),
                color = colorResource(R.color.colorWhite),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Center,
            )

            // Master name
            if (messageLog.master___name != null) {

                TextViewWithFont(
                    text = messageLog.master___name,
                    color = colorResource(R.color.colorWhite),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                    textAlign = TextAlign.Center,
                )
            }

            TextViewWithFont(
                text = messageLog.subject,
                color = colorResource(R.color.colorWhite),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Center,
            )

            // Body
            if (messageLog.body != "") {

                TextViewWithFont(
                    text = messageLog.body,
                    color = colorResource(R.color.colorWhite),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Arrow section (1 weight)

        Spacer(modifier = Modifier.weight(1f))
        // Delete button section (1.2 weight)
        Box(
            modifier = Modifier
                .weight(1.2f)
                .padding(end = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_remove),
                contentDescription = "Delete",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .padding(top = 5.dp)
                    .clickable { onItemClick() }
            )
        }
    }
}

private fun formatCorrectDate(createdOnDate: String): String {
    return try {
        val fromStringToDate = createdOnDate.formatFromStringToDate()
        fromStringToDate.formatToViewDateTimeDefaults()
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
}