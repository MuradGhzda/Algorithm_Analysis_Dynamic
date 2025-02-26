import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//
// * Time complexity analysis:
// * 1. tsp(int mask, int pos):
// *    - This method utilizes dynamic programming to solve the Traveling Salesman Problem (TSP) using bitmasking.
// *    - The time complexity is O(2^N * N^2), where N is the number of cities.
// *    - The function iterates through all possible subsets of cities (2^N) and for each subset, it iterates through all cities (N).
// *    - Within the loop, it performs constant time operations.
// *    - So, the overall time complexity is dominated by the nested loops.
// *
// * 2. printPath(int mask, int pos):
// *    - This method recursively prints the path obtained from backtracking.
// *    - The time complexity is O(N), where N is the number of cities.
// *    - The function recursively backtracks through the parent array, which has at most N elements.
// *    - So, the time complexity is linear with respect to the number of cities.
// *
// * 3. main(String[] args):
// *    - This method reads input data from files, initializes variables, and calls tsp() and printPath() methods.
// *    - The time complexity is dominated by the nested loops used for reading input data.
// *    - Assuming the number of cities is M, the time complexity of reading input data is O(M^2).
// *    - Then, the tsp() and printPath() methods are called, which together contribute O(2^M * M^2) to the overall time complexity.
// *    - So, the overall time complexity is O(M^2 + 2^M * M^2).
// *
// * Overall, the time complexity of the entire program is O(2^N * N^2), where N is the number of cities.
//


class Algo_Analiz_Hw2{

    static int totalLandmarks;
    static int ALL_VISITED;
    static double[][] attractiveness;
    static double[][] travelTime;
    static double[][] dp;
    static int[][] parent;
    static String[] landmarkNames;
    static List<String> paths = new ArrayList<>();
    static double totalAttractiveness = 0.0;

    static double maximizeAttractiveness(int mask, int pos) {
        if (mask == ALL_VISITED) {
            return attractiveness[pos][0];
        }
        if (dp[mask][pos] != -1) {
            return dp[mask][pos];
        }

        double maxScore = Double.MIN_VALUE;
        int nextLandmark = -1;

        for (int landmark = 0; landmark < totalLandmarks; landmark++) {
            if ((mask & (1 << landmark)) == 0) {
                double newScore = attractiveness[pos][landmark] + maximizeAttractiveness(mask | (1 << landmark), landmark);
                if (newScore > maxScore) {
                    maxScore = newScore;
                    nextLandmark = landmark;
                }
            }
        }

        parent[mask][pos] = nextLandmark;
        return dp[mask][pos] = maxScore;
    }

    static void printPath(int mask, int pos) {
        int nextLandmark = parent[mask][pos];
        if (nextLandmark == -1) {
            paths.add(landmarkNames[pos]);
            return;
        }

        printPath(mask | (1 << nextLandmark), nextLandmark);
        paths.add(landmarkNames[pos]);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the total number of landmarks (including Hotel):");
        int number = scanner.nextInt();
        try (BufferedReader br = new BufferedReader(new FileReader("landmark_map_data.txt"));
             BufferedReader interestReader = new BufferedReader(new FileReader("personal_interest.txt"));
             BufferedReader loadReader = new BufferedReader(new FileReader("visitor_load.txt"))) {

            br.readLine(); // Skipping header
            loadReader.readLine();
            interestReader.readLine();

            attractiveness = new double[number][number];
            travelTime = new double[number][number];
            parent = new int[1 << number][number];
            landmarkNames = new String[number];

            Map<String, Double> interestMap = new HashMap<>();
            Map<String, Double> loadMap = new HashMap<>();

            int landmarkIndex = 0;
            String line;
            while ((line = interestReader.readLine()) != null) {
                String[] parts = line.split("\t");
                landmarkNames[landmarkIndex] = parts[0];
                interestMap.put(parts[0], Double.valueOf(parts[1]));
                landmarkIndex++;
            }

            landmarkIndex = 0;
            while ((line = loadReader.readLine()) != null) {
                String[] parts = line.split("\t");
                landmarkNames[landmarkIndex] = parts[0];
                loadMap.put(parts[0], 1.0 - Double.parseDouble(parts[1]));
                landmarkIndex++;
            }

            for (int i = 0; i < number; i++) {
                for (int j = 0; j < number; j++) {
                    if (i != j) {
                        String[] parts = br.readLine().split("\t");
                        String to = parts[1];
                        double baseScore = Double.parseDouble(parts[2]);
                        double time = Double.parseDouble(parts[3]);
                        double val = loadMap.get(to) * interestMap.get(to) * baseScore;
                        attractiveness[i][j] = val;
                        travelTime[i][j] = time;
                    } else {
                        attractiveness[i][j] = 0;
                        travelTime[i][j] = 0;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        totalLandmarks = attractiveness.length;
        ALL_VISITED = (1 << totalLandmarks) - 1;
        dp = new double[1 << totalLandmarks][totalLandmarks];
        for (double[] row : dp) {
            Arrays.fill(row, -1);
        }
        for (int[] row : parent) {
            Arrays.fill(row, -1);
        }

        System.out.println("Tour planning in progress...");
        System.out.println("Maximized total attractiveness score: " + maximizeAttractiveness(1, 0));

        printPath(1, 0);

        Collections.reverse(paths);
        System.out.println("The visited landmarks:");
        for (int i = 0; i < paths.size(); i++) {
            System.out.println((i + 1) + "-" + paths.get(i));
        }
        System.out.println((paths.size() + 1) + "-Hotel");

        for (int i = 0; i < paths.size() - 1; i++) {
            int fromIndex = Arrays.asList(landmarkNames).indexOf(paths.get(i));
            int toIndex = Arrays.asList(landmarkNames).indexOf(paths.get(i + 1));
            totalAttractiveness += travelTime[fromIndex][toIndex];
        }

        int lastIndex = Arrays.asList(landmarkNames).indexOf(paths.get(paths.size() - 1));
        totalAttractiveness += travelTime[lastIndex][0];

        System.out.println("Total Travel Time: " + totalAttractiveness);
    }
}