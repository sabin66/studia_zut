#include <iostream>
#include <ctime>
#include <string>
using namespace std;
struct test
{
    int intField;
    char charField;
};
test* generate_random_object() {
    test* obj = new test;
    obj->intField = rand() % 10001;
    obj->charField = 'a' + rand() % 26;
    return obj;
}   
bool test_comparer(const test* a, const test* b) {
    return a->intField == b->intField && a->charField == b->charField;
}
// Node
template <class Elem>
class Node {
public:
    Elem value;
    Node* prev;
    Node* next;
    Node(Elem value) : value{ value } {}
};

// DoublyLinkedList

template <class Elem>
class DLList {
public:
    DLList() = default;
    Node<Elem>* get(int idx)const; //const nie pozwala modyfikowac zmiennych definiowanych w obrebie klasy
    void ins_head(const Elem& element); //const w nawiasie -> argumenty nie mog¹ byc modyfikowane w obrebie tej funkcji
    void ins_tail(const Elem& element);
    void ins(int idx, const Elem& element);
    int search(const Elem& value)const;
    void rm_head();
    void rm_tail();
    void rm(int idx);
    int size() const;
    void show()const;
    void show_backwards()const;
    void set(int idx, const Elem& value);
    bool search_and_remove(const Elem& value);
    void ins_sorted(const Elem& value);
    void clear();
    string to_str()const;
private:
    Node<Elem>* head = nullptr;
    Node<Elem>* tail = nullptr;
    int quantity = 0;
};
template <class Elem>
void DLList<Elem>::rm_tail() {
    if (quantity == 0) {
        return;
    }
    if (quantity == 1) {
        rm_head(); //wskaznik na ostatni element TODO
        return;
    }
    Node<Elem>* node = tail; // pomocniczy wskaznik node ustawiamy na element ostatni
    tail = tail->prev; // tail ustawiamy na element przedostatni 
    tail->next = nullptr;
    delete node;
    quantity--;
}
template <class Elem>
void DLList<Elem>::rm_head() {
    if(quantity == 0) {
        return;
    }
    Node<Elem>* node = head; // pomocniczy wskaznik pokazuje na dotychczasowy head
    head = head->next; // dotychczasowy  head ustawiamy na nastepnym elemencie
    delete node;
    if (head != nullptr) {
        head->prev = nullptr;
    }
    quantity--;
}
template <class Elem>
void DLList<Elem>::rm(int idx) {
    if (quantity == 0) {
        return;
    }
    if (idx < 0 || idx >= quantity) {
        return;
    }
    if (idx == 0) {
        rm_head();
        return;
    }
    else if (idx == quantity - 1) {
        rm_tail();
        return;
    }
    Node<Elem>* prev_node = head;
    for (int i = 0; i < idx - 1; ++i)
    {
        prev_node = prev_node->next;
    }
    Node<Elem>* node = prev_node->next; // element ktory chcemy usunac
    Node<Elem>* next_node = node->next; // element ktory znajduje sie za tym ktory chcemy usunac
    prev_node->next = next_node;
    next_node->prev = prev_node;
    delete node;
    quantity--;
}
template <class Elem>
void DLList<Elem>::ins_head(const Elem& value) {
    Node<Elem>* node = new Node<Elem>{ value }; // nowy element o zawartosci takiej jak argument funkcji
    node->next = head; // nowy element jako nastepny pokazuje dotychczas pierwszy element
    if (head != nullptr) {
        head->prev = node;
    }
    head = node;
    if (quantity == 0) {
        tail = head;
    }
    quantity++;
}
template <class Elem>
void DLList<Elem>::ins_tail(const Elem& value) {
    if (quantity == 0) {
        ins_head(value);
        return;
    }
    Node<Elem>* node = new Node<Elem>{ value };
    tail->next = node; // wskaznik ostatniego elementu ustawiamy na nowym elemencie
    node->prev = tail; // wskaznik pokazujacy na element przed nowym elementem ustawiamy na element ostatni
    tail = node;
    quantity++;
}
template <class Elem>
void DLList<Elem>::ins(int idx, const Elem& value) {
    if(idx<0 || idx > quantity) {
        return;
    }
    if (idx == 0) {
        ins_head(value);
        return;
    }
    if (idx == quantity) {
        ins_tail(value);
        return;
    }
    Node<Elem>* prev_node = head;
    for (int i = 0; i < idx - 1; ++i) {
        prev_node = prev_node->next; 
    }
    Node<Elem>* next_node = prev_node->next; // ustawiamy sie na elemencie ktory bedzie za naszym wstawionym
    Node<Elem>* node = new Node<Elem>{ value };
    node->next = next_node; // nasz nowy element pokazuje na element przed ktorym go wstawiamy
    node->prev = prev_node;// pokazuje na element za ktorym wstawiamy
    prev_node->next = node;
    next_node->prev = node;
    quantity++;
}
template <class Elem>
int DLList<Elem>::size()const {
    return quantity;
}
template <class Elem>
int DLList<Elem>::search(const Elem& value) const {
    if (quantity == 0) {
        return -1; 
    }
    int idx = 0;
    Node<Elem>* tmpnode = head;

    while (tmpnode != nullptr) { 
        if (tmpnode->value == value) {
            return idx; 
        }
        idx++;
        tmpnode = tmpnode->next;
    }

    return -1; 
}
template <class Elem>
Node<Elem>* DLList<Elem>::get(int idx)const {
    if (idx < 0 || idx > quantity) {
        return nullptr;
    }
    if (idx == 0) {
        return head;
    }
    if (idx == quantity - 1) {
        return tail;
    }
    Node<Elem>* tmpnode = head;
    for (int i = 0; i < idx; ++i) {
        tmpnode = tmpnode->next;
    }
    return tmpnode;
}
template <class Elem>
void DLList<Elem>::show()const {
    Node<Elem>* tmpnode = head;
    while (tmpnode) {
        cout << tmpnode->value << "\t";
        tmpnode = tmpnode->next;
    }
    cout << endl;
}
template <class Elem>
void DLList<Elem>::show_backwards()const {
    Node<Elem>* tmpnode = tail;
    while (tmpnode) {
        cout << tmpnode->value << "\t";
        tmpnode = tmpnode->prev;
    }
    cout << endl;
}
template <class Elem>
void DLList<Elem>::set(int idx, const Elem& value) {
    Node<Elem>* node = get(idx); 
    if (node != nullptr) {
        node->value = value; 
    }
}
template <class Elem>
bool DLList<Elem>::search_and_remove(const Elem& value) {
    int idx = search(value);
    if (idx != -1) {
        rm(idx);
        return true;
    }
    return false;
}
template <class Elem>
void DLList<Elem>::ins_sorted(const Elem& value) {
    if (quantity == 0 || head->value >= value) {
        ins_head(value); 
        return;
    }
    if (tail->value < value) {
        ins_tail(value);
        return;
    }
    Node<Elem>* current = head;
    int idx = 0;
    while (current->value < value) {
        current = current->next;
        idx++;
    }
    ins(idx, value);
}
template <class Elem>
void DLList<Elem>::clear() {
    while (quantity > 0) {
        rm_head(); 
    }
}
// to_str
/*template <class Elem>
string DLList<Elem>::to_str() const {
    string result = "List size: " + to_string(quantity) + "\nElements: ";
    Node<Elem>* current = head;
    while (current != nullptr) {
        result += "{intField: " + to_string(current->value->intField) +
            ", charField: " + current->value->charField + "} ";
        current = current->next;
    }
    return result;
}*/ 


int main()
{
    //DLList<int> list{};
    // TEST 1
    /*list.ins_head(10);
    list.ins_tail(30);
    list.ins_tail(50);
    cout << "Po dodaniu na pocz¹tek i koniec:\n";
    list.show();

    list.ins(1, 20);
    cout << "Po wstawieniu 20 na pozycjê 1:\n";
    list.show(); 

    int idx = list.search(30);
    cout << "Wyszukiwanie elementu 30: " << idx << "\n"; 

    list.rm_head();
    list.rm_tail();
    cout << "Po usuniêciu pierwszego i ostatniego elementu:\n";
    list.show(); 

    list.set(1, 40);
    cout << "Po podmianie elementu na indeksie 1 na 40:\n";
    list.show(); 


    bool removed = list.search_and_remove(20);
    cout << "Wyszukiwanie i usuniêcie 20: " << (removed ? "powodzenie" : "niepowodzenie") << "\n";
    list.show(); 

    list.ins_sorted(25);
    list.ins_sorted(15);
    list.ins_sorted(50);
    cout << "Po dodaniu elementów z wymuszeniem porz¹dku:\n";
    list.show(); 

    list.clear();
    cout << "Po czyszczeniu listy:\n";
    list.show(); 

    return 0;*/

    //TEST 2
    const int MAX = 6; // maksymalny rz¹d wielkoœci rozmiaru dodawanych danych
    srand(static_cast<unsigned>(time(0))); // inicjalizacja generatora liczb losowych

    DLList<test*>* lista = new DLList<test*>(); // stworzenie listy; pierwszy * to typ listy, która przechowuje wskaŸniki na obiekty typu
    // drugi * o wskaŸnik na taki obiekt listy, który sam jest dynamicznie alokowany w pamiêci.

    for (int o = 1; o <= MAX; o++) {  // pêtla po kolejnych rzêdach wielkoœci
        const int n = pow(10, o); // rozmiar danych
        clock_t t1 = clock();  // pomiar czasu rozpoczêcia dodawania danych

        // dodawanie do listy
        for (int i = 0; i < n; i++) {
            test* testObject = generate_random_object();
            lista->ins_tail(testObject);
        }

        clock_t t2 = clock();  // pomiar czasu zakoñczenia dodawania danych
        double time_ins = double(t2 - t1) / CLOCKS_PER_SEC;
        cout << "Dodano " << n << " elementow, czas: " << time_ins << " sekund\n";

        // wyszukiwanie i usuwanie z listy
        const int m = pow(10, 4);  // liczba prób wyszukiwania
        t1 = clock();  // pomiar czasu rozpoczêcia wyszukiwania i usuwania

        for (int i = 0; i < m; i++) {
            test* testObject = generate_random_object();  // losowy wzorzec do wyszukiwania
            lista->search_and_remove(testObject);  // wyszukiwanie i usuwanie
            delete testObject;  // usuwamy chwilowy obiekt
        }
        t2 = clock();  // pomiar czasu zakoñczenia wyszukiwania i usuwania
        double time_search_remove = double(t2 - t1) / CLOCKS_PER_SEC * 1000;
        cout << "Przeprowadzono " << m << " wyszukiwan i usuniec, czas: " << time_search_remove << " milisekund\n";
        lista->clear();
    }

    delete lista;  
    return 0;
    
}
