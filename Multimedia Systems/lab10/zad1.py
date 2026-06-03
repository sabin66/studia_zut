import cv2
import numpy as np
import matplotlib.pyplot as plt
from skimage.metrics import peak_signal_noise_ratio as psnr
from skimage.metrics import structural_similarity as ssim
import os

def water_mark(img,mask,alpha=0.25):
    assert (img.shape[0]==mask.shape[0]) and (img.shape[1]==mask.shape[1]), "Wrong size"
    if len(img.shape)<3:
        flag=True
        t_img=cv2.cvtColor(img,cv2.COLOR_GRAY2RGBA)
    else:
        flag=False
        t_img=cv2.cvtColor(img,cv2.COLOR_RGB2RGBA)      
    if (mask.dtype==bool):
        t_mask=cv2.cvtColor((mask*255).astype(np.uint8),cv2.COLOR_GRAY2RGBA)
    elif (mask.dtype==np.uint8):
        if len(mask.shape)<3:
            t_mask=cv2.cvtColor((mask).astype(np.uint8),cv2.COLOR_GRAY2RGBA)
        else:
            t_mask=cv2.cvtColor((mask).astype(np.uint8),cv2.COLOR_RGB2RGBA)
    else:
        if len(mask.shape)<3:
            t_mask=cv2.cvtColor((mask*255).astype(np.uint8),cv2.COLOR_GRAY2RGBA)
        else:
            t_mask=cv2.cvtColor((mask*255).astype(np.uint8),cv2.COLOR_RGB2RGBA)
    t_out=cv2.addWeighted(t_img,1,t_mask,alpha,0)
    if flag:
        out=cv2.cvtColor(t_out,cv2.COLOR_RGBA2GRAY)
    else:
        out=cv2.cvtColor(t_out,cv2.COLOR_RGBA2RGB)
    return out


def put_data(img,data,binary_mask=np.uint8(1)):
    assert img.dtype==np.uint8 , "img wrong data type"
    assert binary_mask.dtype==np.uint8, "binary_mask wrong data type"
    un_binary_mask=np.unpackbits(binary_mask)
    if data.dtype!=bool:
        unpacked_data=np.unpackbits(data)
    else:
        unpacked_data=data
    dataspace=img.shape[0]*img.shape[1]*int(np.sum(un_binary_mask))
    assert (dataspace>=unpacked_data.size) , "too much data"
    if dataspace==unpacked_data.size:
        prepered_data=unpacked_data.reshape(img.shape[0],img.shape[1],int(np.sum(un_binary_mask))).astype(np.uint8)
    else:
        prepered_data=np.resize(unpacked_data,(img.shape[0],img.shape[1],int(np.sum(un_binary_mask)))).astype(np.uint8)
    mask=np.full((img.shape[0],img.shape[1]),binary_mask)
    img=np.bitwise_and(img,np.invert(mask))
    bv=0
    for i,b in enumerate(un_binary_mask[::-1]):
        if b:
            temp=prepered_data[:,:,bv]
            temp=np.left_shift(temp,i)
            img=np.bitwise_or(img,temp)
            bv+=1
    return img

def pop_data(img,binary_mask=np.uint8(1),out_shape=None):
    un_binary_mask=np.unpackbits(binary_mask)
    data=np.zeros((img.shape[0],img.shape[1],int(np.sum(un_binary_mask)))).astype(np.uint8)
    bv=0
    for i,b in enumerate(un_binary_mask[::-1]):
        if b:
            mask=np.full((img.shape[0],img.shape[1]),2**i)
            temp=np.bitwise_and(img,mask)           
            data[:,:,bv]=temp[:,:].astype(np.uint8)             
            bv+=1
    if out_shape!=None:
        tmp=np.packbits(data.flatten())        
        tmp=tmp[:np.prod(out_shape)]
        data=tmp.reshape(out_shape)
    return data

def convert_string(filename):
    with open(filename,'r') as f:
        filecontent = f.read()
    a = np.array([ord(c) for c in filecontent], dtype=np.uint8)
    return a

os.makedirs('wyniki', exist_ok=True)

# ZADANIE 1

image1 = cv2.imread('monkey.png')
print(f"Obraz 1 (monkey.png): {image1.shape}, typ: {image1.dtype}")

image2 = np.zeros((512, 512, 3), dtype=np.uint8)
for i in range(512):
    for j in range(512):
        image2[i, j, 0] = int(255 * i / 511)  # B
        image2[i, j, 1] = int(255 * j / 511)  # G
        image2[i, j, 2] = int(255 * (511 - i) / 511)  # R
cv2.imwrite('image2.png', image2)
print(f"Obraz 2 (image2.png): {image2.shape}, typ: {image2.dtype}")

mask_color = cv2.imread('mask.png')
mask_gray = cv2.cvtColor(mask_color, cv2.COLOR_BGR2GRAY)
_, binary_mask_img = cv2.threshold(mask_gray, 127, 255, cv2.THRESH_BINARY)
binary_mask_bool = (binary_mask_img > 0)
cv2.imwrite('binary_mask.png', binary_mask_img)
print(f"Obraz binarny (binary_mask.png): {binary_mask_img.shape}, typ: {binary_mask_img.dtype}")

with open('cytat.txt', 'r') as f:
    cytat = f.read()
print(f"Plik tekstowy (cytat.txt): '{cytat}' ({len(cytat)} znaków)")

fig, axes = plt.subplots(1, 4, figsize=(16, 4))
axes[0].imshow(cv2.cvtColor(image1, cv2.COLOR_BGR2RGB))
axes[0].set_title('Obraz 1 (monkey)')
axes[0].axis('off')
axes[1].imshow(cv2.cvtColor(image2, cv2.COLOR_BGR2RGB))
axes[1].set_title('Obraz 2 (gradient)')
axes[1].axis('off')
axes[2].imshow(binary_mask_img, cmap='gray')
axes[2].set_title('Obraz binarny')
axes[2].axis('off')
axes[3].text(0.5, 0.5, cytat, ha='center', va='center', fontsize=8, wrap=True,
             transform=axes[3].transAxes)
axes[3].set_title('Cytat')
axes[3].axis('off')
plt.tight_layout()
plt.savefig('wyniki/zad1_dane_testowe.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad1_dane_testowe.png")

# ZADANIE 2

text_data = convert_string('cytat.txt')
text_len = len(text_data)
print(f"Długość danych tekstowych: {text_len} bajtów")

B_orig = image1[:,:,0].copy()
G_orig = image1[:,:,1].copy()
R_orig = image1[:,:,2].copy()

B_encoded = put_data(B_orig.copy(), text_data, np.uint8(1))

recovered_data = pop_data(B_encoded, np.uint8(1), out_shape=(text_len,))
recovered_text = ''.join([chr(b) for b in recovered_data])

print(f"Tekst oryginalny: '{cytat}'")
print(f"Tekst odzyskany: '{recovered_text}'")
print(f"Zgodność: {cytat == recovered_text}")

psnr_B = psnr(B_orig, B_encoded)
ssim_B = ssim(B_orig, B_encoded)
print(f"PSNR kanału B: {psnr_B:.2f} dB")
print(f"SSIM kanału B: {ssim_B:.6f}")

img_encoded = image1.copy()
img_encoded[:,:,0] = B_encoded

fig, axes = plt.subplots(2, 3, figsize=(15, 10))
axes[0,0].imshow(cv2.cvtColor(image1, cv2.COLOR_BGR2RGB))
axes[0,0].set_title('Obraz oryginalny')
axes[0,0].axis('off')
axes[0,1].imshow(cv2.cvtColor(img_encoded, cv2.COLOR_BGR2RGB))
axes[0,1].set_title('Obraz z ukrytym tekstem')
axes[0,1].axis('off')
diff = np.abs(B_orig.astype(int) - B_encoded.astype(int)).astype(np.uint8)
axes[0,2].imshow(diff * 255, cmap='hot')
axes[0,2].set_title('Różnica kanału B (x255)')
axes[0,2].axis('off')

axes[1,0].imshow(B_orig, cmap='gray')
axes[1,0].set_title('Kanał B - oryginalny')
axes[1,0].axis('off')
axes[1,1].imshow(B_encoded, cmap='gray')
axes[1,1].set_title('Kanał B - po kodowaniu')
axes[1,1].axis('off')
axes[1,2].text(0.5, 0.5, f"PSNR: {psnr_B:.2f} dB\nSSIM: {ssim_B:.6f}\n\nTekst odzyskany poprawnie: {cytat == recovered_text}",
               ha='center', va='center', fontsize=12, transform=axes[1,2].transAxes)
axes[1,2].set_title('Metryki')
axes[1,2].axis('off')
plt.suptitle('Zadanie 2 - Ukrycie tekstu w LSB kanału B', fontsize=14)
plt.tight_layout()
plt.savefig('wyniki/zad2_steganografia_tekst.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad2_steganografia_tekst.png")

# ZADANIE 3

carrier = image1.copy()
secret = image2.copy()

B_carrier = carrier[:,:,0].copy()
G_carrier = carrier[:,:,1].copy()
R_carrier = carrier[:,:,2].copy()

B_secret = secret[:,:,0].copy()
G_secret = secret[:,:,1].copy()
R_secret = secret[:,:,2].copy()

B_bits = np.zeros((512, 512, 3), dtype=bool)
B_bits[:,:,0] = (B_secret & 128) > 0   # bit 7
B_bits[:,:,1] = (B_secret & 64) > 0    # bit 6
B_bits[:,:,2] = (B_secret & 32) > 0    # bit 5

G_bits = np.zeros((512, 512, 2), dtype=bool)
G_bits[:,:,0] = (G_secret & 128) > 0   # bit 7
G_bits[:,:,1] = (G_secret & 64) > 0    # bit 6

R_bits = np.zeros((512, 512, 2), dtype=bool)
R_bits[:,:,0] = (R_secret & 128) > 0   # bit 7
R_bits[:,:,1] = (R_secret & 64) > 0    # bit 6

B_stego = put_data(B_carrier.copy(), B_bits.flatten().astype(bool), np.uint8(7))
G_stego = put_data(G_carrier.copy(), G_bits.flatten().astype(bool), np.uint8(3))
R_stego = put_data(R_carrier.copy(), R_bits.flatten().astype(bool), np.uint8(3))

stego_image = carrier.copy()
stego_image[:,:,0] = B_stego
stego_image[:,:,1] = G_stego
stego_image[:,:,2] = R_stego
cv2.imwrite('wyniki/zad3_stego.png', stego_image)

B_recovered = pop_data(B_stego, np.uint8(7))
G_recovered = pop_data(G_stego, np.uint8(3))
R_recovered = pop_data(R_stego, np.uint8(3))

B_rec_img = np.zeros((512, 512), dtype=np.uint8)
B_rec_img = B_rec_img + ((B_recovered[:,:,0] > 0).astype(np.uint8) * 128)
B_rec_img = B_rec_img + ((B_recovered[:,:,1] > 0).astype(np.uint8) * 64)
B_rec_img = B_rec_img + ((B_recovered[:,:,2] > 0).astype(np.uint8) * 32)

G_rec_img = np.zeros((512, 512), dtype=np.uint8)
G_rec_img = G_rec_img + ((G_recovered[:,:,0] > 0).astype(np.uint8) * 128)
G_rec_img = G_rec_img + ((G_recovered[:,:,1] > 0).astype(np.uint8) * 64)

R_rec_img = np.zeros((512, 512), dtype=np.uint8)
R_rec_img = R_rec_img + ((R_recovered[:,:,0] > 0).astype(np.uint8) * 128)
R_rec_img = R_rec_img + ((R_recovered[:,:,1] > 0).astype(np.uint8) * 64)

recovered_secret = np.zeros_like(secret)
recovered_secret[:,:,0] = B_rec_img
recovered_secret[:,:,1] = G_rec_img
recovered_secret[:,:,2] = R_rec_img
cv2.imwrite('wyniki/zad3_recovered.png', recovered_secret)

print("\nMetryki nosiciela (przed i po ukryciu):")
print(f"  Kanał B - PSNR: {psnr(B_carrier, B_stego):.2f} dB, SSIM: {ssim(B_carrier, B_stego):.6f}")
print(f"  Kanał G - PSNR: {psnr(G_carrier, G_stego):.2f} dB, SSIM: {ssim(G_carrier, G_stego):.6f}")
print(f"  Kanał R - PSNR: {psnr(R_carrier, R_stego):.2f} dB, SSIM: {ssim(R_carrier, R_stego):.6f}")

fig, axes = plt.subplots(2, 4, figsize=(20, 10))
axes[0,0].imshow(cv2.cvtColor(carrier, cv2.COLOR_BGR2RGB))
axes[0,0].set_title('Nosiciel — oryginalny')
axes[0,0].axis('off')
axes[0,1].imshow(cv2.cvtColor(secret, cv2.COLOR_BGR2RGB))
axes[0,1].set_title('Obraz ukrywany')
axes[0,1].axis('off')
axes[0,2].imshow(cv2.cvtColor(stego_image, cv2.COLOR_BGR2RGB))
axes[0,2].set_title('Nosiciel z ukrytym obrazem')
axes[0,2].axis('off')
axes[0,3].imshow(cv2.cvtColor(recovered_secret, cv2.COLOR_BGR2RGB))
axes[0,3].set_title('Obraz odzyskany')
axes[0,3].axis('off')

channels = ['B', 'G', 'R']
orig_channels = [B_carrier, G_carrier, R_carrier]
stego_channels = [B_stego, G_stego, R_stego]
for idx in range(3):
    p = psnr(orig_channels[idx], stego_channels[idx])
    s = ssim(orig_channels[idx], stego_channels[idx])
    axes[1,idx].imshow(np.abs(orig_channels[idx].astype(int) - stego_channels[idx].astype(int)).astype(np.uint8) * 30, cmap='hot')
    axes[1,idx].set_title(f'Różnica {channels[idx]}\nPSNR={p:.2f}, SSIM={s:.4f}')
    axes[1,idx].axis('off')

axes[1,3].text(0.5, 0.5,
    f"PSNR B: {psnr(B_carrier, B_stego):.2f} dB\n"
    f"SSIM B: {ssim(B_carrier, B_stego):.6f}\n\n"
    f"PSNR G: {psnr(G_carrier, G_stego):.2f} dB\n"
    f"SSIM G: {ssim(G_carrier, G_stego):.6f}\n\n"
    f"PSNR R: {psnr(R_carrier, R_stego):.2f} dB\n"
    f"SSIM R: {ssim(R_carrier, R_stego):.6f}",
    ha='center', va='center', fontsize=11, transform=axes[1,3].transAxes,
    family='monospace')
axes[1,3].set_title('Metryki')
axes[1,3].axis('off')

plt.suptitle('Zadanie 3 - Ukrycie kolorowego obrazu w nosicielu\n(B: 3 LSB, R: 2 LSB, G: 2 LSB)', fontsize=14)
plt.tight_layout()
plt.savefig('wyniki/zad3_ukrycie_obrazu.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad3_ukrycie_obrazu.png")

# ZADANIE 4

configs = [
    (1, 0, 0, "1 bit (B:1)"),
    (1, 1, 0, "2 bity (B:1, G:1)"),
    (1, 1, 1, "3 bity (B:1, G:1, R:1)"),
    (2, 1, 1, "4 bity (B:2, G:1, R:1)"),
    (2, 2, 1, "5 bitów (B:2, G:2, R:1)"),
    (2, 2, 2, "6 bitów (B:2, G:2, R:2)"),
    (3, 2, 2, "7 bitów (B:3, G:2, R:2)"),
    (3, 3, 2, "8 bitów (B:3, G:3, R:2)"),
    (3, 3, 3, "9 bitów (B:3, G:3, R:3)"),
    (4, 3, 3, "10 bitów (B:4, G:3, R:3)"),
    (4, 4, 4, "12 bitów (B:4, G:4, R:4)"),
    (5, 5, 5, "15 bitów (B:5, G:5, R:5)"),
    (6, 6, 6, "18 bitów (B:6, G:6, R:6)"),
    (7, 7, 7, "21 bitów (B:7, G:7, R:7)"),
    (8, 8, 8, "24 bity (B:8, G:8, R:8)"),
]

results = []
stego_images_zad4 = []

for bits_b, bits_g, bits_r, desc in configs:
    total_bits = bits_b + bits_g + bits_r
    
    carrier_copy = image1.copy()
    B_ch = carrier_copy[:,:,0].copy()
    G_ch = carrier_copy[:,:,1].copy()
    R_ch = carrier_copy[:,:,2].copy()
    
    if bits_b > 0:
        mask_val_b = np.uint8(2**bits_b - 1)
        random_data_b = np.random.randint(0, 2, size=(512*512*bits_b,)).astype(bool)
        B_ch = put_data(B_ch, random_data_b, mask_val_b)
    if bits_g > 0:
        mask_val_g = np.uint8(2**bits_g - 1)
        random_data_g = np.random.randint(0, 2, size=(512*512*bits_g,)).astype(bool)
        G_ch = put_data(G_ch, random_data_g, mask_val_g)
    if bits_r > 0:
        mask_val_r = np.uint8(2**bits_r - 1)
        random_data_r = np.random.randint(0, 2, size=(512*512*bits_r,)).astype(bool)
        R_ch = put_data(R_ch, random_data_r, mask_val_r)
    
    stego_img = carrier_copy.copy()
    stego_img[:,:,0] = B_ch
    stego_img[:,:,1] = G_ch
    stego_img[:,:,2] = R_ch
    stego_images_zad4.append((stego_img.copy(), desc, total_bits))
    
    p_b = psnr(image1[:,:,0], B_ch) if bits_b > 0 else float('inf')
    p_g = psnr(image1[:,:,1], G_ch) if bits_g > 0 else float('inf')
    p_r = psnr(image1[:,:,2], R_ch) if bits_r > 0 else float('inf')
    
    s_b = ssim(image1[:,:,0], B_ch) if bits_b > 0 else 1.0
    s_g = ssim(image1[:,:,1], G_ch) if bits_g > 0 else 1.0
    s_r = ssim(image1[:,:,2], R_ch) if bits_r > 0 else 1.0
    
    p_total = psnr(image1, stego_img)
    s_total = ssim(image1, stego_img, channel_axis=2)
    
    results.append({
        'desc': desc,
        'total_bits': total_bits,
        'psnr_b': p_b, 'psnr_g': p_g, 'psnr_r': p_r,
        'ssim_b': s_b, 'ssim_g': s_g, 'ssim_r': s_r,
        'psnr_total': p_total, 'ssim_total': s_total
    })
    
    print(f"  {desc}: PSNR={p_total:.2f} dB, SSIM={s_total:.6f}")

print("\n--- Tabela wyników ---")
print(f"{'Konfiguracja':<30} {'Bity':>5} {'PSNR [dB]':>10} {'SSIM':>10}")
print("-" * 60)
for r in results:
    psnr_str = f"{r['psnr_total']:.2f}" if r['psnr_total'] != float('inf') else "inf"
    print(f"{r['desc']:<30} {r['total_bits']:>5} {psnr_str:>10} {r['ssim_total']:.6f}")

selected_indices = [0, 2, 5, 8, 11, 13, 14]
fig, axes = plt.subplots(2, 4, figsize=(20, 10))
axes_flat = axes.flatten()

axes_flat[0].imshow(cv2.cvtColor(image1, cv2.COLOR_BGR2RGB))
axes_flat[0].set_title('Oryginalny')
axes_flat[0].axis('off')

for plot_idx, sel_idx in enumerate(selected_indices):
    img, desc, total = stego_images_zad4[sel_idx]
    r = results[sel_idx]
    axes_flat[plot_idx+1].imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    axes_flat[plot_idx+1].set_title(f'{desc}\nPSNR={r["psnr_total"]:.1f}, SSIM={r["ssim_total"]:.4f}', fontsize=9)
    axes_flat[plot_idx+1].axis('off')

plt.suptitle('Zadanie 4 - Wpływ budżetu bitowego na jakość nosiciela', fontsize=14)
plt.tight_layout()
plt.savefig('wyniki/zad4_budzet_bitowy.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad4_budzet_bitowy.png")

fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))
total_bits_list = [r['total_bits'] for r in results]
psnr_list = [r['psnr_total'] for r in results]
ssim_list = [r['ssim_total'] for r in results]

ax1.plot(total_bits_list, psnr_list, 'bo-', linewidth=2, markersize=6)
ax1.set_xlabel('Łączna liczba bitów')
ax1.set_ylabel('PSNR [dB]')
ax1.set_title('PSNR vs budżet bitowy')
ax1.grid(True, alpha=0.3)
ax1.axhline(y=30, color='r', linestyle='--', label='Próg 30 dB')
ax1.legend()

ax2.plot(total_bits_list, ssim_list, 'ro-', linewidth=2, markersize=6)
ax2.set_xlabel('Łączna liczba bitów')
ax2.set_ylabel('SSIM')
ax2.set_title('SSIM vs budżet bitowy')
ax2.grid(True, alpha=0.3)
ax2.axhline(y=0.95, color='b', linestyle='--', label='Próg 0.95')
ax2.legend()

plt.suptitle('Zadanie 4 - Zależność metryk od budżetu bitowego', fontsize=14)
plt.tight_layout()
plt.savefig('wyniki/zad4_wykresy.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad4_wykresy.png")

# ZADANIE 5

alphas = [0.10, 0.25, 0.50]

fig, axes = plt.subplots(2, 4, figsize=(20, 10))

axes[0,0].imshow(cv2.cvtColor(image1, cv2.COLOR_BGR2RGB))
axes[0,0].set_title('Obraz oryginalny')
axes[0,0].axis('off')

axes[1,0].imshow(binary_mask_img, cmap='gray')
axes[1,0].set_title('Maska binarna (znak wodny)')
axes[1,0].axis('off')

print("\nWyniki znakowania wodnego:")
print(f"{'Alpha':>8} {'PSNR [dB]':>10} {'SSIM':>10}")
print("-" * 32)

watermark_results = []
for idx, alpha in enumerate(alphas):
    wm_img = water_mark(image1, binary_mask_img, alpha)
    cv2.imwrite(f'wyniki/zad5_watermark_a{alpha}.png', wm_img)
    
    p = psnr(image1, wm_img)
    s = ssim(image1, wm_img, channel_axis=2)
    watermark_results.append({'alpha': alpha, 'psnr': p, 'ssim': s})
    
    print(f"{alpha:>8.2f} {p:>10.2f} {s:>10.6f}")
    
    axes[0,idx+1].imshow(cv2.cvtColor(wm_img, cv2.COLOR_BGR2RGB))
    axes[0,idx+1].set_title(f'α = {alpha}\nPSNR={p:.2f}, SSIM={s:.4f}')
    axes[0,idx+1].axis('off')
    
    diff_wm = np.abs(image1.astype(int) - wm_img.astype(int)).astype(np.uint8)
    axes[1,idx+1].imshow(cv2.cvtColor(diff_wm * 5, cv2.COLOR_BGR2RGB))
    axes[1,idx+1].set_title(f'Różnica (x5), α = {alpha}')
    axes[1,idx+1].axis('off')

plt.suptitle('Zadanie 5 - Znakowanie wodne przy różnych α', fontsize=14)
plt.tight_layout()
plt.savefig('wyniki/zad5_watermark.png', dpi=150, bbox_inches='tight')
plt.close()
print("Zapisano: wyniki/zad5_watermark.png")

print("\n" + "="*60)
print("PODSUMOWANIE")
print("="*60)
print("Wszystkie wyniki zapisano w katalogu 'wyniki/'.")
print("Pliki wynikowe:")
for f in os.listdir('wyniki'):
    print(f"  - wyniki/{f}")

if os.path.exists('page.html'):
    os.remove('page.html')
