package com.mridang.owninfo;

import java.util.Random;

import org.acra.ACRA;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class OwninfoWidget extends DashClockExtension {

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("OwninfoWidget", "Created");
		ACRA.init(new AcraApplication(getApplicationContext()));

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d("OwninfoWidget", "Fetching phone owner information");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			Log.d("OwninfoWidget", "Get the phone owner from the Me contact");

			Cursor curOwner = getContentResolver()
					.query(Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
							ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
							new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS,
									ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
									ContactsContract.Profile.DISPLAY_NAME, },
							ContactsContract.Contacts.Data.MIMETYPE + " = ?",
							new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
							ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

			if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("heading", "")
					.isEmpty()) {

				edtInformation.expandedTitle(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
						.getString("heading", ""));

			}

			if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("message", "")
					.isEmpty()) {

				edtInformation.expandedBody(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
						.getString("message", ""));

			}

			while (curOwner.moveToNext()) {

				if (!curOwner.getString(curOwner.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)).isEmpty()
						&& edtInformation.expandedTitle() == null) {

					edtInformation.expandedTitle(curOwner.getString(curOwner
							.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)));

				}

				edtInformation
						.clickIntent(new Intent(Intent.ACTION_VIEW).setData(ContactsContract.Profile.CONTENT_URI));

				if (!curOwner.getString(curOwner.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
						.isEmpty() && edtInformation.expandedBody() == null) {

					edtInformation.expandedBody(curOwner.getString(curOwner
							.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));

				}

			}

			edtInformation.visible(true);

			curOwner.close();

			if (new Random().nextInt(5) == 0 && !(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {

				PackageManager mgrPackages = getApplicationContext().getPackageManager();

				try {

					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);

				} catch (NameNotFoundException e) {

					Integer intExtensions = 0;
					Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
					String strPackage;

					for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {

						strPackage = info.serviceInfo.applicationInfo.packageName;
						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0);

					}

					if (intExtensions > 1) {

						edtInformation.visible(true);
						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri
								.parse("market://details?id=com.mridang.donate")));
						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
						edtInformation
								.expandedBody("Thank you for using "
										+ intExtensions
										+ " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
						setUpdateWhenScreenOn(true);

					}

				}

			} else {
				setUpdateWhenScreenOn(false);
			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e("OwninfoWidget", "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("OwninfoWidget", "Done");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();
		Log.d("OwninfoWidget", "Destroyed");

	}

}