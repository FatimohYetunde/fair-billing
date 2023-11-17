import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FairBilling {

    static class LogData {
        String user;
        Date time;
        boolean isStart;

        LogData(String user, Date time, boolean isStart) {
            this.user = user;
            this.time = time;
            this.isStart = isStart;
        }
    }

    public static void main(String[] args) {

        // Check to see if the program was run with the command line argument
        if (args.length < 1) {
            System.out.println("Error, usage: java ClassName inputfile");
            System.exit(1);
        }

        String filePath = args[0];
        List<LogData> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            while ((line = br.readLine()) != null) {
                String[] dataArr = line.split(" ");
                // split data in file and put in variable
                if (dataArr.length == 3) {
                    String timeStr = dataArr[0];
                    String user = dataArr[1];
                    String session = dataArr[2];

                    if (session.equals("Start") || session.equals("End")) {
                        try {
                            Date time = dateFormat.parse(timeStr);
                            boolean isStart = session.equals("Start");
                            records.add(new LogData(user, time, isStart));
                        } catch (ParseException e) {
                            // Ignore invalid time format
                        }
                    }
                }
            }

            Map<String, Integer> sessionCount = new HashMap<>();
            Map<String, Long> totalDuration = new HashMap<>();
            Map<String, Date> lastEndTime = new HashMap<>();
            Date earliestTime = records.isEmpty() ? null : records.get(0).time;
            Date latestTime = null;

            // Applying logic to check if data has start time or not, count the duration and
            // assign to the user.
            for (LogData record : records) {
                String user = record.user;

                if (!sessionCount.containsKey(user)) {
                    sessionCount.put(user, 0);
                    totalDuration.put(user, 0L);
                    lastEndTime.put(user, earliestTime);
                }

                if (record.isStart) {
                    sessionCount.put(user, sessionCount.get(user) + 1);
                } else {
                    Date startTime = lastEndTime.get(user);
                    if (startTime == null) {
                        startTime = earliestTime;
                    }

                    long duration = (record.time.getTime() - startTime.getTime()) / 1000;
                    totalDuration.put(user, totalDuration.get(user) + duration);
                    lastEndTime.put(user, record.time);
                }

                if (latestTime == null || record.time.after(latestTime)) {
                    latestTime = record.time;
                }
            }

            for (Map.Entry<String, Integer> entry : sessionCount.entrySet()) {
                String user = entry.getKey();
                int sessions = entry.getValue();
                long duration = totalDuration.get(user);
                System.out.println(user + " " + sessions + " " + duration);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
