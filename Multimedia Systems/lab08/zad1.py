import cv2
import numpy as np
import matplotlib.pyplot as plt
import os

##############################################################################
######   Konfiguracja       ##################################################
##############################################################################

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

def plotDiffrence(ReferenceFrame,DecompressedFrame,ROI):
    ref_BGR = cv2.cvtColor(ReferenceFrame, cv2.COLOR_YCrCb2BGR)
    dec_BGR = cv2.cvtColor(DecompressedFrame, cv2.COLOR_YCrCb2BGR)
    ref_RGB = cv2.cvtColor(ref_BGR, cv2.COLOR_BGR2RGB)
    dec_RGB = cv2.cvtColor(dec_BGR, cv2.COLOR_BGR2RGB)
    
    ref_roi = ref_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_roi = dec_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_rgb = np.abs(ref_roi.astype(float) - dec_roi.astype(float))

    ref_ycrcb_roi = ReferenceFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_ycrcb_roi = DecompressedFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_ycrcb = np.abs(ref_ycrcb_roi.astype(float) - dec_ycrcb_roi.astype(float))
    
    fig, axs = plt.subplots(7, 3, figsize=(16, 22))
    fig.suptitle("Porównanie klatek (ROI: [{},{}] x [{},{}])".format(ROI[0],ROI[1],ROI[2],ROI[3]), fontsize=14)
    
    axs[0,0].imshow(ref_roi)
    axs[0,0].set_title("Oryginał (RGB)")
    axs[0,1].imshow(diff_rgb)
    axs[0,1].set_title("Różnica |RGB|")
    axs[0,2].imshow(dec_roi)
    axs[0,2].set_title("Dekompresja (RGB)")

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
    print("Diff RGB min:", np.min(diff_rgb), "max:", np.max(diff_rgb))
    print("Diff Y min:", np.min(diff_ycrcb[:,:,0]), "max:", np.max(diff_ycrcb[:,:,0]))
    print("Diff Cb min:", np.min(diff_ycrcb[:,:,2]), "max:", np.max(diff_ycrcb[:,:,2]))
    print("Diff Cr min:", np.min(diff_ycrcb[:,:,1]), "max:", np.max(diff_ycrcb[:,:,1]))


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

plt.show()