import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class Main {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 3;

    private static final Map<String, List<Object>> objectMap = new HashMap<>();
    private static final Set<ClientHandler> activeClients = ConcurrentHashMap.newKeySet();

    private static volatile boolean running = true;

    public static void main(String[] args) {
        if (args.length == 0) {
            startServer();
        } else {
            int clientId = Integer.parseInt(args[0]);
            String objectType = args[1];
            new Thread(new ClientThread(clientId,objectType)).start();
        }
    }

    private static void startServer() {
        initializeObjects();

        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running) {
                    if (scanner.nextLine().trim().equalsIgnoreCase("STOP")) {
                        running = false;
                        System.out.println("Serwer zamyka się...");
                    }
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer uruchomiony...");
            serverSocket.setSoTimeout(1000);

            while (running) {
                if (!running) break;

                try {
                    Socket clientSocket = serverSocket.accept();

                    synchronized (activeClients) {
                        if (activeClients.size() < MAX_CLIENTS) {
                            ClientHandler clientHandler = new ClientHandler(clientSocket);
                            activeClients.add(clientHandler);
                            new Thread(clientHandler).start();
                        } else {
                            try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                                System.err.println("Przekroczono limit aktywnych klientów, odrzucono połączenie klienta ");
                                out.writeObject("REFUSED");
                                out.flush();
                            }
                            clientSocket.close();
                        }
                    }
                } catch (SocketException e) {
                    if (!running) break;
                }
                catch (SocketTimeoutException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (activeClients) {
            for (ClientHandler clientHandler : activeClients) {
                try {
                    clientHandler.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Serwer został zamknięty.");
    }

    private static void initializeObjects() {
        List<Object> koty = new ArrayList<>();
        List<Object> psy = new ArrayList<>();
        List<Object> ptaki = new ArrayList<>();

        koty.add(new Kot("Kot_1", "Siberian", 2));
        koty.add(new Kot("Kot_2", "Persian", 3));
        koty.add(new Kot("Kot_3", "Maine Coon", 1));
        koty.add(new Kot("Kot_4", "British Shorthair", 4));

        psy.add(new Pies("Pies_1", "Small", true));
        psy.add(new Pies("Pies_2", "Medium", false));
        psy.add(new Pies("Pies_3", "Large", true));
        psy.add(new Pies("Pies_4", "Medium", true));

        ptaki.add(new Ptak("Ptak_1", "Red", 0.25));
        ptaki.add(new Ptak("Ptak_2", "Green", 0.30));
        ptaki.add(new Ptak("Ptak_3", "Blue", 0.20));
        ptaki.add(new Ptak("Ptak_4", "Yellow", 0.35));

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

        public int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
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

                while (running) {
                    try {
                        String request = (String) in.readObject();
                        System.out.println("Klient " + clientId + " zażądał: " + request);

                        String key = request.split("_")[1].toLowerCase();
                        if (!objectMap.containsKey(key)) {
                            key = (String) objectMap.keySet().toArray() [getRandomNumber(0,objectMap.size())];
                            System.out.println("Klient " + clientId + " żąda nieistniejącego typu objektów, zwracam : " + key);
                        }
                        List<Object> objects = objectMap.get(key);

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
                synchronized (activeClients) {
                    activeClients.remove(this);
                }
                System.out.println("Klient " + clientId + " rozłączony.");
            }
        }

        public void closeConnection() throws IOException {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }

    private static class ClientThread implements Runnable {
        private int clientId;
        private String objectType;

        public ClientThread(int clientId,String objectType) {
            this.clientId = clientId;
            this.objectType = objectType;
        }

        @Override
        public void run() {
            startClient(clientId,objectType);
        }
    }

    private static void startClient(int clientId, String objectType) {
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
            } else {
                System.out.println("Klient " + clientId + ": Połączono pomyślnie.");
            }

            requestObjects(in, out, clientId, "get_"+objectType);

        } catch (ConnectException e) {
            System.out.println("Klient " + clientId + ": Nie można połączyć się z serwerem. Może być wyłączony lub osiągnął maksymalną pojemność. Proszę spróbować później.");
        } catch (SocketException e) {
            System.out.println("Klient " + clientId + ": Połączenie zostało przerwane. Serwer może być wyłączony.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void requestObjects(ObjectInputStream in, ObjectOutputStream out, int clientId, String request) throws IOException, ClassNotFoundException {
        System.out.println("Klient " + clientId + ": Żądanie " + request);
        try {
            int delay = new Random().nextInt(10000) + 5000;
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

class Kot implements Serializable {
    private final String name;
    private final String breed;
    private final int age;

    public Kot(String name, String breed, int age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Kot{nazwa='" + name + "', rasa='" + breed + "', wiek=" + age + "}";
    }
}

class Pies implements Serializable {
    private final String name;
    private final String size;
    private final boolean isTrained;

    public Pies(String name, String size, boolean isTrained) {
        this.name = name;
        this.size = size;
        this.isTrained = isTrained;
    }

    @Override
    public String toString() {
        return "Pies{nazwa='" + name + "', rozmiar='" + size + "', wyszkolony=" + isTrained + "}";
    }
}

class Ptak implements Serializable {
    private final String name;
    private final String color;
    private final double wingspan;

    public Ptak(String name, String color, double wingspan) {
        this.name = name;
        this.color = color;
        this.wingspan = wingspan;
    }

    @Override
    public String toString() {
        return "Ptak{nazwa='" + name + "', kolor='" + color + "', rozpiętość skrzydeł=" + wingspan + "}";
    }
}
