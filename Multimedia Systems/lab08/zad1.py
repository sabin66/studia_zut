import cv2
import numpy as np
import matplotlib.pyplot as plt
import os

##############################################################################
######   Konfiguracja       ##################################################
##############################################################################

<<<<<<< HEAD
kat=r'.'                                 # katalog z plikami wideo
plik="clip_2.mp4"                       # nazwa pliku
ile=100                                 # ile klatek odtworzyć? <0 - całość
key_frame_counter=4                     # co która klatka ma być kluczowa i nie podlegać kompresji
plot_frames=np.array([31])              # automatycznie wyrysuj wykresy
auto_pause_frames=np.array([25])        # automatycznie za pauzuj dla klatki
subsampling="4:4:4"                     # parametry dla chroma subsampling
dzielnik=1                              # dzielnik przy zapisie różnicy
wyswietlaj_kaltki=True                  # czy program ma wyświetlać klatki
ROI = [[150,350,800,1100]]                   # wyświetlane fragmenty (można podać kilka )
=======
kat=r'.'                                # katalog z plikami wideo
plik="clip_2.mp4"                       # nazwa pliku
ile=20                                  # ile klatek odtworzyć? <0 - całość
key_frame_counter=12                 # co która klatka ma być kluczowa i nie podlegać kompresji
plot_frames=np.array([13])              # automatycznie wyrysuj wykresy
auto_pause_frames=np.array([])          # automatycznie za pauzuj dla klatki
subsampling="4:2:0"                     # parametry dla chroma subsampling
dzielnik=2                              # dzielnik przy zapisie różnicy
wyswietlaj_kaltki=False                 # czy program ma wyświetlać klatki
ROI = [[0,100,0,100]]                   # wyświetlane fragmenty (można podać kilka)
czy_rle = True
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938

##############################################################################
####     Kompresja RLE       #################################################
##############################################################################

def RLE_encode(data_2d):
    flat = data_2d.flatten()
    if len(flat) == 0:
        return []
    
    runs = []
    current_val = flat[0]
    count = 1
    
    for i in range(1, len(flat)):
        if flat[i] == current_val:
            count += 1
        else:
            runs.append((count, int(current_val)))
            current_val = flat[i]
            count = 1
    runs.append((count, int(current_val)))
    return runs

def RLE_decode(runs, shape):
    flat = []
    for count, value in runs:
        flat.extend([value] * count)
    return np.array(flat, dtype=int).reshape(shape)

def get_compressed_size(c_data):
    if isinstance(c_data, list):
        return len(c_data) * 2
    else:
        return c_data.size

class data:
    def init(self):
        self.Y=None 
        self.Cb=None
        self.Cr=None 
        self.semi_Y=None
        self.semi_Cb=None
        self.semi_Cr=None

def Chroma_subsampling(L, subsampling):
    if subsampling == "4:2:2":
        return L[:, ::2]
    elif subsampling == "4:4:0":
        return L[::2, :]
    elif subsampling == "4:2:0":
        return L[::2, ::2]
    elif subsampling == "4:1:1":
        return L[:, ::4]
    elif subsampling == "4:1:0":
        return L[::2, ::4]
    else:
        return L

def Chroma_resampling(L, subsampling):
    if subsampling == "4:2:2":
        return np.repeat(L, 2, axis=1)
    elif subsampling == "4:4:0":
        return np.repeat(L, 2, axis=0)
    elif subsampling == "4:2:0":
        L = np.repeat(L, 2, axis=0)
        L = np.repeat(L, 2, axis=1)
        return L
    elif subsampling == "4:1:1":
        return np.repeat(L, 4, axis=1)
    elif subsampling == "4:1:0":
        L = np.repeat(L, 2, axis=0)
        L = np.repeat(L, 4, axis=1)
        return L
    else:  # domyślnie "4:4:4" - bez zmian
        return L

        
def frame_image_to_class(frame,subsampling):
    Frame_class = data()
    Frame_class.Y=frame[:,:,0].astype(int)
    Frame_class.Cb=Chroma_subsampling(frame[:,:,2].astype(int),subsampling)
    Frame_class.Cr=Chroma_subsampling(frame[:,:,1].astype(int),subsampling)
    return Frame_class


def frame_layers_to_image(Y,Cr,Cb,subsampling):  
    Cb=Chroma_resampling(Cb,subsampling)
    Cr=Chroma_resampling(Cr,subsampling)
    return np.dstack([Y,Cr,Cb]).clip(0,255).astype(np.uint8)

def compress_KeyFrame(Frame_class):
    KeyFrame = data()
    KeyFrame.semi_Y=Frame_class.Y
    KeyFrame.semi_Cb=Frame_class.Cb
    KeyFrame.semi_Cr=Frame_class.Cr
    if czy_rle:
        KeyFrame.Y=RLE_encode(Frame_class.Y) # Frame_class.Y jesli bez RLE
        KeyFrame.Cb=RLE_encode(Frame_class.Cb)
        KeyFrame.Cr=RLE_encode(Frame_class.Cr)
    else:
        KeyFrame.Y = Frame_class.Y
        KeyFrame.Cb = Frame_class.Cb
        KeyFrame.Cr = Frame_class.Cr
    return KeyFrame

def decompress_KeyFrame(KeyFrame):
    Y=KeyFrame.semi_Y
    Cb=KeyFrame.semi_Cb
    Cr=KeyFrame.semi_Cr
    frame_image=frame_layers_to_image(Y,Cr,Cb,subsampling)
    return frame_image

def compress_not_KeyFrame(Frame_class, KeyFrame, inne_paramerty_do_dopisania=None):
    Compress_data = data()
    # (Frame - KeyFrame) // dzielnik
    diff_Y = (Frame_class.Y - KeyFrame.semi_Y) // dzielnik
    diff_Cb = (Frame_class.Cb - KeyFrame.semi_Cb) // dzielnik
    diff_Cr = (Frame_class.Cr - KeyFrame.semi_Cr) // dzielnik
    Compress_data.semi_Y = diff_Y
    Compress_data.semi_Cb = diff_Cb
    Compress_data.semi_Cr = diff_Cr
    Compress_data.Y = RLE_encode(diff_Y)
    Compress_data.Cb = RLE_encode(diff_Cb)
    Compress_data.Cr = RLE_encode(diff_Cr)
    return Compress_data

def decompress_not_KeyFrame(Compress_data,  KeyFrame , inne_paramerty_do_dopisania=None):
    Y = KeyFrame.semi_Y + Compress_data.semi_Y * dzielnik
    Cb = KeyFrame.semi_Cb + Compress_data.semi_Cb * dzielnik
    Cr = KeyFrame.semi_Cr + Compress_data.semi_Cr * dzielnik
    return frame_layers_to_image(Y,Cr,Cb,subsampling)

<<<<<<< HEAD
def plotDiffrence(ReferenceFrame,DecompressedFrame,ROI, save_path=None, title_extra=""):
    # Porównanie: obraz w RGB, warstwy różnic w Y/Cb/Cr
    # ROI - Region of Interest współrzędne fragmentu który chcemy przybliżyć i ocenić w formacie [w1,w2,k1,k2]
    
    # Konwersja do RGB na potrzeby obrazu
=======
def plotDiffrence(ReferenceFrame,DecompressedFrame,ROI):
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938
    ref_BGR = cv2.cvtColor(ReferenceFrame, cv2.COLOR_YCrCb2BGR)
    dec_BGR = cv2.cvtColor(DecompressedFrame, cv2.COLOR_YCrCb2BGR)
    ref_RGB = cv2.cvtColor(ref_BGR, cv2.COLOR_BGR2RGB)
    dec_RGB = cv2.cvtColor(dec_BGR, cv2.COLOR_BGR2RGB)
    
<<<<<<< HEAD
    ref_rgb_roi = ref_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_rgb_roi = dec_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_rgb = np.abs(ref_rgb_roi.astype(float) - dec_rgb_roi.astype(float))
    
    # Kanały YCbCr do analizy warstw
    # ReferenceFrame jest w formacie YCrCb (OpenCV): kanał 0=Y, 1=Cr, 2=Cb
=======
    ref_roi = ref_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_roi = dec_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_rgb = np.abs(ref_roi.astype(float) - dec_roi.astype(float))

>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938
    ref_ycrcb_roi = ReferenceFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_ycrcb_roi = DecompressedFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    
<<<<<<< HEAD
    ref_Y  = ref_ycrcb_roi[:,:,0].astype(float)
    ref_Cr = ref_ycrcb_roi[:,:,1].astype(float)
    ref_Cb = ref_ycrcb_roi[:,:,2].astype(float)
    dec_Y  = dec_ycrcb_roi[:,:,0].astype(float)
    dec_Cr = dec_ycrcb_roi[:,:,1].astype(float)
    dec_Cb = dec_ycrcb_roi[:,:,2].astype(float)
    
    diff_Y  = np.abs(ref_Y - dec_Y)
    diff_Cb = np.abs(ref_Cb - dec_Cb)
    diff_Cr = np.abs(ref_Cr - dec_Cr)
    
    # Obliczenie metryk jakości w RGB
    mse_val = np.mean(diff_rgb**2)
    psnr_val = 10 * np.log10(255**2 / mse_val) if mse_val > 0 else float('inf')
    
    fig, axs = plt.subplots(4, 3, figsize=(16, 14))
    main_title = f"Porównanie klatek (ROI: [{ROI[0]},{ROI[1]}] x [{ROI[2]},{ROI[3]}])"
    if title_extra:
        main_title += f"\n{title_extra}"
    main_title += f"\nMSE={mse_val:.2f}, PSNR={psnr_val:.2f} dB"
    fig.suptitle(main_title, fontsize=13)
    
    # Wiersz 0: Obraz RGB - oryginał, różnica, dekompresja
    axs[0,0].imshow(ref_rgb_roi)
=======
    fig, axs = plt.subplots(7, 3, figsize=(16, 22))
    fig.suptitle("Porównanie klatek (ROI: [{},{}] x [{},{}])".format(ROI[0],ROI[1],ROI[2],ROI[3]), fontsize=14)
    
    axs[0,0].imshow(ref_roi)
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938
    axs[0,0].set_title("Oryginał (RGB)")
    axs[0,1].imshow(diff_rgb)
    axs[0,1].set_title("Różnica |RGB|")
    axs[0,2].imshow(dec_rgb_roi)
    axs[0,2].set_title("Dekompresja (RGB)")
<<<<<<< HEAD
    
    # Wiersz 1: Warstwa Y (luminancja)
    axs[1,0].imshow(ref_Y, cmap='gray', vmin=0, vmax=255)
    axs[1,0].set_title("Oryginał Y")
    axs[1,1].imshow(diff_Y, cmap='hot', vmin=0, vmax=max(np.max(diff_Y),1))
    axs[1,1].set_title("Różnica Y (max={:.1f})".format(np.max(diff_Y)))
    axs[1,2].imshow(dec_Y, cmap='gray', vmin=0, vmax=255)
    axs[1,2].set_title("Dekompresja Y")
    
    # Wiersz 2: Warstwa Cb (chrominancja niebieska)
    axs[2,0].imshow(ref_Cb, cmap='gray', vmin=0, vmax=255)
    axs[2,0].set_title("Oryginał Cb")
    axs[2,1].imshow(diff_Cb, cmap='hot', vmin=0, vmax=max(np.max(diff_Cb),1))
    axs[2,1].set_title("Różnica Cb (max={:.1f})".format(np.max(diff_Cb)))
    axs[2,2].imshow(dec_Cb, cmap='gray', vmin=0, vmax=255)
    axs[2,2].set_title("Dekompresja Cb")
    
    # Wiersz 3: Warstwa Cr (chrominancja czerwona)
    axs[3,0].imshow(ref_Cr, cmap='gray', vmin=0, vmax=255)
    axs[3,0].set_title("Oryginał Cr")
    axs[3,1].imshow(diff_Cr, cmap='hot', vmin=0, vmax=max(np.max(diff_Cr),1))
    axs[3,1].set_title("Różnica Cr (max={:.1f})".format(np.max(diff_Cr)))
    axs[3,2].imshow(dec_Cr, cmap='gray', vmin=0, vmax=255)
    axs[3,2].set_title("Dekompresja Cr")
=======

    axs[1,0].imshow(ref_roi[:,:,0], cmap='Reds', vmin=0, vmax=255)
    axs[1,0].set_title("Oryginał R")
    axs[1,1].imshow(diff_rgb[:,:,0], cmap='Reds', vmin=0, vmax=max(np.max(diff_rgb[:,:,0]),1))
    axs[1,1].set_title("Różnica R (max={:.1f})".format(np.max(diff_rgb[:,:,0])))
    axs[1,2].imshow(dec_roi[:,:,0], cmap='Reds', vmin=0, vmax=255)
    axs[1,2].set_title("Dekompresja R")
    
    axs[2,0].imshow(ref_roi[:,:,1], cmap='Greens', vmin=0, vmax=255)
    axs[2,0].set_title("Oryginał G")
    axs[2,1].imshow(diff_rgb[:,:,1], cmap='Greens', vmin=0, vmax=max(np.max(diff_rgb[:,:,1]),1))
    axs[2,1].set_title("Różnica G (max={:.1f})".format(np.max(diff_rgb[:,:,1])))
    axs[2,2].imshow(dec_roi[:,:,1], cmap='Greens', vmin=0, vmax=255)
    axs[2,2].set_title("Dekompresja G")
    
    axs[3,0].imshow(ref_roi[:,:,2], cmap='Blues', vmin=0, vmax=255)
    axs[3,0].set_title("Oryginał B")
    axs[3,1].imshow(diff_rgb[:,:,2], cmap='Blues', vmin=0, vmax=max(np.max(diff_rgb[:,:,2]),1))
    axs[3,1].set_title("Różnica B (max={:.1f})".format(np.max(diff_rgb[:,:,2])))
    axs[3,2].imshow(dec_roi[:,:,2], cmap='Blues', vmin=0, vmax=255)
    axs[3,2].set_title("Dekompresja B")
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938
    
    axs[4,0].imshow(ref_ycrcb_roi[:,:,0], cmap='gray', vmin=0, vmax=255)
    axs[4,0].set_title("Oryginał Y")
    axs[4,1].imshow(diff_ycrcb[:,:,0], cmap='gray', vmin=0, vmax=max(np.max(diff_ycrcb[:,:,0]),1))
    axs[4,1].set_title("Różnica Y (max={:.1f})".format(np.max(diff_ycrcb[:,:,0])))
    axs[4,2].imshow(dec_ycrcb_roi[:,:,0], cmap='gray', vmin=0, vmax=255)
    axs[4,2].set_title("Dekompresja Y")
    
    axs[5,0].imshow(ref_ycrcb_roi[:,:,2], cmap='coolwarm', vmin=0, vmax=255)
    axs[5,0].set_title("Oryginał Cb")
    axs[5,1].imshow(diff_ycrcb[:,:,2], cmap='coolwarm', vmin=0, vmax=max(np.max(diff_ycrcb[:,:,2]),1))
    axs[5,1].set_title("Różnica Cb (max={:.1f})".format(np.max(diff_ycrcb[:,:,2])))
    axs[5,2].imshow(dec_ycrcb_roi[:,:,2], cmap='coolwarm', vmin=0, vmax=255)
    axs[5,2].set_title("Dekompresja Cb")
    
    axs[6,0].imshow(ref_ycrcb_roi[:,:,1], cmap='coolwarm', vmin=0, vmax=255)
    axs[6,0].set_title("Oryginał Cr")
    axs[6,1].imshow(diff_ycrcb[:,:,1], cmap='coolwarm', vmin=0, vmax=max(np.max(diff_ycrcb[:,:,1]),1))
    axs[6,1].set_title("Różnica Cr (max={:.1f})".format(np.max(diff_ycrcb[:,:,1])))
    axs[6,2].imshow(dec_ycrcb_roi[:,:,1], cmap='coolwarm', vmin=0, vmax=255)
    axs[6,2].set_title("Dekompresja Cr")
    
    plt.tight_layout()
<<<<<<< HEAD
    print("Diff RGB min:", np.min(diff_rgb), "max:", np.max(diff_rgb), f"MSE={mse_val:.2f} PSNR={psnr_val:.2f}dB")
    
    if save_path:
        fig.savefig(save_path, dpi=150, bbox_inches='tight')
        print(f"Zapisano: {save_path}")
        plt.close(fig)
    
    return mse_val, psnr_val
=======
    print("Diff RGB min:", np.min(diff_rgb), "max:", np.max(diff_rgb))
    print("Diff Y min:", np.min(diff_ycrcb[:,:,0]), "max:", np.max(diff_ycrcb[:,:,0]))
    print("Diff Cb min:", np.min(diff_ycrcb[:,:,2]), "max:", np.max(diff_ycrcb[:,:,2]))
    print("Diff Cr min:", np.min(diff_ycrcb[:,:,1]), "max:", np.max(diff_ycrcb[:,:,1]))
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938


all_subsamplings = ["4:4:4", "4:2:2", "4:4:0", "4:2:0", "4:1:1", "4:1:0"]

for subsampling in all_subsamplings:
    print("\n=== Subsampling: {} ===".format(subsampling))
    
    cap = cv2.VideoCapture(os.path.join(kat,plik))
    
    if ile<0:
        total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    else:
        total = ile
    
    if wyswietlaj_kaltki:
        cv2.namedWindow('Normal Frame')
        cv2.namedWindow('Decompressed Frame')
    
    compression_information=np.zeros((3,total))
    
    for i in range(total):
        ret, frame = cap.read()
        if not ret:
            break
        if wyswietlaj_kaltki:
            cv2.imshow('Normal Frame',frame)
        frame=cv2.cvtColor(frame,cv2.COLOR_BGR2YCrCb)
        original_channel_size = frame[:,:,0].size
        Frame_class = frame_image_to_class(frame,subsampling)
        if (i % key_frame_counter)==0: # pobieranie klatek kluczowych
            KeyFrame = compress_KeyFrame(Frame_class)
            cY=KeyFrame.Y
            cCb=KeyFrame.Cb
            cCr=KeyFrame.Cr
            Decompresed_Frame = decompress_KeyFrame(KeyFrame)
        else: # kompresja
            Compress_data = compress_not_KeyFrame(Frame_class, KeyFrame)
            cY=Compress_data.Y
            cCb=Compress_data.Cb
            cCr=Compress_data.Cr
            Decompresed_Frame = decompress_not_KeyFrame(Compress_data,  KeyFrame)
        
        compression_information[0,i]= (original_channel_size - get_compressed_size(cY))/original_channel_size
        compression_information[1,i]= (original_channel_size - get_compressed_size(cCb))/original_channel_size
        compression_information[2,i]= (original_channel_size - get_compressed_size(cCr))/original_channel_size
        if wyswietlaj_kaltki:
            cv2.imshow('Decompressed Frame',cv2.cvtColor(Decompresed_Frame,cv2.COLOR_YCrCb2BGR))
        
        if np.any(plot_frames==i): # rysuj wykresy
            for r in ROI:
                plotDiffrence(frame,Decompresed_Frame,r)
            
        if np.any(auto_pause_frames==i):
            cv2.waitKey(-1) #wait until any key is pressed
        
        if wyswietlaj_kaltki:
            k = cv2.waitKey(1) #& 0xff
            if k==ord('q'):
                break
            elif k == ord('p'):
                cv2.waitKey(-1) #wait until any key is pressed
    
    cap.release()
    
    plt.figure()
    plt.plot(np.arange(0,total),compression_information[0,:]*100, label='Y')
    plt.plot(np.arange(0,total),compression_information[1,:]*100, label='Cb')
    plt.plot(np.arange(0,total),compression_information[2,:]*100, label='Cr')
    plt.legend()
    plt.xlabel("Numer klatki")
    plt.ylabel("Stopień kompresji [%]")
    plt.title("File:{}, subsampling={}, divider={}, KeyFrame={}, RLE".format(plik,subsampling,dzielnik,key_frame_counter))

<<<<<<< HEAD
plt.figure()
plt.plot(np.arange(0,ile),compression_information[0,:]*100, label='Y')
plt.plot(np.arange(0,ile),compression_information[1,:]*100, label='Cb')
plt.plot(np.arange(0,ile),compression_information[2,:]*100, label='Cr')
plt.legend()
plt.xlabel("Numer klatki")
plt.ylabel("Stopień kompresji [%]")
plt.title("File:{}, subsampling={}, divider={}, KeyFrame={} ".format(plik,subsampling,dzielnik,key_frame_counter))
plt.savefig(f"kompresja_{plik.replace('.mp4','')}_{subsampling.replace(':','')}_{dzielnik}_kf{key_frame_counter}.png", dpi=150, bbox_inches='tight')
print(f"Zapisano wykres liniowy kompresji")
=======
>>>>>>> bea2eddb2ded5a074f49dd3d2c480ca053302938
plt.show()