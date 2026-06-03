import random

def generuj_macierz(nazwa_pliku, wiersze=1000, kolumny=1000):
    with open(nazwa_pliku, "w") as plik:
        plik.write(f"{wiersze} {kolumny}\n")
        
        for _ in range(wiersze):
            wiersz = [str(random.randint(1, 10)) for _ in range(kolumny)]
            plik.write(" ".join(wiersz) + "\n")

generuj_macierz("macierz_1000x1000.txt")