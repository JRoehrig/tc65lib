package org.javacint.otap;

import com.siemens.icm.io.ATCommand;
import java.io.IOException;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.BufferedReader;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;

/**
 * Program auto-updating class.
 *
 * This class checks if the current version matches the target version. If not,
 * it triggers the OTAP process.
 *
 * The class retries at each scheduled period to fetch the version number. If it
 * could fetch it, it doesn't retry until the "timeBetweenUpdates" period has
 * expired. If the fetched version is superior to the current version, the OTAP
 * process is triggered.
 *
 */
public class AutoUpdater extends TimerTask {

	private final ATCommand atc;
	private final String version;
	private final String url;
	private final int timeBetweenUpdates;
	private final String FIELD_VERSION = "MIDlet-Version:";
	private long lastDone;
	private Runnable before;

	/**
	 * Constructor.
	 *
	 * @param atc ATCommand instance used to launch the OTAP
	 * @param version Current version of the program ("Midlet-Version" is
	 * usually stored in Global.version)
	 * @param url Url of the file containing the version number
	 * @param timeBetweenUpdates Time in milliseconds between updates
	 *
	 * Note: the scheduling should be more frequent than the time between
	 * updates (like every hour) because fetching the version might fail.
	 *
	 * Sample usage: <pre>
	 * AutoUpdater up = new AutoUpdate(Global.atc, Global.version, "http://server/version.txt");
	 * Global.slowTimer.schedule ( up, 0, 3600 * 1000 );
	 * </pre> This will make it check the version now and then every
	 * [timeBetweenUpdates] period (in millisecond). If it fails, it will check
	 * every hour until it has a response from the server.
	 */
	public AutoUpdater(ATCommand atc, String version, String url, int timeBetweenUpdates) {
		this.atc = atc;
		this.version = version;
		this.url = url;
		this.timeBetweenUpdates = timeBetweenUpdates;
	}

	public void setBefore(Runnable before) {
		this.before = before;
	}

	/**
	 * Constructor.
	 *
	 * The timeBeforeUpdate arg is set to a default value of 24h (24*3600*1000
	 * milliseconds)
	 *
	 * @param atc ATCommand instance used to launch the OTAP
	 * @param version Current version of the program ("Midlet-Version" is
	 * usually stored in Global.version)
	 * @param url Url of the file containing the version number
	 */
	public AutoUpdater(ATCommand atc, String version, String url) {
		this(atc, version, url, 24 * 3600 * 1000);
	}

	/**
	 * Constructor.
	 *
	 * Update the program to the default JADURL file.
	 *
	 * @param atc
	 * @param version
	 */
	public AutoUpdater(ATCommand atc, String version) {
		this(atc, version, Settings.getSetting(Settings.SETTING_JADURL));
	}

	public void run() {
		try {
			if (needsUpdate()) {
//				if (Logger.BUILD_DEBUG) {
//					Logger.log("We need to perform an update !");
//				}
				// At this stage, we need the "apn" and "jadurl" settings to be defined

				if (before != null) {
					try {
						before.run();
					} catch (Throwable ex) {
					}
				}
				ATExecution.update(atc);
			}
		} catch (Exception ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log(this + ".run", ex, true);
			}
		}
	}

	/**
	 * Tests if the device needs an update.
	 *
	 * @return If the device needs an update
	 * @throws IOException
	 */
	private boolean needsUpdate() throws IOException {
		if ((System.currentTimeMillis() - lastDone) < timeBetweenUpdates) {
			return false;
		}
		HttpConnection conn = (HttpConnection) Connector.open(url);
		{
//			try {
			conn.setRequestProperty("user-agent", "tc65lib/" + ATExecution.getImei(atc));
//			} catch (Exception ex) {
//				if (Logger.BUILD_CRITICAL) {
//					Logger.log(this + ".needsUpdate", ex, true);
//				}
//			}
			int rc = conn.getResponseCode();
			if (rc != HttpConnection.HTTP_OK) {
				if (Logger.BUILD_NOTICE) {
					Logger.log("Could not fetch the version's file !", true);
				}
				cancel();
			}
		}

		String remoteVersion = null;
		BufferedReader reader = new BufferedReader(conn.openInputStream());
		if (url.endsWith(".jad")) { // If it's a jad file
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(FIELD_VERSION)) {
					remoteVersion = line.substring(FIELD_VERSION.length()).trim();
					break;
				}
			}
		} else { // else it's a file with just a version file
			remoteVersion = reader.readLine();
		}
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ": remoteVersion=" + remoteVersion);
		}
		lastDone = System.currentTimeMillis();
		boolean needIt = compareVersions(remoteVersion, version) > 0;
		if (needIt) {
			if (Logger.BUILD_NOTICE) {
				Logger.log(this + " : We will update from " + version + " to " + remoteVersion, true);
			}
		} else {
			if (Logger.BUILD_NOTICE) {
				Logger.log(this + " : No update is required !");
			}
		}
		return needIt;
	}

	/**
	 * Compares two versions of the code.
	 *
	 * @param v1 String of the version
	 * @param v2 String of the version
	 * @return > 0 if v1 > v2
	 *
	 * Note: The two versions SHOULD have the same length. If not, only the
	 * first numbers are taken into account. Thus: "1.3" == "1.3.2.1"
	 */
	private int compareVersions(String v1, String v2) {
		String va1[] = Strings.split('.', v1);
		String va2[] = Strings.split('.', v2);

		for (int i = 0; i < va1.length && i < va2.length; i++) {
			int i1 = Integer.parseInt(va1[i]);
			int i2 = Integer.parseInt(va2[i]);
			if (i1 != i2) {
				return i1 - i2;
			}
		}
		return 0;
	}

	public String toString() {
		return "AutoUpdater";
	}
}
