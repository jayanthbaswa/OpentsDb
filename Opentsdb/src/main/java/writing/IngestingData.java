package writing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

import net.opentsdb.core.TSDB;
import net.opentsdb.uid.NoSuchUniqueName;
import net.opentsdb.uid.UniqueId.UniqueIdType;
import net.opentsdb.utils.Config;

/**
 * Examples for how to add points to the tsdb.
 *
 */
public class IngestingData {
    private static String pathToConfigFile;

    public static void processArgs(final String[] args) {
        // Set these as arguments so you don't have to keep path information in
        // source files
        if (args != null && args.length > 0) {
            pathToConfigFile = args[0];
        }
    }

    public static void main(final String[] args) throws Exception {
        processArgs(args);

        // Create a config object with a path to the file for parsing. Or manually
        // override settings.
        // e.g. config.overrideConfig("tsd.storage.hbase.zk_quorum", "localhost");
        final Config config;
        if (pathToConfigFile != null && !pathToConfigFile.isEmpty()) {
            config = new Config(pathToConfigFile);
        } else {
            // Search for a default config from /etc/opentsdb/opentsdb.conf, etc.
            config = new Config(true);
        }
        final TSDB tsdb = new TSDB(config);

        // Declare new metric
        String metricName = "population.generated";
        // First check to see it doesn't already exist
        byte[] byteMetricUID; // we don't actually need this for the first
        // .addPoint() call below.
        // TODO: Ideally we could just call a not-yet-implemented tsdb.uIdExists()
        // function.
        // Note, however, that this is optional. If auto metric is enabled
        // (tsd.core.auto_create_metrics), the UID will be assigned in call to
        // addPoint().
        try {
            byteMetricUID = tsdb.getUID(UniqueIdType.METRIC, metricName);
        } catch (IllegalArgumentException iae) {
            System.out.println("Metric name not valid.");
            iae.printStackTrace();
            System.exit(1);
        } catch (NoSuchUniqueName nsune) {
            // If not, great. Create it.
            byteMetricUID = tsdb.assignUid("metric", metricName);
        }

        String fileName = "/home/jay/Documents/datapop.txt";
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line;
        int n = 840;
        ArrayList<Deferred<Object>> deferreds = new ArrayList<Deferred<Object>>(n);
        Map<String, String> tags = new HashMap<String, String>(1);
        while((line = br.readLine()) != null) {
            String[] splitted = line.split(" ");
            System.out.println(splitted[1] + " " + splitted[2] + " " + splitted[3] + " " + splitted[4]);
            String [] tags1=splitted[3].split("=");
            tags.put(tags1[0],tags1[1]);
            String [] tags2=splitted[4].split("=");
            tags.put(tags2[0],tags2[1]);
            Deferred<Object> deferred = tsdb.addPoint(metricName, Long.parseLong(splitted[1]), Long.parseLong(splitted[2]) , tags);
            deferreds.add(deferred);
            tags.clear();
        }
        br.close();
        // Make a single datum
        long timestamp =1591488000;
        long value = 2;
        // Make key-val

        // Start timer
        long startTime1 = System.currentTimeMillis();

        // Write a number of data points at 30 second intervals. Each write will
        // return a deferred (similar to a Java Future or JS Promise) that will
        // be called on completion with either a "null" value on success or an
        // exception.


        // Add the callbacks to the deferred object. (They might have already
        // returned, btw)
        // This will cause the calling thread to wait until the add has completed.
        System.out.println("Waiting for deferred result to return...");
        Deferred.groupInOrder(deferreds)
                .addErrback(new IngestingData().new errBack())
                .addCallback(new IngestingData().new succBack())
                // Block the thread until the deferred returns it's result.
                .join();
        // Alternatively you can add another callback here or use a join with a
        // timeout argument.

        // End timer.
        long elapsedTime1 = System.currentTimeMillis() - startTime1;
        System.out.println("\nAdding " + n + " points took: " + elapsedTime1
                + " milliseconds.\n");

        // Gracefully shutdown connection to TSDB. This is CRITICAL as it will
        // flush any pending operations to HBase.
        tsdb.shutdown().join();
    }

    // This is an optional errorback to handle when there is a failure.
    class errBack implements Callback<String, Exception> {
        public String call(final Exception e) throws Exception {
            String message = ">>>>>>>>>>>Failure!>>>>>>>>>>>";
            System.err.println(message + " " + e.getMessage());
            e.printStackTrace();
            return message;
        }
    };

    // This is an optional success callback to handle when there is a success.
    class succBack implements Callback<Object, ArrayList<Object>> {
        public Object call(final ArrayList<Object> results) {
            System.out.println("Successfully wrote " + results.size() + " data points");
            return null;
        }
    };

}