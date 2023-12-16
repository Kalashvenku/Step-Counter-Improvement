import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DefaultStepCounter implements StepCounter {

    public static ArrayList<Double> getPeakValuesFromIndexes(ArrayList<Integer> peakIndexes, ArrayList<Double> mags) {
        ArrayList<Double> peaks = new ArrayList<>();
        for (int i = 0; i < peakIndexes.size(); i++) {
            double val = mags.get(peakIndexes.get(i));
            peaks.add(val);
        }
        return peaks;
    }

    public static ArrayList<Integer> getPeakIndexes(ArrayList<Double> mags) {
        ArrayList<Integer> peakIndexes = new ArrayList<>();

        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i - 1) < mags.get(i) && mags.get(i) > mags.get(i + 1)) {
                peakIndexes.add(i);
            }
        }
        return peakIndexes;
    }

    public static int countPeaks(ArrayList<Double> mags) {
        int count = 0;
        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i - 1) < mags.get(i) && mags.get(i) > mags.get(i + 1)) {
                count++;
            }
        }
        return count;
    }

    public static ArrayList<Double> calculateMagnitudes(ArrayList<Double> accX, ArrayList<Double> accY, ArrayList<Double> accZ) {
        ArrayList<Double> output = new ArrayList<>();
        if (accX.size() != accY.size() || accX.size() != accZ.size() || accY.size() != accZ.size()) {
            System.out.println("WARNING: x, y, z acceleration lists not the same length");
        }

        for (int i = 0; i < accX.size(); i++) {
            double mag = magnitude(accX.get(i), accY.get(i), accZ.get(i));
            output.add(mag);
        }
        return output;
    }

    private static double magnitude(Double x, Double y, Double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static ArrayList<Double> getColumnAsList(String[] lines, int colNum) {
        ArrayList<Double> output = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {  // start at 1 to skip the header line
            String line = lines[i];

            String[] values = line.split(",");
            String columnValue = values[colNum];
            double valueAsDouble = Double.parseDouble(columnValue.trim());
            output.add(valueAsDouble);
        }

        return output;
    }

    @Override
    public int countSteps(ArrayList<Double> xAcc, ArrayList<Double> yAcc, ArrayList<Double> zAcc, ArrayList<Double> xGyro, ArrayList<Double> yGyro, ArrayList<Double> zGyro) {
        ArrayList<Double> mags = calculateMagnitudes(xAcc, yAcc, zAcc);
        int count = 0;
        boolean passedLower = true;
        double threshold = MainTester.threshold;
        double lowerThreshold = MainTester.lowerThreshold;
        for (double mag : mags){
            if (mag <= lowerThreshold){
                passedLower = true;
            }
            if (lowerThreshold == 50){passedLower = true;}
            if (mag >= threshold && passedLower){
                count++;
                passedLower = false;
            }
        }
        return count;
    }

    private void smooth(ArrayList<Double> mags) {
        for (int i = 2; i < mags.size()-2; i++){
            double val = mags.get(i);
            val = mags.get(i-2)/6 + mags.get(i-1)/6 + val/3 + mags.get(i+1)/6 + mags.get(i+2)/6;
            mags.set(i,val);
        }
    }

    @Override
    public int countSteps(String csvFileText) {
        String[] lines = csvFileText.split("\n");

        ArrayList<Double> accX = getColumnAsList(lines, 0);
        ArrayList<Double> accY = getColumnAsList(lines, 1);
        ArrayList<Double> accZ = getColumnAsList(lines, 2);
        ArrayList<Double> gyroX = getColumnAsList(lines, 3);
        ArrayList<Double> gyroY = getColumnAsList(lines, 4);
        ArrayList<Double> gyroZ = getColumnAsList(lines, 5);

        return countSteps(accX, accY, accZ, gyroX, gyroY, gyroZ);
    }
}


