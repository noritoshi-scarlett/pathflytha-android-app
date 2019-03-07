# Pathflytha App

- *w trakcie dalszego rozwoju*
- *dla Androida 5.0 lub wyższego*
- *aplikacja zaprojektowana i zrealizowana w ramach pracy magisterskiej "Wyznaczanie trasy dla obiektów latających"*

*Pathflytha  App* to samodzielna aplikacja mobilna dla systemu *Android*, obecnie znajdująca się w fazie dalszego rozwoju.
Aplikacja posiada bazę przezkód lotniczych oraz rzeźby terenu. Wyszukuje najkrótszą trasę dla lotu w obsarze południowej Polski (województwa opolskie, śląskie, małopolskie i świętokrzyskie).
Aplikacja wymaga dopracowania, jednakże swoją główną funkcjonalność spełnia.

## Screeny z alikacji:
<img width="250" alt="Strona Główna" src="https://i.imgur.com/FhwvYRp.png">
<img width="250" alt="Wyszukiwarka" src="https://i.imgur.com/NDno5GR.png">
<img width="250" alt="Menu Główne" src="https://i.imgur.com/ihTEcJ4.png">
<img width="250" alt="Subskrybowane fora" src="https://i.imgur.com/o4twKi2.png">
<img width="250" alt="Ustawienia postaci" src="https://i.imgur.com/X9zGHN6.png">


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
