#include <iostream>
#include <ctime>
#include <cmath>
#include <cstdlib>
#include <string>
#include <sstream>
using namespace std;

template<typename Elem>
class BSTNode {
public:
    Elem value;
    BSTNode* l;
    BSTNode* r;
    BSTNode* p;
    int idx;

    BSTNode(Elem value, int idx) : value(value), l(nullptr), r(nullptr), p(nullptr), idx(idx) {}
};

template<typename Elem>
class BST {
public:
    BST() : root(nullptr), nodeCount(0) {}

    void add(Elem value, bool (*cmp)(const Elem&, const Elem&) = nullptr) {
        root = addRecursive(root, value, nullptr, nodeCount, cmp);
        nodeCount++; 
    }

    bool find(Elem value, bool (*cmp)(const Elem&, const Elem&) = nullptr) {
        return findRecursive(root, value, cmp) != nullptr;
    }

    void remove(Elem value, bool (*cmp)(const Elem&, const Elem&) = nullptr) {
        root = removeRecursive(root, value, cmp);
    }

    void clear() {
        clearRecursive(root);
        root = nullptr;
        nodeCount = 0;
    }

    int getHeight() {
        return getHeightRecursive(root);
    }

    string toString() {
        stringstream stream;
        stream << "binary search tree:\n";
        stream << "size: " << nodeCount << "\n";
        stream << "height: " << getHeight() << "\n{\n";
        toStringRecursive(root, stream);
        stream << "}\n";
        return stream.str();
    }

    void inOrderTraversal() {
        inOrderRecursive(root);
    }

    void preOrderTraversal() {
        preOrderRecursive(root);
    }

private:
    BSTNode<Elem>* root;
    int nodeCount;

    BSTNode<Elem>* addRecursive(BSTNode<Elem>* node, Elem value, BSTNode<Elem>* parent, int idx, bool (*cmp)(const Elem&, const Elem&)) {
        if (!node) {
            node = new BSTNode<Elem>(value, idx);
            node->p = parent;
            return node;
        }
        if (cmp ? cmp(value, node->value) : value < node->value) {
            node->l = addRecursive(node->l, value, node, idx, cmp);
        }
        else {
            node->r = addRecursive(node->r, value, node, idx, cmp);
        }
        return node;
    }

    BSTNode<Elem>* findRecursive(BSTNode<Elem>* node, Elem value, bool (*cmp)(const Elem&, const Elem&)) {
        if (!node) return nullptr;
        if (node->value == value) return node;  
        return (cmp ? cmp(value, node->value) : value < node->value) ?
            findRecursive(node->l, value, cmp) : findRecursive(node->r, value, cmp);
    }

    BSTNode<Elem>* removeRecursive(BSTNode<Elem>* node, Elem value, bool (*cmp)(const Elem&, const Elem&)) {
        if (!node) return node;

        if (cmp ? cmp(value, node->value) : value < node->value) {
            node->l = removeRecursive(node->l, value, cmp);
        }
        else if (cmp ? cmp(node->value, value) : node->value < value) {
            node->r = removeRecursive(node->r, value, cmp);
        }
        else {
            if (!node->l) {
                BSTNode<Elem>* tmp = node->r;
                delete node;
                return tmp;
            }
            else if (!node->r) {
                BSTNode<Elem>* tmp = node->l;
                delete node;
                return tmp;
            }
            BSTNode<Elem>* temp = minValueNode(node->r);
            node->value = temp->value;
            node->r = removeRecursive(node->r, temp->value, cmp);
        }
        return node;
    }

    BSTNode<Elem>* minValueNode(BSTNode<Elem>* node) {
        BSTNode<Elem>* current = node;
        while (current && current->l)
            current = current->l;
        return current;
    }

    void clearRecursive(BSTNode<Elem>* node) {
        if (!node) return;
        clearRecursive(node->l);
        clearRecursive(node->r);
        delete node;
    }

    int getHeightRecursive(BSTNode<Elem>* node) {
        if (!node) return 0;
        return 1 + max(getHeightRecursive(node->l), getHeightRecursive(node->r));
    }

    void inOrderRecursive(BSTNode<Elem>* node) { // rekurencyjnie odwiedzamy lewe poddrzewo, potem bie¿¹cy wêze³, a na koñcu prawe poddrzewo.
        if (!node) return;
        inOrderRecursive(node->l);
        cout << node->value << " ";
        inOrderRecursive(node->r);
    }

    void preOrderRecursive(BSTNode<Elem>* node) { // odwiedzamy wêze³ bie¿¹cy, a nastêpnie rekurencyjnie przechodzimy do jego lewego poddrzewa, a potem prawego poddrzewa
        if (!node) return;
        cout << node->value << " ";
        preOrderRecursive(node->l);
        preOrderRecursive(node->r);
    }

    void toStringRecursive(BSTNode<Elem>* node, stringstream& stream) {
        if (!node) return;
        stream << "(" << node->idx << ": [p: " << (node->p ? to_string(node->p->idx) : "NULL")
            << ", l: " << (node->l ? to_string(node->l->idx) : "NULL")
            << ", r: " << (node->r ? to_string(node->r->idx) : "NULL") << "], wartosc: "
            << node->value << ")\n";
        toStringRecursive(node->l, stream);
        toStringRecursive(node->r, stream);
    }
};

bool intComparator(const int& a, const int& b) {
    return a < b;  
}
int main() {
    
    srand(static_cast<unsigned>(time(0)));
    const int MAX_ORDER = 7;
    BST<int> bst;

    for (int i = 1; i <= MAX_ORDER; ++i) {
        int n = static_cast<int>(pow(10, i));
        clock_t startAdd = clock();

        for (int i = 0; i < n; ++i) {
            int random_value = rand() % 10000;
            bst.add(random_value, intComparator);
        }

        clock_t endAdd = clock();
        double addTime = double(endAdd - startAdd) / CLOCKS_PER_SEC;

        int height = bst.getHeight();
        double logN = log2(n);
        //cout << bst.toString();
        cout << "Data Size: " << n << ", Add Time: " << addTime << "s, Height: " << height
            << ", Height/Log2(N): " << (height / logN) << endl;

        const int SEARCH_TRIALS = 10000;
        int hits = 0;
        clock_t startFind = clock();

        for (int i = 0; i < SEARCH_TRIALS; ++i) {
            int search_value = rand() % 10000;
            if (bst.find(search_value, intComparator)) {
                ++hits;
            }
        }

        clock_t endFind = clock();
        double findTime = double(endFind - startFind) / CLOCKS_PER_SEC;

        cout << "Search Time: " << findTime << "s, hits: " << hits << endl;

        //cout << bst.toString();
        bst.clear();
        
    }
    /*bst.add(10, intComparator);
    bst.add(5, intComparator);
    bst.add(15, intComparator);
    bst.add(3, intComparator);
    bst.add(7, intComparator);

    cout << bst.toString();
    if (bst.find(2, intComparator)) {
        cout << "found" << endl;
    }
    else {
        cout << "not found" << endl;
    }*/
    return 0;
}
