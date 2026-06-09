"""
Zadanie 1 — Badanie jakości dla różnych parametrów (bez RLE/ByteRun)
Testuje 6 trybów subsamplingu × 6 dzielników = 36 kombinacji.
Generuje wykresy plotDifference (RGB obraz + warstwy Y/Cb/Cr) + tabelę podsumowującą.
"""
import cv2
import numpy as np
import matplotlib
matplotlib.use('Agg')  # backend bezokienkowy
import matplotlib.pyplot as plt
import os

##############################################################################
######   Konfiguracja       ##################################################
##############################################################################

kat = r'.'
plik = "clip_1.mp4"
key_frame_counter = 4
target_frame = key_frame_counter - 1    # klatka tuż PRZED następną kluczową

ROI = [150, 350, 800, 1100]             # region z ruchem obiektów

subsamplings = ["4:4:4", "4:2:2", "4:4:0", "4:2:0", "4:1:1", "4:1:0"]
dzielniki = [1, 2, 4, 8, 16, 32]

output_dir = os.path.join(kat, "wyniki_zad1")
os.makedirs(output_dir, exist_ok=True)

##############################################################################
####     Funkcje kompresji     ###############################################
##############################################################################

class data:
    def init(self):
        self.Y = None
        self.Cb = None
        self.Cr = None
        self.semi_Y = None
        self.semi_Cb = None
        self.semi_Cr = None

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
    else:
        return L

def frame_image_to_class(frame, subsampling):
    Frame_class = data()
    Frame_class.Y = frame[:, :, 0].astype(int)
    Frame_class.Cb = Chroma_subsampling(frame[:, :, 2].astype(int), subsampling)
    Frame_class.Cr = Chroma_subsampling(frame[:, :, 1].astype(int), subsampling)
    return Frame_class

def frame_layers_to_image(Y, Cr, Cb, subsampling):
    Cb = Chroma_resampling(Cb, subsampling)
    Cr = Chroma_resampling(Cr, subsampling)
    return np.dstack([Y, Cr, Cb]).clip(0, 255).astype(np.uint8)

def compress_KeyFrame(Frame_class):
    KeyFrame = data()
    KeyFrame.Y = Frame_class.Y
    KeyFrame.Cb = Frame_class.Cb
    KeyFrame.Cr = Frame_class.Cr
    KeyFrame.semi_Y = Frame_class.Y
    KeyFrame.semi_Cb = Frame_class.Cb
    KeyFrame.semi_Cr = Frame_class.Cr
    return KeyFrame

def decompress_KeyFrame(KeyFrame, subsampling):
    Y = KeyFrame.semi_Y
    Cb = KeyFrame.semi_Cb
    Cr = KeyFrame.semi_Cr
    return frame_layers_to_image(Y, Cr, Cb, subsampling)

def compress_not_KeyFrame(Frame_class, KeyFrame, dzielnik):
    Compress_data = data()
    diff_Y = (Frame_class.Y - KeyFrame.semi_Y) // dzielnik
    diff_Cb = (Frame_class.Cb - KeyFrame.semi_Cb) // dzielnik
    diff_Cr = (Frame_class.Cr - KeyFrame.semi_Cr) // dzielnik
    Compress_data.Y = diff_Y
    Compress_data.Cb = diff_Cb
    Compress_data.Cr = diff_Cr
    Compress_data.semi_Y = diff_Y
    Compress_data.semi_Cb = diff_Cb
    Compress_data.semi_Cr = diff_Cr
    return Compress_data

def decompress_not_KeyFrame(Compress_data, KeyFrame, dzielnik, subsampling):
    Y = KeyFrame.semi_Y + Compress_data.semi_Y * dzielnik
    Cb = KeyFrame.semi_Cb + Compress_data.semi_Cb * dzielnik
    Cr = KeyFrame.semi_Cr + Compress_data.semi_Cr * dzielnik
    return frame_layers_to_image(Y, Cr, Cb, subsampling)

##############################################################################
####     plotDifference — obraz RGB + warstwy Y/Cb/Cr    #####################
##############################################################################

def plotDiffrence(ReferenceFrame, DecompressedFrame, ROI, save_path=None, title_extra=""):
    # Konwersja do RGB na potrzeby obrazu
    ref_BGR = cv2.cvtColor(ReferenceFrame, cv2.COLOR_YCrCb2BGR)
    dec_BGR = cv2.cvtColor(DecompressedFrame, cv2.COLOR_YCrCb2BGR)
    ref_RGB = cv2.cvtColor(ref_BGR, cv2.COLOR_BGR2RGB)
    dec_RGB = cv2.cvtColor(dec_BGR, cv2.COLOR_BGR2RGB)

    ref_rgb_roi = ref_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_rgb_roi = dec_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_rgb = np.abs(ref_rgb_roi.astype(float) - dec_rgb_roi.astype(float))

    # Kanały YCbCr — OpenCV YCrCb: 0=Y, 1=Cr, 2=Cb
    ref_ycrcb_roi = ReferenceFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_ycrcb_roi = DecompressedFrame[ROI[0]:ROI[1], ROI[2]:ROI[3]]

    ref_Y  = ref_ycrcb_roi[:,:,0].astype(float)
    ref_Cr = ref_ycrcb_roi[:,:,1].astype(float)
    ref_Cb = ref_ycrcb_roi[:,:,2].astype(float)
    dec_Y  = dec_ycrcb_roi[:,:,0].astype(float)
    dec_Cr = dec_ycrcb_roi[:,:,1].astype(float)
    dec_Cb = dec_ycrcb_roi[:,:,2].astype(float)

    diff_Y  = np.abs(ref_Y - dec_Y)
    diff_Cb = np.abs(ref_Cb - dec_Cb)
    diff_Cr = np.abs(ref_Cr - dec_Cr)

    mse_val = np.mean(diff_rgb ** 2)
    psnr_val = 10 * np.log10(255 ** 2 / mse_val) if mse_val > 0 else float('inf')

    fig, axs = plt.subplots(4, 3, figsize=(16, 14))
    main_title = f"Porównanie klatek (ROI: [{ROI[0]},{ROI[1]}] x [{ROI[2]},{ROI[3]}])"
    if title_extra:
        main_title += f"\n{title_extra}"
    main_title += f"\nMSE={mse_val:.2f}, PSNR={psnr_val:.2f} dB"
    fig.suptitle(main_title, fontsize=13)

    # Wiersz 0: Obraz RGB
    axs[0, 0].imshow(ref_rgb_roi)
    axs[0, 0].set_title("Oryginał (RGB)")
    axs[0, 1].imshow(diff_rgb.astype(np.uint8))
    axs[0, 1].set_title("Różnica |RGB|")
    axs[0, 2].imshow(dec_rgb_roi)
    axs[0, 2].set_title("Dekompresja (RGB)")

    # Wiersz 1: Y (luminancja)
    axs[1, 0].imshow(ref_Y, cmap='gray', vmin=0, vmax=255)
    axs[1, 0].set_title("Oryginał Y")
    axs[1, 1].imshow(diff_Y, cmap='hot', vmin=0, vmax=max(np.max(diff_Y), 1))
    axs[1, 1].set_title("Różnica Y (max={:.1f})".format(np.max(diff_Y)))
    axs[1, 2].imshow(dec_Y, cmap='gray', vmin=0, vmax=255)
    axs[1, 2].set_title("Dekompresja Y")

    # Wiersz 2: Cb (chrominancja niebieska)
    axs[2, 0].imshow(ref_Cb, cmap='gray', vmin=0, vmax=255)
    axs[2, 0].set_title("Oryginał Cb")
    axs[2, 1].imshow(diff_Cb, cmap='hot', vmin=0, vmax=max(np.max(diff_Cb), 1))
    axs[2, 1].set_title("Różnica Cb (max={:.1f})".format(np.max(diff_Cb)))
    axs[2, 2].imshow(dec_Cb, cmap='gray', vmin=0, vmax=255)
    axs[2, 2].set_title("Dekompresja Cb")

    # Wiersz 3: Cr (chrominancja czerwona)
    axs[3, 0].imshow(ref_Cr, cmap='gray', vmin=0, vmax=255)
    axs[3, 0].set_title("Oryginał Cr")
    axs[3, 1].imshow(diff_Cr, cmap='hot', vmin=0, vmax=max(np.max(diff_Cr), 1))
    axs[3, 1].set_title("Różnica Cr (max={:.1f})".format(np.max(diff_Cr)))
    axs[3, 2].imshow(dec_Cr, cmap='gray', vmin=0, vmax=255)
    axs[3, 2].set_title("Dekompresja Cr")

    plt.tight_layout()
    print(f"  MSE={mse_val:.2f}, PSNR={psnr_val:.2f}dB, diff_Y_max={np.max(diff_Y):.0f}, diff_Cb_max={np.max(diff_Cb):.0f}, diff_Cr_max={np.max(diff_Cr):.0f}")

    if save_path:
        fig.savefig(save_path, dpi=150, bbox_inches='tight')
        print(f"  Zapisano: {save_path}")
        plt.close(fig)

    return mse_val, psnr_val

##############################################################################
####     Wczytanie klatek       ##############################################
##############################################################################

print(f"Wczytywanie klatek z {plik}...")
cap = cv2.VideoCapture(os.path.join(kat, plik))
frames_needed = target_frame + 1

raw_frames = []
for i in range(frames_needed):
    ret, frame = cap.read()
    if not ret:
        raise RuntimeError(f"Nie udało się wczytać klatki {i}")
    raw_frames.append(cv2.cvtColor(frame, cv2.COLOR_BGR2YCrCb))
cap.release()
print(f"Wczytano {len(raw_frames)} klatek. Analizowana klatka: {target_frame}")

##############################################################################
####     Główna pętla testowa       ##########################################
##############################################################################

results = []
total = len(subsamplings) * len(dzielniki)
counter = 0

for sub in subsamplings:
    for div in dzielniki:
        counter += 1
        print(f"\n[{counter}/{total}] subsampling={sub}, dzielnik={div}")

        frame0 = raw_frames[0]
        Frame_class_0 = frame_image_to_class(frame0, sub)
        KeyFrame = compress_KeyFrame(Frame_class_0)

        for fi in range(1, target_frame + 1):
            frame_i = raw_frames[fi]
            Frame_class_i = frame_image_to_class(frame_i, sub)

            if (fi % key_frame_counter) == 0:
                KeyFrame = compress_KeyFrame(Frame_class_i)
                Decompressed = decompress_KeyFrame(KeyFrame, sub)
            else:
                Compress_data = compress_not_KeyFrame(Frame_class_i, KeyFrame, div)
                Decompressed = decompress_not_KeyFrame(Compress_data, KeyFrame, div, sub)

        original_frame = raw_frames[target_frame]

        sub_safe = sub.replace(":", "")
        save_name = f"plotdiff_sub{sub_safe}_div{div}_klatka{target_frame}.png"
        save_path = os.path.join(output_dir, save_name)

        title = f"subsampling={sub}, dzielnik={div}, klatka={target_frame}, KeyFrame co {key_frame_counter}"
        mse, psnr = plotDiffrence(original_frame, Decompressed, ROI,
                                   save_path=save_path, title_extra=title)
        results.append((sub, div, mse, psnr))

##############################################################################
####     Tabela podsumowująca       ##########################################
##############################################################################

print("\n" + "=" * 70)
print("PODSUMOWANIE — MSE i PSNR dla wszystkich kombinacji")
print("=" * 70)
print(f"{'Subsampling':<12} {'Dzielnik':<10} {'MSE':<12} {'PSNR [dB]':<12}")
print("-" * 46)
for sub, div, mse, psnr in results:
    print(f"{sub:<12} {div:<10} {mse:<12.2f} {psnr:<12.2f}")

best = min(results, key=lambda x: x[2])
print(f"\n>>> NAJLEPSZA KOMBINACJA: subsampling={best[0]}, dzielnik={best[1]}")
print(f"    MSE={best[2]:.2f}, PSNR={best[3]:.2f} dB")

# Heatmap
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))

mse_matrix = np.zeros((len(subsamplings), len(dzielniki)))
psnr_matrix = np.zeros((len(subsamplings), len(dzielniki)))
for idx, (sub, div, mse, psnr) in enumerate(results):
    si = subsamplings.index(sub)
    di = dzielniki.index(div)
    mse_matrix[si, di] = mse
    psnr_matrix[si, di] = psnr

im1 = ax1.imshow(mse_matrix, cmap='RdYlGn_r', aspect='auto')
ax1.set_xticks(range(len(dzielniki)))
ax1.set_xticklabels(dzielniki)
ax1.set_yticks(range(len(subsamplings)))
ax1.set_yticklabels(subsamplings)
ax1.set_xlabel("Dzielnik")
ax1.set_ylabel("Subsampling")
ax1.set_title("MSE (im niższe, tym lepiej)")
for i in range(len(subsamplings)):
    for j in range(len(dzielniki)):
        ax1.text(j, i, f"{mse_matrix[i,j]:.1f}", ha='center', va='center', fontsize=8,
                 color='white' if mse_matrix[i,j] > np.median(mse_matrix) else 'black')
plt.colorbar(im1, ax=ax1)

im2 = ax2.imshow(psnr_matrix, cmap='RdYlGn', aspect='auto')
ax2.set_xticks(range(len(dzielniki)))
ax2.set_xticklabels(dzielniki)
ax2.set_yticks(range(len(subsamplings)))
ax2.set_yticklabels(subsamplings)
ax2.set_xlabel("Dzielnik")
ax2.set_ylabel("Subsampling")
ax2.set_title("PSNR [dB] (im wyższe, tym lepiej)")
for i in range(len(subsamplings)):
    for j in range(len(dzielniki)):
        ax2.text(j, i, f"{psnr_matrix[i,j]:.1f}", ha='center', va='center', fontsize=8,
                 color='white' if psnr_matrix[i,j] < np.median(psnr_matrix) else 'black')
plt.colorbar(im2, ax=ax2)

fig.suptitle(f"Porównanie jakości — {plik}, klatka {target_frame}, KeyFrame co {key_frame_counter}", fontsize=14)
plt.tight_layout()
summary_path = os.path.join(output_dir, "podsumowanie_jakosc.png")
fig.savefig(summary_path, dpi=150, bbox_inches='tight')
print(f"\nZapisano tabelę podsumowującą: {summary_path}")
plt.close(fig)

print(f"\nWszystkie wykresy zapisane w: {output_dir}/")
print("GOTOWE!")
