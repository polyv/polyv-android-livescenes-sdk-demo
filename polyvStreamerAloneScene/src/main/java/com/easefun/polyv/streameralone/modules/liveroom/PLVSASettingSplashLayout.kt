package com.easefun.polyv.streameralone.modules.liveroom

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager
import com.easefun.polyv.livecommon.module.data.PLVStatefulData
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker
import com.easefun.polyv.livecommon.module.utils.PLVFileProvider
import com.easefun.polyv.livecommon.module.utils.imageloader.loadImage
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO
import com.easefun.polyv.streameralone.R
import com.plv.foundationsdk.component.coroutine.PLVGlobalScope
import com.plv.foundationsdk.component.coroutine.subscribeAsResult
import com.plv.livescenes.access.PLVUserAbility
import com.plv.livescenes.access.PLVUserAbilityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.polyv.android.common.libs.lang.state.mutableStateOf
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Hoshiiro
 */
class PLVSASettingSplashLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val settingSplashIv by lazy { findViewById<ImageView>(R.id.plvsa_setting_splash_iv) }
    private val settingSplashHintTv by lazy { findViewById<TextView>(R.id.plvsa_setting_splash_hint_tv) }

    private val splashImage = mutableStateOf("")

    private var liveRoomDataManager: IPLVLiveRoomDataManager? = null
    private var streamerPresenter: IPLVStreamerContract.IStreamerPresenter? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.plvsa_setting_splash_layout, this)

        val canChangeSplashImage = PLVUserAbilityManager.myAbility()
            .hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_CHANNEL_SPLASH_IMAGE)
        if (canChangeSplashImage) {
            settingSplashHintTv.visibility = View.VISIBLE
            setOnClickListener(PLVDebounceClicker.OnClickListener(this))
        } else {
            settingSplashHintTv.visibility = View.GONE
            setOnClickListener { }
        }
    }

    fun init(liveRoomDataManager: IPLVLiveRoomDataManager, streamerPresenter: IPLVStreamerContract.IStreamerPresenter) {
        this.liveRoomDataManager = liveRoomDataManager
        this.streamerPresenter = streamerPresenter
        liveRoomDataManager.getClassDetailVO()
            .observe(context as LifecycleOwner, object : Observer<PLVStatefulData<PolyvLiveClassDetailVO>> {
                override fun onChanged(statefulData: PLVStatefulData<PolyvLiveClassDetailVO>?) {
                    liveRoomDataManager.getClassDetailVO().removeObserver(this)
                    if (statefulData?.data?.data == null) return
                    splashImage.setValue(statefulData.data.data.splashImg ?: "")
                }
            })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return
        splashImage.observe {
            settingSplashIv.loadImage(it)
        }.disposeOnDetached(this)
    }

    override fun onClick(v: View?) {
        if (liveRoomDataManager == null || streamerPresenter == null) return
        PLVGlobalScope.launch(Dispatchers.Main) {
            val pickResult = PLVSASettingSplashPickFragment.pick(context as FragmentActivity).getOrThrow()
            val cropResult = PLVSASettingSplashCropFragment.crop(context as FragmentActivity, pickResult).getOrThrow()
            File(pickResult.path!!).delete()
            val remoteUrl = withContext(Dispatchers.IO) {
                streamerPresenter!!.updateChannelSplashImage(cropResult).subscribeAsResult()
            }.getOrThrow()
            splashImage.setValue(remoteUrl)
        }
    }

}

class PLVSASettingSplashPickFragment : Fragment() {
    lateinit var onSelectResult: (result: Result<Uri>) -> Unit

    companion object {
        suspend fun pick(context: FragmentActivity): Result<Uri> = suspendCoroutine { continuation ->
            val fragment = PLVSASettingSplashPickFragment().apply {
                onSelectResult = { continuation.resume(it) }
            }
            context.supportFragmentManager.beginTransaction()
                .add(fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val intent = Intent.createChooser(
            Intent().apply {
                setType("image/*")
                setAction(Intent.ACTION_GET_CONTENT)
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            },
            "Select Splash Image"
        )
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context = context ?: return
        if (requestCode != 1000) {
            return
        }
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val outputFile = File(context.cacheDir, "pick_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri).use { input ->
                    outputFile.outputStream().use { output ->
                        input?.copyTo(output)
                        output.flush()
                    }
                }
                val outputUri = PLVFileProvider.getUriForFile(
                    context,
                    "${context.applicationContext.packageName}.plvfileprovider",
                    outputFile
                )
                onSelectResult(Result.success(outputUri))
            } else {
                onSelectResult(Result.failure(IllegalStateException("result is null")))
            }
        } else {
            onSelectResult(Result.failure(IllegalStateException("result is not ok: $resultCode")))
        }
        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }
}

class PLVSASettingSplashCropFragment : Fragment() {
    lateinit var onSelectResult: (result: Result<Bitmap>) -> Unit
    lateinit var sourceUri: Uri
    private var outputUri: Uri? = null

    companion object {
        suspend fun crop(context: FragmentActivity, source: Uri): Result<Bitmap> = suspendCoroutine { continuation ->
            val fragment = PLVSASettingSplashCropFragment().apply {
                onSelectResult = { continuation.resume(it) }
                sourceUri = source
            }
            context.supportFragmentManager.beginTransaction()
                .add(fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startCrop(sourceUri)
    }

    private fun startCrop(source: Uri) {
        val context = context ?: return
        try {
            val cropIntent = Intent("com.android.camera.action.CROP").apply {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(source, "image/*")
                putExtra("crop", "true")
                putExtra("aspectX", 16)
                putExtra("aspectY", 9)
                putExtra("outputX", 1920)
                putExtra("outputY", 1080)
                putExtra("return-data", false)
                putExtra("scale", true)
                putExtra("scaleUpIfNeeded", true)
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                outputUri = source
                putExtra("output", outputUri)
            }

            if (cropIntent.resolveActivity(context.packageManager) != null) {
                startActivityForResult(cropIntent, 1000)
            } else {
                outputSourceImage(source)
            }
        } catch (e: Exception) {
            onSelectResult(Result.failure(e))
            fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val outputUri = outputUri ?: return
        if (requestCode != 1000) {
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            onSelectResult(Result.failure(IllegalStateException("Crop result is not ok: $resultCode")))
            fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
            return
        }

        val bitmap = BitmapFactory.decodeStream(context!!.contentResolver.openInputStream(outputUri))
        onSelectResult(Result.success(bitmap))
        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }

    private fun outputSourceImage(uri: Uri) {
        val context = context ?: return
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputBitmap: Bitmap
        inputStream.use {
            outputBitmap = BitmapFactory.decodeStream(it)
        }
        onSelectResult(Result.success(outputBitmap))

        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }
}