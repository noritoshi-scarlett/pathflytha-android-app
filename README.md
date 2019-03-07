# Pathflytha App

- *w trakcie dalszego rozwoju*
- *dla Androida 5.0 lub wyższego*
- *aplikacja zaprojektowana i zrealizowana w ramach pracy magisterskiej "Wyznaczanie trasy dla obiektów latających"*

*Pathflytha  App* to samodzielna aplikacja mobilna dla systemu *Android*, obecnie znajdująca się w fazie dalszego rozwoju.
Aplikacja posiada bazę przezkód lotniczych oraz rzeźby terenu. Wyszukuje najkrótszą trasę dla lotu w obsarze południowej Polski (województwa opolskie, śląskie, małopolskie i świętokrzyskie).
Aplikacja wymaga dopracowania, jednakże swoją główną funkcjonalność spełnia.

## Screeny z alikacji:

### Strona Główna | Menu Główne

<img width="250" alt="Strona Główna" src="https://i.imgur.com/mWT9eNx.png"> <img width="250" alt="Menu Główne" src="https://i.imgur.com/CKzN8z8.png">

### Przegląd przeszkód | Wybór współrzędnych
Przegląd przeszkód z dołączonej bazy. Są to przeskzody lotnicze o wysokości 100m lub wyżej. Wybór współrzędnych z mapy, dla których wytyczona zostanie trasa.

<img width="250" alt="Przegląd przeszkód" src="https://i.imgur.com/X2klSyp.png"> <img width="250" alt="Wybór współrzędnych" src="https://i.imgur.com/5xWZSo9.png">

### Graf Ścieżek | Mapa z trasą | Wykres wysokości
Prezentacja wyników działania algorytmu wyznaczania trasy. Prezentowana jest ona na trzy sposoby i w dwóch płaszczyznach - rzut z góry oraz przekrój trasy.

<img width="250" alt="Graf ścieżek" src="https://i.imgur.com/sXTet6C.png"> <img width="250" alt="Mapa z trasą" src="https://i.imgur.com/hbhtAlO.png"> <img width="250" alt="Wykres wysokości" src="https://i.imgur.com/oYGpVef.png">


## Funkcje obecnie dostępne w aplikacji
**Aplikacja - funkcje główne:**
- [x] inicjalzacja bazy danych,
- [x] przeglądanie przeszkód lotniczych na mapie oraz na liście,
- [ ] przeglądanie wyliczonych tras *(adapter jest, brakuje zbierania samych danych o trasie)*,
- [x] wyznaczanie punktów stratowych i końcowych (łącznie 4 punkty) na interaktywnej Mapie Google.
- [ ] wbieranie kryteriów lotu (rodzaj statku i licencji),
- [x] wybieranie sposobu wytyczania trasy (kompletna lub omijania przeszkód z zachowaniem lotu zbliżonego do linii prostej).

**Wyliczanie trasy:**
- [x] wyznaczanie połączeń pomiędzy okręgammi wejśćia i wyjścia,
- [ ] sprawdzanie przeszkód w okolicy startu i lądowania *(funkcja istnieje, ale nie jest włączona)*,
- [ ] wymuszenie łagodnych ścieżek na okręgach startu i lądowania *(z jakiegoś powodu czasem tworzą się kąty ostre)*,
- [x] wyszukiwanie przeszkód w obrębie wyznaczonych połączeń, których usytuowanie zagraża lotu,
- [x] wyznaczane stycznych pomiędzy wszystkimi okręgami,
- [x] tworzenie grafu ze stycznych oraz łuków na okręgach,
- [x] wyszukiwanie najkrótszej ściezki w grafie *(Algorytm Dijikstry) - czasem najkrótsza ścieżka nie jest znajdowana*,
- [x] prezentacja całego grafu,
- [ ] prezentacja najkrótzej ściezki na mapie *(funkcja jest zaimplementowana, ale algorytm przekształcania współrzędnych daje złe wyniki)*,
- [ ] prezentacja modelu wysokości *jest wyznaczany, ale wizualnie nie wygląda najlepiej; należy też zweryfikować podawaną odległosć na osi OX*.
