package com.nateshmbhat.card_scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import com.nateshmbhat.card_scanner.scanner_core.models.CardDetails;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CardScannerPlugin
 */
public class CardScannerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    private static final int SCAN_REQUEST_CODE = 666;
    private Activity activity;
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    public static MethodChannel channel;

    public final static String METHOD_CHANNEL_NAME = "nateshmbhat/card_scanner";
    private Context context;
    private Result pendingResult;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), METHOD_CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        context = null;
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("scan_card".equals(call.method)) {
            if (activity == null) {
                result.error("no_activity", "card_scanner plugin requires a foreground activity.", null);
                return;
            }
            if (pendingResult != null) {
                result.error("ALREADY_ACTIVE", "Scan card is already active", null);
                return;
            }
            pendingResult = result;
            activity.startActivityForResult(new Intent(context, CardScannerCameraActivity.class), SCAN_REQUEST_CODE);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(CardScannerCameraActivity.SCAN_RESULT)) {
                CardDetails cardDetails = data.getParcelableExtra(CardScannerCameraActivity.SCAN_RESULT);
                pendingResult.success(cardDetails.toMap());
            } else {
                pendingResult.success(null);
            }

            pendingResult = null;
            return true;
        }
        return false;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    }

    @Override
    public void onDetachedFromActivity() {
    }
}



