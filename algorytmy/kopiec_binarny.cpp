#include <iostream>
#include <ctime>
#include <cstdlib>
#include <cmath>


using namespace std;
template <typename T>
class DynamicArray {
public:
    DynamicArray() : size_(0), capacity_(1), data_(new T[1]) {}

    ~DynamicArray() {
        delete[] data_;
    }

    void push_back(const T& element) {
        if (size_ == capacity_) {
            resize(capacity_ * 2);
        }
        data_[size_++] = element;
    }

    void pop_back() {
        if (size_ > 0) {
            --size_;
        }
    }

    T& operator[](int index) {
        if (index < 0 || index >= size_) {
            cout << ("Index out of range");
        }
        return data_[index];
    }

    const T& operator[](int index) const {
        if (index < 0 || index >= size_) {
            cout << ("Index out of range");
        }
        return data_[index];
    }

    int size() const {
        return size_;
    }

    bool empty() const {
        return size_ == 0;
    }

    void clear() {
        delete[] data_;
        size_ = 0;
        capacity_ = 1;
        data_ = new T[1];
    }

private:
    T* data_;
    int size_;
    int capacity_;

    void resize(int new_capacity) {
        T* new_data = new T[new_capacity];
        for (int i = 0; i < size_; ++i) {
            new_data[i] = data_[i];
        }
        delete[] data_;
        data_ = new_data;
        capacity_ = new_capacity;
    }
};

using namespace std;

template <typename T>
class BinaryHeap {
public:
    void add(const T& element, bool (*comparator)(const T&, const T&)) {
        data.push_back(element);
        heapifyUp(data.size() - 1, comparator);
    }

    T poll(bool (*comparator)(const T&, const T&)) {
        if (data.empty()) {
            cout << ("Heap is empty");
        }
        T root = data[0];
        data[0] = data[data.size() - 1];
        data.pop_back();
        if (!data.empty()) {
            heapifyDown(0, comparator);
        }
        return root;
    }

    void clear() {
        data.clear();
    }

    void printHeap() const {
        for (int i = 0; i < data.size(); ++i) {
            cout << data[i] << " ";
        }
        cout << endl;
    }

    bool isEmpty() const {
        return data.empty();
    }

private:
    DynamicArray<T> data;

    void heapifyUp(int index, bool (*comparator)(const T&, const T&)) { // dba o to, aby nowy element wstawiony do kopca znalaz� si� na odpowiednim miejscu, przesuwaj�c si� w g�r�
        if (index == 0) {
            return; // Je�li indeks to 0, osi�gn�li�my korze�, wi�c nie ma ju� wy�ej, gdzie przesun�� element
        }

        int parentIndex = (index - 1) / 2; // Obliczamy indeks rodzica
        // Sprawdzamy, czy element w obecnym indeksie jest wi�kszy od rodzica
        if (comparator(data[index], data[parentIndex])) {
            swap(data[index], data[parentIndex]); // Zamiana miejscami
            heapifyUp(parentIndex, comparator); // Rekurencyjne wywo�anie dla rodzica
        }
    }


    void heapifyDown(int index, bool (*comparator)(const T&, const T&)) {  // naprawia kopiec po usuni�ciu korzenia, przesuwaj�c element w d�.
        int size = data.size();
        int leftChild = 2 * index + 1; // Indeks lewego dziecka
        int rightChild = 2 * index + 2; // Indeks prawego dziecka
        int largest = index; // Za��my, �e bie��cy element jest najwi�kszy

        // Sprawdzamy, czy lewe dziecko istnieje i jest wi�ksze od bie��cego elementu
        if (leftChild < size && comparator(data[leftChild], data[largest])) {
            largest = leftChild;
        }
        // Sprawdzamy, czy prawe dziecko istnieje i jest wi�ksze od bie��cego elementu (lub lewego dziecka)
        if (rightChild < size&& comparator(data[rightChild], data[largest])) {
            largest = rightChild;
        }

        // Je�li kt�ry� z dzieci jest wi�kszy, zamieniamy miejscami
        if (largest != index) {
            swap(data[index], data[largest]);
            heapifyDown(largest, comparator); // Rekurencyjne wywo�anie dla nowego indeksu
        }
    }

};

bool maxHeapComparator(const int& a, const int& b) {
    return a > b;
}

int main() {
    const int MAX_ORDER = 6;
    BinaryHeap<int> bh;
    for (int o = 1; o <= MAX_ORDER; ++o) {
        const int n = pow(10, o);

        // Dodawanie element�w do kopca
        clock_t t1 = clock();
        for (int i = 0; i < n; ++i) {
            int randomValue = rand() % 10000; // Losowa liczba ca�kowita
            bh.add(randomValue, maxHeapComparator);
        }
        clock_t t2 = clock();
        cout << "Added " << n << " elements to heap in "
            << static_cast<double>(t2 - t1) / CLOCKS_PER_SEC << " seconds." << endl;
      //    // Pobieranie element�w z kopca
        t1 = clock();
        while (!bh.isEmpty()) {
            bh.poll(maxHeapComparator);
        }
       t2 = clock();
        cout << "Removed " << n << " elements from heap in "
            << static_cast<double>(t2 - t1) / CLOCKS_PER_SEC << " seconds." << endl;

        // Czyszczenie kopca
        bh.clear();
    }
    /*cout << "Dodawanie elementow do kopca:\n";
    bh.add(23, maxHeapComparator);
    bh.add(20, maxHeapComparator);
    bh.add(22, maxHeapComparator);
    bh.add(15, maxHeapComparator);
    bh.add(6, maxHeapComparator);
    bh.add(1, maxHeapComparator);
    bh.add(17, maxHeapComparator);
    bh.add(5, maxHeapComparator);
    bh.add(13, maxHeapComparator);
    bh.add(1, maxHeapComparator);


    cout << "\n\nKopiec po dodaniu elementow:\n";
    bh.printHeap(); 
    cout << "\n\nDodanie elementu '26':\n";
    bh.add(26, maxHeapComparator);
    cout << "\n\nKopiec po dodaniu elementu '26':\n";
    bh.printHeap();

    cout << "\nPoll - usuwanie elementu maksymalnego:\n";
    int maxElement = bh.poll(maxHeapComparator);
    cout << "Usuniety element: " << maxElement << endl;

    cout << "\nKopiec po usunieciu elementu maksymalnego:\n";
    bh.printHeap();*/

    return 0;
}
