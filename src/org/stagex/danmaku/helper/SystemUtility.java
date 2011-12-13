package org.stagex.danmaku.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import tv.acfun.R;
import android.os.Build;
import android.os.Environment;

public class SystemUtility {

	static {
		System.loadLibrary("vlccore");
	}

	public static native int setenv(String name, String value, boolean overwrite);

	private static int sArmArchitecture = -1;

	public static int getArmArchitecture() {
		if (sArmArchitecture != -1)
			return sArmArchitecture;
		try {
			InputStream is = new FileInputStream("/proc/cpuinfo");
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			try {
				String name = "CPU architecture";
				while (true) {
					String line = br.readLine();
					String[] pair = line.split(":");
					if (pair.length != 2)
						continue;
					String key = pair[0].trim();
					String val = pair[1].trim();
					if (key.compareToIgnoreCase(name) == 0) {
						String n = val.substring(0, 1);
						sArmArchitecture = Integer.parseInt(n);
						break;
					}
				}
			} finally {
				br.close();
				ir.close();
				is.close();
				if (sArmArchitecture == -1)
					sArmArchitecture = 6;
			}
		} catch (Exception e) {
			sArmArchitecture = 6;
		}
		return sArmArchitecture;
	}

	public static int getSDKVersionCode() {
		// TODO: fix this
		return Build.VERSION.SDK_INT;
	}

	public static String getExternalStoragePath() {
		boolean exists = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (exists)
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		else
			return "/";
	}

	@SuppressWarnings("rawtypes")
	public static int getDrawableId(String name) {
		int result = -1;
		try {
			Class clz = Class.forName(R.drawable.class.getName());
			Field field = clz.getField(name);
			result = field.getInt(new R.drawable());
		} catch (Exception e) {
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public static Object realloc(Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType,
				newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	public static String getTimeString(int msec) {
		if (msec < 0) {
			return String.format("--:--:--");
		}
		int total = msec / 1000;
		int hour = total / 3600;
		total = total % 3600;
		int minute = total / 60;
		int second = total % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	protected static String sTempPath = "/data/local/tmp";

	public static String getTempPath() {
		return sTempPath;
	}

	public static boolean simpleHttpGet(String url, String file) {
		try {
			HttpGet request = new HttpGet(url);
			request.setHeader("Accept-Encoding", "gzip");
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return false;
			}
			HttpEntity entity = response.getEntity();
			Header contentEncoding = entity.getContentEncoding();
			InputStream is = entity.getContent();
			if (contentEncoding != null
					&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				is = new GZIPInputStream(is);
			}
			OutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			while (true) {
				int count = is.read(buffer);
				if (count <= 0)
					break;
				os.write(buffer, 0, count);
			}
			os.close();
			is.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static int getStringHash(String s) {
		byte[] target = s.getBytes();
		int hash = 1315423911;
		for (int i = 0; i < target.length; i++) {
			byte val = target[i];
			hash ^= ((hash << 5) + val + (hash >> 2));
		}
		hash &= 0x7fffffff;
		return hash;
	}

}
