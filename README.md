
## Wymagania
# Pobierz repozytorium z github 

git clone https://github.com/TomaszGrybos/TomaszGrybos_ProZaaw_S4_zaliczenie.git

git checkout master

# Otwórz projekt ProZaaw_S4_ProjektAplikacja.iml w IntellijJ IDEA wersja 2024.1.3 ultimate (evaluation)

# W module settings upewnij się, że jest poprawnie skonfigurowana openjdk 19

# Zbuduj/skompiluj projekt
 

## Przykład uruchomienia:

# Okno 1 - serwer
Uruchom klasę Main bez parametrów, co spowoduje uruchomienie programu w trybie serwera.
np. przez Server.cmd lub polecenia:

set PATH=C:\Users\Tomek\.jdks\openjdk-19.0.2\bin;%PATH%
cd .\out\production\ProZaaw_S4_ProjektAplikacja
java.exe Main
pause

by zakończyć proces serwera wisz w jego oknie STOP


# Okno 2 - start 4 klientów równolegle (windows)

uruchom test.bat

skrypt zawiera równoległe uruchomienie 4 klientów, serwer posiada ograniczenie do 3 równoległych

start .\Client.cmd 11 Koty
start .\Client.cmd 2 Psy
start .\Client.cmd 33 Ptaki
start .\Client.cmd 4 Konie


# Przykładowa odpowiedź serwera:

Serwer uruchomiony...
pause
ID klienta: 11
Klient 11 połączony.
ID klienta: 33
Klient 33 połączony.
ID klienta: 4
Klient 4 połączony.
Przekroczono limit aktywnych klientów, odrzucono połączenie klienta
Klient 11 zażądał: get_Koty
Wysłano obiekty typu koty do klienta 11: [Kot{nazwa='Kot_1', rasa='Siberian', wiek=2}, Kot{nazwa='Kot_2', rasa='Persian', wiek=3}, Kot{nazwa='Kot_3', rasa='Maine Coon', wiek=1}, Kot{nazwa='Kot_4', rasa='British Shorthair', wiek=4}]
Klient 11 rozłączony.
Klient 4 zażądał: get_Konie
Klient 4 żąda nieistniejącego typu objektów, zwracam : psy
Wysłano obiekty typu psy do klienta 4: [Pies{nazwa='Pies_1', rozmiar='Small', wyszkolony=true}, Pies{nazwa='Pies_2', rozmiar='Medium', wyszkolony=false}, Pies{nazwa='Pies_3', rozmiar='Large', wyszkolony=true}, Pies{nazwa='Pies_4', rozmiar='Medium', wyszkolony=true}]
Klient 4 rozłączony.
Klient 33 zażądał: get_Ptaki
Wysłano obiekty typu ptaki do klienta 33: [Ptak{nazwa='Ptak_1', kolor='Red', rozpiętość skrzydeł=0.25}, Ptak{nazwa='Ptak_2', kolor='Green', rozpiętość skrzydeł=0.3}, Ptak{nazwa='Ptak_3', kolor='Blue', rozpiętość skrzydeł=0.2}, Ptak{nazwa='Ptak_4', kolor='Yellow', rozpiętość skrzydeł=0.35}]
Klient 33 rozłączony.
STOP
Serwer zamyka się...
Serwer został zamknięty.

# Przykładowe odpowiedzi klientów:

c:\Users\Tomek\WSB-S3\ProZaaw_S4_ProjektAplikacja\out\production\ProZaaw_S4_ProjektAplikacja>java.exe Main 33 Ptaki
Klient 33: Łączenie z serwerem...
Klient 33: Odpowiedź z serwera: OK
Klient 33: Połączono pomyślnie.
Klient 33: Żądanie get_Ptaki
Klient 33: Otrzymano get_Ptaki: [Ptak{nazwa='Ptak_1', kolor='Red', rozpiętość skrzydeł=0.25}, Ptak{nazwa='Ptak_2', kolor='Green', rozpiętość skrzydeł=0.3}, Ptak{nazwa='Ptak_3', kolor='Blue', rozpiętość skrzydeł=0.2}, Ptak{nazwa='Ptak_4', kolor='Yellow', rozpiętość skrzydeł=0.35}]


c:\Users\Tomek\WSB-S3\ProZaaw_S4_ProjektAplikacja\out\production\ProZaaw_S4_ProjektAplikacja>java.exe Main 4 Konie
Klient 4: Łączenie z serwerem...
Klient 4: Odpowiedź z serwera: OK
Klient 4: Połączono pomyślnie.
Klient 4: Żądanie get_Konie
Klient 4: Otrzymano get_Konie: [Pies{nazwa='Pies_1', rozmiar='Small', wyszkolony=true}, Pies{nazwa='Pies_2', rozmiar='Medium', wyszkolony=false}, Pies{nazwa='Pies_3', rozmiar='Large', wyszkolony=true}, Pies{nazwa='Pies_4', rozmiar='Medium', wyszkolony=true}]


c:\Users\Tomek\WSB-S3\ProZaaw_S4_ProjektAplikacja\out\production\ProZaaw_S4_ProjektAplikacja>java.exe Main 2 Psy
Klient 2: Łączenie z serwerem...
Klient 2: Połączenie zostało przerwane. Serwer może być wyłączony.
