#include <iostream>
#include <sstream>
#include <ctime>
#include <cmath>
#include <cstdlib>
using namespace std;

enum class Color
{
    RED,
    BLACK
};

template<typename Elem>
class RBNode {
public:
    Elem value;
    RBNode* parent;
    RBNode* left;
    RBNode* right;
    Color color;
    int idx;

    RBNode(Elem value, int idx)
        : value(value), parent(nullptr), left(nullptr), right(nullptr), color(Color::RED), idx(idx) {}
};

template<typename Elem>
class RBTree {
public:
    RBTree() : root(nullptr), nodeCount(0) {}

    void add(Elem value) {
        RBNode<Elem>* newNode = new RBNode<Elem>(value, nodeCount++);
        if (root == nullptr) {
            root = newNode;
            root->color = Color::BLACK;
        }
        else
        {
            bstInsert(root, newNode);
        }
        fixInsertion(newNode);
    }

    bool find(Elem value) {
        return findNode(root, value) != nullptr;
    }

    void clear() {
        clearTree(root);
        root = nullptr;
        nodeCount = 0;
    }

    int getHeight() {
        return calculateHeight(root);
    }

    void inOrderTraversal() {
        inOrder(root);
        cout << endl;
    }

    void preOrderTraversal() {
        preOrder(root);
        cout << endl;
    }

    string toString() {
        stringstream stream;
        stream << "red-black tree:\nsize: " << nodeCount << "\n{\n";
        generateTreeString(root, stream);
        stream << "}\n";
        return stream.str();
    }

private:
    RBNode<Elem>* root;
    int nodeCount;
    // nastepca - wezel z najmniejsza wartoscia w prawym poddrzewie
    RBNode<Elem>* bstInsert(RBNode<Elem>* node, RBNode<Elem>* newNode) {
        if (!node) return newNode;

        if (newNode->value < node->value) {
            node->left = bstInsert(node->left, newNode);
            node->left->parent = node;
        }
        else {
            node->right = bstInsert(node->right, newNode);
            node->right->parent = node;
        }
        return node;
    }
    void fixInsertion(RBNode<Elem>* node) {
        if (node == nullptr || node->parent == nullptr)
        {
            return;
        }
        while (node != root && node->parent->color == Color::RED)
        {
            RBNode<Elem>* grandparent = node->parent->parent;
            // Sprawdzenie, czy dziadek istnieje
            if (grandparent == nullptr)
            {
                return;
            }

            if (node->parent == grandparent->left)	// rodzic jest lewym dzieckiem dziadka
            {
                RBNode<Elem>* uncle = grandparent->right;

                // Przypadek 1: wujek jest czerwony
                if (uncle != nullptr && uncle->color == Color::RED)
                {
                    node->parent->color = Color::BLACK;
                    uncle->color = Color::BLACK;
                    grandparent->color = Color::RED;
                    node = grandparent;			//nie zmienia to samego wêz³a grandparent ani jego wartoœci
                }
                else // wujek jest czarny
                {
                    // Przypadek 2: 
                    if (node == node->parent->right)
                    {
                        node = node->parent;
                        rotateLeft(node);
                    }
                    // Przypadek 3: 
                    node->parent->color = Color::BLACK;
                    grandparent->color = Color::RED;
                    rotateRight(grandparent);
                }
            }
            else	// rodzic jest prawy dzieckiem dziadka
            {
                RBNode<Elem>* uncle = grandparent->left;

                // Przypadek 1: 
                if (uncle != nullptr && uncle->color == Color::RED)
                {
                    node->parent->color = Color::BLACK;
                    uncle->color = Color::BLACK;
                    grandparent->color = Color::RED;
                    node = grandparent;	// przesuwamy wêze³ w górê
                }
                else
                {
                    // Przypadek 2: 
                    if (node == node->parent->left)
                    {
                        node = node->parent;
                        rotateRight(node);
                    }
                    // Przypadek 3: 
                    node->parent->color = Color::BLACK;
                    grandparent->color = Color::RED;
                    rotateLeft(grandparent);
                }
            }
        }
        // korzeñ jest czarny
        root->color = Color::BLACK;
    }



    void rotateLeft(RBNode<Elem>* node) {
        RBNode<Elem>* rightChild = node->right;
        node->right = rightChild->left;
        // Jeœli prawy syn ma lewe dziecko, to ustawiamy jego rodzica na node
        if (rightChild->left) {
            rightChild->left->parent = node;
        }
        rightChild->parent = node->parent;
        // Jeœli node jest korzeniem, to root zmienia siê na rightChild
        if (!node->parent) {
            root = rightChild;
        }
        else if (node == node->parent->left) {
            node->parent->left = rightChild;
        }
        else {
            node->parent->right = rightChild;
        }
        // Ustawiamy leftChild jako lewe dziecko wêz³a
        rightChild->left = node;
        node->parent = rightChild;
    }



    void rotateRight(RBNode<Elem>* node) {
        RBNode<Elem>* leftChild = node->left;
        node->left = leftChild->right;

        // Jeœli lewy syn ma prawe dziecko, to ustawiamy jego rodzica na node
        if (leftChild->right) {
            leftChild->right->parent = node;
        }

        leftChild->parent = node->parent;

        // Jeœli node jest korzeniem, to root zmienia siê na leftChild
        if (!node->parent) {
            root = leftChild;
        }
        else if (node == node->parent->right) {
            node->parent->right = leftChild;
        }
        else {
            node->parent->left = leftChild;
        }

        // Ustawiamy rightChild jako prawe dziecko wêz³a
        leftChild->right = node;
        node->parent = leftChild;
    }



    RBNode<Elem>* findNode(RBNode<Elem>* node, Elem value) {
        if (!node || node->value == value) return node;
        if (value < node->value) return findNode(node->left, value);
        return findNode(node->right, value);
    }

    void clearTree(RBNode<Elem>* node) {
        if (!node) return;
        clearTree(node->left);
        clearTree(node->right);
        delete node;
    }

    int calculateHeight(RBNode<Elem>* node) {
        if (!node) return 0;
        return 1 + max(calculateHeight(node->left), calculateHeight(node->right));
    }

    void inOrder(RBNode<Elem>* node) {
        if (!node) return;
        inOrder(node->left);
        cout << node->value << " ";
        inOrder(node->right);
    }

    void preOrder(RBNode<Elem>* node) {
        if (!node) return;
        cout << node->value << " ";
        preOrder(node->left);
        preOrder(node->right);
    }

    void generateTreeString(RBNode<Elem>* node, stringstream& stream) {
        if (!node) return;
        stream << "(" << node->idx << ": [" << (node->color == Color::RED ? "red" : "black") << ", p: "
            << (node->parent ? to_string(node->parent->idx) : "NULL")
            << ", l: " << (node->left ? to_string(node->left->idx) : "NULL")
            << ", r: " << (node->right ? to_string(node->right->idx) : "NULL") << "] " << node->value << ")\n";
        generateTreeString(node->left, stream);
        generateTreeString(node->right, stream);
    }
};
int main() {
    const int MAX_ORDER = 7;
    RBTree<int>* rbt = new RBTree<int>(); 

    for (int o = 1; o <= MAX_ORDER; o++) {
        const int n = pow(10, o); 

        clock_t t1 = clock();
        for (int i = 0; i < n; i++) {
            int value = rand() % (10 * n);
            rbt->add(value);
        }
        clock_t t2 = clock();
        cout << "Dodano " << n << " elementw. Czas: " << double(t2 - t1) / CLOCKS_PER_SEC << " s" << endl;

        int height = rbt->getHeight();
        double logN = log2(n);
        cout << "Liczba elementow: " << n << "\n";
        cout << "Wysokosc drzewa: " << height << "\n";
        cout << "log2(n): " << logN << "\n";
        cout << "Stosunek wysokosci drzewa do log2(n): "
            << (height / logN) << "\n\n";

        const int m = pow(10, 4); 
        int hits = 0; 
        t1 = clock();
        for (int i = 0; i < m; i++) {
            int searchValue = rand() % (10 * n); 
            if (rbt->find(searchValue)) {
                hits++;
            }
        }
        t2 = clock();
        cout << "Proby: " << m << ", Trafienia: " << hits
            << ", Czas: " << double(t2 - t1) / CLOCKS_PER_SEC << " s" << endl;

        rbt->clear(); 
    }

    delete rbt; 
    return 0;
    /*rbt->add(10);
    rbt->add(20);
    rbt->add(30);
    rbt->add(15);
    rbt->add(25);
    rbt->add(22);
    rbt->add(14);
    cout << rbt->toString() << endl;
    int h = rbt->getHeight();
    cout << h << endl;
    delete rbt;*/
}




// preorder - root, lewe, prawe
// inorder - lewe,root,prawe
// postorder - lewe,prawe,root