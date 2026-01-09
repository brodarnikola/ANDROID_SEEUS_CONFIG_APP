package hr.sil.android.seeusadmin.util.ui.extensions

import android.graphics.Color
import androidx.core.content.ContextCompat
import android.view.ViewManager
import hr.sil.android.mplhuber.view.ui.util.extensions.bold
import hr.sil.android.seeusadmin.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar

fun ViewManager.toolbarUI() =
        toolbar {
            id = R.id.toolbar
            relativeLayout {

                imageView(R.drawable.ic_arrow_back){
                    id = R.id.mainactivity_toolbar_arrow
                }.lparams{
                    width = dip(40)
                    height = dip(40)
                    alignParentStart()
                    centerVertically()
                    leftMargin = dip(5)
                }


                imageView(R.drawable.seeus_black_thin){
                    id = R.id.mainactivity_toolbar_huber_picture
                }.lparams{

                    centerInParent()
                }


            }.lparams {
                width = matchParent
                height = wrapContent
                rightMargin = dip(20)
            }

            backgroundColor = Color.WHITE
        }

fun ViewManager.toolbarUI(activity: String) =
        toolbar {
            setTitleTextColor(Color.BLACK)
            id = R.id.toolbar
            title = activity
            backgroundColor = Color.WHITE
        }

