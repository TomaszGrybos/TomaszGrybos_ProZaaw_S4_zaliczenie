import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class Main {
    // Constants
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 3; // Change this to 3

    // Shared resources
    private static final Map<String, List<Object>> objectMap = new HashMap<>();
    private static final Queue<ClientHandler> waitingClients = new LinkedList<>();
    private static final Set<ClientHandler> activeClients = ConcurrentHashMap.newKeySet();

    // Add a counter for served clients
    private static int servedClients = 0;

    // Main method
    public static void main(String[] args) {
        if (args.length == 0) {
            startServer();
        } else {
            int clientId = Integer.parseInt(args[0]);
            new Thread(new ClientThread(clientId)).start();
        }
    }

    // Server logic
    private static void startServer() {
        // Initialize objects
        initializeObjects();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer uruchomiony...");
            while (true) {
                if (servedClients >= MAX_CLIENTS) {
                    System.out.println("Serwer osiągnął maksymalną liczbę klientów. Zamykanie...");
                    break;
                }

                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                synchronized (waitingClients) {
                    if (activeClients.size() < MAX_CLIENTS) {
                        activeClients.add(clientHandler);
                        new Thread(clientHandler).start();
                        servedClients++; // Increment the counter when a client is served
                    } else {
                        waitingClients.add(clientHandler);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeObjects() {
        List<Object> koty = new ArrayList<>();
        List<Object> psy = new ArrayList<>();
        List<Object> ptaki = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            koty.add(new Kot("Kot_" + i));
            psy.add(new Pies("Pies_" + i));
            ptaki.add(new Ptak("Ptak_" + i));
        }

        objectMap.put("koty", koty);
        objectMap.put("psy", psy);
        objectMap.put("ptaki", ptaki);
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                clientId = in.readInt();
                out.writeObject("OK");
                out.flush();
                System.out.println("ID klienta: " + clientId);
                System.out.println("Klient " + clientId + " połączony.");

                while (true) {
                    try {
                        String request = (String) in.readObject();
                        System.out.println("Klient " + clientId + " zażądał: " + request);

                        String key = request.split("_")[1].toLowerCase();
                        List<Object> objects = objectMap.getOrDefault(key, new ArrayList<>());

                        out.writeObject(objects);
                        out.flush();
                        System.out.println("Wysłano obiekty typu " + key + " do klienta " + clientId + ": " + objects);
                    } catch (EOFException | SocketException e) {
                        break;
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (waitingClients) {
                    activeClients.remove(this);
                    if (!waitingClients.isEmpty()) {
                        ClientHandler nextClient = waitingClients.poll();
                        activeClients.add(nextClient);
                        new Thread(nextClient).start();
                    }
                }
                System.out.println("Klient " + clientId + " rozłączony.");
            }
        }
    }

    // Client logic
    private static class ClientThread implements Runnable {
        private int clientId;

        public ClientThread(int clientId) {
            this.clientId = clientId;
        }

        @Override
        public void run() {
            startClient(clientId);
        }
    }

    private static void startClient(int clientId) {
        try (Socket socket = new Socket("localhost", PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Klient " + clientId + ": Łączenie z serwerem...");
            out.writeInt(clientId);
            out.flush();

            String response = (String) in.readObject();
            System.out.println("Klient " + clientId + ": Odpowiedź z serwera: " + response);

            if ("REFUSED".equals(response)) {
                System.out.println("Klient " + clientId + ": Połączenie odrzucone. Zamykanie...");
                return;
            } else if ("Server has reached the maximum number of clients. Shutting down...".equals(response)) {
                System.out.println("Klient " + clientId + ": Serwer nie może obsłużyć więcej klientów. Zamykanie...");
                return;
            } else {
                System.out.println("Klient " + clientId + ": Połączono pomyślnie.");
            }

            // Change the requests based on the clientId
            if (clientId == 1) {
                requestObjects(in, out, clientId, "get_Koty");
            } else if (clientId == 2) {
                requestObjects(in, out, clientId, "get_Psy");
            } else if (clientId == 3) {
                requestObjects(in, out, clientId, "get_Ptaki");
            }
        } catch (ConnectException e) {
            System.out.println("Klient " + clientId + ": Nie można połączyć się z serwerem. Może być wyłączony lub osiągnął maksymalną pojemność. Proszę spróbować później.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void requestObjects(ObjectInputStream in, ObjectOutputStream out, int clientId, String request) throws IOException, ClassNotFoundException {
        System.out.println("Klient " + clientId + ": Żądanie " + request);
        // Add a random delay before sending the request
        try {
            int delay = new Random().nextInt(5000) + 1000; // Random delay between 1000ms (1 second) and 5000ms (5 seconds)
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out.writeObject(request);
        out.flush();

        try {
            List<Object> objects = (List<Object>) in.readObject();
            System.out.println("Klient " + clientId + ": Otrzymano " + request + ": " + objects);
        } catch (ClassCastException e) {
            System.out.println("Klient " + clientId + ": Błąd rzutowania otrzymanego obiektu.");
        }
    }
}

// Object classes
class Kot implements Serializable {
    private final String name;

    public Kot(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Kot{nazwa='" + name + "'}";
    }
}

class Pies implements Serializable {
    private final String name;

    public Pies(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pies{nazwa='" + name + "'}";
    }
}

class Ptak implements Serializable {
    private final String name;

    public Ptak(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Ptak{nazwa='" + name + "'}";
    }
}
