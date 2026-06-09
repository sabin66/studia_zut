"""
Zadanie 2 — Badanie skuteczności kompresji z ByteRun (PackBits)
- Tylko wykresy Z kompresją strumieniową (ByteRun)
- Testuje odległości klatek kluczowych: 2, 4, 8, 12, 16, 20
- Analiza opłacalności kompresji luminancji
- Walidacja na drugim filmie (plotDifference + wykresy liniowe)
"""
import cv2
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import time

##############################################################################
######   Konfiguracja       ##################################################
##############################################################################

kat = r'.'
plik = "clip_1.mp4"
plik2 = "clip_2.mp4"

# NAJLEPSZE USTAWIENIA Z ZADANIA 1
subsampling = "4:2:0"
dzielnik = 2

ile_klatek = 20
ROI = [150, 350, 800, 1100]

key_frame_distances = [2, 4, 8, 12, 16, 20]

output_dir = os.path.join(kat, "wyniki_zad2")
os.makedirs(output_dir, exist_ok=True)

##############################################################################
####     ByteRun (PackBits)     ##############################################
##############################################################################

def byterun_encode(data_1d):
    """Kompresja ByteRun (PackBits) dla tablicy 1D int."""
    data = data_1d.flatten().astype(np.int16)
    encoded = []
    i = 0
    n = len(data)

    while i < n:
        run_val = data[i]
        run_len = 1
        while i + run_len < n and data[i + run_len] == run_val and run_len < 128:
            run_len += 1

        if run_len >= 3:
            encoded.append(-(run_len - 1))
            encoded.append(int(run_val))
            i += run_len
        else:
            literals = [int(data[i])]
            i += 1
            while i < n and len(literals) < 128:
                if i + 2 < n and data[i] == data[i + 1] == data[i + 2]:
                    break
                literals.append(int(data[i]))
                i += 1
            encoded.append(len(literals) - 1)
            encoded.extend(literals)

    return encoded


def compressed_size(encoded_list):
    """Rozmiar zakodowanych danych w bajtach (int16 = 2 bajty)."""
    return len(encoded_list) * 2


##############################################################################
####     Funkcje kompresji wideo      ########################################
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
    if subsampling == "4:2:2":   return L[:, ::2]
    elif subsampling == "4:4:0": return L[::2, :]
    elif subsampling == "4:2:0": return L[::2, ::2]
    elif subsampling == "4:1:1": return L[:, ::4]
    elif subsampling == "4:1:0": return L[::2, ::4]
    else: return L

def Chroma_resampling(L, subsampling):
    if subsampling == "4:2:2":   return np.repeat(L, 2, axis=1)
    elif subsampling == "4:4:0": return np.repeat(L, 2, axis=0)
    elif subsampling == "4:2:0":
        L = np.repeat(L, 2, axis=0); return np.repeat(L, 2, axis=1)
    elif subsampling == "4:1:1": return np.repeat(L, 4, axis=1)
    elif subsampling == "4:1:0":
        L = np.repeat(L, 2, axis=0); return np.repeat(L, 4, axis=1)
    else: return L

def frame_image_to_class(frame, sub):
    Frame_class = data()
    Frame_class.Y = frame[:, :, 0].astype(int)
    Frame_class.Cb = Chroma_subsampling(frame[:, :, 2].astype(int), sub)
    Frame_class.Cr = Chroma_subsampling(frame[:, :, 1].astype(int), sub)
    return Frame_class

def frame_layers_to_image(Y, Cr, Cb, sub):
    Cb = Chroma_resampling(Cb, sub)
    Cr = Chroma_resampling(Cr, sub)
    return np.dstack([Y, Cr, Cb]).clip(0, 255).astype(np.uint8)

##############################################################################
####     plotDifference — obraz RGB + warstwy Y/Cb/Cr    #####################
##############################################################################

def plotDiffrence(ReferenceFrame, DecompressedFrame, ROI, save_path=None, title_extra=""):
    ref_BGR = cv2.cvtColor(ReferenceFrame, cv2.COLOR_YCrCb2BGR)
    dec_BGR = cv2.cvtColor(DecompressedFrame, cv2.COLOR_YCrCb2BGR)
    ref_RGB = cv2.cvtColor(ref_BGR, cv2.COLOR_BGR2RGB)
    dec_RGB = cv2.cvtColor(dec_BGR, cv2.COLOR_BGR2RGB)

    ref_rgb_roi = ref_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    dec_rgb_roi = dec_RGB[ROI[0]:ROI[1], ROI[2]:ROI[3]]
    diff_rgb = np.abs(ref_rgb_roi.astype(float) - dec_rgb_roi.astype(float))

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
    if title_extra: main_title += f"\n{title_extra}"
    main_title += f"\nMSE={mse_val:.2f}, PSNR={psnr_val:.2f} dB"
    fig.suptitle(main_title, fontsize=13)

    axs[0,0].imshow(ref_rgb_roi); axs[0,0].set_title("Oryginał (RGB)")
    axs[0,1].imshow(diff_rgb.astype(np.uint8)); axs[0,1].set_title("Różnica |RGB|")
    axs[0,2].imshow(dec_rgb_roi); axs[0,2].set_title("Dekompresja (RGB)")

    axs[1,0].imshow(ref_Y, cmap='gray', vmin=0, vmax=255); axs[1,0].set_title("Oryginał Y")
    axs[1,1].imshow(diff_Y, cmap='hot', vmin=0, vmax=max(np.max(diff_Y),1)); axs[1,1].set_title(f"Różnica Y (max={np.max(diff_Y):.1f})")
    axs[1,2].imshow(dec_Y, cmap='gray', vmin=0, vmax=255); axs[1,2].set_title("Dekompresja Y")

    axs[2,0].imshow(ref_Cb, cmap='gray', vmin=0, vmax=255); axs[2,0].set_title("Oryginał Cb")
    axs[2,1].imshow(diff_Cb, cmap='hot', vmin=0, vmax=max(np.max(diff_Cb),1)); axs[2,1].set_title(f"Różnica Cb (max={np.max(diff_Cb):.1f})")
    axs[2,2].imshow(dec_Cb, cmap='gray', vmin=0, vmax=255); axs[2,2].set_title("Dekompresja Cb")

    axs[3,0].imshow(ref_Cr, cmap='gray', vmin=0, vmax=255); axs[3,0].set_title("Oryginał Cr")
    axs[3,1].imshow(diff_Cr, cmap='hot', vmin=0, vmax=max(np.max(diff_Cr),1)); axs[3,1].set_title(f"Różnica Cr (max={np.max(diff_Cr):.1f})")
    axs[3,2].imshow(dec_Cr, cmap='gray', vmin=0, vmax=255); axs[3,2].set_title("Dekompresja Cr")

    plt.tight_layout()
    if save_path:
        fig.savefig(save_path, dpi=150, bbox_inches='tight')
        print(f"    Zapisano: {save_path}")
        plt.close(fig)
    return mse_val, psnr_val


##############################################################################
####     Przetwarzanie wideo z ByteRun      ##################################
##############################################################################

def process_video_byterun(video_path, sub, div, kfc, max_frames, compress_Y=True):
    """
    Przetwarza wideo TYLKO z ByteRun.
    Zwraca: byterun_info [3, nf], is_keyframe [nf], nf,
            oraz ostatnią klatkę oryginalną i dekompresowaną (do plotDiff).
    """
    cap = cv2.VideoCapture(video_path)
    total_available = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    num_frames = min(max_frames, total_available)

    byterun_info = np.zeros((3, num_frames))
    is_keyframe = np.zeros(num_frames, dtype=bool)

    original_Y_size = None
    KeyFrame = None
    last_original = None
    last_decompressed = None

    for i in range(num_frames):
        ret, frame = cap.read()
        if not ret:
            num_frames = i
            break

        frame_ycrcb = cv2.cvtColor(frame, cv2.COLOR_BGR2YCrCb)
        Frame_class = frame_image_to_class(frame_ycrcb, sub)

        if original_Y_size is None:
            original_Y_size = frame_ycrcb[:, :, 0].size

        if (i % kfc) == 0:
            is_keyframe[i] = True
            KeyFrame = data()
            KeyFrame.Y = Frame_class.Y; KeyFrame.Cb = Frame_class.Cb; KeyFrame.Cr = Frame_class.Cr
            KeyFrame.semi_Y = Frame_class.Y; KeyFrame.semi_Cb = Frame_class.Cb; KeyFrame.semi_Cr = Frame_class.Cr
            cY, cCb, cCr = KeyFrame.Y, KeyFrame.Cb, KeyFrame.Cr
            decompressed = frame_layers_to_image(KeyFrame.semi_Y, KeyFrame.semi_Cr, KeyFrame.semi_Cb, sub)
        else:
            diff_Y = (Frame_class.Y - KeyFrame.semi_Y) // div
            diff_Cb = (Frame_class.Cb - KeyFrame.semi_Cb) // div
            diff_Cr = (Frame_class.Cr - KeyFrame.semi_Cr) // div
            cY, cCb, cCr = diff_Y, diff_Cb, diff_Cr
            Y_dec = KeyFrame.semi_Y + diff_Y * div
            Cb_dec = KeyFrame.semi_Cb + diff_Cb * div
            Cr_dec = KeyFrame.semi_Cr + diff_Cr * div
            decompressed = frame_layers_to_image(Y_dec, Cr_dec, Cb_dec, sub)

        # ByteRun na wszystkich kanałach
        if compress_Y:
            enc_Y = byterun_encode(cY)
            byterun_info[0, i] = (original_Y_size * 2 - compressed_size(enc_Y)) / (original_Y_size * 2)
        else:
            byterun_info[0, i] = 0  # Y bez kompresji strumieniowej

        enc_Cb = byterun_encode(cCb)
        enc_Cr = byterun_encode(cCr)
        byterun_info[1, i] = (original_Y_size * 2 - compressed_size(enc_Cb)) / (original_Y_size * 2)
        byterun_info[2, i] = (original_Y_size * 2 - compressed_size(enc_Cr)) / (original_Y_size * 2)

        last_original = frame_ycrcb
        last_decompressed = decompressed

    cap.release()
    return byterun_info[:, :num_frames], is_keyframe[:num_frames], num_frames, last_original, last_decompressed


def get_frame_before_keyframe(video_path, sub, div, kfc, target_frame_idx):
    """Przetwarza do podanej klatki i zwraca oryginał + dekompresję."""
    cap = cv2.VideoCapture(video_path)
    KeyFrame = None
    original = None
    decompressed = None

    for i in range(target_frame_idx + 1):
        ret, frame = cap.read()
        if not ret:
            break
        frame_ycrcb = cv2.cvtColor(frame, cv2.COLOR_BGR2YCrCb)
        Frame_class = frame_image_to_class(frame_ycrcb, sub)

        if (i % kfc) == 0:
            KeyFrame = data()
            KeyFrame.Y = Frame_class.Y; KeyFrame.Cb = Frame_class.Cb; KeyFrame.Cr = Frame_class.Cr
            KeyFrame.semi_Y = Frame_class.Y; KeyFrame.semi_Cb = Frame_class.Cb; KeyFrame.semi_Cr = Frame_class.Cr
            decompressed = frame_layers_to_image(KeyFrame.semi_Y, KeyFrame.semi_Cr, KeyFrame.semi_Cb, sub)
        else:
            diff_Y = (Frame_class.Y - KeyFrame.semi_Y) // div
            diff_Cb = (Frame_class.Cb - KeyFrame.semi_Cb) // div
            diff_Cr = (Frame_class.Cr - KeyFrame.semi_Cr) // div
            Y_dec = KeyFrame.semi_Y + diff_Y * div
            Cb_dec = KeyFrame.semi_Cb + diff_Cb * div
            Cr_dec = KeyFrame.semi_Cr + diff_Cr * div
            decompressed = frame_layers_to_image(Y_dec, Cr_dec, Cb_dec, sub)

        original = frame_ycrcb

    cap.release()
    return original, decompressed


##############################################################################
####     TEST 1: Odległości klatek kluczowych (tylko ByteRun)   ##############
##############################################################################

def run_tests_for_video(video_name, video_path, roi, generate_plotdiff=True):
    """Uruchamia pełne testy dla jednego filmu."""
    print(f"\n{'='*70}")
    print(f"TESTY ByteRun (PackBits) — {video_name}")
    print(f"Subsampling: {subsampling}, Dzielnik: {dzielnik}")
    print(f"{'='*70}")

    vid_short = video_name.replace('.mp4', '')

    # --- WYKRESY LINIOWE: odległości klatek kluczowych ---
    for kfc in key_frame_distances:
        print(f"\n>>> KeyFrame co {kfc} klatek...")
        t_start = time.time()

        comp_br, is_kf, nf, _, _ = process_video_byterun(
            video_path, subsampling, dzielnik, kfc, ile_klatek, compress_Y=True
        )
        elapsed = time.time() - t_start
        print(f"    Przetworzono {nf} klatek w {elapsed:.1f}s")

        fig, ax = plt.subplots(figsize=(14, 6))
        ax.plot(np.arange(nf), comp_br[0, :] * 100, label='Y (ByteRun)', marker='o', markersize=3)
        ax.plot(np.arange(nf), comp_br[1, :] * 100, label='Cb (ByteRun)', marker='s', markersize=3)
        ax.plot(np.arange(nf), comp_br[2, :] * 100, label='Cr (ByteRun)', marker='^', markersize=3)

        kf_indices = np.where(is_kf)[0]
        for kfi in kf_indices:
            ax.axvline(x=kfi, color='gray', linestyle='--', alpha=0.3)

        ax.legend()
        ax.set_xlabel("Numer klatki")
        ax.set_ylabel("Stopień kompresji [%]")
        ax.set_title(f"ByteRun (PackBits) — {video_name}, sub={subsampling}, div={dzielnik}, KF co {kfc}")
        ax.grid(True, alpha=0.3)

        plt.tight_layout()
        save_path = os.path.join(output_dir, f"byterun_kf{kfc}_{vid_short}_sub{subsampling.replace(':','')}_div{dzielnik}.png")
        fig.savefig(save_path, dpi=150, bbox_inches='tight')
        print(f"    Zapisano: {save_path}")
        plt.close(fig)

    # --- PLOT DIFFERENCE: dla najlepszych ustawień ---
    if generate_plotdiff:
        best_kfc = 4  # wybrane na podstawie analizy
        target = best_kfc - 1
        print(f"\n>>> Generowanie plotDifference dla klatki {target} (KF co {best_kfc})...")

        original, decompressed = get_frame_before_keyframe(
            video_path, subsampling, dzielnik, best_kfc, target
        )

        if original is not None and decompressed is not None:
            pd_path = os.path.join(output_dir, f"plotdiff_{vid_short}_sub{subsampling.replace(':','')}_div{dzielnik}_kf{best_kfc}_klatka{target}.png")
            title = f"{video_name}, sub={subsampling}, div={dzielnik}, KF co {best_kfc}, klatka={target}, ByteRun"
            plotDiffrence(original, decompressed, roi, save_path=pd_path, title_extra=title)

    return


##############################################################################
####     GŁÓWNE URUCHOMIENIE       ###########################################
##############################################################################

# === Film 1 ===
run_tests_for_video(plik, os.path.join(kat, plik), ROI)

# === Opłacalność kompresji Y ===
print(f"\n{'='*70}")
print("ANALIZA: Opłacalność kompresji luminancji (Y) przez ByteRun")
print(f"{'='*70}")

kfc_test = 4

comp_with_Y, is_kf_y, nf_y, _, _ = process_video_byterun(
    os.path.join(kat, plik), subsampling, dzielnik, kfc_test, ile_klatek, compress_Y=True
)
comp_no_Y, _, _, _, _ = process_video_byterun(
    os.path.join(kat, plik), subsampling, dzielnik, kfc_test, ile_klatek, compress_Y=False
)

fig, ax = plt.subplots(figsize=(14, 6))
ax.plot(np.arange(nf_y), comp_with_Y[0, :] * 100, label='Y z ByteRun', linewidth=2)
ax.plot(np.arange(nf_y), comp_no_Y[0, :] * 100, label='Y bez ByteRun', linewidth=2, linestyle='--')
ax.plot(np.arange(nf_y), comp_with_Y[1, :] * 100, label='Cb z ByteRun', linewidth=1, alpha=0.7)
ax.plot(np.arange(nf_y), comp_with_Y[2, :] * 100, label='Cr z ByteRun', linewidth=1, alpha=0.7)

kf_idx = np.where(is_kf_y)[0]
for kfi in kf_idx:
    ax.axvline(x=kfi, color='gray', linestyle='--', alpha=0.3, label='Klatka kluczowa' if kfi == kf_idx[0] else '')

ax.legend()
ax.set_xlabel("Numer klatki")
ax.set_ylabel("Stopień kompresji [%]")
ax.set_title(f"Opłacalność kompresji Y — ByteRun (PackBits)\n{plik}, sub={subsampling}, div={dzielnik}, KF co {kfc_test}")
ax.grid(True, alpha=0.3)
plt.tight_layout()
save_path = os.path.join(output_dir, f"luminancja_byterun_{plik.replace('.mp4','')}.png")
fig.savefig(save_path, dpi=150, bbox_inches='tight')
print(f"Zapisano: {save_path}")
plt.close(fig)

# Statystyki Y
mean_Y_with = np.mean(comp_with_Y[0, :]) * 100
mean_Y_without = np.mean(comp_no_Y[0, :]) * 100
mean_Y_kf = np.mean(comp_with_Y[0, is_kf_y]) * 100
mean_Y_nkf = np.mean(comp_with_Y[0, ~is_kf_y]) * 100 if np.any(~is_kf_y) else 0

print(f"\nŚrednia kompresja Y z ByteRun:    {mean_Y_with:.1f}%")
print(f"Średnia kompresja Y bez ByteRun:  {mean_Y_without:.1f}%")
print(f"Różnica:                          {mean_Y_with - mean_Y_without:.1f} pp")
print(f"Kompresja Y (klatki kluczowe):    {mean_Y_kf:.1f}%")
print(f"Kompresja Y (klatki niekluczowe): {mean_Y_nkf:.1f}%")

# === Film 2 — walidacja ===
video2_path = os.path.join(kat, plik2)
if os.path.exists(video2_path):
    # Znajdź ROI z ruchem dla drugiego filmu
    print(f"\n>>> Szukanie ROI z ruchem dla {plik2}...")
    cap2 = cv2.VideoCapture(video2_path)
    f2_list = []
    for i in range(4):
        ret, f = cap2.read()
        if ret: f2_list.append(f)
    cap2.release()

    if len(f2_list) >= 4:
        diff2 = np.abs(f2_list[3].astype(float) - f2_list[0].astype(float)).mean(axis=2)
        h2, w2 = diff2.shape
        best_mean = 0
        best_roi2 = ROI
        bh2, bw2 = h2 // 4, w2 // 4
        for r in range(4):
            for c in range(4):
                region = diff2[r*bh2:(r+1)*bh2, c*bw2:(c+1)*bw2]
                m = region.mean()
                if m > best_mean:
                    best_mean = m
                    best_roi2 = [r*bh2, (r+1)*bh2, c*bw2, (c+1)*bw2]
        print(f"    ROI dla {plik2}: {best_roi2} (mean_diff={best_mean:.2f})")
    else:
        best_roi2 = ROI

    run_tests_for_video(plik2, video2_path, best_roi2)
else:
    print(f"\n!!! UWAGA: Nie znaleziono '{plik2}' — pomiń walidację")

##############################################################################
####     Podsumowanie        #################################################
##############################################################################

print(f"\n{'='*70}")
print("PODSUMOWANIE — Zadanie 2")
print(f"{'='*70}")
print(f"Film bazowy:      {plik}")
print(f"Film walidacyjny: {plik2}")
print(f"Subsampling:      {subsampling}")
print(f"Dzielnik:         {dzielnik}")
print(f"Metoda kompresji: ByteRun (PackBits)")
print(f"KF distances:     {key_frame_distances}")
print(f"\nWykresy zapisane w: {output_dir}/")
print("GOTOWE!")
