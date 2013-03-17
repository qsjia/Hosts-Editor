package tk.qsjia.hostseditor.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;

/**
 * Created with IntelliJ IDEA.
 * User: SJia
 * Date: 13-3-17
 * Time: 上午11:37
 */
public class QRCodeUtils {

	public static void scanQrCode(Fragment fragment){
		IntentIntegrator integrator = new IntentIntegratorSupportV4(fragment);
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
	}

	public static void encodeBarcode(CharSequence type, CharSequence data, Fragment fragment) {
		IntentIntegrator integrator = new IntentIntegratorSupportV4(fragment);
		integrator.shareText(data, type);
	}

	public static void encodeBarcode(CharSequence type, Bundle data, Fragment fragment) {
		IntentIntegrator integrator = new IntentIntegratorSupportV4(fragment);
		integrator.addExtra("ENCODE_DATA", data);
		integrator.shareText(data.toString(), type); // data.toString() isn't used
	}
}
