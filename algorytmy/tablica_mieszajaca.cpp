#include <iostream>
#include <string>
#include <stdexcept>
#include <vector>
#include <cstdlib>
#include <ctime>
#include <cmath>
using namespace std;

struct Data {
    int number;
    char letter;

    Data(int n, char l) : number(n), letter(l) {}

    string to_String() const {
        return "(" + to_string(number) + ", " + letter + ")";
    }
};

bool compare(const Data& a, const Data& b) {
    return a.number == b.number && a.letter == b.letter;
}

bool compare_Order(const Data& a, const Data& b) {
    return a.number < b.number;
}

template <typename T>
struct Node {
    T data;
    Node<T>* prev;
    Node<T>* next;

    Node(const T& d) : data(d), prev(nullptr), next(nullptr) {}
};

template <typename T>
class Linked_List {
public:
    // Konstruktor
    Linked_List() : head(nullptr), tail(nullptr), size(0) {}

    // Destruktor
    ~Linked_List() {
        clear();
    }

    // (a) Dodanie nowego elementu na koñcu listy
    void add(const T& data) {
        Node<T>* newNode = new Node<T>(data);
        if (tail) {
            tail->next = newNode;
            newNode->prev = tail;
            tail = newNode;
        }
        else {
            head = tail = newNode;
        }
        size++;
    }

    // (b) Dodanie nowego elementu na pocz¹tku listy
    void add_First(const T& data) {
        Node<T>* newNode = new Node<T>(data);
        if (head) {
            head->prev = newNode;
            newNode->next = head;
            head = newNode;
        }
        else {
            head = tail = newNode;
        }
        size++;
    }

    // (c) Usuniêcie ostatniego elementu
    void remove_Last() {
        if (tail) {
            Node<T>* toDelete = tail;
            tail = tail->prev;
            if (tail) tail->next = nullptr;
            else head = nullptr;
            delete toDelete;
            size--;
        }
        else {
            throw runtime_error("Lista jest pusta");
        }
    }

    // (d) Usuniêcie pierwszego elementu
    void remove_First() {
        if (head) {
            Node<T>* toDelete = head;
            head = head->next;
            if (head) head->prev = nullptr;
            else tail = nullptr;
            delete toDelete;
            size--;
        }
        else {
            throw runtime_error("Lista jest pusta");
        }
    }

    // (e) Zwrócenie danych i-tego elementu listy
    T get(size_t index) const {
        if (index >= size) throw out_of_range("Indeks poza zakresem");
        Node<T>* current = head;
        for (size_t i = 0; i < index; ++i) {
            current = current->next;
        }
        return current->data;
    }

    // (f) Ustawienie (podmiana) danych i-tego elementu listy
    void set(size_t index, const T& data) {
        if (index >= size) throw out_of_range("Indeks poza zakresem");
        Node<T>* current = head;
        for (size_t i = 0; i < index; ++i) {
            current = current->next;
        }
        current->data = data;
    }

    // (g) Wyszukanie elementu
    Node<T>* find(const T& data, bool(*cmp)(const T&, const T&)) const {
        Node<T>* current = head;
        while (current) {
            if (cmp(current->data, data)) return current;
            current = current->next;
        }
        return nullptr;
    }

    // (h) Wyszukanie i usuniêcie elementu
    bool seek_and_destroy(const T& data, bool(*cmp)(const T&, const T&)) {
        Node<T>* current = find(data, cmp);
        if (!current) return false;

        if (current->prev) current->prev->next = current->next;
        else head = current->next;

        if (current->next) current->next->prev = current->prev;
        else tail = current->prev;

        delete current;
        size--;
        return true;
    }

    // (i) Dodanie nowego elementu z wymuszeniem porz¹dku
    void add_Ordered(const T& data, bool(*cmp)(const T&, const T&)) {
        Node<T>* newNode = new Node<T>(data);
        if (!head || cmp(data, head->data)) {
            add_First(data);
        }
        else if (cmp(tail->data, data)) {
            add(data);
        }
        else {
            Node<T>* current = head;
            while (current->next && cmp(current->next->data, data)) {
                current = current->next;
            }
            newNode->next = current->next;
            newNode->prev = current;
            if (current->next) current->next->prev = newNode;
            current->next = newNode;
            size++;
        }
    }

    // (j) Czyszczenie listy
    void clear() {
        while (size > 0) {
            remove_Last();
        }
    }

    // (k) Zwrócenie napisowej reprezentacji listy
    string to_String() const {
        string result = "Lista: ";
        Node<T>* current = head;
        while (current) {
            result += current->data.to_String() + " ";
            current = current->next;
        }
        result += "\nRozmiar listy: " + to_string(size);
        return result;
    }

    // Zwrócenie rozmiaru listy
    size_t get_Size() const {
        return size;
    }
    private:
        Node<T>* head;
        Node<T>* tail;
        size_t size;

};

template <typename V>
class HashTable {
public:
    // Konstruktor
    HashTable(size_t initialCapacity = 16)
        : capacity(initialCapacity), currentSize(0) {
        table = new Linked_List<Entry>[capacity];
    }

    // Destruktor
    ~HashTable() {
        clear();
        delete[] table;
    }

    // Dodanie nowego elementu
    void put(const string& key, const V& value) {
        if ((double)currentSize / capacity >= loadFactor) {
            rehash();
        }

        int index = hash(key);
        Node<Entry>* found = table[index].find(Entry(key, value), [](const Entry& a, const Entry& b) {
            return a.key == b.key;
            });

        if (found) {
            found->data.value = value; // Aktualizacja wartoœci, jeœli klucz ju¿ istnieje
        }
        else {
            table[index].add(Entry(key, value));
            ++currentSize;
        }
    }

    // Wyszukiwanie elementu
    V* get(const string& key) {
        int index = hash(key);
        Node<Entry>* found = table[index].find(Entry(key, V()), [](const Entry& a, const Entry& b) {
            return a.key == b.key;
            });

        return found ? &found->data.value : nullptr;
    }

    // Usuniêcie elementu
    bool remove(const string& key) {
        int index = hash(key);
        bool result = table[index].seek_and_destroy(Entry(key, V()), [](const Entry& a, const Entry& b) {
            return a.key == b.key;
            });

        if (result) {
            --currentSize;
        }
        return result;
    }

    // Czyszczenie tablicy
    void clear() {
        for (size_t i = 0; i < capacity; ++i) {
            table[i].clear();
        }
        currentSize = 0;
    }

    // Zwracanie statystyk
    void printStats() const {
        size_t nonEmptyBuckets = 0; // Liczba niepustych komórek
        size_t totalListSize = 0;  // £¹czna d³ugoœæ wszystkich list
        size_t maxListSize = 0;    // Maksymalna d³ugoœæ listy
        size_t minListSize = capacity; // Minimalna d³ugoœæ niepustej listy (inicjalizujemy na capacity)

        for (size_t i = 0; i < capacity; ++i) {
            size_t listSize = table[i].get_Size();
            if (listSize > 0) {
                nonEmptyBuckets++;
                totalListSize += listSize;
                maxListSize = std::max(maxListSize, listSize);
                minListSize = std::min(minListSize, listSize);
            }
        }

        cout << "Capacity: " << capacity << endl;
        cout << "Number of elements: " << currentSize << endl;
        cout << "Load factor: " << (double)currentSize / capacity << endl;
        cout << "Average list size: " << (double)totalListSize / nonEmptyBuckets << endl;
        cout << "Max list size: " << maxListSize << endl;
        cout << "Min list size: " << minListSize << endl << endl;
    }
private:
    struct Entry {
        string key;
        V value;

        Entry(const string& k, const V& v) : key(k), value(v) {}

        string to_String() const {
            return "{" + key + ": " + to_string(value) + "}";
        }
    };

    Linked_List<Entry>* table; // Tablica list powi¹zanych
    size_t capacity;
    size_t currentSize;
    const double loadFactor = 0.75;

    // Funkcja mieszaj¹ca
    int hash(const string& key) const {
        unsigned long hash = 0;
        const int base = 31;
        for (char ch : key) {
            hash = (hash * base + ch) % capacity;
        }
        return hash % capacity;
    }

    // Funkcja do rozszerzania i przemieszania tablicy
    void rehash() {
        size_t newCapacity = capacity * 2;
        Linked_List<Entry>* newTable = new Linked_List<Entry>[newCapacity];

        for (size_t i = 0; i < capacity; ++i) {
            while (table[i].get_Size() > 0) {
                Entry entry = table[i].get(0);
                table[i].remove_First();

                int newIndex = hash(entry.key) % newCapacity;
                newTable[newIndex].add(entry);
            }
        }

        delete[] table;
        table = newTable;
        capacity = newCapacity;
    }
};

string random_key(int length) {
    const char charset[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    string result;
    for (int i = 0; i < length; ++i) {
        result += charset[rand() % (sizeof(charset) - 1)];
    }
    return result;
}

int main() {
    const int MAX_ORDER = 6;
    const int RANDOM_KEY_LENGTH = 6;
    const int SEARCH_COUNT = pow(10, 4);

    srand(static_cast<unsigned int>(time(nullptr))); // Inicjalizacja generatora losowego
    HashTable<int> ht; // Tworzenie tablicy mieszaj¹cej
    vector<string> keys; // Lista kluczy dodanych do tablicy

    for (int o = 1; o <= MAX_ORDER; ++o) {
        const int n = pow(10, o); // Liczba elementów do dodania
        keys.clear(); // Wyczyœæ listê kluczy przed ka¿dym eksperymentem

        // Dodawanie elementów do tablicy mieszaj¹cej
        cout << "Test for n = " << n << " elements:\n";
        clock_t t1 = clock();
        for (int i = 0; i < n; ++i) {
            string key = random_key(RANDOM_KEY_LENGTH);
            ht.put(key, i);
            keys.push_back(key); // Zapisz klucz do listy
        }
        clock_t t2 = clock();
        double insertionTime = static_cast<double>(t2 - t1) / CLOCKS_PER_SEC;

        cout << "Addition time: " << insertionTime << " seconds\n";

        // Wyszukiwanie w tablicy mieszaj¹cej
        int hits = 0;
        t1 = clock();
        for (int i = 0; i < SEARCH_COUNT; ++i) {
            const string& key = keys[rand() % keys.size()];
            int* value = ht.get(key);
            if (value != nullptr) {
                ++hits;
            }
        }
        t2 = clock();
        double searchTime = static_cast<double>(t2 - t1) / CLOCKS_PER_SEC;

        cout << "Search time: " << searchTime << " seconds\n";
        cout << "Number of hits: " << hits << "\n";

        ht.printStats();
        if (n == 10 || n == 100) {
            ht.printStats();
        }

        ht.clear();
        cout << "Tablica wyczyszczona.\n";
        cout << "#######################################################################\n";
    }
}
