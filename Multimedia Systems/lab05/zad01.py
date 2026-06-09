import numpy as np
import sys
from tqdm import tqdm
from PIL import Image

def get_size(obj, seen=None):
    size = sys.getsizeof(obj)
    if seen is None:
        seen = set()
    obj_id = id(obj)
    if obj_id in seen:
        return 0
    seen.add(obj_id)
    if isinstance(obj, np.ndarray):
        size = obj.nbytes
    elif isinstance(obj, dict):
        size += sum([get_size(v, seen) for v in obj.values()])
        size += sum([get_size(k, seen) for k in obj.keys()])
    elif hasattr(obj, '__dict__'):
        size += get_size(obj.__dict__, seen)
    elif hasattr(obj, '__iter__') and not isinstance(obj, (str, bytes, bytearray)):
        size += sum([get_size(i, seen) for i in obj])
    return size

# dwie ponizsze funckje od dobrego andrzeja zwirka
def get_repeating_length(data, start_idx):
    count = 1
    for i in range(start_idx, len(data) - 1):
        if data[i] == data[i+1]:
            count += 1
            if count == 128:
                break
        else:
            break
    return count

def get_different_length(data, start_idx):
    count = 1
    for i in range(start_idx + 1, len(data)):
        if i < len(data) - 1 and data[i] == data[i+1]:
            break
        count += 1
        if count == 128:
            break
    return count

def rle_encode(data):
    data = data.astype(int)
    shape = data.shape
    n_dims = len(shape)
    
    header = [n_dims] + list(shape)
    flat_data = data.flatten()
    
    if len(flat_data) == 0:
        return np.array(header, dtype=int)
        
    encoded = []
    count = 1
    prev = flat_data[0]
    
    for i in range(1, len(flat_data)):
        if flat_data[i] == prev:
            count += 1
        else:
            encoded.extend([count, prev])
            prev = flat_data[i]
            count = 1
    encoded.extend([count, prev])
    
    return np.concatenate([header, encoded]).astype(int)

def rle_decode(encoded_data):
    n_dims = encoded_data[0]
    shape = tuple(encoded_data[1 : int(1 + n_dims)])
    compressed = encoded_data[int(1 + n_dims) :]
    
    decoded = []
    for i in range(0, len(compressed), 2):
        count = compressed[i]
        value = compressed[i+1]
        decoded.extend([value] * count)
        
    return np.array(decoded, dtype=int).reshape(shape)

def byterun_encode(data: np.ndarray) -> np.ndarray:
    data = data.astype(int)
    shape = data.shape
    n_dims = len(shape)
    
    header = [n_dims] + list(shape)
    flat_data = data.flatten()
    
    if len(flat_data) == 0:
        return np.array(header, dtype=int)
        
    encoded = []
    i = 0
    n = len(flat_data)
    
    while i < n:
        run_len = get_repeating_length(flat_data, i)
            
        if run_len > 1:
            encoded.extend([-(run_len - 1), flat_data[i]])
            i += run_len
        else:
            non_run_len = get_different_length(flat_data, i)
            encoded.append(non_run_len - 1)
            encoded.extend(flat_data[i : i + non_run_len])
            i += non_run_len
            
    return np.concatenate([header, encoded]).astype(int)

def byterun_decode(encoded_data):
    n_dims = encoded_data[0]
    shape = tuple(encoded_data[1 : int(1 + n_dims)])
    compressed = encoded_data[int(1 + n_dims) :]
    
    decoded = []
    i = 0
    n = len(compressed)
    
    while i < n:
        indicator = compressed[i]
        i += 1
        if indicator < 0:
            count = 1 - indicator
            value = compressed[i]
            i += 1
            decoded.extend([value] * count)
        else:
            count = indicator + 1
            decoded.extend(compressed[i : i + count])
            i += count
            
    return np.array(decoded, dtype=int).reshape(shape)


if __name__ == "__main__":
    test_files = ["rysunek_techniczny.jpg", "formualrz.png", "ross.jpg"]
    
    for file in tqdm(test_files, desc="Przetwarzanie plików", position=0):
        tqdm.write(f"\n{'='*50}")
        tqdm.write(f"ANALIZA OBRAZU: {file}")
        tqdm.write(f"{'='*50}")
        
        try:
            img = Image.open(file)
            data = np.array(img).astype(int)
        except FileNotFoundError:
            tqdm.write(f"Błąd: Nie znaleziono pliku '{file}'")
            continue
            
        rozmiar_przed = get_size(data)
        
        algorytmy = [("RLE", rle_encode, rle_decode), ("ByteRun", byterun_encode, byterun_decode)]
        
        for algorithm, koder, dekoder in algorytmy:
            tqdm.write(f"\n--- Algorytm: {algorithm} ---")
            
            # Kompresja
            skompresowane = koder(data)
            rozmiar_po = get_size(skompresowane)
            
            # Dekompresja
            zdekompresowane = dekoder(skompresowane)
            
            # Weryfikacja
            poprawnosc = np.array_equal(data, zdekompresowane)
            
            CR = rozmiar_przed / rozmiar_po if rozmiar_po > 0 else 0
            PR = (rozmiar_po / rozmiar_przed) * 100 if rozmiar_przed > 0 else 0
            
            tqdm.write(f"Poprawność dekompresji (identyczność): {poprawnosc}")
            tqdm.write(f"Rozmiar przed kompresją: {rozmiar_przed} bajtów")
            tqdm.write(f"Rozmiar po kompresji:    {rozmiar_po} bajtów")
            tqdm.write(f"Stopień kompresji (CR):  {CR:.4f}")
            tqdm.write(f"Procent oryginału (PR):  {PR:.2f}%")