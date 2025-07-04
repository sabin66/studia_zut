#include <iostream>
#include <cstring>
#include <cmath>
#include <ctime>
#include <functional>
using namespace std;

class DynamicArray {
private:
    int* data;
    size_t capacity;
    size_t currentSize;

public:
    DynamicArray() : data(nullptr), capacity(1), currentSize(0) {
     data = new int[capacity];  
    }

    DynamicArray(size_t initialCapacity)
        : capacity(initialCapacity > 0 ? initialCapacity : 1), currentSize(0) {
            data = new int[capacity];
    }

    ~DynamicArray() {
        delete[] data;
    }

    void push_back(int value) {
        if (!data) {
            cerr << "Data is null!" << endl;
            return;
        }

        if (currentSize == capacity) {
            size_t newCapacity = capacity * 2;
            int* newData = nullptr;
            try {
                newData = new int[newCapacity];
            }
            catch (std::bad_alloc&) {
                cerr << "Memory allocation failed!" << endl;
                return;
            }

            for (size_t i = 0; i < currentSize; ++i) {
                newData[i] = data[i];
            }

            delete[] data;
            data = newData;
            capacity = newCapacity;
        }

        data[currentSize++] = value;
    }


    size_t size() const {
        return currentSize;
    }

    int& operator[](size_t index) {
        if (index >= currentSize) {
            throw out_of_range("Index out of range");
        }
        return data[index];
    }

    const int& operator[](size_t index) const {
        if (index >= currentSize) {
            throw out_of_range("Index out of range");
        }
        return data[index];
    }

    void bubbleSort(int* arr, size_t n) {
        if (n < 2) return;

        bool swapped;
        for (size_t i = 0; i < n - 1; ++i) {
            swapped = false;
            for (size_t j = 0; j < n - i - 1; ++j) {
                if (arr[j] > arr[j + 1]) {
                    std::swap(arr[j], arr[j + 1]);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
                cout << "BROKE\n" << std::endl;
            }           
        }
    }

};


void counting_sort(int* array, size_t n, int m) {
    int* count = new int[m]();
    for (size_t i = 0; i < n; ++i) {
        count[array[i]]++;
    }

    size_t index = 0;
    for (int i = 0; i < m; ++i) {
        while (count[i] > 0) {
            array[index++] = i;
            count[i]--;
        }
    }

    delete[] count;
}

void bucket_sort(int* array, size_t n, int m) {
    if (n == 0 || m <= 0) {
        cerr << "Error: Invalid array size or range value!" << endl;
        return;
    }

    const size_t bucketCount = n;  // Liczba kube³ków równa liczbie elementów
    DynamicArray* buckets = new DynamicArray[bucketCount];

    // Rozdzielanie elementów na kube³ki
    for (size_t i = 0; i < n; ++i) {
        int bucketIndex = static_cast<int>((static_cast<double>(array[i]) / m) * bucketCount);
        bucketIndex = min(bucketIndex, static_cast<int>(bucketCount - 1));
        buckets[bucketIndex].push_back(array[i]);
    }

    // Sortowanie kube³ków
    for (size_t i = 0; i < bucketCount; ++i) {
        if (buckets[i].size() > 1) {
            // Tworzymy tymczasow¹ tablicê na dane z kube³ka
            int* tempArray = new int[buckets[i].size()];

            for (size_t j = 0; j < buckets[i].size(); ++j) {
                tempArray[j] = buckets[i][j];  // Kopiowanie elementów do tymczasowej tablicy
            }

            // Wywo³anie funkcji bubbleSort
            buckets[i].bubbleSort(tempArray, buckets[i].size());

            // Kopiujemy dane z powrotem do kube³ka
            for (size_t j = 0; j < buckets[i].size(); ++j) {
                buckets[i][j] = tempArray[j];
            }

            delete[] tempArray;  // Zwolnienie pamiêci po u¿yciu
        }
    }

    // £¹czenie kube³ków do tablicy wynikowej
    size_t index = 0;
    for (size_t i = 0; i < bucketCount; ++i) {
        for (size_t j = 0; j < buckets[i].size(); ++j) {
            array[index++] = buckets[i][j];
        }
    }

    delete[] buckets;
}


// Binary Heap Implementation
class BinaryHeap {
private:
    int* heap;
    size_t size;
    size_t capacity;

    void heapify_down(size_t idx) {
        size_t largest = idx;
        size_t left = 2 * idx + 1;
        size_t right = 2 * idx + 2;

        if (left < size && heap[left] > heap[largest]) largest = left;
        if (right < size && heap[right] > heap[largest]) largest = right;

        if (largest != idx) {
            std::swap(heap[idx], heap[largest]);
            heapify_down(largest);
        }
    }

    void heapify_up(size_t idx) {
        while (idx > 0) {
            size_t parent = (idx - 1) / 2;
            if (heap[idx] <= heap[parent]) break;
            std::swap(heap[idx], heap[parent]);
            idx = parent;
        }
    }

public:
    BinaryHeap(int* array, size_t n)
        : heap(new int[n]), size(n), capacity(n) {
        // Copy the array to avoid modifying the original array
        for (size_t i = 0; i < n; ++i) {
            heap[i] = array[i];
        }
        for (int i = size / 2 - 1; i >= 0; --i) {
            heapify_down(i);
        }
    }

    void sort() {
        while (size > 1) {
            std::swap(heap[0], heap[size - 1]);
            --size;
            heapify_down(0);
        }
    }

    ~BinaryHeap() {
        delete[] heap;
    }
};


// Main Function
int main() {
    srand(static_cast<unsigned>(time(0)));

    const int MAX_ORDER = 6;
    const int m = static_cast<int>(pow(10, 7));

    for (int o = 1; o <= MAX_ORDER; ++o) {
        const int n = static_cast<int>(pow(10, o));

        int* array1 = new int[n];
        for (int i = 0; i < n; ++i) {
            array1[i] = rand() % m;
        }

        int* array2 = new int[n];
        int* array3 = new int[n];
        memcpy(array2, array1, n * sizeof(int));
        memcpy(array3, array1, n * sizeof(int));

        clock_t t1, t2;

        // Counting Sort
        t1 = clock();
        counting_sort(array1, n, m);
        t2 = clock();
        cout << "Counting Sort: " << (t2 - t1) / static_cast<double>(CLOCKS_PER_SEC) << "s\n";

        // Heap Sort
        t1 = clock();
        BinaryHeap heap(array2, n);
        heap.sort();
        t2 = clock();
        cout << "Heap Sort: " << (t2 - t1) / static_cast<double>(CLOCKS_PER_SEC) << "s\n";

        //Bucket Sort
        t1 = clock();
        bucket_sort(array3, n, m);
        t2 = clock();
        cout << "Bucket Sort: " << (t2 - t1) / static_cast<double>(CLOCKS_PER_SEC) << "s\n";

        delete[] array1;
        delete[] array2;
        delete[] array3;
        cout << "-----------------------TEST FOR 10 TO THE POWER OF " << o << " ENDED-----------------------\n";
        cout << "-----------------------STARTING TEST FOR 10 TO THE POWER OF " << o + 1 << "----------------------\n";
    }

    return 0;
}
