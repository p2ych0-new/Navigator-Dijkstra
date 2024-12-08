import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Navigation {
    private static class Coordinate {
        String city;
        int x;
        int y;

        Coordinate(String city, int x, int y) {
            this.city = city;
            this.x = x;
            this.y = y;
        }
    }

    private static class Connection {
        String cityA;
        String cityB;
        double distance;

        Connection(String cityA, String cityB, double distance) {
            this.cityA = cityA;
            this.cityB = cityB;
            this.distance = distance;
        }
    }

    private static class Node implements Comparable<Node> {
        int city;
        double distance;

        Node(int city, double distance) {
            this.city = city;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    private static class CustomPriorityQueue {
        private List<Node> queue;

        CustomPriorityQueue() {
            queue = new ArrayList<>();
        }

        void offer(Node node) {
            queue.add(node);
            Collections.sort(queue);
        }

        Node poll() {
            if (isEmpty()) {
                return null;
            }
            return queue.remove(0);
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    private ArrayList<Coordinate> cityCoordinates;
    private ArrayList<ArrayList<Connection>> connections;
    private double[] distances;

    public Navigation() {
        cityCoordinates = new ArrayList<>();
        connections = new ArrayList<>();
        readCityCoordinates("city_coordinates.txt");
        readCityConnections("city_connections.txt");
    }

    private void readCityCoordinates(String filename) { //reads the data in the file in accordance with the format in the file named city_coordinates
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String city = parts[0].trim();
                int x = Integer.parseInt(parts[1].trim());
                int y = Integer.parseInt(parts[2].trim());
                cityCoordinates.add(new Coordinate(city, x, y));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCityConnections(String filename) { //reads the data in the file in accordance with the format in the file named city_connections
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cities = line.split(",");
                String cityA = cities[0].trim();
                String cityB = cities[1].trim();
                Coordinate coordA = cityCoordinates.get(getCityIndex(cityA));
                Coordinate coordB = cityCoordinates.get(getCityIndex(cityB));
                double distance = calculateDistance(coordA.x, coordA.y, coordB.x, coordB.y);
                Connection connection = new Connection(cityA, cityB, distance);
                int indexA = getCityIndex(cityA);
                int indexB = getCityIndex(cityB);
                while (connections.size() <= Math.max(indexA, indexB)) {
                    connections.add(new ArrayList<>());
                }
                connections.get(indexA).add(connection);
                connections.get(indexB).add(new Connection(cityB, cityA, distance));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCityIndex(String city) { //finds the index of a city in the cityCoordinates list
        for (int i = 0; i < cityCoordinates.size(); i++) {
            if (cityCoordinates.get(i).city.equals(city)) {
                return i;
            }
        }
        return -1;
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) { //calculates the distance between the points (x1,y1) and (x2,y2)
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private int[] dijkstra(int start, int end) { //finds shortest path between two cities
        int n = cityCoordinates.size();
        distances = new double[n];
        int[] previousNodes = new int[n];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        Arrays.fill(previousNodes, -2);
        distances[start] = 0.0;
        previousNodes[start] = -1;
        CustomPriorityQueue customPQ = new CustomPriorityQueue();
        customPQ.offer(new Node(start, 0.0));

        while (!customPQ.isEmpty()) {
            Node currentNode = customPQ.poll();
            int currentCity = currentNode.city;
            double currentDistance = currentNode.distance;

            if (currentDistance > distances[currentCity]) {
                continue;
            }

            if (currentCity == end) {
                break;
            }

            ArrayList<Connection> neighborConnections = connections.get(currentCity);
            if (neighborConnections != null) {
                for (Connection connection : neighborConnections) {
                    int neighborCity = getCityIndex(connection.cityB);
                    double newDistance = currentDistance + connection.distance;
                    if (newDistance < distances[neighborCity]) {
                        distances[neighborCity] = newDistance;
                        previousNodes[neighborCity] = currentCity;
                        customPQ.offer(new Node(neighborCity, newDistance));
                    }
                }
            }
        }

        return previousNodes;
    }

    private ArrayList<String> reconstructPath(int start, int end, int[] previousNodes) { //reconstructs the shortest path based on the previousNodes array
        ArrayList<String> path = new ArrayList<>();
        int currentCity = end;
        if (previousNodes[currentCity] == -2) {
            return null;
        }
        path.add(0, cityCoordinates.get(currentCity).city);

        while (currentCity != start) {
            currentCity = previousNodes[currentCity];
            if (currentCity == -2) {
                return null;
            }
            path.add(0, cityCoordinates.get(currentCity).city);
        }

        return path;
    }

    private void displayCityConnections(ArrayList<String> path) { //Std part
        StdDraw.setCanvasSize(2377/2, 1055/2);
        StdDraw.setXscale(0, 2377);
        StdDraw.setYscale(0, 1055);

        StdDraw.picture(2377/2,1055/2,"map.png",2377,1055);
        StdDraw.enableDoubleBuffering();

        StdDraw.setPenRadius(0.003);
        for (Coordinate city : cityCoordinates) {
            StdDraw.setPenColor(StdDraw.GRAY);
            StdDraw.filledCircle(city.x, city.y, 10);
            StdDraw.text(city.x, city.y + 20, city.city);
        }

        for (ArrayList<Connection> connectionList : connections) {
            for (Connection connection : connectionList) {
                Coordinate cityA = cityCoordinates.get(getCityIndex(connection.cityA));
                Coordinate cityB = cityCoordinates.get(getCityIndex(connection.cityB));
                StdDraw.setPenColor(StdDraw.GRAY);
                StdDraw.line(cityA.x, cityA.y, cityB.x, cityB.y);
            }
        }

        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.setPenRadius(0.01);
        for (int i = 0; i < path.size() - 1; i++) {
            Coordinate cityA = cityCoordinates.get(getCityIndex(path.get(i)));
            Coordinate cityB = cityCoordinates.get(getCityIndex(path.get(i + 1)));
            StdDraw.line(cityA.x, cityA.y, cityB.x, cityB.y);
        }

        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        for (String cityName : path) {
            Coordinate city = cityCoordinates.get(getCityIndex(cityName));
            StdDraw.text(city.x, city.y + 20, city.city);
        }

        StdDraw.show();
    }

    public static void main(String[] args) {
        Navigation navigation = new Navigation();
        Scanner scanner = new Scanner(System.in);
        int j = 0;

        while (true && j < 1)
        {
            System.out.print("Enter starting city: ");
            String startCity = scanner.nextLine().trim();
            int startIndex = navigation.getCityIndex(startCity);
            if(startIndex == -1) {
                while(startIndex == -1) {
                    System.out.println("City named '" + startCity + "' not found. Please enter a valid city name.");
                    System.out.print("Enter starting city again: ");
                    startCity = scanner.nextLine().trim();
                    startIndex = navigation.getCityIndex(startCity);
                }
            }

            System.out.print("Enter destination city: ");
            String endCity = scanner.nextLine().trim();
            int endIndex = navigation.getCityIndex(endCity);
            if (endIndex == -1) {
                while(endIndex == -1) {
                    System.out.println("City named '" + endCity + "' not found. Please enter a valid city name.");
                    System.out.print("Enter end city again: ");
                    endCity = scanner.nextLine().trim();
                    endIndex = navigation.getCityIndex(endCity);
                }
            }

            int[] previousNodes = navigation.dijkstra(startIndex, endIndex);
            ArrayList<String> path = navigation.reconstructPath(startIndex, endIndex, previousNodes);

            if (path == null) {
                System.out.println("No path could be found");
                System.exit(0);
            }

            double totalDistance = navigation.distances[endIndex];

            System.out.printf("Total Distance: %.2f. Path: ", totalDistance);
            for (int i = 0; i < path.size(); i++) {
                System.out.print(path.get(i));
                if (i != path.size() - 1) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();

            navigation.displayCityConnections(path);
            j = j+1;
        }
    }
}