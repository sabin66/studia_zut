#include <iostream>
#include <stdexcept>
#include <ctime>
#include <cmath>
#include <sstream>

using namespace std;

template <typename Obj>
class DynamicArray {
    void resize(int new_capacity) {
        Obj* new_array = new Obj[new_capacity];
        for (int i = 0; i < size; i++) {
            new_array[i] = array[i];
        }
        delete[] array;
        array = new_array;
        capacity = new_capacity;
    }

public:
    DynamicArray() : size(0), capacity(1) {
        array = new Obj[capacity];
    }

    ~DynamicArray() {
        delete[] array;
    }

    void add(const Obj& element) {
        if (size == capacity) {
            resize(capacity * 2);
        }
        array[size++] = element;
    }

    Obj get(int index) const {
        if (index < 0 || index >= size) throw out_of_range("Indeks poza zasiegiem!");
        return array[index];
    }

    void set(int index, const Obj& element) {
        if (index < 0 || index >= size) throw out_of_range("Indeks poza zasiegiem!");
        array[index] = element;
    }

    void clear() {
        delete[] array;
        array = new Obj[capacity];
        size = 0;
    }

    void bubbleSort() {
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    Obj tmp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = tmp;
                }
            }
        }
    }

    string toString(int num_elements = 10) const {
        stringstream stream;
        stream << "Rozmiar: " << size << ", Pojemnosc: " << capacity << "\n";
        stream << "Elementy: ";
        for (int i = 0; i < min(num_elements, size); i++) { // zeby nie miec dostepu do elementow poza zakresem tablicy, jesli jest ich mniej niz num_elements elementow
            stream << array[i] << " ";
        }
        return stream.str();
    }

    int getSize() const { 
        return size; 
    }

    int getCapacity() const {
        return capacity; 
    }

private:
    Obj* array;
    int size;
    int capacity;
};

int main() {
    DynamicArray<int> array;
    const int order = 7;
    const int n = pow(10, order);
    double max_time_per_element = 0.0;
    double total_time = 0.0;
    
    // TEST 1
    //TAK JAK W INSTRUKCJI
    clock_t t1 = clock();
    for (int i = 0; i < n; i++) {
        int element = rand() % 10000; // generowanie losowych danych
        clock_t t1_element = clock();

        array.add(element);

        clock_t t2_element = clock();
        double time_per_element = double(t2_element - t1_element) / CLOCKS_PER_SEC;
        if (time_per_element > max_time_per_element) {
            max_time_per_element = time_per_element;
            cout << "Nowy najgorszy czas; indeks  " << i << ": " << max_time_per_element << " sekund\n";
        }
    }
    clock_t t2 = clock();

    total_time = double(t2 - t1) / CLOCKS_PER_SEC;
    double amortized_time = total_time / n;

    cout << "Czas calkowity : " << total_time << " sekund\n";
    cout << "Czas zamortyzowany (czas na dodanie) : " << amortized_time << " sekund\n"; //   Koszt zamortyzowany jest œrednim kosztem operacji w czasie, 
    //w którym rozpatruje siê wszystkie wykonane operacje, a nie tylko te najbardziej kosztowne. 
    //To oznacza, ¿e koszt operacji jest "roz³o¿ony" na wiele dzia³añ.
    cout << array.toString(10) << endl;

    
    //TEST 2 
    //CZAS ZAMORTYZOWANY PO KAZDYM DODANIU ELEMENTU
    /*
    for (int i = 0; i < n; i++) {
        int element = rand() % 10000; // Generowanie losowych danych
        clock_t t1_element = clock();

        array.add(element);

        clock_t t2_element = clock();
        double time_per_element = double(t2_element - t1_element) / CLOCKS_PER_SEC;
        total_time += time_per_element; // Dodanie do calkowitego czasu

        if (time_per_element > max_time_per_element) {
            max_time_per_element = time_per_element;
            cout << "Nowy najgorszy czas; indeks " << i << ": " << max_time_per_element << " sekund\n";
        }

        // Obliczanie czasu amortyzowanego po ka¿dej sekwencji
        double amortized_time = total_time / (i + 1);
        cout << "Czas zamortyzowany po dodaniu " << (i + 1) << " elementow: " << amortized_time << " sekund\n";
    }

    // Podsumowanie
    cout << "Czas calkowity: " << total_time << " sekund\n";
    cout << array.toString(10) << endl;
    */
    
    //TEST 3
    // PO POJAWIENIU SIE NOWEGO NAJGORSZEGO CZASU
    /*
    for (int i = 0; i < n; i++) {
        int element = rand() % 10000; // Generowanie losowych danych
        clock_t t1 = clock();

        array.add(element);

        clock_t t2 = clock();
        double time_per_element = double(t2 - t1) / CLOCKS_PER_SEC;
        total_time += time_per_element; // Dodanie do ca³kowitego czasu

        if (time_per_element > max_time_per_element) {
            max_time_per_element = time_per_element;
            cout << "Nowy najgorszy czas; indeks " << i << ": " << max_time_per_element << " sekund\n";

            // Obliczanie i wyœwietlanie czasu zamortyzowanego
            double amortized_time = total_time / (i + 1);
            cout << "Czas zamortyzowany po dodaniu " << (i + 1) << " elementow: " << amortized_time << " sekund\n";
        }
    }

    // Podsumowanie
    cout << "Czas calkowity: " << total_time << " sekund\n";
    cout << array.toString(10) << endl;

    */
    
    return 0;
}
