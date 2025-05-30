#include <iostream>
#include <ctime>
#include <fstream>
#include <sstream>
using namespace std;
enum UnionFindVariant {
	Naive,
	ByRank,
	PathCompression,
	ByRankWithPathCompression
};
template <typename T>
class DynamicArray {
private:
	T* data;
	size_t size;
	size_t capacity;

	void resize(size_t newCapacity) {
		capacity = newCapacity;
		T* new_data = new T[capacity];
		for (size_t i = 0; i < size; i++) {
			new_data[i] = data[i];
		}
		delete[] data;
		data = new_data;
	}
public:
	DynamicArray(size_t initial_capacity = 10) {
		capacity = initial_capacity;
		data = new T[capacity];
		size = 0;
	}
	// Konstruktor kopiuj�cy
	DynamicArray(const DynamicArray& other) : size(other.size), capacity(other.capacity) {
		data = new T[capacity];
		for (size_t i = 0; i < size; i++) {
			data[i] = other.data[i];
		}
	}

	// Konstruktor przenosz�cy
	DynamicArray(DynamicArray&& other) noexcept : size(other.size), capacity(other.capacity), data(other.data) {
		other.data = nullptr;
		other.size = 0;
		other.capacity = 0;
	}

	// Operator przypisania kopiuj�cy
	DynamicArray& operator=(const DynamicArray& other) {
		if (this != &other) {
			delete[] data;
			size = other.size;
			capacity = other.capacity;
			data = new T[capacity];
			for (size_t i = 0; i < size; i++) {
				data[i] = other.data[i];
			}
		}
		return *this;
	}

	// Operator przypisania przenosz�cy
	DynamicArray& operator=(DynamicArray&& other) noexcept {
		if (this != &other) {
			delete[] data;
			size = other.size;
			capacity = other.capacity;
			data = other.data;
			other.data = nullptr;
			other.size = 0;
			other.capacity = 0;
		}
		return *this;
	}

	~DynamicArray() {
		delete[] data;
	}
	void push_back(T value) {
		if (size == capacity) {
			resize(capacity * 2);
		}
		data[size++] = value;
	}
	T& operator[](size_t index) {
		if (index >= size) {
			throw std::out_of_range("Index out of range");
		}
		return data[index];
	}
	size_t get_size() {
		return size;
	}
	void initialize(size_t newSize, const T& value) {
		if (newSize > capacity) {
			resize(newSize);
		}
		size = newSize;
		for (size_t i = 0; i < newSize; i++) {
			data[i] = value;
		}
	}
	T* begin() {
		return data;
	}

	T* end() {
		return data + size;
	}

	const T* begin() const {
		return data;
	}

	const T* end() const {
		return data + size;
	}
};
class UnionFind {
private:
	DynamicArray<int> parent;
	DynamicArray<int> rank;
	UnionFindVariant variant;
public:
	UnionFind(int n, UnionFindVariant variant = ByRankWithPathCompression) : variant(variant) {
		parent.initialize(n, 0);
		rank.initialize(n, 0);
		for (int i = 0; i < n; i++) {
			parent[i] = i;
		}
	}
	int find(int x) {
		if (variant == PathCompression || variant == ByRankWithPathCompression) {
			if (x != parent[x]) {
				parent[x] = find(parent[x]);
			}
			return parent[x];
		}
		else {
			while (parent[x] != x) {
				x = parent[x];
			}
			return x;
		}
	}

	void addElement() {
		int newIndex = parent.get_size();
		parent.push_back(newIndex);
		rank.push_back(0);
	}
	void unite(int x, int y) {
		int rootX = find(x);
		int rootY = find(y);

		if (rootX != rootY) {
			if (variant == ByRank || variant == ByRankWithPathCompression) {
				if (rank[rootX] < rank[rootY]) {
					parent[rootX] = rootY;
				}
				else if (rank[rootX] > rank[rootY]) {
					parent[rootY] = rootX;
				}
				else {
					parent[rootY] = rootX;
					rank[rootX]++;
				}
			}
			else {
				parent[rootY] = rootX; // Naive union
			}
		}
	}

};
struct Node { // w tej strukturze przechowywane sa wspolrzedne wezla - pomoc w wizualizacji
	int index;
	float x, y;
	Node() : index(0), x(0.0f), y(0.0f) {}
	Node(int index, float x, float y) : index(index), x(x), y(y) {}
};
struct Edge { // krawedzie przechowuja pare wezlow i wage tj. koszt przejscia
	int node1, node2;
	float weight;
	Edge() : node1(0), node2(0), weight(0.0f) {}
	Edge(int node1, int node2, float weight) : node1(node1), node2(node2), weight(weight) {}

	bool operator<(const Edge& other) const {
		return weight < other.weight;
	}
};
class Graph { // graf zawiera liste wezlow i krawedzi
public:
	DynamicArray<Node> nodes;
	DynamicArray<Edge> edges;
	Graph(int n) {
		nodes.initialize(n, Node(0, 0.0f, 0.0f));
	}
	void AddNode(int index, float x, float y) {
		nodes.push_back(Node(index, x, y));
	}
	void AddEdge(int node1, int node2, float weight) {
		edges.push_back(Edge(node1, node2, weight));
	}
};
template <typename T>
int partition(DynamicArray<T>& arr, int low, int high) {
	T pivot = arr[high];
	int i = low - 1;

	for (int j = low; j < high; j++) {
		if (arr[j].weight < pivot.weight) {
			i++;
			T temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}
	}
	T temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
}
template <typename T>
void quickSort(DynamicArray<T>& arr, int low, int high) {
	if (low < high) {
		int pi = partition(arr, low, high);
		quickSort(arr, low, pi - 1);
		quickSort(arr, pi + 1, high);
	}
}
class Kruskal {
private:
	Graph& graph;
	UnionFind& unionFind;
public:
	Kruskal(Graph& g, UnionFind& uf) : graph(g), unionFind(uf) {}

	DynamicArray<Edge> run() {
		clock_t startSort, endSort, startMain, endMain;
		startSort = clock();
		quickSort(graph.edges, 0, graph.edges.get_size() - 1);
		endSort = clock();

		DynamicArray<Edge> mst(graph.nodes.get_size());
		int mstSize = 0;
		startMain = clock();
		for (int i = 0; i < graph.edges.get_size(); i++) {
			const Edge& edge = graph.edges[i];
			int root1 = unionFind.find(edge.node1);
			int root2 = unionFind.find(edge.node2);

			if (root1 != root2) {
				unionFind.unite(edge.node1, edge.node2);
				mst.push_back(edge);
				mstSize++;
			}
			if (mstSize == graph.nodes.get_size() - 1) {
				break;
			}
		}
		endMain = clock();
		double sortTime = double(endSort - startSort) / CLOCKS_PER_SEC;
		double mainTime = double(endMain - startMain) / CLOCKS_PER_SEC;
		cout << "Czas sortowania: " << sortTime << "s" << endl;
		cout << "Czas algorytmu: " << mainTime << "s" << endl;
		return mst;
	}
};
int main() {
	ifstream file("g2.txt");
	if (!file.is_open()) {
		cout << "Nie udalo sie otworzyc pliku" << endl;
		return 1;
	}

	int n, m;
	file >> n;
	Graph g(n);

	for (int i = 0; i < n; i++) {
		int idx;
		float x, y;
		file >> idx >> x >> y;
		g.AddNode(idx, x, y);
	}

	file >> m;
	for (int i = 0; i < m; i++) {
		int node1, node2;
		float weight;
		file >> node1 >> node2 >> weight;
		g.AddEdge(node1, node2, weight);
	}
	file.close();

	UnionFindVariant variants[] = { UnionFindVariant::Naive, UnionFindVariant::ByRank, UnionFindVariant::PathCompression, UnionFindVariant::ByRankWithPathCompression };
	const char* variantNames[] = { "Naive", "ByRank", "PathCompression", "ByRankWithPathCompression" };

	for (int i = 0; i < 4; i++) {
		cout << "Testowanie wariantu " << variantNames[i] << endl;

		UnionFind uf(n, variants[i]);
		Kruskal kruskal(g, uf);
		DynamicArray<Edge> mst = kruskal.run();

		cout << "MST: " << endl;
		float totalWeight = 0;
		for (size_t i = 0; i < mst.get_size(); ++i) {
			totalWeight += mst[i].weight;
		}

		cout << "Waga drzewa: " << totalWeight << endl;
		cout << "Liczba krawedzi: " << mst.get_size() << endl;
		cout << endl;

	}

	return 0;
}




//Naive: Proste, ale wolne, zw�aszcza dla du�ych zbior�w.
//Path Compression : Przyspiesza operacje find, skracaj�c �cie�ki do korzenia.
//Union by Rank : Przyspiesza operacje unite, zapewniaj�c mniejsze wysoko�ci drzew.
//Path Compression + Union by Rank : Najszybszy wariant, ��czy obie optymalizacje, co zapewnia minimaln� g��boko�� drzew i szybkie operacje.















//1. Naive UnionFind Naive
//W tym wariancie operacje find i unite s� wykonywane bez �adnych optymalizacji.
//
//find(x) : Szuka korzenia zbioru, do kt�rego nale�y element x.Procedura polega na tym, �e idziemy w g�r� drzewa, a� napotkamy korze�
// (czyli element, kt�ry jest r�wny swojemu rodzicowi).
//unite(x, y) : ��czy dwa zbiory, do kt�rych nale�� elementy x i y.W tym przypadku po prostu ustawiamy rodzica y na x, bez �adnych optymalizacji.



//2. UnionFind z Path Compression
//W tym wariancie zastosowano optymalizacj� �cie�ki, kt�ra ma na celu zmniejszenie g��boko�ci drzew podczas operacji find.
//
//find(x) : Podczas przeszukiwania drzewa w poszukiwaniu korzenia, po drodze kompresujemy �cie�k�, czyli ustawiamy 
// bezpo�rednich rodzic�w wszystkich odwiedzonych w�z��w na korze�.Dzi�ki temu kolejne operacje find b�d� szybsze, poniewa� �cie�ka do korzenia b�dzie kr�tsza.
//
//Przyk�ad : Je�li element x ma rodzica y, a y ma rodzica z, to po wykonaniu operacji find(x) element x b�dzie mia� bezpo�redniego rodzica z.
//
//unite(x, y) : Operacja unite nie zmienia si� w stosunku do wersji naiwnej.Po prostu ��czy dwa zbiory, ale dzi�ki kompresji �cie�ki 
//operacja find b�dzie szybsza w przysz�o�ci.



//3. Union by Rank
//W tym wariancie dodano optymalizacj� polegaj�c� na ��czeniu drzew o mniejszej g��boko�ci z drzewami o wi�kszej g��boko�ci
// (na podstawie ranku, czyli wysoko�ci drzewa).
//
//find(x) : Dzia�a podobnie jak w wersji naiwnej, ale mo�e by� zoptymalizowane przez kompresj� �cie�ki, je�li jest w��czona.
//unite(x, y) : ��czy dwa zbiory, ale je�li jedno drzewo jest wy�sze od drugiego, to drzewo o mniejszej wysoko�ci jest 
//pod��czane pod korze� drzewa o wi�kszej wysoko�ci.Dzi�ki temu drzewo nie ro�nie zbyt g��boko.



//4. Union by Rank with Path Compression
//Jest to najbardziej zoptymalizowana wersja UnionFind, kt�ra ��czy oba wcze�niejsze podej�cia : ��czenie na podstawie rangi i kompresj� �cie�ki.
//
//find(x) : Dzia�a jak w wersji z kompresj� �cie�ki � podczas przeszukiwania drzewa w poszukiwaniu korzenia, wszystkie odwiedzone 
// w�z�y maj� ustawionych bezpo�rednich rodzic�w na korze�, co skraca przysz�e operacje find.
//
//unite(x, y) : ��czy dwa zbiory, ale najpierw sprawdza rangi(g��boko�� drzew).Drzewo o mniejszej g��boko�ci jest pod��czane pod korze� 
//drzewa o wi�kszej g��boko�ci.Ponadto, dzi�ki kompresji �cie�ki, operacja find jest znacznie szybsza w przysz�o�ci.